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

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import at.pcgamingfreaks.georgh.MarriageMaster.MarriageMaster;

public class InteractEntity implements Listener
{
	MarriageMaster marriageMaster;
	
	public InteractEntity (MarriageMaster marriagemaster)
	{
		marriageMaster = marriagemaster;
	}
	
	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event)
	{
		Player player = event.getPlayer();
		String playername = player.getName();
		if(marriageMaster.config.CheckPerm(player, "marry.kiss") && player.isSneaking() && marriageMaster.HasPartner(playername) && marriageMaster.kiss.CanKissAgain(playername))
		{
			Entity entity = event.getRightClicked();
			if(entity != null && entity instanceof Player)
			{
				Player otherPlayer = (Player) entity;
				if(marriageMaster.DB.GetPartner(playername).equalsIgnoreCase(otherPlayer.getName()))
				{
					if(marriageMaster.InRadius(player, otherPlayer, marriageMaster.config.GetRange("KissInteract")))
        			{
						marriageMaster.kiss.kiss(player, otherPlayer);
        			}
				}
			}
		}
	}
}