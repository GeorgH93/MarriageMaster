/*
 * Copyright (C) 2014-2016 GeorgH93
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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Network;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class Effect_1_7_Cauldron extends EffectBase
{
	private static final Class<?> PACKET_PLAY_OUT_WORLD_PARTICLES = Reflection_Cauldron.getNMSClass("PacketPlayOutWorldParticles");
	private static final Class<?> PACKET = Reflection_Cauldron.getNMSClass("Packet");
	
	@SuppressWarnings("ConstantConditions")
	@Override
	public void spawnParticle(Location loc, Effects type, double visibleRange, int count, float offsetX, float offsetY, float offsetZ, float speed) throws Exception
	{
		if(PACKET_PLAY_OUT_WORLD_PARTICLES == null) return;
		try
		{
			Object packet = PACKET_PLAY_OUT_WORLD_PARTICLES.getConstructor(new Class[] { String.class, float.class, float.class, float.class, float.class, float.class, float.class, float.class, int.class })
					.newInstance(type.getName(), (float) loc.getX(), (float) loc.getY(), (float) loc.getZ(), offsetX, offsetY, offsetZ, speed, count);
			Object handle, connection;
			for(Entity entity : loc.getWorld().getEntities())
			{
				if(entity instanceof Player && entity.getLocation().getWorld().equals(loc.getWorld()) && entity.getLocation().distance(loc) < visibleRange)
				{
					handle = Reflection_Cauldron.getHandle(entity);
					if(handle != null && handle.getClass().getName().endsWith(".EntityPlayerMP"))
					{
						connection = Reflection_Cauldron.getNMSField(handle.getClass(), "playerConnection").get(handle);
						Reflection_Cauldron.getNMSMethod(connection.getClass(), "sendPacket", PACKET).invoke(connection, packet);
					}
				}
			}
		}
		catch (Exception e)
		{
			System.out.println("Unable to spawn particle " + type.getName() + ". (Version 1.7 Cauldron)");
			e.printStackTrace();
		}
	}
}