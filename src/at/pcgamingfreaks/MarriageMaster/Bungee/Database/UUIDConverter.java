/*
 *   Copyright (C) 2014-2015 GeorgH93
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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;
import java.util.UUID;

import net.md_5.bungee.api.ProxyServer;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;

public class UUIDConverter
{
	private static Gson gson = new Gson();
	
	protected static String getNameFromUUID(String uuid)
	{
		String name = null;
		try
		{
			URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid);
			URLConnection connection = url.openConnection();
			Scanner jsonScanner = new Scanner(connection.getInputStream(), "UTF-8");
			String json = jsonScanner.next();
			JsonParser parser = new JsonParser();
			Object obj = parser.parse(json);
			name = (((JsonObject)obj).get("name")).toString();
			jsonScanner.close();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return name;
	}

	protected static String getUUIDFromName(String name)
	{
		if(ProxyServer.getInstance().getConfigurationAdapter().getBoolean("online_mode", true))
		{
			try
			{
				ProfileData profC = new ProfileData(name);
				String UUID = null;
				for (int i = 1; i <= 100; i++)
				{
					PlayerProfile[] result = post(new URL("https://api.mojang.com/profiles/page/" + i), Proxy.NO_PROXY, gson.toJson(profC).getBytes());
					if (result.length == 0)
					{
						break;
					}
					UUID = result[0].getId();
				}
				return UUID;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
		    return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(Charsets.UTF_8)).toString();
		}
		return null;
	}

	private static PlayerProfile[] post(URL url, Proxy proxy, byte[] bytes) throws IOException
	{
		HttpURLConnection connection = (HttpURLConnection) url.openConnection(proxy);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "application/json");
		connection.setDoInput(true);
		connection.setDoOutput(true);
	
		DataOutputStream out = new DataOutputStream(connection.getOutputStream());
		out.write(bytes);
		out.flush();
		out.close();
	
		BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		StringBuffer response = new StringBuffer();
		String line;
		while ((line = reader.readLine()) != null)
		{
			response.append(line);
			response.append('\r');
		}
		reader.close();
		return gson.fromJson(response.toString(), SearchResult.class).getProfiles();
	}

	private static class PlayerProfile
	{
		private String id;
		public String getId()
		{
			return id;
		}
	}

	private static class SearchResult
	{
		private PlayerProfile[] profiles;
		public PlayerProfile[] getProfiles()
		{
			return profiles;
		}
	}

	private static class ProfileData
	{ 
		@SuppressWarnings("unused")
		private String name;
		@SuppressWarnings("unused")
		private String agent = "minecraft";
		public ProfileData(String name)
		{
			this.name = name;
		}
	}
}