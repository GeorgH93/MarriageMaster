/*
 *   Copyright (C) 2018 GeorgH93
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

package at.pcgamingfreaks.MarriageMaster.Bukkit.BackpackIntegration;

import at.pcgamingfreaks.ConsoleColor;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.Reflection;
import at.pcgamingfreaks.VersionRange;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.LinkedList;
import java.util.List;

public class BackpackIntegrationManager
{
	private static final class BackpackPluginMetadata
	{
		private final String identifier;
		private final VersionRange versionRange;
		private final Class<? extends IBackpackIntegration> integrationClass;

		private BackpackPluginMetadata(String identifier, VersionRange versionRange, Class<? extends IBackpackIntegration> integrationClass)
		{
			this.identifier = identifier;
			this.versionRange = versionRange;
			this.integrationClass = integrationClass;
		}
	}

	private static IBackpackIntegration integration = null;

	private static final List<BackpackPluginMetadata> compatiblePlugins = new LinkedList<>();

	private static void registerCompatiblePlugin(String identifier, VersionRange versionRange, Class<? extends IBackpackIntegration> integrationClass)
	{
		compatiblePlugins.add(new BackpackPluginMetadata(identifier, versionRange, integrationClass));
	}

	static
	{
		registerCompatiblePlugin("MinePacks", new VersionRange("1.17.7", "1.999"), MinepacksV1Integration.class);
		registerCompatiblePlugin("Minepacks", new VersionRange("2.0-SNAPSHOT", "2.999"), MinepacksV2Integration.class);
	}

	public static IBackpackIntegration getIntegration()
	{
		return integration;
	}

	public static void initIntegration()
	{
		for(BackpackPluginMetadata plugin : compatiblePlugins)
		{
			//noinspection CatchMayIgnoreException
			try
			{
				Plugin bukkitPlugin = Bukkit.getPluginManager().getPlugin(plugin.identifier);
				if(bukkitPlugin != null && plugin.versionRange.inRange(bukkitPlugin.getDescription().getVersion()) && bukkitPlugin.isEnabled())
				{
					integration = (IBackpackIntegration) Reflection.getConstructor(plugin.integrationClass).newInstance();
					MarriageMaster.getInstance().getLogger().info(ConsoleColor.GREEN + "Successful linked with " + ConsoleColor.YELLOW + plugin.identifier + " v" + bukkitPlugin.getDescription().getVersion() + ConsoleColor.GREEN + "!" + ConsoleColor.RESET);
					return;
				}
			}
			catch(Throwable t)
			{
				if(t.getCause() instanceof BackpackPluginIncompatibleException) MarriageMaster.getInstance().getLogger().warning(ConsoleColor.RED + " " + t.getCause().getMessage() + ConsoleColor.RESET);
			}
		}
		MarriageMaster.getInstance().getLogger().info("No compatible backpack plugin found.");
	}
}