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
 * Event is fired right before a player teleports to his home. All the checks are done. We just await your approval.
 */
@SuppressWarnings("unused")
public class HomeTPEvent extends MarriageMasterCancellableEvent
{
	private final MarriagePlayer player;
	private final Marriage marriageData;

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