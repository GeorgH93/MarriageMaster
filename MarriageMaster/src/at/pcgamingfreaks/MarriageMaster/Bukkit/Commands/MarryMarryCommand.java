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

import at.pcgamingfreaks.Bukkit.Util.Utils;
import at.pcgamingfreaks.Command.HelpData;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarryCommand;
import at.pcgamingfreaks.MarriageMaster.Bukkit.CommonMessages;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Permissions;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MarryMarryCommand extends MarryCommand
{
	private String helpSelf, helpPriest;
	private final String descriptionSelf;

	public MarryMarryCommand(MarriageMaster plugin)
	{
		super(plugin, "marry", plugin.getLanguage().getTranslated("Commands.Description.Marry"), plugin.getLanguage().getCommandAliases("Marry"));

		// Generating help parameters
		String help = "<" + CommonMessages.getHelpPlayerNameVariable() + ">";
		helpPriest = help + " " + help;
		helpSelf   = help;
		if(plugin.isSurnamesEnabled())
		{
			help = plugin.getLanguage().getTranslated("Commands.SurnameVariable");
			if(plugin.isSurnamesForced())
			{
				help = "<" + help + ">";
			}
			else
			{
				help = "(" + help + ")";
			}
			helpPriest += " " + help;
			helpSelf   += " " + help;
		}
		descriptionSelf = plugin.getLanguage().getTranslated("Commands.Description.MarrySelf");
	}

	@Override
	public void execute(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		if(canUse(sender))
		{
			if(args.length > 0 && args.length < 4)
			{
				if(sender instanceof Player) // Is a player
				{
					executePlayer((Player) sender, mainCommandAlias, args);
				}
				else
				{
					// Console can not marry a play even if self marriage is enabled. So we have to do other checks
					executePriest(sender, mainCommandAlias, args);
				}
			}
			else
			{
				showHelp(sender, mainCommandAlias);
			}
		}
		else
		{
			CommonMessages.getMessageNoPermission().send(sender);
		}
	}

	private void executePlayer(final @NotNull Player sender, final @NotNull String mainCommandAlias, final @NotNull String[] args)
	{
		MarriagePlayer player = getMarriagePlugin().getPlayerData(sender);
		if(getMarriagePlugin().isSelfMarriageAllowed()) // Self marriage
		{
			if(args.length == 3 && player.isPriest())
			{
				getMarriagePlugin().getMarriageManager().marry(getMarriagePlugin().getPlayerData(args[0]), getMarriagePlugin().getPlayerData(args[1]), sender, args[2]);
			}
			else if(args.length == 2 && getMarriagePlugin().isSurnamesForced())
			{
				getMarriagePlugin().getMarriageManager().marry(player, getMarriagePlugin().getPlayerData(args[0]), args[1]);
			}
			else if(args.length == 2 && !getMarriagePlugin().isSurnamesForced())
			{
				MarriagePlayer player2 = getMarriagePlugin().getPlayerData(args[1]);
				if(player.isPriest())
				{
					getMarriagePlugin().getMarriageManager().marry(getMarriagePlugin().getPlayerData(args[0]), player2, sender);
				}
				else
				{
					getMarriagePlugin().getMarriageManager().marry(player, getMarriagePlugin().getPlayerData(args[0]), args[1]);
				}
			}
			else if(args.length == 1 && !getMarriagePlugin().isSurnamesForced())
			{
				getMarriagePlugin().getMarriageManager().marry(player, getMarriagePlugin().getPlayerData(args[0]));
			}
			else
			{
				showHelp(sender, mainCommandAlias);
			}
		}
		else // Priest only
		{
			executePriest(sender, mainCommandAlias, args);
		}
	}

	private void executePriest(final @NotNull CommandSender sender, final @NotNull String mainCommandAlias, final @NotNull String[] args)
	{
		if(args.length == 3 && getMarriagePlugin().isSurnamesEnabled())
		{
			getMarriagePlugin().getMarriageManager().marry(getMarriagePlugin().getPlayerData(args[0]), getMarriagePlugin().getPlayerData(args[1]), sender, args[2]);
		}
		else if(args.length == 2 && !getMarriagePlugin().isSurnamesForced())
		{
			getMarriagePlugin().getMarriageManager().marry(getMarriagePlugin().getPlayerData(args[0]), getMarriagePlugin().getPlayerData(args[1]), sender);
		}
		else
		{
			showHelp(sender, mainCommandAlias);
		}
	}

	@Override
	public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		if(args.length == 0 || !canUse(sender)) return EMPTY_TAB_COMPLETE_LIST;
		return Utils.getPlayerNamesStartingWithVisibleOnly(args[args.length - 1], sender, Permissions.BYPASS_VANISH);
	}

	@Override
	public List<HelpData> getHelp(@NotNull CommandSender sender)
	{
		if(canUse(sender))
		{
			List<HelpData> help = new ArrayList<>(2);
			MarriagePlayer marriagePlayerData = (sender instanceof Player) ? getMarriagePlugin().getPlayerData((Player) sender) : null;
			if(marriagePlayerData != null && getMarriagePlugin().isSelfMarriageAllowed() && sender.hasPermission(Permissions.SELF_MARRY))
			{
				help.add(new HelpData(getTranslatedName(), helpSelf, descriptionSelf));
			}
			if(marriagePlayerData == null || marriagePlayerData.isPriest())
			{
				help.add(new HelpData(getTranslatedName(), helpPriest, getDescription()));
			}
			return help;
		}
		else
		{
			return EMPTY_HELP_LIST;
		}
	}

	@Override
	public boolean canUse(@NotNull CommandSender sender)
	{
		if(sender instanceof Player && !(getMarriagePlugin().isSelfMarriageAllowed() && sender.hasPermission(Permissions.SELF_MARRY)))
		{
			MarriagePlayer marriagePlayerData = getMarriagePlugin().getPlayerData((Player) sender);
			return marriagePlayerData.isPriest();
		}
		else
		{
			return true;
		}
	}
}