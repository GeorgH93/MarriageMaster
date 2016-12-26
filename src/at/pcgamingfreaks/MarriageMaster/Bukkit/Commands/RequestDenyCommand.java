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
import at.pcgamingfreaks.MarriageMaster.API.HelpData;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarryCommand;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RequestDenyCommand extends MarryCommand
{
	private final Message messageNothingToDeny;

	public RequestDenyCommand(MarriageMaster plugin)
	{
		super(plugin, "deny", plugin.getLanguage().getTranslated("Commands.Description.Deny"), null, true, plugin.getLanguage().getCommandAliases("Deny"));

		messageNothingToDeny = plugin.getLanguage().getMessage("Ingame.Requests.NothingToDeny");
	}

	@Override
	public void execute(@NotNull final CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		MarriagePlayer player = getMarriagePlugin().getPlayerData((Player) sender);
		if(player.getOpenRequest() != null)
		{
			player.getOpenRequest().deny(player);
		}
		else
		{
			messageNothingToDeny.send(sender);
		}
	}

	@Override
	public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		return null;
	}

	@Override
	public List<HelpData> getHelp(@NotNull CommandSender requester)
	{
		MarriagePlayer player = getMarriagePlugin().getPlayerData((Player) requester);
		if(player.getOpenRequest() != null)
		{
			super.getHelp(requester);
		}
		return null;
	}
}