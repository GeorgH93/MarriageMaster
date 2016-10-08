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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Commands;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Network.EffectBase;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Network.Effects;

public class Kiss
{
	private MarriageMaster plugin;
	private Map<String, Long> wait;
	private EffectBase eb = null;

	public Kiss(MarriageMaster marriagemaster) 
	{
		plugin = marriagemaster;
		wait = new HashMap<>();
		eb = EffectBase.getEffect();
		if(eb == null)
		{
			plugin.log.warning(plugin.lang.get("Console.NotSupportedNet"));
		}
	}
	
	public boolean CanKissAgain(String playername)
	{
		if(!wait.containsKey(playername))
		{
			return true;
		}
		if(wait.get(playername) + plugin.config.GetKissWaitTime() < System.currentTimeMillis())
		{
			wait.remove(playername);
			return true;
		}
		return false;
	}
	
	public int GetKissTimeOut(String playername)
	{
		if(!wait.containsKey(playername))
		{
			return 0;
		}
		return (int)((wait.get(playername) + plugin.config.GetKissWaitTime() - System.currentTimeMillis())/1000);
	}
	
	public void kiss(Player player, Player otherPlayer)
	{
		String youKissed = plugin.lang.get("Ingame.YouKissed");
		String youGotKissed = plugin.lang.get("Ingame.YouGotKissed");
		if (youKissed != null && !youKissed.isEmpty())
		{
			player.sendMessage(ChatColor.GREEN + youKissed);
		}
		if (youGotKissed != null && !youGotKissed.isEmpty())
		{
			otherPlayer.sendMessage(ChatColor.GREEN + youGotKissed);
		}

		DrawHearts(otherPlayer);
		DrawHearts(player);
		if(!plugin.CheckPerm(player, "marry.skiptpdelay", false))
		{
			wait.put(player.getName(), System.currentTimeMillis());
		}
	}
	
	public void DrawHearts(Player player)
	{
		if(eb != null)
		{
			Location loc = player.getLocation();
			if(loc == null)
			{
	    		return;
			}
	    	try
	    	{
	    		eb.SpawnParticle(loc, Effects.Heart, plugin.config.GetRange("HearthVisible"), plugin.config.GetKissHearthCount(), 1.0F, 1.0F, 1.0F, 1.0F);
	    	}
	    	catch(Exception e)
	    	{
	    		plugin.log.warning("Failed spawning heart! Bukkit: " + Bukkit.getVersion());
	    		e.printStackTrace();
	    	}
		}
	}
}