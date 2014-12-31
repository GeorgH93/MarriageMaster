/*
 *   Copyright (C) 2014 GeorgH93
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

package at.pcgamingfreaks.georgh.MarriageMaster.Databases;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import at.pcgamingfreaks.georgh.MarriageMaster.MarriageMaster;

public class Config
{
	private MarriageMaster marriageMaster;
	private FileConfiguration config;
	private static final int CONFIG_VERSION = 13;
	
	private boolean UsePerms = false;
	
	public Config(MarriageMaster marriagemaster)
	{
		marriageMaster = marriagemaster;
		LoadConfig();
		
		UsePerms = config.getBoolean("Permissions");
	}
	
	public void Reload()
	{
		LoadConfig();
		
		UsePerms = config.getBoolean("Permissions");
	}
	
	private void LoadConfig()
	{
		File file = new File(marriageMaster.getDataFolder(), "config.yml");
		if(!file.exists())
		{
			NewConfig(file);
		}
		else
		{
			config = YamlConfiguration.loadConfiguration(file);
			UpdateConfig(file);
		}
	}
	
	private boolean UUIDComp()
	{
		try
		{
			String[] GameVersion = Bukkit.getBukkitVersion().split("-");
			GameVersion = GameVersion[0].split("\\.");
			if(Integer.parseInt(GameVersion[1]) > 7 || (Integer.parseInt(GameVersion[1]) == 7 && Integer.parseInt(GameVersion[2]) > 5))
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		catch(Exception e)
		{
			return false;
		}
	}
	
	private void NewConfig(File file)
	{
		config = new YamlConfiguration();
		config.set("Permissions", true);
		config.set("VaultPermissions", true);
		config.set("AllowBlockPvP", false);
		config.set("Announcement", true);
		config.set("InformOnPartnerJoin", true);
		config.set("Language","en");
		config.set("LanguageUpdateMode","Overwrite");
		config.set("PriestCMD", "priest");
		config.set("UseAltChatToggleCommand", false);
		config.set("ChatToggleCommand", "chattoggle");
		config.set("UseUUIDs", Bukkit.getServer().getOnlineMode() && UUIDComp());
		config.set("AllowSelfMarry", false);
		config.set("Surname", false);
		config.set("UseMinepacks", false);
		config.set("Economy.Enable", false);
		config.set("Economy.Divorce", 100.00);
		config.set("Economy.Marry", 100.00);
		config.set("Economy.Tp", 25.00);
		config.set("Economy.HomeTp", 25.00);
		config.set("Economy.SetHome", 100.00);
		config.set("Economy.Gift", 10.00);
		config.set("HealthRegain.Enable", true);
		config.set("HealthRegain.Amount", 2);
		config.set("BonusXp.Enable", true);
		config.set("BonusXp.Multiplier", 2);
		config.set("Prefix.Enable", true);
		config.set("Prefix.String", "<heart><partnername><heart>");
		config.set("Database.Type","Files");
		config.set("Database.UpdatePlayer", true);
		config.set("Database.MySQL.Host", "localhost:3306");
		config.set("Database.MySQL.Database", "minecraft");
		config.set("Database.MySQL.User", "minecraft");
		config.set("Database.MySQL.Password", "minecraft");
		config.set("Database.Tables.User", "marry_players");
		config.set("Database.Tables.Home", "marry_home");
		config.set("Database.Tables.Priests", "marry_priests");
		config.set("Database.Tables.Partner", "marry_partners");
		config.set("Confirmation.Enable", true);
		config.set("Confirmation.AutoDialog", true);
		config.set("Kiss.Enable", true);
		config.set("Kiss.WaitTime", 10);
		config.set("Kiss.HearthCount", 50);
		config.set("Kiss.CompatibilityMode", false);
		config.set("Misc.Metrics", true);
		config.set("Misc.AutoUpdate", true);
		config.set("Range.Marry", 25.0F);
		config.set("Range.Kiss", 2.0F);
		config.set("Range.KissInteract", 25.0F);
		config.set("Range.HearthVisible", 128.0F);
		config.set("Range.Heal", 2.0F);
		config.set("Range.BonusXP", 10.0F);
		config.set("Range.Gift", 0);
		config.set("Range.Backpack", 5);
		config.set("Teleport.Delay", false);
		config.set("Teleport.DelayTime", 3);
		config.set("TPBlacklistedWorlds", new ArrayList<String>());
		config.set("Version",CONFIG_VERSION);
		
		try 
		{
			config.save(file);
			marriageMaster.log.info("Config File has been generated.");
		}
  	  	catch (IOException e) 
  	  	{
  	  		e.printStackTrace();
  	  	}
	}
	
	private boolean UpdateConfig(File file)
	{
		switch(config.getInt("Version"))
		{
			case 1:
				config.set("Database.MySQL.Database", "minecraft");
				config.set("LanguageUpdateMode","Overwrite");
			case 2:
				config.set("Prefix.Enable", true);
				config.set("Prefix.String", "<heart><partnername><heart>");
			case 3:
				config.set("Confirmation.Enable", true);
				config.set("Confirmation.AutoDialog", true);
				config.set("PriestCMD", "priest");
			case 4:
				config.set("Kiss.Enable", true);
				config.set("Kiss.WaitTime", 10);
				config.set("Kiss.HearthCount", 50);
				config.set("Misc.Metrics", true);
				config.set("Misc.AutoUpdate", true);
				config.set("Economy.Gift", 10.00);
			case 5:
				config.set("Range.Marry", 25.0F);
				config.set("Range.Kiss", 2.0F);
				config.set("Range.KissInteract", 25.0F);
				config.set("Range.HearthVisible", 128.0F);
				config.set("Range.Heal", 2.0F);
				config.set("Range.BonusXP", 10.0F);
				config.set("TPBlacklistedWorlds", new ArrayList<String>());
			case 6:
				config.set("UseUUIDs", Bukkit.getServer().getOnlineMode() && UUIDComp());
			case 7:
				config.set("AllowSelfMarry", false);
			case 8:
				config.set("Teleport.Delay", false);
				config.set("Teleport.DelayTime", 3);
			case 9:
				config.set("UseAltChatToggleCommand", false);
				config.set("ChatToggleCommand", "chattoggle");
			case 10:
				config.set("Surname", false);
				config.set("Range.Gift", 0);
			case 11:
				config.set("UseMinepacks", false);
				config.set("Range.Backpack", 5);
				config.set("Database.UpdatePlayer", true);
				config.set("Database.Tables.User", "marry_players");
				config.set("Database.Tables.Home", "marry_home");
				config.set("Database.Tables.Priests", "marry_priests");
				config.set("Database.Tables.Partner", "marry_partners");
			case 12:
				config.set("Kiss.CompatibilityMode", false);
				config.set("VaultPermissions", true);
			break;
			case CONFIG_VERSION: return false;
			default: marriageMaster.log.info("Config File Version newer than expected!"); return false;
		}
		config.set("Version", CONFIG_VERSION);
		try 
		{
			config.save(file);
			marriageMaster.log.info("Config File has been updated.");
		}
  	  	catch (IOException e) 
  	  	{
  	  		e.printStackTrace();
  	  		return false;
  	  	}
		return true;
	}
	
	public String GetLanguage()
	{
		return config.getString("Language");
	}
	
	public String GetLanguageUpdateMode()
	{
		return config.getString("LanguageUpdateMode");
	}
	
	public boolean GetAnnouncementEnabled()
	{
		return config.getBoolean("Announcement");
	}
	
	public boolean GetBonusXPEnabled()
	{
		return config.getBoolean("BonusXp.Enable");
	}
	
	public boolean GetInformOnPartnerJoinEnabled()
	{
		return config.getBoolean("InformOnPartnerJoin");
	}
	
	public int GetBonusXPAmount()
	{
		return config.getInt("BonusXp.Multiplier");
	}
	
	public boolean GetHealthRegainEnabled()
	{
		return config.getBoolean("HealthRegain.Enable");
	}
	
	public int GetHealthRegainAmount()
	{
		return config.getInt("HealthRegain.Amount");
	}
	
	public boolean GetAllowBlockPvP()
	{
		return config.getBoolean("AllowBlockPvP");
	}
	
	public boolean getUseVaultPermissions()
	{
		return UsePerms && config.getBoolean("VaultPermissions", true);
	}
	
	public boolean UsePrefix()
	{
		return config.getBoolean("Prefix.Enable");	
	}
	
	public String GetPrefix()
	{
		return config.getString("Prefix.String");
	}
	
	public boolean UseEconomy()
	{
		return config.getBoolean("Economy.Enable");
	}
	
	public double GetEconomyDivorce()
	{
		return config.getDouble("Economy.Divorce");
	}
	
	public double GetEconomyMarry()
	{
		return config.getDouble("Economy.Marry");
	}
	
	public double GetEconomyTp()
	{
		return config.getDouble("Economy.Tp");
	}
	
	public double GetEconomyHomeTp()
	{
		return config.getDouble("Economy.HomeTp");
	}
	
	public double GetEconomySetHome()
	{
		return config.getDouble("Economy.SetHome");
	}
	
	public double GetEconomyGift()
	{
		return config.getDouble("Economy.Gift");
	}
	
	public String GetDatabaseType()
	{
		return config.getString("Database.Type");
	}
	
	public boolean CheckPerm(Player player, String Perm)
	{
		return CheckPerm(player,Perm, true);
	}
	
	public boolean CheckPerm(Player player,String Perm, boolean def)
	{
		if(player.isOp())
		{
			return true;
		}
		if(marriageMaster.perms != null)
		{
			return marriageMaster.perms.has(player, Perm);
		}
		else if(UsePerms)
		{
			return player.hasPermission(Perm);
		}
		return def;
	}
	
	public String GetMySQLHost()
	{
		return config.getString("Database.MySQL.Host");
	}
	
	public String GetMySQLDatabase()
	{
		return config.getString("Database.MySQL.Database");
	}
	
	public String GetMySQLUser()
	{
		return config.getString("Database.MySQL.User");
	}
	
	public String GetMySQLPassword()
	{
		return config.getString("Database.MySQL.Password");
	}
	
	public String getUserTable()
	{
		return config.getString("Database.Tables.User", "marry_players");
	}
	
	public String getHomesTable()
	{
		return config.getString("Database.Tables.Home", "marry_home");
	}
	
	public String getPriestsTable()
	{
		return config.getString("Database.Tables.Priests", "marry_priests");
	}
	
	public String getPartnersTable()
	{
		return config.getString("Database.Tables.Partner", "marry_partners");
	}
	
	public boolean getUpdatePlayer()
	{
		return config.getBoolean("Database.UpdatePlayer", true);
	}
	
	public String GetPriestCMD()
	{
		return config.getString("PriestCMD");
	}
	
	public boolean UseConfirmation()
	{
		return config.getBoolean("Confirmation.Enable");
	}
	
	public boolean UseConfirmationAutoDialog()
	{
		return config.getBoolean("Confirmation.AutoDialog");
	}
	
	public boolean GetKissEnabled()
	{
		return config.getBoolean("Kiss.Enable");
	}
	
	public int GetKissWaitTime()
	{
		return config.getInt("Kiss.WaitTime") * 1000;
	}
	
	public boolean GetKissCompMode()
	{
		return config.getBoolean("Kiss.CompatibilityMode", false);
	}
	
	public int GetKissHearthCount()
	{
		return config.getInt("Kiss.HearthCount");
	}
	
	public boolean UseMetrics()
	{
		return config.getBoolean("Misc.Metrics");
	}
	
	public boolean UseUpdater()
	{
		return config.getBoolean("Misc.AutoUpdate");
	}
	
	public List<String> GetBlacklistedWorlds()
	{
		return config.getStringList("TPBlacklistedWorlds");
	}
	
	public double GetRange(String option)
	{
		return config.getDouble("Range." + option);
	}
	
	public boolean getUseUUIDs()
	{
		return config.getBoolean("UseUUIDs");
	}
	
	public boolean UseAltChatToggleCommand()
	{
		return config.getBoolean("UseAltChatToggleCommand");
	}
	
	public String ChatToggleCommand()
	{
		return config.getString("ChatToggleCommand").replace(' ', '_');
	}
	
	public boolean AllowSelfMarry()
	{
		return config.getBoolean("AllowSelfMarry");
	}
	
	public boolean DelayTP()
	{
		return config.getBoolean("Teleport.Delay");
	}
	
	public int TPDelayTime()
	{
		return config.getInt("Teleport.DelayTime");
	}
	
	public boolean getSurname()
	{
		return config.getBoolean("Surname", false);
	}
	
	public boolean getUseMinepacks()
	{
		return config.getBoolean("UseMinepacks", false);
	}
}