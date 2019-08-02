/*
 *   Copyright (C) 2019 GeorgH93
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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Management;

import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.DivorceEvent;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.DivorcedEvent;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.MarriedEvent;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.MarryEvent;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Database.MarriageData;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Management.Requests.PriestDivorceAcceptRequest;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Management.Requests.PriestMarryAcceptRequest;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Management.Requests.SelfDivorceAcceptRequest;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Management.Requests.SelfMarryAcceptRequest;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

public class MarriageManager implements at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriageManager
{ // TODO refactor me!!!!
	private static final String CONSOLE_NAME = "Console", CONSOLE_DISPLAY_NAME = ChatColor.GRAY + CONSOLE_NAME, COLOR_CODE_REGEX = "&[a-fA-F0-9l-orL-OR]";

	private final MarriageMaster plugin;
	private final String  surnameNotAllowedCharactersRex, dialogDoYouWant, dialogMarried;
	private final Message messageSurnameSuccess, messageSurnameFailed, messageSurnameToShort, messageSurnameToLong, messageSurnameAlreadyUsed;
	private final Message messageAlreadyMarried, messageNotWithHimself, messageSurnameNeeded, messageMarried, messageHasMarried, messageBroadcastMarriage, messageNotInRange, messageAlreadyOpenRequest;
	private final Message messageNotYourself, messageSelfNotInRange, messageSelfAlreadyMarried, messageSelfOtherAlreadyMarried, messageSelfAlreadyOpenRequest, messageSelfConfirm, messageSelfMarryRequestSent;
	private final Message messageBroadcastDivorce, messageDivorced, messageDivorcedPlayer, messageDivorceNotInRange, messageSelfNotOnYourOwn;
	private final Message messageSelfDivorced, messageSelfBroadcastDivorce, messageSelfDivorcedPlayer, messageSelfDivorceRequestSent, messageSelfDivorceConfirm, messageSelfDivorceNotInRange;
	private final boolean surnameAllowColors, announceMarriage, confirm, bothOnDivorce, autoDialog, otherPlayerOnSelfDivorce;
	private final int surnameMinLength, surnameMaxLength;
	private final double rangeMarry, rangeDivorce, rangeMarrySquared, rangeDivorceSquared;

	public MarriageManager(MarriageMaster plugin)
	{
		this.plugin = plugin;

		surnameAllowColors = plugin.getConfiguration().getSurnamesAllowColors();
		if(plugin.getConfiguration().getSurnamesAllowedCharacters().equalsIgnoreCase("all"))
		{
			surnameNotAllowedCharactersRex = null;
		}
		else
		{
			surnameNotAllowedCharactersRex = "[^" + plugin.getConfiguration().getSurnamesAllowedCharacters() + "]";
		}
		surnameMinLength = plugin.getConfiguration().getSurnamesMinLength();
		surnameMaxLength = plugin.getConfiguration().getSurnamesMaxLength();

		rangeMarry       = plugin.getConfiguration().getRange("Marry");
		rangeDivorce     = plugin.getConfiguration().getRange("Divorce");
		announceMarriage = plugin.getConfiguration().isMarryAnnouncementEnabled();
		confirm          = plugin.getConfiguration().isMarryConfirmationEnabled();
		bothOnDivorce    = plugin.getConfiguration().isConfirmationBothPlayersOnDivorceEnabled();
		autoDialog       = plugin.getConfiguration().isMarryConfirmationAutoDialogEnabled();
		otherPlayerOnSelfDivorce = plugin.getConfiguration().isConfirmationOtherPlayerOnSelfDivorceEnabled();
		rangeMarrySquared   = rangeMarry * rangeMarry;
		rangeDivorceSquared = rangeDivorce * rangeDivorce;

		dialogDoYouWant     = plugin.getLanguage().getDialog("DoYouWant").replaceAll("\\{Player1Name}", "%1\\$s").replaceAll("\\{Player1DisplayName}", "%2\\$s").replaceAll("\\{Player2Name}", "%3\\$s").replaceAll("\\{Player2DisplayName}", "%4\\$s");
		dialogMarried       = plugin.getLanguage().getDialog("Married");

		messageSurnameSuccess          = getMSG("Ingame.Surname.SetSuccessful");
		messageSurnameFailed           = getMSG("Ingame.Surname.SetFailed");
		messageSurnameToShort          = getMSG("Ingame.Surname.ToShort").replaceAll("\\{MinLength\\}", surnameMinLength + "").replaceAll("\\{MaxLength\\}", surnameMaxLength + "");
		messageSurnameToLong           = getMSG("Ingame.Surname.ToLong").replaceAll("\\{MinLength\\}", surnameMinLength + "").replaceAll("\\{MaxLength\\}", surnameMaxLength + "");
		messageSurnameAlreadyUsed      = getMSG("Ingame.Surname.AlreadyUsed");

		messageAlreadyMarried          = getMSG("Ingame.Marry.AlreadyMarried").replaceAll("\\{Name\\}", "%1\\$s").replaceAll("\\{DisplayName\\}", "%2\\$s");
		messageNotWithHimself          = getMSG("Ingame.Marry.NotWithHimself").replaceAll("\\{Name\\}", "%1\\$s").replaceAll("\\{DisplayName\\}", "%2\\$s");
		messageSurnameNeeded           = getMSG("Ingame.Marry.SurnameNeeded");
		messageMarried                 = getMSG("Ingame.Marry.Married").replaceAll("\\{Player1Name\\}", "%1\\$s").replaceAll("\\{Player1DisplayName\\}", "%2\\$s").replaceAll("\\{Player2Name\\}", "%3\\$s").replaceAll("\\{Player2DisplayName\\}", "%4\\$s");
		messageHasMarried              = getMSG("Ingame.Marry.HasMarried").replaceAll("\\{PriestName\\}", "%1\\$s").replaceAll("\\{PriestDisplayName\\}", "%2\\$s").replaceAll("\\{PartnerName\\}", "%3\\$s").replaceAll("\\{PartnerDisplayName\\}", "%4\\$s");
		messageBroadcastMarriage       = getMSG("Ingame.Marry.Broadcast").replaceAll("\\{PriestName\\}", "%1\\$s").replaceAll("\\{PriestDisplayName\\}", "%2\\$s").replaceAll("\\{Player1Name\\}", "%3\\$s").replaceAll("\\{Player1DisplayName\\}", "%4\\$s").replaceAll("\\{Player2Name\\}", "%5\\$s").replaceAll("\\{Player2DisplayName\\}", "%6\\$s");
		messageNotInRange              = getMSG("Ingame.Marry.NotInRange").replaceAll("\\{Range\\}", "%.1f");
		messageAlreadyOpenRequest      = getMSG("Ingame.Marry.AlreadyOpenRequest").replaceAll("\\{Name\\}", "%1\\$s").replaceAll("\\{DisplayName\\}", "%2\\$s");

		messageSelfConfirm             = getMSG("Ingame.Marry.Self.Confirm").replaceAll("\\{Name\\}", "%1\\$s").replaceAll("\\{DisplayName\\}", "%2\\$s");
		messageNotYourself             = getMSG("Ingame.Marry.Self.NotYourself");
		messageSelfNotInRange          = getMSG("Ingame.Marry.Self.NotInRange").replaceAll("\\{Range\\}", "%.1f");
		messageSelfAlreadyMarried      = getMSG("Ingame.Marry.Self.AlreadyMarried").replaceAll("\\{Name\\}", "%1\\$s").replaceAll("\\{DisplayName\\}", "%2\\$s");
		messageSelfMarryRequestSent    = getMSG("Ingame.Marry.Self.RequestSent");
		messageSelfOtherAlreadyMarried = getMSG("Ingame.Marry.Self.OtherAlreadyMarried").replaceAll("\\{Name\\}", "%1\\$s").replaceAll("\\{DisplayName\\}", "%2\\$s");
		messageSelfAlreadyOpenRequest  = getMSG("Ingame.Marry.Self.AlreadyOpenRequest");
		messageSelfNotOnYourOwn        = getMSG("Ingame.Marry.Self.NotOnYourOwn");

		messageDivorced                = getMSG("Ingame.Divorce.Divorced").replaceAll("\\{Player1Name\\}", "%1\\$s").replaceAll("\\{Player1DisplayName\\}", "%2\\$s").replaceAll("\\{Player2Name\\}", "%3\\$s").replaceAll("\\{Player2DisplayName\\}", "%4\\$s");
		messageDivorcedPlayer          = getMSG("Ingame.Divorce.DivorcedPlayer").replaceAll("\\{PriestName\\}", "%1\\$s").replaceAll("\\{PriestDisplayName\\}", "%2\\$s").replaceAll("\\{PartnerName\\}", "%3\\$s").replaceAll("\\{PartnerDisplayName\\}", "%4\\$s");
		messageBroadcastDivorce        = getMSG("Ingame.Divorce.Broadcast").replaceAll("\\{PriestName\\}", "%1\\$s").replaceAll("\\{PriestDisplayName\\}", "%2\\$s").replaceAll("\\{Player1Name\\}", "%3\\$s").replaceAll("\\{Player1DisplayName\\}", "%4\\$s").replaceAll("\\{Player2Name\\}", "%5\\$s").replaceAll("\\{Player2DisplayName\\}", "%6\\$s");
		messageDivorceNotInRange       = getMSG("Ingame.Divorce.NotInRange").replaceAll("\\{Range\\}", "%.1f");

		messageSelfDivorced            = getMSG("Ingame.Divorce.Self.Divorced").replaceAll("\\{Name\\}", "%1\\$s").replaceAll("\\{DisplayName\\}", "%2\\$s");
		messageSelfDivorceConfirm      = getMSG("Ingame.Divorce.Self.Confirm").replaceAll("\\{Name\\}", "%1\\$s").replaceAll("\\{DisplayName\\}", "%2\\$s");
		messageSelfDivorcedPlayer      = getMSG("Ingame.Divorce.Self.DivorcedPlayer").replaceAll("\\{Name\\}", "%1\\$s").replaceAll("\\{DisplayName\\}", "%2\\$s");
		messageSelfBroadcastDivorce    = getMSG("Ingame.Divorce.Self.Broadcast").replaceAll("\\{Player1Name\\}", "%1\\$s").replaceAll("\\{Player1DisplayName\\}", "%2\\$s").replaceAll("\\{Player2Name\\}", "%3\\$s").replaceAll("\\{Player2DisplayName\\}", "%4\\$s");
		messageSelfDivorceRequestSent  = getMSG("Ingame.Divorce.Self.RequestSent").replaceAll("\\{Name\\}", "%1\\$s").replaceAll("\\{DisplayName\\}", "%2\\$s");
		messageSelfDivorceNotInRange   = getMSG("Ingame.Divorce.Self.NotInRange").replaceAll("\\{Range\\}", "%.1f");

		//region init messages of request classes
		SelfMarryAcceptRequest.loadMessages(plugin);
		PriestMarryAcceptRequest.loadMessages(plugin);
		SelfDivorceAcceptRequest.loadMessages(plugin);
		PriestDivorceAcceptRequest.loadMessages(plugin);
		//endregion
	}

	public void close()
	{
		SelfMarryAcceptRequest.unLoadMessages();
		PriestMarryAcceptRequest.unLoadMessages();
		SelfDivorceAcceptRequest.unLoadMessages();
		PriestDivorceAcceptRequest.unLoadMessages();
	}
	
	private Message getMSG(String path)
	{
		return plugin.getLanguage().getMessage(path);
	}

	//region settings getter
	public boolean isAutoDialogEnabled()
	{
		return autoDialog;
	}

	public boolean isConfirmEnabled()
	{
		return confirm;
	}

	public boolean isAnnounceMarriageEnabled()
	{
		return announceMarriage;
	}
	//endregion

	//region surname functions
	@Override
	public String cleanupSurname(String surname)
	{
		if(surname == null || surname.isEmpty()) return null;

		surname = surname.replace('§', '&').replaceAll("&k", "");
		if(surnameNotAllowedCharactersRex != null)
		{
			String s = surname.replaceAll(COLOR_CODE_REGEX, "");
			String surnameCleaned = s.replaceAll(surnameNotAllowedCharactersRex, "");
			if(!s.equals(surnameCleaned))
			{ // Surname contains not allowed chars
				surname = surnameCleaned; //TODO add back colors
			}
		}
		return (surnameAllowColors) ? ChatColor.translateAlternateColorCodes('&', surname) : surname;
	}

	@Override
	public boolean isSurnameAvailable(@NotNull String surname)
	{
		return !plugin.getDatabase().getSurnames().contains(surname);
	}

	@Override
	public boolean isSurnameValid(@NotNull String surname)
	{
		surname = cleanupSurname(surname);
		//noinspection ConstantConditions
		return isSurnameLengthValid(surname) && isSurnameAvailable(surname);
	}

	@Override
	public boolean isSurnameLengthValid(@NotNull String surname)
	{
		return surname.length() >= surnameMinLength && surname.length() <= surnameMaxLength;
	}

	@Override
	public void setSurname(@NotNull Marriage marriage, String surname)
	{
		setSurname(marriage, surname, Bukkit.getConsoleSender());
	}

	@Override
	public void setSurname(@NotNull Marriage marriage, String surname, @NotNull CommandSender changer)
	{
		surname = (!plugin.isSurnamesEnabled() && (surname.equalsIgnoreCase("null") || surname.equalsIgnoreCase("none") || surname.equalsIgnoreCase("remove"))) ? null : cleanupSurname(surname);
		if(surname == null && plugin.isSurnamesForced())
		{
			messageSurnameFailed.send(changer);
		}
		else if(surname == null || surname.length() >= surnameMinLength)
		{
			if(surname == null || surname.length() <= surnameMaxLength)
			{
				if(surname == null || isSurnameAvailable(surname))
				{
					((marriage.setSurname(surname)) ? messageSurnameSuccess : messageSurnameFailed).send(changer);
				}
				else
				{
					messageSurnameAlreadyUsed.send(changer);
				}
			}
			else
			{
				messageSurnameToLong.send(changer);
			}
		}
		else
		{
			messageSurnameToShort.send(changer);
		}
	}
	//endregion

	//region marry functions
	//region not fully parameterized marry functions
	//region Bukkit-Player marry functions - just convert the Bukkit-Player into a MarriagePlayer and send it to the next function
	@Override
	public void marry(@NotNull Player player1, @NotNull Player player2)
	{
		marry(plugin.getPlayerData(player1), plugin.getPlayerData(player2), (String) null);
	}

	@Override
	public void marry(@NotNull Player player1, @NotNull Player player2, @NotNull CommandSender priest)
	{
		marry(plugin.getPlayerData(player1), plugin.getPlayerData(player2), priest, null);
	}

	@Override
	public void marry(@NotNull Player player1, @NotNull Player player2, String surname)
	{
		marry(plugin.getPlayerData(player1), plugin.getPlayerData(player2), surname);
	}

	@Override
	public void marry(@NotNull Player player1, @NotNull Player player2, @NotNull CommandSender priest, String surname)
	{
		marry(plugin.getPlayerData(player1), plugin.getPlayerData(player2), priest, surname);
	}
	//endregion

	@Override
	public void marry(@NotNull MarriagePlayer player1, @NotNull MarriagePlayer player2)
	{
		marry(player1, player2, (String) null);
	}

	@Override
	public void marry(@NotNull MarriagePlayer player1, @NotNull MarriagePlayer player2, @NotNull CommandSender priest)
	{
		marry(player1, player2, priest, null);
	}

	@Override
	public void marry(@NotNull MarriagePlayer player1, @NotNull MarriagePlayer player2, @NotNull MarriagePlayer priest)
	{
		marry(player1, player2, priest, null);
	}

	@Override
	public void marry(@NotNull MarriagePlayer player1, @NotNull MarriagePlayer player2, String surname)
	{
		marry(player1, player2, player1, surname);
	}
	//endregion

	//region marry helper function
	private boolean marrySurnameTest(CommandSender priest, String surname)
	{
		if(plugin.isSurnamesEnabled() && surname != null && !surname.isEmpty())
		{
			if(surname.length() >= surnameMinLength)
			{
				if(surname.length() <= surnameMaxLength)
				{
					if(!isSurnameAvailable(surname))
					{
						messageSurnameAlreadyUsed.send(priest);
						return false;
					}
				}
				else
				{
					messageSurnameToLong.send(priest);
					return false;
				}
			}
			else
			{
				messageSurnameToShort.send(priest);
				return false;
			}
		}
		else if(plugin.isSurnamesForced() && (surname == null || surname.isEmpty()))
		{
			messageSurnameNeeded.send(priest);
			return false;
		}
		return true;
	}

	private boolean marryOnlineTest(CommandSender priest, OfflinePlayer player1, OfflinePlayer player2)
	{
		if(!player1.isOnline())
		{
			plugin.messagePlayerNotOnline.send(priest, player1.getName());
		}
		else if(!player2.isOnline())
		{
			plugin.messagePlayerNotOnline.send(priest, player2.getName());
		}
		else
		{
			return true;
		}
		return false;
	}

	private boolean marryPriestTestCanMarry(MarriagePlayer player1, MarriagePlayer player2, CommandSender priest)
	{
		if(player1.equals(player2))
		{
			messageNotWithHimself.send(priest, player1.getName(), player1.getDisplayName());
		}
		else if(!plugin.areMultiplePartnersAllowed() && (player1.isMarried() || player2.isMarried()))
		{
			if(player1.isMarried()) messageAlreadyMarried.send(priest, player1.getName(), player1.getDisplayName());
			if(player2.isMarried()) messageAlreadyMarried.send(priest, player2.getName(), player2.getDisplayName());
		}
		else
		{
			return true;
		}
		return false;
	}

	public void marryPriestFinish(MarriagePlayer player1, MarriagePlayer player2, CommandSender priest, String surname)
	{
		surname = cleanupSurname(surname);
		MarryEvent event = new MarryEvent(player1, player2, priest, surname);
		plugin.getServer().getPluginManager().callEvent(event);
		if(!event.isCancelled())
		{
			String priestName, priestDPName;
			MarriageData marriage;
			if(priest instanceof Player)
			{
				priestName = priest.getName();
				priestDPName = ((Player) priest).getDisplayName();
				marriage = new MarriageData(player1, player2, plugin.getPlayerData((Player) priest), new Date(), surname);
				if(confirm && autoDialog)
				{
					((Player) priest).chat(dialogMarried);
				}
			}
			else
			{
				priestName = CONSOLE_NAME;
				priestDPName = CONSOLE_DISPLAY_NAME;
				marriage = new MarriageData(player1, player2, null, surname);
			}

			plugin.getDatabase().cachedMarry(marriage);
			messageMarried.send(priest, player1.getName(), player1.getDisplayName(), player2.getName(), player2.getDisplayName());
			player1.sendMessage(messageHasMarried, priestName, priestDPName, player2.getName(), player2.getDisplayName());
			player2.sendMessage(messageHasMarried, priestName, priestDPName, player1.getName(), player1.getDisplayName());
			if(announceMarriage)
			{
				messageBroadcastMarriage.broadcast(priestName, priestDPName, player1.getName(), player1.getDisplayName(), player2.getName(), player2.getDisplayName());
			}
			plugin.getServer().getPluginManager().callEvent(new MarriedEvent(marriage));
		}
	}
	//endregion

	@Override
	public void marry(@NotNull MarriagePlayer player1, @NotNull MarriagePlayer player2, @NotNull CommandSender priest, String surname) // Console priest
	{
		if(priest instanceof Player)
		{
			marry(player1, player2, plugin.getPlayerData((Player) priest), surname);
		}
		else if(marryOnlineTest(priest, player1.getPlayer(), player2.getPlayer()) && marryPriestTestCanMarry(player1, player2, priest) && marrySurnameTest(priest, surname))
		{
			marryPriestFinish(player1, player2, priest, surname);
		}
	}

	@Override
	public void marry(@NotNull MarriagePlayer player1, @NotNull MarriagePlayer player2, @NotNull MarriagePlayer priest, String surname)
	{
		if(priest.isOnline() && marryOnlineTest(priest.getPlayerOnline(), player1.getPlayer(), player2.getPlayer()) && marrySurnameTest(priest.getPlayerOnline(), surname))
		{
			if(player1.equals(priest) || player2.equals(priest)) // Self marry
			{
				if(plugin.isSelfMarriageAllowed() && priest.hasPermission("marry.selfmarry"))
				{
					MarriagePlayer otherPlayer = (player1.equals(priest)) ? player2 : player1;
					if(player1.equals(player2))
					{
						priest.send(messageNotYourself);
					}
					else if(!plugin.isInRangeSquared(player1, player2, rangeMarrySquared))
					{
						priest.send(messageSelfNotInRange, rangeMarry);
					}
					else if(!plugin.areMultiplePartnersAllowed() && (player1.isMarried() || player2.isMarried()))
					{
						if(priest.isMarried())
						{
							priest.send(messageSelfAlreadyMarried, priest.getPartner().getName(), priest.getPartner().getDisplayName());
						}
						if(otherPlayer.isMarried())
						{
							priest.send(messageSelfOtherAlreadyMarried, otherPlayer.getName(), otherPlayer.getDisplayName());
						}
					}
					else if(priest.getOpenRequest() != null || priest.getRequestsToCancel().size() > 0)
					{
						priest.send(messageSelfAlreadyOpenRequest);
					}
					else if(otherPlayer.getOpenRequest() != null || otherPlayer.getRequestsToCancel().size() > 0)
					{
						priest.send(messageAlreadyOpenRequest, otherPlayer.getName(), otherPlayer.getDisplayName());
					}
					else
					{
						otherPlayer.send(messageSelfConfirm, priest.getName(), priest.getDisplayName());
						priest.send(messageSelfMarryRequestSent);
						plugin.getCommandManager().registerAcceptPendingRequest(new SelfMarryAcceptRequest(this, otherPlayer, priest, surname));
					}
				}
				else
				{
					priest.send(messageSelfNotOnYourOwn);
				}
			}
			else // Player priest
			{
				if(priest.isPriest())
				{
					Player bPriest = priest.getPlayerOnline();
					if(marryPriestTestCanMarry(player1, player2, bPriest))
					{
						if(priest.hasPermission("marry.bypass.rangelimit") || (plugin.isInRangeSquared(player1, player2, rangeMarrySquared) && plugin.isInRangeSquared(priest, player1, rangeMarrySquared) &&
								plugin.isInRangeSquared(priest, player2, rangeMarrySquared)))
						{
							if(!confirm)
							{
								marryPriestFinish(player1, player2, bPriest, surname);
							}
							else
							{
								if(player1.getOpenRequest() != null || player1.getRequestsToCancel().size() > 0)
								{
									priest.send(messageAlreadyOpenRequest, player1.getName(), player1.getDisplayName());
								}
								else if(player2.getOpenRequest() != null || player2.getRequestsToCancel().size() > 0)
								{
									priest.send(messageAlreadyOpenRequest, player2.getName(), player2.getDisplayName());
								}
								else
								{
									if(autoDialog)
									{
										bPriest.chat(String.format(dialogDoYouWant, player1.getName(), player1.getDisplayName(), player2.getName(), player2.getDisplayName()));
									}
									plugin.getCommandManager().registerAcceptPendingRequest(new PriestMarryAcceptRequest(this, player1, player2, priest, surname, true));
								}
							}
						}
						else
						{
							priest.send(messageNotInRange, rangeMarry);
						}
					}
				}
				else
				{
					priest.send(plugin.messageNoPermission);
				}
			}
		}
	}
	//endregion

	//region divorce functions
	public void priestFinishDivorce(@NotNull Marriage marriage, @NotNull CommandSender divorceBy)
	{
		DivorceEvent divorceEvent = new DivorceEvent(marriage, divorceBy);
		Bukkit.getPluginManager().callEvent(divorceEvent);
		if(!divorceEvent.isCancelled())
		{
			marriage.divorce();
			MarriagePlayer player1 = marriage.getPartner1(), player2 = marriage.getPartner2();
			String priestName, priestDPName, player1DisplayName = player1.getDisplayName(), player2DisplayName = player2.getDisplayName();
			if(divorceBy instanceof Player)
			{
				priestName = divorceBy.getName();
				priestDPName = ((Player) divorceBy).getDisplayName();
			}
			else
			{
				priestName = CONSOLE_NAME;
				priestDPName = CONSOLE_DISPLAY_NAME;
			}
			if(announceMarriage)
			{
				messageBroadcastDivorce.broadcast(priestName, priestDPName, player1.getName(), player1DisplayName, player2.getName(), player2DisplayName);
			}
			messageDivorced.send(divorceBy, player1.getName(), player1DisplayName, player2.getName(), player2DisplayName);
			if(player1.isOnline()) player1.send(messageDivorcedPlayer, priestName, priestDPName, player2.getName(), player2DisplayName);
			if(player2.isOnline()) player2.send(messageDivorcedPlayer, priestName, priestDPName, player1.getName(), player1DisplayName);
			plugin.getServer().getPluginManager().callEvent(new DivorcedEvent(marriage.getPartner1(), marriage.getPartner2(), divorceBy));
		}
	}

	public void selfFinishDivorce(@NotNull Marriage marriage, @NotNull MarriagePlayer divorceBy)
	{
		DivorceEvent divorceEvent = new DivorceEvent(marriage, divorceBy.getPlayerOnline());
		Bukkit.getPluginManager().callEvent(divorceEvent);
		if(!divorceEvent.isCancelled())
		{
			MarriagePlayer otherPlayer = marriage.getPartner(divorceBy);
			//noinspection ConstantConditions
			String otherName = otherPlayer.getName(), otherDisplayName = otherPlayer.getDisplayName();
			marriage.divorce();
			divorceBy.send(messageSelfDivorced, otherName, otherDisplayName);
			if(otherPlayer.isOnline())
			{
				otherPlayer.send(messageSelfDivorcedPlayer, divorceBy.getName(), divorceBy.getDisplayName());
			}
			if(announceMarriage)
			{
				messageSelfBroadcastDivorce.broadcast(divorceBy.getName(), divorceBy.getDisplayName(), otherName, otherDisplayName);
			}
			plugin.getServer().getPluginManager().callEvent(new DivorcedEvent(marriage.getPartner1(), marriage.getPartner2()));
		}
	}

	@Override
	public void divorce(@NotNull Marriage marriage) // Divorce by console
	{
		divorce(marriage, Bukkit.getConsoleSender());
	}

	@Override
	public void divorce(@NotNull Marriage marriage, @NotNull CommandSender divorceBy) // Divorce by console
	{
		if(divorceBy instanceof Player)
		{
			divorce(marriage, plugin.getPlayerData((Player) divorceBy));
		}
		else
		{
			priestFinishDivorce(marriage, divorceBy);
		}
	}

	@Override
	public void divorce(@NotNull Marriage marriage, @NotNull MarriagePlayer divorceBy) // Self divorce
	{
		if(!divorceBy.isOnline()) return;
		if(marriage.hasPlayer(divorceBy)) // Self divorce
		{
			if(plugin.isSelfDivorceAllowed() && divorceBy.hasPermission("marry.selfdivorce"))
			{
				MarriagePlayer otherPlayer = marriage.getPartner(divorceBy);
				//noinspection ConstantConditions
				if(!otherPlayer.isOnline())
				{
					// Self divorce with offline partner
					if(divorceBy.hasPermission("marry.offlinedivorce"))
					{
						selfFinishDivorce(marriage, divorceBy);
					}
					else
					{
						divorceBy.send(plugin.messagePartnerOffline);
					}
				}
				else
				{
					if(plugin.isInRangeSquared(divorceBy, otherPlayer, rangeDivorceSquared))
					{
						if(otherPlayerOnSelfDivorce)
						{
							if(marriage.getPartner1().getOpenRequest() != null || marriage.getPartner2().getOpenRequest() != null)
							{
								if(marriage.getPartner1().getOpenRequest() != null)
								{
									divorceBy.send(messageAlreadyOpenRequest, marriage.getPartner1().getName(), marriage.getPartner1().getDisplayName());
								}
								if(marriage.getPartner2().getOpenRequest() != null)
								{
									divorceBy.send(messageAlreadyOpenRequest, marriage.getPartner2().getName(), marriage.getPartner2().getDisplayName());
								}
							}
							else
							{
								otherPlayer.send(messageSelfDivorceConfirm, divorceBy.getName(), divorceBy.getDisplayName());
								divorceBy.send(messageSelfDivorceRequestSent, otherPlayer.getName(), otherPlayer.getDisplayName());
								plugin.getCommandManager().registerAcceptPendingRequest(new SelfDivorceAcceptRequest(this, otherPlayer, divorceBy, marriage));
							}
						}
						else
						{
							selfFinishDivorce(marriage, divorceBy);
						}
					}
					else
					{
						divorceBy.send(messageSelfDivorceNotInRange, rangeDivorce);
					}
				}
			}
			else
			{
				divorceBy.send(plugin.messageNoPermission);
			}
		}
		else // Divorce by player priest
		{
			divorce(marriage, divorceBy, marriage.getPartner1());
		}
	}

	@Override
	public void divorce(@NotNull Marriage marriage, @NotNull MarriagePlayer divorceBy, @NotNull MarriagePlayer first) // Divorced by priest
	{
		if(!divorceBy.isOnline() || !marriage.hasPlayer(first)) return;
		if(marriage.hasPlayer(divorceBy)) // Self divorce
		{
			divorce(marriage, divorceBy);
		}
		else // Divorce by player priest
		{
			if(divorceBy.isPriest())
			{
				if(marriage.isBothOnline())
				{
					if(divorceBy.hasPermission("marry.bypass.rangelimit") || (plugin.isInRange(divorceBy, marriage.getPartner1(), rangeDivorce) && plugin.isInRange(divorceBy, marriage.getPartner2(), rangeDivorce)))
					{
						if(confirm)
						{
							if(marriage.getPartner1().getOpenRequest() != null || marriage.getPartner2().getOpenRequest() != null)
							{
								if(marriage.getPartner1().getOpenRequest() != null)
								{
									divorceBy.send(messageAlreadyOpenRequest, marriage.getPartner1().getName(), marriage.getPartner1().getDisplayName());
								}
								if(marriage.getPartner2().getOpenRequest() != null)
								{
									divorceBy.send(messageAlreadyOpenRequest, marriage.getPartner2().getName(), marriage.getPartner2().getDisplayName());
								}
							}
							else
							{
								plugin.getCommandManager().registerAcceptPendingRequest(new PriestDivorceAcceptRequest(this, first, divorceBy, marriage, bothOnDivorce));
							}
						}
						else
						{
							priestFinishDivorce(marriage, divorceBy.getPlayerOnline());
						}
					}
					else
					{
						divorceBy.send(messageDivorceNotInRange, rangeDivorce);
					}
				}
				else if(divorceBy.hasPermission("marry.offlinedivorce"))
				{
					if(!marriage.getPartner1().isOnline() && !marriage.getPartner2().isOnline()) // Both are offline
					{
						if(confirm) // We can't confirm if both are offline!
						{
							// We don't have to test but I don't want to copy the message sending over
							marryOnlineTest(divorceBy.getPlayerOnline(), marriage.getPartner1().getPlayerOnline(), marriage.getPartner2().getPlayerOnline());
						}
						else
						{
							priestFinishDivorce(marriage, divorceBy.getPlayerOnline());
						}
					}
					else
					{
						if(first.isOnline())
						{
							if(confirm)
							{
								if(marriage.getPartner1().getOpenRequest() != null || marriage.getPartner2().getOpenRequest() != null)
								{
									if(marriage.getPartner1().getOpenRequest() != null)
									{
										divorceBy.send(messageAlreadyOpenRequest, marriage.getPartner1().getName(), marriage.getPartner1().getDisplayName());
									}
									if(marriage.getPartner2().getOpenRequest() != null)
									{
										divorceBy.send(messageAlreadyOpenRequest, marriage.getPartner2().getName(), marriage.getPartner2().getDisplayName());
									}
								}
								else
								{
									// If only one of the two is online he has to confirm. We ignore the bothOnDivorce setting for this case!
									plugin.getCommandManager().registerAcceptPendingRequest(new PriestDivorceAcceptRequest(this, first, divorceBy, marriage, false));
								}
							}
							else
							{
								priestFinishDivorce(marriage, divorceBy.getPlayerOnline());
							}
						}
						else
						{
							//noinspection ConstantConditions
							divorceBy.send(plugin.messagePlayerNotOnline, marriage.getPartner(first).getName());
						}
					}
				}
				else
				{
					// We don't have to test but I don't want to copy the message sending over
					marryOnlineTest(divorceBy.getPlayerOnline(), marriage.getPartner1().getPlayerOnline(), marriage.getPartner2().getPlayerOnline());
				}
			}
			else
			{
				divorceBy.send(plugin.messageNoPermission);
			}
		}
	}
	//endregion
}