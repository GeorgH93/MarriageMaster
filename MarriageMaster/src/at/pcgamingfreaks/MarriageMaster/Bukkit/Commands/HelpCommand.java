/*
 *   Copyright (C) 2021 GeorgH93
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

import at.pcgamingfreaks.Bukkit.Command.SubCommand;
import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.Command.HelpData;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarryCommand;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class HelpCommand extends MarryCommand
{
	private Collection<MarryCommand> commands;
	private final Message messageHeader, messageFooter;

	public HelpCommand(final @NotNull MarriageMaster plugin, final @NotNull Collection<MarryCommand> commands)
	{
		super(plugin, "help", plugin.getLanguage().getTranslated("Commands.Description.Help"), plugin.getLanguage().getCommandAliases("Help"));
		this.commands = commands;

		messageHeader = plugin.getLanguage().getMessage("Ingame.Help.Header");
		messageFooter = plugin.getLanguage().getMessage("Ingame.Help.Footer");
	}

	@Override
	public void execute(final @NotNull CommandSender sender, final @NotNull String mainCommandAlias, final @NotNull String alias, final @NotNull String[] args)
	{
		messageHeader.send(sender);
		Collection<HelpData> help = new ArrayList<>(), temp;
		for(SubCommand cmd : commands)
		{
			temp = cmd.doGetHelp(sender);
			if(temp != null) help.addAll(temp);
		}
		((CommandManagerImplementation) getMarriagePlugin().getCommandManager()).sendHelp(sender, mainCommandAlias, help);
		messageFooter.send(sender);
		//TODO: pages
	}

	@Override
	public List<String> tabComplete(final @NotNull CommandSender sender, final @NotNull String mainCommandAlias, final @NotNull String alias, final @NotNull String[] args)
	{
		return null;
	}

	@Override
	public void close()
	{
		commands = null;
	}
}