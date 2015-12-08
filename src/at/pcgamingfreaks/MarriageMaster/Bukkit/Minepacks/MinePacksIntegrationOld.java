/*
 * Copyright (C) 2014-2015 GeorgH93
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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Minepacks;

import at.pcgamingfreaks.georgh.MinePacks.MinePacks;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class MinePacksIntegrationOld extends MinePacksIntegrationBase
{
	private MinePacks minepacks;

	public MinePacksIntegrationOld()
	{
		if(Bukkit.getServer().getPluginManager().getPlugin("MinePacks") == null)
		{
			return;
		}
		RegisteredServiceProvider<MinePacks> mpProvider = Bukkit.getServer().getServicesManager().getRegistration(MinePacks.class);
		if (mpProvider != null)
		{
			minepacks = mpProvider.getProvider();
		}
	}

	@Override
	public void OpenBackpack(Player opener, Player owner, boolean editable)
	{
		minepacks.OpenBackpack(opener, owner, editable);
	}
}