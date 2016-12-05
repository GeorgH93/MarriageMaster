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

package at.pcgamingfreaks.MarriageMaster.API;

import org.jetbrains.annotations.Nullable;

/**
 * Ignore this class!
 * Use the Bukkit or BungeeCord implementation!
 */
@SuppressWarnings("unused")
public abstract class AbstractHome
{
	private final String homeServer;

	public AbstractHome(@Nullable String homeServer)
	{
		this.homeServer = homeServer;
	}

	/**
	 * Gets the server of the represented home.
	 *
	 * @return The name of the server where the home is located. Null if BungeeCord is not used.
	 */
	public @Nullable String getHomeServer()
	{
		return homeServer;
	}
}