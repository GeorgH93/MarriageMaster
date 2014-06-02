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

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import at.pcgamingfreaks.georgh.MarriageMaster.MarriageMaster;
import at.pcgamingfreaks.georgh.MarriageMaster.Marry_Requests;

public class JoinLeave implements Listener 
{
	private MarriageMaster marriageMaster;

	public JoinLeave(MarriageMaster marriagemaster) 
	{
		marriageMaster = marriagemaster;
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
		else if(marriageMaster.config.UsePrefix() && partner != null && !partner.isEmpty())
		{
			event.setFormat(marriageMaster.config.GetPrefix().replace("<heart>", ChatColor.RED + "\u2764" + ChatColor.WHITE).replace("<partnername>", marriageMaster.DB.GetPartner(event.getPlayer())) + " " + event.getFormat());
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
	}
}
