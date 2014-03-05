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

import com.google.common.io.Files;

import at.pcgamingfreaks.georgh.MarriageMaster.MarriageMaster;

public class Language
{
	private MarriageMaster marriageMaster;
	private FileConfiguration lang;
	private static final int LANG_VERSION = 3;

	public Language(MarriageMaster marriagemaster) 
	{
		marriageMaster = marriagemaster;
		LoadFile();
	}
	
	public String Get(String Option)
	{
		return lang.getString("Language." + Option);
	}
	
	public void Reload()
	{
		LoadFile();
	}
	
	private void LoadFile()
	{
		File file = new File(marriageMaster.getDataFolder() + File.separator + "Lang", marriageMaster.config.GetLanguage()+".yml");
		if(!file.exists())
		{
			ExtractLangFile(file);
		}
		lang = YamlConfiguration.loadConfiguration(file);
		UpdateLangFile(file);
	}
	
	private void ExtractLangFile(File Target)
	{
		try
		{
			marriageMaster.saveResource("Lang" + File.separator + marriageMaster.config.GetLanguage() + ".yml", true);
		}
		catch(Exception ex)
		{
			try
			{
				File file_en = new File(marriageMaster.getDataFolder() + File.separator + "Lang", "en.yml");
				if(!file_en.exists())
				{
					marriageMaster.saveResource("Lang" + File.separator + "en.yml", true);
				}
				Files.copy(file_en, Target);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private boolean UpdateLangFile(File file)
	{
		if(lang.getInt("Version") != LANG_VERSION)
		{
			if(marriageMaster.config.GetLanguageUpdateMode().equalsIgnoreCase("overwrite"))
			{
				ExtractLangFile(file);
				LoadFile();
				marriageMaster.log.info(Get("Console.LangUpdated"));
				return true;
			}
			else
			{
				switch(lang.getInt("Version"))
				{
					case 1:
						lang.set("Console.LangUpdated", "Language File has been updated.");
						lang.set("Priest.UnMadeYouAPriest", "%s has fired you as a priest.");
						lang.set("Priest.UnMadeAPriest", "You have fired %s as a priest.");
						lang.set("Ingame.PvPIsOff", "You can't hurt your Partner if you have PvP disabled.");
					case 2:
						lang.set("Ingame.ListeningStarted", "You are now listening to the marry chat.");
						lang.set("Ingame.ListeningStoped", "You are no longer listening to the marry chat.");
						lang.set("Description.ListenChat", "Shows all chatmessages sent from a married player to his partner.");
						break;
					case 3: return false;
					default: marriageMaster.log.warning("Language File Version newer than expected!"); return false;
				}
				lang.set("Version", LANG_VERSION);
				try 
				{
					lang.save(file);
					marriageMaster.log.info(Get("Console.LangUpdated"));
				}
		  	  	catch (IOException e) 
		  	  	{
		  	  		e.printStackTrace();
		  	  	}
				return true;
			}
		}
		return false;
	}
}
