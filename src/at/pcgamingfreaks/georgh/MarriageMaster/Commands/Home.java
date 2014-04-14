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

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import at.pcgamingfreaks.georgh.MarriageMaster.MarriageMaster;

public class Home 
{
	private MarriageMaster marriageMaster;

	public Home(MarriageMaster marriagemaster) 
	{
		marriageMaster = marriagemaster;
	}

	public void TP(Player player)
	{
		Location loc = marriageMaster.DB.GetMarryHome(player);
		
		if(loc != null)
		{
			if(marriageMaster.config.UseEconomy())
			{
				if(marriageMaster.economy.HomeTeleport(player, marriageMaster.config.GetEconomyHomeTp()))
				{
					TPHome(player, loc);
				}
			}
			else
			{
				TPHome(player, loc);
			}
		}
		else
		{
			player.sendMessage(ChatColor.GREEN + marriageMaster.lang.Get("Ingame.NoHome"));
		}
	}
	
	private void TPHome(Player player, Location loc)
	{
		player.teleport(loc);
		player.sendMessage(ChatColor.GREEN + marriageMaster.lang.Get("Ingame.HomeTP"));
	}

	public void SetHome(Player player)
	{
		Location home = player.getLocation();
		if(marriageMaster.config.UseEconomy())
		{
			if(marriageMaster.economy.HomeTeleport(player, marriageMaster.config.GetEconomySetHome()))
			{
				SetMarryHome(player, home);
			}
		}
		else
		{
			SetMarryHome(player, home);
		}
	}
	
	private void SetMarryHome(Player player, Location home)
	{
		marriageMaster.DB.SetMarryHome(home, player);
		player.sendMessage(ChatColor.GREEN + marriageMaster.lang.Get("Ingame.HomeSet"));
	}
}
