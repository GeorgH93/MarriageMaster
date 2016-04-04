/*
 *   Copyright (C) 2014-2016 GeorgH93
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

package at.pcgamingfreaks.MarriageMaster;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import com.google.common.base.Charsets;
import com.google.gson.Gson;

/**
 * Functions to get UUIDs to player names. This library doesn't cache the results! You will have to do this on your own!
 * Works with Bukkit and BungeeCord!
 */
public class UUIDConverter
{
	private final static Gson GSON = new Gson();

	//region Multi querys
	//TODO: JavaDoc Exception handling, more parameters, fallback
	private final static int BATCH_SIZE = 100; // Limit from Mojang

	private class Profile
	{
		public String id;
		public String name;

		public UUID getUUID()
		{
			return UUID.fromString(id.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
		}
	}

	public static Map<String, String> getUUIDsFromNames(Collection<String> names, boolean onlineMode, boolean withSeparators)
	{
		Map<String, String> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		for(Map.Entry<String, UUID> entry : getUUIDsFromNamesAsUUIDs(names, onlineMode).entrySet())
		{
			result.put(entry.getKey(), (withSeparators) ? entry.getValue().toString() : entry.getValue().toString().replaceAll("-", ""));
		}
		return result;
	}

	public static Map<String, UUID> getUUIDsFromNamesAsUUIDs(Collection<String> names, boolean onlineMode)
	{
		if(onlineMode)
		{
			return getUUIDsFromNamesAsUUIDs(names);
		}
		Map<String,UUID> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		for(String name : names)
		{
			result.put(name, UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(Charsets.UTF_8)));
		}
		return result;
	}

	public static Map<String, UUID> getUUIDsFromNamesAsUUIDs(Collection<String> names)
	{
		List<String> batch = new ArrayList<>();
		Iterator<String> players = names.iterator();
		Map<String,UUID> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		boolean success;
		while (players.hasNext())
		{
			for (int i = 0; players.hasNext() && i < BATCH_SIZE; i++)
			{
				batch.add(players.next());
			}
			do
			{
				HttpURLConnection connection = null;
				try
				{
					connection = (HttpURLConnection) new URL("https://api.mojang.com/profiles/minecraft").openConnection();
					connection.setRequestMethod("POST");
					connection.setRequestProperty("Content-Type", "application/json; encoding=UTF-8");
					connection.setUseCaches(false);
					connection.setDoInput(true);
					connection.setDoOutput(true);
					try(OutputStream out = connection.getOutputStream())
					{
						out.write(GSON.toJson(batch).getBytes(Charsets.UTF_8));
					}
					Profile[] profiles;
					try(Reader in = new BufferedReader(new InputStreamReader(connection.getInputStream())))
					{
						profiles = GSON.fromJson(in, Profile[].class);
					}
					for (Profile profile : profiles)
					{
						result.put(profile.name, profile.getUUID());
					}
				}
				catch(IOException e)
				{
					try
					{
						if(connection != null && connection.getResponseCode() == 429)
						{
							System.out.println("Reached the request limit of the mojang api!\nConverting will be paused for 10 minutes and then continue!");
							//TODO: better fail handling
							Thread.sleep(10*60*1000L);
							success = false;
							continue;
						}
					}
					catch(Exception ignore) {}
					e.printStackTrace();
					return result;
				}
				batch.clear();
				success = true;
			} while(!success);
		}
		return result;
	}
	//endregion
}