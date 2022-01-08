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

package at.pcgamingfreaks.MarriageMaster.API;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class Home
{
	@Getter private final @NotNull String worldName;
	private final @Nullable String homeServer;
	@Getter private final double x, y, z;
	@Getter private final float yaw, pitch;

	/**
	 * Gets the server of the represented home.
	 *
	 * @return The name of the server where the home is located. Null if BungeeCord is not used.
	 */
	@SuppressWarnings("unused")
	public @Nullable String getHomeServer()
	{
		return homeServer;
	}
}