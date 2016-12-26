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

import at.pcgamingfreaks.MarriageMaster.API.HelpData;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarryCommand;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class MarryDivorceCommand extends MarryCommand
{
	private String descriptionSelf, helpPriest, helpParam;

	public MarryDivorceCommand(MarriageMaster plugin)
	{
		super(plugin, "divorce", plugin.getLanguage().getTranslated("Commands.Description.Divorce"), plugin.getLanguage().getCommandAliases("Divorce"));

		descriptionSelf = plugin.getLanguage().getTranslated("Commands.Description.DivorceSelf");
		helpParam = "<" + plugin.helpPartnerNameVariable + ">";
		helpPriest = helpParam;
		if(plugin.isPolygamyAllowed())
		{
			helpPriest += " " + helpParam;
		}
	}

	@Override
	public void execute(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		MarriagePlayer player = (sender instanceof Player) ? getMarriagePlugin().getPlayerData((Player) sender) : null;
		if(!(sender instanceof Player) || player.isPriest() || (getMarriagePlugin().isSelfDivorceAllowed() && player.isMarried() && (sender.hasPermission("marry.selfmarry") || sender.hasPermission("marry.selfdivorce"))))
		{
			if((!(sender instanceof Player) || player.isPriest()) && (getMarriagePlugin().isPolygamyAllowed() ? args.length == 2 : args.length == 1))
			{
				MarriagePlayer p1 = getMarriagePlugin().getPlayerData(args[0]);
				if(args.length == 1)
				{
					Marriage marriage = p1.getMarriageData();
					if(marriage == null)
					{
						((MarriageMaster) getMarriagePlugin()).messagePlayerNotMarried.send(sender, args[0]);
					}
					else
					{
						if(sender instanceof Player)
						{
							getMarriagePlugin().getMarriageManager().divorce(marriage, player, p1);
						}
						else
						{
							getMarriagePlugin().getMarriageManager().divorce(marriage, sender);
						}
					}
				}
				else
				{
					MarriagePlayer p2 = getMarriagePlugin().getPlayerData(args[1]);
					if(!p1.isMarried())
					{
						((MarriageMaster) getMarriagePlugin()).messagePlayerNotMarried.send(sender, args[0]);
					}
					else if(!p2.isMarried())
					{
						((MarriageMaster) getMarriagePlugin()).messagePlayerNotMarried.send(sender, args[1]);
					}
					else if(p1.isPartner(p2))
					{
						if(sender instanceof Player)
						{
							getMarriagePlugin().getMarriageManager().divorce(p1.getMarriageData(p2), player, p1);
						}
						else
						{
							getMarriagePlugin().getMarriageManager().divorce(p1.getMarriageData(p2), sender);
						}
					}
					else
					{
						((MarriageMaster) getMarriagePlugin()).messagePlayersNotMarried.send(sender);
					}
				}
			}
			else if(player != null && getMarriagePlugin().isSelfMarriageAllowed() && player.isMarried() && (sender.hasPermission("marry.selfmarry") || sender.hasPermission("marry.selfdivorce")) &&
					(getMarriagePlugin().isPolygamyAllowed() ? (args.length == 1 || player.getPartners().size() == 1 && args.length == 0) : args.length == 0))
			{
				// Self marriage
				Marriage marriage;
				if(args.length == 1)
				{
					MarriagePlayer player2 = getMarriagePlugin().getPlayerData(args[0]);
					if(!player.isPartner(player2))
					{
						((MarriageMaster) getMarriagePlugin()).messageTargetPartnerNotFound.send(sender);
						return;
					}
					else
					{
						marriage = player.getMarriageData(player2);
					}
				}
				else
				{
					marriage = player.getMarriageData();
				}
				marriage.divorce(player);
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
		List<String> names = null;
		MarriagePlayer marriagePlayerData = (sender instanceof Player) ? getMarriagePlugin().getPlayerData((Player) sender) : null;
		if(!(sender instanceof Player) || marriagePlayerData.isPriest())
		{
			names = new LinkedList<>();
			String arg = args[args.length - 1].toLowerCase();
			for(Player player : Bukkit.getOnlinePlayers())
			{
				if(!player.getName().equals(sender.getName()) && player.getName().toLowerCase().startsWith(arg))
				{
					names.add(player.getName());
				}
			}
		}
		else if(getMarriagePlugin().isSelfDivorceAllowed() && marriagePlayerData.isMarried() && (sender.hasPermission("marry.selfmarry") || sender.hasPermission("marry.selfdivorce")))
		{
			names = marriagePlayerData.getMatchingPartnerNames(args[args.length - 1]);
		}
		return names;
	}

	@Override
	public List<HelpData> getHelp(@NotNull CommandSender requester)
	{
		List<HelpData> help = new LinkedList<>();
		MarriagePlayer player = (requester instanceof Player) ? getMarriagePlugin().getPlayerData((Player) requester) : null;
		if(requester instanceof Player && getMarriagePlugin().isSelfDivorceAllowed() && player.isMarried())
		{
			if(player.getPartners().size() > 1)
			{
				help.add(new HelpData(getTranslatedName(), helpParam, descriptionSelf));
			}
			else
			{
				help.add(new HelpData(getTranslatedName(), "", descriptionSelf));
			}
		}
		if(!(requester instanceof Player) || player.isPriest())
		{
			help.add(new HelpData(getTranslatedName(), helpPriest, getDescription()));
		}
		return help;
	}
}