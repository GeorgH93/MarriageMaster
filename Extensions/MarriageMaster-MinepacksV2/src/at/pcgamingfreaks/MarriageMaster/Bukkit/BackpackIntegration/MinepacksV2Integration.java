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
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.MarriageMaster.Bukkit.BackpackIntegration;

import at.pcgamingfreaks.Minepacks.Bukkit.API.MinepacksPlugin;
import at.pcgamingfreaks.Version;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import lombok.Getter;

import java.util.MissingResourceException;

/**
 * Minepacks v2.x integration
 */
public class MinepacksV2Integration implements IBackpackIntegration
{
	private static final String PLUGIN_NAME = "Minepacks";
	private static final Version MIN_VERSION = new Version("2.0"), ITEM_FILTER_MIN_VERSION = new Version("2.3.8");
	private MinepacksPlugin minepacks;
	private final boolean supportsItemFilter;
	@Getter private final String version;

	public MinepacksV2Integration() throws NullPointerException, BackpackPluginIncompatibleException
	{
		Plugin bukkitPlugin = Bukkit.getPluginManager().getPlugin(PLUGIN_NAME);
		if(!(bukkitPlugin instanceof MinepacksPlugin)) throw new MissingResourceException("Plugin " + PLUGIN_NAME + " is not available!", this.getClass().getName(), PLUGIN_NAME);
		this.version = bukkitPlugin.getDescription().getVersion();
		Version installedVersion = new Version(version, true);
		if(MIN_VERSION.newerThan(installedVersion))
		{
			throw new BackpackPluginIncompatibleException("Your Minepacks version is outdated! Please update in order to use it with this plugin!\n" +
					                                              "You have installed: " + installedVersion + " Required: " + MIN_VERSION);
		}
		supportsItemFilter = ITEM_FILTER_MIN_VERSION.olderThan(installedVersion);
		minepacks = (MinepacksPlugin) bukkitPlugin;
	}

	@Override
	public void openBackpack(final @NotNull Player opener, final @NotNull Player owner, final boolean editable)
	{
		minepacks.openBackpack(opener, owner, editable);
	}

	@Override
	public void close()
	{
		minepacks = null;
	}

	@Override
	public @NotNull String getName()
	{
		return PLUGIN_NAME;
	}

	@Override
	public boolean isBackpackItem(ItemStack item)
	{
		if(!supportsItemFilter) return false;
		return minepacks.isBackpackItem(item);
	}
}