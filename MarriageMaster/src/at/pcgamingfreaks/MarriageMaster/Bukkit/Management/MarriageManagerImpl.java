/*
 *   Copyright (C) 2022 GeorgH93
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
 *   along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.MarriageMaster.Bukkit.Management;

import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.Bukkit.Message.MessageBuilder;
import at.pcgamingfreaks.Bukkit.Message.Sender.SendMethod;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.*;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.CommonMessages;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Database.MarriageData;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Database.MarriagePlayerData;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Management.Requests.PriestDivorceAcceptRequest;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Management.Requests.PriestMarryAcceptRequest;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Management.Requests.SelfDivorceAcceptRequest;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Management.Requests.SelfMarryAcceptRequest;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Range;
import at.pcgamingfreaks.MarriageMaster.Database.MarriagePlayerDataBase;
import at.pcgamingfreaks.MarriageMaster.Permissions;
import at.pcgamingfreaks.MarriageMaster.Placeholder.Placeholders;
import at.pcgamingfreaks.MarriageMaster.Placeholder.Processors.DisplayNamePlaceholderProcessor;
import at.pcgamingfreaks.Message.MessageColor;
import at.pcgamingfreaks.Message.MessageComponent;
import at.pcgamingfreaks.Message.Placeholder.Placeholder;
import at.pcgamingfreaks.Message.Placeholder.Processors.FloatPlaceholderProcessor;
import at.pcgamingfreaks.Message.Placeholder.Processors.ParameterTypeAwarePlaceholderProcessor;
import at.pcgamingfreaks.Message.Placeholder.Processors.PassthroughMessageComponentPlaceholderProcessor;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

public final class MarriageManagerImpl implements at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriageManager
{ // TODO refactor me!!!!
	public static final String CONSOLE_NAME = "Console", CONSOLE_DISPLAY_NAME = MessageColor.GRAY + CONSOLE_NAME, COLOR_CODE_REGEX = "&[a-fA-F0-9l-orL-OR]";
	public static final MessageComponent CONSOLE_DISPLAY_NAME_COMPONENT = new MessageBuilder().appendLegacy(CONSOLE_DISPLAY_NAME).getAsComponent();

	private final MarriageMaster plugin;
	private final String  surnameNotAllowedCharactersRex, dialogDoYouWant, dialogMarried;
	private final Message messageSurnameSuccess, messageSurnameFailed, messageSurnameToShort, messageSurnameToLong, messageSurnameAlreadyUsed;
	private final Message messageAlreadyMarried, messageNotWithHimself, messageSurnameNeeded, messageMarried, messageHasMarried, messageBroadcastMarriage, messageNotInRange, messageAlreadyOpenRequest;
	private final Message messageNotYourself, messageSelfNotInRange, messageSelfAlreadyMarried, messageSelfOtherAlreadyMarried, messageSelfAlreadyOpenRequest, messageSelfConfirm, messageSelfMarryRequestSent;
	private final Message messageAlreadySamePair, messageSelfAlreadySamePair;
	private final Message messageBroadcastDivorce, messageDivorced, messageDivorcedPlayer, messageDivorceNotInRange, messageSelfNotOnYourOwn;
	private final Message messageSelfDivorced, messageSelfBroadcastDivorce, messageSelfDivorcedPlayer, messageSelfDivorceRequestSent, messageSelfDivorceConfirm, messageSelfDivorceNotInRange;
	private final Message messageMaxPartnersReached, messageSelfMaxPartnersReached, messageSelfMaxPartnersReachedOther;
	private final boolean surnameAllowColors, confirm, bothOnDivorce, autoDialog, otherPlayerOnSelfDivorce;
	private final int surnameMinLength, surnameMaxLength, maxPartners;
	private final double rangeMarry, rangeDivorce, rangeMarrySquared, rangeDivorceSquared;

	public MarriageManagerImpl(final @NotNull MarriageMaster plugin)
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

		rangeMarry       = plugin.getConfiguration().getRange(Range.Marry);
		rangeDivorce     = plugin.getConfiguration().getRange(Range.Divorce);
		boolean announceMarriage = plugin.getConfiguration().isMarryAnnouncementEnabled();
		boolean announceDivorce  = plugin.getConfiguration().isDivorceAnnouncementEnabled();
		confirm          = plugin.getConfiguration().isMarryConfirmationEnabled();
		bothOnDivorce    = plugin.getConfiguration().isConfirmationBothPlayersOnDivorceEnabled();
		autoDialog       = plugin.getConfiguration().isMarryConfirmationAutoDialogEnabled();
		otherPlayerOnSelfDivorce = plugin.getConfiguration().isConfirmationOtherPlayerOnSelfDivorceEnabled();
		rangeMarrySquared   = (rangeMarry > 0) ? rangeMarry * rangeMarry : rangeMarry;
		rangeDivorceSquared = (rangeDivorce > 0) ? rangeDivorce * rangeDivorce : rangeDivorce;
		maxPartners         = plugin.getConfiguration().getMaxPartners();

		dialogDoYouWant     = plugin.getLanguage().getDialog("DoYouWant").replace("{Player1Name}", "%1$s").replace("{Player1DisplayName}", "%2$s").replace("{Player2Name}", "%3$s").replace("{Player2DisplayName}", "%4$s");
		dialogMarried       = plugin.getLanguage().getDialog("Married").replace("{Player1Name}", "%1$s").replace("{Player1DisplayName}", "%2$s").replace("{Player2Name}", "%3$s").replace("{Player2DisplayName}", "%4$s");

		Placeholder rangePlaceholder = new Placeholder("Range", new FloatPlaceholderProcessor(1));
		ParameterTypeAwarePlaceholderProcessor priestProcessor = new ParameterTypeAwarePlaceholderProcessor();
		priestProcessor.add(MarriagePlayer.class, DisplayNamePlaceholderProcessor.INSTANCE);
		priestProcessor.add(MarriagePlayerData.class, DisplayNamePlaceholderProcessor.INSTANCE);
		priestProcessor.add(MarriagePlayerDataBase.class, DisplayNamePlaceholderProcessor.INSTANCE);
		priestProcessor.add(MessageComponent.class, PassthroughMessageComponentPlaceholderProcessor.INSTANCE);
		Placeholder[] priestPlaceholders = new Placeholder[] { new Placeholder("PriestName", null, Placeholder.AUTO_INCREMENT_INDIVIDUALLY), new Placeholder("PriestDisplayName", priestProcessor, Placeholder.AUTO_INCREMENT_INDIVIDUALLY)};

		messageSurnameSuccess          = getMSG("Ingame.Surname.SetSuccessful");
		messageSurnameFailed           = getMSG("Ingame.Surname.SetFailed");
		messageSurnameToShort          = getMSG("Ingame.Surname.ToShort").replaceAll("\\{MinLength}", surnameMinLength + "").replaceAll("\\{MaxLength}", surnameMaxLength + "");
		messageSurnameToLong           = getMSG("Ingame.Surname.ToLong").replaceAll("\\{MinLength}", surnameMinLength + "").replaceAll("\\{MaxLength}", surnameMaxLength + "");
		messageSurnameAlreadyUsed      = getMSG("Ingame.Surname.AlreadyUsed");

		messageAlreadySamePair         = getMSG("Ingame.Marry.AlreadySamePair").placeholders(Placeholders.PLAYER1_NAME).placeholders(Placeholders.PLAYER2_NAME);
		messageAlreadyMarried          = getMSG("Ingame.Marry.AlreadyMarried").placeholders(Placeholders.PLAYER_NAME);
		messageNotWithHimself          = getMSG("Ingame.Marry.NotWithHimself").placeholders(Placeholders.PLAYER_NAME);
		messageSurnameNeeded           = getMSG("Ingame.Marry.SurnameNeeded");
		messageMarried                 = getMSG("Ingame.Marry.Married").placeholders(Placeholders.PLAYER1_NAME).placeholders(Placeholders.PLAYER2_NAME);
		messageHasMarried              = getMSG("Ingame.Marry.HasMarried").placeholders(priestPlaceholders).placeholders(Placeholders.PARTNER_NAME);
		messageBroadcastMarriage       = getMSG("Ingame.Marry.Broadcast").placeholders(priestPlaceholders).placeholders(Placeholders.PLAYER1_NAME).placeholders(Placeholders.PLAYER2_NAME);
		if (!announceMarriage) messageBroadcastMarriage.setSendMethod(SendMethod.DISABLED);
		messageNotInRange              = getMSG("Ingame.Marry.NotInRange").placeholders(rangePlaceholder);
		messageAlreadyOpenRequest      = getMSG("Ingame.Marry.AlreadyOpenRequest").placeholders(Placeholders.PLAYER_NAME);

		messageSelfConfirm             = getMSG("Ingame.Marry.Self.Confirm").placeholders(Placeholders.PLAYER_NAME);
		messageNotYourself             = getMSG("Ingame.Marry.Self.NotYourself");
		messageSelfNotInRange          = getMSG("Ingame.Marry.Self.NotInRange").placeholders(rangePlaceholder);
		messageSelfAlreadySamePair     = getMSG("Ingame.Marry.Self.AlreadySamePair").placeholders(Placeholders.mkPlayerNameRegex("(Partner)?"));
		messageSelfAlreadyMarried      = getMSG("Ingame.Marry.Self.AlreadyMarried").placeholders(Placeholders.PLAYER_NAME);
		messageSelfMarryRequestSent    = getMSG("Ingame.Marry.Self.RequestSent");
		messageSelfOtherAlreadyMarried = getMSG("Ingame.Marry.Self.OtherAlreadyMarried").placeholders(Placeholders.PLAYER_NAME);
		messageSelfAlreadyOpenRequest  = getMSG("Ingame.Marry.Self.AlreadyOpenRequest");
		messageSelfNotOnYourOwn        = getMSG("Ingame.Marry.Self.NotOnYourOwn");

		messageMaxPartnersReached          = getMSG("Ingame.Marry.MaxPartnersReached").replaceAll("\\{MaxPartnerCount}", Integer.toString(maxPartners)).placeholders(Placeholders.PLAYER_NAME);
		messageSelfMaxPartnersReached      = getMSG("Ingame.Marry.Self.MaxPartnersReached").replaceAll("\\{MaxPartnerCount}", Integer.toString(maxPartners));
		messageSelfMaxPartnersReachedOther = getMSG("Ingame.Marry.Self.MaxPartnersReachedOther").replaceAll("\\{MaxPartnerCount}", Integer.toString(maxPartners)).placeholders(Placeholders.PLAYER_NAME);

		messageDivorced                = getMSG("Ingame.Divorce.Divorced").placeholders(Placeholders.PLAYER1_NAME).placeholders(Placeholders.PLAYER2_NAME);
		messageDivorcedPlayer          = getMSG("Ingame.Divorce.DivorcedPlayer").placeholders(priestPlaceholders).placeholders(Placeholders.PARTNER_NAME);
		messageBroadcastDivorce        = getMSG("Ingame.Divorce.Broadcast").placeholders(priestPlaceholders).placeholders(Placeholders.PLAYER1_NAME).placeholders(Placeholders.PLAYER2_NAME);
		if (!announceDivorce) messageBroadcastDivorce.setSendMethod(SendMethod.DISABLED);
		messageDivorceNotInRange       = getMSG("Ingame.Divorce.NotInRange").placeholders(rangePlaceholder);

		messageSelfDivorced            = getMSG("Ingame.Divorce.Self.Divorced").placeholders(Placeholders.PLAYER_NAME);
		messageSelfDivorceConfirm      = getMSG("Ingame.Divorce.Self.Confirm").placeholders(Placeholders.PLAYER_NAME);
		messageSelfDivorcedPlayer      = getMSG("Ingame.Divorce.Self.DivorcedPlayer").placeholders(Placeholders.PLAYER_NAME);
		messageSelfBroadcastDivorce    = getMSG("Ingame.Divorce.Self.Broadcast").placeholders(Placeholders.PLAYER1_NAME).placeholders(Placeholders.PLAYER2_NAME);
		if (!announceDivorce) messageSelfBroadcastDivorce.setSendMethod(SendMethod.DISABLED);
		messageSelfDivorceRequestSent  = getMSG("Ingame.Divorce.Self.RequestSent").placeholders(Placeholders.PLAYER_NAME);
		messageSelfDivorceNotInRange   = getMSG("Ingame.Divorce.Self.NotInRange").placeholders(rangePlaceholder);

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
	//endregion

	//region surname functions
	@Override
	@Contract(value = "null->null")
	public String cleanupSurname(String surname)
	{
		if(surname == null || surname.isEmpty()) return null;
		if(plugin.getCommandManager().isRemoveSwitch(surname) || "null".equalsIgnoreCase(surname) || "none".equalsIgnoreCase(surname)) return null;

		surname = surname.replace('§', '&').replace("&k", "");
		if(surnameNotAllowedCharactersRex != null)
		{
			String s = surname.replaceAll(COLOR_CODE_REGEX, "");
			String surnameCleaned = s.replaceAll(surnameNotAllowedCharactersRex, "");
			if(!s.equals(surnameCleaned))
			{ // Surname contains not allowed chars
				surname = surnameCleaned; //TODO add back colors
			}
		}
		return (surnameAllowColors) ? MessageColor.translateAlternateColorCodes('&', surname) : surname;
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
		return surname != null && isSurnameLengthValid(surname) && isSurnameAvailable(surname);
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
		surname = cleanupSurname(surname);
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
					SurnameChangeEvent event = new SurnameChangeEvent(marriage, surname, changer);
					plugin.getServer().getPluginManager().callEvent(event);
					if(!event.isCancelled())
					{
						surname = event.getNewSurname();
						if(marriage.setSurname(surname))
						{
							messageSurnameSuccess.send(changer);
							plugin.getServer().getPluginManager().callEvent(new SurnameChangedEvent(marriage, surname, changer));
						}
						else
						{
							messageSurnameFailed.send(changer);
						}
					}
					else
					{
						messageSurnameAlreadyUsed.send(changer);
					}
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
	private boolean marrySurnameTest(final @NotNull CommandSender priest, final @Nullable String surname)
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

	private boolean marryOnlineTest(final @NotNull CommandSender priest, final @NotNull MarriagePlayer player1, final @NotNull MarriagePlayer player2)
	{
		if(!player1.isOnline())
		{
			CommonMessages.getMessagePlayerNotOnline().send(priest, player1.getName());
		}
		else if(!player2.isOnline())
		{
			CommonMessages.getMessagePlayerNotOnline().send(priest, player2.getName());
		}
		else return true;
		return false;
	}

	private boolean marryPriestTestCanMarry(MarriagePlayer player1, MarriagePlayer player2, CommandSender priest)
	{
		if(player1.equals(player2))
		{
			messageNotWithHimself.send(priest, player1);
		}
		else if (player1.getPartners().contains(player2))
		{
			messageAlreadySamePair.send(priest, player1, player2);
		}
		else if(!plugin.areMultiplePartnersAllowed() && (player1.isMarried() || player2.isMarried()))
		{
			if(player1.isMarried()) messageAlreadyMarried.send(priest, player1);
			if(player2.isMarried()) messageAlreadyMarried.send(priest, player2);
		}
		else if(player1.getPartners().size() >= maxPartners)
		{
			messageMaxPartnersReached.send(priest, player1);
		}
		else if(player2.getPartners().size() >= maxPartners)
		{
			messageMaxPartnersReached.send(priest, player2);
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
			String priestName;
			Object priestNameProvider;
			MarriageData marriage;
			if(priest instanceof Player)
			{
				priestName = priest.getName();
				marriage = new MarriageData(player1, player2, plugin.getPlayerData((Player) priest), new Date(), surname);
				if(confirm && autoDialog)
				{
					String msg = String.format(dialogMarried, player1.getName(), player1.getDisplayName(), player2.getName(), player2.getDisplayName());
					((Player) priest).chat(msg);
				}
				priestNameProvider = plugin.getPlayerData((Player) priest);
			}
			else
			{
				priestName = CONSOLE_NAME;
				marriage = new MarriageData(player1, player2, null, surname);
				priestNameProvider = CONSOLE_DISPLAY_NAME_COMPONENT;
			}

			plugin.getDatabase().cachedMarry(marriage);
			messageMarried.send(priest, player1, player2);
			player1.sendMessage(messageHasMarried, priestName, priestNameProvider, player2);
			player2.sendMessage(messageHasMarried, priestName, priestNameProvider, player1);
			messageBroadcastMarriage.broadcast(priestName, priestNameProvider, player1, player2);
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
		else if(marryOnlineTest(priest, player1, player2) && marryPriestTestCanMarry(player1, player2, priest) && marrySurnameTest(priest, surname))
		{
			marryPriestFinish(player1, player2, priest, surname);
		}
	}

	@Override
	public void marry(@NotNull MarriagePlayer player1, @NotNull MarriagePlayer player2, @NotNull MarriagePlayer priest, String surname)
	{
		if(priest.isOnline() && marryOnlineTest(priest.getPlayerOnline(), player1, player2) && marrySurnameTest(priest.getPlayerOnline(), surname))
		{
			if(player1.equals(priest) || player2.equals(priest)) // Self marry
			{
				if(plugin.isSelfMarriageAllowed() && priest.hasPermission(Permissions.SELF_MARRY))
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
					else if (priest.getPartners().contains(otherPlayer))
					{
						priest.send(messageSelfAlreadySamePair, otherPlayer);
					}
					else if(!plugin.areMultiplePartnersAllowed() && (player1.isMarried() || player2.isMarried()))
					{
						if(priest.isMarried())
						{
							priest.send(messageSelfAlreadyMarried, priest.getPartner());
						}
						else if(otherPlayer.isMarried())
						{
							priest.send(messageSelfOtherAlreadyMarried, otherPlayer);
						}
					}
					else if(priest.getPartners().size() >= maxPartners)
					{
						priest.send(messageSelfMaxPartnersReached);
					}
					else if(otherPlayer.getPartners().size() >= maxPartners)
					{
						priest.send(messageSelfMaxPartnersReachedOther, otherPlayer);
					}
					else if(priest.getOpenRequest() != null || !priest.getRequestsToCancel().isEmpty())
					{
						priest.send(messageSelfAlreadyOpenRequest);
					}
					else if(otherPlayer.getOpenRequest() != null || !otherPlayer.getRequestsToCancel().isEmpty())
					{
						priest.send(messageAlreadyOpenRequest, otherPlayer);
					}
					else
					{
						otherPlayer.send(messageSelfConfirm, priest);
						priest.send(messageSelfMarryRequestSent);
						plugin.getCommandManager().registerAcceptPendingRequest(new SelfMarryAcceptRequest(otherPlayer, priest, surname));
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
						if(priest.hasPermission(Permissions.BYPASS_RANGELIMIT) || (plugin.isInRangeSquared(player1, player2, rangeMarrySquared) && plugin.isInRangeSquared(priest, player1, rangeMarrySquared) &&
								plugin.isInRangeSquared(priest, player2, rangeMarrySquared)))
						{
							if(!confirm)
							{
								marryPriestFinish(player1, player2, bPriest, surname);
							}
							else
							{
								if(player1.getOpenRequest() != null || !player1.getRequestsToCancel().isEmpty())
								{
									priest.send(messageAlreadyOpenRequest, player1);
								}
								else if(player2.getOpenRequest() != null || !player2.getRequestsToCancel().isEmpty())
								{
									priest.send(messageAlreadyOpenRequest, player2);
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
					priest.send(CommonMessages.getMessageNoPermission());
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
			String priestName;
			Object priestNameProvider;
			if(divorceBy instanceof Player)
			{
				priestName = divorceBy.getName();
				priestNameProvider = plugin.getPlayerData((Player) divorceBy);
			}
			else
			{
				priestName = CONSOLE_NAME;
				priestNameProvider = CONSOLE_DISPLAY_NAME_COMPONENT;
			}
			messageBroadcastDivorce.broadcast(priestName, priestNameProvider, player1, player2);
			messageDivorced.send(divorceBy, player1, player2);
			if(player1.isOnline()) player1.send(messageDivorcedPlayer, priestName, priestNameProvider, player2);
			if(player2.isOnline()) player2.send(messageDivorcedPlayer, priestName, priestNameProvider, player1);
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
			marriage.divorce();
			divorceBy.send(messageSelfDivorced, otherPlayer);
			if(otherPlayer.isOnline())
			{
				otherPlayer.send(messageSelfDivorcedPlayer, divorceBy);
			}
			messageSelfBroadcastDivorce.broadcast(divorceBy, otherPlayer);
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
			if(plugin.isSelfDivorceAllowed() && divorceBy.hasPermission(Permissions.SELF_DIVORCE))
			{
				MarriagePlayer otherPlayer = marriage.getPartner(divorceBy);
				//noinspection ConstantConditions
				if(!otherPlayer.isOnline())
				{
					// Self divorce with offline partner
					if(divorceBy.hasPermission(Permissions.OFFLINEDIVORCE))
					{
						selfFinishDivorce(marriage, divorceBy);
					}
					else
					{
						divorceBy.send(CommonMessages.getMessagePartnerOffline());
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
									divorceBy.send(messageAlreadyOpenRequest, marriage.getPartner1());
								}
								if(marriage.getPartner2().getOpenRequest() != null)
								{
									divorceBy.send(messageAlreadyOpenRequest, marriage.getPartner2());
								}
							}
							else
							{
								otherPlayer.send(messageSelfDivorceConfirm, divorceBy);
								divorceBy.send(messageSelfDivorceRequestSent, otherPlayer);
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
				divorceBy.send(CommonMessages.getMessageNoPermission());
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
					if(divorceBy.hasPermission(Permissions.BYPASS_RANGELIMIT) || (plugin.isInRange(divorceBy, marriage.getPartner1(), rangeDivorce) && plugin.isInRange(divorceBy, marriage.getPartner2(), rangeDivorce)))
					{
						if(confirm)
						{
							if(marriage.getPartner1().getOpenRequest() != null || marriage.getPartner2().getOpenRequest() != null)
							{
								if(marriage.getPartner1().getOpenRequest() != null)
								{
									divorceBy.send(messageAlreadyOpenRequest, marriage.getPartner1());
								}
								if(marriage.getPartner2().getOpenRequest() != null)
								{
									divorceBy.send(messageAlreadyOpenRequest, marriage.getPartner2());
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
				else if(divorceBy.hasPermission(Permissions.OFFLINEDIVORCE))
				{
					if(!marriage.getPartner1().isOnline() && !marriage.getPartner2().isOnline()) // Both are offline
					{
						if(confirm) // We can't confirm if both are offline!
						{
							// We don't have to test but I don't want to copy the message sending over
							marryOnlineTest(divorceBy.getPlayerOnline(), marriage.getPartner1(), marriage.getPartner2());
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
										divorceBy.send(messageAlreadyOpenRequest, marriage.getPartner1());
									}
									if(marriage.getPartner2().getOpenRequest() != null)
									{
										divorceBy.send(messageAlreadyOpenRequest, marriage.getPartner2());
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
							divorceBy.send(CommonMessages.getMessagePlayerNotOnline(), marriage.getPartner(first).getName());
						}
					}
				}
				else
				{
					// We don't have to test but I don't want to copy the message sending over
					marryOnlineTest(divorceBy.getPlayerOnline(), marriage.getPartner1(), marriage.getPartner2());
				}
			}
			else
			{
				divorceBy.send(CommonMessages.getMessageNoPermission());
			}
		}
	}
	//endregion
}