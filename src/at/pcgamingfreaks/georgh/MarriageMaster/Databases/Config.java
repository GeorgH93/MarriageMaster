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
	private static final int CONFIG_VERSION = 8;
	
	public Config(MarriageMaster marriagemaster)
	{
		marriageMaster = marriagemaster;
		LoadConfig();
	}
	
	public void Reload()
	{
		LoadConfig();
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
			config = YamlConfiguration.loadConfiguration(new File(marriageMaster.getDataFolder(), "config.yml"));
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
		config.set("AllowBlockPvP", false);
		config.set("Announcement", true);
		config.set("InformOnPartnerJoin", true);
		config.set("Language","en");
		config.set("LanguageUpdateMode","Overwrite");
		config.set("PriestCMD", "priest");
		config.set("UseUUIDs", Bukkit.getServer().getOnlineMode() && UUIDComp());
		config.set("AllowSelfMarry", false);
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
		config.set("Database.MySQL.Host", "localhost:3306");
		config.set("Database.MySQL.Database", "minecraft");
		config.set("Database.MySQL.User", "minecraft");
		config.set("Database.MySQL.Password", "minecraft");
		config.set("Confirmation.Enable", true);
		config.set("Confirmation.AutoDialog", true);
		config.set("Kiss.Enable", true);
		config.set("Kiss.WaitTime", 10);
		config.set("Kiss.HearthCount", 50);
		config.set("Misc.Metrics", true);
		config.set("Misc.AutoUpdate", true);
		config.set("Range.Marry", 25.0F);
		config.set("Range.Kiss", 2.0F);
		config.set("Range.KissInteract", 25.0F);
		config.set("Range.HearthVisible", 128.0F);
		config.set("Range.Heal", 2.0F);
		config.set("Range.BonusXP", 10.0F);
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
	
	public boolean UsePermissions()
	{
		return config.getBoolean("Permissions");	
	}
	
	public void SetPermissionsOff()
	{
		config.set("Permissions", false);
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
	
	public void SetEconomyOff()
	{
		config.set("Economy.Enable", false);
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
		if(UsePermissions())
		{
			return marriageMaster.perms.has(player, Perm);
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
	
	public boolean UseUUIDs()
	{
		return config.getBoolean("UseUUIDs");
	}
	
	public boolean AllowSelfMarry()
	{
		return config.getBoolean("AllowSelfMarry");
	}
}