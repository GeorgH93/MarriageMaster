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

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import at.pcgamingfreaks.georgh.MarriageMaster.MarriageMaster;

public class Config
{
	private MarriageMaster marriageMaster;
	private FileConfiguration config;
	
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
	
	private void NewConfig(File file)
	{
		FileConfiguration config = new YamlConfiguration();
		config.set("Permissions", false);
		config.set("Economy.Enable", false);
		config.set("Economy.Divorce", 100.00);
		config.set("Economy.Marry", 100.00);
		config.set("Economy.Tp", 25.00);
		config.set("Economy.HomeTp", 25.00);
		config.set("Economy.SetHome", 100.00);
		config.set("HealthRegain.Enable", true);
		config.set("HealthRegain.Amount", 2);
		config.set("BonusXp.Enable", true);
		config.set("BonusXp.Multiplier", 2);
		config.set("AllowBlockPvP", false);
		config.set("Announcement", true);
		config.set("InformOnPartnerJoin", true);
		config.set("PriestEnable", true);
		config.set("Language","en");
		config.set("Database.Type","Files");
		config.set("Database.MySQL.Host", "localhost");
		config.set("Database.MySQL.Database", "minecraft");
		config.set("Database.MySQL.User", "minecraft");
		config.set("Database.MySQL.Password", "minecraft");
		config.set("Version",2);
		
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
			case 1: config.set("Database.MySQL.Database", "minecraft"); config.set("Version", 2); break;
			case 2: return false;
			default: marriageMaster.log.info("Config File Version newer than expected!"); return false;
		}
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
	
	public boolean GetPriestStatus()
	{
		return config.getBoolean("PriestEnable");
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
	
	public boolean GetEconomyStatus()
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
}
