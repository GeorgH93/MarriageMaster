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
import at.pcgamingfreaks.MarriageMaster.Placeholder.Processors.DisplayNamePlaceholderProcessor;

import org.jetbrains.annotations.NotNull;

public class SelfDivorceAcceptRequest extends AcceptPendingRequest
{
	private static Message messageSelfDivorcePlayerOff, messageSelfDivorceDeny, messageSelfDivorceYouDeny, messageSelfDivorceCancelled, messageSelfDivorceYouCancelled;

	public static void loadMessages(MarriageMaster plugin)
	{
		messageSelfDivorceDeny         = plugin.getLanguage().getMessage("Ingame.Divorce.Self.Deny").placeholders(Placeholders.PLAYER_NAME);
		messageSelfDivorceYouDeny      = plugin.getLanguage().getMessage("Ingame.Divorce.Self.YouDeny").placeholders(Placeholders.PLAYER_NAME);
		messageSelfDivorceCancelled    = plugin.getLanguage().getMessage("Ingame.Divorce.Self.Cancelled").placeholders(Placeholders.PLAYER_NAME);
		messageSelfDivorceYouCancelled = plugin.getLanguage().getMessage("Ingame.Divorce.Self.YouCancelled").placeholders(Placeholders.PLAYER_NAME);
		messageSelfDivorcePlayerOff    = plugin.getLanguage().getMessage("Ingame.Divorce.Self.PlayerOff").placeholders(Placeholders.PLAYER_NAME);
	}

	public static void unLoadMessages()
	{
		messageSelfDivorcePlayerOff = messageSelfDivorceDeny = messageSelfDivorceYouDeny = messageSelfDivorceCancelled = messageSelfDivorceYouCancelled = null;
	}
	
	private final Marriage marriageData;
	private final MarriageManager manager;

	public SelfDivorceAcceptRequest(@NotNull MarriageManager manager, @NotNull MarriagePlayer hasToAccept, @NotNull MarriagePlayer canCancel, @NotNull Marriage marriageData)
	{
		super(hasToAccept, canCancel);
		this.marriageData = marriageData;
		this.manager = manager;
	}

	@Override
	protected void onAccept()
	{
		manager.selfFinishDivorce(marriageData, getPlayersThatCanCancel()[0]);
	}

	@Override
	protected void onDeny()
	{
		getPlayersThatCanCancel()[0].send(messageSelfDivorceDeny, getPlayerThatHasToAccept());
		getPlayerThatHasToAccept().send(messageSelfDivorceYouDeny, getPlayersThatCanCancel()[0]);
	}

	@Override
	protected void onCancel(@NotNull MarriagePlayer player)
	{
		getPlayerThatHasToAccept().send(messageSelfDivorceCancelled, player);
		player.send(messageSelfDivorceYouCancelled, getPlayerThatHasToAccept());
	}

	@Override
	protected void onDisconnect(@NotNull MarriagePlayer player)
	{
		if(player.equals(getPlayersThatCanCancel()[0]))
		{
			getPlayerThatHasToAccept().send(messageSelfDivorcePlayerOff, player);
		}
		else
		{
			getPlayersThatCanCancel()[0].send(messageSelfDivorcePlayerOff, player);
		}
	}
}