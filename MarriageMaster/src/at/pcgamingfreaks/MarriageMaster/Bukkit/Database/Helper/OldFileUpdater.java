/*
 *   Copyright (C) 2021 GeorgH93
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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Database.Helper;

import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.Message.MessageColor;
import at.pcgamingfreaks.yaml.YAML;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * This is a helper class with all the config and language keys to upgrade old (V1.X) config and language files into new (V2.X) ones.
 * I have put the code to copy the values into static methods into this class in order to make the config and language class more readable and don't blot them unnecessarily with all of this static copy operations.
 */
@SuppressWarnings("SpellCheckingInspection") // I don't want to see the info about fixed typos in idea.
public final class OldFileUpdater
{
	public static void updateLanguage(YAML oldYAML, YAML newYAML)
	{
		Map<String, String> simpleConverter = new LinkedHashMap<>(), advancedConverter = new LinkedHashMap<>();
		String[] keys;
		String helper;
		for(String key : oldYAML.getKeys(true))
		{
			try
			{
				keys = key.split("\\.");
				if(keys.length == 3)
				{
					switch(keys[1])
					{
						case "Ingame": helper = "Language.Ingame.";
							switch(keys[2])
							{
								case "NotMarried": advancedConverter.put(helper + "NotMarried", MessageColor.RED + oldYAML.getString(key)); break;
								case "NoMarriedPlayers": advancedConverter.put(helper + "List.NoMarriedPlayers", MessageColor.RED + oldYAML.getString(key)); break;
								case "NoPermission": advancedConverter.put(helper + "NoPermission", MessageColor.RED + oldYAML.getString(key)); break;
								case "PartnerOnline": advancedConverter.put(helper + "JoinLeaveInfo.PartnerOnline", MessageColor.GREEN + oldYAML.getString(key)); break;
								case "PartnerNowOnline": advancedConverter.put(helper + "JoinLeaveInfo.PartnerNowOnline", MessageColor.GREEN + oldYAML.getString(key)); break;
								case "PartnerOffline": advancedConverter.put(helper + "JoinLeaveInfo.PartnerOffline", MessageColor.GOLD + oldYAML.getString(key)); break;
								case "PartnerNowOffline": advancedConverter.put(helper + "JoinLeaveInfo.PartnerNowOffline", MessageColor.GOLD + oldYAML.getString(key)); break;
								case "PvPOn": advancedConverter.put(helper + "PvP.On", MessageColor.GREEN + oldYAML.getString(key)); break;
								case "PvPOff": advancedConverter.put(helper + "PvP.Off", MessageColor.GREEN + oldYAML.getString(key)); break;
								case "PvPIsOff": advancedConverter.put(helper + "PvP.IsDisabled", MessageColor.RED + oldYAML.getString(key)); break;
								case "PlayerNoHome": advancedConverter.put(helper + "Home.PlayerNoHome", MessageColor.RED + oldYAML.getString(key)); break;
								case "NoHome": advancedConverter.put(helper + "Home.NoHome", MessageColor.RED + oldYAML.getString(key)); break;
								case "HomeTP": advancedConverter.put(helper + "Home.TPed", MessageColor.GREEN + oldYAML.getString(key)); break;
								case "HomeSet": advancedConverter.put(helper + "Home.Set", MessageColor.GREEN + oldYAML.getString(key)); break;
								case "HomeDeleted": advancedConverter.put(helper + "Home.Deleted", MessageColor.GREEN + oldYAML.getString(key)); break;
								case "PlayerNotOn": advancedConverter.put(helper + "PlayerNotOnline", MessageColor.RED + oldYAML.getString(key).replaceAll("%s", "{PlayerName}")); break;
								case "NoTPInVanish": advancedConverter.put(helper + "TP.PartnerVanished", MessageColor.RED + oldYAML.getString(key)); break;
								case "TP": advancedConverter.put(helper + "TP.Teleport", MessageColor.GREEN + oldYAML.getString(key)); break;
								case "TPto": advancedConverter.put(helper + "TP.TeleportTo", MessageColor.GREEN + oldYAML.getString(key)); break;
								case "TPUnsafe": advancedConverter.put(helper + "TP.Unsafe", MessageColor.RED + oldYAML.getString(key)); break;
								case "TPtoUnsafe": advancedConverter.put(helper + "TP.ToUnsafe", MessageColor.RED + oldYAML.getString(key)); break;
								case "WorldNotAllowed": advancedConverter.put(helper + "TP.WorldNotAllowed", MessageColor.RED + oldYAML.getString(key)); break;
								case "ListeningStarted": advancedConverter.put(helper + "Chat.ListeningStarted", MessageColor.GREEN + oldYAML.getString(key)); break;
								case "ListeningStoped": advancedConverter.put(helper + "Chat.ListeningStopped", MessageColor.RED + oldYAML.getString(key)); break;
								case "ChatJoined": advancedConverter.put(helper + "Chat.Joined", MessageColor.GREEN + oldYAML.getString(key)); break;
								case "ChatLeft": advancedConverter.put(helper + "Chat.Left", MessageColor.RED + oldYAML.getString(key)); break;
								case "BackpackOnlyInSurvival": advancedConverter.put(helper + "Backpack.OnlyInSurvival", MessageColor.RED + oldYAML.getString(key)); break;
								case "BackpackShareOn": advancedConverter.put(helper + "Backpack.ShareOn", MessageColor.GREEN + oldYAML.getString(key)); break;
								case "BackpackShareOff": advancedConverter.put(helper + "Backpack.ShareOff", MessageColor.RED + oldYAML.getString(key)); break;
								case "BackpackOpend": advancedConverter.put(helper + "Backpack.Opened", MessageColor.GREEN + oldYAML.getString(key)); break;
								case "BackpackShareDenied": advancedConverter.put(helper + "Backpack.ShareDenied", MessageColor.RED + oldYAML.getString(key)); break;
								case "NaN": advancedConverter.put(helper + "NaN", MessageColor.RED + oldYAML.getString(key)); break;
								case "YouKissed": advancedConverter.put(helper + "Kiss.Kissed", MessageColor.GREEN + oldYAML.getString(key)); break;
								case "YouGotKissed": advancedConverter.put(helper + "Kiss.GotKissed", MessageColor.GREEN + oldYAML.getString(key)); break;
								case "TooFarToKiss": advancedConverter.put(helper + "Kiss.TooFarAway", MessageColor.RED + oldYAML.getString(key).replaceAll("2 Blocks", "{Distance} Blocks")); break;
								case "KissWait": advancedConverter.put(helper + "Kiss.Wait", MessageColor.RED + oldYAML.getString(key).replaceAll("%s", "{Time}")); break;
								case "GiftsOnlyInSurvival": advancedConverter.put(helper + "Gift.OnlyInSurvival", MessageColor.RED + oldYAML.getString(key)); break;
								case "NoUpdate": advancedConverter.put(helper + "Admin.NoUpdate", MessageColor.GOLD + oldYAML.getString(key)); break;
								case "Updated": advancedConverter.put(helper + "Admin.Updated", MessageColor.GREEN + oldYAML.getString(key)); break;
								case "CheckingForUpdates": advancedConverter.put(helper + "Admin.CheckingForUpdates", MessageColor.BLUE + oldYAML.getString(key)); break;
								case "PartnerInvFull": advancedConverter.put(helper + "Gift.PartnerInvFull", MessageColor.RED + oldYAML.getString(key)); break;
								case "NoItemInHand": advancedConverter.put(helper + "Gift.NoItemInHand", MessageColor.RED + oldYAML.getString(key)); break;
								case "TPMoved": advancedConverter.put(helper + "TP.Moved", MessageColor.RED + oldYAML.getString(key)); break;
								case "TPDontMove": advancedConverter.put(helper + "TP.DontMove", MessageColor.RED + oldYAML.getString(key).replaceAll("%s", "{Time}")); break;
								case "PriestMarryOff": advancedConverter.put(helper + "Marry.PriestOff", MessageColor.RED + oldYAML.getString(key).replaceAll("%s", "{DisplayName}" + MessageColor.RED)); break;
								case "PlayerMarryOff":  advancedConverter.put(helper + "Marry.PlayerOff", MessageColor.RED + oldYAML.getString(key).replaceAll("%s", "{DisplayName}" + MessageColor.RED));
														advancedConverter.put(helper + "Marry.Self.PlayerOff", MessageColor.RED + oldYAML.getString(key).replaceAll("%s", "{DisplayName}" + MessageColor.RED)); break;
								case "PlayerCalledOff": advancedConverter.put(helper + "Marry.PlayerCalledOff", MessageColor.RED + oldYAML.getString(key).replaceAll("%s", "{DisplayName}" + MessageColor.RED)); break;
								case "YouCalledOff": advancedConverter.put(helper + "Marry.YouCalledOff", MessageColor.RED + oldYAML.getString(key)); break;
								case "MarryConfirm": advancedConverter.put(helper + "Marry.Self.Confirm", MessageColor.WHITE + oldYAML.getString(key).replaceAll("%s", "{DisplayName}" + MessageColor.WHITE)); break;
								case "NotInRange": advancedConverter.put(helper + "PartnerNotInRange", MessageColor.RED + oldYAML.getString(key)); break;
								case "AlreadyMarried": advancedConverter.put(helper + "Marry.Self.AlreadyMarried", MessageColor.RED + oldYAML.getString(key)); break;
								case "NotYourself": advancedConverter.put(helper + "Marry.Self.NotYourself", MessageColor.RED + oldYAML.getString(key)); break;
								case "MarryRequestSent": advancedConverter.put(helper + "Marry.Self.AlreadyMarried", MessageColor.RED + oldYAML.getString(key)); break;
								case "AlreadyOpenRequest": advancedConverter.put(helper + "Marry.Self.AlreadyOpenRequest", MessageColor.RED + oldYAML.getString(key)); break;
								case "OtherAlreadyMarried": advancedConverter.put(helper + "Marry.Self.OtherAlreadyMarried", MessageColor.RED + oldYAML.getString(key).replaceAll("%s", "{DisplayName}" + MessageColor.RED)); break;
								case "BroadcastMarriage": advancedConverter.put(helper + "Marry.Self.Broadcast", MessageColor.GREEN + oldYAML.getString(key).replaceAll("%1\\$s", "{Player1DisplayName}" + MessageColor.GREEN).replaceAll("%2\\$s", "{Player2DisplayName}" + MessageColor.GREEN)); break;
								case "HasMarried": advancedConverter.put(helper + "Marry.Self.Married", MessageColor.GREEN + oldYAML.getString(key).replaceAll("%s", "{DisplayName}" + MessageColor.GREEN)); break;
								case "Divorced": advancedConverter.put(helper + "Divorce.Self.Divorced", MessageColor.GREEN + oldYAML.getString(key).replaceAll("%s", "{DisplayName}" + MessageColor.GREEN)); break;
								case "DivorcedPlayer": advancedConverter.put(helper + "Divorce.Self.DivorcedPlayer", MessageColor.GREEN + oldYAML.getString(key).replaceAll("%s", "{DisplayName}" + MessageColor.GREEN)); break;
								case "BroadcastDivorce": advancedConverter.put(helper + "Divorce.Self.Broadcast", MessageColor.GREEN + oldYAML.getString(key).replaceAll("%1\\$s", "{Player1DisplayName}" + MessageColor.GREEN).replaceAll("%2\\$s", "{Player2DisplayName}" + MessageColor.GREEN)); break;
							}
							break;
						case "Priest": helper = "Language.Ingame.";
							switch(keys[2])
							{
								case "NotWithHimself": advancedConverter.put(helper + "Marry.NotWithHimself", MessageColor.RED + oldYAML.getString(key).replaceAll("%s", "{DisplayName}" + MessageColor.RED)); break;
								case "NotInRange": advancedConverter.put(helper + "Marry.NotInRange", MessageColor.RED + oldYAML.getString(key)); break;
								case "AlreadyMarried": advancedConverter.put(helper + "Marry.AlreadyMarried", MessageColor.RED + oldYAML.getString(key)); break;
								case "BroadcastMarriage": advancedConverter.put(helper + "Marry.Broadcast", MessageColor.GREEN + oldYAML.getString(key).replaceAll("%1\\$s", "{PriestDisplayName}" + MessageColor.GREEN).replaceAll("%2\\$s", "{Player1DisplayName}" + MessageColor.GREEN).replaceAll("%3\\$s", "{Player2DisplayName}" + MessageColor.GREEN)); break;
								case "BroadcastDivorce": advancedConverter.put(helper + "Divorce.Broadcast", MessageColor.GREEN + oldYAML.getString(key).replaceAll("%1\\$s", "{PriestDisplayName}" + MessageColor.GREEN).replaceAll("%2\\$s", "{Player1DisplayName}" + MessageColor.GREEN).replaceAll("%3\\$s", "{Player2DisplayName}" + MessageColor.GREEN)); break;
								case "Married": advancedConverter.put(helper + "Marry.Married", MessageColor.GREEN + oldYAML.getString(key).replaceAll("%1\\$s", "{Player1DisplayName}" + MessageColor.GREEN).replaceAll("%2\\$s", "{Player2DisplayName}" + MessageColor.GREEN)); break;
								case "HasMarried": advancedConverter.put(helper + "Marry.HasMarried", MessageColor.GREEN + oldYAML.getString(key).replaceAll("%1\\$s", "{PriestDisplayName}" + MessageColor.GREEN).replaceAll("%2\\$s", "{PartnerDisplayName}" + MessageColor.GREEN)); break;
								case "MadeYouAPriest": advancedConverter.put(helper + "SetPriest.MadeYouAPriest", MessageColor.GREEN + oldYAML.getString(key).replaceAll("%s", "{Name}" + MessageColor.GREEN)); break;
								case "MadeAPriest": advancedConverter.put(helper + "SetPriest.YouMadeAPriest", MessageColor.GREEN + oldYAML.getString(key).replaceAll("%s", "{Name}" + MessageColor.GREEN)); break;
								case "UnMadeYouAPriest": advancedConverter.put(helper + "SetPriest.FiredYou", MessageColor.RED + oldYAML.getString(key).replaceAll("%s", "{Name}" + MessageColor.RED)); break;
								case "UnMadeAPriest": advancedConverter.put(helper + "SetPriest.YouFiredAPriest", MessageColor.RED + oldYAML.getString(key).replaceAll("%s", "{Name}" + MessageColor.RED)); break;
								case "PlayerNotMarried": advancedConverter.put(helper + "PlayerNotMarried", MessageColor.RED + oldYAML.getString(key)); break;
								case "DivorcedPlayer": advancedConverter.put(helper + "Divorce.DivorcedPlayer", MessageColor.GREEN + oldYAML.getString(key).replaceAll("%1\\$s", "{PriestDisplayName}" + MessageColor.GREEN).replaceAll("%2\\$s", "{PartnerDisplayName}" + MessageColor.GREEN)); break;
								case "Divorced": advancedConverter.put(helper + "Divorce.Divorced", MessageColor.GREEN + oldYAML.getString(key).replaceAll("%1\\$s", "{Player1DisplayName}" + MessageColor.GREEN).replaceAll("%2\\$s", "{Player2DisplayName}" + MessageColor.GREEN)); break;
								case "Confirm": advancedConverter.put(helper + "Marry.Confirm", oldYAML.getString(key)); break;
								case "AlreadyOpenRequest": advancedConverter.put(helper + "Marry.AlreadyOpenRequest", MessageColor.RED + oldYAML.getString(key).replaceAll("%s", "{DisplayName}" + MessageColor.RED)); break;
								case "DivPlayerOff": advancedConverter.put(helper + "Divorce.PlayerOff", MessageColor.RED + oldYAML.getString(key).replaceAll("%s", "{DisplayName}" + MessageColor.RED)); break;
								case "DivPriestOff": advancedConverter.put(helper + "Divorce.PriestOff", MessageColor.RED + oldYAML.getString(key).replaceAll("%s", "{DisplayName}" + MessageColor.RED)); break;
								case "PlayerCanceled": advancedConverter.put(helper + "Divorce.PlayerCanceled", MessageColor.RED + oldYAML.getString(key).replaceAll("%s", "{DisplayName}" + MessageColor.RED)); break;
								case "DivorceCanceled": advancedConverter.put(helper + "Divorce.YouCancelled", oldYAML.getString(key)); break;
								case "DivorceConfirm": advancedConverter.put(helper + "Divorce.Confirm", oldYAML.getString(key)); break;
								case "DivorceRequestSent": advancedConverter.put(helper + "Divorce.Self.RequestSent", oldYAML.getString(key)); break;
								case "SurnameSet": advancedConverter.put(helper + "Surname.SetSuccessful", MessageColor.GREEN + oldYAML.getString(key)); break;
							}
							break;
						case "Dialog": simpleConverter.put("Dialog." + keys[2], key); break;
						case "Description": helper = "Language.Commands.Description.";
							switch(keys[2])
							{
								case "ListAll": simpleConverter.put(helper + "List", key); break;
								case "SelfMarry": simpleConverter.put(helper + "RequirePriest", key); break;
								case "SelfDivorce": simpleConverter.put(helper + "DivorceRequiresPriest", key); break;
								case "ListenChat": simpleConverter.put(helper + "ChatListen", key); break;
								case "TP": simpleConverter.put(helper + "Tp", key); break;
								case "Priest": simpleConverter.put(helper + "SetPriest", key); break;
								case "TPHome": simpleConverter.put(helper + "Home", key); break;
								case "TPHomeOther": simpleConverter.put(helper + "HomeOther", key); break;
								default: simpleConverter.put(helper + keys[2], key);
							}
							break;
						case "Economy": helper = "Language.Economy.";
							switch(keys[2])
							{
								case "NotEnough": advancedConverter.put(helper + "NotEnough", MessageColor.RED + oldYAML.getString(key).replaceAll("%1\\$s", "{Cost} {CurrencyName}")); break;
								case "PartnerNotEnough": advancedConverter.put(helper + "PartnerNotEnough", MessageColor.RED + oldYAML.getString(key).replaceAll("%1\\$s", "{Cost} {CurrencyName}")); break;
								case "NotEnoughPriestInfo": advancedConverter.put(helper + "PriestMarryNotEnough", MessageColor.RED + oldYAML.getString(key)); break;
								case "DivNotEnoPriestI": advancedConverter.put(helper + "PriestDivorceNotEnough", MessageColor.RED + oldYAML.getString(key)); break;
								case "TPPaid": advancedConverter.put(helper + "TpPaid", MessageColor.GREEN + oldYAML.getString(key).replaceAll("%1\\$s", "{Cost} {CurrencyName}").replaceAll("%2\\$s", "{Remaining} {CurrencyName}")); break;
								case "HomeTPPaid": advancedConverter.put(helper + "HomeTPPaid", MessageColor.GREEN + oldYAML.getString(key).replaceAll("%1\\$s", "{Cost} {CurrencyName}").replaceAll("%2\\$s", "{Remaining} {CurrencyName}")); break;
								case "SetHomePaid": advancedConverter.put(helper + "SetHomePaid", MessageColor.GREEN + oldYAML.getString(key).replaceAll("%1\\$s", "{Cost} {CurrencyName}").replaceAll("%2\\$s", "{Remaining} {CurrencyName}")); break;
								case "GiftPaid": advancedConverter.put(helper + "GiftPaid", MessageColor.GREEN + oldYAML.getString(key).replaceAll("%1\\$s", "{Cost} {CurrencyName}").replaceAll("%2\\$s", "{Remaining} {CurrencyName}")); break;
								case "MarriagePaid": advancedConverter.put(helper + "MarriagePaid", MessageColor.GREEN + oldYAML.getString(key).replaceAll("%1\\$s", "{Cost} {CurrencyName}").replaceAll("%2\\$s", "{Remaining} {CurrencyName}")); break;
								case "DivorcePaid": advancedConverter.put(helper + "DivorcePaid", MessageColor.GREEN + oldYAML.getString(key).replaceAll("%1\\$s", "{Cost} {CurrencyName}").replaceAll("%2\\$s", "{Remaining} {CurrencyName}")); break;
							}
							break;
					}
				}
			}
			catch(Exception e)
			{
				MarriageMaster.getInstance().getLogger().warning("Failed to convert the old \"" + key + "\" language value into the corresponding new one.");
				e.printStackTrace();
			}
		}

		// Patch them into the lang file
		try
		{
			for(Map.Entry<String, String> entry : advancedConverter.entrySet())
			{
				newYAML.set(entry.getKey(), entry.getValue());
			}
			for(Map.Entry<String, String> entry : simpleConverter.entrySet())
			{
				newYAML.set(entry.getKey(), oldYAML.getString(entry.getValue()));
			}
		}
		catch(Exception e)
		{
			MarriageMaster.getInstance().getLogger().warning("Failed to write the old language values into the new language file.");
			e.printStackTrace();
		}
	}

	public static void updateConfig(YAML oldYAML, YAML newConfig)
	{
		// Just copy all the old settings into the new file ...
		try
		{
			newConfig.set("Language.Language", oldYAML.getString("Language", "en"));
			newConfig.set("PvP.AllowBlocking", oldYAML.getBoolean("AllowBlockPvP", false));
			newConfig.set("InfoOnPartnerJoinLeave.Enable", oldYAML.getBoolean("InformOnPartnerJoin", true));
			newConfig.set("InfoOnPartnerJoinLeave.JoinDelay", oldYAML.getInt("DelayMessageForJoiningPlayer", 0));
			newConfig.set("Database.UseUUIDs", oldYAML.getBoolean("UseUUIDs", true));
			newConfig.set("Database.UUID_Type", oldYAML.getString("UUID_Type", "auto"));
			newConfig.set("Database.Type", oldYAML.getString("Database.Type", "SQLite"));
			if(oldYAML.getString("Database.Type", "SQLite").equalsIgnoreCase("mysql"))
			{
				newConfig.set("Database.SQL.Tables.Fields.PriestID", "priest_id");
			}
			newConfig.set("Database.SQL.Host", oldYAML.getString("Database.MySQL.Host", "localhost"));
			newConfig.set("Database.SQL.Database", oldYAML.getString("Database.MySQL.Database", "minecraft"));
			newConfig.set("Database.SQL.User", oldYAML.getString("Database.MySQL.User", "minecraft"));
			newConfig.set("Database.SQL.Password", oldYAML.getString("Database.MySQL.Password", "minecraft"));
			newConfig.set("Database.SQL.MaxConnections", oldYAML.getInt("Database.MySQL.MaxConnections", 4));
			newConfig.set("Database.SQL.Tables.User", oldYAML.getString("Database.Tables.User", "marry_players"));
			newConfig.set("Database.SQL.Tables.Priests", oldYAML.getString("Database.Tables.Priests", "marry_priests"));
			newConfig.set("Database.SQL.Tables.Partner", oldYAML.getString("Database.Tables.Partner", "marry_partners"));
			newConfig.set("Database.SQL.Tables.Home", oldYAML.getString("Database.Tables.Home", "marry_home"));
			newConfig.set("Marriage.AnnounceOnMarriage", oldYAML.getBoolean("Announcement", true));
			newConfig.set("Marriage.RequirePriest", !oldYAML.getBoolean("AllowSelfMarry", false));
			if(!oldYAML.getString("AllowSelfDivorce", "auto").equalsIgnoreCase("auto"))
			{
				switch(oldYAML.getString("AllowSelfDivorce", "auto").trim().toLowerCase())
				{
					case "on": case "yes": case "true": newConfig.set("Marriage.DivorceRequiresPriest", false); break;
					case "off": case "no": case "false": newConfig.set("Marriage.DivorceRequiresPriest", true);
				}
			}
			newConfig.set("Marriage.Surnames.Enable", oldYAML.getBoolean("Surname", false));
			newConfig.set("Marriage.Surnames.AllowColors", oldYAML.getBoolean("AllowSurnameColors", false));
			newConfig.set("Marriage.Surnames.AllowedCharacters", oldYAML.getString("AllowedSurnameCharacters", "A-Za-z"));
			newConfig.set("Marriage.Confirmation.Enable", oldYAML.getBoolean("Confirmation.Enable", true));
			newConfig.set("Marriage.Confirmation.AutoDialog", oldYAML.getBoolean("Confirmation.AutoDialog", true));
			newConfig.set("Marriage.Confirmation.BothPlayersOnDivorce", oldYAML.getBoolean("Confirmation.BothPlayersOnDivorce", false));
			newConfig.set("Marriage.DisableSetPriestCommand", !oldYAML.getBoolean("PriestEnabled", true));
			newConfig.set("Kiss.Enable", oldYAML.getBoolean("Kiss.Enable", true));
			newConfig.set("Kiss.WaitTime", oldYAML.getInt("Kiss.WaitTime", 10));
			newConfig.set("Kiss.HearthCount", oldYAML.getInt("Kiss.HearthCount", 50));
			newConfig.set("Range.Kiss", oldYAML.getDouble("Range.Kiss", 2));
			newConfig.set("Range.Marry", oldYAML.getDouble("Range.Marry", 25));
			newConfig.set("Range.Divorce", oldYAML.getDouble("Range.Divorce", oldYAML.getDouble("Range.Marry", 25)));
			newConfig.set("Range.Backpack", oldYAML.getDouble("Range.Backpack", 5));
			newConfig.set("Range.KissInteract", oldYAML.getDouble("Range.KissInteract", 5));
			newConfig.set("Range.HearthVisible", oldYAML.getDouble("Range.HearthVisible", 128));
			newConfig.set("Range.Heal", oldYAML.getDouble("Range.Heal", 3));
			newConfig.set("Range.BonusXP", oldYAML.getDouble("Range.BonusXP", 10));
			newConfig.set("Range.Gift", oldYAML.getDouble("Range.Gift", 0));
			newConfig.set("Misc.AutoUpdate", oldYAML.getBoolean("Misc.AutoUpdate", true));
			newConfig.set("Teleport.Delay", oldYAML.getBoolean("Teleport.Delay", false));
			newConfig.set("Teleport.DelayTime", oldYAML.getInt("Teleport.DelayTime", 3));
			newConfig.set("Teleport.CheckSafety", oldYAML.getBoolean("Teleport.CheckSafety", true));
			newConfig.set("Teleport.BlacklistedWorlds", oldYAML.getStringList("Teleport.BlacklistedWorlds", new LinkedList<>()));
			newConfig.set("Gift.AllowInCreative", oldYAML.getBoolean("AllowGiftsInCreative", false));
			newConfig.set("BonusXp.Enable", oldYAML.getBoolean("BonusXp.Enable", true));
			newConfig.set("BonusXp.Multiplier", oldYAML.getInt("BonusXp.Multiplier", 2));
			newConfig.set("HealthRegain.Enable", oldYAML.getBoolean("HealthRegain.Enable", true));
			newConfig.set("HealthRegain.Multiplier", oldYAML.getInt("HealthRegain.Multiplier", 2));
			newConfig.set("Prefix.Enable", oldYAML.getBoolean("Prefix.Enable", false));
			newConfig.set("Prefix.OnLineBeginning", oldYAML.getBoolean("Prefix.OnLineBeginning", true));
			newConfig.set("Prefix.String", oldYAML.getString("Prefix.String", "<heart>{PartnerName}<heart>").replace("<partnername>", "{PartnerName}").replace("<statusheart>", "{StatusHeart}")
					.replace("<magicheart>", "{MagicHeart}"));
			newConfig.set("Suffix.Enable", oldYAML.getBoolean("Suffix.Enable", false));
			newConfig.set("Suffix.String", oldYAML.getString("Suffix.String", "{Surname}").replace("<partnername>", "{PartnerName}"));
			newConfig.set("UseBungeeCord", oldYAML.getBoolean("Misc.UseBungeeCord", false));
			newConfig.set("Economy.Enable", oldYAML.getBoolean("Economy.Enable", false));
			newConfig.set("Economy.Divorce", oldYAML.getDouble("Economy.Divorce", 100));
			newConfig.set("Economy.Marry", oldYAML.getDouble("Economy.Marry", 100));
			newConfig.set("Economy.Tp", oldYAML.getDouble("Economy.Tp", 25));
			newConfig.set("Economy.HomeTp", oldYAML.getDouble("Economy.HomeTp", 25));
			newConfig.set("Economy.SetHome", oldYAML.getDouble("Economy.SetHome", 100));
			newConfig.set("Economy.Gift", oldYAML.getDouble("Economy.Gift", 10));
			newConfig.set("BackpackShare.Enable", oldYAML.getBoolean("UseMinepacks", true));
		}
		catch(Exception e)
		{
			MarriageMaster.getInstance().getLogger().warning("There was a problem upgrading the old config file into the new config file.");
			e.printStackTrace();
		}
	}
}