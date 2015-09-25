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

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.regex.Pattern;

public abstract class MinePacksIntegrationBase
{
	public abstract void OpenBackpack(Player opener, Player owner, boolean editable);

	public static MinePacksIntegrationBase getIntegration()
	{
		MinePacksIntegrationBase mpIB = null;
		try
		{
			Plugin pl = Bukkit.getServer().getPluginManager().getPlugin("MinePacks");
			if(pl != null)
			{
				String[] MPV = (pl.getDescription().getVersion().split("-")[0]).split(Pattern.quote("."));
				if(Integer.parseInt(MPV[0]) > 1 || (MPV.length > 1 && Integer.parseInt(MPV[0]) == 1 && Integer.parseInt(MPV[1]) >= 14))
				{
					mpIB = new MinePacksIntegrationNew();
				}
				else
				{
					mpIB = new MinePacksIntegrationOld();
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return mpIB;
	}
}