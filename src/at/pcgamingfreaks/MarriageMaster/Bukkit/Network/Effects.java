/*
 *   Copyright (C) 2014-2017 GeorgH93
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

public enum Effects
{
	Heart(34, "heart", "HEART");
	
	private final int id;
	private final String name, nameUpperCase, newName;
	private final Enum<?> nmsEnumParticle;
	
	Effects(int ID, String NAME, String NEWNAME)
	{
		id = ID;
		name = NAME;
		nameUpperCase = name.toUpperCase();
		newName = NEWNAME;
		if(Reflection.getVersion().contains("1_8") || Reflection.getVersion().contains("1_9") || Reflection.getVersion().contains("1_10") || Reflection.getVersion().contains("1_11") || Reflection.getVersion().contains("1_12"))
		{
			//noinspection ConstantConditions
			nmsEnumParticle = Reflection.getEnum(Reflection.getNMSClass("EnumParticle").getName() + "." + (newName));
		}
		else
		{
			nmsEnumParticle = null;
		}
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getNameUpperCase()
	{
		return nameUpperCase;
	}
	
	public String getNewName()
	{
		return newName;
	}
	
	public int getID()
	{
		return id;
	}
	
	public Enum<?> getEnum()
	{
		return nmsEnumParticle;
	}
}