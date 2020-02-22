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

package at.pcgamingfreaks.MarriageMaster.API;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Home
{
	private final String homeServer, world;
	private final double x, y, z;

	public Home(final @NotNull String world, final @Nullable String homeServer, final double x, final double y, final double z)
	{
		this.world = world;
		this.homeServer = homeServer;
		this.x = x;
		this.y = y;
		this.z = z;
	}

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

	/**
	 * Gets the x position of the represented home.
	 *
	 * @return The x position.
	 */
	public double getX()
	{
		return x;
	}

	/**
	 * Gets the y position of the represented home.
	 *
	 * @return The y position.
	 */
	public double getY()
	{
		return y;
	}

	/**
	 * Gets the z position of the represented home.
	 *
	 * @return The z position.
	 */
	public double getZ()
	{
		return z;
	}

	/**
	 * Gets the world name of the represented home.
	 *
	 * @return The world name position.
	 */
	public @NotNull String getWorldName()
	{
		return world;
	}
}