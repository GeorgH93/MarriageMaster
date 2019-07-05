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

package at.pcgamingfreaks.MarriageMaster.Database.Backend;

import at.pcgamingfreaks.ConsoleColor;
import at.pcgamingfreaks.Database.ConnectionProvider.ConnectionProvider;
import at.pcgamingfreaks.Database.ConnectionProvider.SQLiteConnectionProvider;
import at.pcgamingfreaks.MarriageMaster.API.Home;
import at.pcgamingfreaks.MarriageMaster.Database.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public class SQLite<MARRIAGE_PLAYER extends MarriagePlayerDataBase, MARRIAGE extends MarriageDataBase, HOME extends Home> extends SQL<MARRIAGE_PLAYER, MARRIAGE, HOME>
{
	public SQLite(final @NotNull IPlatformSpecific<MARRIAGE_PLAYER, MARRIAGE, HOME> platform, final @NotNull DatabaseConfiguration dbConfig, final boolean bungee, final boolean surname,
	                 final @NotNull Cache<MARRIAGE_PLAYER, MARRIAGE> cache, final @NotNull Logger logger, final @Nullable ConnectionProvider connectionProvider, final @NotNull String pluginName, final @NotNull File dataFolder)
	{
		super(platform, dbConfig, bungee, surname, cache, logger,
		      (connectionProvider == null) ?
				      new SQLiteConnectionProvider(logger, pluginName, dataFolder.getAbsolutePath() + File.separator + "database.db") :
				      connectionProvider);
	}

	@Override
	public @NotNull Connection getConnection() throws SQLException
	{
		Connection connection = super.getConnection();
		try(Statement statement = connection.createStatement())
		{
			statement.execute("PRAGMA foreign_keys = ON"); // We need foreign keys!
		}
		return connection;
	}

	@Override
	protected void checkDatabase()
	{
		try(Connection connection = getConnection(); Statement statement = connection.createStatement())
		{
			statement.execute(replacePlaceholders("CREATE TABLE IF NOT EXISTS {TPlayers} ({FPlayerID} INTEGER PRIMARY KEY NOT NULL, {FName} VARCHAR(16) NOT NULL, {FUUID} CHAR(36) UNIQUE DEFAULT NULL, " +
					                                      "{FShareBackpack} TINYINT(1) NOT NULL DEFAULT 0);"));
			statement.execute(replacePlaceholders("CREATE TABLE IF NOT EXISTS {TPriests} ({FPlayerID} INTEGER PRIMARY KEY NOT NULL," +
					                                      "CONSTRAINT fk_{TPriests}_{TPlayers}_{FPlayerID} FOREIGN KEY ({FPlayerID}) REFERENCES {TPlayers} ({FPlayerID}) ON DELETE CASCADE ON UPDATE CASCADE);"));
			statement.execute(replacePlaceholders("CREATE TABLE IF NOT EXISTS {TMarriages} ({FMarryID} INTEGER PRIMARY KEY NOT NULL,{FPlayer1} INT NOT NULL,{FPlayer2} INT NOT NULL," +
					                                      "{FPriest} INT NULL,{FSurname} VARCHAR(45) NULL,{FPvPState} TINYINT(1) NOT NULL DEFAULT 0,{FDate} DATE NOT NULL," +
					                                      "CONSTRAINT fk_{TMarriages}_{TPlayers}_{FPlayer1} FOREIGN KEY ({FPlayer1}) REFERENCES {TPlayers} ({FPlayerID}) ON DELETE CASCADE ON UPDATE CASCADE," +
					                                      "CONSTRAINT fk_{TMarriages}_{TPlayers}_{FPlayer2} FOREIGN KEY ({FPlayer2}) REFERENCES {TPlayers} ({FPlayerID}) ON DELETE CASCADE ON UPDATE CASCADE," +
					                                      "CONSTRAINT fk_{TMarriages}_{TPlayers}_{FPriest} FOREIGN KEY ({FPriest}) REFERENCES {TPlayers} ({FPlayerID}) ON DELETE SET NULL ON UPDATE CASCADE);"));
			statement.execute(replacePlaceholders("CREATE TABLE IF NOT EXISTS {THomes} ({FMarryID} INTEGER PRIMARY KEY NOT NULL,{FHomeX} DOUBLE NOT NULL,{FHomeY} DOUBLE NOT NULL," +
					                                      "{FHomeZ} DOUBLE NOT NULL,{FHomeWorld} VARCHAR(45) NOT NULL DEFAULT 'world'," +
					                                      "CONSTRAINT fk_{THomes}_{TMarriages}_{FMarryID} FOREIGN KEY ({FMarryID}) REFERENCES {TMarriages} ({FMarryID}) ON DELETE CASCADE ON UPDATE CASCADE);"));
		}
		catch(SQLException e)
		{
			logger.warning(ConsoleColor.RED + "Failed to create SQLite tables!" + ConsoleColor.RESET);
			e.printStackTrace();
		}
	}

	@Override
	protected void buildQuerys()
	{
		super.buildQuerys();
		querySetPriest  = querySetPriest .replace("REPLACE INTO", "INSERT OR REPLACE INTO").replace(" VALUE ", " VALUES ");
		queryUpdateHome = queryUpdateHome.replace("REPLACE INTO", "INSERT OR REPLACE INTO");
		queryAddPlayer  = queryAddPlayer .replace("INSERT IGNORE INTO", "INSERT OR IGNORE INTO");
	}

	@Override
	protected void loadTableAndFieldNames(final @NotNull DatabaseConfiguration dbConfig) {} // We use fixed names in sqlite

	@Override
	public @NotNull String getDatabaseTypeName()
	{
		return "SQLite";
	}

	@Override
	public boolean supportsBungeeCord()
	{
		return false;
	}
}