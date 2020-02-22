/*
 *   Copyright (C) 2019 GeorgH93
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
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.MarriageMaster.Bukkit.Commands;

import at.pcgamingfreaks.Command.HelpData;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarryCommand;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class MarryMarryCommand extends MarryCommand
{
	private String helpSelf, helpPriest, descriptionSelf;

	public MarryMarryCommand(MarriageMaster plugin)
	{
		super(plugin, "marry", plugin.getLanguage().getTranslated("Commands.Description.Marry"), plugin.getLanguage().getCommandAliases("Marry"));

		// Generating help parameters
		String help = "<" + plugin.helpPlayerNameVariable + ">";
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
					MarriagePlayer player = getMarriagePlugin().getPlayerData((Player) sender);
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
				}
				else // Console can not marry a play even if self marriage is enabled. So we have to do other checks
				{
					if((args.length == 2 && !getMarriagePlugin().isSurnamesForced()) || (args.length == 3 && getMarriagePlugin().isSurnamesEnabled()))
					{
						if(args.length == 3)
						{
							getMarriagePlugin().getMarriageManager().marry(getMarriagePlugin().getPlayerData(args[0]), getMarriagePlugin().getPlayerData(args[1]), sender, args[2]);
						}
						else
						{
							getMarriagePlugin().getMarriageManager().marry(getMarriagePlugin().getPlayerData(args[0]), getMarriagePlugin().getPlayerData(args[1]), sender);
						}
					}
					else
					{
						showHelp(sender, mainCommandAlias);
					}
				}
			}
			else
			{
				showHelp(sender, mainCommandAlias);
			}
		}
		else
		{
			((MarriageMaster) getMarriagePlugin()).messageNoPermission.send(sender);
		}
	}

	@Override
	public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		if(args.length == 0 || !canUse(sender)) return null;
		List<String> names = new LinkedList<>();
		String arg = args[args.length - 1].toLowerCase();
		for(Player player : Bukkit.getOnlinePlayers())
		{
			if(!player.getName().equals(sender.getName()) && player.getName().toLowerCase().startsWith(arg))
			{
				names.add(player.getName());
			}
		}
		return names;
	}

	@Override
	public List<HelpData> getHelp(@NotNull CommandSender sender)
	{
		if(canUse(sender))
		{
			List<HelpData> help = new LinkedList<>();
			MarriagePlayer marriagePlayerData = (sender instanceof Player) ? getMarriagePlugin().getPlayerData((Player) sender) : null;
			if(sender instanceof Player && getMarriagePlugin().isSelfMarriageAllowed() && sender.hasPermission("marry.selfmarry"))
			{
				help.add(new HelpData(getTranslatedName(), helpSelf, descriptionSelf));
			}
			if(!(sender instanceof Player) || marriagePlayerData.isPriest())
			{
				help.add(new HelpData(getTranslatedName(), helpPriest, getDescription()));
			}
			return help;
		}
		else
		{
			return null;
		}
	}

	@Override
	public boolean canUse(@NotNull CommandSender sender)
	{
		if(sender instanceof Player && !(getMarriagePlugin().isSelfMarriageAllowed() && sender.hasPermission("marry.selfmarry")))
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