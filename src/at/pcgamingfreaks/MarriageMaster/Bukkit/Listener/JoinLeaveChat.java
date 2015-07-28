/*
 *   Copyright (C) 2014-2015 GeorgH93
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
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Marry_Requests;

public class JoinLeaveChat implements Listener 
{
	private MarriageMaster plugin;
	private String prefix = null;

	public JoinLeaveChat(MarriageMaster marriagemaster) 
	{
		plugin = marriagemaster;
		if(plugin.config.UsePrefix() && plugin.config.GetPrefix() != null)
		{
			prefix = ChatColor.translateAlternateColorCodes('&', plugin.config.GetPrefix()).replace("<heart>", ChatColor.RED + "\u2764" + ChatColor.WHITE);
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void PlayerLoginEvent(PlayerJoinEvent event) 
	{
		if(plugin.config.GetInformOnPartnerJoinEnabled())
		{
			String partner = plugin.DB.GetPartner(event.getPlayer());
			if(partner != null)
			{
				Player otherPlayer = plugin.getServer().getPlayer(partner);
				if(otherPlayer != null && otherPlayer.isOnline())
				{
					event.getPlayer().sendMessage(ChatColor.GREEN + plugin.lang.Get("Ingame.PartnerOnline"));
					otherPlayer.sendMessage(ChatColor.GREEN + plugin.lang.Get("Ingame.PartnerNowOnline"));
				}
				else
				{
					event.getPlayer().sendMessage(ChatColor.GREEN + plugin.lang.Get("Ingame.PartnerOffline"));
				}
			}
		}
		plugin.DB.UpdatePlayer(event.getPlayer());
		if(plugin.HomeServer == null && plugin.pluginchannel != null)
		{
			plugin.pluginchannel.requestServerName();
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event)
	{
		Player player = event.getPlayer();
		String partner = plugin.DB.GetPartner(player);
		if(partner != null && !partner.isEmpty())
		{
			if(plugin.chat.Marry_ChatDirect.contains(player))
			{
				Player otP = plugin.getServer().getPlayerExact(partner);
				plugin.chat.Chat(player, otP, event.getMessage());
				event.setCancelled(true);
			}
			else
			{
				String format = event.getFormat();
				if(prefix != null)
				{
					format = prefix.replace("<partnername>", partner) + " " + format;
				}
				if(plugin.config.getSurname())
				{
					String Surname = plugin.DB.GetSurname(event.getPlayer());
					if(Surname != null && !Surname.isEmpty())
					{
						format = format.replace("%1$s", "%1$s " + Surname);
					}
				}
				event.setFormat(format);
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
		Iterator<Marry_Requests> m = plugin.mr.iterator();
		Marry_Requests temp;
		while (m.hasNext())
		{
			temp = m.next();
			if(temp.p1 == event.getPlayer())
			{
				if(temp.priest != null)
				{
					temp.priest.sendMessage(String.format(plugin.lang.Get("Ingame.PlayerMarryOff"), temp.p1.getName()));
				}
				temp.p2.sendMessage(String.format(plugin.lang.Get("Ingame.PlayerMarryOff"), temp.p1.getName()));
				m.remove();
			}
			else if(temp.p2 == event.getPlayer())
			{
				if(temp.priest != null)
				{
					temp.priest.sendMessage(String.format(plugin.lang.Get("Ingame.PlayerMarryOff"), temp.p2.getName()));
				}
				temp.p1.sendMessage(String.format(plugin.lang.Get("Ingame.PlayerMarryOff"), temp.p2.getName()));
				m.remove();
			}
			else if(temp.priest != null && temp.priest == event.getPlayer())
			{
				temp.p1.sendMessage(String.format(plugin.lang.Get("Ingame.PriestMarryOff"), temp.priest.getName()));
				temp.p2.sendMessage(String.format(plugin.lang.Get("Ingame.PriestMarryOff"), temp.priest.getName()));
				m.remove();
			}
		}
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
			m = plugin.bdr.iterator();
			while (m.hasNext())
			{
				temp = m.next();
				if(temp.p1 == event.getPlayer())
				{
					if(temp.priest != null)
					{
						temp.priest.sendMessage(String.format(plugin.lang.Get("Ingame.PlayerMarryOff"), temp.p1.getName()));
					}
					temp.p2.sendMessage(String.format(plugin.lang.Get("Ingame.PlayerMarryOff"), temp.p1.getName()));
					m.remove();
				}
				else if(temp.p2 == event.getPlayer())
				{
					if(temp.priest != null)
					{
						temp.priest.sendMessage(String.format(plugin.lang.Get("Ingame.PlayerMarryOff"), temp.p2.getName()));
					}
					temp.p1.sendMessage(String.format(plugin.lang.Get("Ingame.PlayerMarryOff"), temp.p2.getName()));
					m.remove();
				}
				else if(temp.priest != null && temp.priest == event.getPlayer())
				{
					temp.p1.sendMessage(String.format(plugin.lang.Get("Ingame.PriestMarryOff"), temp.priest.getName()));
					temp.p2.sendMessage(String.format(plugin.lang.Get("Ingame.PriestMarryOff"), temp.priest.getName()));
					m.remove();
				}
			}
		}
	}
}
