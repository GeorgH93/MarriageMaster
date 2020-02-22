/*
 * Copyright (C) 2016, 2018 GeorgH93
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

public class IntervalChecked extends UnCacheStrategie implements Runnable
{
	private final long delay;
	private final int taskID;

	public IntervalChecked(Cache cache)
	{
		super(cache);
		long delay = MarriageMaster.getInstance().getConfiguration().getUnCacheDelay();
		taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(MarriageMaster.getInstance(), this, delay, MarriageMaster.getInstance().getConfiguration().getUnCacheInterval());
		this.delay = delay * 50L;
	}

	@Override
	public void run()
	{
		long currentTime = System.currentTimeMillis() - delay;
		for(MarriagePlayerData player : cache.getLoadedPlayers())
		{
			if(!player.isOnline() && player.getPlayer().getLastPlayed() < currentTime && !player.isMarried() && !player.isPriest())
			{
				this.cache.unCache(player);
			}
		}
	}

	@Override
	public void close()
	{
		Bukkit.getScheduler().cancelTask(taskID);
	}
}