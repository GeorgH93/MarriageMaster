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
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Management.MarriageManager;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.DisplayNamePlaceholderProcessor;

import org.jetbrains.annotations.NotNull;

public class PriestMarryAcceptRequest extends AcceptPendingRequest
{
	private static Message messageYouCalledOff, messagePlayerCalledOff, messageConfirm, messagePriestMarryOff, messagePlayerMarryOff;
	private static String dialogAndDoYouWant, dialogYesIWant, dialogNoIDontWant;

	public static void loadMessages(MarriageMaster plugin)
	{
		dialogAndDoYouWant     = plugin.getLanguage().getDialog("AndDoYouWant").replace("{Player1Name}", "%1$s").replace("{Player1DisplayName}", "%2$s").replace("{Player2Name}", "%3$s").replace("{Player2DisplayName}", "%4$s");
		dialogYesIWant         = plugin.getLanguage().getDialog("YesIWant");
		dialogNoIDontWant      = plugin.getLanguage().getDialog("NoIDontWant");
		messageConfirm         = plugin.getLanguage().getMessage("Ingame.Marry.Confirm").placeholder("PriestName").placeholder("PriestDisplayName", DisplayNamePlaceholderProcessor.INSTANCE).placeholder("PlayerName").placeholder("PlayerDisplayName", DisplayNamePlaceholderProcessor.INSTANCE);
		messageYouCalledOff    = plugin.getLanguage().getMessage("Ingame.Marry.YouCalledOff");
		messagePlayerCalledOff = plugin.getLanguage().getMessage("Ingame.Marry.PlayerCalledOff").placeholder("Name").placeholder("DisplayName", DisplayNamePlaceholderProcessor.INSTANCE);
		messagePriestMarryOff  = plugin.getLanguage().getMessage("Ingame.Marry.PriestOff").placeholder("Name").placeholder("DisplayName", DisplayNamePlaceholderProcessor.INSTANCE);
		messagePlayerMarryOff  = plugin.getLanguage().getMessage("Ingame.Marry.PlayerOff").placeholder("Name").placeholder("DisplayName", DisplayNamePlaceholderProcessor.INSTANCE);
	}

	public static void unLoadMessages()
	{
		dialogAndDoYouWant = dialogYesIWant = dialogNoIDontWant = null;
		messageYouCalledOff = messagePlayerCalledOff = messageConfirm = messagePriestMarryOff = messagePlayerMarryOff = null;
	}

	private final MarriageManager manager;
	private final String surname;
	private final boolean firstPlayer;

	public PriestMarryAcceptRequest(@NotNull MarriageManager manager, @NotNull MarriagePlayer player1, @NotNull MarriagePlayer player2, MarriagePlayer priest, String surname, boolean firstPlayer)
	{
		super(player1, player2, priest);
		this.surname = surname;
		this.firstPlayer = firstPlayer;
		this.manager = manager;
		player1.send(messageConfirm, priest.getName(), priest, player2.getName(), player2);
	}

	@Override
	public void onAccept()
	{
		if(getPlayersThatCanCancel() == null || !getPlayersThatCanCancel()[0].isOnline() || !getPlayersThatCanCancel()[1].isOnline() || !getPlayerThatHasToAccept().isOnline()) return;
		if(manager.isAutoDialogEnabled())
		{
			getPlayerThatHasToAccept().getPlayerOnline().chat(dialogYesIWant);
		}
		if(firstPlayer)
		{
			MarriagePlayer player = getPlayerThatHasToAccept(), otherPlayer = getPlayersThatCanCancel()[0];
			if(manager.isAutoDialogEnabled())
			{
				getPlayersThatCanCancel()[1].getPlayerOnline().chat(String.format(dialogAndDoYouWant, player.getName(), player.getDisplayName(), otherPlayer.getName(), otherPlayer.getDisplayName()));
			}
			MarriageMaster.getInstance().getCommandManager().registerAcceptPendingRequest(new PriestMarryAcceptRequest(manager, getPlayersThatCanCancel()[0], getPlayerThatHasToAccept(), getPlayersThatCanCancel()[1], surname, false));
		}
		else
		{
			manager.marryPriestFinish(getPlayersThatCanCancel()[0], getPlayerThatHasToAccept(), getPlayersThatCanCancel()[1].getPlayerOnline(), surname);
		}
	}

	@Override
	public void onDeny()
	{
		if(!getPlayersThatCanCancel()[0].isOnline() || !getPlayersThatCanCancel()[1].isOnline() || !getPlayerThatHasToAccept().isOnline()) return;
		MarriagePlayer player = getPlayerThatHasToAccept();
		if(manager.isConfirmEnabled() && manager.isAutoDialogEnabled())
		{
			getPlayerThatHasToAccept().getPlayerOnline().chat(dialogNoIDontWant);
		}
		player.send(messageYouCalledOff);
		getPlayersThatCanCancel()[1].send(messagePlayerCalledOff, player.getName(), player);
		getPlayersThatCanCancel()[0].send(messagePlayerCalledOff, player.getName(), player);
	}

	@Override
	public void onCancel(@NotNull MarriagePlayer player)
	{
		if(getPlayersThatCanCancel() == null || !getPlayersThatCanCancel()[0].isOnline() || !getPlayersThatCanCancel()[1].isOnline() || !getPlayerThatHasToAccept().isOnline()) return;
		player.send(messageYouCalledOff);
		getPlayerThatHasToAccept().send(messagePlayerCalledOff, player.getName(), player);
		((player.equals(getPlayersThatCanCancel()[0])) ? getPlayersThatCanCancel()[1] : getPlayersThatCanCancel()[0]).send(messagePlayerCalledOff, player.getName(), player);
	}

	@Override
	protected void onDisconnect(@NotNull MarriagePlayer player)
	{
		if(player.equals(getPlayersThatCanCancel()[1]))
		{
			getPlayerThatHasToAccept().send(messagePriestMarryOff, player.getName(), player);
			getPlayersThatCanCancel()[0].send(messagePriestMarryOff, player.getName(), player);
		}
		else if(player.equals(getPlayersThatCanCancel()[0]))
		{
			getPlayerThatHasToAccept().send(messagePlayerMarryOff, player.getName(), player);
			getPlayersThatCanCancel()[1].send(messagePlayerMarryOff, player.getName(), player);
		}
		else
		{
			getPlayersThatCanCancel()[0].send(messagePlayerMarryOff, player.getName(), player);
			getPlayersThatCanCancel()[1].send(messagePlayerMarryOff, player.getName(), player);
		}
	}
}