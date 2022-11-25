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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Commands;

import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.Command.HelpData;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.AcceptPendingRequest;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarryCommand;
import at.pcgamingfreaks.MarriageMaster.Bukkit.CommonMessages;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RequestCancelCommand extends MarryCommand
{
	private final String param;
	private final Message messageNothingToCancel;

	public RequestCancelCommand(MarriageMaster plugin)
	{
		super(plugin, "cancel", plugin.getLanguage().getTranslated("Commands.Description.Cancel"), null, true, plugin.getLanguage().getCommandAliases("Cancel"));

		param = "<" + CommonMessages.getHelpPlayerNameVariable() + " / " + plugin.getCommandManager().getAllSwitchTranslation() + ">";
		messageNothingToCancel = plugin.getLanguage().getMessage("Ingame.Requests.NothingToCancel");
	}

	@Override
	public void execute(@NotNull final CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		MarriagePlayer player = getMarriagePlugin().getPlayerData((Player) sender);
		if(!player.getRequestsToCancel().isEmpty())
		{ // No open requests
			messageNothingToCancel.send(sender);
			return;
		}

		if (args.length == 0)
		{ // No args, cancel first request
			player.getRequestsToCancel().get(0).cancel(player);
			return;
		}

		if(getMarriagePlugin().getCommandManager().isAllSwitch(args[0]))
		{
			for(AcceptPendingRequest request : player.getRequestsToCancel())
			{
				request.cancel(player);
			}
		}
		else
		{
			MarriagePlayer otherPlayer = getMarriagePlugin().getPlayerData(args[0]);
			for(AcceptPendingRequest request : player.getRequestsToCancel())
			{
				if(request.getPlayerThatHasToAccept().equals(otherPlayer))
				{
					request.cancel(player);
				}
			}
		}
	}

	@Override
	public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		MarriagePlayer player = getMarriagePlugin().getPlayerData((Player) sender);
		if(player.getRequestsToCancel().size() > 1)
		{
			List<String> tab = new ArrayList<>(player.getRequestsToCancel().size());
			String arg = args[0].toLowerCase();
			for(AcceptPendingRequest request : player.getRequestsToCancel())
			{
				if(request.getPlayerThatHasToAccept().getName().toLowerCase().startsWith(arg))
				{
					tab.add(request.getPlayerThatHasToAccept().getName());
				}
			}
			return tab;
		}
		return EMPTY_TAB_COMPLETE_LIST;
	}

	@Override
	public List<HelpData> getHelp(@NotNull CommandSender requester)
	{
		MarriagePlayer player = getMarriagePlugin().getPlayerData((Player) requester);
		if(player.getRequestsToCancel().size() == 1)
		{
			super.getHelp(requester);
		}
		else if(player.getRequestsToCancel().size() > 1)
		{
			List<HelpData> help = new ArrayList<>(1);
			help.add(new HelpData(getTranslatedName(), param, getDescription()));
			return help;
		}
		return EMPTY_HELP_LIST;
	}
}