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

package at.pcgamingfreaks.MarriageMaster.Bukkit.BackpackIntegration;

import at.pcgamingfreaks.MinePacks.MinePacks;
import at.pcgamingfreaks.Version;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import lombok.Getter;

import java.util.MissingResourceException;

class MinepacksV1Integration implements IBackpackIntegration
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
	public void openBackpack(Player opener, Player owner, boolean editable)
	{
		minepacks.openBackpack(opener, owner, editable);
	}

	@Override
	public void close()
	{
		minepacks = null;
	}

	@Override
	public String getName()
	{
		return PLUGIN_NAME;
	}
}