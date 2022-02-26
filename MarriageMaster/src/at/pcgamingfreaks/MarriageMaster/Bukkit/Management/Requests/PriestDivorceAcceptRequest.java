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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Management.Requests;

import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.AcceptPendingRequest;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Management.MarriageManager;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.DisplayNamePlaceholderProcessor;

import org.jetbrains.annotations.NotNull;

public class PriestDivorceAcceptRequest extends AcceptPendingRequest
{
	private static Message messageDivorcePlayerOff, messageDivorcePriestOff, messageDivorceDeny, messageDivorceYouDeny, messageDivorceConfirm, messageDivorcePlayerCancelled,
			messageDivorcePriestCancelled, messageDivorceYouCancelled, messageDivorceYouCancelledPriest;

	public static void loadMessages(MarriageMaster plugin)
	{
		messageDivorceDeny               = plugin.getLanguage().getMessage("Ingame.Divorce.Deny").placeholder("Name").placeholder("DisplayName", DisplayNamePlaceholderProcessor.INSTANCE);
		messageDivorceYouDeny            = plugin.getLanguage().getMessage("Ingame.Divorce.YouDeny").placeholder("Name").placeholder("DisplayName", DisplayNamePlaceholderProcessor.INSTANCE);
		messageDivorceConfirm            = plugin.getLanguage().getMessage("Ingame.Divorce.Confirm").placeholder("PriestName").placeholder("PriestDisplayName", DisplayNamePlaceholderProcessor.INSTANCE).placeholder("PartnerName").placeholder("PartnerDisplayName", DisplayNamePlaceholderProcessor.INSTANCE);
		messageDivorcePlayerOff          = plugin.getLanguage().getMessage("Ingame.Divorce.PlayerOff").placeholder("Name").placeholder("DisplayName", DisplayNamePlaceholderProcessor.INSTANCE);
		messageDivorcePriestOff          = plugin.getLanguage().getMessage("Ingame.Divorce.PriestOff").placeholder("Name").placeholder("DisplayName", DisplayNamePlaceholderProcessor.INSTANCE);
		messageDivorcePriestCancelled    = plugin.getLanguage().getMessage("Ingame.Divorce.PriestCancelled").placeholder("PriestName").placeholder("PriestDisplayName", DisplayNamePlaceholderProcessor.INSTANCE).placeholder("PartnerName").placeholder("PartnerDisplayName", DisplayNamePlaceholderProcessor.INSTANCE);
		messageDivorcePlayerCancelled    = plugin.getLanguage().getMessage("Ingame.Divorce.PlayerCancelled").placeholder("PlayerName").placeholder("PlayerDisplayName", DisplayNamePlaceholderProcessor.INSTANCE).placeholder("PartnerName").placeholder("PartnerDisplayName", DisplayNamePlaceholderProcessor.INSTANCE);
		messageDivorceYouCancelled       = plugin.getLanguage().getMessage("Ingame.Divorce.YouCancelled").placeholder("Name").placeholder("DisplayName", DisplayNamePlaceholderProcessor.INSTANCE);
		messageDivorceYouCancelledPriest = plugin.getLanguage().getMessage("Ingame.Divorce.YouCancelledPriest").placeholder("Player1Name").placeholder("Player1DisplayName", DisplayNamePlaceholderProcessor.INSTANCE).placeholder("Player2Name").placeholder("Player2DisplayName", DisplayNamePlaceholderProcessor.INSTANCE);
	}

	public static void unLoadMessages()
	{
		messageDivorcePlayerOff = messageDivorcePriestOff = messageDivorceDeny = messageDivorceYouDeny = messageDivorceConfirm = messageDivorcePlayerCancelled = messageDivorcePriestCancelled = null;
		messageDivorceYouCancelled = messageDivorceYouCancelledPriest = null;
	}
	
	private final boolean first;
	private final Marriage marriage;
	private final MarriagePlayer partner, priest;
	private final MarriageManager manager;

	public PriestDivorceAcceptRequest(@NotNull MarriageManager manager, @NotNull MarriagePlayer hasToAccept, @NotNull MarriagePlayer priest, @NotNull Marriage marriage, boolean first)
	{
		super(hasToAccept, ((marriage.getPartner(hasToAccept).isOnline()) ? new MarriagePlayer[] { marriage.getPartner(hasToAccept), priest } : new MarriagePlayer[] { priest }));
		this.first = first;
		this.marriage = marriage;
		this.priest = priest;
		this.manager = manager;
		partner = marriage.getPartner(hasToAccept);
		hasToAccept.send(messageDivorceConfirm, priest.getName(), priest, partner.getName(), partner);
	}

	@Override
	protected void onAccept()
	{
		if(first)
		{
			MarriageMaster.getInstance().getCommandManager().registerAcceptPendingRequest(new PriestDivorceAcceptRequest(manager, partner, priest, marriage, false));
		}
		else
		{
			manager.priestFinishDivorce(marriage, priest.getPlayerOnline());
		}
	}

	@Override
	protected void onDeny()
	{
		MarriagePlayer player = getPlayerThatHasToAccept();
		if(partner.isOnline()) partner.send(messageDivorceDeny, player.getName(), player);
		priest.send(messageDivorceDeny, player.getName(), player);
		player.send(messageDivorceYouDeny, partner.getName(), partner);
	}

	@Override
	protected void onCancel(@NotNull MarriagePlayer player)
	{
		if(player.equals(priest)) // The priest cancelled the divorce
		{
			getPlayerThatHasToAccept().send(messageDivorcePriestCancelled, player.getName(), player, partner.getName(), partner);
			player.send(messageDivorceYouCancelledPriest, marriage.getPartner1().getName(), marriage.getPartner1(), marriage.getPartner2().getName(), marriage.getPartner2());
		}
		else
		{
			priest.send(messageDivorcePlayerCancelled, player.getName(), player, getPlayerThatHasToAccept().getName(), getPlayerThatHasToAccept());
			player.send(messageDivorceYouCancelled, partner.getName(), partner);
		}
	}

	@Override
	protected void onDisconnect(@NotNull MarriagePlayer player)
	{
		if(player.equals(getPlayerThatHasToAccept()))
		{
			getPlayersThatCanCancel()[0].send(messageDivorcePlayerOff, player.getName(), player);
			getPlayersThatCanCancel()[1].send(messageDivorcePlayerOff, player.getName(), player);
		}
		else if(player.equals(getPlayersThatCanCancel()[0]))
		{
			getPlayerThatHasToAccept().send(messageDivorcePlayerOff, player.getName(), player);
			getPlayersThatCanCancel()[1].send(messageDivorcePlayerOff, player.getName(), player);
		}
		else
		{
			getPlayerThatHasToAccept().send(messageDivorcePriestOff, player.getName(), player);
			getPlayersThatCanCancel()[0].send(messageDivorcePriestOff, player.getName(), player);
		}
	}
}