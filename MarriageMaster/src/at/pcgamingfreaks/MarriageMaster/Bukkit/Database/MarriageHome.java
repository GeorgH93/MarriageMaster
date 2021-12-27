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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Database;

import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Home;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Listener.PluginChannelCommunicator;

import org.bukkit.Location;

import java.util.Objects;

/**
 * Just a helper class that sets the server name for us
 */
public class MarriageHome extends Home
{
	public MarriageHome(Location location)
	{
		super(location, PluginChannelCommunicator.getServerName());
	}

	public MarriageHome(Location location, String homeServer)
	{
		super(location, homeServer);
	}

	public MarriageHome(String worldName, double x, double y, double z, float yaw, float pitch, String homeServer)
	{
		super(worldName, homeServer, x, y, z, yaw, pitch);
	}

	@Override
	public boolean isOnThisServer()
	{
		return Objects.equals(getHomeServer(), PluginChannelCommunicator.getServerName());
	}
}