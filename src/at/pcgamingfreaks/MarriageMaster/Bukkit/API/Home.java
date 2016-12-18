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

package at.pcgamingfreaks.MarriageMaster.Bukkit.API;

import at.pcgamingfreaks.MarriageMaster.API.AbstractHome;

import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a home for a marriage
 */
@SuppressWarnings("unused")
public abstract class Home extends AbstractHome
{
	private final Location location;

	public Home(Location location)
	{
		this(location, null);
	}

	public Home(@NotNull Location location, @Nullable String homeServer)
	{
		super(homeServer);
		this.location = location;
	}

	/**
	 * Gets the location of the represented home.
	 *
	 * @return The location of the home.
	 */
	public @NotNull Location getLocation()
	{
		return location;
	}

	/**
	 * Gets the x position of the represented home.
	 *
	 * @return The x position.
	 */
	public double getX()
	{
		return location.getX();
	}

	/**
	 * Gets the y position of the represented home.
	 *
	 * @return The y position.
	 */
	public double getY()
	{
		return location.getX();
	}

	/**
	 * Gets the z position of the represented home.
	 *
	 * @return The z position.
	 */
	public double getZ()
	{
		return location.getX();
	}

	/**
	 * Gets the world name of the represented home.
	 *
	 * @return The world name position.
	 */
	public @NotNull String getWorldName()
	{
		return getWorld().getName();
	}

	/**
	 * Gets the world of the represented home.
	 *
	 * @return The x position.
	 */
	public @NotNull World getWorld()
	{
		return location.getWorld();
	}

	/**
	 * Checks if the home is on the server.
	 *
	 * @return True if the home is on the server, false if not.
	 */
	public abstract boolean isOnThisServer();
}