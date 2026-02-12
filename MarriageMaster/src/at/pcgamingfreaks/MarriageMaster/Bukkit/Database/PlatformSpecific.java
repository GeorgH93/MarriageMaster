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

import at.pcgamingfreaks.ConsoleColor;
import at.pcgamingfreaks.Database.ConnectionProvider.ConnectionProvider;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Bukkit.SpecialInfoWorker.DbErrorLoadingDataInfo;
import at.pcgamingfreaks.MarriageMaster.Database.IPlatformSpecific;
import at.pcgamingfreaks.Message.MessageColor;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Logger;

public class PlatformSpecific implements IPlatformSpecific<MarriagePlayerData, MarriageData, MarriageHome>
{
	private final MarriageMaster plugin;

	public PlatformSpecific(MarriageMaster plugin) {this.plugin = plugin;}

	@Override
	public MarriagePlayerData producePlayer(final @Nullable UUID uuid, final @NotNull String name, final boolean priest, final boolean sharesBackpack, final @Nullable Object databaseKey)
	{
		return new MarriagePlayerData(uuid, name, priest, sharesBackpack, databaseKey);
	}

	@Override
	public MarriageData produceMarriage(final @NotNull MarriagePlayerData player1, final @NotNull MarriagePlayerData player2, final @Nullable MarriagePlayerData priest, final @NotNull Date weddingDate,
	                                    final @Nullable String surname, final boolean pvpEnabled, final @Nullable MessageColor color, final @Nullable MarriageHome home, final @Nullable Object databaseKey)
	{
		return new MarriageData(player1, player2, priest, weddingDate, surname, pvpEnabled, color, home, databaseKey);
	}

	@Override
	public MarriageHome produceHome(final @NotNull String world, final @Nullable String server, final double x, final double y, final double z, float yaw, float pitch)
	{
		return new MarriageHome(world, x, y, z, yaw, pitch, server);
	}

	@Override
	public void runAsync(final @NotNull Runnable runnable, final long delay)
	{
		if(delay < 1)
		{
			plugin.getScheduler().runAsync(task -> runnable.run());
		}
		else
		{
			plugin.getScheduler().runLaterAsync(runnable, delay);
		}
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
			connectionProvider = at.pcgamingfreaks.PluginLib.Bukkit.PluginLib.getInstance().getConnectionProvider();
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