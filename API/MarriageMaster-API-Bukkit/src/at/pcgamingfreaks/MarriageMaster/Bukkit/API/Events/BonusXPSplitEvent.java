/*
 *   Copyright (C) 2022 GeorgH93
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

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event is fired when a married player picks up. All the checks are done. We just await your approval.
 */
@SuppressWarnings("unused")
public class BonusXPSplitEvent extends MarriageMasterCancellableEvent
{
	private final MarriagePlayer player;
	private final Marriage marriageData;
	private int amount;

	/**
	 * @param player The player that has picked up the xp orb.
	 * @param marriageData The marriage data of the player.
	 * @param amount The amount of XP that the player and his partner will get.
	 */
	public BonusXPSplitEvent(@NotNull MarriagePlayer player, @NotNull Marriage marriageData, int amount)
	{
		this.player = player;
		this.marriageData = marriageData;
		this.amount = amount;
	}

	/**
	 * Gets the player that has picked up the xp orb.
	 *
	 * @return The player that has picked up the xp orb.
	 */
	public @NotNull MarriagePlayer getPlayer()
	{
		return player;
	}

	/**
	 * Gets the marriage data for the player.
	 *
	 * @return The marriage data for the player.
	 */
	public @NotNull Marriage getMarriageData()
	{
		return marriageData;
	}


	/**
	 * The amount of XP that the player and his partner will get.
	 *
	 * @return The amount of XP the players will get.
	 */
	public int getAmount() {
		return amount;
	}

	/**
	 * Sets the amount of XP that the player and his partner will get.
	 *
	 * @param amount The amount of XP the players that will get.
	 */
	public void setAmount(int amount)
	{
		this.amount = amount;
	}


	// Bukkit handler stuff
	private static final HandlerList handlers = new HandlerList();

	@Override
	public @NotNull HandlerList getHandlers()
	{
		return getHandlerList();
	}

	public static HandlerList getHandlerList()
	{
		return handlers;
	}
}