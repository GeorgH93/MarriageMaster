/*
 *   Copyright (C) 2019 GeorgH93
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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Listener;

import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.MarriagePlayerJoinEvent;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.MarriagePlayerQuitEvent;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.MarriedPlayerJoinEvent;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.MarriedPlayerQuitEvent;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Creates the Join/Quit for married players
 */
public class JoinLeaveWorker implements Listener
{
	private final MarriageMaster plugin;

	public JoinLeaveWorker(MarriageMaster plugin)
	{
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onJoin(PlayerJoinEvent event)
	{
		MarriagePlayer player = plugin.getPlayerData(event.getPlayer());
		Bukkit.getPluginManager().callEvent(new MarriagePlayerJoinEvent(player));
		if(player.isMarried())
		{
			Bukkit.getPluginManager().callEvent(new MarriedPlayerJoinEvent(player));
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onLeave(PlayerQuitEvent event)
	{
		MarriagePlayer player = plugin.getPlayerData(event.getPlayer());
		Bukkit.getPluginManager().callEvent(new MarriagePlayerQuitEvent(player));
		if(player.isMarried())
		{
			Bukkit.getPluginManager().callEvent(new MarriedPlayerQuitEvent(player));
		}
	}
}