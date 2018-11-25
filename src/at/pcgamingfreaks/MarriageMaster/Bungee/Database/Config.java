/*
 *   Copyright (C) 2014-2018 GeorgH93
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import at.pcgamingfreaks.MarriageMaster.Bungee.MarriageMaster;

import com.google.common.io.ByteStreams;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class Config
{
	private MarriageMaster plugin;
	private Configuration config;
	private ConfigurationProvider configurationProvider;
	private static final int CONFIG_VERSION = 7;
	
	public Config(MarriageMaster marriagemaster)
	{
		plugin = marriagemaster;
		configurationProvider = ConfigurationProvider.getProvider(YamlConfiguration.class);
		loadConfig();
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	private void loadConfig()
	{
		File file = new File(plugin.getDataFolder(), "config.yml");
		if(!file.exists())
		{
			try
			{
				if (!plugin.getDataFolder().exists())
				{
					plugin.getDataFolder().mkdir();
				}
				file.createNewFile();
				try (InputStream is = plugin.getResourceAsStream("bungee_config.yml"); OutputStream os = new FileOutputStream(file))
				{
					ByteStreams.copy(is, os);
				}
				plugin.log.info("Config extracted successfully!");
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		try
		{
			config = configurationProvider.load(file);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		updateConfig(file);
	}
	
	private void updateConfig(File file)
	{
		switch(config.getInt("Version"))
		{
			case 1: config.set("DelayMessageForJoiningPlayer", 1);
			case 2: config.set("Misc.DisableV2Info", false);
			case 3: config.set("Database.MySQL.Properties", new ArrayList<>());
			case 4: config.set("Database.UUID_Type", "online");
			case 5:
			case 6: config.set("Database.MySQL.MaxConnections", 2);
				break;
			case CONFIG_VERSION: return;
			default: plugin.log.info("Config File Version newer than expected!"); return;
		}
		config.set("Version", CONFIG_VERSION);
		try
		{
			configurationProvider.save(config, file);
			plugin.log.info("Config File has been updated.");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	// Settings Reader
	public String getLanguage()
	{
		return config.getString("Language");
	}
	
	public String getLanguageUpdateMode()
	{
		return config.getString("LanguageUpdateMode");
	}
	
	public boolean getUseUpdater()
	{
		return config.getBoolean("Misc.AutoUpdate");
	}
	
	public boolean getInformOnPartnerJoinEnabled()
	{
		return config.getBoolean("InformOnPartnerJoin");
	}

	public int getDelayMessageForJoiningPlayer()
	{
		return config.getInt("DelayMessageForJoiningPlayer", 1);
	}
	
	public boolean getChatGlobal()
	{
		return config.getBoolean("Chat.Global");
	}
	
	public String getChatToggleCommand()
	{
		String cmd = config.getString("Chat.ToggleCommand").toLowerCase();
		if(cmd.equals("chattoggle"))
		{
			//noinspection SpellCheckingInspection
			return "ctoggle";
		}
		return cmd;
	}
	
	public String getChatFormat()
	{
		return ChatColor.translateAlternateColorCodes('&', config.getString("Chat.PrivateFormat").replace("<heart>", ChatColor.RED + "\u2764" + ChatColor.WHITE));
	}
	
	public boolean getHomeGlobal()
	{
		return config.getBoolean("Home.Global");
	}
	
	public boolean getHomeDelayed()
	{
		return config.getBoolean("Home.Delayed");
	}
	
	public HashSet<String> getHomeFromServersBlocked()
	{
		HashSet<String> blockFrom = new HashSet<>();
		for(String s : config.getStringList("Home.FromServersBlocked"))
		{
			blockFrom.add(s.toLowerCase());
		}
		return blockFrom;
	}
	
	public boolean getTPGlobal()
	{
		return config.getBoolean("TP.Global");
	}
	
	public boolean getTPDelayed()
	{
		return config.getBoolean("TP.Delayed");
	}
	
	public HashSet<String> getTPFromServersBlocked()
	{
		HashSet<String> blockFrom = new HashSet<>();
		for(String s : config.getStringList("TP.FromServersBlocked"))
		{
			blockFrom.add(s.toLowerCase());
		}
		return blockFrom;
	}
	
	// DB Settings
	public String getMySQLHost()
	{
		return config.getString("Database.MySQL.Host");
	}
	
	public String getMySQLDatabase()
	{
		return config.getString("Database.MySQL.Database");
	}
	
	public String getMySQLUser()
	{
		return config.getString("Database.MySQL.User");
	}
	
	public String getMySQLPassword()
	{
		return config.getString("Database.MySQL.Password");
	}

	public String getMySQLProperties()
	{
		List<String> list = config.getStringList("Database.MySQL.Properties");
		StringBuilder str = new StringBuilder();
		if(list != null)
		{
			for(String s : list)
			{
				str.append("&").append(s);
			}
		}
		return str.toString();
	}

	public int getMySQLMaxConnections()
	{
		return Math.max(1, config.getInt("Database.MySQL.MaxConnections", 2));
	}

	public String getUUIDType()
	{
		return config.getString("Database.UUID_Type", "auto");
	}

	public String getUserTable()
	{
		return config.getString("Database.Tables.User", "marry_players");
	}
	
	public String getHomesTable()
	{
		return config.getString("Database.Tables.Home", "marry_home");
	}
	
	public String getPartnersTable()
	{
		return config.getString("Database.Tables.Partner", "marry_partners");
	}
	
	public boolean getUpdatePlayer()
	{
		return config.getBoolean("Database.UpdatePlayer", true);
	}

	public boolean isV2InfoDisabled()
	{
		return config.getBoolean("Misc.DisableV2Info", false);
	}
}