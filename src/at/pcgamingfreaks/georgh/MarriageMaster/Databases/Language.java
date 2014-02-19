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

	public Language(MarriageMaster marriagemaster) 
	{
		marriageMaster = marriagemaster;
		
		File file = new File(marriageMaster.getDataFolder() + File.separator + "Lang", marriageMaster.config.GetLanguage()+".yml");
		if(!file.exists())
		{
			try
			{
				marriageMaster.saveResource("Lang" + File.separator + marriageMaster.config.GetLanguage() + ".yml", false);
			}
			catch(Exception ex)
			{
				try
				{
					File file_en = new File(marriageMaster.getDataFolder() + File.separator + "Lang", "en.yml");
					if(!file_en.exists())
					{
						marriageMaster.saveResource("Lang" + File.separator + "en.yml", false);
					}
					Files.copy(file_en, file);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		
		lang = YamlConfiguration.loadConfiguration(file);
	}
	
	public String Get(String Option)
	{
		return lang.getString("Language." + Option);
	}
}
