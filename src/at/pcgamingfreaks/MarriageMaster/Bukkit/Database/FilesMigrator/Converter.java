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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Database.FilesMigrator;

import at.pcgamingfreaks.MarriageMaster.Bukkit.Database.Config;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Database.Database;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;

import java.util.Set;

public class Converter
{
	public static void runConverter(MarriageMaster plugin, Database newDB)
	{
		plugin.getLogger().info("Loading config file ...");
		Config config = plugin.getConfiguration();
		plugin.getLogger().info("Config file loaded.");

		Files f = new Files(config.useUUIDs(), plugin.getLogger());
		Set<MigrationPlayer> players = f.getPlayers();
		Set<MigrationMarriage> marriages = f.getMarriages();
		f.close();
		plugin.getLogger().info("Start migration to " + newDB.getDatabaseTypeName() + " database ...");
		for(MigrationPlayer p : players)
		{
			newDB.migratePlayer(p);
		}
		plugin.getLogger().info("Finished writing players to MySQL database.");
		plugin.getLogger().info("Writing marriages to MySQL database ...");
		for(MigrationMarriage m : marriages)
		{
			newDB.migrateMarriage(m);
		}
		plugin.getLogger().info("Finished writing marriages to MySQL database.");
		config.setDatabaseType(newDB.getDatabaseTypeName());
		plugin.getLogger().info("Finished migrating files to MySQL database.");
	}
}