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
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.MarriedEvent;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.MarryEvent;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Database.MarriageData;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Management.MarriageManager;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SelfMarryAcceptRequest extends AcceptPendingRequest
{
	private static Message messageSelfBroadcastMarriage, messageSelfMarried, messageSelfPlayerCalledOff, messageSelfYouCalledOff, messageSelfPlayerMarryOff;

	public static void loadMessages(MarriageMaster plugin)
	{
		messageSelfMarried           = plugin.getLanguage().getMessage("Ingame.Marry.Self.Married").replaceAll("\\{Name\\}", "%1\\$s").replaceAll("\\{DisplayName\\}", "%2\\$s");
		messageSelfYouCalledOff      = plugin.getLanguage().getMessage("Ingame.Marry.Self.YouCalledOff");
		messageSelfPlayerMarryOff    = plugin.getLanguage().getMessage("Ingame.Marry.Self.PlayerOff").replaceAll("\\{Name\\}", "%1\\$s").replaceAll("\\{DisplayName\\}", "%2\\$s");
		messageSelfPlayerCalledOff   = plugin.getLanguage().getMessage("Ingame.Marry.Self.PlayerCalledOff").replaceAll("\\{Name\\}", "%1\\$s").replaceAll("\\{DisplayName\\}", "%2\\$s");
		messageSelfBroadcastMarriage = plugin.getLanguage().getMessage("Ingame.Marry.Self.Broadcast").replaceAll("\\{Player1Name\\}", "%1\\$s").replaceAll("\\{Player1DisplayName\\}", "%2\\$s").replaceAll("\\{Player2Name\\}", "%3\\$s").replaceAll("\\{Player2DisplayName\\}", "%4\\$s");
	}

	public static void unLoadMessages()
	{
		messageSelfBroadcastMarriage = messageSelfMarried = messageSelfPlayerCalledOff = messageSelfYouCalledOff = messageSelfPlayerMarryOff = null;
	}

	private final MarriageManager manager;
	private final String surname;

	public SelfMarryAcceptRequest(@NotNull MarriageManager manager, @NotNull MarriagePlayer hasToAccept, @NotNull MarriagePlayer sender, @Nullable String surname)
	{
		super(hasToAccept, sender);
		this.surname = surname;
		this.manager = manager;
	}

	@Override
	public void onAccept()
	{
		if(getPlayersThatCanCancel().length == 0 || !getPlayersThatCanCancel()[0].isOnline() || !getPlayerThatHasToAccept().isOnline()) return;
		MarriagePlayer player1 = getPlayersThatCanCancel()[0], player2 = getPlayerThatHasToAccept();
		MarryEvent marryEvent = new MarryEvent(player1, player2, player1.getPlayerOnline(), surname);
		Bukkit.getPluginManager().callEvent(marryEvent);
		if(!marryEvent.isCancelled())
		{
			MarriageData marriage = new MarriageData(getPlayersThatCanCancel()[0], getPlayerThatHasToAccept(), getPlayersThatCanCancel()[0], surname);
			MarriageMaster.getInstance().getDatabase().cachedMarry(marriage);
			player2.send(messageSelfMarried, player1.getName(), player1.getDisplayName());
			player1.send(messageSelfMarried, player2.getName(), player2.getDisplayName());
			if(manager.isAnnounceMarriageEnabled())
			{
				messageSelfBroadcastMarriage.broadcast(player1.getName(), player1.getDisplayName(), player2.getName(), player2.getDisplayName());
			}
			MarriageMaster.getInstance().getServer().getPluginManager().callEvent(new MarriedEvent(marriage));
		}
	}

	@Override
	public void onDeny()
	{
		if(getPlayersThatCanCancel().length == 0 || !getPlayersThatCanCancel()[0].isOnline() || !getPlayerThatHasToAccept().isOnline()) return;
		MarriagePlayer p1 = getPlayersThatCanCancel()[0], p2 = getPlayerThatHasToAccept();
		p2.send(messageSelfYouCalledOff);
		p1.send(messageSelfPlayerCalledOff, p2.getName(), p2.getDisplayName());
	}

	@Override
	public void onCancel(@NotNull MarriagePlayer player)
	{
		if(getPlayersThatCanCancel().length == 0 || !getPlayersThatCanCancel()[0].isOnline() || !getPlayerThatHasToAccept().isOnline()) return;
		MarriagePlayer p1 = getPlayersThatCanCancel()[0], p2 = getPlayerThatHasToAccept();
		p1.send(messageSelfYouCalledOff);
		p2.send(messageSelfPlayerCalledOff, p1.getName(), p1.getDisplayName());
	}

	@Override
	protected void onDisconnect(@NotNull MarriagePlayer player)
	{
		if(player.equals(getPlayersThatCanCancel()[0]))
		{
			getPlayerThatHasToAccept().send(messageSelfPlayerMarryOff, player.getName(), player.getDisplayName());
		}
		else
		{
			getPlayersThatCanCancel()[0].send(messageSelfPlayerMarryOff, player.getName(), player.getDisplayName());
		}
	}
}