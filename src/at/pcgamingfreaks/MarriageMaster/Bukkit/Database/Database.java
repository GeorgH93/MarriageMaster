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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Database;

import at.pcgamingfreaks.MarriageMaster.Bukkit.Database.UnCacheStrategies.UnCacheStrategie;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Database.BaseDatabase;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class Database extends BaseDatabase<MarriageMaster, MarriagePlayerData, MarriageData, MarriageHome> implements Listener
{
	private final UnCacheStrategie unCacheStrategie;

	public Database(@NotNull MarriageMaster plugin)
	{
		super(plugin, plugin.getLogger(), new PlatformSpecific(plugin), plugin.getConfiguration(), plugin.getDescription().getName(), plugin.getDataFolder(), plugin.getConfiguration().isBungeeEnabled(), false);
		unCacheStrategie  = UnCacheStrategie.getUnCacheStrategie(cache);
		startup();
	}

	@Override
	protected void startup()
	{
		super.startup();
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@Override
	public void close()
	{
		unCacheStrategie.close(); // Killing the uncache strategie before killing the rest like the caches
		super.close();
		HandlerList.unregisterAll(this);
	}

	@Override
	public MarriagePlayerData getPlayer(UUID uuid)
	{
		MarriagePlayerData player = cache.getPlayer(uuid);
		if(player == null)
		{
			// We cache all our married players on startup, we also load unmarried players on join. If there is no data for him in the cache we return a new player.
			// It's very likely that he was only requested in order to show a info about his marriage status. When someone change the player the database will fix him anyway.
			player = new MarriagePlayerData(Bukkit.getOfflinePlayer(uuid));
			cache.cache(player); // Let's put the new player into the cache
			load(player);
		}
		return player;
	}

	@EventHandler(priority = EventPriority.LOWEST) // We want to start the loading of the player as soon as he connects, so he probably is ready as soon as someone requests the player.
	public void onPlayerLoginEvent(PlayerJoinEvent event) // This will load the player if he isn't loaded yet
	{
		UUID uuid = event.getPlayer().getUniqueId();
		MarriagePlayerData player = cache.getPlayer(uuid);
		if(player == null)
		{
			// We cache all our married players on startup, we also load unmarried players on join. If there is no data for him in the cache we return a new player.
			// It's very likely that he was only requested in order to show a info about his marriage status. When someone change the player the database will fix him anyway.
			player = new MarriagePlayerData(Bukkit.getOfflinePlayer(uuid));
			cache.cache(player); // Let's put the new player into the cache
		}
		load(player);
	}
}