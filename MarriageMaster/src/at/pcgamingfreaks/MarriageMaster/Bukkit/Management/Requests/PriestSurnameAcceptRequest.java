/*
 *   Copyright (C) 2025 GeorgH93
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
import at.pcgamingfreaks.MarriageMaster.Bukkit.Management.MarriageManagerImpl;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Bukkit.SurnameConfirmationMode;
import at.pcgamingfreaks.MarriageMaster.Placeholder.Placeholders;

import org.jetbrains.annotations.NotNull;

public class PriestSurnameAcceptRequest extends AcceptPendingRequest
{
	private static Message PlayerOff, PriestOff, Deny, YouDeny, Confirm, PlayerCancelled, PriestCancelled, YouCancelled, YouCancelledPriest;

	public static void loadMessages(MarriageMaster plugin)
	{
		Deny               = plugin.getLanguage().getMessage("Ingame.Surname.Deny").placeholders(Placeholders.PLAYER_NAME).placeholder("Surname").placeholder("NewSurname");
		YouDeny            = plugin.getLanguage().getMessage("Ingame.Surname.YouDeny").placeholders(Placeholders.PLAYER_NAME).placeholder("Surname").placeholder("NewSurname");
		Confirm            = plugin.getLanguage().getMessage("Ingame.Surname.Confirm").placeholders(Placeholders.PRIEST_NAME).placeholders(Placeholders.PARTNER_NAME).placeholder("Surname").placeholder("NewSurname");
		PlayerOff          = plugin.getLanguage().getMessage("Ingame.Surname.PlayerOff").placeholders(Placeholders.PLAYER_NAME).placeholder("Surname").placeholder("NewSurname");
		PriestOff          = plugin.getLanguage().getMessage("Ingame.Surname.PriestOff").placeholders(Placeholders.PLAYER_NAME).placeholder("Surname").placeholder("NewSurname");
		PriestCancelled    = plugin.getLanguage().getMessage("Ingame.Surname.PriestCancelled").placeholders(Placeholders.PRIEST_NAME).placeholders(Placeholders.PARTNER_NAME).placeholder("Surname").placeholder("NewSurname");
		PlayerCancelled    = plugin.getLanguage().getMessage("Ingame.Surname.PlayerCancelled").placeholders(Placeholders.mkPlayerNameRegex("(Player)?")).placeholders(Placeholders.PARTNER_NAME).placeholder("Surname").placeholder("NewSurname");
		YouCancelled       = plugin.getLanguage().getMessage("Ingame.Surname.YouCancelled").placeholders(Placeholders.PLAYER_NAME).placeholder("Surname").placeholder("NewSurname");
		YouCancelledPriest = plugin.getLanguage().getMessage("Ingame.Surname.YouCancelledPriest").placeholders(Placeholders.PLAYER1_NAME).placeholders(Placeholders.PLAYER2_NAME).placeholder("Surname").placeholder("NewSurname");
	}

	public static void unLoadMessages()
	{
		PlayerOff = PriestOff = Deny = YouDeny = Confirm = PlayerCancelled = PriestCancelled = null;
		YouCancelled = YouCancelledPriest = null;
	}
	
	private final boolean first;
	private final Marriage marriage;
	private final MarriagePlayer partner, priest;
	private final MarriageManagerImpl manager;
	private final String surname, newSurname, newSurnameDisplay;

	public PriestSurnameAcceptRequest(@NotNull MarriageManagerImpl manager, @NotNull MarriagePlayer hasToAccept, @NotNull MarriagePlayer priest, @NotNull Marriage marriage, boolean first, String newSurname)
	{
		super(hasToAccept, ((marriage.getPartner(hasToAccept).isOnline()) ? new MarriagePlayer[] { marriage.getPartner(hasToAccept), priest } : new MarriagePlayer[] { priest }));
		this.first = first;
		this.marriage = marriage;
		this.priest = priest;
		this.manager = manager;
		this.newSurname = newSurname;
		this.newSurnameDisplay = newSurname == null ? "" : newSurname;
		this.surname = marriage.getSurnameString();
		partner = marriage.getPartner(hasToAccept);
		hasToAccept.send(Confirm, priest, partner, surname, newSurnameDisplay);
	}

	@Override
	protected void onAccept()
	{
		if(first && manager.getSurnameConfirmationMode() == SurnameConfirmationMode.BothPlayers)
		{
			MarriageMaster.getInstance().getCommandManager().registerAcceptPendingRequest(new PriestSurnameAcceptRequest(manager, partner, priest, marriage, false, newSurname));
		}
		else
		{
			manager.finishSetSurname(marriage, newSurname, priest.getPlayerOnline());
		}
	}

	@Override
	protected void onDeny()
	{
		MarriagePlayer player = getPlayerThatHasToAccept();
		if(partner.isOnline()) partner.send(Deny, player, surname, newSurnameDisplay);
		priest.send(Deny, player, surname, newSurnameDisplay);
		player.send(YouDeny, partner, surname, newSurnameDisplay);
	}

	@Override
	protected void onCancel(@NotNull MarriagePlayer player)
	{
		if(player.equals(priest)) // The priest cancelled the surname change
		{
			getPlayerThatHasToAccept().send(PriestCancelled, player, partner, surname, newSurnameDisplay);
			player.send(YouCancelledPriest, marriage.getPartner1(), marriage.getPartner2(), surname, newSurnameDisplay);
		}
		else
		{
			priest.send(PlayerCancelled, player, getPlayerThatHasToAccept(), surname, newSurnameDisplay);
			player.send(YouCancelled, partner, surname, newSurnameDisplay);
		}
	}

	@Override
	protected void onDisconnect(@NotNull MarriagePlayer player)
	{
		if(player.equals(getPlayerThatHasToAccept()))
		{
			getPlayersThatCanCancel()[0].send(PlayerOff, player, surname, newSurnameDisplay);
			getPlayersThatCanCancel()[1].send(PlayerOff, player, surname, newSurnameDisplay);
		}
		else if(player.equals(getPlayersThatCanCancel()[0]))
		{
			getPlayerThatHasToAccept().send(PlayerOff, player, surname, newSurnameDisplay);
			getPlayersThatCanCancel()[1].send(PlayerOff, player, surname, newSurnameDisplay);
		}
		else
		{
			getPlayerThatHasToAccept().send(PriestOff, player, surname, newSurnameDisplay);
			getPlayersThatCanCancel()[0].send(PriestOff, player, surname, newSurnameDisplay);
		}
	}
}