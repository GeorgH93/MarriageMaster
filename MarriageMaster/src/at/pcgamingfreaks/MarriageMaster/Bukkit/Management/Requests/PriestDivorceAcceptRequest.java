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
import at.pcgamingfreaks.MarriageMaster.Placeholder.Placeholders;

import org.jetbrains.annotations.NotNull;

public class PriestDivorceAcceptRequest extends AcceptPendingRequest
{
	private static Message messageDivorcePlayerOff, messageDivorcePriestOff, messageDivorceDeny, messageDivorceYouDeny, messageDivorceConfirm, messageDivorcePlayerCancelled,
			messageDivorcePriestCancelled, messageDivorceYouCancelled, messageDivorceYouCancelledPriest;

	public static void loadMessages(MarriageMaster plugin)
	{
		messageDivorceDeny               = plugin.getLanguage().getMessage("Ingame.Divorce.Deny").placeholders(Placeholders.PLAYER_NAME);
		messageDivorceYouDeny            = plugin.getLanguage().getMessage("Ingame.Divorce.YouDeny").placeholders(Placeholders.PLAYER_NAME);
		messageDivorceConfirm            = plugin.getLanguage().getMessage("Ingame.Divorce.Confirm").placeholders(Placeholders.PRIEST_NAME).placeholders(Placeholders.PARTNER_NAME);
		messageDivorcePlayerOff          = plugin.getLanguage().getMessage("Ingame.Divorce.PlayerOff").placeholders(Placeholders.PLAYER_NAME);
		messageDivorcePriestOff          = plugin.getLanguage().getMessage("Ingame.Divorce.PriestOff").placeholders(Placeholders.PLAYER_NAME);
		messageDivorcePriestCancelled    = plugin.getLanguage().getMessage("Ingame.Divorce.PriestCancelled").placeholders(Placeholders.PRIEST_NAME).placeholders(Placeholders.PARTNER_NAME);
		messageDivorcePlayerCancelled    = plugin.getLanguage().getMessage("Ingame.Divorce.PlayerCancelled").placeholders(Placeholders.mkPlayerNameRegex("(Player)?")).placeholders(Placeholders.PARTNER_NAME);
		messageDivorceYouCancelled       = plugin.getLanguage().getMessage("Ingame.Divorce.YouCancelled").placeholders(Placeholders.PLAYER_NAME);
		messageDivorceYouCancelledPriest = plugin.getLanguage().getMessage("Ingame.Divorce.YouCancelledPriest").placeholders(Placeholders.PLAYER1_NAME).placeholders(Placeholders.PLAYER2_NAME);
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
		hasToAccept.send(messageDivorceConfirm, priest, partner);
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
		if(partner.isOnline()) partner.send(messageDivorceDeny, player);
		priest.send(messageDivorceDeny, player);
		player.send(messageDivorceYouDeny, partner);
	}

	@Override
	protected void onCancel(@NotNull MarriagePlayer player)
	{
		if(player.equals(priest)) // The priest cancelled the divorce
		{
			getPlayerThatHasToAccept().send(messageDivorcePriestCancelled, player, partner);
			player.send(messageDivorceYouCancelledPriest, marriage.getPartner1(), marriage.getPartner2());
		}
		else
		{
			priest.send(messageDivorcePlayerCancelled, player, getPlayerThatHasToAccept());
			player.send(messageDivorceYouCancelled, partner);
		}
	}

	@Override
	protected void onDisconnect(@NotNull MarriagePlayer player)
	{
		if(player.equals(getPlayerThatHasToAccept()))
		{
			getPlayersThatCanCancel()[0].send(messageDivorcePlayerOff, player);
			getPlayersThatCanCancel()[1].send(messageDivorcePlayerOff, player);
		}
		else if(player.equals(getPlayersThatCanCancel()[0]))
		{
			getPlayerThatHasToAccept().send(messageDivorcePlayerOff, player);
			getPlayersThatCanCancel()[1].send(messageDivorcePlayerOff, player);
		}
		else
		{
			getPlayerThatHasToAccept().send(messageDivorcePriestOff, player);
			getPlayersThatCanCancel()[0].send(messageDivorcePriestOff, player);
		}
	}
}