/*
 *   Copyright (C) 2014-2015 GeorgH93
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

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class EffectBase
{
	public void SpawnParticle(Location loc, Effects type, double visrange, int count, float offsetX, float offsetY, float offsetZ, float speed) throws Exception {}
	
	public static EffectBase getEffect(boolean compMode)
	{
		EffectBase eb = null;
		String name = Bukkit.getServer().getClass().getPackage().getName();
		String[] version = name.substring(name.lastIndexOf('.') + 2).split("_");
		try
		{
			if(Bukkit.getServer().getName().toLowerCase().contains("cauldron"))
			{
				eb = new Effect_1_7_Cauldron();
			}
			else if(compMode)
			{
				if(version[0].equals("1"))
				{
					if(version[1].equals("7"))
					{
						if(version[2].equals("R1"))
						{
							eb = new Effect_1_7_R1();
						}
						else if(version[2].equals("R2"))
						{
							eb = new Effect_1_7_R2();
						}
						else if(version[2].equals("R3"))
						{
							eb = new Effect_1_7_R3();
						}
						else if(version[2].equals("R4"))
						{
							eb = new Effect_1_7_R4();
						}
					}
					else if(version[1].equals("8"))
					{
						if(version[2].equals("R1"))
						{
							eb = new Effect_1_8_R1();
						}
						else if(version[2].equals("R2"))
						{
							eb = new Effect_1_8_R2();
						}
						else if(version[2].equals("R3"))
						{
							eb = new Effect_1_8_R3();
						}
					}
				}
			}
			else
			{
				if(version[0].equals("1"))
				{
					if(version[1].equals("7"))
					{
						eb = new Effect_1_7();
					}
					else if(version[1].equals("8"))
					{
						eb = new Effect_1_8();
					}
				}
			}
		}
		catch (NoClassDefFoundError e)
		{
			eb = null;
		}
		catch (Exception e)
		{
			eb = null;
		}
		if(eb == null)
		{
			Bukkit.getServer().getLogger().warning("Could not initialize effect spawner. Runing: " + name + ":" + Bukkit.getVersion());
		}
		return eb;
	}
}