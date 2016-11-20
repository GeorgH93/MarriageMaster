/*
 *   Copyright (C) 2014-2016 GeorgH93
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.MarriageMaster.Bukkit.Listener;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Marry_Requests;

public class JoinLeaveChat implements Listener 
{
	private static final String HEART = "\u2764";
	private static final String HEART_GRAY = ChatColor.GRAY + HEART + ChatColor.WHITE, HEART_RED = ChatColor.RED + HEART + ChatColor.WHITE;

	private MarriageMaster plugin;
	private String prefix = null, suffix = null;
	private int delay = 0;
	private boolean useSurname, changeChatFormat, prefixOnLineBeginning, magicHeart, statusHeart, onJoinInfo;
	private char[] chatColors = new char[]{ '0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f' };
	private Map<String, ChatColor> colorMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	private Random random = new Random();

	public JoinLeaveChat(MarriageMaster marriagemaster) 
	{
		plugin = marriagemaster;
		if(plugin.config.UsePrefix() && plugin.config.GetPrefix() != null)
		{
			prefix = ChatColor.translateAlternateColorCodes('&', plugin.config.GetPrefix()).replace("<heart>", HEART_RED);
			magicHeart = prefix.contains("<magicheart>");
			statusHeart = prefix.contains("<statusheart>");
		}
		if(plugin.config.UseSuffix() && plugin.config.GetSuffix() != null)
		{
			suffix = ChatColor.translateAlternateColorCodes('&', plugin.config.GetSuffix()).replace("<heart>", HEART_RED);
		}
		delay = plugin.config.getDelayMessageForJoiningPlayer() * 20 + 1;
		useSurname = plugin.config.getSurname();
		prefixOnLineBeginning = plugin.config.GetPrefixOnLineBeginning();
		changeChatFormat = prefix != null | suffix != null | useSurname;
		onJoinInfo = plugin.config.GetInformOnPartnerJoinEnabled();
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerLoginEvent(PlayerJoinEvent event)
	{
		if(onJoinInfo)
		{
			final String partner = plugin.DB.GetPartner(event.getPlayer());
			if(partner != null)
			{
				if(magicHeart && !colorMap.containsKey(event.getPlayer().getName()))
				{
					ChatColor color = ChatColor.getByChar(chatColors[random.nextInt(chatColors.length)]);
					colorMap.put(event.getPlayer().getName(), color);
					colorMap.put(partner, color);
				}
				Player otherPlayer = plugin.getServer().getPlayerExact(partner);
				final Player sender = event.getPlayer();
				plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
					@Override
					public void run()
					{
						Player otherPlayer = plugin.getServer().getPlayerExact(partner);
						if(otherPlayer != null && otherPlayer.isOnline())
						{
							sender.sendMessage(ChatColor.GREEN + plugin.lang.get("Ingame.PartnerOnline"));
						}
						else
						{
							sender.sendMessage(ChatColor.GREEN + plugin.lang.get("Ingame.PartnerOffline"));
						}
					}
				}, delay);
				if(otherPlayer != null && otherPlayer.isOnline())
				{
					otherPlayer.sendMessage(ChatColor.GREEN + plugin.lang.get("Ingame.PartnerNowOnline"));
				}
			}
		}
		plugin.DB.UpdatePlayer(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerChatPrivate(AsyncPlayerChatEvent event)
	{
		Player player = event.getPlayer();
		if(plugin.chat.Marry_ChatDirect.contains(player))
		{
			Player partner = plugin.DB.GetPlayerPartner(player);
			if(partner != null)
			{
				plugin.chat.Chat(player, partner, event.getMessage());
				event.setCancelled(true);
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerChat(AsyncPlayerChatEvent event)
	{
		if(!changeChatFormat) return;
		Player player = event.getPlayer();
		String partner = plugin.DB.GetPartner(player);
		if(partner != null && !partner.isEmpty())
		{
			String format = event.getFormat(), formatReplacer = "%1$s";
			boolean changed = false;
			if(prefix != null)
			{
				if(prefixOnLineBeginning)
				{
					format = prefix.replace("<partnername>", partner) + ' ' + format;
				}
				else
				{
					formatReplacer = prefix + ' ' + formatReplacer;
				}
				changed = true;
			}
			if(useSurname)
			{
				String surname = plugin.DB.GetSurname(event.getPlayer());
				if(surname != null && !surname.isEmpty())
				{
					formatReplacer += ' ' + surname;
					changed = true;
				}
			}
			if(suffix != null)
			{
				formatReplacer += ' ' + suffix;
				changed = true;
			}
			if(changed)
			{
				event.setFormat(format.replace("%1$s", formatReplacer.replace("<partnername>", partner)).replace("<magicheart>", getMagicHeart(player))
						                .replace("<statusheart>", HEART_RED));
			}
		}
		else
		{
			if(statusHeart)
			{
				if(prefixOnLineBeginning)
				{
					event.setFormat(HEART_GRAY + ' ' + event.getFormat());
				}
				else
				{
					event.setFormat(event.getFormat().replace("%1$s", HEART_GRAY + " %1$s"));
				}
			}
		}
	}

	public String getMagicHeart(Player player)
	{
		if(!magicHeart) return "";
		ChatColor color = colorMap.get(player.getName());
		if(color == null) return "";
		return color + HEART + ChatColor.WHITE;
	}

	@EventHandler
	public void PlayerLeaveEvent(PlayerQuitEvent event)
	{
		if(onJoinInfo)
		{
			Player otherPlayer = plugin.DB.GetPlayerPartner(event.getPlayer());
			if(otherPlayer != null && otherPlayer.isOnline())
			{
				otherPlayer.sendMessage(ChatColor.GREEN + plugin.lang.get("Ingame.PartnerNowOffline"));
			}
		}
		plugin.chat.pcl.remove(event.getPlayer());
		plugin.chat.Marry_ChatDirect.remove(event.getPlayer());
		playerWentOffline(plugin.mr.iterator(), event.getPlayer());
		if(!plugin.config.getConfirmationBothDivorce())
		{
			Iterator<Entry<Player, Player>> d = plugin.dr.entrySet().iterator();
			Entry<Player, Player> e;
			while(d.hasNext())
			{
				e = d.next();
				if(event.getPlayer().equals(e.getKey()))
				{
					e.getValue().sendMessage(String.format(plugin.lang.get("Priest.DivPlayerOff"), e.getKey().getName()));
					d.remove();
				}
				else if(event.getPlayer().equals(e.getValue()))
				{
					e.getKey().sendMessage(String.format(plugin.lang.get("Priest.DivPriestOff"), e.getValue().getName()));
					d.remove();
				}
			}
		}
		else
		{
			playerWentOffline(plugin.bdr.iterator(), event.getPlayer());
		}
	}

	public void playerWentOffline(Iterator<Marry_Requests> m, Player player)
	{
		Marry_Requests temp;
		while (m.hasNext())
		{
			temp = m.next();
			if(temp.p1.equals(player))
			{
				if(temp.priest != null)
				{
					temp.priest.sendMessage(String.format(plugin.lang.get("Ingame.PlayerMarryOff"), temp.p1.getName()));
				}
				temp.p2.sendMessage(String.format(plugin.lang.get("Ingame.PlayerMarryOff"), temp.p1.getName()));
				m.remove();
			}
			else if(temp.p2.equals(player))
			{
				if(temp.priest != null)
				{
					temp.priest.sendMessage(String.format(plugin.lang.get("Ingame.PlayerMarryOff"), temp.p2.getName()));
				}
				temp.p1.sendMessage(String.format(plugin.lang.get("Ingame.PlayerMarryOff"), temp.p2.getName()));
				m.remove();
			}
			else if(temp.priest != null && temp.priest.equals(player))
			{
				temp.p1.sendMessage(String.format(plugin.lang.get("Ingame.PriestMarryOff"), temp.priest.getName()));
				temp.p2.sendMessage(String.format(plugin.lang.get("Ingame.PriestMarryOff"), temp.priest.getName()));
				m.remove();
			}
		}
	}
}