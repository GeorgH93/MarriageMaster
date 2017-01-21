/*
 * Copyright (C) 2016 GeorgH93
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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Database.UnCacheStrategies;

import at.pcgamingfreaks.MarriageMaster.Bukkit.Database.MarriagePlayerData;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Database.Cache;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class OnDisconnectDelayed extends UnCacheStrategie implements Listener
{
	private final long delay;

	public OnDisconnectDelayed(Cache cache)
	{
		super(cache);
		delay = MarriageMaster.getInstance().getConfiguration().getUnCacheDelay();
		Bukkit.getPluginManager().registerEvents(this, MarriageMaster.getInstance());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void playerLeaveEvent(PlayerQuitEvent event)
	{
		final MarriagePlayerData player = cache.getPlayer(event.getPlayer().getUniqueId());
		if(!player.isMarried() && !player.isPriest()) // We only uncache unmarried player.
		{
			new BukkitRunnable()
			{
				@Override
				public void run()
				{
					if(!player.isOnline())
					{
						OnDisconnectDelayed.this.cache.unCache(player);
					}
				}
			}.runTaskLater(MarriageMaster.getInstance(), delay);
		}
	}

	@Override
	public void close()
	{
		HandlerList.unregisterAll(this);
	}
}