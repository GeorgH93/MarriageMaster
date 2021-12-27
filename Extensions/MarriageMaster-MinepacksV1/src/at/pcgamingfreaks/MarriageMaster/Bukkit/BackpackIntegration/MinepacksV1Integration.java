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

package at.pcgamingfreaks.MarriageMaster.Bukkit.BackpackIntegration;

import at.pcgamingfreaks.MinePacks.MinePacks;
import at.pcgamingfreaks.Version;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import lombok.Getter;

import java.util.MissingResourceException;

/**
 * Minepacks v1.x integration
 */
public class MinepacksV1Integration implements IBackpackIntegration
{
	private static final String PLUGIN_NAME = "MinePacks";
	private static final Version MIN_VERSION = new Version("1.17.7");
	private MinePacks minepacks;
	@Getter private final String version;

	public MinepacksV1Integration() throws NullPointerException, BackpackPluginIncompatibleException
	{
		Plugin bukkitPlugin = Bukkit.getPluginManager().getPlugin(PLUGIN_NAME);
		if(!(bukkitPlugin instanceof MinePacks)) throw new MissingResourceException("Plugin " + PLUGIN_NAME + " is not available!", this.getClass().getName(), PLUGIN_NAME);
		version = bukkitPlugin.getDescription().getVersion();
		Version installedVersion = new Version(version, true);
		if(MIN_VERSION.newerThan(installedVersion))
		{
			throw new BackpackPluginIncompatibleException("Your Minepacks version is outdated! Please update in order to use it with this plugin!\n" +
					                                              "You have installed: " + installedVersion + " Required: " + MIN_VERSION);
		}
		minepacks = (MinePacks) bukkitPlugin;
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
		return false;
	}
}