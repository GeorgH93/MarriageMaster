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
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarryCommand;
import at.pcgamingfreaks.MarriageMaster.Bukkit.CommonMessages;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Management.MarriageManagerImpl;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Permissions;
import at.pcgamingfreaks.MarriageMaster.Placeholder.Placeholders;
import at.pcgamingfreaks.Message.Placeholder.Processors.FormattedStringPlaceholderProcessor;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import scala.concurrent.impl.FutureConvertersImpl;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class SetPriestCommand extends MarryCommand
{
	private final Message messageMadeYouAPriest, messageFiredYou, messageYouMadeAPriest, messageYouFiredAPriest, messagePerPermission;
	private final String helpParam;

	public SetPriestCommand(MarriageMaster plugin)
	{
		super(plugin, "setpriest", plugin.getLanguage().getTranslated("Commands.Description.SetPriest"), Permissions.SET_PRIEST, plugin.getLanguage().getCommandAliases("SetPriest"));

		messageMadeYouAPriest  = plugin.getLanguage().getMessage("Ingame.SetPriest.MadeYouAPriest") .placeholder("Name").placeholder("DisplayName", FormattedStringPlaceholderProcessor.INSTANCE);
		messageFiredYou        = plugin.getLanguage().getMessage("Ingame.SetPriest.FiredYou")       .placeholder("Name").placeholder("DisplayName", FormattedStringPlaceholderProcessor.INSTANCE);
		messageYouMadeAPriest  = plugin.getLanguage().getMessage("Ingame.SetPriest.YouMadeAPriest") .placeholders(Placeholders.PLAYER_NAME);
		messageYouFiredAPriest = plugin.getLanguage().getMessage("Ingame.SetPriest.YouFiredAPriest").placeholders(Placeholders.PLAYER_NAME);
		messagePerPermission   = plugin.getLanguage().getMessage("Ingame.SetPriest.PerPermission")  .placeholders(Placeholders.PLAYER_NAME);

		helpParam = "<" + CommonMessages.getHelpPlayerNameVariable() + ">";
	}

	private String getSenderDisplayName(final @NotNull CommandSender sender)
	{
		if (sender instanceof Player)
		{
			return ((Player) sender).getDisplayName();
		}
		return MarriageManagerImpl.CONSOLE_DISPLAY_NAME;
	}

	@Override
	public void execute(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		if(args.length == 0)
		{
			showHelp(sender, mainCommandAlias);
			return;
		}
		String senderName = sender.getName(), senderDisplayName = getSenderDisplayName(sender);
		for(String arg : args)
		{
			Player bTarget = Bukkit.getPlayer(arg);
			if(bTarget == null)
			{
				CommonMessages.getMessagePlayerNotOnline().send(sender, arg);
				continue;
			}

			MarriagePlayer target = getMarriagePlugin().getPlayerData(bTarget);
			if(bTarget.hasPermission(Permissions.PRIEST))
			{
				messagePerPermission.send(sender, target);
			}
			else
			{
				if(target.isPriest())
				{
					messageYouFiredAPriest.send(sender, target);
					messageFiredYou.send(bTarget, senderName, senderDisplayName);
					target.setPriest(false);
				}
				else
				{
					messageYouMadeAPriest.send(sender, target);
					messageMadeYouAPriest.send(bTarget, senderName, senderDisplayName);
					target.setPriest(true);
				}
			}
		}
	}

	@Override
	public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		List<String> names = new LinkedList<>();
		if(args.length > 0)
		{
			String name, arg = args[args.length - 1].toLowerCase(Locale.ENGLISH);
			for(Player player : Bukkit.getOnlinePlayers())
			{
				name = player.getName().toLowerCase(Locale.ENGLISH);
				if(!name.equalsIgnoreCase(sender.getName()) && name.startsWith(arg))
				{
					names.add(name);
				}
			}
		}
		return names;
	}

	@Override
	public List<HelpData> getHelp(@NotNull CommandSender requester)
	{
		List<HelpData> help = new LinkedList<>();
		help.add(new HelpData(getTranslatedName(), helpParam, getDescription()));
		return help;
	}
}
