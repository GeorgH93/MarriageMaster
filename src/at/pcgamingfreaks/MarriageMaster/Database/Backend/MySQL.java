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
import at.pcgamingfreaks.MarriageMaster.API.Home;
import at.pcgamingfreaks.MarriageMaster.Database.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
	public String getDatabaseTypeName()
	{
		return "MySQL";
	}

	@Override
	protected String getEngine()
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
}