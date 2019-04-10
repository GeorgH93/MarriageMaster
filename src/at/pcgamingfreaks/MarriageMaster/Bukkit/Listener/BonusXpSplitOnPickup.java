/*
 *   Copyright (C) 2019 GeorgH93
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

import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.BonusXPSplitEvent;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;

public class BonusXpSplitOnPickup implements Listener
{
	private final MarriageMaster plugin;
	private final double range;

	public BonusXpSplitOnPickup(MarriageMaster marriagemaster)
	{
		plugin = marriagemaster;
		range = marriagemaster.getConfiguration().getRange("BonusXP");
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityDeath(PlayerExpChangeEvent event)
	{
		MarriagePlayer player = plugin.getPlayerData(event.getPlayer());
		if(player.isMarried())
		{
			Marriage marriage = player.getNearestPartnerMarriageData();
			if(marriage != null)
			{
				MarriagePlayer partner = marriage.getPartner(player);
				if(partner != null && partner.isOnline() && marriage.inRange(range))
				{
					double amount = event.getAmount() / 2.0;
					int xpPlayer = (int) Math.round((amount)), xpPartner = (int) amount;
					BonusXPSplitEvent xpSplitEvent = new BonusXPSplitEvent(player, marriage, xpPlayer);
					plugin.getServer().getPluginManager().callEvent(xpSplitEvent);
					if(!xpSplitEvent.isCancelled())
					{
						event.setAmount(xpSplitEvent.getAmount());
						if(xpPartner > 0) //noinspection ConstantConditions
							partner.getPlayerOnline().giveExp(xpPartner); // If the partner is near he/she must also be online
					}
				}
			}
		}
	}
}