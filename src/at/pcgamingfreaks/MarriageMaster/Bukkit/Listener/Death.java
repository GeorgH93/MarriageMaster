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

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;

public class Death implements Listener 
{
	private MarriageMaster plugin;

	public Death(MarriageMaster marriagemaster) 
	{
		plugin = marriagemaster;
	}

	@EventHandler
	public void onEntityDeath(EntityDeathEvent event)
    {
		if(event.getEntityType() != EntityType.PLAYER)
		{
			Player killer = event.getEntity().getKiller();
			if(killer != null)
			{
				Player otherPlayer = plugin.DB.GetPlayerPartner(killer);
				if(otherPlayer != null && otherPlayer.isOnline())
				{
					if(plugin.InRadius(killer, otherPlayer, plugin.config.GetRange("BonusXP")))
					{
						int xp = (event.getDroppedExp() / 2) * plugin.config.GetBonusXPAmount();
						otherPlayer.giveExp(xp);
						killer.giveExp(xp);
						event.setDroppedExp(0);
					}
				}
			}
		}
	}
}