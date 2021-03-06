/*
 *   Copyright (C) 2021 GeorgH93
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

package at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events;

import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event is fired right before a XP orb with bonus XP is spawned. All the checks are done. We just await your approval.
 */
@SuppressWarnings("unused")
public class BonusXPDropEvent extends Event implements Cancellable
{
	private boolean cancelled = false;
	private MarriagePlayer player;
	private Marriage marriageData;
	private int amount;

	/**
	 * @param player The player that gets a heal bonus.
	 * @param marriageData The marriage data of the player.
	 * @param amount The amount of XP that will drop.
	 */
	public BonusXPDropEvent(@NotNull MarriagePlayer player, @NotNull Marriage marriageData, int amount)
	{
		this.player = player;
		this.marriageData = marriageData;
		this.amount = amount;
	}

	/**
	 * Gets the player that has killed the mob.
	 *
	 * @return The player that has killed the mob.
	 */
	public @NotNull MarriagePlayer getPlayer()
	{
		return player;
	}

	/**
	 * Gets the marriage data of the player.
	 *
	 * @return The marriage data of the player.
	 */
	public @NotNull Marriage getMarriageData()
	{
		return marriageData;
	}


	/**
	 * Gets the amount of XP that will be dropped.
	 * This value already contains the bonus.
	 *
	 * @return The amount of XP dropped.
	 */
	public int getAmount() {
		return amount;
	}

	/**
	 * Sets the amount of XP that will be dropped.
	 *
	 * @param amount The amount of XP that will be dropped.
	 */
	public void setAmount(int amount)
	{
		this.amount = amount;
	}


	/**
	 * Gets the cancellation state of this event. A cancelled event will not
	 * be executed on the server, but will still pass to other plugins.
	 *
	 * @return True if this event has been cancelled.
	 */
	@Override
	public boolean isCancelled()
	{
		return cancelled;
	}

	/**
	 * Sets the cancellation state of this event. A cancelled event will not
	 * be executed on the server, but will still pass to other plugins.
	 *
	 * @param cancel True if you wish to cancel this event.
	 */
	@Override
	public void setCancelled(boolean cancel)
	{
		cancelled = cancel;
	}

	// Bukkit handler stuff
	private static final HandlerList handlers = new HandlerList();

	@Override
	public @NotNull HandlerList getHandlers()
	{
		return handlers;
	}

	public static HandlerList getHandlerList()
	{
		return handlers;
	}
}