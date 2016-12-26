/*
 * Copyright (C) 2016 GeorgH93
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.MarriageMaster.Bukkit.BackpackIntegration;

import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;
import at.pcgamingfreaks.Version;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class MinePacksIntegration extends BackpacksIntegrationBase
{
	private static final Version MIN_VERSION = new Version("2.0");
	private Minepacks minepacks = null;

	public MinePacksIntegration() throws NullPointerException, BackpackPluginIncompatibleException
	{
		if(Bukkit.getPluginManager().getPlugin("Minepacks") == null)
		{
			return;
		}
		RegisteredServiceProvider<Minepacks> mpProvider = Bukkit.getServicesManager().getRegistration(Minepacks.class);
		if(mpProvider != null)
		{
			minepacks = mpProvider.getProvider();
			Version installedVersion = new Version(mpProvider.getPlugin().getDescription().getVersion(), true);
			if(MIN_VERSION.olderOrEqualThan(installedVersion))
			{
				throw new BackpackPluginIncompatibleException("Your MinePacks version is outdated! Please update in order to use it with this plugin!\n" +
						                                              "You have installed: " + installedVersion + " Required: " + MIN_VERSION);
			}
		}
		else
		{
			throw new NullPointerException("Can't retrieve the backpack plugin!");
		}
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
}