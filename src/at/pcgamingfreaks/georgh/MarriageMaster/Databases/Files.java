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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import at.pcgamingfreaks.georgh.MarriageMaster.MarriageMaster;

public class Files 
{
	private MarriageMaster marriageMaster;
	private Map<String, FileConfiguration> MarryMap;
	private List<String> Priests;
	
	public Files(MarriageMaster marriagemaster)
	{
		marriageMaster = marriagemaster;
		MarryMap = new HashMap<String, FileConfiguration>();
		Priests = new ArrayList<String>();
		LoadAllPlayers();
		LoadPriests();
	}
	
	private void LoadAllPlayers()
	{
		File file = new File((new StringBuilder()).append(marriageMaster.getDataFolder()).append(File.separator).append("players").toString());
		String temp;
		if(file.exists())
		{
			File[] allFiles = file.listFiles();
			for (File item : allFiles)
			{
				temp = item.getName();
				LoadPlayer(temp.substring(0, temp.length()-4));
			}
		}
	}
	
	public void Reload()
	{
		MarryMap.clear();
		LoadAllPlayers();
		LoadPriests();
	}
	
	public void LoadPlayer(String playername)
	{
		File file = new File((new StringBuilder()).append(marriageMaster.getDataFolder()).append(File.separator).append("players").append(File.separator).append(playername).append(".yml").toString());
		if(file.exists())
		{
			MarryMap.put(playername, YamlConfiguration.loadConfiguration(file));
		}
	}
	
	public void LoadPriests()
	{
		File file = new File((new StringBuilder()).append(marriageMaster.getDataFolder()).append(File.separator).append("priests.yml").toString());
		Priests.clear();
		if(file.exists())
		{
			BufferedReader in = null;
	        FileReader fr = null;
			try
			{
	            fr = new FileReader(file);
	            in = new BufferedReader(fr);
	            String str;
	            while ((str = in.readLine()) != null)
	            {
	            	if(!str.isEmpty())
	            	{
	            		Priests.add(str);
	            	}
	            }
	        }
			catch (Exception e)
			{
	            e.printStackTrace();
	        }
			finally
			{
				try
				{
					in.close();
	            	fr.close();
				}
				catch (Exception e)
				{
				}
	        }
		}
	}
	
	public void UnloadPlayer(String playername)
	{
		MarryMap.remove(playername);
	}
	
	public boolean GetPvPState(String playername)
	{
		return MarryMap.get(playername).getBoolean("PvP");
	}
	
	public void SetPvPState(String playername, boolean state)
	{
		MarryMap.get(playername).set("PvP", state);
		
		try
        {
			File file = new File((new StringBuilder()).append(marriageMaster.getDataFolder()).append(File.separator).append("players").append(File.separator).append(playername).append(".yml").toString());
			MarryMap.get(playername).save(file);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
	}
	
	public void SaveMarriedPlayerDivorce(String playername)
	{	
		MarryMap.get(playername).set("MarriedStatus", "Single");
		MarryMap.get(playername).set("MarriedTo", "");
		MarryMap.get(playername).set("MarriedBy", "");
		MarryMap.get(playername).set("MarriedDay", "");
		MarryMap.get(playername).set("MarriedHome", "");
		MarryMap.get(playername).set("MarriedHome.location.World" , "");
		MarryMap.get(playername).set("MarriedHome.location.X" , "");
		MarryMap.get(playername).set("MarriedHome.location.Y" , "");
		MarryMap.get(playername).set("MarriedHome.location.Z" , "");
		
		try
        {
			File file = new File((new StringBuilder()).append(marriageMaster.getDataFolder()).append(File.separator).append("players").append(File.separator).append(playername).append(".yml").toString());
			MarryMap.get(playername).save(file);
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

		MarryMap.put(playername,YamlConfiguration.loadConfiguration(file));
		
        Calendar cal = Calendar.getInstance();
        
        MarryMap.get(playername).set("MarriedStatus", "Married");
        MarryMap.get(playername).set("MarriedTo", otherPlayer);
        MarryMap.get(playername).set("MarriedBy", priester);
        MarryMap.get(playername).set("MarriedDay", cal.getTime());
        MarryMap.get(playername).set("MarriedHome", "");
        MarryMap.get(playername).set("PvP", false);
			
        try
        {
        	MarryMap.get(playername).save(file);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
	}
	
	public void SaveMarriedHome(Location loc, String playername)
	{
		File file = new File((new StringBuilder()).append(marriageMaster.getDataFolder()).append(File.separator).append("players").append(File.separator).append(playername).append(".yml").toString());
		
		if(MarryMap.get(playername).getString("MarriedStatus").equalsIgnoreCase("Married"))
		{
			MarryMap.get(playername).set("MarriedHome.location.World" , loc.getWorld().getName());
			MarryMap.get(playername).set("MarriedHome.location.X" , loc.getX());
			MarryMap.get(playername).set("MarriedHome.location.Y" , loc.getY());
			MarryMap.get(playername).set("MarriedHome.location.Z" , loc.getZ());
		
			try
	        {
				MarryMap.get(playername).save(file);
	        }
	        catch(Exception e)
	        {
	            e.printStackTrace();
	        }
		}
	}
	
	public void AddPriest(String priestname)
	{
		Priests.add(priestname);
		SavePriests();
	}
	
	public void SavePriests()
	{
		File file = new File((new StringBuilder()).append(marriageMaster.getDataFolder()).append(File.separator).append("priests.yml").toString());
		FileWriter writer = null;
		try
		{
			writer = new FileWriter(file,false);
			boolean first = true;
			for(String str: Priests)
			{
				if(first)
				{
					writer.write(str);
					first = false;
				}
				else
				{
					writer.write("\n" + str);
				}
			}
		}
  	  	catch (IOException e) 
  	  	{
  	  		e.printStackTrace();
  	  	}
		finally
		{
			try
			{
				writer.close();
			}
			catch (IOException e)
			{
			}
		}
	}
	
	public void DelPriest(String priestname)
	{
		Priests.remove(priestname);
		SavePriests();
	}
	
	public boolean IsPriester(String playername)
	{
		if(Priests.contains(playername))
		{
			return true;
		}
		
		return false;
	}
	
	public Location LoadMarriedHome(String playername)
	{
		try
		{
			World world = marriageMaster.getServer().getWorld(MarryMap.get(playername).getString("MarriedHome.location.World"));
			double x = (Double) MarryMap.get(playername).get("MarriedHome.location.X");
			double y = (Double) MarryMap.get(playername).get("MarriedHome.location.Y");
			double z = (Double) MarryMap.get(playername).get("MarriedHome.location.Z");
			
			return(new Location(world, x, y, z));
		}
		catch(Exception e)
		{
			return null;			
		}
	}
	
	public String GetPartner(String playername)
	{
		if(MarryMap.get(playername) != null)
		{
			String x = MarryMap.get(playername).getString("MarriedTo");
			if(x != null && !x.isEmpty())
			{
				return x;
			}
		}
		return null;
	}
	
	public TreeMap<String, String> LoadAllMarriedPlayers()
	{
		TreeMap<String, String> MarryMap_out = new TreeMap<String, String>();
		String marriedTo;
		for(Entry<String, FileConfiguration> entry : MarryMap.entrySet())
		{
			marriedTo = entry.getValue().getString("MarriedTo");
			if(marriedTo != null && !marriedTo.equalsIgnoreCase(""))
			{
				if(!MarryMap_out.containsKey(entry.getKey()) && !MarryMap_out.containsKey(marriedTo) && !MarryMap_out.containsValue(marriedTo) && !MarryMap_out.containsValue(entry.getKey()))
				{
					MarryMap_out.put(entry.getKey(), marriedTo);
				}
			}
		}
		
		return MarryMap_out;
	}
}
