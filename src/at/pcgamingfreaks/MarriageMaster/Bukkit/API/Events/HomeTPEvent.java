/*
 *   Copyright (C) 2016 GeorgH93
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
 * Event is fired right before a player teleports to his home. All the checks are done. We just await your approval.
 */
@SuppressWarnings("unused")
public class HomeTPEvent extends Event implements Cancellable
{
	private boolean cancelled = false;
	private MarriagePlayer player;
	private Marriage marriageData;

	/**
	 * @param player The player that teleports to his home.
	 * @param marriageData The marriage data of the player containing the home that gets teleported to.
	 */
	public HomeTPEvent(@NotNull MarriagePlayer player, @NotNull Marriage marriageData)
	{
		this.player = player;
		this.marriageData = marriageData;
	}

	/**
	 * Gets the player that teleports to his home.
	 *
	 * @return The player that teleports to his home.
	 */
	public @NotNull MarriagePlayer getPlayer()
	{
		return player;
	}

	/**
	 * Gets the marriage data of the player containing the home that gets teleported to.
	 *
	 * @return The marriage data of the player containing the home that gets teleported to.
	 */
	public @NotNull Marriage getMarriageData()
	{
		return marriageData;
	}

	/**
	 * Gets the cancellation state of this event. A cancelled event will not
	 * be executed on the server, but will still pass to other plugins.
	 *
	 * @return True if this event is cancelled.
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
	public HandlerList getHandlers()
	{
		return handlers;
	}

	public static HandlerList getHandlerList()
	{
		return handlers;
	}
}