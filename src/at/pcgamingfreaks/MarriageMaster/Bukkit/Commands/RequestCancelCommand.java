/*
 * Copyright (C) 2016 GeorgH93
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.MarriageMaster.Bukkit.Commands;

import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.AcceptPendingRequest;
import at.pcgamingfreaks.MarriageMaster.API.HelpData;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarryCommand;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class RequestCancelCommand extends MarryCommand
{
	private final String param;
	private final Message messageNothingToCancel;

	public RequestCancelCommand(MarriageMaster plugin)
	{
		super(plugin, "cancel", plugin.getLanguage().getTranslated("Commands.Description.Cancel"), null, true, plugin.getLanguage().getCommandAliases("Cancel"));

		param = "<" + plugin.helpPlayerNameVariable + " / " + plugin.getCommandManager().getAllSwitchTranslation() + ">";
		messageNothingToCancel = plugin.getLanguage().getMessage("Ingame.Requests.NothingToCancel");
	}

	@Override
	public void execute(@NotNull final CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		MarriagePlayer player = getMarriagePlugin().getPlayerData((Player) sender);
		if(player.getRequestsToCancel().size() > 0)
		{
			if(getMarriagePlugin().getCommandManager().isAllSwitch(args[0]))
			{
				for(AcceptPendingRequest request : player.getRequestsToCancel())
				{
					request.cancel(player);
				}
			}
			else
			{
				if(player.getRequestsToCancel().size() > 1)
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
				else
				{
					player.getRequestsToCancel().get(0).cancel(player);
				}
			}
		}
		else
		{
			messageNothingToCancel.send(sender);
		}
	}

	@Override
	public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		MarriagePlayer player = getMarriagePlugin().getPlayerData((Player) sender);
		if(player.getRequestsToCancel().size() > 1)
		{
			List<String> tab = new LinkedList<>();
			String arg = args[0].toLowerCase();
			for(AcceptPendingRequest request : player.getRequestsToCancel())
			{
				if(request.getPlayerThatHasToAccept().getPlayer().getName().toLowerCase().startsWith(arg))
				{
					tab.add(request.getPlayerThatHasToAccept().getPlayer().getName());
				}
			}
			return tab;
		}
		return null;
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
			List<HelpData> help = new LinkedList<>();
			help.add(new HelpData(getTranslatedName(), param, getDescription()));
			return help;
		}
		return null;
	}
}