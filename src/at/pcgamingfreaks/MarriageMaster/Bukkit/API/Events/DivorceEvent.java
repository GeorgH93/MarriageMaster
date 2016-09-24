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

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event is fired right before two players get divorced. All the checks are done. We just await your approval.
 */
@SuppressWarnings("unused")
public class DivorceEvent extends Event implements Cancellable
{
	private boolean cancelled = false;
	private Marriage marriageData;
	private CommandSender priest;

	/**
	 * @param marriageData The marriage data of the couple that should get divorced.
	 * @param priest The priest that want to divorce the couple.
	 */
	public DivorceEvent(@NotNull Marriage marriageData, @NotNull CommandSender priest)
	{
		this.marriageData = marriageData;
		this.priest = priest;
	}

	/**
	 * Gets the marriage data containing all data of the marriage.
	 *
	 * @return The marriage data of the couple that should get divorced.
	 */
	public @NotNull Marriage getMarriageData()
	{
		return marriageData;
	}

	/**
	 * Checks if the priest is a player.
	 *
	 * @return True if the priest is a player. False if the priest is the console or there is no priest at all. Self marriage will return true.
	 */
	public boolean isPriestAPlayer()
	{
		return (priest instanceof Player);
	}

	/**
	 * Gets the priest that would like to divorce the players.
	 *
	 * @return The priest that would like to divorce the players. null if there is no priest. Self divorce will return the player that requested the divorce.
	 */
	public @NotNull CommandSender getPriest()
	{
		return priest;
	}

	/**
	 * Gets the cancellation state of this event. A cancelled event will not
	 * be executed in the server, but will still pass to other plugins.
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
	 * be executed in the server, but will still pass to other plugins.
	 *
	 * @param cancel True if you wish to cancel this event
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