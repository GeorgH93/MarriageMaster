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

package at.pcgamingfreaks.MarriageMaster.Database.FilesMigrator;

import at.pcgamingfreaks.yaml.YAML;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

public class Files
{
	private final Set<String> priests = new HashSet<>();
	private final Set<MigrationMarriage> marriages = new HashSet<>();
	private final Map<String, YAML> marryMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	private final Map<String, MigrationPlayer> player = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	private final boolean useUUIDs;
	private final Logger logger;
	private final File pluginDir;

	public Files(boolean useUUIDs, Logger logger, File pluginDir)
	{
		this.useUUIDs = useUUIDs;
		this.logger = logger;
		this.pluginDir = pluginDir;
		player.put((useUUIDs) ? "00000000000000000000000000000000" : "none", new MigrationPlayer("none", "00000000000000000000000000000000", false, false));
		player.put((useUUIDs) ? "00000000000000000000000000000001" : "Console", new MigrationPlayer("Console", "00000000000000000000000000000001", false, false));
		loadPriests();
		loadAllPlayers();
		prepareData();
	}

	public void close()
	{
		logger.info("Cleaning some memory ...");
		for(Map.Entry<String, YAML> stringYAMLEntry : marryMap.entrySet())
		{
			stringYAMLEntry.getValue().dispose();
		}
		priests.clear();
		marryMap.clear();
		player.clear();
		logger.info("Finished cleaning memory.");
	}

	public Set<MigrationMarriage> getMarriages()
	{
		return marriages;
	}

	public Set<MigrationPlayer> getPlayers()
	{
		Set<MigrationPlayer> players = new HashSet<>();
		Iterator<Map.Entry<String, MigrationPlayer>> iterator = player.entrySet().iterator();
		Map.Entry<String, MigrationPlayer> e;
		while(iterator.hasNext())
		{
			e = iterator.next();
			players.add(e.getValue());
		}
		return players;
	}

	private void prepareData()
	{
		logger.info("Preparing data for database ...");
		Iterator<Map.Entry<String, YAML>> iterator = marryMap.entrySet().iterator();
		Map.Entry<String, YAML> e;
		while(iterator.hasNext())
		{
			e = iterator.next();
			if(useUUIDs)
			{
				player.put(e.getKey(), new MigrationPlayer(e.getValue().getString("Name", ""), e.getKey(), e.getValue().getBoolean("isPriest", false), e.getValue().getBoolean("ShareBackpack", false)));
			}
			else
			{
				player.put(e.getKey(), new MigrationPlayer(e.getKey(), null, e.getValue().getBoolean("isPriest", false), e.getValue().getBoolean("ShareBackpack", false)));
			}
		}

		iterator = marryMap.entrySet().iterator();
		Set<String> checked = new HashSet<>();
		String s;
		while(iterator.hasNext())
		{
			e = iterator.next();
			if(checked.contains(e.getKey())) continue;
			if(useUUIDs)
			{
				s = e.getValue().getString("MarriedToUUID", "");
			}
			else
			{
				s = e.getValue().getString("MarriedTo", "");
			}
			MigrationPlayer p1 = player.get(e.getKey()), p2, p = (e.getValue().getString("MarriedBy", null) != null) ? player.get(e.getValue().getString("MarriedBy", null)) : null;
			p2 = player.get(s);
			checked.add(s);
			marriages.add(new MigrationMarriage(p1, p2, p, e.getValue().getString("Surname", null), e.getValue().getBoolean("PvP", false), getHome(e.getValue())));
		}
		logger.info("Data prepared.");
	}

	private Home getHome(YAML yaml)
	{
		try
		{
			return new Home(yaml.getDouble("MarriedHome.location.X"), yaml.getDouble("MarriedHome.location.Y"), yaml.getDouble("MarriedHome.location.Z"), yaml.getString("MarriedHome.location.World"));
		}
		catch(Exception ignored) { }
		return null;
	}

	private void loadPriests()
	{
		logger.info("Loading priests ...");
		File file = new File(pluginDir, "priests.yml");
		priests.clear();
		if(file.exists())
		{
			try(Scanner scanner = new Scanner(file, "UTF-8"))
			{
				while(scanner.hasNextLine())
				{
					String line = scanner.nextLine();
					if(!line.isEmpty())
					{
						priests.add(line.replace("\r", ""));
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		logger.info("Priests loaded.");
	}

	private void loadAllPlayers()
	{
		logger.info("Loading players ...");
		File file = new File(pluginDir, "players");
		String temp;
		if(file.exists())
		{
			File[] allFiles = file.listFiles();
			if(allFiles != null && allFiles.length > 0)
			{
				for(File item : allFiles)
				{
					temp = item.getName();
					if(temp.endsWith(".yml"))
					{
						loadPlayer(temp.substring(0, temp.length() - 4));
					}
				}
			}
		}
		logger.info("Players loaded.");
	}

	private void loadPlayer(String player)
	{
		File file = new File(pluginDir, "players" + File.separator + player + ".yml");
		if(file.exists())
		{
			try
			{
				marryMap.put(player, new YAML(file));
				if(marryMap.get(player).getString("MarriedTo", "").equalsIgnoreCase(player))
				{
					marryMap.get(player).dispose();
					marryMap.remove(player);
					if(!file.delete())
					{
						logger.warning("Failed to delete file (" + file.getAbsolutePath() + ").");
					}
				}
				else
				{
					marryMap.get(player).set("isPriest", priests.contains(player));
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}