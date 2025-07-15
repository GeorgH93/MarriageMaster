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

import at.pcgamingfreaks.Bukkit.Util.Utils;
import at.pcgamingfreaks.Command.HelpData;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarryCommand;
import at.pcgamingfreaks.MarriageMaster.Bukkit.CommonMessages;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Permissions;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MarryDivorceCommand extends MarryCommand
{
	private final String descriptionSelf, helpParam, helpPriest;

	public MarryDivorceCommand(MarriageMaster plugin)
	{
		super(plugin, "divorce", plugin.getLanguage().getTranslated("Commands.Description.Divorce"), plugin.getLanguage().getCommandAliases("Divorce"));

		descriptionSelf = plugin.getLanguage().getTranslated("Commands.Description.DivorceSelf");
		helpParam = "<" + CommonMessages.getHelpPartnerNameVariable() + ">";
		if(plugin.areMultiplePartnersAllowed())
		{
			helpPriest = helpParam + " " + helpParam;
		}
		else
		{
			helpPriest = helpParam;
		}
	}

	private boolean canSelfDivorce(final @NotNull MarriagePlayer player)
	{
		return getMarriagePlugin().isSelfDivorceAllowed() && player.isMarried() && player.hasPermission(Permissions.SELF_DIVORCE);
	}

	@Override
	public void execute(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		final MarriagePlayer player = (sender instanceof Player) ? getMarriagePlugin().getPlayerData((Player) sender) : null;
		final boolean priest = player == null || player.isPriest();
		if(priest || canSelfDivorce(player))
		{
			if(priest && (getMarriagePlugin().areMultiplePartnersAllowed() ? args.length == 2 : args.length == 1))
			{
				executePriestDivorce(sender, player, args);
			}
			else if(player != null && canSelfDivorce(player) && (getMarriagePlugin().areMultiplePartnersAllowed() ? (args.length == 1 || player.getPartners().size() == 1 && args.length == 0) : args.length == 0))
			{
				// Self marriage
				executeSelfDivorce(player, args);
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

	private void executePriestDivorce(final @NotNull CommandSender sender, final @Nullable MarriagePlayer player, final @NotNull String[] args)
	{
		MarriagePlayer p1 = getMarriagePlugin().getPlayerData(args[0]);
		if(args.length == 1)
		{
			Marriage marriage = p1.getMarriageData();
			if(marriage == null)
			{
				CommonMessages.getMessagePlayerNotMarried().send(sender, args[0]);
			}
			else
			{
				if(player != null)
				{
					getMarriagePlugin().getMarriageManager().divorce(marriage, player, p1);
				}
				else
				{
					getMarriagePlugin().getMarriageManager().divorce(marriage, sender);
				}
			}
			return;
		}

		MarriagePlayer p2 = p1.getPartner(args[1]);
		if (p2 == null) p2 = getMarriagePlugin().getPlayerData(args[1]);
		if(!p1.isMarried())
		{
			CommonMessages.getMessagePlayerNotMarried().send(sender, args[0]);
		}
		else if(!p2.isMarried())
		{
			CommonMessages.getMessagePlayerNotMarried().send(sender, args[1]);
		}
		else if(p1.isPartner(p2))
		{
			if(player != null)
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
			CommonMessages.getMessagePlayersNotMarried().send(sender);
		}
	}

	private void executeSelfDivorce(final @NotNull MarriagePlayer player, final @NotNull String[] args)
	{
		Marriage marriage;
		if(args.length == 1)
		{
			MarriagePlayer player2 = player.getPartner(args[0]);
			if(player2 == null)
			{
				player.send(CommonMessages.getMessageTargetPartnerNotFound());
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

	@Override
	public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		if(args.length == 0 || !canUse(sender)) return EMPTY_TAB_COMPLETE_LIST;
		List<String> names = null;
		MarriagePlayer marriagePlayerData = (sender instanceof Player) ? getMarriagePlugin().getPlayerData((Player) sender) : null;
		if(marriagePlayerData == null || marriagePlayerData.isPriest())
		{
			if (args.length == 1)
			{
				names = Utils.getPlayerNamesStartingWithVisibleOnly(args[0], sender, Permissions.BYPASS_VANISH);
			}
			else if (args.length == 2)
			{
				MarriagePlayer player1 = getMarriagePlugin().getPlayerData(args[0]);
				names = player1.getMatchingPartnerNames(args[1]);
			}
		}
		else if(canSelfDivorce(marriagePlayerData))
		{
			names = marriagePlayerData.getMatchingPartnerNames(args[args.length - 1]);
		}
		return names;
	}

	@Override
	public List<HelpData> getHelp(@NotNull CommandSender requester)
	{
		List<HelpData> help = new ArrayList<>(2);
		MarriagePlayer player = (requester instanceof Player) ? getMarriagePlugin().getPlayerData((Player) requester) : null;
		if(player != null && getMarriagePlugin().isSelfDivorceAllowed() && player.isMarried() && (requester.hasPermission(Permissions.SELF_MARRY) || requester.hasPermission(Permissions.SELF_DIVORCE)))
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
		if(player == null || player.isPriest())
		{
			help.add(new HelpData(getTranslatedName(), helpPriest, getDescription()));
		}
		return help;
	}
}