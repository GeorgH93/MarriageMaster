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

import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.BonusHealEvent;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;

public class RegainHealth implements Listener
{
	private final MarriageMaster plugin;
	private final double range, multiplier;

	public RegainHealth(MarriageMaster marriagemaster)
	{
		plugin = marriagemaster;
		range = plugin.getConfiguration().getRangeSquared("Heal");
		multiplier = plugin.getConfiguration().getHPRegainMultiplier();
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onHeal(EntityRegainHealthEvent event)
	{
		if(event.getEntity() instanceof Player)
		{
			MarriagePlayer player = plugin.getPlayerData((Player) event.getEntity());
			if(player.isMarried())
			{
				Marriage marriage = player.getNearestPartnerMarriageData();
				if(marriage != null && marriage.inRangeSquared(range))
				{
					BonusHealEvent bonusHealEvent = new BonusHealEvent(player, marriage, event.getAmount() * multiplier, event.getRegainReason());
					plugin.getServer().getPluginManager().callEvent(bonusHealEvent);
					if(!bonusHealEvent.isCancelled())
					{
						event.setAmount(bonusHealEvent.getAmount());
					}
				}
			}
		}
	}
}