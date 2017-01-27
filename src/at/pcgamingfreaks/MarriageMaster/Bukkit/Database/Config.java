/*
 *   Copyright (C) 2016 GeorgH93
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

import at.pcgamingfreaks.Bukkit.Configuration;
import at.pcgamingfreaks.LanguageUpdateMethod;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Database.Helper.OldFileUpdater;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Bukkit.SpecialInfoWorker.UpgradedInfo;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class Config extends Configuration
{
	private static final int CONFIG_VERSION = 90, UPGRADE_THRESHOLD = 90, PRE_V2_VERSIONS = 90;
	
	public Config(JavaPlugin plugin)
	{
		super(plugin, CONFIG_VERSION, UPGRADE_THRESHOLD);
		languageKey = "Language.Language";
		languageUpdateKey = "Language.UpdateMode";
	}

	@Override
	protected void doUpgrade(at.pcgamingfreaks.Configuration oldConfig)
	{
		if(oldConfig.getVersion() < PRE_V2_VERSIONS)
		{
			OldFileUpdater.updateConfig(oldConfig.getConfig(), this.getConfig());
			new UpgradedInfo(MarriageMaster.getInstance());
		}
		else
		{
			for(String key : config.getKeys(true))
			{
				if(oldConfig.getConfig().isSet(key))
				{
					if(key.equals("Version"))
						continue;
					config.set(key, oldConfig.getConfig().getString(key, null));
				}
			}
		}
	}

	@Override
	protected void doUpdate()
	{
		// We don't have to update our config by now :)
	}

	//region Getter
	//region Global settings
	public boolean isPolygamyAllowed()
	{
		return config.getBoolean("Marriage.AllowPolygamy", false);
	}

	public boolean isSelfMarriageAllowed()
	{
		return config.getBoolean("Marriage.AllowSelfMarriage", false);
	}

	public boolean isSelfDivorceAllowed()
	{
		String val = config.getString("Marriage.AllowSelfDivorce", "auto").trim().toLowerCase();
		switch(val)
		{
			case "on":
			case "yes":
			case "true": return true;
			case "off":
			case "no":
			case "false": return false;
			default: return isSelfMarriageAllowed();
		}
	}

	public boolean isSurnamesEnabled()
	{
		return config.getBoolean("Marriage.Surnames.Enable", false);
	}

	public boolean isSurnamesForced()
	{
		return config.getBoolean("Marriage.Surnames.Forced", false);
	}

	public double getRange(String option)
	{
		return config.getDouble("Range." + option, 25.0);
	}

	public boolean isMarryAnnouncementEnabled()
	{
		return config.getBoolean("Marriage.AnnounceOnMarriage", true);
	}

	public boolean isSetPriestCommandEnabled()
	{
		return !config.getBoolean("Marriage.DisableSetPriestCommand", false);
	}
	//endregion

	//region Confirmation settings
	public boolean isMarryConfirmationEnabled()
	{
		return config.getBoolean("Marriage.Confirmation.Enable", true);
	}

	public boolean isMarryConfirmationAutoDialogEnabled()
	{
		return config.getBoolean("Marriage.Confirmation.AutoDialog", true);
	}

	public boolean isConfirmationBothPlayersOnDivorceEnabled()
	{
		return config.getBoolean("Marriage.Confirmation.BothPlayersOnDivorce", true);
	}

	public boolean isConfirmationOtherPlayerOnSelfDivorceEnabled()
	{
		return config.getBoolean("Marriage.Confirmation.OtherPlayerOnSelfDivorce", false);
	}
	//endregion

	//region Surname settings
	public boolean getSurnamesAllowColors()
	{
		return config.getBoolean("Marriage.Surnames.AllowColors", false);
	}

	public String getSurnamesAllowedCharacters()
	{
		return config.getString("Marriage.Surnames.AllowedCharacters", "A-Za-z");
	}

	public int getSurnamesMinLength()
	{
		return Math.max(config.getInt("Marriage.Surnames.MinLength", 3), 3);
	}

	public int getSurnamesMaxLength()
	{
		return Math.max(config.getInt("Marriage.Surnames.MaxLength", 16), getSurnamesMinLength());
	}
	//endregion

	//region List command settings
	public boolean useListFooter()
	{
		return config.getBoolean("List.UseFooter", true);
	}

	public int getListEntriesPerPage()
	{
		return config.getInt("List.EntriesPerPage", 8);
	}
	//endregion

	//region Home/TP command settings
	public int getTPDelayTime()
	{
		return (config.getBoolean("Teleport.Delay", false) ? config.getInt("Teleport.DelayTime", 3) : 0);
	}

	public Set<String> getTPBlackListedWorlds()
	{
		Set<String> blackListedWorlds = new HashSet<>();
		for(String world : config.getStringList("Teleport.BlacklistedWorlds", new LinkedList<String>()))
		{
			blackListedWorlds.add(world.toLowerCase());
		}
		return blackListedWorlds;
	}

	public boolean getSafetyCheck()
	{
		return config.getBoolean("Teleport.CheckSafety", true);
	}
	//endregion

	//region Kiss command settings
	public boolean isKissEnabled()
	{
		return config.getBoolean("Kiss.Enable", true);
	}

	public int getKissWaitTime()
	{
		return config.getInt("Kiss.WaitTime", 10) * 1000;
	}

	public int getKissHearthCount()
	{
		return config.getInt("Kiss.HearthCount", 50);
	}
	//endregion

	//region Gift command settings
	public boolean isGiftEnabled()
	{
		return config.getBoolean("Gift.Enable", true);
	}

	public boolean isGiftAllowedInCreative()
	{
		return config.getBoolean("Gift.AllowInCreative", false);
	}
	//endregion

	// PvP command settings
	public boolean getPvPAllowBlocking()
	{
		return config.getBoolean("PvP.AllowBlocking", true);
	}

	//region BonusXP
	public boolean isBonusXPEnabled()
	{
		return config.getBoolean("BonusXp.Enable", false);
	}

	public double getBonusXpMultiplier()
	{
		return config.getDouble("BonusXp.Multiplier", 2);
	}

	public boolean isBonusXPSplitOnPickupEnabled()
	{
		return config.getBoolean("BonusXp.SplitXpOnPickup", true);
	}
	//endregion

	//region HP Regain
	public boolean isHPRegainEnabled()
	{
		return config.getBoolean("HealthRegain.Enable", false);
	}

	public double getHPRegainMultiplier()
	{
		return config.getDouble("HealthRegain.Multiplier", 2);
	}
	//endregion

	//region Database getter
	public String getDatabaseType()
	{
		return config.getString("Database.Type", "SQLite").toLowerCase();
	}

	public void setDatabaseType(String newType)
	{
		config.set("Database.Type", newType);
		try
		{
			saveConfig();
		}
		catch(FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}

	public boolean useUUIDs()
	{
		return config.getBoolean("Database.UseUUIDs", true);
	}

	public boolean useUUIDSeparators()
	{
		return config.getBoolean("Database.UseUUIDSeparators", false);
	}

	public boolean getUseOnlineUUIDs()
	{
		String type = config.getString("Database.UUID_Type", "auto").toLowerCase();
		if(type.equals("auto"))
		{
			return plugin.getServer().getOnlineMode();
		}
		return type.equals("online");
	}

	public String getSQLHost()
	{
		return config.getString("Database.SQL.Host", "localhost:3306");
	}

	public String getSQLDatabase()
	{
		return config.getString("Database.SQL.Database", "minecraft");
	}

	public String getSQLUser()
	{
		return config.getString("Database.SQL.User", "minecraft");
	}

	public String getSQLPassword()
	{
		return config.getString("Database.SQL.Password", "minecraft");
	}

	public int getSQLMaxConnections()
	{
		return Math.max(1, config.getInt("Database.SQL.MaxConnections", 2));
	}

	public String getSQLTableUser()
	{
		return config.getString("Database.SQL.Tables.User", "marry_players");
	}

	public String getSQLTableHomes()
	{
		return config.getString("Database.SQL.Tables.Home", "marry_home");
	}

	public String getSQLTablePriests()
	{
		return config.getString("Database.SQL.Tables.Priests", "marry_priests");
	}

	public String getSQLTableMarriages()
	{
		return config.getString("Database.SQL.Tables.Partner", "marry_partners");
	}

	public String getSQLField(String field, String defaultValue)
	{
		return config.getString("Database.SQL.Tables.Fields." + field, defaultValue);
	}

	public String getUnCacheStrategie()
	{
		return config.getString("Database.Cache.UnCache.Strategie", "interval").toLowerCase();
	}

	public long getUnCacheInterval()
	{
		return config.getLong("Database.Cache.UnCache.Interval", 600) * 20L;
	}

	public long getUnCacheDelay()
	{
		return config.getLong("Database.Cache.UnCache.Delay", 600) * 20L;
	}
	//endregion

	//region Join Leave Info
	public boolean isJoinLeaveInfoEnabled()
	{
		return config.getBoolean("InfoOnPartnerJoinLeave.Enable", true);
	}

	public long getJoinInfoDelay()
	{
		return config.getLong("InfoOnPartnerJoinLeave.JoinDelay", 0) * 20L;
	}
	//endregion

	//region Prefix/Suffix
	public boolean isPrefixEnabled()
	{
		return config.getBoolean("Prefix.Enable", true);
	}

	public boolean isSuffixEnabled()
	{
		return config.getBoolean("Suffix.Enable", true);
	}

	public String getPrefix()
	{
		return ChatColor.translateAlternateColorCodes('&', config.getString("Prefix.String", "<heart>{PartnerName}<heart>").replace("<heart>", ChatColor.RED + "\u2764" + ChatColor.WHITE));
	}

	public boolean isPrefixOnLineBeginning()
	{
		return config.getBoolean("Prefix.OnLineBeginning", true);
	}

	public String getSuffix()
	{
		return ChatColor.translateAlternateColorCodes('&', config.getString("Suffix.String", "<heart>{PartnerName}<heart>").replace("<heart>", ChatColor.RED + "\u2764" + ChatColor.WHITE));
	}
	//endregion

	//region Backpack share
	public boolean isBackpackShareEnabled()
	{
		return config.getBoolean("BackpackShare.Enable", true);
	}
	//endregion

	//region Economy
	public boolean isEconomyEnabled()
	{
		return config.getBoolean("Economy.Enable", false);
	}

	public double getEconomyValue(String valueName)
	{
		return config.getDouble("Economy." + valueName, 0);
	}
	//endregion

	//region Misc getter
	public boolean useMetrics()
	{
		return config.getBoolean("Misc.Metrics", true);
	}

	public boolean useUpdater()
	{
		return config.getBoolean("Misc.AutoUpdate", true);
	}

	public boolean isBungeeEnabled()
	{
		return config.getBoolean("Misc.UseBungeeCord", false);
	}
	//endregion
	//endregion
}