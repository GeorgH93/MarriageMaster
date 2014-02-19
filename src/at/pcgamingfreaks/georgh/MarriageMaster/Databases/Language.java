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
