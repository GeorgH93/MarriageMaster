/*
 *   Copyright (C) 2020 GeorgH93
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
import at.pcgamingfreaks.MarriageMaster.Bukkit.Database.Helper.OldFileUpdater;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Bukkit.SpecialInfoWorker.UpgradedInfo;
import at.pcgamingfreaks.MarriageMaster.Database.DatabaseConfiguration;
import at.pcgamingfreaks.MarriageMaster.MagicValues;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.util.*;

public class Config extends Configuration implements DatabaseConfiguration
{
	public Config(JavaPlugin plugin)
	{
		super(plugin, MagicValues.CONFIG_VERSION, MagicValues.CONFIG_VERSION);
		languageKey = "Language.Language";
		languageUpdateKey = "Language.UpdateMode";
	}

	@Override
	protected void doUpgrade(at.pcgamingfreaks.YamlFileManager oldConfig)
	{
		if(oldConfig.getVersion() < MagicValues.CONFIG_PRE_V2_VERSIONS)
		{
			OldFileUpdater.updateConfig(oldConfig.getYaml(), this.getConfigE());
			new UpgradedInfo(MarriageMaster.getInstance());
		}
		else
		{
			super.doUpgrade(oldConfig);
			if(oldConfig.getYamlE().isSet("Marriage.AllowPolygamy")) getConfigE().set("Marriage.AllowMultiplePartners", getConfigE().getBoolean("Marriage.AllowPolygamy", false));
		}
	}

	@Override
	protected void doUpdate()
	{
		// We don't have to update our config by now :)
	}

	//region Getter
	//region Global settings
	public boolean areMultiplePartnersAllowed()
	{
		return getConfigE().getBoolean("Marriage.AllowMultiplePartners", getConfigE().getBoolean("Marriage.AllowPolygamy", false));
	}

	public boolean isSelfMarriageAllowed()
	{
		return !getConfigE().getBoolean("Marriage.RequirePriest", true);
	}

	public boolean isSelfDivorceAllowed()
	{
		String val = getConfigE().getString("Marriage.DivorceRequiresPriest", "auto").trim().toLowerCase(Locale.ENGLISH);
		switch(val)
		{
			case "on":
			case "yes":
			case "true": return false;
			case "off":
			case "no":
			case "false": return true;
			default: return isSelfMarriageAllowed();
		}
	}

	public boolean isSurnamesEnabled()
	{
		return getConfigE().getBoolean("Marriage.Surnames.Enable", false);
	}

	public boolean isSurnamesForced()
	{
		return getConfigE().getBoolean("Marriage.Surnames.Force", false);
	}

	public double getRange(String option)
	{
		return getConfigE().getDouble("Range." + option, 25.0);
	}

	public double getRangeSquared(String option)
	{
		double range = getRange(option);
		return (range > 0) ? range * range : range;
	}

	public boolean isMarryAnnouncementEnabled()
	{
		return getConfigE().getBoolean("Marriage.AnnounceOnMarriage", true);
	}

	public boolean isSetPriestCommandEnabled()
	{
		return !getConfigE().getBoolean("Marriage.DisableSetPriestCommand", false);
	}
	//endregion

	//region Confirmation settings
	public boolean isMarryConfirmationEnabled()
	{
		return getConfigE().getBoolean("Marriage.Confirmation.Enable", true);
	}

	public boolean isMarryConfirmationAutoDialogEnabled()
	{
		return getConfigE().getBoolean("Marriage.Confirmation.AutoDialog", true);
	}

	public boolean isConfirmationBothPlayersOnDivorceEnabled()
	{
		return getConfigE().getBoolean("Marriage.Confirmation.BothPlayersOnDivorce", true);
	}

	public boolean isConfirmationOtherPlayerOnSelfDivorceEnabled()
	{
		return getConfigE().getBoolean("Marriage.Confirmation.OtherPlayerOnSelfDivorce", false);
	}
	//endregion

	//region Surname settings
	public boolean getSurnamesAllowColors()
	{
		return getConfigE().getBoolean("Marriage.Surnames.AllowColors", false);
	}

	public String getSurnamesAllowedCharacters()
	{
		return getConfigE().getString("Marriage.Surnames.AllowedCharacters", "A-Za-z");
	}

	public int getSurnamesMinLength()
	{
		return Math.max(getConfigE().getInt("Marriage.Surnames.MinLength", 3), 3);
	}

	public int getSurnamesMaxLength()
	{
		return Math.max(getConfigE().getInt("Marriage.Surnames.MaxLength", 16), getSurnamesMinLength());
	}
	//endregion

	//region List command settings
	public boolean useListFooter()
	{
		return getConfigE().getBoolean("List.UseFooter", true);
	}

	public int getListEntriesPerPage()
	{
		return getConfigE().getInt("List.EntriesPerPage", 8);
	}
	//endregion

	//region Home/TP command settings
	public int getTPDelayTime()
	{
		return (getConfigE().getBoolean("Teleport.Delay", false) ? getConfigE().getInt("Teleport.DelayTime", 3) : 0);
	}

	public Set<String> getTPBlackListedWorlds()
	{
		Set<String> blackListedWorlds = new HashSet<>();
		for(String world : getConfigE().getStringList("Teleport.BlacklistedWorlds", new LinkedList<>()))
		{
			blackListedWorlds.add(world.toLowerCase(Locale.ENGLISH));
		}
		return blackListedWorlds;
	}

	public boolean getSafetyCheck()
	{
		return getConfigE().getBoolean("Teleport.CheckSafety", true);
	}

	public boolean getRequireConfirmation()
	{
		return getConfigE().getBoolean("Teleport.RequireConfirmation", true);
	}
	//endregion

	//region Kiss command settings
	public boolean isKissEnabled()
	{
		return getConfigE().getBoolean("Kiss.Enable", true);
	}

	public int getKissWaitTime()
	{
		return getConfigE().getInt("Kiss.WaitTime", 10) * 1000;
	}

	public int getKissHearthCount()
	{
		return getConfigE().getInt("Kiss.HearthCount", 50);
	}
	//endregion

	//region Gift command settings
	public boolean isGiftEnabled()
	{
		return getConfigE().getBoolean("Gift.Enable", true);
	}

	public boolean isGiftAllowedInCreative()
	{
		return getConfigE().getBoolean("Gift.AllowInCreative", false);
	}

	public boolean isGiftRequireConfirmationEnabled()
	{
		return getConfigE().getBoolean("Gift.RequireConfirmation", false);
	}

	public Set<String> getGiftBlackListedWorlds()
	{
		Set<String> blackListedWorlds = new HashSet<>();
		for(String world : getConfigE().getStringList("Gift.BlacklistedWorlds", new LinkedList<>()))
		{
			blackListedWorlds.add(world.toLowerCase());
		}
		return blackListedWorlds;
	}
	//endregion

	// PvP command settings
	public boolean getPvPAllowBlocking()
	{
		return getConfigE().getBoolean("PvP.AllowBlocking", true);
	}

	//region BonusXP
	public boolean isBonusXPEnabled()
	{
		return getConfigE().getBoolean("BonusXp.Enable", false);
	}

	public double getBonusXpMultiplier()
	{
		return getConfigE().getDouble("BonusXp.Multiplier", 2);
	}

	public boolean isBonusXPSplitOnPickupEnabled()
	{
		return getConfigE().getBoolean("BonusXp.SplitXpOnPickup", true);
	}

	public boolean isSkillApiBonusXPEnabled()
	{
		return getConfigE().getBoolean("BonusXp.SkillAPI.Enable", false);
	}

	public List<String> getSkillApiBonusXpBlockedSources()
	{
		return getConfigE().getStringList("BonusXp.SkillAPI.ExcludeSources", new LinkedList<>());
	}

	public double getSkillApiBonusXpMultiplier()
	{
		return getConfigE().getDouble("BonusXp.SkillAPI.Multiplier", 2);
	}

	public boolean isSkillApiBonusXPSplitEnabled()
	{
		return getConfigE().getBoolean("BonusXp.SkillAPI.SplitXp", true);
	}

	public boolean isMcMMOBonusXPEnabled()
	{
		return getConfigE().getBoolean("BonusXp.McMMO.Enable", false);
	}

	public Set<String> getMcMMOBonusXpBlockedSources()
	{
		Set<String> blockedSources = new HashSet<>();
		getConfigE().getStringList("BonusXp.McMMO.ExcludeSources", new LinkedList<>()).forEach(source -> blockedSources.add(source.toUpperCase()));
		return blockedSources;
	}

	public Set<String> getMcMMOBonusXpBlockedSkills()
	{
		Set<String> blockedSkills = new HashSet<>();
		getConfigE().getStringList("BonusXp.McMMO.ExcludeSkills", new LinkedList<>()).forEach(source -> blockedSkills.add(source.toUpperCase()));
		return blockedSkills;
	}

	public float getMcMMOBonusXpMultiplier()
	{
		return getConfigE().getFloat("BonusXp.McMMO.Multiplier", 2);
	}

	public boolean isMcMMOBonusXPSplitEnabled()
	{
		return getConfigE().getBoolean("BonusXp.McMMO.SplitXp", true);
	}
	//endregion

	//region HP Regain
	public boolean isHPRegainEnabled()
	{
		return getConfigE().getBoolean("HealthRegain.Enable", false);
	}

	public double getHPRegainMultiplier()
	{
		return getConfigE().getDouble("HealthRegain.Multiplier", 2);
	}
	//endregion

	//region Database getter
	@Override
	public @NotNull String getDatabaseType()
	{
		return getConfigE().getString("Database.Type", "SQLite").toLowerCase(Locale.ENGLISH);
	}

	@Override
	public void setDatabaseType(final @NotNull String newType)
	{
		set("Database.Type", newType);
		trySave();
	}

	@Override
	public boolean useOnlineUUIDs()
	{
		String type = getConfigE().getString("Database.UUID_Type", "auto").toLowerCase(Locale.ENGLISH);
		if(type.equals("auto"))
		{
			return plugin.getServer().getOnlineMode();
		}
		return type.equals("online");
	}

	public void setUseUUIDSeparators(boolean useUUIDSeparators)
	{
		set("Database.UseUUIDSeparators", useUUIDSeparators);
		trySave();
	}

	public void setUUIDType(String type)
	{
		set("Database.UUID_Type", type);
		trySave();
	}
	//endregion

	//region Join Leave Info
	public boolean isJoinLeaveInfoEnabled()
	{
		return getConfigE().getBoolean("InfoOnPartnerJoinLeave.Enable", true);
	}

	public long getJoinInfoDelay()
	{
		return getConfigE().getLong("InfoOnPartnerJoinLeave.JoinDelay", 0) * 20L;
	}
	//endregion

	//region Chat settings
	public boolean isChatEnabled()
	{
		return getConfigE().getBoolean("Chat.Enabled", true);
	}

	public boolean isChatSurveillanceEnabled()
	{
		return getConfigE().getBoolean("Chat.AllowSurveillance", false);
	}
	//endregion

	//region Prefix/Suffix
	public boolean isPrefixEnabled()
	{
		return getConfigE().getBoolean("Prefix.Enable", false) && !getPrefix().isEmpty();
	}

	public boolean isSuffixEnabled()
	{
		return getConfigE().getBoolean("Suffix.Enable", false) && !getSuffix().isEmpty();
	}

	public @NotNull String getPrefix()
	{
		return ChatColor.translateAlternateColorCodes('&', getConfigE().getString("Prefix.String", "").replace("<heart>", ChatColor.RED + MagicValues.SYMBOL_HEART + ChatColor.WHITE));
	}

	public boolean isPrefixOnLineBeginning()
	{
		return getConfigE().getBoolean("Prefix.OnLineBeginning", true);
	}

	public @NotNull String getSuffix()
	{
		return ChatColor.translateAlternateColorCodes('&', getConfigE().getString("Suffix.String", "").replace("<heart>", ChatColor.RED + MagicValues.SYMBOL_HEART + ChatColor.WHITE));
	}
	//endregion

	//region Backpack share
	public boolean isBackpackShareEnabled()
	{
		return getConfigE().getBoolean("BackpackShare.Enable", true);
	}
	//endregion

	//region Economy
	public boolean isEconomyEnabled()
	{
		return getConfigE().getBoolean("Economy.Enable", false);
	}

	public double getEconomyValue(String valueName)
	{
		return getConfigE().getDouble("Economy." + valueName, 0);
	}
	//endregion

	//region Command Executor
	public boolean isCommandExecutorEnabled()
	{
		return getConfigE().getBoolean("CommandExecutor.Enable", false);
	}

	public Collection<String> getCommandExecutorOnMarry()
	{
		return getConfigE().getStringList("CommandExecutor.OnMarry", new LinkedList<>());
	}

	public Collection<String> getCommandExecutorOnMarryWithPriest()
	{
		return getConfigE().getStringList("CommandExecutor.OnMarryWithPriest", new LinkedList<>());
	}

	public Collection<String> getCommandExecutorOnDivorce()
	{
		return getConfigE().getStringList("CommandExecutor.OnDivorce", new LinkedList<>());
	}

	public Collection<String> getCommandExecutorOnDivorceWithPriest()
	{
		return getConfigE().getStringList("CommandExecutor.OnDivorceWithPriest", new LinkedList<>());
	}
	//endregion

	//region Misc getter
	public boolean useUpdater()
	{
		return getConfigE().getBoolean("Misc.AutoUpdate", true);
	}

	public boolean isBungeeEnabled()
	{
		return getConfigE().getBoolean("Misc.UseBungeeCord", false);
	}

	public String getServerName()
	{
		return getConfigE().getString("Misc.ServerName", null);
	}

	public void setServerName(String serverName)
	{
		try
		{
			getConfigE().set("Misc.ServerName", serverName);
			save();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	//endregion
	//endregion

	private void trySave()
	{
		try
		{
			save();
		}
		catch(FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}
}