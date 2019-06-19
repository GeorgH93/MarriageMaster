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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Management.Requests;

import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.AcceptPendingRequest;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Management.MarriageManager;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;

import org.jetbrains.annotations.NotNull;

public class PriestDivorceAcceptRequest extends AcceptPendingRequest
{
	private static Message messageDivorcePlayerOff, messageDivorcePriestOff, messageDivorceDeny, messageDivorceYouDeny, messageDivorceConfirm, messageDivorcePlayerCancelled,
			messageDivorcePriestCancelled, messageDivorceYouCancelled, messageDivorceYouCancelledPriest;

	public static void loadMessages(MarriageMaster plugin)
	{
		messageDivorceDeny               = plugin.getLanguage().getMessage("Ingame.Divorce.Deny").replaceAll("\\{Name\\}", "%1\\$s").replaceAll("\\{DisplayName\\}", "%2\\$s");
		messageDivorceYouDeny            = plugin.getLanguage().getMessage("Ingame.Divorce.YouDeny").replaceAll("\\{Name\\}", "%1\\$s").replaceAll("\\{DisplayName\\}", "%2\\$s");
		messageDivorceConfirm            = plugin.getLanguage().getMessage("Ingame.Divorce.Confirm").replaceAll("\\{PriestName\\}", "%1\\$s").replaceAll("\\{PriestDisplayName\\}", "%2\\$s").replaceAll("\\{PartnerName\\}", "%3\\$s").replaceAll("\\{PartnerDisplayName\\}", "%4\\$s");
		messageDivorcePlayerOff          = plugin.getLanguage().getMessage("Ingame.Divorce.PlayerOff").replaceAll("\\{Name\\}", "%1\\$s").replaceAll("\\{DisplayName\\}", "%2\\$s");
		messageDivorcePriestOff          = plugin.getLanguage().getMessage("Ingame.Divorce.PriestOff").replaceAll("\\{Name\\}", "%1\\$s").replaceAll("\\{DisplayName\\}", "%2\\$s");
		messageDivorcePriestCancelled    = plugin.getLanguage().getMessage("Ingame.Divorce.PriestCancelled").replaceAll("\\{PriestName\\}", "%1\\$s").replaceAll("\\{PriestDisplayName\\}", "%2\\$s").replaceAll("\\{PartnerName\\}", "%3\\$s").replaceAll("\\{PartnerDisplayName\\}", "%4\\$s");
		messageDivorcePlayerCancelled    = plugin.getLanguage().getMessage("Ingame.Divorce.PlayerCancelled").replaceAll("\\{PlayerName\\}", "%1\\$s").replaceAll("\\{PlayerDisplayName\\}", "%2\\$s").replaceAll("\\{PartnerName\\}", "%3\\$s").replaceAll("\\{PartnerDisplayName\\}", "%4\\$s");
		messageDivorceYouCancelled       = plugin.getLanguage().getMessage("Ingame.Divorce.YouCancelled").replaceAll("\\{Name\\}", "%1\\$s").replaceAll("\\{DisplayName\\}", "%2\\$s");
		messageDivorceYouCancelledPriest = plugin.getLanguage().getMessage("Ingame.Divorce.YouCancelledPriest").replaceAll("\\{Player1Name\\}", "%1\\$s").replaceAll("\\{Player1DisplayName\\}", "%2\\$s").replaceAll("\\{Player2Name\\}", "%3\\$s").replaceAll("\\{Player2DisplayName\\}", "%4\\$s");
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
		hasToAccept.send(messageDivorceConfirm, priest.getName(), priest.getDisplayName(), partner.getName(), partner.getDisplayName());
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