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

import at.pcgamingfreaks.ConsoleColor;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.regex.Pattern;

public abstract class BackpacksIntegrationBase
{
	public static BackpacksIntegrationBase getIntegration()
	{
		try
		{
			Plugin pl = Bukkit.getPluginManager().getPlugin("MinePacks");
			if(pl != null)
			{
				return new MinePacksIntegration();
			}
		}
		catch(BackpackPluginIncompatibleException e)
		{
			MarriageMaster.getInstance().getLogger().warning(ConsoleColor.RED + " " + e.getMessage() + ConsoleColor.RESET);
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public abstract void openBackpack(Player opener, Player owner, boolean editable);

	public void close() {}
}