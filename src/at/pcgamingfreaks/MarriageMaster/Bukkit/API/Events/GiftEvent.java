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
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Event is fired right before the player gifts an item stack to his partner. All the checks are done. We just await your approval.
 */
@SuppressWarnings("unused")
public class GiftEvent extends Event implements Cancellable
{
	private boolean cancelled = false;
	private MarriagePlayer player;
	private Marriage marriageData;
	private ItemStack itemStack;

	/**
	 * @param sender       The player that sends the gift to his partner.
	 * @param marriageData The marriage data of the player containing the partner that gets the gift.
	 * @param itemStack    The item stack that gets gifted.
	 */
	public GiftEvent(@NotNull MarriagePlayer sender, @NotNull Marriage marriageData, @NotNull ItemStack itemStack)
	{
		this.player = sender;
		this.marriageData = marriageData;
		this.itemStack = itemStack;
	}

	/**
	 * Gets the player that sends the gift to his partner.
	 *
	 * @return The player that sends the gift to his partner.
	 */
	public @NotNull MarriagePlayer getPlayer()
	{
		return player;
	}

	/**
	 * Gets the marriage data of the player containing the partner that gets the gift.
	 *
	 * @return The marriage data of the player containing the partner that gets the gift.
	 */
	public @NotNull Marriage getMarriageData()
	{
		return marriageData;
	}

	/**
	 * Gets the item stack that gets gifted.
	 *
	 * @return The item stack that gets gifted.
	 */
	public @NotNull ItemStack getItemStack()
	{
		return itemStack;
	}

	/**
	 * Sets the item stack that gets gifted.
	 *
	 * @param itemStack The item stack that gets gifted.
	 */
	public void setItemStack(ItemStack itemStack)
	{
		this.itemStack = itemStack;
	}

	/**
	 * Gets the cancellation state of this event. A cancelled event will not
	 * be executed on the server, but will still pass to other plugins.
	 *
	 * @return true if this event is cancelled.
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