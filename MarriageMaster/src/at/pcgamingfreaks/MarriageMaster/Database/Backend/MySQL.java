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

package at.pcgamingfreaks.MarriageMaster.Database.Backend;

import at.pcgamingfreaks.Config.Configuration;
import at.pcgamingfreaks.Database.ConnectionProvider.ConnectionProvider;
import at.pcgamingfreaks.Database.ConnectionProvider.MySQLConnectionProvider;
import at.pcgamingfreaks.Database.DBTools;
import at.pcgamingfreaks.MarriageMaster.API.Home;
import at.pcgamingfreaks.MarriageMaster.Database.*;
import at.pcgamingfreaks.Version;

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MySQL<MARRIAGE_PLAYER extends MarriagePlayerDataBase, MARRIAGE extends MarriageDataBase, HOME extends Home> extends SQL<MARRIAGE_PLAYER, MARRIAGE, HOME>
{
	private Version serverVersion = null;

	public MySQL(final @NotNull IPlatformSpecific<MARRIAGE_PLAYER, MARRIAGE, HOME> platform, final @NotNull DatabaseConfiguration dbConfig, final boolean bungee, final boolean surname,
	                 final @NotNull Cache<MARRIAGE_PLAYER, MARRIAGE> cache, final @NotNull Logger logger, final @Nullable ConnectionProvider connectionProvider, final @NotNull String pluginName)
	{
		super(platform, dbConfig, bungee, surname, cache, logger, (connectionProvider == null) ? new MySQLConnectionProvider(logger, pluginName, dbConfig) : connectionProvider);
	}

	@Override
	public boolean supportsBungeeCord()
	{
		return true;
	}

	@Override
	public @NotNull String getDatabaseTypeName()
	{
		return "MySQL";
	}

	@Override
	protected @NotNull String getEngine()
	{
		return " ENGINE=InnoDB";
	}

	@Override
	protected void buildQueries()
	{
		queryAddPlayer = "INSERT IGNORE INTO {TPlayers} ({FName},{FUUID},{FShareBackpack}) SELECT ?,?,? FROM (SELECT 1) AS `tmp` WHERE NOT EXISTS (SELECT * FROM {TPlayers} WHERE {FUUID}=?);";
		super.buildQueries();
	}

	void queryDatabaseVersion(final @NotNull Connection connection)
	{
		try(Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery("SELECT VERSION();"))
		{
			if(rs.next())
			{
				String version = rs.getString(1);
				logger.log(Level.INFO, "MySQL server version: {}", version);
				serverVersion = new Version(version);
			}
		}
		catch(SQLException e)
		{
			logger.log(Level.SEVERE, "Failed to obtain MySQL server version!", e);
		}
		if(serverVersion == null)
		{
			logger.warning("Unable to obtain MySQL server version.");
			serverVersion = new Version("1.0");
		}
	}

	@Override
	protected void checkDatabase()
	{
		try(Connection connection = getConnection())
		{
			queryDatabaseVersion(connection);
			String dateDefault = "NOW()";
			if(serverVersion.olderThan(new Version("6.0")))
			{
				try(Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery("SELECT NOW();"))
				{
					dateDefault = "'" + ((rs.next()) ? rs.getString(1) : "2019-07-19 12:12:12") + "'";
				}
			}
			@Language("SQL")
			String  queryTPlayers = replacePlaceholders("CREATE TABLE IF NOT EXISTS {TPlayers} (\n{FPlayerID} INT UNSIGNED NOT NULL AUTO_INCREMENT,\n{FName} VARCHAR(16) NOT NULL,\n{FUUID} CHAR(36) DEFAULT NULL,\n" +
					                                           "{FShareBackpack} TINYINT(1) NOT NULL DEFAULT 0,\nPRIMARY KEY ({FPlayerID}),\nUNIQUE INDEX {FUUID}_UNIQUE ({FUUID})\n)" + getEngine() + ";"),
					queryTMarriages = replacePlaceholders("CREATE TABLE IF NOT EXISTS {TMarriages} (\n{FMarryID} INT UNSIGNED NOT NULL AUTO_INCREMENT,\n{FPlayer1} INT UNSIGNED NOT NULL,\n" +
							                                      "{FPlayer2} INT UNSIGNED NOT NULL,\n{FPriest} INT UNSIGNED NULL,\n{FSurname} VARCHAR(45) NULL,\n{FPvPState} TINYINT(1) NOT NULL DEFAULT 0,\n" +
							                                      "{FDate} DATETIME NOT NULL DEFAULT " + dateDefault + ",\n{FColor} VARCHAR(20) NULL DEFAULT NULL,\n" +
							                                      "PRIMARY KEY ({FMarryID}),\nINDEX {FPlayer1}_idx ({FPlayer1}),\nINDEX {FPlayer2}_idx ({FPlayer2}),\nINDEX {FPriest}_idx ({FPriest}),\n" +
							                                      "CONSTRAINT fk_{TMarriages}_{TPlayers}_{FPlayer1} FOREIGN KEY ({FPlayer1}) REFERENCES {TPlayers} ({FPlayerID}) ON DELETE CASCADE ON UPDATE CASCADE,\n" +
							                                      "CONSTRAINT fk_{TMarriages}_{TPlayers}_{FPlayer2} FOREIGN KEY ({FPlayer2}) REFERENCES {TPlayers} ({FPlayerID}) ON DELETE CASCADE ON UPDATE CASCADE,\n" +
							                                      "CONSTRAINT fk_{TMarriages}_{TPlayers}_{FPriest} FOREIGN KEY ({FPriest}) REFERENCES {TPlayers} ({FPlayerID}) ON DELETE SET NULL ON UPDATE CASCADE\n)" + getEngine() + ";"),
					queryTHomes = replacePlaceholders("CREATE TABLE IF NOT EXISTS {THomes} (\n{FMarryID} INT UNSIGNED NOT NULL,\n{FHomeX} DOUBLE NOT NULL,\n{FHomeY} DOUBLE NOT NULL,\n{FHomeZ} DOUBLE NOT NULL,\n" +
							                                  "{FHomeYaw} FLOAT NOT NULL DEFAULT 0,\n{FHomePitch} FLOAT NOT NULL DEFAULT 0,\n{FHomeWorld} VARCHAR(45) NOT NULL DEFAULT 'world',\n" +
							                                  ((useBungee) ? "{FHomeServer} VARCHAR(45) DEFAULT NULL,\n" : "") + "PRIMARY KEY ({FMarryID}),\n" +
							                                  "CONSTRAINT fk_{THomes}_{TMarriages}_{FMarryID} FOREIGN KEY ({FMarryID}) REFERENCES {TMarriages} ({FMarryID}) ON DELETE CASCADE ON UPDATE CASCADE\n)" + getEngine() + ";");
			DBTools.updateDB(connection, queryTPlayers);
			DBTools.updateDB(connection, queryTMarriages);
			DBTools.updateDB(connection, queryTHomes);
			checkPriestsTable(connection);
			DBTools.runStatement(connection, replacePlaceholders("UPDATE {TMarriages} SET {FDate}=NOW() WHERE {FDate}=0;"));
		}
		catch(SQLException e)
		{
			logger.log(Level.SEVERE, "Failed to setup database tables!", e);
		}
	}

	private void checkPriestsTable(final @NotNull Connection connection) throws SQLException
	{
		boolean exists = false;
		try(PreparedStatement preparedStatement = connection.prepareStatement("SHOW TABLES LIKE ?;"))
		{
			preparedStatement.setString(1, tablePriests);
			try(ResultSet rs = preparedStatement.executeQuery())
			{
				if(rs.next())
				{ // Table exists
					exists = true;
				}
			}
		}

		if(exists)
		{
			try(Statement stmt = connection.createStatement())
			{
				try(ResultSet rs = stmt.executeQuery(replacePlaceholders("SHOW KEYS FROM {TPriests} WHERE Key_name = 'PRIMARY'")))
				{
					if(rs.next())
					{
						String primKey = rs.getString("Column_name");
						if(!primKey.equalsIgnoreCase(fieldPriestID))
						{
							logger.log(Level.WARNING, "PriestId field name currently used ({}}) in the database does not math the configured one in the config ({}})!\n" +
									                  "If you would like to change the name of the field please change both the name in the database and the config.\nChanging config to: {}",
							           new Object[]{ primKey, fieldPriestID, primKey });
							dbConfig.getConfigE().set("Database.SQL.Tables.Fields.PriestID", primKey);
							try
							{
								((Configuration) dbConfig).save();
							}
							catch(FileNotFoundException e)
							{
								logger.log(Level.WARNING, "Failed to save priest column name in config!", e);
							}
							fieldPriestID = primKey;
						}
					}
				}
			}
		}
		else
		{
			@Language("SQL") String queryTPriests = replacePlaceholders("CREATE TABLE IF NOT EXISTS {TPriests} (\n{FPriestID} INT UNSIGNED NOT NULL,\nPRIMARY KEY ({FPriestID}),\nCONSTRAINT fk_{TPriests}_{TPlayers}_{FPriestID}" + " FOREIGN KEY ({FPriestID}) REFERENCES {TPlayers} ({FPlayerID}) ON DELETE CASCADE ON UPDATE CASCADE\n)" + getEngine() + ";");
			DBTools.updateDB(connection, queryTPriests);
		}
	}
}