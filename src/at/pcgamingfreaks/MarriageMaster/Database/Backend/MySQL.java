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

import at.pcgamingfreaks.Database.ConnectionProvider.ConnectionProvider;
import at.pcgamingfreaks.Database.ConnectionProvider.MySQLConnectionProvider;
import at.pcgamingfreaks.Database.DBTools;
import at.pcgamingfreaks.MarriageMaster.API.Home;
import at.pcgamingfreaks.MarriageMaster.Database.*;
import at.pcgamingfreaks.Version;

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public class MySQL<MARRIAGE_PLAYER extends MarriagePlayerDataBase, MARRIAGE extends MarriageDataBase, HOME extends Home> extends SQL<MARRIAGE_PLAYER, MARRIAGE, HOME>
{
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
	protected void buildQuerys()
	{
		if(useUUIDs)
		{
			queryAddPlayer = "INSERT IGNORE INTO {TPlayers} ({FName},{FUUID},{FShareBackpack}) SELECT ?,?,? FROM (SELECT 1) AS `tmp` WHERE NOT EXISTS (SELECT * FROM {TPlayers} WHERE {FUUID}=?);";
		}
		super.buildQuerys();
	}

	@Override
	protected void checkDatabase()
	{
		try(Connection connection = getConnection())
		{
			String dateDefault = "NOW()";
			try(Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery("SELECT VERSION();"))
			{
				if(rs.next())
				{
					String version = rs.getString(1);
					logger.info("MySQL Server version: " + version);
					Version server = new Version(version);
					if(server.olderThan(new Version("6.0")))
					{
						try(ResultSet rsNow = stmt.executeQuery("SELECT NOW();"))
						{
							if(rsNow.next())
							{
								dateDefault = rs.getString(1);
							}
							else
							{
								dateDefault = "2019-07-19 12:12:12";
							}
							dateDefault = "'" + dateDefault + "'";
						}
					}
				}
			}
			@Language("SQL")
			String queryTPlayers = replacePlaceholders("CREATE TABLE IF NOT EXISTS {TPlayers} (\n{FPlayerID} INT UNSIGNED NOT NULL AUTO_INCREMENT,\n{FName} VARCHAR(16) NOT NULL,\n{FUUID} CHAR(36) DEFAULT NULL,\n" +
					                                           "{FShareBackpack} TINYINT(1) NOT NULL DEFAULT 0,\nPRIMARY KEY ({FPlayerID}),\nUNIQUE INDEX {FUUID}_UNIQUE ({FUUID})\n)" + getEngine() + ";"),
					queryTPriests = replacePlaceholders("CREATE TABLE IF NOT EXISTS {TPriests} (\n{FPriestID} INT UNSIGNED NOT NULL,\nPRIMARY KEY ({FPriestID}),\nCONSTRAINT fk_{TPriests}_{TPlayers}_{FPriestID}" +
							                                    " FOREIGN KEY ({FPriestID}) REFERENCES {TPlayers} ({FPlayerID}) ON DELETE CASCADE ON UPDATE CASCADE\n)" + getEngine() + ";"),
					queryTMarriages = replacePlaceholders("CREATE TABLE IF NOT EXISTS {TMarriages} (\n{FMarryID} INT UNSIGNED NOT NULL AUTO_INCREMENT,\n{FPlayer1} INT UNSIGNED NOT NULL,\n{FPlayer2} INT UNSIGNED NOT NULL,\n" +
							                                      "{FPriest} INT UNSIGNED NULL,\n{FSurname} VARCHAR(45) NULL,\n{FPvPState} TINYINT(1) NOT NULL DEFAULT 0,\n{FDate} DATETIME NOT NULL DEFAULT " +
							                                      dateDefault +",\nPRIMARY KEY ({FMarryID}),\nINDEX {FPlayer1}_idx ({FPlayer1}),\nINDEX {FPlayer2}_idx ({FPlayer2}),\nINDEX {FPriest}_idx ({FPriest}),\n" +
							                                      "CONSTRAINT fk_{TMarriages}_{TPlayers}_{FPlayer1} FOREIGN KEY ({FPlayer1}) REFERENCES {TPlayers} ({FPlayerID}) ON DELETE CASCADE ON UPDATE CASCADE,\n" +
							                                      "CONSTRAINT fk_{TMarriages}_{TPlayers}_{FPlayer2} FOREIGN KEY ({FPlayer2}) REFERENCES {TPlayers} ({FPlayerID}) ON DELETE CASCADE ON UPDATE CASCADE,\n" +
							                                      "CONSTRAINT fk_{TMarriages}_{TPlayers}_{FPriest} FOREIGN KEY ({FPriest}) REFERENCES {TPlayers} ({FPlayerID}) ON DELETE SET NULL ON UPDATE CASCADE\n)" + getEngine() + ";"),
					queryTHomes = replacePlaceholders("CREATE TABLE IF NOT EXISTS {THomes} (\n{FMarryID} INT UNSIGNED NOT NULL,\n{FHomeX} DOUBLE NOT NULL,\n{FHomeY} DOUBLE NOT NULL,\n{FHomeZ} DOUBLE NOT NULL,\n" +
							                                  "{FHomeWorld} VARCHAR(45) NOT NULL DEFAULT 'world',\n" + ((bungee) ? "{FHomeServer} VARCHAR(45) DEFAULT NULL,\n" : "") + "PRIMARY KEY ({FMarryID}),\n" +
							                                  "CONSTRAINT fk_{THomes}_{TMarriages}_{FMarryID} FOREIGN KEY ({FMarryID}) REFERENCES {TMarriages} ({FMarryID}) ON DELETE CASCADE ON UPDATE CASCADE\n)" + getEngine() + ";");
			DBTools.updateDB(connection, queryTPlayers);
			DBTools.updateDB(connection, queryTPriests);
			DBTools.updateDB(connection, queryTMarriages);
			DBTools.updateDB(connection, queryTHomes);
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}
}