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
import java.util.Map.Entry;

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
	private MarriageMaster plugin;
	private String prefix = null, suffix = null;
	private int delay = 0;
	private boolean useSurname = false, changeChatFormat = false, prefixOnLineBeginning;

	public JoinLeaveChat(MarriageMaster marriagemaster) 
	{
		plugin = marriagemaster;
		if(plugin.config.usePrefix() && plugin.config.getPrefix() != null)
		{
			prefix = ChatColor.translateAlternateColorCodes('&', plugin.config.getPrefix()).replace("<heart>", ChatColor.RED + "\u2764" + ChatColor.WHITE);
		}
		if(plugin.config.UseSuffix() && plugin.config.GetSuffix() != null)
		{
			suffix = ChatColor.translateAlternateColorCodes('&', plugin.config.GetSuffix()).replace("<heart>", ChatColor.RED + "\u2764" + ChatColor.WHITE);
		}
		delay = plugin.config.getDelayMessageForJoiningPlayer() * 20 + 1;
		useSurname = plugin.config.getSurname();
		prefixOnLineBeginning = plugin.config.getPrefixOnLineBeginning();
		changeChatFormat = prefix != null | suffix != null | useSurname;
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerLoginEvent(PlayerJoinEvent event)
	{
		if(plugin.config.GetInformOnPartnerJoinEnabled())
		{
			final String partner = plugin.DB.GetPartner(event.getPlayer());
			if(partner != null)
			{
				Player otherPlayer = plugin.getServer().getPlayerExact(partner);
				final Player sender = event.getPlayer();
				plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
					@Override
					public void run()
					{
						Player otherPlayer = plugin.getServer().getPlayerExact(partner);
						if(otherPlayer != null && otherPlayer.isOnline())
						{
							sender.sendMessage(ChatColor.GREEN + plugin.lang.Get("Ingame.PartnerOnline"));
						}
						else
						{
							sender.sendMessage(ChatColor.GREEN + plugin.lang.Get("Ingame.PartnerOffline"));
						}
					}
				}, delay);
				if(otherPlayer != null && otherPlayer.isOnline())
				{
					otherPlayer.sendMessage(ChatColor.GREEN + plugin.lang.Get("Ingame.PartnerNowOnline"));
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
				event.setFormat(format.replace("%1$s", formatReplacer.replace("<partnername>", partner)));
			}
		}
	}

	@EventHandler
	public void PlayerLeaveEvent(PlayerQuitEvent event)
	{
		if(plugin.config.GetInformOnPartnerJoinEnabled())
		{
			Player otherPlayer = plugin.DB.GetPlayerPartner(event.getPlayer());
			if(otherPlayer != null && otherPlayer.isOnline())
			{
				otherPlayer.sendMessage(ChatColor.GREEN + plugin.lang.Get("Ingame.PartnerNowOffline"));
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
					e.getValue().sendMessage(String.format(plugin.lang.Get("Priest.DivPlayerOff"), e.getKey().getName()));
					d.remove();
				}
				else if(event.getPlayer().equals(e.getValue()))
				{
					e.getKey().sendMessage(String.format(plugin.lang.Get("Priest.DivPriestOff"), e.getValue().getName()));
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
					temp.priest.sendMessage(String.format(plugin.lang.Get("Ingame.PlayerMarryOff"), temp.p1.getName()));
				}
				temp.p2.sendMessage(String.format(plugin.lang.Get("Ingame.PlayerMarryOff"), temp.p1.getName()));
				m.remove();
			}
			else if(temp.p2.equals(player))
			{
				if(temp.priest != null)
				{
					temp.priest.sendMessage(String.format(plugin.lang.Get("Ingame.PlayerMarryOff"), temp.p2.getName()));
				}
				temp.p1.sendMessage(String.format(plugin.lang.Get("Ingame.PlayerMarryOff"), temp.p2.getName()));
				m.remove();
			}
			else if(temp.priest != null && temp.priest.equals(player))
			{
				temp.p1.sendMessage(String.format(plugin.lang.Get("Ingame.PriestMarryOff"), temp.priest.getName()));
				temp.p2.sendMessage(String.format(plugin.lang.Get("Ingame.PriestMarryOff"), temp.priest.getName()));
				m.remove();
			}
		}
	}
}