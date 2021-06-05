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

package at.pcgamingfreaks.MarriageMaster.Bukkit.API;

import at.pcgamingfreaks.Message.MessageColor;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public interface Marriage extends at.pcgamingfreaks.MarriageMaster.API.Marriage<MarriagePlayer, CommandSender, Home>
{
	/**
	 * Gets the distance between the two married players.
	 *
	 * @return The distance between two players. Double.POSITIVE_INFINITY if they are both online but in different worlds. Double.NEGATIVE_INFINITY if one or both of them are offline.
	 */
	double getDistance();

	/**
	 * Checks if both players are within a given distance.
	 *
	 * @param maxDistance The maximum distance between both players.
	 *                    -1 no range limit and no world limit, 0 no range limit but in the same world, greater than 0 range limit in meters/blocks.
	 * @return True if they are within the given distance. False if not or one is offline or both of them are offline.
	 */
	boolean inRange(double maxDistance);

	/**
	 * Checks if both players are within a given distance.
	 *
	 * @param maxDistanceSquared The maximum squared distance between both players.
	 *                           -1 no range limit and no world limit, 0 no range limit but in the same world, greater than 0 range limit in meters/blocks.
	 * @return True if they are within the given distance. False if not or one is offline or both of them are offline.
	 */
	boolean inRangeSquared(double maxDistanceSquared);

	/**
	 * Gets the color of the marriage, since there are only 16 colors available in Minecraft it is possible that two or more marriages have the same color.
	 * The default color is random. But can be changed.
	 *
	 * @return The color of the marriage.
	 * @deprecated Replaced by {@link Marriage#getColor()}
	 */
	@Deprecated
	@NotNull ChatColor getMarriageColor();

	/**
	 * Sets the color of the marriage, since there are only 16 colors available in Minecraft it is possible that two or more marriages have the same color.
	 *
	 * @param color the color that should be set for the marriage.
	 * @deprecated Replaced by {@link Marriage#setColor(MessageColor)}
	 */
	@Deprecated
	void setMarriageColor(@NotNull ChatColor color);
}