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

package at.pcgamingfreaks.MarriageMaster.Bungee.Database;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import at.pcgamingfreaks.MarriageMaster.Bungee.MarriageMaster;

import com.google.common.io.ByteStreams;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class Language
{
	private MarriageMaster plugin;
	private Configuration lang;
	private ConfigurationProvider langprovider;
	private static final int LANG_VERSION = 1;
	
	public Language(MarriageMaster marriagemaster)
	{
		plugin = marriagemaster;
		langprovider = ConfigurationProvider.getProvider(YamlConfiguration.class);
		LoadLang();
	}
	
	public void Reload()
	{
		LoadLang();
	}
	
	private void LoadLang()
	{
		File file = new File(plugin.getDataFolder(), "lang_" + plugin.config.getLanguage() + ".yml");
		if(!file.exists())
		{
			ExtractLangFile(file);
		}
		try
		{
			lang = langprovider.load(file);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		UpdateLangFile(file);
	}
	
	private void ExtractLangFile(File file)
	{
		try
		{
			if(file.exists())
			{
				file.delete();
	        }
            file.createNewFile();
            try (InputStream is = plugin.getResourceAsStream("Lang/bungee_" + plugin.config.getLanguage() + ".yml"); OutputStream os = new FileOutputStream(file))
            {
                ByteStreams.copy(is, os);
            }
            catch(Exception e)
            {
            	try (InputStream is = plugin.getResourceAsStream("Lang/bungee_en.yml"); OutputStream os = new FileOutputStream(file))
                {
                    ByteStreams.copy(is, os);
                }
            }
            plugin.log.info("Lang extracted successfully!");
        }
		catch (IOException e)
		{
            e.printStackTrace();
        }
	}
	
	private boolean UpdateLangFile(File file)
	{
		if(plugin.config.getLanguageUpdateMode().equalsIgnoreCase("overwrite") && lang.getInt("Version") < LANG_VERSION)
		{
			ExtractLangFile(file);
			LoadLang();
			plugin.log.info(getString("Console.LangUpdated"));
			return true;
		}
		else
		{
			switch(lang.getInt("Version"))
			{
				case 0: break;
				case LANG_VERSION: return false;
				default: plugin.log.info("Lang File Version newer than expected!"); return false;
			}
			lang.set("Version", LANG_VERSION);
			try
			{
				langprovider.save(lang, file);
				plugin.log.info("Config File has been updated.");
			}
			catch (IOException e)
			{
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}
	
	//Geter
	public String getString(String Option)
	{
		return ChatColor.translateAlternateColorCodes('&', lang.getString("Language." + Option));
	}
	
	public BaseComponent[] getReady(String Option)
	{
		return TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', lang.getString("Language." + Option)));
	}
}