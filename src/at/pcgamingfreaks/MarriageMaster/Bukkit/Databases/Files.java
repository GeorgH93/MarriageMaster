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
import org.bukkit.entity.Player;

import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;

public class Files extends Database
{
	private Map<String, FileConfiguration> MarryMap;
	private List<String> Priests;
	
	public Files(MarriageMaster marriagemaster)
	{
		super(marriagemaster);
		MarryMap = new HashMap<String, FileConfiguration>();
		Priests = new ArrayList<String>();
		LoadAllPlayers();
		LoadPriests();
		CheckUUIDs();
	}
	
	private void LoadAllPlayers()
	{
		File file = new File((new StringBuilder()).append(plugin.getDataFolder()).append(File.separator).append("players").toString());
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
	
	private String GetPlayerID(Player player)
	{
		if(plugin.UseUUIDs)
		{
			return player.getUniqueId().toString().replace("-", "");
		}
		else
		{
			return player.getName();
		}
	}
	
	public void Recache()
	{
		MarryMap.clear();
		LoadAllPlayers();
		LoadPriests();
		CheckUUIDs();
	}
	
	private void LoadPlayer(String player)
	{
		File file = new File((new StringBuilder()).append(plugin.getDataFolder()).append(File.separator).append("players").append(File.separator).append(player).append(".yml").toString());
		if(file.exists())
		{
			MarryMap.put(player, YamlConfiguration.loadConfiguration(file));
			if(MarryMap.get(player).getString("MarriedTo") == player)
			{
				MarryMap.remove(player);
				file.delete();
			}
		}
	}
	
	private void LoadPriests()
	{
		File file = new File((new StringBuilder()).append(plugin.getDataFolder()).append(File.separator).append("priests.yml").toString());
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
	            		Priests.add(str.replace("\r", ""));
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
	
	private void CheckUUIDs()
	{
		if(!plugin.UseUUIDs)
		{
			return;
		}
		List<String> convert = new ArrayList<String>();
		for (String string : Priests)
		{
			if(string.length() != 32)
			{
				if(string.length() <= 16)
				{
					convert.add(string);
				}
			}
		}
		Map<String, FileConfiguration> CMarryMap = new HashMap<String, FileConfiguration>();
		for (Entry<String, FileConfiguration> entry : MarryMap.entrySet())
		{
			if(entry.getKey().length() != 32)
			{
				if(entry.getKey().length() <= 16)
				{
					CMarryMap.put(entry.getKey(),entry.getValue());
					continue;
				}
			}
			if(entry.getValue().getString("MarriedStatus").equalsIgnoreCase("married") && entry.getValue().getString("MarriedToUUID") == null)
			{
				CMarryMap.put(entry.getKey(),entry.getValue());
			}
		}
		if(convert.size() > 0 || CMarryMap.size() > 0)
		{
			plugin.log.info(plugin.lang.Get("Console.UpdateUUIDs"));
			for(String s : convert)
			{
				Priests.remove(s);
				s = UUIDConverter.getUUIDFromName(s);
				if(s != null)
				{
					Priests.add(s);
				}
			}
			String hilf;
			FileConfiguration fchilf;
			for (Entry<String, FileConfiguration> entry : CMarryMap.entrySet())
			{
				MarryMap.remove(entry.getKey());
				fchilf = entry.getValue();
				hilf = entry.getKey();
				if(entry.getKey().length() != 32)
				{
					hilf = UUIDConverter.getUUIDFromName(hilf);
					if(hilf != null)
					{
						fchilf.set("Name", entry.getKey());					
					}
				}
				if(fchilf.getString("MarriedStatus").equalsIgnoreCase("married") && fchilf.getString("MarriedToUUID") == null)
				{
					fchilf.set("MarriedToUUID", UUIDConverter.getUUIDFromName(fchilf.getString("MarriedTo")));
				}
				MarryMap.put(hilf,fchilf);
			}
			ReSaveAll();
			SavePriests();
			plugin.log.info(String.format(plugin.lang.Get("Console.UpdatedUUIDs"), convert.size() + CMarryMap.size()));
		}
	}
	
	private void ReSaveAll()
	{
		File file = new File((new StringBuilder()).append(plugin.getDataFolder()).append(File.separator).append("players").toString());
		if(file.exists())
		{
			File[] allFiles = file.listFiles();
			for (File item : allFiles)
			{
				item.delete();
			}
		}
		for (Entry<String, FileConfiguration> entry : MarryMap.entrySet())
		{
			try
			{
				entry.getValue().save(new File(file, entry.getKey() + ".yml"));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public void UpdatePlayer(Player player)
	{
		if(plugin.UseUUIDs)
		{
			FileConfiguration dat = MarryMap.get(player.getUniqueId().toString().replace("-", "")), dat2 = null;
			if(dat != null)
			{
				if(!dat.getString("Name").equalsIgnoreCase(player.getName()))
				{
					dat.set("Name", player.getName());
					if(dat.getString("MarriedStatus").equalsIgnoreCase("married"))
					{
						dat2 = MarryMap.get(dat.getString("MarriedToUUID"));
						dat2.set("MarriedTo", player.getName());
					}
					try
			        {
						File file = new File((new StringBuilder()).append(plugin.getDataFolder()).append(File.separator).append("players").append(File.separator).append(player.getUniqueId().toString().replace("-", "")).append(".yml").toString());
						dat.save(file);
						if(dat2 != null)
						{
							file = new File((new StringBuilder()).append(plugin.getDataFolder()).append(File.separator).append("players").append(File.separator).append(dat.getString("MarriedToUUID")).append(".yml").toString());
							dat2.save(file);
						}
			        }
			        catch(Exception e)
			        {
			            e.printStackTrace();
			        }
				}
			}
		}
	}
	
	public boolean GetPvPEnabled(Player player)
	{
		return MarryMap.get(GetPlayerID(player)).getBoolean("PvP");
	}
	
	public void SetPvPEnabled(Player player, boolean state)
	{
		String partner = plugin.UseUUIDs ? GetPartnerUUID(player) : GetPartner(player);
		String pid = GetPlayerID(player);
		MarryMap.get(pid).set("PvP", state);
		MarryMap.get(partner).set("PvP", state);
		try
        {
			File file = new File((new StringBuilder()).append(plugin.getDataFolder()).append(File.separator).append("players").append(File.separator).append(pid).append(".yml").toString());
			MarryMap.get(pid).save(file);
			file = new File((new StringBuilder()).append(plugin.getDataFolder()).append(File.separator).append("players").append(File.separator).append(partner).append(".yml").toString());
			MarryMap.get(partner).save(file);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
	}
	
	public void DivorcePlayer(Player player)
	{
		String partner = plugin.UseUUIDs ? GetPartnerUUID(player) : GetPartner(player);
		String pid = GetPlayerID(player);
		
		MarryMap.remove(pid);
		MarryMap.remove(partner);
		
		try
        {
			File file = new File((new StringBuilder()).append(plugin.getDataFolder()).append(File.separator).append("players").append(File.separator).append(pid).append(".yml").toString());
			file.delete();
			file = new File((new StringBuilder()).append(plugin.getDataFolder()).append(File.separator).append("players").append(File.separator).append(partner).append(".yml").toString());
			file.delete();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
	}
	
	public void MarryPlayers(Player player1, Player player2, String priester, String surname)
	{
		String p1id = GetPlayerID(player1);
		String p2id = GetPlayerID(player2);
		File file = new File((new StringBuilder()).append(plugin.getDataFolder()).append(File.separator).append("players").append(File.separator).append(p1id).append(".yml").toString());
		File file2 = new File((new StringBuilder()).append(plugin.getDataFolder()).append(File.separator).append("players").append(File.separator).append(p2id).append(".yml").toString());
		try
		{
			//Save P1
			MarryMap.put(p1id,YamlConfiguration.loadConfiguration(file));
	        MarryMap.get(p1id).set("MarriedStatus", "Married");
	        MarryMap.get(p1id).set("MarriedTo", player2.getName());
	        if(plugin.UseUUIDs)
	        {
	        	MarryMap.get(p1id).set("Name", player1.getName());
	        	MarryMap.get(p1id).set("MarriedToUUID", player2.getUniqueId().toString().replace("-", ""));
	        }
	        if(plugin.config.getSurname() && surname != null)
	        {
	        	MarryMap.get(p1id).set("Surname", surname);
	        }
	        MarryMap.get(p1id).set("MarriedBy", priester);
	        MarryMap.get(p1id).set("MarriedDay", Calendar.getInstance().getTime());
	        MarryMap.get(p1id).set("MarriedHome", "");
	        MarryMap.get(p1id).set("PvP", false);
	        MarryMap.get(p1id).set("ShareBackpack", false);
	        MarryMap.get(p1id).save(file);
	        //Save P2
	        MarryMap.put(p2id,YamlConfiguration.loadConfiguration(file));
	        MarryMap.get(p2id).set("MarriedStatus", "Married");
	        MarryMap.get(p2id).set("MarriedTo", player1.getName());
	        if(plugin.UseUUIDs)
	        {
	        	MarryMap.get(p2id).set("Name", player2.getName());
	        	MarryMap.get(p2id).set("MarriedToUUID", player1.getUniqueId().toString().replace("-", ""));
	        }
	        if(plugin.config.getSurname() && surname != null)
	        {
	        	MarryMap.get(p2id).set("Surname", surname);
	        }
	        MarryMap.get(p2id).set("MarriedBy", priester);
	        MarryMap.get(p2id).set("MarriedDay", Calendar.getInstance().getTime());
	        MarryMap.get(p2id).set("MarriedHome", "");
	        MarryMap.get(p2id).set("PvP", false);
	        MarryMap.get(p2id).set("ShareBackpack", false);
	        MarryMap.get(p2id).save(file2);
		}
        catch(Exception e)
        {
            e.printStackTrace();
        }
	}
	
	public void SetMarryHome(Location loc, Player player)
	{
		String pid = GetPlayerID(player);
		if(MarryMap.get(pid).getString("MarriedStatus").equalsIgnoreCase("Married"))
		{
			String partner = plugin.UseUUIDs ? GetPartnerUUID(player) : GetPartner(player);
			File file = new File((new StringBuilder()).append(plugin.getDataFolder()).append(File.separator).append("players").append(File.separator).append(pid).append(".yml").toString());
			File file2 = new File((new StringBuilder()).append(plugin.getDataFolder()).append(File.separator).append("players").append(File.separator).append(partner).append(".yml").toString());
			
			MarryMap.get(pid).set("MarriedHome.location.World" , loc.getWorld().getName());
			MarryMap.get(pid).set("MarriedHome.location.X" , loc.getX());
			MarryMap.get(pid).set("MarriedHome.location.Y" , loc.getY());
			MarryMap.get(pid).set("MarriedHome.location.Z" , loc.getZ());
			MarryMap.get(partner).set("MarriedHome.location.World" , loc.getWorld().getName());
			MarryMap.get(partner).set("MarriedHome.location.X" , loc.getX());
			MarryMap.get(partner).set("MarriedHome.location.Y" , loc.getY());
			MarryMap.get(partner).set("MarriedHome.location.Z" , loc.getZ());
		
			try
	        {
				MarryMap.get(pid).save(file);
				MarryMap.get(partner).save(file2);
	        }
	        catch(Exception e)
	        {
	            e.printStackTrace();
	        }
		}
	}
	
	public Location GetMarryHome(Player player)
	{
		String pid = GetPlayerID(player);
		try
		{
			World world = plugin.getServer().getWorld(MarryMap.get(pid).getString("MarriedHome.location.World"));
			if(world == null)
			{
				return null;
			}
			double x = (Double) MarryMap.get(pid).get("MarriedHome.location.X");
			double y = (Double) MarryMap.get(pid).get("MarriedHome.location.Y");
			double z = (Double) MarryMap.get(pid).get("MarriedHome.location.Z");
			
			return(new Location(world, x, y, z));
		}
		catch(Exception e)
		{
			return null;			
		}
	}
	
	public void SetPriest(Player player)
	{
		String pid = GetPlayerID(player);
		Priests.add(pid);
		SavePriests();
	}
	
	public void SavePriests()
	{
		File file = new File((new StringBuilder()).append(plugin.getDataFolder()).append(File.separator).append("priests.yml").toString());
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
	
	public void DelPriest(Player player)
	{
		String pid = GetPlayerID(player);
		Priests.remove(pid);
		SavePriests();
	}
	
	public boolean IsPriest(Player player)
	{
		String pid = GetPlayerID(player);
		if(Priests.contains(pid))
		{
			return true;
		}
		return false;
	}
	
	public String GetPartner(Player player)
	{
		String pid = GetPlayerID(player);
		if(MarryMap.get(pid) != null)
		{
			String x = MarryMap.get(pid).getString("MarriedTo");
			if(x != null && !x.isEmpty())
			{
				return x;
			}
		}
		return null;
	}
	
	private String GetPartnerUUID(Player player)
	{
		String pid = GetPlayerID(player);
		if(MarryMap.get(pid) != null)
		{
			return MarryMap.get(pid).getString("MarriedToUUID");
		}
		return null;
	}
	
	public String GetSurname(Player player)
	{
		String pid = GetPlayerID(player);
		if(MarryMap.get(pid) != null)
		{
			return MarryMap.get(pid).getString("Surname");
		}
		return null;
	}
	
	public void SetSurname(Player player, String Surname)
	{
		String pid = GetPlayerID(player);
		if(MarryMap.get(pid).getString("MarriedStatus").equalsIgnoreCase("Married"))
		{
			String partner = plugin.UseUUIDs ? GetPartnerUUID(player) : GetPartner(player);
			File file = new File((new StringBuilder()).append(plugin.getDataFolder()).append(File.separator).append("players").append(File.separator).append(pid).append(".yml").toString());
			File file2 = new File((new StringBuilder()).append(plugin.getDataFolder()).append(File.separator).append("players").append(File.separator).append(partner).append(".yml").toString());
			MarryMap.get(pid).set("Surname", Surname);
			MarryMap.get(partner).set("Surname", Surname);
			try
	        {
				MarryMap.get(pid).save(file);
				MarryMap.get(partner).save(file2);
	        }
	        catch(Exception e)
	        {
	            e.printStackTrace();
	        }
		}
	}
	
	public void SetShareBackpack(Player player, boolean allow)
	{
		String pid = GetPlayerID(player);
		try
		{
			MarryMap.get(pid).set("ShareBackpack", allow);
			File file = new File((new StringBuilder()).append(plugin.getDataFolder()).append(File.separator).append("players").append(File.separator).append(pid).append(".yml").toString());
			MarryMap.get(pid).save(file);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public boolean GetPartnerShareBackpack(Player player)
	{
		String pid = GetPlayerID(player);
		if(MarryMap.get(pid) != null)
		{
			return MarryMap.get(pid).getBoolean("ShareBackpack", false);
		}
		return false;
	}
	
	public TreeMap<String, String> GetAllMarriedPlayers()
	{
		TreeMap<String, String> MarryMap_out = new TreeMap<String, String>();
		String marriedTo;
		if(plugin.UseUUIDs)
		{
			for(Entry<String, FileConfiguration> entry : MarryMap.entrySet())
			{
				marriedTo = entry.getValue().getString("MarriedTo");
				if(marriedTo != null && !marriedTo.equalsIgnoreCase(""))
				{
					if(!MarryMap_out.containsKey(entry.getValue().getString("Name")) && !MarryMap_out.containsKey(marriedTo) && !MarryMap_out.containsValue(marriedTo) && !MarryMap_out.containsValue(entry.getValue().getString("Name")))
					{
						MarryMap_out.put(entry.getValue().getString("Name"), marriedTo);
					}
				}
			}
		}
		else
		{
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
		}
		
		return MarryMap_out;
	}
}
