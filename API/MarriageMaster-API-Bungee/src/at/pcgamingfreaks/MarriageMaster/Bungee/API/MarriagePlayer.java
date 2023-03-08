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

package at.pcgamingfreaks.MarriageMaster.Bungee.API;

import net.md_5.bungee.api.connection.ProxiedPlayer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface MarriagePlayer extends at.pcgamingfreaks.MarriageMaster.API.MarriagePlayer<Marriage, MarriagePlayer>
{
	/**
	 * Gets the ProxiedPlayer that is represented by this marriage player data.
	 *
	 * @return The ProxiedPlayer represented by this marriage player. null if offline.
	 */
	@Nullable ProxiedPlayer getPlayer();

	boolean canSee(ProxiedPlayer player);

	/**
	 * Checks if the player is married to a given player.
	 *
	 * @param player The player to be checked.
	 * @return True if they are married. False if not.
	 */
	boolean isPartner(@NotNull ProxiedPlayer player);
}