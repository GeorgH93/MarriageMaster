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

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;

import at.pcgamingfreaks.georgh.MarriageMaster.MarriageMaster;

public class RegainHealth implements Listener
{
	private MarriageMaster marriageMaster;

	public RegainHealth(MarriageMaster marriageMaster) 
	{
		this.marriageMaster = marriageMaster;
	}

	@EventHandler
	public void onHeal(EntityRegainHealthEvent event) 
	{
		Player player = null;
		
		if (event.getEntity() instanceof Player)
		{
			player = (Player) event.getEntity();
		}
		
		if(player != null)
		{
			if(this.marriageMaster.config.GetHealthRegainEnabled())
			{
				int amount = this.marriageMaster.config.GetHealthRegainAmount();
				
				String partner = this.marriageMaster.DB.GetPartner(player.getName());
				if(partner != null)
				{
					Player otherPlayer = this.marriageMaster.getServer().getPlayer(partner);
					
					if(otherPlayer != null)
					{
						if(otherPlayer.isOnline())
						{
							if(this.getRadius(player, otherPlayer))
							{
								event.setAmount((double)amount);
							}
						}
					}
				}
			}
		}
	}
	
	private boolean getRadius(Player player, Player otherPlayer) 
	{
		Location pl = player.getLocation();
		Location opl = otherPlayer.getLocation();
		
		if(pl.distance(opl) <= 2 && opl.distance(pl) <= 2)
		{
			return true;
		}
		
		return false;
	}
}
