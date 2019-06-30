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

package at.pcgamingfreaks.MarriageMaster.Database.FilesMigrator;

import at.pcgamingfreaks.MarriageMaster.Database.Backend.DatabaseBackend;
import at.pcgamingfreaks.MarriageMaster.Database.DatabaseConfiguration;

import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.logging.Logger;

public class Converter
{
	public static void runConverter(final @NotNull Logger logger, final @NotNull DatabaseConfiguration config, final @NotNull DatabaseBackend newDB)
	{
		logger.info("Loading config file ...");
		logger.info("Config file loaded.");

		Files f = new Files(config.useUUIDs(), logger);
		Set<MigrationPlayer> players = f.getPlayers();
		Set<MigrationMarriage> marriages = f.getMarriages();
		f.close();
		logger.info("Start migration to " + newDB.getDatabaseTypeName() + " database ...");
		for(MigrationPlayer p : players)
		{
			newDB.migratePlayer(p);
		}
		logger.info("Finished writing players to MySQL database.");
		logger.info("Writing marriages to MySQL database ...");
		for(MigrationMarriage m : marriages)
		{
			newDB.migrateMarriage(m);
		}
		logger.info("Finished writing marriages to MySQL database.");
		config.setDatabaseType(newDB.getDatabaseTypeName());
		logger.info("Finished migrating files to MySQL database.");
	}
}