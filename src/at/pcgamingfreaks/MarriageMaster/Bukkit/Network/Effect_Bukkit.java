/*
 *   Copyright (C) 2016 GeorgH93
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

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Effect_Bukkit extends EffectBase
{
	private final static Class<?> ENTITY_PLAYER = Reflection.getNMSClass("EntityPlayer");
	private final static Method SEND_PACKET = Reflection.getMethod(Reflection.getNMSClass("PlayerConnection"), "sendPacket");
	private final static Field PLAYER_CONNECTION = Reflection.getField(ENTITY_PLAYER, "playerConnection");

	public static void sendPacket(Player player, Object packet) throws IllegalAccessException, InvocationTargetException
	{
		if(SEND_PACKET == null || PLAYER_CONNECTION == null)
			return;
		Object handle = Reflection.getHandle(player);
		if(handle != null && handle.getClass().equals(ENTITY_PLAYER)) // If it's not a real player we can't send him the packet
		{
			SEND_PACKET.invoke(PLAYER_CONNECTION.get(handle), packet);
		}
	}

	protected void spawnParticle(Location location, double visibleRange, Object particle) throws IllegalAccessException, InvocationTargetException
	{
		if(particle == null)
			return;
		for(Entity entity : location.getWorld().getEntities())
		{
			if(entity instanceof Player && entity.getLocation().getWorld().getName().equalsIgnoreCase(location.getWorld().getName()) && entity.getLocation().distance(location) < visibleRange)
			{
				sendPacket((Player) entity, particle);
			}
		}
	}
}