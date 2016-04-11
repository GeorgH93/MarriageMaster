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

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Network.Reflection;

public class InteractEntity implements Listener
{
	private MarriageMaster plugin;
	private boolean dualHandMC;
	
	public InteractEntity (MarriageMaster marriagemaster)
	{
		plugin = marriagemaster;
		dualHandMC = Reflection.getVersion().contains("1_9");
	}
	
	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event)
	{
		if(!dualHandMC || event.getHand().equals(EquipmentSlot.HAND))
		{
			Player player = event.getPlayer();
			String playername = player.getName();
			if(player.isSneaking() && plugin.CheckPerm(player, "marry.kiss"))
			{
				Entity entity = event.getRightClicked();
				if(entity != null && entity instanceof Player)
				{
					Player otherPlayer = (Player) entity;
					String partner = plugin.DB.GetPartner(player);
					if(partner != null && plugin.kiss.CanKissAgain(playername) && partner.equalsIgnoreCase(otherPlayer.getName()))
					{
						if(plugin.InRadius(player, otherPlayer, plugin.config.GetRange("KissInteract")))
						{
							plugin.kiss.kiss(player, otherPlayer);
						}
					}
				}
			}
		}
	}
}