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
		if(marriageMaster.config.UsePrefix() && marriageMaster.HasPartner(event.getPlayer()))
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
		marriageMaster.pcl.remove(event.getPlayer());
		for (Marry_Requests m : marriageMaster.mr)
		{
			if(m.p1 == event.getPlayer())
			{
				marriageMaster.mr.remove(m);
				m.priest.sendMessage(String.format(marriageMaster.lang.Get("Ingame.PlayerMarryOff"), m.p1));
				m.p2.sendMessage(String.format(marriageMaster.lang.Get("Ingame.PlayerMarryOff"), m.p1));
			}
			if(m.p2 == event.getPlayer())
			{
				marriageMaster.mr.remove(m);
				m.priest.sendMessage(String.format(marriageMaster.lang.Get("Ingame.PlayerMarryOff"), m.p2));
				m.p1.sendMessage(String.format(marriageMaster.lang.Get("Ingame.PlayerMarryOff"), m.p2));
			}
			if(m.priest == event.getPlayer())
			{
				marriageMaster.mr.remove(m);
				m.p1.sendMessage(String.format(marriageMaster.lang.Get("Ingame.PriestMarryOff"), m.priest));
				m.p2.sendMessage(String.format(marriageMaster.lang.Get("Ingame.PriestMarryOff"), m.priest));
			}
		}
	}
}
