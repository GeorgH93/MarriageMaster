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

import at.pcgamingfreaks.Bungee.Configuration;
import at.pcgamingfreaks.MarriageMaster.Database.DatabaseConfiguration;
import at.pcgamingfreaks.YamlFileManager;

import net.md_5.bungee.api.plugin.Plugin;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Config extends Configuration implements DatabaseConfiguration
{
	private static final int CONFIG_VERSION = 100;

	public Config(Plugin plugin)
	{
		super(plugin, CONFIG_VERSION, 100);
	}

	@Override
	protected void doUpgrade(YamlFileManager oldConfig)
	{
		if(oldConfig.getVersion() < 100)
		{
			try
			{
				getConfigE().set("Language.Language", oldConfig.getYamlE().getString("Language", "en"));
				getConfigE().set("InfoOnPartnerJoinLeave.Enable", oldConfig.getYamlE().getBoolean("InformOnPartnerJoin", true));
				getConfigE().set("InfoOnPartnerJoinLeave.JoinDelay", oldConfig.getYamlE().getInt("DelayMessageForJoiningPlayer", 0));
				getConfigE().set("Database.UseUUIDs", oldConfig.getYamlE().getBoolean("UseUUIDs", true));
				getConfigE().set("Database.UUID_Type ", oldConfig.getYamlE().getString("UUID_Type", "auto"));
				getConfigE().set("Database.Type", oldConfig.getYamlE().getString("Database.Type", "SQLite"));
				getConfigE().set("Database.SQL.Host", oldConfig.getYamlE().getString("Database.MySQL.Host", "localhost"));
				getConfigE().set("Database.SQL.Database", oldConfig.getYamlE().getString("Database.MySQL.Database", "minecraft"));
				getConfigE().set("Database.SQL.User", oldConfig.getYamlE().getString("Database.MySQL.User", "minecraft"));
				getConfigE().set("Database.SQL.Password", oldConfig.getYamlE().getString("Database.MySQL.Password", "minecraft"));
				getConfigE().set("Database.SQL.MaxConnections", oldConfig.getYamlE().getInt("Database.MySQL.MaxConnections", 4));
				getConfigE().set("Database.SQL.Tables.User", oldConfig.getYamlE().getString("Database.Tables.User", "marry_players"));
				getConfigE().set("Database.SQL.Tables.Priests", oldConfig.getYamlE().getString("Database.Tables.Priests", "marry_priests"));
				getConfigE().set("Database.SQL.Tables.Partner", oldConfig.getYamlE().getString("Database.Tables.Partner", "marry_partners"));
				getConfigE().set("Database.SQL.Tables.Home", oldConfig.getYamlE().getString("Database.Tables.Home", "marry_home"));
				getConfigE().set("Marriage.Surnames.Enable", oldConfig.getYamlE().getBoolean("Surname", false));
				getConfigE().set("Marriage.Surnames.AllowColors", oldConfig.getYamlE().getBoolean("AllowSurnameColors", false));
				getConfigE().set("Marriage.Surnames.AllowedCharacters", oldConfig.getYamlE().getString("AllowedSurnameCharacters", "A-Za-z"));
				getConfigE().set("Misc.AutoUpdate", oldConfig.getYamlE().getBoolean("Misc.AutoUpdate", true));
				getConfigE().set("Chat.Global", oldConfig.getYamlE().getBoolean("Chat.Global", true));
				getConfigE().set("Teleport.Delayed", oldConfig.getYamlE().getBoolean("TP.Delayed", false));
				getConfigE().set("Teleport.Global", oldConfig.getYamlE().getBoolean("TP.Global", true));
				getConfigE().set("Teleport.BlockedFrom", oldConfig.getYamlE().getStringList("TP.BlockedFrom", new LinkedList<>()));
				getConfigE().set("Teleport.BlockedTo", oldConfig.getYamlE().getStringList("TP.BlockedTo", new LinkedList<>()));
				getConfigE().set("Home.Delayed", oldConfig.getYamlE().getBoolean("Home.Delayed", false));
				getConfigE().set("Home.Global", oldConfig.getYamlE().getBoolean("Home.Global", true));
				getConfigE().set("Home.BlockedFrom", oldConfig.getYamlE().getStringList("Home.BlockedFrom", new LinkedList<>()));
			}
			catch(Exception e)
			{
				plugin.getLogger().warning("There was a problem upgrading the old config file into the new config file.");
				e.printStackTrace();
			}
		}
		else
		{
			super.doUpgrade(oldConfig);
		}
	}

	@Override
	protected void doUpdate()
	{
		// We don't have to update our config by now :)
	}

	//region Getters
	//region Global settings
	public boolean isPolygamyAllowed()
	{
		return getConfigE().getBoolean("Marriage.AllowMultiplePartners", false);
	}

	public boolean isSelfMarriageAllowed()
	{
		return getConfigE().getBoolean("Marriage.AllowSelfMarriage", false);
	}

	public boolean isSurnamesEnabled()
	{
		return getConfigE().getBoolean("Marriage.Surnames.Enable", false);
	}

	public boolean isSurnamesForced()
	{
		return getConfigE().getBoolean("Marriage.Surnames.Forced", false);
	}
	//endregion

	//region Database getter
	@Override
	public boolean getUseOnlineUUIDs()
	{
		String type = getConfigE().getString("Database.UUID_Type", "auto").toLowerCase();
		if(type.equals("auto"))
		{
			return plugin.getProxy().getConfig().isOnlineMode();
		}
		return type.equals("online");
	}
	//endregion

	//region Chat getter
	public boolean isChatHandlerEnabled()
	{
		return getConfigE().getBoolean("Chat.Global", true);
	}
	//endregion

	//region TP getter
	public boolean isTPHandlerEnabled()
	{
		return getConfigE().getBoolean("Teleport.Global", true);
	}

	public boolean isTPDelayed()
	{
		return getConfigE().getBoolean("Teleport.Delayed", false);
	}

	public Set<String> getTPBlackListedServersFrom()
	{
		return new HashSet<>(toLowerCase(getConfigE().getStringList("Teleport.BlockedFrom", new LinkedList<>())));
	}

	public Set<String> getTPBlackListedServersTo()
	{
		return new HashSet<>(toLowerCase(getConfigE().getStringList("Teleport.BlockedTo", new LinkedList<>())));
	}
	//endregion

	//region Home getter
	public boolean isHomeHandlerEnabled()
	{
		return getConfigE().getBoolean("Home.Global", true);
	}

	public boolean isHomeDelayed()
	{
		return getConfigE().getBoolean("Home.Delayed", false);
	}

	public Set<String> getHomeBlackListedServersFrom()
	{
		return new HashSet<>(toLowerCase(getConfigE().getStringList("Home.BlockedFrom", new LinkedList<>())));
	}

	public Set<String> getHomeBlackListedServersTo()
	{
		return new HashSet<>(toLowerCase(getConfigE().getStringList("Home.BlockedTo", new LinkedList<>())));
	}
	//endregion

	//region Join Leave Info
	public boolean isJoinLeaveInfoEnabled()
	{
		return getConfigE().getBoolean("InfoOnPartnerJoinLeave.Enable", true);
	}

	public long getJoinInfoDelay()
	{
		return getConfigE().getLong("InfoOnPartnerJoinLeave.JoinDelay", 0);
	}
	//endregion

	//region Misc getter
	public boolean useUpdater()
	{
		return getConfigE().getBoolean("Misc.AutoUpdate", true);
	}
	//endregion
	//endregion

	private static List<String> toLowerCase(List<String> strings)
	{
		List<String> outList = new LinkedList<>();
		for(String str : strings)
		{
			outList.add(str.toLowerCase());
		}
		return outList;
	}
}
