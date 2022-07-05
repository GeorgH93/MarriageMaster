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
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarryCommand;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Database.MarriageData;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Permissions;
import at.pcgamingfreaks.Message.MessageColor;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SetColorCommand extends MarryCommand
{
	private final Message messageUnknownColor, messageColorSet, messageColorSelection;

	public SetColorCommand(final @NotNull MarriageMaster plugin)
	{
		super(plugin, "setcolor", plugin.getLanguage().getTranslated("Commands.Description.SetColor"), Permissions.SET_COLOR, true, true, plugin.getLanguage().getCommandAliases("SetColor"));

		messageUnknownColor = plugin.getLanguage().getMessage("Ingame.SetColor.UnknownColor").placeholder("Input");
		messageColorSet = plugin.getLanguage().getMessage("Ingame.SetColor.Set");
		messageColorSelection = plugin.getLanguage().getMessage("Ingame.SetColor.Selection").placeholder("MainCmdAlias").placeholder("SubCmdAlias");
	}

	@Override
	public void execute(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		MarriagePlayer player = getMarriagePlugin().getPlayerData((Player) sender);
		if(args.length < 1)
		{
			if(player.getPartners().size() > 1) showHelp(sender, mainCommandAlias);
			else messageColorSelection.send(sender, mainCommandAlias, alias);
			return;
		}
		Marriage marriage;
		if(getMarriagePlugin().areMultiplePartnersAllowed() && args.length == 2)
		{
			marriage = player.getMarriageData(getMarriagePlugin().getPlayerData(args[0]));
			if(marriage == null)
			{
				((MarriageMaster) getMarriagePlugin()).messageTargetPartnerNotFound.send(sender);
				return;
			}
		}
		else
		{
			marriage = player.getNearestPartnerMarriageData();
		}

		final String colorArg = args[args.length - 1];
		final MessageColor color = MessageColor.getColor(colorArg); // try to identify the color
		if(color == null)
		{
			messageUnknownColor.send(sender, colorArg);
		}
		else
		{
			//noinspection ConstantConditions
			((MarriageData) marriage).setColor(color);
			messageColorSet.send(sender);
		}
	}

	@Override
	public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		if(args.length != 1 && args.length != 2) return null;
		List<String> complete = getMarriagePlugin().getCommandManager().getSimpleTabComplete(sender, args);
		List<String> colors = MessageColor.getNamesStartingWith(args[args.length - 1]);
		if(complete == null) return colors;
		complete.addAll(colors);
		return complete;
	}
}