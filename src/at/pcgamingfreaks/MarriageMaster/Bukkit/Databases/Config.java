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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Databases;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Config
{
	private JavaPlugin plugin;
	private FileConfiguration config;
	private static final int CONFIG_VERSION = 19;
	
	public Config(JavaPlugin pl)
	{
		plugin = pl;
		LoadConfig();
	}
	
	public boolean Loaded()
	{
		return config != null;
	}
	
	public void Reload()
	{
		LoadConfig();
	}
	
	private boolean LoadConfig()
	{
		File file = new File(plugin.getDataFolder(), "config.yml");
		if(!file.exists())
		{
			plugin.getLogger().info("No config found. Create new one ...");
			NewConfig(file);
		}
		else
		{
			try
			{
				config = YamlConfiguration.loadConfiguration(file);
				UpdateConfig(file);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				config = null;
			}
		}
		return config != null;
	}
	
	public boolean UUIDComp()
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
		config.set("UseUUIDs", Bukkit.getServer().getOnlineMode() && UUIDComp());
		config.set("AllowSelfMarry", false);
		config.set("AllowSelfDivorce", "auto");
		config.set("Surname", false);
		config.set("AllowSurnameColors", false);
		config.set("AllowedSurnameCharacters", "A-Za-z");
		config.set("AllowGiftsInCreative", false);
		config.set("UseMinepacks", false);
		config.set("UseBungeeCord", false);
		config.set("Chat.ToggleCommand", "chattoggle");
		config.set("Chat.PrivateFormat", "<heart> %1$s&r => %2$s: %3$s");
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
		config.set("Suffix.Enable", false);
		config.set("Suffix.String", " (<heart><partnername><heart>)");
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
		config.set("Confirmation.BothPlayersOnDivorce", false);
		config.set("Kiss.Enable", true);
		config.set("Kiss.WaitTime", 10);
		config.set("Kiss.HearthCount", 50);
		config.set("Kiss.CompatibilityMode", false);
		config.set("Misc.Metrics", true);
		config.set("Misc.AutoUpdate", true);
		config.set("Range.Marry", 25.0F);
		config.set("Range.Kiss", 2.0F);
		config.set("Range.KissInteract", 2.0F);
		config.set("Range.HearthVisible", 128.0F);
		config.set("Range.Heal", 2.0F);
		config.set("Range.BonusXP", 10.0F);
		config.set("Range.Gift", 0);
		config.set("Range.Backpack", 5);
		config.set("Teleport.Delay", false);
		config.set("Teleport.DelayTime", 3);
		config.set("Teleport.CheckSafety", true);
		config.set("Teleport.BlacklistedWorlds", new ArrayList<String>());
		config.set("Version", CONFIG_VERSION);
		try 
		{
			config.save(file);
			plugin.getLogger().info("Config file has been generated.");
		}
  	  	catch (IOException e) 
  	  	{
  	  		e.printStackTrace();
  	  		config = null;
  	  	}
	}
	
	private boolean UpdateConfig(File file)
	{
		plugin.getLogger().info("Config Version: " + config.getInt("Version") + " => " + ((config.getInt("Version") == CONFIG_VERSION) ? "no updated needed" : "update needed"));
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
			case 6:
				config.set("UseUUIDs", Bukkit.getServer().getOnlineMode() && UUIDComp());
			case 7:
				config.set("AllowSelfMarry", false);
			case 8:
				config.set("Teleport.Delay", false);
				config.set("Teleport.DelayTime", 3);
			case 9:
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
			case 13:
				config.set("Teleport.BlacklistedWorlds", (config.getInt("Version") > 5) ? config.getStringList("TPBlacklistedWorlds") : new ArrayList<String>());
				config.set("Chat.ToggleCommand", (config.getInt("Version") > 9) ? config.getString("ChatToggleCommand") : "chattoggle");
				config.set("UseBungeeCord", false);
				config.set("Chat.PrivateFormat", "<heart> %1$s&r => %2$s: %3$s");
			case 14:
				config.set("Confirmation.BothPlayersOnDivorce", false);
			case 15:
				config.set("AllowGiftsInCreative", false);
			case 16:
				config.set("Teleport.CheckSafety", true);
			case 17:
				config.set("AllowSelfDivorce", "auto");
				config.set("Suffix.Enable", false);
				config.set("Suffix.String", " (<heart><partnername><heart>)");
			case 18:
				config.set("AllowSurnameColors", false);
				config.set("AllowedSurnameCharacters", "A-Za-z");
			break;
			case CONFIG_VERSION: return false;
			default: plugin.getLogger().info("Config File Version newer than expected!"); return false;
		}
		config.set("Version", CONFIG_VERSION);
		try 
		{
			config.save(file);
			plugin.getLogger().info("Config File has been updated.");
		}
  	  	catch (IOException e) 
  	  	{
  	  		e.printStackTrace();
  	  		config = null;
  	  		return false;
  	  	}
		return true;
	}
	
	public boolean getUseBungeeCord()
	{
		return config.getBoolean("UseBungeeCord", false);
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
	
	public boolean getUsePermissions()
	{
		return config.getBoolean("Permissions");
	}
	
	public boolean getUseVaultPermissions()
	{
		return getUsePermissions() && config.getBoolean("VaultPermissions", true);
	}
	
	public boolean UsePrefix()
	{
		return config.getBoolean("Prefix.Enable");	
	}
	
	public String GetPrefix()
	{
		return config.getString("Prefix.String");
	}
	
	public boolean UseSuffix()
	{
		return config.getBoolean("Suffix.Enable");	
	}
	
	public String GetSuffix()
	{
		return config.getString("Suffix.String");
	}
	
	public boolean UseEconomy()
	{
		return config.getBoolean("Economy.Enable");
	}
	
	public double GetEconomy(String str)
	{
		return config.getDouble("Economy." + str);
	}
	
	public String GetDatabaseType()
	{
		return config.getString("Database.Type").toLowerCase();
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
	
	public boolean getConfirmationBothDivorce()
	{
		return config.getBoolean("Confirmation.BothPlayersOnDivorce");
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
		return config.getStringList("Teleport.BlacklistedWorlds");
	}
	
	public double GetRange(String option)
	{
		return config.getDouble("Range." + option, 25.0);
	}
	
	public boolean getUseUUIDs()
	{
		return config.getBoolean("UseUUIDs");
	}
	
	public String getChatToggleCommand()
	{
		return config.getString("Chat.ToggleCommand").replace(' ', '_');
	}
	
	public String getChatPrivateFormat()
	{
		return ChatColor.translateAlternateColorCodes('&', config.getString("Chat.PrivateFormat")).replace("<heart>", ChatColor.RED + "\u2764" + ChatColor.WHITE);
	}
	
	public boolean AllowSelfMarry()
	{
		return config.getBoolean("AllowSelfMarry");
	}
	
	public boolean AllowSelfDivorce()
	{
		switch(config.getString("AllowSelfDivorce").toLowerCase())
		{
			case "1":
			case "on":
			case "t":
			case "true":
			case "yes":
			case "y":
				return true;
			case "0":
			case "off":
			case "f":
			case "false":
			case "no":
			case "n":
				return false;
			default: return config.getBoolean("AllowSelfMarry");
		}
	}
	
	public boolean DelayTP()
	{
		return config.getBoolean("Teleport.Delay");
	}
	
	public boolean getCheckTPSafety()
	{
		return config.getBoolean("Teleport.CheckSafety", true);
	}
	
	public int TPDelayTime()
	{
		return config.getInt("Teleport.DelayTime");
	}
	
	public boolean getAllowGiftsInCreative()
	{
		return config.getBoolean("AllowGiftsInCreative");
	}
	
	public boolean getSurname()
	{
		return config.getBoolean("Surname", false);
	}
	
	public boolean getAllowSurnameColors()
	{
		return config.getBoolean("AllowSurnameColors", false);
	}
	
	public String getAllowedSurnameCharacters()
	{
		return config.getString("AllowedSurnameCharacters", "all");
	}
	
	public boolean getUseMinepacks()
	{
		return config.getBoolean("UseMinepacks", false);
	}
}