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
 *   along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events;

import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Event is fired right before the private message of a player is sent to his partner. All the checks are done. We just await your approval.
 */
public class MarryChatMultiTargetEvent extends Event implements Cancellable
{
	private boolean cancelled = false;
	private final MarriagePlayer player;
	private final List<Marriage> marriageData;
	private String message;

	/**
	 * @param player The player that sends the private message.
	 * @param marriageData Marriage data of the player containing the partner that should get the message.
	 * @param message The message sent from the player.
	 */
	public MarryChatMultiTargetEvent(@NotNull MarriagePlayer player, @NotNull List<Marriage> marriageData, @NotNull String message)
	{
		super(true);
		this.player = player;
		this.marriageData = marriageData;
		this.message = message;
	}

	/**
	 * Gets the player that has sent the private message.
	 *
	 * @return The player that has sent the private message.
	 */
	public @NotNull MarriagePlayer getSender()
	{
		return player;
	}

	/**
	 * Gets marriage data of the player containing the partners.
	 *
	 * @return Marriage data of the player containing the partners.
	 */
	public @NotNull List<Marriage> getMarriageData()
	{
		return marriageData;
	}

	/**
	 * Gets the private message sent by a player.
	 *
	 * @return The private message.
	 */
	public @NotNull String getMessage()
	{
		return message;
	}

	/**
	 * Changes the message sent by a player.
	 *
	 * @param newMessage The new private message.
	 */
	public void setMessage(@NotNull String newMessage)
	{
		message = newMessage;
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
	public @NotNull HandlerList getHandlers()
	{
		return handlers;
	}

	public static HandlerList getHandlerList()
	{
		return handlers;
	}
}