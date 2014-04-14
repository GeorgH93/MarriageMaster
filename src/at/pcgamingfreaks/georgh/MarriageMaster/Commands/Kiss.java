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

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import at.pcgamingfreaks.georgh.MarriageMaster.*;
import at.pcgamingfreaks.georgh.MarriageMaster.Network.*;

public class Kiss
{
	private MarriageMaster marriageMaster;
	private Map<String, Long> wait;
	private EffectBase eb = null;

	public Kiss(MarriageMaster marriagemaster) 
	{
		marriageMaster = marriagemaster;
		wait = new HashMap<String, Long>();
		try
		{
			eb = new Effect_1_7_R1();
		}
		catch (NoClassDefFoundError e)
		{
			try
			{
				eb = new Effect_1_7_R2();
			}
			catch (NoClassDefFoundError ex)
			{
				try
				{
					eb = new Effect_1_7_R3();
				}
				catch (NoClassDefFoundError exc)
				{
					eb = null;
					marriageMaster.log.warning(marriageMaster.lang.Get("Console.NotSupportedNet"));
				}
			}
		}
	}
	
	public boolean CanKissAgain(String playername)
	{
		if(!wait.containsKey(playername))
		{
			return true;
		}
		if(wait.get(playername) + marriageMaster.config.GetKissWaitTime() < System.currentTimeMillis())
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
		if(eb != null)
		{
			Location loc = player.getLocation();
			if(loc == null)
			{
	    		return;
			}
	    	try
	    	{
	    		eb.SpawnParticle(loc, "heart", marriageMaster.config.GetRange("HearthVisible"), marriageMaster.config.GetKissHearthCount(), 1.0F, 1.0F, 1.0F, 1.0F);
	    	}
	    	catch(Exception e)
	    	{
	    		e.printStackTrace();
	    	}
		}
	}
}