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
	private static final int LANG_VERSION = 8;

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
					case 3:
						lang.set("Ingane.PriestMarryOff", "Priest (%s) is now offline, wedding called off.");
						lang.set("Ingane.PlayerMarryOff", "%s is now offline, wedding called off.");
						lang.set("Ingane.PlayerCalledOff", "%s called the wedding off.");
						lang.set("Ingane.YouCalledOff", "You have called the wedding off.");
						lang.set("Priest.Confirm", "Accept the marriage with /marry accept or deny it with /marry deny");
						lang.set("Priest.AlreadyAccepted", "You have already accepted the marriage.");
						lang.set("Priest.NoRequest", "There is no open marriage request.");
						lang.set("Dialog.DoYouWant", "Do you %1$s, want to marry %2$s on this server?");
						lang.set("Dialog.AndDoYouWant", "And do you %1$s, want to marry %2$s on this server?");
						lang.set("Dialog.Married", "I now pronounce you husband and wife. You may now kiss .");
						lang.set("Dialog.YesIWant", "Yes, I will!");
						lang.set("Dialog.NoIDontWant", "No, I will not!");
					case 4:
						lang.set("Priest.NotYourSelf", "You are not allowed to marry yourself.");
						lang.set("Ingame.NoItemInHand", "You are not holding an item.");
						lang.set("Ingame.PartnerInvFull", "Your partner have no empty space in his inventory.");
						lang.set("Ingame.ItemSent", "You have sent your partner %1$s %2$s.");
						lang.set("Ingame.ItemReceived", "Your partner have sent you %1$s %2$s.");
						lang.set("Ingame.GiftsOnlyInSurvival", "You can only gift items to your partner when you are in survival mode.");
				        lang.set("Ingame.YouKissed", "You have kissed your partner!");
				        lang.set("Ingame.YouGotKissed", "Your partner has kissed you!");
				        lang.set("Ingame.TooFarToKiss", "You are too far away to kiss your partner. (Max 2 Blocks)");
				        lang.set("Ingame.KissWait", "You have to wait %s seconds to kiss your partner again.");
						lang.set("Description.Kiss", "Kisses your partner.");
						lang.set("Description.Gift", "Gifts the item in your hand to your partner.");
						lang.set("Console.MetricsOffline", "Metrics offline.");
						lang.set("Economy.GiftPaid", "You have paid %1$s for gifting an item to your partner (%2$s left)");
					case 5:
						lang.set("Economy.GiftPaid", "You have paid %1$s for gifting an item to your partner (%2$s left)");
						lang.set("Ingame.WorldNotAllowed", "Your partner is in a world where tp is not allowed.");
						lang.set("Ingame.Updated", "Plugin updated, will be loaded on next restart/reload.");
						lang.set("Ingame.NoUpdate", "No plugin update available.");
						lang.set("Description.Update", "Checks if there is an update available and downloads it.");
						lang.set("Description.Marry", "Marry two Persons in a range of %s blocks.");
						lang.set("Description.Divorce", "Divorces two Persons in a range of %s blocks.");
					case 6:
					case 7:
						lang.set("Console.NotSupportedNet", "Not supported MC version. Heart effect disabled.");
						lang.set("Console.UpdateUUIDs", "Start updating database to UUIDs ...");
						lang.set("Console.UpdatedUUIDs", "Updated %s accounts to UUIDs.");
						break;
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
