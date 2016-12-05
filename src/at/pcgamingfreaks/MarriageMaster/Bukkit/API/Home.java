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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a home for a marriage
 */
@SuppressWarnings("unused")
public abstract class Home extends AbstractHome
{
	private final Location location;

	/*public Home(Location location)
	{
		this(location, null);
	}*/

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

	public abstract boolean isOnThisServer();
}