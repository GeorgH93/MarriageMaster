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

package at.pcgamingfreaks.MarriageMaster.Bungee.Database;

import at.pcgamingfreaks.MarriageMaster.API.Home;
import at.pcgamingfreaks.MarriageMaster.Bungee.Database.UnCacheStrategies.UnCacheStrategie;
import at.pcgamingfreaks.MarriageMaster.Bungee.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Database.BaseDatabase;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.UUID;

public class Database extends BaseDatabase<MarriageMaster, MarriagePlayerData, MarriageData, Home> implements Listener
{
	private final UnCacheStrategie unCacheStrategie;

	public Database(MarriageMaster plugin)
	{
		super(plugin, plugin.getLogger(), new PlatformSpecific(plugin), plugin.getConfig(), plugin.getDescription().getName(), plugin.getDataFolder(), true, true);
		unCacheStrategie = UnCacheStrategie.getUnCacheStrategie(cache);
		plugin.getProxy().getPluginManager().registerListener(plugin, this);
		loadRunnable.run();
	}

	@Override
	public void close()
	{
		plugin.getProxy().getPluginManager().unregisterListener(this);
		unCacheStrategie.close(); // Killing the uncache strategie
		super.close();
	}

	@Override
	public MarriagePlayerData getPlayer(UUID uuid)
	{
		MarriagePlayerData player = cache.getPlayer(uuid);
		if(player == null)
		{
			// We cache all our married players on startup, we also load unmarried players on join. If there is no data for him in the cache we return a new player.
			// It's very likely that he was only requested in order to show a info about his marriage status. When someone change the player the database will fix him anyway.
			ProxiedPlayer pPlayer = plugin.getProxy().getPlayer(uuid);
			player = new MarriagePlayerData(uuid, pPlayer != null ? pPlayer.getName() : "Unknown");
			cache.cache(player); // Let's put the new player into the cache
			load(player);
		}
		return player;
	}

	@SuppressWarnings("unused")
	@EventHandler(priority = Byte.MIN_VALUE) // We want to start the loading of the player as soon as he connects, so he probably is ready as soon as someone requests the player.
	public void onPlayerLoginEvent(PostLoginEvent event)
	{
		getPlayer(event.getPlayer().getUniqueId()); // This will load the player if he isn't loaded yet
	}
}