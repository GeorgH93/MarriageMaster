/*
 *   Copyright (C) 2020 GeorgH93
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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Listener.BonusXP;

import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.BonusXPDropEvent;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class BonusXpListener implements Listener, IBonusXpListener<EntityDeathEvent, Object>
{
	private final IBonusXpCalculator<EntityDeathEvent, Object> calculator;

	public BonusXpListener(MarriageMaster plugin)
	{
		calculator = new NearestPartnerBonusXpCalculator<>(plugin, plugin.getConfiguration().getBonusXpMultiplier(), false, this);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityDeath(EntityDeathEvent event)
	{
		if(event.getEntityType() != EntityType.PLAYER)
		{
			Player killer = event.getEntity().getKiller();
			if(killer != null)
			{
				calculator.process(event, killer, event.getDroppedExp(), null);
			}
		}
	}

	@Override
	public void setEventExp(EntityDeathEvent event, double xp, Object o, MarriagePlayer player, Marriage marriage)
	{
		BonusXPDropEvent bonusXPDropEvent = new BonusXPDropEvent(player, marriage, (int) Math.round(xp));
		Bukkit.getServer().getPluginManager().callEvent(bonusXPDropEvent);
		if(!bonusXPDropEvent.isCancelled())
		{
			event.setDroppedExp(bonusXPDropEvent.getAmount());
		}
	}

	@Override
	public void splitWithPartner(EntityDeathEvent entityDeathEvent, Player partner, double xp, Object o, MarriagePlayer player, Marriage marriage) {}
}