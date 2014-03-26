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

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;

import at.pcgamingfreaks.georgh.MarriageMaster.MarriageMaster;

public class RegainHealth implements Listener
{
	private MarriageMaster marriageMaster;

	public RegainHealth(MarriageMaster marriagemaster) 
	{
		marriageMaster = marriagemaster;
	}

	@EventHandler
	public void onHeal(EntityRegainHealthEvent event) 
	{
		if (event.getEntity() instanceof Player)
		{
			Player player = (Player) event.getEntity();
			if(player != null)
			{
				String partner = marriageMaster.DB.GetPartner(player.getName());
				if(partner != null)
				{
					Player otherPlayer = marriageMaster.getServer().getPlayer(partner);
					if(otherPlayer != null && otherPlayer.isOnline())
					{
						if(marriageMaster.InRadius(player, otherPlayer, marriageMaster.config.GetRange("Heal")))
						{
							event.setAmount((double)marriageMaster.config.GetHealthRegainAmount());
						}
					}
				}
			}
		}
	}
}