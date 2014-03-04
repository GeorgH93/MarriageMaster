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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import at.pcgamingfreaks.georgh.MarriageMaster.MarriageMaster;

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
			String otherPlayer = marriageMaster.DB.GetPartner(event.getPlayer().getName());
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
		if(marriageMaster.config.UsePrefix())
		{
			marriageMaster.chat.setPlayerPrefix(event.getPlayer(), marriageMaster.config.GetPrefix());
		}
	}

	@EventHandler
	public void PlayerLeaveEvent(PlayerQuitEvent event)
	{
		if(marriageMaster.config.GetInformOnPartnerJoinEnabled())
		{
			String otherPlayer = marriageMaster.DB.GetPartner(event.getPlayer().getName());
			
			if(otherPlayer != null)
			{
				Player oPlayer = marriageMaster.getServer().getPlayer(otherPlayer);
				
				if(oPlayer != null)
				{
					oPlayer.sendMessage(ChatColor.GREEN + marriageMaster.lang.Get("Ingame.PartnerNowOffline"));
				}
			}
		}
	}
}
