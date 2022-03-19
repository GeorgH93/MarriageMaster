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
import org.jetbrains.annotations.Nullable;

/**
 * Event is fired after two players got married.
 */
@SuppressWarnings("unused")
public class MarriedEvent extends MarriageMasterEvent
{
	private final Marriage marriageData;

	/**
	 * @param marriageData Marriage data of the new formed marriage.
	 */
	public MarriedEvent(@NotNull Marriage marriageData)
	{
		this.marriageData = marriageData;
	}

	/**
	 * Gets marriage data containing all data of the marriage.
	 *
	 * @return Marriage data of the new formed marriage.
	 */
	public @NotNull Marriage getMarriageData()
	{
		return marriageData;
	}

	/**
	 * Gets the first player of the marriage.
	 *
	 * @return The first player of the marriage.
	 */
	public @NotNull MarriagePlayer getPlayer1()
	{
		return marriageData.getPartner1();
	}

	/**
	 * Gets the second player of the marriage.
	 *
	 * @return The second player of the marriage.
	 */
	public @NotNull MarriagePlayer getPlayer2()
	{
		return marriageData.getPartner2();
	}

	/**
	 * Gets the priest involved in marrying the couple.
	 *
	 * @return The priest involved in marrying the couple.
	 */
	public @Nullable MarriagePlayer getPriest()
	{
		return marriageData.getPriest();
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