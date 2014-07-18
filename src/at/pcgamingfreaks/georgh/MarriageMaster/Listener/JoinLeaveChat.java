/*
 *   Copyright (C) 2014 GeorgH93
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

package at.pcgamingfreaks.georgh.MarriageMaster.Listener;

import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import at.pcgamingfreaks.georgh.MarriageMaster.MarriageMaster;
import at.pcgamingfreaks.georgh.MarriageMaster.Marry_Requests;

public class JoinLeaveChat implements Listener 
{
	private MarriageMaster marriageMaster;
	private String prefix = null;

	public JoinLeaveChat(MarriageMaster marriagemaster) 
	{
		marriageMaster = marriagemaster;
		if(marriageMaster.config.UsePrefix())
		{
			prefix = marriageMaster.config.GetPrefix().replace("<heart>", ChatColor.RED + "\u2764" + ChatColor.WHITE);
		}
	}
	
	@EventHandler
	public void PlayerLoginEvent(PlayerJoinEvent event) 
	{
		if(marriageMaster.config.GetInformOnPartnerJoinEnabled())
		{
			String otherPlayer = marriageMaster.DB.GetPartner(event.getPlayer());
			if(otherPlayer != null && !otherPlayer.isEmpty())
			{
				Player oPlayer = marriageMaster.getServer().getPlayer(otherPlayer);
				
				if(oPlayer != null)
				{
					event.getPlayer().sendMessage(ChatColor.GREEN + marriageMaster.lang.Get("Ingame.PartnerOnline"));
					oPlayer.sendMessage(ChatColor.GREEN + marriageMaster.lang.Get("Ingame.PartnerNowOnline"));
				}
				else
				{
					event.getPlayer().sendMessage(ChatColor.GREEN + marriageMaster.lang.Get("Ingame.PartnerOffline"));
				}
			}
		}
		if(marriageMaster.config.UseUUIDs())
		{
			marriageMaster.DB.UpdatePlayer(event.getPlayer());
		}
	}
	
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event)
	{
		Player player = event.getPlayer();
		String partner = marriageMaster.DB.GetPartner(player);
		if(marriageMaster.chat.Marry_ChatDirect.contains(player))
		{
			Player otP = marriageMaster.getServer().getPlayer(partner);
			marriageMaster.chat.Chat(player, otP, event.getMessage());
			event.setCancelled(true);
		}
		else if(partner != null && !partner.isEmpty())
		{
			String format = event.getFormat();
			if(prefix != null)
			{
				format = prefix.replace("<partnername>", partner) + " " + format;
			}
			if(marriageMaster.config.getSurname())
			{
				String Surname = marriageMaster.DB.GetSurname(event.getPlayer());
				if(Surname != null && !Surname.isEmpty())
				{
					format = format.replace("%1$s", "%1$s " + Surname);
				}
			}
			event.setFormat(format);
		}
	}

	@EventHandler
	public void PlayerLeaveEvent(PlayerQuitEvent event)
	{
		if(marriageMaster.config.GetInformOnPartnerJoinEnabled())
		{
			String otherPlayer = marriageMaster.DB.GetPartner(event.getPlayer());
			
			if(otherPlayer != null)
			{
				Player oPlayer = marriageMaster.getServer().getPlayer(otherPlayer);
				
				if(oPlayer != null)
				{
					oPlayer.sendMessage(ChatColor.GREEN + marriageMaster.lang.Get("Ingame.PartnerNowOffline"));
				}
			}
		}
		marriageMaster.chat.pcl.remove(event.getPlayer());
		marriageMaster.chat.Marry_ChatDirect.remove(event.getPlayer());
		Iterator<Marry_Requests> m = marriageMaster.mr.iterator();
		Marry_Requests temp;
		while (m.hasNext())
		{
			temp = m.next();
			if(temp.p1 == event.getPlayer())
			{
				m.remove();
				temp.priest.sendMessage(String.format(marriageMaster.lang.Get("Ingame.PlayerMarryOff"), temp.p1.getName()));
				temp.p2.sendMessage(String.format(marriageMaster.lang.Get("Ingame.PlayerMarryOff"), temp.p1.getName()));
			}
			else if(temp.p2 == event.getPlayer())
			{
				m.remove();
				temp.priest.sendMessage(String.format(marriageMaster.lang.Get("Ingame.PlayerMarryOff"), temp.p2.getName()));
				temp.p1.sendMessage(String.format(marriageMaster.lang.Get("Ingame.PlayerMarryOff"), temp.p2.getName()));
			}
			else if(temp.priest != null && temp.priest == event.getPlayer())
			{
				m.remove();
				temp.p1.sendMessage(String.format(marriageMaster.lang.Get("Ingame.PriestMarryOff"), temp.priest.getName()));
				temp.p2.sendMessage(String.format(marriageMaster.lang.Get("Ingame.PriestMarryOff"), temp.priest.getName()));
			}
		}
		Iterator<Entry<Player, Player>> d = marriageMaster.dr.entrySet().iterator();
		Entry<Player, Player> e;
		while(d.hasNext())
		{
			e = d.next();
			if(event.getPlayer().equals(e.getKey()))
			{
				e.getValue().sendMessage(String.format(marriageMaster.lang.Get("Priest.DivPlayerOff"), e.getKey().getName()));
				d.remove();
			}
			else if(event.getPlayer().equals(e.getValue()))
			{
				e.getKey().sendMessage(String.format(marriageMaster.lang.Get("Priest.DivPriestOff"), e.getValue().getName()));
				d.remove();
			}
		}
	}
}
