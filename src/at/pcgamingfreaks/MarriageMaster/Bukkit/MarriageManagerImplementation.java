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

package at.pcgamingfreaks.MarriageMaster.Bukkit;

import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.AcceptPendingRequest;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.DivorceEvent;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.DivorcedEvent;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.MarriedEvent;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.MarryEvent;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Database.MarriageData;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

public class MarriageManagerImplementation implements at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriageManager
{
	private static final String CONSOLE_NAME = "Console", CONSOLE_DISPLAY_NAME = ChatColor.GRAY + CONSOLE_NAME;

	private final MarriageMaster plugin;
	private final String  surnameNotAllowedCharactersRex, dialogDoYouWant, dialogAndDoYouWant, dialogMarried, dialogYesIWant, dialogNoIDontWant;
	private final Message messageSurnameSuccess, messageSurnameFailed, messageSurnameToShort, messageSurnameToLong, messageSurnameAlreadyUsed;
	private final Message messageAlreadyMarried, messageNotWithHimself, messageSurnameNeeded, messageMarried, messageHasMarried, messageBroadcastMarriage, messageNotInRange, messageAlreadyOpenRequest,
							messageYouCalledOff, messagePlayerCalledOff, messageConfirm, messagePriestMarryOff, messagePlayerMarryOff;
	private final Message messageNotYourself, messageSelfNotInRange, messageSelfAlreadyMarried, messageSelfOtherAlreadyMarried, messageSelfAlreadyOpenRequest, messageSelfBroadcastMarriage, messageSelfMarried,
							messageSelfPlayerCalledOff, messageSelfYouCalledOff, messageSelfConfirm, messageSelfMarryRequestSent, messageSelfPlayerMarryOff;
	private final Message messageBroadcastDivorce, messageDivorced, messageDivorcedPlayer, messageDivorcePlayerOff, messageDivorcePriestOff, messageDivorceDeny, messageDivorceYouDeny, messageDivorceConfirm,
							messageDivorceNotInRange, messageDivorcePlayerCancelled, messageDivorcePriestCancelled, messageDivorceYouCancelled, messageDivorceYouCancelledPriest;
	private final Message messageSelfDivorcePlayerOff, messageSelfDivorced, messageSelfBroadcastDivorce, messageSelfDivorcedPlayer, messageSelfDivorceRequestSent, messageSelfDivorceConfirm, messageSelfDivorceDeny,
							messageSelfDivorceYouDeny, messageSelfDivorceCancelled, messageSelfDivorceYouCancelled, messageSelfDivorceNotInRange;
	private final boolean surnameAllowColors, announceMarriage, confirm, bothOnDivorce, autoDialog, otherPlayerOnSelfDivorce;
	private final int surnameMinLength, surnameMaxLength;
	private final double rangeMarry, rangeDivorce, rangeMarrySquared, rangeDivorceSquared;

	public MarriageManagerImplementation(MarriageMaster plugin)
	{
		this.plugin = plugin;

		surnameAllowColors = plugin.getConfiguration().getSurnamesAllowColors();
		if(plugin.getConfiguration().getSurnamesAllowedCharacters().equalsIgnoreCase("all"))
		{
			surnameNotAllowedCharactersRex = null;
		}
		else
		{
			surnameNotAllowedCharactersRex = "[^" + plugin.getConfiguration().getSurnamesAllowedCharacters() + ((surnameAllowColors) ? "&§" : "") + "]";
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
		dialogAndDoYouWant  = plugin.getLanguage().getDialog("AndDoYouWant").replaceAll("\\{Player1Name}", "%1\\$s").replaceAll("\\{Player1DisplayName}", "%2\\$s").replaceAll("\\{Player2Name}", "%3\\$s").replaceAll("\\{Player2DisplayName}", "%4\\$s");
		dialogMarried       = plugin.getLanguage().getDialog("Married");
		dialogYesIWant      = plugin.getLanguage().getDialog("YesIWant");
		dialogNoIDontWant   = plugin.getLanguage().getDialog("NoIDontWant");

		messageSurnameSuccess          = getMSG("Ingame.Surname.SetSuccessful");
		messageSurnameFailed           = getMSG("Ingame.Surname.Failed");
		messageSurnameToShort          = getMSG("Ingame.Surname.ToShort").replaceAll("\\{MinLength\\}", surnameMinLength + "").replaceAll("\\{MaxLength\\}", surnameMaxLength + "");
		messageSurnameToLong           = getMSG("Ingame.Surname.ToLong").replaceAll("\\{MinLength\\}", surnameMinLength + "").replaceAll("\\{MaxLength\\}", surnameMaxLength + "");
		messageSurnameAlreadyUsed      = getMSG("Ingame.Surname.AlreadyUsed");

		messageConfirm                 = getMSG("Ingame.Marry.Confirm").replaceAll("\\{PriestName\\}", "%1\\$s").replaceAll("\\{PriestDisplayName\\}", "%2\\$s").replaceAll("\\{PlayerName\\}", "%3\\$s").replaceAll("\\{PlayerDisplayName\\}", "%4\\$s");
		messageAlreadyMarried          = getMSG("Ingame.Marry.AlreadyMarried").replaceAll("\\{Name\\}", "%1\\$s").replaceAll("\\{DisplayName\\}", "%2\\$s");
		messageNotWithHimself          = getMSG("Ingame.Marry.NotWithHimself").replaceAll("\\{Name\\}", "%1\\$s").replaceAll("\\{DisplayName\\}", "%2\\$s");
		messageSurnameNeeded           = getMSG("Ingame.Marry.SurnameNeeded");
		messageMarried                 = getMSG("Ingame.Marry.Married").replaceAll("\\{Player1Name\\}", "%1\\$s").replaceAll("\\{Player1DisplayName\\}", "%2\\$s").replaceAll("\\{Player2Name\\}", "%3\\$s").replaceAll("\\{Player2DisplayName\\}", "%4\\$s");
		messageHasMarried              = getMSG("Ingame.Marry.HasMarried").replaceAll("\\{PriestName\\}", "%1\\$s").replaceAll("\\{PriestDisplayName\\}", "%2\\$s").replaceAll("\\{PartnerName\\}", "%3\\$s").replaceAll("\\{PartnerDisplayName\\}", "%4\\$s");
		messageBroadcastMarriage       = getMSG("Ingame.Marry.Broadcast").replaceAll("\\{PriestName\\}", "%1\\$s").replaceAll("\\{PriestDisplayName\\}", "%2\\$s").replaceAll("\\{Player1Name\\}", "%3\\$s").replaceAll("\\{Player1DisplayName\\}", "%4\\$s").replaceAll("\\{Player2Name\\}", "%5\\$s").replaceAll("\\{Player2DisplayName\\}", "%6\\$s");
		messageNotInRange              = getMSG("Ingame.Marry.NotInRange").replaceAll("\\{Range\\}", "%.1f");
		messageAlreadyOpenRequest      = getMSG("Ingame.Marry.AlreadyOpenRequest").replaceAll("\\{Name\\}", "%1\\$s").replaceAll("\\{DisplayName\\}", "%2\\$s");
		messageYouCalledOff            = getMSG("Ingame.Marry.YouCalledOff");
		messagePlayerCalledOff         = getMSG("Ingame.Marry.PlayerCalledOff").replaceAll("\\{Name\\}", "%1\\$s").replaceAll("\\{DisplayName\\}", "%2\\$s");
		messagePriestMarryOff          = getMSG("Ingame.Marry.PriestOff").replaceAll("\\{Name\\}", "%1\\$s").replaceAll("\\{DisplayName\\}", "%2\\$s");
		messagePlayerMarryOff          = getMSG("Ingame.Marry.PlayerOff").replaceAll("\\{Name\\}", "%1\\$s").replaceAll("\\{DisplayName\\}", "%2\\$s");

		messageSelfConfirm             = getMSG("Ingame.Marry.Self.Confirm").replaceAll("\\{Name\\}", "%1\\$s").replaceAll("\\{DisplayName\\}", "%2\\$s");
		messageNotYourself             = getMSG("Ingame.Marry.Self.NotYourself");
		messageSelfNotInRange          = getMSG("Ingame.Marry.Self.NotInRange").replaceAll("\\{Range\\}", "%.1f");
		messageSelfMarried             = getMSG("Ingame.Marry.Self.Married").replaceAll("\\{Name\\}", "%1\\$s").replaceAll("\\{DisplayName\\}", "%2\\$s");
		messageSelfAlreadyMarried      = getMSG("Ingame.Marry.Self.AlreadyMarried").replaceAll("\\{Name\\}", "%1\\$s").replaceAll("\\{DisplayName\\}", "%2\\$s");
		messageSelfYouCalledOff        = getMSG("Ingame.Marry.Self.YouCalledOff");
		messageSelfPlayerMarryOff      = getMSG("Ingame.Marry.Self.PlayerOff").replaceAll("\\{Name\\}", "%1\\$s").replaceAll("\\{DisplayName\\}", "%2\\$s");
		messageSelfMarryRequestSent    = getMSG("Ingame.Marry.Self.RequestSent");
		messageSelfPlayerCalledOff     = getMSG("Ingame.Marry.Self.PlayerCalledOff").replaceAll("\\{Name\\}", "%1\\$s").replaceAll("\\{DisplayName\\}", "%2\\$s");
		messageSelfOtherAlreadyMarried = getMSG("Ingame.Marry.Self.OtherAlreadyMarried").replaceAll("\\{Name\\}", "%1\\$s").replaceAll("\\{DisplayName\\}", "%2\\$s");
		messageSelfAlreadyOpenRequest  = getMSG("Ingame.Marry.Self.AlreadyOpenRequest");
		messageSelfBroadcastMarriage   = getMSG("Ingame.Marry.Self.Broadcast").replaceAll("\\{Player1Name\\}", "%1\\$s").replaceAll("\\{Player1DisplayName\\}", "%2\\$s").replaceAll("\\{Player2Name\\}", "%3\\$s").replaceAll("\\{Player2DisplayName\\}", "%4\\$s");

		messageDivorceDeny             = getMSG("Ingame.Divorce.Deny").replaceAll("\\{Name\\}", "%1\\$s").replaceAll("\\{DisplayName\\}", "%2\\$s");
		messageDivorceYouDeny          = getMSG("Ingame.Divorce.YouDeny").replaceAll("\\{Name\\}", "%1\\$s").replaceAll("\\{DisplayName\\}", "%2\\$s");
		messageDivorceConfirm          = getMSG("Ingame.Divorce.Confirm").replaceAll("\\{PriestName\\}", "%1\\$s").replaceAll("\\{PriestDisplayName\\}", "%2\\$s").replaceAll("\\{PartnerName\\}", "%3\\$s").replaceAll("\\{PartnerDisplayName\\}", "%4\\$s");
		messageDivorcePlayerOff        = getMSG("Ingame.Divorce.PlayerOff").replaceAll("\\{Name\\}", "%1\\$s").replaceAll("\\{DisplayName\\}", "%2\\$s");
		messageDivorcePriestOff        = getMSG("Ingame.Divorce.PriestOff").replaceAll("\\{Name\\}", "%1\\$s").replaceAll("\\{DisplayName\\}", "%2\\$s");
		messageDivorced                = getMSG("Ingame.Divorce.Divorced").replaceAll("\\{Player1Name\\}", "%1\\$s").replaceAll("\\{Player1DisplayName\\}", "%2\\$s").replaceAll("\\{Player2Name\\}", "%3\\$s").replaceAll("\\{Player2DisplayName\\}", "%4\\$s");
		messageDivorcedPlayer          = getMSG("Ingame.Divorce.DivorcedPlayer").replaceAll("\\{PriestName\\}", "%1\\$s").replaceAll("\\{PriestDisplayName\\}", "%2\\$s").replaceAll("\\{PartnerName\\}", "%3\\$s").replaceAll("\\{PartnerDisplayName\\}", "%4\\$s");
		messageBroadcastDivorce        = getMSG("Ingame.Divorce.Broadcast").replaceAll("\\{PriestName\\}", "%1\\$s").replaceAll("\\{PriestDisplayName\\}", "%2\\$s").replaceAll("\\{Player1Name\\}", "%3\\$s").replaceAll("\\{Player1DisplayName\\}", "%4\\$s").replaceAll("\\{Player2Name\\}", "%5\\$s").replaceAll("\\{Player2DisplayName\\}", "%6\\$s");
		messageDivorceNotInRange       = getMSG("Ingame.Divorce.NotInRange").replaceAll("\\{Range\\}", "%.1f");
		messageDivorcePriestCancelled  = getMSG("Ingame.Divorce.PriestCancelled").replaceAll("\\{PriestName\\}", "%1\\$s").replaceAll("\\{PriestDisplayName\\}", "%2\\$s").replaceAll("\\{PartnerName\\}", "%3\\$s").replaceAll("\\{PartnerDisplayName\\}", "%4\\$s");
		messageDivorcePlayerCancelled  = getMSG("Ingame.Divorce.PlayerCancelled").replaceAll("\\{PlayerName\\}", "%1\\$s").replaceAll("\\{PlayerDisplayName\\}", "%2\\$s").replaceAll("\\{PartnerName\\}", "%3\\$s").replaceAll("\\{PartnerDisplayName\\}", "%4\\$s");
		messageDivorceYouCancelled     = getMSG("Ingame.Divorce.YouCancelled").replaceAll("\\{Name\\}", "%1\\$s").replaceAll("\\{DisplayName\\}", "%2\\$s");
		messageDivorceYouCancelledPriest = getMSG("Ingame.Divorce.YouCancelledPriest").replaceAll("\\{Player1Name\\}", "%1\\$s").replaceAll("\\{Player1DisplayName\\}", "%2\\$s").replaceAll("\\{Player2Name\\}", "%3\\$s").replaceAll("\\{Player2DisplayName\\}", "%4\\$s");

		messageSelfDivorced            = getMSG("Ingame.Divorce.Self.Divorced").replaceAll("\\{Name\\}", "%1\\$s").replaceAll("\\{DisplayName\\}", "%2\\$s");
		messageSelfDivorceDeny         = getMSG("Ingame.Divorce.Self.Deny").replaceAll("\\{Name\\}", "%1\\$s").replaceAll("\\{DisplayName\\}", "%2\\$s");
		messageSelfDivorceYouDeny      = getMSG("Ingame.Divorce.Self.YouDeny").replaceAll("\\{Name\\}", "%1\\$s").replaceAll("\\{DisplayName\\}", "%2\\$s");
		messageSelfDivorceCancelled    = getMSG("Ingame.Divorce.Self.Cancelled").replaceAll("\\{Name\\}", "%1\\$s").replaceAll("\\{DisplayName\\}", "%2\\$s");
		messageSelfDivorceYouCancelled = getMSG("Ingame.Divorce.Self.YouCancelled").replaceAll("\\{Name\\}", "%1\\$s").replaceAll("\\{DisplayName\\}", "%2\\$s");
		messageSelfDivorceConfirm      = getMSG("Ingame.Divorce.Self.Confirm").replaceAll("\\{Name\\}", "%1\\$s").replaceAll("\\{DisplayName\\}", "%2\\$s");
		messageSelfDivorcedPlayer      = getMSG("Ingame.Divorce.Self.DivorcedPlayer").replaceAll("\\{Name\\}", "%1\\$s").replaceAll("\\{DisplayName\\}", "%2\\$s");
		messageSelfBroadcastDivorce    = getMSG("Ingame.Divorce.Self.Broadcast").replaceAll("\\{Player1Name\\}", "%1\\$s").replaceAll("\\{Player1DisplayName\\}", "%2\\$s").replaceAll("\\{Player2Name\\}", "%3\\$s").replaceAll("\\{Player2DisplayName\\}", "%4\\$s");
		messageSelfDivorcePlayerOff    = getMSG("Ingame.Divorce.Self.PlayerOff").replaceAll("\\{Name\\}", "%1\\$s").replaceAll("\\{DisplayName\\}", "%2\\$s");
		messageSelfDivorceRequestSent  = getMSG("Ingame.Divorce.Self.RequestSent").replaceAll("\\{Name\\}", "%1\\$s").replaceAll("\\{DisplayName\\}", "%2\\$s");
		messageSelfDivorceNotInRange   = getMSG("Ingame.Divorce.Self.NotInRange").replaceAll("\\{Range\\}", "%.1f");
	}
	
	private Message getMSG(String path)
	{
		return plugin.getLanguage().getMessage(path);
	}

	//region surname functions
	@Override
	public String cleanupSurname(String surname)
	{
		if(surname == null || surname.isEmpty())
		{
			return null;
		}
		surname = surname.replace('§', '&').replaceAll("&k", "");
		if(surnameNotAllowedCharactersRex != null)
		{
			surname = surname.replaceAll(surnameNotAllowedCharactersRex, "");
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
		Player bPlayer1 = player1.getPlayerOnline(), bPlayer2 = player2.getPlayerOnline();
		if(player1.equals(player2))
		{
			messageNotWithHimself.send(priest, bPlayer1.getName(), bPlayer1.getDisplayName());
		}
		else if(!plugin.isPolygamyAllowed() && (player1.isMarried() || player2.isMarried()))
		{
			if(player1.isMarried()) messageAlreadyMarried.send(priest, bPlayer1.getName(), bPlayer1.getDisplayName());
			if(player2.isMarried()) messageAlreadyMarried.send(priest, bPlayer2.getName(), bPlayer2.getDisplayName());
		}
		else
		{
			return true;
		}
		return false;
	}

	private void marryPriestFinish(MarriagePlayer player1, MarriagePlayer player2, CommandSender priest, String surname)
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
			Player bPlayer1 = player1.getPlayerOnline(), bPlayer2 = player2.getPlayerOnline(), bPriest = priest.getPlayerOnline();
			if(player1.equals(priest) || player2.equals(priest)) // Self marry
			{
				if(plugin.isSelfMarriageAllowed() && priest.hasPermission("marry.selfmarry"))
				{
					MarriagePlayer otherPlayer = (player1.equals(priest)) ? player2 : player1;
					Player bOtherPlayer = otherPlayer.getPlayerOnline();
					if(player1.equals(player2))
					{
						messageNotYourself.send(bPriest);
					}
					else if(!plugin.isInRangeSquared(bPlayer1, bPlayer2, rangeMarrySquared))
					{
						messageSelfNotInRange.send(bPriest, rangeMarry);
					}
					else if(!plugin.isPolygamyAllowed() && (player1.isMarried() || player2.isMarried()))
					{
						if(priest.isMarried())
						{
							//noinspection ConstantConditions
							messageSelfAlreadyMarried.send(bPriest, priest.getPartner().getName(), priest.getPartner().getDisplayName());
						}
						if(otherPlayer.isMarried())
						{
							messageSelfOtherAlreadyMarried.send(bPriest, bOtherPlayer.getName(), bOtherPlayer.getDisplayName());
						}
					}
					else if(player1.getOpenRequest() != null || player2.getOpenRequest() != null)
					{
						if(priest.getOpenRequest() != null)
						{
							messageSelfAlreadyOpenRequest.send(bPriest);
						}
						if(otherPlayer.getOpenRequest() != null)
						{
							messageAlreadyOpenRequest.send(bPriest, bOtherPlayer.getName(), bOtherPlayer.getDisplayName());
						}
					}
					else
					{
						messageSelfConfirm.send(bOtherPlayer, bPriest.getName(), bPriest.getDisplayName());
						messageSelfMarryRequestSent.send(bPriest);
						plugin.getCommandManager().registerAcceptPendingRequest(new SelfMarryAcceptRequest(otherPlayer, priest, surname));
					}
				}
				else
				{
					priest.send(plugin.messageNoPermission);
				}
			}
			else // Player priest
			{
				if(priest.isPriest())
				{
					if(marryPriestTestCanMarry(player1, player2, bPriest))
					{
						if(priest.hasPermission("marry.bypass.rangelimit") || (plugin.isInRange(bPlayer1, bPlayer2, rangeMarry) && plugin.isInRange(bPriest, bPlayer1, rangeMarry) && plugin.isInRange(bPriest, bPlayer2, rangeMarry)))
						{
							if(!confirm)
							{
								marryPriestFinish(player1, player2, bPriest, surname);
							}
							else
							{
								if(player1.getOpenRequest() != null || player2.getOpenRequest() != null)
								{
									if(player1.getOpenRequest() != null)
									{
										messageAlreadyOpenRequest.send(bPriest, bPlayer2.getName(), bPlayer2.getDisplayName());
									}
									if(player2.getOpenRequest() != null)
									{
										messageAlreadyOpenRequest.send(bPriest, bPlayer1.getName(), bPlayer1.getDisplayName());
									}
								}
								else
								{
									if(autoDialog)
									{
										bPriest.chat(String.format(dialogDoYouWant, bPlayer1.getName(), bPlayer1.getDisplayName(), bPlayer2.getName(), bPlayer2.getDisplayName()));
									}
									plugin.getCommandManager().registerAcceptPendingRequest(new PriestMarryAcceptRequest(player1, player2, priest, surname, true));
								}
							}
						}
						else
						{
							messageNotInRange.send(bPriest, rangeMarry);
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

	private class SelfMarryAcceptRequest extends AcceptPendingRequest
	{
		private String surname;

		public SelfMarryAcceptRequest(@NotNull MarriagePlayer hasToAccept, @NotNull MarriagePlayer sender, @Nullable String surname)
		{
			super(hasToAccept, sender);
			this.surname = surname;
		}

		@Override
		public void onAccept()
		{
			if(getPlayersThatCanCancel() == null || getPlayersThatCanCancel().length == 0 || getPlayersThatCanCancel()[0] == null || !getPlayersThatCanCancel()[0].isOnline() || !getPlayerThatHasToAccept().isOnline()) return;
			MarriagePlayer player1 = getPlayersThatCanCancel()[0], player2 = getPlayerThatHasToAccept();
			MarryEvent marryEvent = new MarryEvent(player1, player2, player1.getPlayerOnline(), surname);
			Bukkit.getPluginManager().callEvent(marryEvent);
			if(!marryEvent.isCancelled())
			{
				MarriageData marriage = new MarriageData(getPlayersThatCanCancel()[0], getPlayerThatHasToAccept(), getPlayersThatCanCancel()[0], surname);
				plugin.getDatabase().cachedMarry(marriage);
				player2.send(messageSelfMarried, player1.getName(), player1.getDisplayName());
				player1.send(messageSelfMarried, player2.getName(), player2.getDisplayName());
				if(announceMarriage)
				{
					messageSelfBroadcastMarriage.broadcast(player1.getName(), player1.getDisplayName(), player2.getName(), player2.getDisplayName());
				}
				plugin.getServer().getPluginManager().callEvent(new MarriedEvent(marriage));
			}
		}

		@Override
		public void onDeny()
		{
			if(getPlayersThatCanCancel() == null || getPlayersThatCanCancel().length == 0 || getPlayersThatCanCancel()[0] == null || !getPlayersThatCanCancel()[0].isOnline() || !getPlayerThatHasToAccept().isOnline()) return;
			Player p1 = getPlayersThatCanCancel()[0].getPlayerOnline(), p2 = getPlayerThatHasToAccept().getPlayerOnline();
			messageSelfYouCalledOff.send(p2);
			messageSelfPlayerCalledOff.send(p1, p2.getName(), p2.getDisplayName());
		}

		@Override
		public void onCancel(@NotNull MarriagePlayer player)
		{
			if(getPlayersThatCanCancel() == null || getPlayersThatCanCancel().length == 0 || getPlayersThatCanCancel()[0] == null || !getPlayersThatCanCancel()[0].isOnline() || !getPlayerThatHasToAccept().isOnline()) return;
			Player p1 = getPlayersThatCanCancel()[0].getPlayerOnline(), p2 = getPlayerThatHasToAccept().getPlayerOnline();
			messageSelfYouCalledOff.send(p1);
			messageSelfPlayerCalledOff.send(p2, p1.getName(), p1.getDisplayName());
		}

		@Override
		protected void onDisconnect(@NotNull MarriagePlayer player)
		{
			//noinspection ConstantConditions
			if(player.equals(getPlayersThatCanCancel()[0]))
			{
				getPlayerThatHasToAccept().send(messageSelfPlayerMarryOff, player.getName(), player.getDisplayName());
			}
			else
			{
				//noinspection ConstantConditions
				getPlayersThatCanCancel()[0].send(messageSelfPlayerMarryOff, player.getName(), player.getDisplayName());
			}
		}
	}

	private class PriestMarryAcceptRequest extends AcceptPendingRequest
	{
		private String surname;
		private boolean firstPlayer;

		public PriestMarryAcceptRequest(MarriagePlayer player1, MarriagePlayer player2, MarriagePlayer priest, String surname, boolean firstPlayer)
		{
			super(player1, player2, priest);
			this.surname = surname;
			this.firstPlayer = firstPlayer;
			player1.send(messageConfirm, priest.getName(), priest.getDisplayName(), player1.getName(), player1.getDisplayName());
		}

		@Override
		public void onAccept()
		{
			//noinspection ConstantConditions
			if(getPlayersThatCanCancel() == null || !getPlayersThatCanCancel()[0].isOnline() || !getPlayersThatCanCancel()[1].isOnline() || !getPlayerThatHasToAccept().isOnline()) return;
			if(autoDialog)
			{
				getPlayerThatHasToAccept().getPlayerOnline().chat(dialogYesIWant);
			}
			if(firstPlayer)
			{
				Player player = getPlayerThatHasToAccept().getPlayerOnline(), otherPlayer = getPlayersThatCanCancel()[0].getPlayerOnline();
				if(autoDialog)
				{
					getPlayersThatCanCancel()[1].getPlayerOnline().chat(String.format(dialogAndDoYouWant, player.getName(), player.getDisplayName(), otherPlayer.getName(), otherPlayer.getDisplayName()));
				}
				plugin.getCommandManager().registerAcceptPendingRequest(new PriestMarryAcceptRequest(getPlayersThatCanCancel()[0], getPlayerThatHasToAccept(), getPlayersThatCanCancel()[1], surname, false));
			}
			else
			{
				marryPriestFinish(getPlayersThatCanCancel()[0], getPlayerThatHasToAccept(), getPlayersThatCanCancel()[1].getPlayerOnline(), surname);
			}
		}

		@Override
		public void onDeny()
		{
			//noinspection ConstantConditions
			if(getPlayersThatCanCancel() == null || !getPlayersThatCanCancel()[0].isOnline() || !getPlayersThatCanCancel()[1].isOnline() || !getPlayerThatHasToAccept().isOnline()) return;
			Player player = getPlayerThatHasToAccept().getPlayerOnline();
			if(confirm && autoDialog)
			{
				getPlayerThatHasToAccept().getPlayerOnline().chat(dialogNoIDontWant);
			}
			messageYouCalledOff.send(player);
			messagePlayerCalledOff.send(getPlayersThatCanCancel()[1].getPlayerOnline(), player.getName(), player.getDisplayName());
			messagePlayerCalledOff.send(getPlayersThatCanCancel()[0].getPlayerOnline(), player.getName(), player.getDisplayName());
		}

		@Override
		public void onCancel(@NotNull MarriagePlayer mPlayer)
		{
			//noinspection ConstantConditions
			if(getPlayersThatCanCancel() == null || !getPlayersThatCanCancel()[0].isOnline() || !getPlayersThatCanCancel()[1].isOnline() || !getPlayerThatHasToAccept().isOnline()) return;
			Player player = mPlayer.getPlayerOnline();
			messageYouCalledOff.send(player);
			getPlayerThatHasToAccept().send(messagePlayerCalledOff, player.getName(), player.getDisplayName());
			((mPlayer.equals(getPlayersThatCanCancel()[0])) ? getPlayersThatCanCancel()[1] : getPlayersThatCanCancel()[0]).send(messagePlayerCalledOff, player.getName(), player.getDisplayName());
		}

		@Override
		protected void onDisconnect(@NotNull MarriagePlayer player)
		{
			//noinspection ConstantConditions
			if(player.equals(getPlayersThatCanCancel()[1]))
			{
				getPlayerThatHasToAccept().send(messagePriestMarryOff, player.getName(), player.getDisplayName());
				//noinspection ConstantConditions
				getPlayersThatCanCancel()[0].send(messagePriestMarryOff, player.getName(), player.getDisplayName());
			}
			else if(player.equals(getPlayersThatCanCancel()[0]))
			{
				getPlayerThatHasToAccept().send(messagePlayerMarryOff, player.getName(), player.getDisplayName());
				//noinspection ConstantConditions
				getPlayersThatCanCancel()[1].send(messagePlayerMarryOff, player.getName(), player.getDisplayName());
			}
			else
			{
				//noinspection ConstantConditions
				getPlayersThatCanCancel()[0].send(messagePlayerMarryOff, player.getName(), player.getDisplayName());
				//noinspection ConstantConditions
				getPlayersThatCanCancel()[1].send(messagePlayerMarryOff, player.getName(), player.getDisplayName());
			}
		}
	}
	//endregion

	//region divorce functions
	private void priestFinishDivorce(@NotNull Marriage marriage, @NotNull CommandSender divorceBy)
	{
		DivorceEvent divorceEvent = new DivorceEvent(marriage, divorceBy);
		Bukkit.getPluginManager().callEvent(divorceEvent);
		if(!divorceEvent.isCancelled())
		{
			marriage.divorce();
			OfflinePlayer player1 = marriage.getPartner1().getPlayer(), player2 = marriage.getPartner2().getPlayer();
			String player1Name = marriage.getPartner1().getName(), player1DisplayName = marriage.getPartner1().getDisplayName();
			String player2Name = marriage.getPartner2().getName(), player2DisplayName = marriage.getPartner2().getDisplayName();
			String priestName, priestDPName;
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
				messageBroadcastDivorce.broadcast(priestName, priestDPName, player1Name, player1DisplayName, player2Name, player2DisplayName);
			}
			messageDivorced.send(divorceBy, player1Name, player1DisplayName, player2Name, player2DisplayName);
			if(player1.isOnline()) messageDivorcedPlayer.send(player1.getPlayer(), priestName, priestDPName, player2Name, player2DisplayName);
			if(player2.isOnline()) messageDivorcedPlayer.send(player2.getPlayer(), priestName, priestDPName, player1Name, player1DisplayName);
			plugin.getServer().getPluginManager().callEvent(new DivorcedEvent(marriage.getPartner1(), marriage.getPartner2()));
		}
	}

	private void selfFinishDivorce(@NotNull Marriage marriage, @NotNull MarriagePlayer divorceBy)
	{
		DivorceEvent divorceEvent = new DivorceEvent(marriage, divorceBy.getPlayerOnline());
		Bukkit.getPluginManager().callEvent(divorceEvent);
		if(!divorceEvent.isCancelled())
		{
			marriage.divorce();
			Player bPlayer = divorceBy.getPlayerOnline();
			MarriagePlayer otherPlayer = marriage.getPartner(divorceBy);
			//noinspection ConstantConditions
			String otherName = otherPlayer.getName(), otherDisplayName = otherPlayer.getDisplayName();
			marriage.divorce();
			messageSelfDivorced.send(bPlayer, otherName, otherDisplayName);
			if(otherPlayer.isOnline())
			{
				otherPlayer.send(messageSelfDivorcedPlayer, bPlayer.getName(), bPlayer.getDisplayName());
			}
			if(announceMarriage)
			{
				messageSelfBroadcastDivorce.broadcast(bPlayer.getName(), bPlayer.getDisplayName(), otherName, otherDisplayName);
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
				Player bPlayer = divorceBy.getPlayerOnline();
				//noinspection ConstantConditions
				if(!otherPlayer.isOnline())
				{
					// Self divorce with offline partner
					if(bPlayer.hasPermission("marry.offlinedivorce"))
					{
						selfFinishDivorce(marriage, divorceBy);
					}
					else
					{
						plugin.messagePartnerOffline.send(bPlayer);
					}
				}
				else
				{
					if(plugin.isInRangeSquared(bPlayer, otherPlayer.getPlayerOnline(), rangeDivorceSquared))
					{
						if(otherPlayerOnSelfDivorce)
						{
							if(marriage.getPartner1().getOpenRequest() != null || marriage.getPartner2().getOpenRequest() != null)
							{
								if(marriage.getPartner1().getOpenRequest() != null)
								{
									messageAlreadyOpenRequest.send(bPlayer, marriage.getPartner1().getName(), marriage.getPartner1().getDisplayName());
								}
								if(marriage.getPartner2().getOpenRequest() != null)
								{
									messageAlreadyOpenRequest.send(bPlayer, marriage.getPartner2().getName(), marriage.getPartner2().getDisplayName());
								}
							}
							else
							{
								otherPlayer.send(messageSelfDivorceConfirm, bPlayer.getName(), bPlayer.getDisplayName());
								messageSelfDivorceRequestSent.send(bPlayer, otherPlayer.getName(), otherPlayer.getDisplayName());
								plugin.getCommandManager().registerAcceptPendingRequest(new SelfDivorceAcceptRequest(otherPlayer, divorceBy, marriage));
							}
						}
						else
						{
							selfFinishDivorce(marriage, divorceBy);
						}
					}
					else
					{
						messageSelfDivorceNotInRange.send(bPlayer, rangeDivorce);
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
					if(divorceBy.hasPermission("marry.bypass.rangelimit") || (plugin.isInRange(divorceBy.getPlayerOnline(), marriage.getPartner1().getPlayerOnline(), rangeDivorce) && plugin.isInRange(divorceBy.getPlayerOnline(), marriage.getPartner2().getPlayerOnline(), rangeDivorce)))
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
								plugin.getCommandManager().registerAcceptPendingRequest(new PriestDivorceAcceptRequest(first, divorceBy, marriage, bothOnDivorce));
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
									plugin.getCommandManager().registerAcceptPendingRequest(new PriestDivorceAcceptRequest(first, divorceBy, marriage, false));
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

	private class SelfDivorceAcceptRequest extends AcceptPendingRequest
	{
		private Marriage marriageData;

		public SelfDivorceAcceptRequest(@NotNull MarriagePlayer hasToAccept, @NotNull MarriagePlayer canCancel, @NotNull Marriage marriageData)
		{
			super(hasToAccept, canCancel);
			this.marriageData = marriageData;
		}

		@Override
		protected void onAccept()
		{
			//noinspection ConstantConditions
			selfFinishDivorce(marriageData, getPlayersThatCanCancel()[0]);
		}

		@Override
		protected void onDeny()
		{
			//noinspection ConstantConditions
			getPlayersThatCanCancel()[0].send(messageSelfDivorceDeny, getPlayerThatHasToAccept().getName(), getPlayerThatHasToAccept().getDisplayName());
			getPlayerThatHasToAccept().send(messageSelfDivorceYouDeny, getPlayersThatCanCancel()[0].getName(), getPlayersThatCanCancel()[0].getDisplayName());
		}

		@Override
		protected void onCancel(@NotNull MarriagePlayer player)
		{
			getPlayerThatHasToAccept().send(messageSelfDivorceCancelled, player.getName(), player.getDisplayName());
			player.send(messageSelfDivorceYouCancelled, getPlayerThatHasToAccept().getName(), getPlayerThatHasToAccept().getDisplayName());
		}

		@Override
		protected void onDisconnect(@NotNull MarriagePlayer player)
		{
			//noinspection ConstantConditions
			if(player.equals(getPlayersThatCanCancel()[0]))
			{
				getPlayerThatHasToAccept().send(messageSelfDivorcePlayerOff, player.getName(), player.getDisplayName());
			}
			else
			{
				//noinspection ConstantConditions
				getPlayersThatCanCancel()[0].send(messageSelfDivorcePlayerOff, player.getName(), player.getDisplayName());
			}
		}
	}

	private class PriestDivorceAcceptRequest extends AcceptPendingRequest
	{
		private boolean first;
		private Marriage marriage;
		private MarriagePlayer partner, priest;

		public PriestDivorceAcceptRequest(@NotNull MarriagePlayer hasToAccept, @NotNull MarriagePlayer priest, @NotNull Marriage marriage, boolean first)
		{
			//noinspection ConstantConditions
			super(hasToAccept, ((marriage.getPartner(hasToAccept).isOnline()) ? new MarriagePlayer[] { marriage.getPartner(hasToAccept), priest } : new MarriagePlayer[] { priest }));
			this.first = first;
			this.marriage = marriage;
			this.priest = priest;
			partner = marriage.getPartner(hasToAccept);
			hasToAccept.send(messageDivorceConfirm, priest.getName(), priest.getDisplayName(), partner.getName(), partner.getDisplayName());
		}

		@Override
		protected void onAccept()
		{
			if(first)
			{
				plugin.getCommandManager().registerAcceptPendingRequest(new PriestDivorceAcceptRequest(partner, priest, marriage, false));
			}
			else
			{
				priestFinishDivorce(marriage, priest.getPlayerOnline());
			}
		}

		@Override
		protected void onDeny()
		{
			MarriagePlayer player = getPlayerThatHasToAccept();
			if(partner.isOnline()) partner.send(messageDivorceDeny, player.getName(), player.getDisplayName());
			priest.send(messageDivorceDeny, player.getName(), player.getDisplayName());
			player.send(messageDivorceYouDeny, partner.getName(), partner.getDisplayName());
		}

		@Override
		protected void onCancel(@NotNull MarriagePlayer player)
		{
			if(player.equals(priest)) // The priest cancelled the divorce
			{
				getPlayerThatHasToAccept().send(messageDivorcePriestCancelled, player.getName(), player.getDisplayName(), partner.getName(), partner.getDisplayName());
				player.send(messageDivorceYouCancelledPriest, marriage.getPartner1().getName(), marriage.getPartner1().getDisplayName(), marriage.getPartner2().getName(), marriage.getPartner2().getDisplayName());
			}
			else
			{
				priest.send(messageDivorcePlayerCancelled, player.getName(), player.getDisplayName(), getPlayerThatHasToAccept().getName(), getPlayerThatHasToAccept().getDisplayName());
				player.send(messageDivorceYouCancelled, partner.getName(), partner.getDisplayName());
			}
		}

		@SuppressWarnings("ConstantConditions")
		@Override
		protected void onDisconnect(@NotNull MarriagePlayer player)
		{
			if(player.equals(getPlayerThatHasToAccept()))
			{
				getPlayersThatCanCancel()[0].send(messageDivorcePlayerOff, player.getName(), player.getDisplayName());
				getPlayersThatCanCancel()[1].send(messageDivorcePlayerOff, player.getName(), player.getDisplayName());
			}
			else if(player.equals(getPlayersThatCanCancel()[0]))
			{
				getPlayerThatHasToAccept().send(messageDivorcePlayerOff, player.getName(), player.getDisplayName());
				getPlayersThatCanCancel()[1].send(messageDivorcePlayerOff, player.getName(), player.getDisplayName());
			}
			else
			{
				getPlayerThatHasToAccept().send(messageDivorcePriestOff, player.getName(), player.getDisplayName());
				getPlayersThatCanCancel()[0].send(messageDivorcePriestOff, player.getName(), player.getDisplayName());
			}
		}
	}
	//endregion
}