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
import java.util.Calendar;
import java.util.TreeMap;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import at.pcgamingfreaks.georgh.MarriageMaster.MarriageMaster;

public class Files 
{
	private MarriageMaster marriageMaster;
	
	public Files(MarriageMaster marriagemaster)
	{
		marriageMaster = marriagemaster;
	}
	
	public boolean GetPvPState(String playername)
	{
		File file = new File((new StringBuilder()).append(marriageMaster.getDataFolder()).append(File.separator).append("players").append(File.separator).append(playername).append(".yml").toString());
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		return config.getBoolean("PvP");
	}
	
	public void SetPvPState(String playername, boolean state)
	{
		File file = new File((new StringBuilder()).append(marriageMaster.getDataFolder()).append(File.separator).append("players").append(File.separator).append(playername).append(".yml").toString());
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		config.set("PvP", state);
		
		try
        {
            config.save(file);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
	}
	
	public void SaveMarriedPlayerDivorce(String playername)
	{
		File file = new File((new StringBuilder()).append(marriageMaster.getDataFolder()).append(File.separator).append("players").append(File.separator).append(playername).append(".yml").toString());
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		
		config.set("MarriedStatus", "Single");
		config.set("MarriedTo", "");
		config.set("MarriedBy", "");
		config.set("MarriedDay", "");
		config.set("MarriedHome", "");
		config.set("MarriedHome.location.World" , "");
		config.set("MarriedHome.location.X" , "");
		config.set("MarriedHome.location.Y" , "");
		config.set("MarriedHome.location.Z" , "");
		
		try
        {
            config.save(file);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
	}

	public void SaveMarriedPlayer(String playername, String otherPlayer, String priester)
	{
		File file = new File((new StringBuilder()).append(marriageMaster.getDataFolder()).append(File.separator).append("players").append(File.separator).append(playername).append(".yml").toString());
		 
		if(!file.exists())
		{
		    try
		    {
		        file.createNewFile();
		    }
		    catch(Exception e)
		    {
		        e.printStackTrace();
		    }
		}

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		
        Calendar cal = Calendar.getInstance();
        
        config.set("MarriedStatus", "Married");
        config.set("MarriedTo", otherPlayer);
        config.set("MarriedBy", priester);
		config.set("MarriedDay", cal.getTime());
		config.set("MarriedHome", "");
		config.set("PvP", false);
			
        try
        {
            config.save(file);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
	}
	
	public void SaveMarriedHome(Location loc, String playername)
	{
		File file = new File((new StringBuilder()).append(marriageMaster.getDataFolder()).append(File.separator).append("players").append(File.separator).append(playername).append(".yml").toString());
		 
		if(!file.exists())
		{
		    try
		    {
		        file.createNewFile();
		    }
		    catch(Exception e)
		    {
		        e.printStackTrace();
		    }
		}
		
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		
		String status = config.getString("MarriedStatus");
		
		if(status.equalsIgnoreCase("Married"))
		{
			config.set("MarriedHome.location.World" , loc.getWorld().getName());
			config.set("MarriedHome.location.X" , loc.getX());
			config.set("MarriedHome.location.Y" , loc.getY());
			config.set("MarriedHome.location.Z" , loc.getZ());
		}
		
		try
        {
            config.save(file);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
	}
	
	public void SavePriester(String priestname)
	{
		File file = new File((new StringBuilder()).append(marriageMaster.getDataFolder()).append(File.separator).append("priests").append(File.separator).append(priestname).append(".yml").toString());

		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		
		config.set("Name", priestname);
		try 
		{
			config.save(file);
		}
  	  	catch (IOException e) 
  	  	{
  	  		e.printStackTrace();
  	  	}
	}
	
	public boolean IsPriester(String playername)
	{
		File file = new File((new StringBuilder()).append(marriageMaster.getDataFolder()).append(File.separator).append("priests").append(File.separator).append(playername).append(".yml").toString());
		if(!file.exists())
		{
			return false;
		}
		
		return true;
	}
	
	public Location LoadMarriedHome(String playername)
	{
		File file = new File((new StringBuilder()).append(marriageMaster.getDataFolder()).append(File.separator).append("players").append(File.separator).append(playername).append(".yml").toString());
		
		if(!file.exists())
		{
		    try
		    {
		        file.createNewFile();
		    }
		    catch(Exception e)
		    {
		        e.printStackTrace();
		    }
		}
		
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		

		
		Location loc = null;
		
		try
		{
			World world = marriageMaster.getServer().getWorld(config.getString("MarriedHome.location.World"));
			double x = (Double) config.get("MarriedHome.location.X");
			double y = (Double) config.get("MarriedHome.location.Y");
			double z = (Double) config.get("MarriedHome.location.Z");
			
			loc = new Location(world, x, y, z);
		}
		catch(Exception e)
		{
			return null;			
		}
		
		return loc;
	}
	
	public String GetPartner(String playername)
	{
		File file = new File((new StringBuilder()).append(marriageMaster.getDataFolder()).append(File.separator).append("players").append(File.separator).append(playername).append(".yml").toString());
		if(file.exists())
		{
			FileConfiguration config = YamlConfiguration.loadConfiguration(file);
			String x = config.getString("MarriedTo");
			if(x != null && !x.isEmpty())
			{
				return x;
			}
		}
		return null;
	}
	
	public TreeMap<String, String> LoadAllMarriedPlayers()
	{
		TreeMap<String, String> map = new TreeMap<String, String>();
		
		File file = new File((new StringBuilder()).append(marriageMaster.getDataFolder()).append(File.separator).append("players").toString());
		 
		if(file.exists())
		{
			File[] allFiles = file.listFiles();
			
			for (File item : allFiles)
			{
				File marriedPlayer = new File((new StringBuilder()).append(marriageMaster.getDataFolder()).append(File.separator).append("players").append(File.separator).append(item.getName()).toString());
				
				FileConfiguration config = YamlConfiguration.loadConfiguration(marriedPlayer);
				
				String marriedTo = config.getString("MarriedTo");
				
				if(marriedTo != null && !marriedTo.equalsIgnoreCase(""))
				{
					if(!map.containsKey(item.getName()) && !map.containsKey(marriedTo) && !map.containsValue(marriedTo) && !map.containsValue(item.getName()))
					{
						map.put(item.getName().replaceAll(".yml", ""), marriedTo);
					}
				}
			}
		}
		
		return map;
	}
}
