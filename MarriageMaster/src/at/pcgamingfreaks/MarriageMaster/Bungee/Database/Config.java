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

package at.pcgamingfreaks.MarriageMaster.Bungee.Database;

import at.pcgamingfreaks.Config.Configuration;
import at.pcgamingfreaks.Config.ILanguageConfiguration;
import at.pcgamingfreaks.Config.YamlFileManager;
import at.pcgamingfreaks.ConsoleColor;
import at.pcgamingfreaks.MarriageMaster.Bungee.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Bungee.SpecialInfoWorker.UpgradedInfo;
import at.pcgamingfreaks.MarriageMaster.Database.DatabaseConfiguration;
import at.pcgamingfreaks.MarriageMaster.MagicValues;
import at.pcgamingfreaks.Version;

import net.md_5.bungee.api.ProxyServer;

import java.util.*;

public class Config extends Configuration implements DatabaseConfiguration, ILanguageConfiguration
{
	public Config(MarriageMaster plugin)
	{
		super(plugin, new Version(MagicValues.BUNGEE_CONFIG_VERSION));
	}

	@Override
	protected void doUpgrade(YamlFileManager oldConfig)
	{
		if(oldConfig.version().olderThan(new Version(MagicValues.CONFIG_PRE_V2_VERSIONS)))
		{
			getLogger().warning(ConsoleColor.RED + "Your config file is from v1.x and is not compatible with versions newer than 2.5!" + ConsoleColor.RESET);
			new UpgradedInfo(MarriageMaster.getInstance());
		}
		else
		{
			Map<String, String> reMappings = new HashMap<>();
			if(oldConfig.version().olderThan("101")) reMappings.put("Misc.AutoUpdate.Enable", "Misc.AutoUpdate");
			if(oldConfig.version().olderThan("102")) reMappings.put("Database.Cache.UnCache.Strategy", "Database.Cache.UnCache.Strategie");
			super.doUpgrade(oldConfig, reMappings, oldConfig.getYamlE().getKeysFiltered("Database\\.SQL\\.(Tables\\.Fields\\..+|MaxLifetime|IdleTimeout)"));
		}
	}

	//region Getters
	//region Global settings
	public boolean areMultiplePartnersAllowed()
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
	public boolean useOnlineUUIDs()
	{
		String type = getConfigE().getString("Database.UUID_Type", "auto").toLowerCase(Locale.ENGLISH);
		if(type.equals("auto"))
		{
			return ProxyServer.getInstance().getConfig().isOnlineMode();
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
		return new HashSet<>(toLowerCase(getConfigE().getStringList("Teleport.BlockedFrom", new ArrayList<>(0))));
	}

	public Set<String> getTPBlackListedServersTo()
	{
		return new HashSet<>(toLowerCase(getConfigE().getStringList("Teleport.BlockedTo", new ArrayList<>(0))));
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
		return new HashSet<>(toLowerCase(getConfigE().getStringList("Home.BlockedFrom", new ArrayList<>(0))));
	}

	public Set<String> getHomeBlackListedServersTo()
	{
		return new HashSet<>(toLowerCase(getConfigE().getStringList("Home.BlockedTo", new ArrayList<>(0))));
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
		return getConfigE().getBoolean("Misc.AutoUpdate.Enable", getConfigE().getBoolean("Misc.AutoUpdate", true));
	}

	public String getUpdateChannel()
	{
		String channel = getConfigE().getString("Misc.AutoUpdate.Channel", "Release");
		if("Release".equals(channel) || "Master".equals(channel) || "Dev".equals(channel))
		{
			return channel;
		}
		else logger.info("Unknown update Channel: " + channel);
		return null;
	}
	//endregion
	//endregion

	private static List<String> toLowerCase(List<String> strings)
	{
		List<String> outList = new ArrayList<>(strings.size());
		for(String str : strings)
		{
			outList.add(str.toLowerCase(Locale.ENGLISH));
		}
		return outList;
	}
}
