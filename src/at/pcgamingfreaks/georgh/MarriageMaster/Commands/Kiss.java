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

package at.pcgamingfreaks.georgh.MarriageMaster.Commands;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.server.v1_7_R1.PacketPlayOutWorldParticles;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import at.pcgamingfreaks.georgh.MarriageMaster.*;

public class Kiss
{
	private MarriageMaster marriageMaster;
	private Map<String, Long> wait;

	public Kiss(MarriageMaster marriagemaster) 
	{
		marriageMaster = marriagemaster;
		wait = new HashMap<String, Long>();
	}
	
	public boolean CanKissAgain(String playername)
	{
		if(wait.get(playername) + marriageMaster.config.GetKissWaitTime() < System.currentTimeMillis())
		{
			wait.remove(playername);
			return true;
		}
		return false;
	}
	
	public int GetKissTimeOut(String playername)
	{
		return (int)((wait.get(playername) + marriageMaster.config.GetKissWaitTime() - System.currentTimeMillis())/1000);
	}
	
	public void kiss(Player player, Player otherPlayer)
	{
		player.sendMessage(ChatColor.GREEN + marriageMaster.lang.Get("Ingame.YouKissed"));
		otherPlayer.sendMessage(ChatColor.GREEN + marriageMaster.lang.Get("Ingame.YouGotKissed"));
		DrawHearts(otherPlayer);
		DrawHearts(player);
		wait.put(player.getName(), System.currentTimeMillis());
	}
	
	public void DrawHearts(Player player)
	{
		Location loc = player.getLocation();
		if(loc == null)
		{
    		return;
		}
    	try
    	{
    		PacketPlayOutWorldParticles packet;
    		for(Entity entity : loc.getWorld().getEntities())
    		{
    			if(entity instanceof CraftPlayer)
    			{
    				if(entity.getLocation().distance(loc) < 128)
    				{
	    				packet = new PacketPlayOutWorldParticles();
	    				setValue(packet, "a", "heart");
	    				setValue(packet, "b", (float) loc.getX());
	    				setValue(packet, "c", (float) loc.getY());
	    				setValue(packet, "d", (float) loc.getZ());
	    				setValue(packet, "e", 1F);
	    				setValue(packet, "f", 1F);
	    				setValue(packet, "g", 1F);
	    				setValue(packet, "h", 1F);
	    				setValue(packet, "i", 200);
	    				((CraftPlayer)entity).getHandle().playerConnection.sendPacket(packet);
    				}
    			}
    		}
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
	}
	
	private static void setValue(Object instance, String fieldName, Object value) throws Exception
	{
		Field field = instance.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(instance, value);
	}
}