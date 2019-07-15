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

import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class OnDisconnect extends UnCacheStrategie implements Listener
{
	public OnDisconnect(Cache cache)
	{
		super(cache);
		MarriageMaster.getInstance().getProxy().getPluginManager().registerListener(MarriageMaster.getInstance(), this);
	}

	@SuppressWarnings("unused")
	@EventHandler(priority = Byte.MAX_VALUE)
	public void playerLeaveEvent(PlayerDisconnectEvent event)
	{
		MarriagePlayerData player = cache.getPlayer(event.getPlayer().getUniqueId());
		if(!player.isMarried()) // We only uncache unmarried player.
		{
			cache.unCache(player);
		}
	}

	@Override
	public void close()
	{
		MarriageMaster.getInstance().getProxy().getPluginManager().unregisterListener(this);
	}
}