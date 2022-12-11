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

package at.pcgamingfreaks.MarriageMaster.Bungee.Database;

import at.pcgamingfreaks.ConsoleColor;
import at.pcgamingfreaks.Database.ConnectionProvider.ConnectionProvider;
import at.pcgamingfreaks.MarriageMaster.API.Home;
import at.pcgamingfreaks.MarriageMaster.Bungee.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Bungee.SpecialInfoWorker.DbErrorLoadingDataInfo;
import at.pcgamingfreaks.MarriageMaster.Database.IPlatformSpecific;
import at.pcgamingfreaks.Message.MessageColor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class PlatformSpecific implements IPlatformSpecific<MarriagePlayerData, MarriageData, Home>
{
	private final MarriageMaster plugin;

	public PlatformSpecific(MarriageMaster plugin) {this.plugin = plugin;}

	@Override
	public @NotNull MarriagePlayerData producePlayer(final @Nullable UUID uuid, final @NotNull String name, final boolean priest, final boolean sharesBackpack, final @Nullable Object databaseKey)
	{
		return new MarriagePlayerData(uuid, name, priest, sharesBackpack, databaseKey);
	}

	@Override
	public @NotNull MarriageData produceMarriage(final @NotNull MarriagePlayerData player1, final @NotNull MarriagePlayerData player2, final @Nullable MarriagePlayerData priest,
	                                             final @NotNull Date weddingDate, final @Nullable String surname, final boolean pvpEnabled, final @Nullable MessageColor color, final @Nullable Home home, final @Nullable Object databaseKey)
	{
		return new MarriageData(player1, player2, priest, weddingDate, surname, pvpEnabled, color, home, databaseKey);
	}

	@Override
	public @NotNull Home produceHome(final @NotNull String world, final @Nullable String server, final double x, final double y, final double z, float yaw, float pitch)
	{
		return new Home(world, server, x, y, z, yaw, pitch);
	}

	@Override
	public void runAsync(final @NotNull Runnable runnable, final long delay)
	{
		if(delay > 0)
			plugin.getProxy().getScheduler().schedule(plugin, runnable, delay * 20, TimeUnit.MILLISECONDS);
		else
			plugin.getProxy().getScheduler().runAsync(plugin, runnable);
	}

	@Override
	public @Nullable ConnectionProvider getExternalConnectionProvider(final @NotNull String dbType, final @NotNull Logger logger) throws SQLException
	{
		ConnectionProvider connectionProvider = null;
		if(dbType.equals("shared") || dbType.equals("external") || dbType.equals("global"))
		{
			/*if[STANDALONE]
			logger.warning(ConsoleColor.RED + "The shared database connection option is not available in standalone mode!" + ConsoleColor.RESET);
				throw new SQLException("The shared database connection option is not available in standalone mode!");
			else[STANDALONE]*/
			connectionProvider = at.pcgamingfreaks.PluginLib.Bungee.PluginLib.getInstance().getConnectionProvider();
			if(connectionProvider == null)
			{
				logger.warning(ConsoleColor.RED + "The shared connection pool is not initialized correctly!" + ConsoleColor.RESET);
				throw new SQLException("The shared connection pool is not initialized correctly!");
			}
			/*end[STANDALONE]*/
		}
		return connectionProvider;
	}

	@Override
	public @NotNull String getPluginVersion()
	{
		return plugin.getDescription().getVersion();
	}

	@Override
	public void spawnDatabaseLoadingErrorMessage(String failedToLoad)
	{
		new DbErrorLoadingDataInfo(plugin, failedToLoad);
	}
}