/*
 *   Copyright (C) 2022 GeorgH93
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
 *   along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.MarriageMaster.Bukkit.Database;

import at.pcgamingfreaks.Bukkit.Database.Cache.UnCacheStrategies.UnCacheStrategyMaker;
import at.pcgamingfreaks.Database.Cache.BaseUnCacheStrategy;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Database.BaseDatabase;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class Database extends BaseDatabase<MarriageMaster, MarriagePlayerData, MarriageData, MarriageHome> implements Listener
{
	private final BaseUnCacheStrategy unCacheStrategy;

	public Database(@NotNull MarriageMaster plugin)
	{
		super(plugin, plugin.getLogger(), new PlatformSpecific(plugin), plugin.getConfiguration(), plugin.getDescription().getName(), plugin.getDataFolder(), plugin.getConfiguration().isBungeeEnabled(), false);
		if(available())
		{
			unCacheStrategy = UnCacheStrategyMaker.make(plugin, cache, plugin.getConfiguration());
			Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
			if(!plugin.getConfiguration().isBungeeEnabled() || !plugin.getServer().getOnlinePlayers().isEmpty())
			{
				new Thread(loadRunnable).start(); // Load async
			}
		}
		else unCacheStrategy = null;
	}

	@Override
	public void close()
	{
		unCacheStrategy.close(); // Killing the uncache strategy before killing the rest like the caches
		super.close();
		HandlerList.unregisterAll(this);
	}

	public MarriagePlayerData getPlayer(OfflinePlayer bPlayer)
	{
		MarriagePlayerData player = cache.getPlayer(bPlayer.getUniqueId());
		if(player == null)
		{
			player = new MarriagePlayerData(bPlayer);
			cache.cache(player); // Let's put the new player into the cache
			load(player);
		}
		return player;
	}

	@Override
	public MarriagePlayerData getPlayer(UUID uuid)
	{
		MarriagePlayerData player = cache.getPlayer(uuid);
		if(player == null)
		{
			OfflinePlayer bPlayer = Bukkit.getPlayer(uuid);
			if(bPlayer == null) bPlayer = Bukkit.getOfflinePlayer(uuid);
			// We cache all our married players on startup, we also load unmarried players on join. If there is no data for him in the cache we return a new player.
			// It's very likely that he was only requested in order to show a info about his marriage status. When someone change the player the database will fix him anyway.
			player = new MarriagePlayerData(bPlayer);
			cache.cache(player); // Let's put the new player into the cache
			load(player);
		}
		return player;
	}

	@EventHandler(priority = EventPriority.LOWEST) // We want to start the loading of the player as soon as he connects, so he probably is ready as soon as someone requests the player.
	public void onPlayerLoginEvent(PlayerJoinEvent event) // This will load the player if he isn't loaded yet
	{
		handlePlayerJoin(event.getPlayer());
	}

	private void handlePlayerJoin(final Player bPlayer)
	{
		MarriagePlayerData player = cache.getPlayer(bPlayer.getUniqueId());
		if(player == null)
		{
			player = new MarriagePlayerData(bPlayer);
			cache.cache(player);
		}
		if(bungee)
		{
			if(player.getDatabaseKey() == null)
			{
				final MarriagePlayerData fPlayer = player;
				plugin.getScheduler().runAtEntityLater(bPlayer, () -> load(fPlayer), 10);
			}
			else if(!player.getName().equals(bPlayer.getName()))
			{
				player.setName(bPlayer.getName());
			}
		}
		else load(player);
	}

	@Override
	protected void loadOnlinePlayers()
	{
		for(Player player : Bukkit.getServer().getOnlinePlayers())
		{
			handlePlayerJoin(player);
		}
	}
}