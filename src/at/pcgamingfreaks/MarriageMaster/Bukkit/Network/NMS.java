/*
 *   Copyright (C) 2014 GeorgH93
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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;

public class NMS
{
	public static void setValue(Object instance, String fieldName, Object value) throws Exception
	{
		Field field = instance.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(instance, value);
	}

	public static String getVersion()
	{
		String name = Bukkit.getServer().getClass().getPackage().getName();
		String version = name.substring(name.lastIndexOf('.') + 1);
		return version;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Enum<?> getEnum(String enumFullName)
	{
		String[] x = enumFullName.split("\\.(?=[^\\.]+$)");
		if (x.length == 2)
		{
			String enumClassName = x[0];
			String enumName = x[1];
			try
			{
				Class<Enum> cl = (Class<Enum>) Class.forName(enumClassName);
				return Enum.valueOf(cl, enumName);
			}
			catch (ClassNotFoundException e)
			{
				e.printStackTrace();
			}
		}
		return null;
	}

	public static Class<?> getNMSClass(String className)
	{
		String fullName = "net.minecraft.server." + getVersion() + "." + className;
		Class<?> clazz = null;
		try
		{
			clazz = Class.forName(fullName);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return clazz;
	}
	
	public static Class<?> getOBCClass(String className)
	{
		String fullName = "org.bukkit.craftbukkit." + getVersion() + "." + className;
		Class<?> clazz = null;
		try
		{
			clazz = Class.forName(fullName);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return clazz;
	}
	
	public static Object getHandle(Object obj)
	{
		try
		{
			return getMethod(obj.getClass(), "getHandle", new Class[0]).invoke(obj, new Object[0]);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public static Field getField(Class<?> clazz, String name)
	{
		try
		{
			Field field = clazz.getDeclaredField(name);
			field.setAccessible(true);
			return field;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public static Method getMethod(Class<?> clazz, String name, Class<?>... args)
	{
		for (Method m : clazz.getMethods())
		{
			if ((m.getName().equals(name)) && ((args.length == 0) || (ClassListEqual(args, m.getParameterTypes()))))
			{
				m.setAccessible(true);
				return m;
			}
		}
		return null;
	}
	
	public static boolean ClassListEqual(Class<?>[] l1, Class<?>[] l2)
	{
		boolean equal = true;
		if (l1.length != l2.length)
		{
			return false;
		}
		for (int i = 0; i < l1.length; i++)
		{
			if (l1[i] != l2[i])
			{
				equal = false;
				break;
			}
		}
		return equal;
	}
}
