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

import org.bukkit.ChatColor;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;

public class Damage implements Listener 
{
	private MarriageMaster plugin;
	
	public Damage(MarriageMaster marriagemaster)
	{
		plugin = marriagemaster;
	}

	@EventHandler
	public void onPlayerDamage(EntityDamageByEntityEvent event)
    {
        if(event.getEntity() instanceof Player && (event.getDamager() instanceof Player || event.getDamager() instanceof Arrow))
        {
        	Player otherPlayer = (Player) event.getEntity(), player;
        	if(event.getDamager() instanceof Player)
        	{
        		player = (Player) event.getEntity();
        	}
        	else
        	{
        		Arrow arrow = (Arrow) event.getDamager();
        		if(!(arrow.getShooter() instanceof Player))
        		{
        			return;
        		}
        		player = (Player) arrow.getShooter();
        	}
        	
			String married1 = plugin.DB.GetPartner(player);
			String married2 = plugin.DB.GetPartner(otherPlayer);
			
			if(married1 != null && married2 != null)
			{
				if(married1.equalsIgnoreCase(otherPlayer.getName()) && married2.equalsIgnoreCase(player.getName()))
				{
					if(!plugin.DB.GetPvPEnabled(player))
					{
						player.sendMessage(ChatColor.RED + plugin.lang.Get("Ingame.PvPIsOff"));
						event.setCancelled(true);
					}
				}
			}
        }
    }
}