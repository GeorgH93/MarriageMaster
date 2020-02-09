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

import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.BonusXPSplitEvent;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;

public class BonusXpSplitOnPickup extends BonusXpBase<PlayerExpChangeEvent, Object> implements Listener
{
	public BonusXpSplitOnPickup(MarriageMaster plugin)
	{
		super(plugin, 1, true);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityDeath(PlayerExpChangeEvent event)
	{
		process(event, event.getPlayer(), event.getAmount(), null);
	}

	@Override
	protected void setEventExp(PlayerExpChangeEvent event, double xp, Object o, MarriagePlayer player, Marriage marriage) {}

	@Override
	protected void splitWithPartner(PlayerExpChangeEvent event, Player partner, double xp, Object o, MarriagePlayer player, Marriage marriage)
	{
		int xpPlayer = (int) Math.round((xp)), xpPartner = (int) xp;
		BonusXPSplitEvent xpSplitEvent = new BonusXPSplitEvent(player, marriage, xpPlayer);
		plugin.getServer().getPluginManager().callEvent(xpSplitEvent);
		if(!xpSplitEvent.isCancelled())
		{
			event.setAmount(xpSplitEvent.getAmount());
			if(xpPartner > 0) partner.giveExp(xpPartner); // If the partner is near he/she must also be online
		}
	}
}