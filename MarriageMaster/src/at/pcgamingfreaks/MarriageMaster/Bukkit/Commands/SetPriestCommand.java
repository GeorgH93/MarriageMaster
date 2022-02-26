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
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Permissions;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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

		messageMadeYouAPriest  = plugin.getLanguage().getMessage("Ingame.SetPriest.MadeYouAPriest") .placeholder("Name").placeholder("DisplayName");
		messageFiredYou        = plugin.getLanguage().getMessage("Ingame.SetPriest.FiredYou")       .placeholder("Name").placeholder("DisplayName");
		messageYouMadeAPriest  = plugin.getLanguage().getMessage("Ingame.SetPriest.YouMadeAPriest") .placeholder("Name").placeholder("DisplayName");
		messageYouFiredAPriest = plugin.getLanguage().getMessage("Ingame.SetPriest.YouFiredAPriest").placeholder("Name").placeholder("DisplayName");
		messagePerPermission   = plugin.getLanguage().getMessage("Ingame.SetPriest.PerPermission")  .placeholder("Name").placeholder("DisplayName");

		helpParam = "<" + plugin.helpPlayerNameVariable + ">";
	}

	@Override
	public void execute(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		if(args.length > 0)
		{
			Player bTarget;
			MarriagePlayer target;
			for(String arg : args)
			{
				bTarget = Bukkit.getPlayer(arg);
				if(bTarget != null)
				{
					if(bTarget.hasPermission(Permissions.PRIEST))
					{
						messagePerPermission.send(sender, bTarget.getName(), bTarget.getDisplayName()); //TODO make format provider
					}
					else
					{
						target = getMarriagePlugin().getPlayerData(bTarget);
						if(target.isPriest())
						{
							messageYouFiredAPriest.send(sender, bTarget.getName(), bTarget.getDisplayName());
							messageFiredYou.send(bTarget, bTarget.getName(), bTarget.getDisplayName());
							target.setPriest(false);
						}
						else
						{
							messageYouMadeAPriest.send(sender, bTarget.getName(), bTarget.getDisplayName());
							messageMadeYouAPriest.send(bTarget, bTarget.getName(), bTarget.getDisplayName());
							target.setPriest(true);
						}
					}
				}
				else
				{
					((MarriageMaster) getMarriagePlugin()).messagePlayerNotOnline.send(sender, arg);
				}
			}
		}
		else
		{
			showHelp(sender, mainCommandAlias);
		}
	}

	@Override
	public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		if(args.length > 0)
		{
			String name, arg = args[args.length - 1].toLowerCase(Locale.ENGLISH);
			List<String> names = new LinkedList<>();
			for(Player player : Bukkit.getOnlinePlayers())
			{
				name = player.getName().toLowerCase(Locale.ENGLISH);
				if(!name.equalsIgnoreCase(sender.getName()) && name.startsWith(arg))
				{
					names.add(name);
				}
			}
			return names;
		}
		return null;
	}

	@Override
	public List<HelpData> getHelp(@NotNull CommandSender requester)
	{
		List<HelpData> help = new LinkedList<>();
		help.add(new HelpData(getTranslatedName(), helpParam, getDescription()));
		return help;
	}
}