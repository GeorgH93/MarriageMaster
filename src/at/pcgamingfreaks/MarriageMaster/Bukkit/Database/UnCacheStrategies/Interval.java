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

import at.pcgamingfreaks.MarriageMaster.Bukkit.Database.Database;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Database.MarriagePlayerData;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;

import org.bukkit.Bukkit;

public class Interval extends UnCacheStrategie implements Runnable
{
	final int taskID;

	public Interval(Database cache)
	{
		super(cache);
		taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(MarriageMaster.getInstance(), this, MarriageMaster.getInstance().getConfiguration().getUnCacheDelay(), MarriageMaster.getInstance().getConfiguration().getUnCacheInterval());
	}

	@Override
	public void run()
	{
		for(MarriagePlayerData player : cache.getLoadedPlayers())
		{
			if(!player.isOnline() && !player.isMarried())
			{
				this.cache.unCache(player);
			}
		}
	}

	@Override
	public void close()
	{
		Bukkit.getScheduler().cancelTask(taskID);
		super.close();
	}
}