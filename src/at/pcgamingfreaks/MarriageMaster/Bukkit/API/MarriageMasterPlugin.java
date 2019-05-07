/*
 *   Copyright (C) 2019 GeorgH93
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

package at.pcgamingfreaks.MarriageMaster.Bukkit.API;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public interface MarriageMasterPlugin extends at.pcgamingfreaks.MarriageMaster.API.MarriageMasterPlugin<OfflinePlayer, MarriagePlayer, Marriage, MarriageManager, CommandManager, DelayableTeleportAction>
{
	/**
	 * Checks if two players are within a certain range to each other.
	 *
	 * @param player1 The fist player to be checked.
	 * @param player2 The second player to be checked.
	 * @param range The range in which the two players should be.
	 * @return True if the players are within the given range, false if not.
	 *         Also True if one of the players has the "marry.bypassrangelimit" permission, the range is 0 and both players are in the same world or the range is below 0.
	 */
	boolean isInRange(@NotNull Player player1, @NotNull Player player2, double range);

	/**
	 * Checks if two players are within a certain range to each other.
	 *
	 * @param player1 The fist player to be checked.
	 * @param player2 The second player to be checked.
	 * @param rangeSquared The squared distance in which the two players should be.
	 * @return True if the players are within the given range, false if not.
	 *         Also True if one of the players has the "marry.bypassrangelimit" permission, the range is 0 and both players are in the same world or the range is below 0.
	 */
	boolean isInRangeSquared(@NotNull Player player1, @NotNull Player player2, double rangeSquared);
}