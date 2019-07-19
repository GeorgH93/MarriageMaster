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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Placeholder.Hooks;

import at.pcgamingfreaks.ConsoleColor;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Placeholder.PlaceholderManager;

import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import java.io.File;
import java.util.List;

public class ClipsPlaceholderHook extends PlaceholderExpansion implements PlaceholderAPIHook
{
	private final MarriageMaster plugin;
	private final PlaceholderManager placeholderManager;

	public ClipsPlaceholderHook(MarriageMaster plugin, PlaceholderManager placeholderManager)
	{
		this.plugin = plugin;
		this.placeholderManager = placeholderManager;
		//region Check for Marriage Master eCloud
		Plugin papi = plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI");
		if(papi != null)
		{
			File marriageMasterEcloud = new File(papi.getDataFolder(), "expansions" + File.separator + "Expansion-MarriageMaster.jar");
			if(marriageMasterEcloud.exists())
			{
				if(marriageMasterEcloud.delete())
				{
					plugin.getLogger().info("Marriage Master PAPI eCloud extension deleted! It is not needed and not compatible with Marriage Master v2.0 and newer.");
				}
				else
				{
					plugin.getLogger().warning("Failed to delete Marriage Master PAPI eCloud extension! Please remove it!\nIt is not needed and not compatible with Marriage Master v2.0 and newer.");
				}
			}
		}
		//endregion
		if(this.register())
		{
			this.plugin.getLogger().info(ConsoleColor.GREEN + "PlaceholderAPI hook was successfully registered!" + ConsoleColor.RESET);
		}
		else
		{
			this.plugin.getLogger().info(ConsoleColor.RED + "PlaceholderAPI hook failed to registered!" + ConsoleColor.RESET);
			this.plugin.getLogger().info("If you currently have the eCloud extension installed please remove it and try again!");
		}
	}

	@Override
	public String onRequest(OfflinePlayer player, String identifier)
	{
		return placeholderManager.replacePlaceholder(player, identifier);
	}

	@Override
	public void close()
	{
		if(PlaceholderAPI.unregisterExpansion(this))
		{
			plugin.getLogger().info(ConsoleColor.GREEN + "PlaceholderAPI hook was successfully unregistered!" + ConsoleColor.RESET);
		}
		else
		{
			plugin.getLogger().info(ConsoleColor.RED + "PlaceholderAPI hook failed to unregistered!" + ConsoleColor.RESET);
		}
	}

	@Override
	public String getName()
	{
		return plugin.getDescription().getName();
	}

	@Override
	public String getIdentifier()
	{
		return plugin.getDescription().getName().toLowerCase();
	}

	@Override
	public String getAuthor()
	{
		return plugin.getDescription().getAuthors().toString();
	}

	@Override
	public String getVersion()
	{
		return plugin.getDescription().getVersion();
	}

	@Override
	public List<String> getPlaceholders()
	{
		return placeholderManager.getPlaceholdersList();
	}

	@Override
	public boolean persist()
	{
		return true;
	}
}