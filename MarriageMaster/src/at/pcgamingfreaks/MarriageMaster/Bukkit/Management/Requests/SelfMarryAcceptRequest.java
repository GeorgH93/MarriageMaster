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
import at.pcgamingfreaks.Bukkit.Message.Sender.SendMethod;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.AcceptPendingRequest;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.MarriedEvent;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.MarryEvent;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Database.MarriageData;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Management.MarriageManager;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Placeholder.Placeholders;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SelfMarryAcceptRequest extends AcceptPendingRequest
{
	private static Message messageSelfBroadcastMarriage, messageSelfMarried, messageSelfPlayerCalledOff, messageSelfYouCalledOff, messageSelfPlayerMarryOff;

	public static void loadMessages(MarriageMaster plugin)
	{
		messageSelfMarried           = plugin.getLanguage().getMessage("Ingame.Marry.Self.Married").placeholders(Placeholders.PLAYER_NAME);
		messageSelfYouCalledOff      = plugin.getLanguage().getMessage("Ingame.Marry.Self.YouCalledOff");
		messageSelfPlayerMarryOff    = plugin.getLanguage().getMessage("Ingame.Marry.Self.PlayerOff").placeholders(Placeholders.PLAYER_NAME);
		messageSelfPlayerCalledOff   = plugin.getLanguage().getMessage("Ingame.Marry.Self.PlayerCalledOff").placeholders(Placeholders.PLAYER_NAME);
		messageSelfBroadcastMarriage = plugin.getLanguage().getMessage("Ingame.Marry.Self.Broadcast").placeholders(Placeholders.PLAYER1_NAME).placeholders(Placeholders.PLAYER2_NAME);
		if (!plugin.getConfiguration().isMarryAnnouncementEnabled()) messageSelfBroadcastMarriage.setSendMethod(SendMethod.DISABLED);
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
			player2.send(messageSelfMarried, player1);
			player1.send(messageSelfMarried, player2);
			messageSelfBroadcastMarriage.broadcast(player1, player2);
			MarriageMaster.getInstance().getServer().getPluginManager().callEvent(new MarriedEvent(marriage));
		}
	}

	@Override
	public void onDeny()
	{
		if(getPlayersThatCanCancel().length == 0 || !getPlayersThatCanCancel()[0].isOnline() || !getPlayerThatHasToAccept().isOnline()) return;
		MarriagePlayer p1 = getPlayersThatCanCancel()[0], p2 = getPlayerThatHasToAccept();
		p2.send(messageSelfYouCalledOff);
		p1.send(messageSelfPlayerCalledOff, p2);
	}

	@Override
	public void onCancel(@NotNull MarriagePlayer player)
	{
		if(getPlayersThatCanCancel().length == 0 || !getPlayersThatCanCancel()[0].isOnline() || !getPlayerThatHasToAccept().isOnline()) return;
		MarriagePlayer p1 = getPlayersThatCanCancel()[0], p2 = getPlayerThatHasToAccept();
		p1.send(messageSelfYouCalledOff);
		p2.send(messageSelfPlayerCalledOff, p1);
	}

	@Override
	protected void onDisconnect(@NotNull MarriagePlayer player)
	{
		if(player.equals(getPlayersThatCanCancel()[0]))
		{
			getPlayerThatHasToAccept().send(messageSelfPlayerMarryOff, player);
		}
		else
		{
			getPlayersThatCanCancel()[0].send(messageSelfPlayerMarryOff, player);
		}
	}
}