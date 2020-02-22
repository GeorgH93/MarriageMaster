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

package at.pcgamingfreaks.MarriageMaster.Bungee.Database.UnCacheStrategies;

import at.pcgamingfreaks.MarriageMaster.Bungee.Database.MarriagePlayerData;
import at.pcgamingfreaks.MarriageMaster.Bungee.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Database.Cache;

import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.util.concurrent.TimeUnit;

public class Interval extends UnCacheStrategie implements Runnable
{
	private ScheduledTask task;

	public Interval(Cache cache)
	{
		super(cache);
		MarriageMaster plugin = MarriageMaster.getInstance();
		task = plugin.getProxy().getScheduler().schedule(plugin, this, plugin.getConfig().getUnCacheDelay(), plugin.getConfig().getUnCacheInterval(), TimeUnit.SECONDS);
	}

	@Override
	public void run()
	{
		for(MarriagePlayerData player : cache.getLoadedPlayers())
		{
			if(!player.isOnline() && !player.isMarried())
			{
				cache.unCache(player);
			}
		}
	}

	@Override
	public void close()
	{
		task.cancel();
	}
}