/*
 *   Copyright (C) 2014-2016 GeorgH93
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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Network;

import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class EffectBase
{
	public void spawnParticle(Location loc, Effects type, double visibleRange, int count, float offsetX, float offsetY, float offsetZ, float speed) throws Exception {}
	
	public static EffectBase getEffect(MarriageMaster plugin)
	{
		EffectBase eb = null;
		String name = Bukkit.getServer().getClass().getPackage().getName();
		try
		{
			if(Bukkit.getServer().getName().toLowerCase().contains("cauldron"))
			{
				eb = new Effect_1_7_Cauldron();
			}
			else
			{
				String[] version = name.substring(name.lastIndexOf('.') + 2).split("_");
				if(version[0].equals("1"))
				{
					if(version[1].equals("7"))
					{
						eb = new Effect_1_7();
					}
					else if(version[1].equals("8") || version[1].equals("9") || version[1].equals("10") || version[1].equals("11") || version[1].equals("12"))
					{
						eb = new Effect_1_8_AndNewer();
					}
				}
			}
		}
		catch (NoClassDefFoundError | Exception e)
		{
			eb = null;
		}
		if(eb == null)
		{
			plugin.getLogger().warning("Could not initialize effect spawner. Running: " +
					                                       plugin.getName() + " (" + plugin.getDescription().getVersion() + ") with" + name + " (" + Bukkit.getVersion() + ")");
			plugin.getLogger().warning("Not supported MC version. Heart effect disabled.");
		}
		return eb;
	}
}