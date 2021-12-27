/*
 *   Copyright (C) 2021 GeorgH93
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

package at.pcgamingfreaks.MarriageMaster.Database;

import at.pcgamingfreaks.Database.Cache.ICacheablePlayer;
import at.pcgamingfreaks.Database.Cache.IPlayerCache;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caches the players and marriages for the plugin.
 */
public final class Cache<MARRIAGE_PLAYER_DATA extends MarriagePlayerDataBase, MARRIAGE_DATA extends MarriageDataBase> implements IPlayerCache
{
	private final Map<Object, MARRIAGE_PLAYER_DATA> databasePlayers = new ConcurrentHashMap<>(); // To resolve players from database key
	private final Map<Object, MARRIAGE_DATA> databaseMarriages = new ConcurrentHashMap<>(); // To resolve marriages form database key
	private final Map<UUID, MARRIAGE_PLAYER_DATA> players = new ConcurrentHashMap<>(); // To resolve players from their UUID
	private final Map<String, MARRIAGE_DATA> surnames = new ConcurrentHashMap<>(); // To resolve marriages from their surname
	private final Set<MARRIAGE_DATA> marriages = ConcurrentHashMap.newKeySet(); // Set containing all the marriages

	/**
	 * Creates a new cache instance
	 */
	public Cache() {}

	public void close()
	{
		clear();
	}

	/**
	 * Clears the cache
	 */
	public void clear()
	{
		databasePlayers.clear();
		databaseMarriages.clear();
		players.clear();
		surnames.clear();
		marriages.clear();
	}

	public void reCacheSurnames()
	{
		surnames.clear();
		for(MARRIAGE_DATA marriage : marriages)
		{
			if(marriage.getSurname() != null && !marriage.getSurname().isEmpty())
			{
				surnames.put(marriage.getSurname(), marriage);
			}
		}
	}

	public MARRIAGE_PLAYER_DATA getPlayer(UUID uuid)
	{
		return players.get(uuid);
	}

	public Collection<MARRIAGE_PLAYER_DATA> getLoadedPlayers()
	{
		return players.values();
	}

	public Collection<MARRIAGE_DATA> getLoadedMarriages()
	{
		return marriages;
	}

	public Collection<String> getSurnames()
	{
		return surnames.keySet();
	}

	public void cache(MARRIAGE_PLAYER_DATA player)
	{
		players.put(player.getUUID(), player);
		if(player.getDatabaseKey() != null)
		{
			databasePlayers.put(player.getDatabaseKey(), player);
		}
	}

	public void cache(MARRIAGE_DATA marriage)
	{
		marriages.add(marriage);
		if(marriage.getDatabaseKey() != null)
		{
			databaseMarriages.put(marriage.getDatabaseKey(), marriage);
		}
		if(marriage.getSurname() != null && !marriage.getSurname().isEmpty())
		{
			addSurname(marriage);
		}
	}

	public void unCache(MARRIAGE_PLAYER_DATA player)
	{
		players.remove(player.getUUID());
		if(player.getDatabaseKey() != null)
		{
			databasePlayers.remove(player.getDatabaseKey());
		}
	}

	public void unCache(MARRIAGE_DATA marriage)
	{
		marriages.remove(marriage);
		if(marriage.getDatabaseKey() != null)
		{
			databaseMarriages.remove(marriage.getDatabaseKey());
		}
		if(marriage.getSurname() != null && !marriage.getSurname().isEmpty())
		{
			removeSurname(marriage.getSurname());
		}
	}

	public void addDbKey(MARRIAGE_PLAYER_DATA player)
	{
		if(player.getDatabaseKey() != null)
		{
			databasePlayers.put(player.getDatabaseKey(), player);
		}
	}

	public void addDbKey(MARRIAGE_DATA marriage)
	{
		if(marriage.getDatabaseKey() != null)
		{
			databaseMarriages.put(marriage.getDatabaseKey(), marriage);
		}
	}

	public MARRIAGE_DATA getMarriageFromDbKey(Object key)
	{
		return databaseMarriages.get(key);
	}

	public boolean isPlayerFromDbKeyLoaded(Object key)
	{
		return databasePlayers.containsKey(key);
	}

	public MARRIAGE_PLAYER_DATA getPlayerFromDbKey(Object key)
	{
		return databasePlayers.get(key);
	}

	public void removeSurname(String surname)
	{
		surnames.remove(surname);
	}

	public void addSurname(MARRIAGE_DATA marriage)
	{
		surnames.put(marriage.getSurname(), marriage);
	}

	@Override
	public @Nullable ICacheablePlayer getCachedPlayer(@NotNull UUID uuid)
	{
		return getPlayer(uuid);
	}

	@Override
	public void unCache(@NotNull ICacheablePlayer player)
	{
		unCache((MARRIAGE_PLAYER_DATA) player);
	}

	@Override
	public @NotNull Collection<? extends ICacheablePlayer> getCachedPlayers()
	{
		return getLoadedPlayers();
	}
}