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
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarryCommand;
import at.pcgamingfreaks.MarriageMaster.Bukkit.CommonMessages;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Permissions;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SurnameCommand extends MarryCommand
{
	private final String helpSurnameParam, helpPriestParam, helpMMParam, descSelf;

	public SurnameCommand(MarriageMaster plugin)
	{
		super(plugin, "surname", plugin.getLanguage().getTranslated("Commands.Description.Surname"), Permissions.CHANGE_SURNAME, plugin.getLanguage().getCommandAliases("Surname"));

		descSelf         = plugin.getLanguage().getTranslated("Commands.Description.SurnameSelf");
		helpSurnameParam = "<" + plugin.getLanguage().getTranslated("Commands.SurnameVariable") + ">";
		helpMMParam      = "<" + CommonMessages.getHelpPartnerNameVariable() + "> " + helpSurnameParam;
		if(plugin.areMultiplePartnersAllowed())
		{
			helpPriestParam = "<" + CommonMessages.getHelpPlayerNameVariable() + "> <" + CommonMessages.getHelpPlayerNameVariable() + "> " + helpSurnameParam;
		}
		else
		{
			helpPriestParam = "<" + CommonMessages.getHelpPlayerNameVariable() + "> " + helpSurnameParam;
		}
	}

	private void executeOwnSurname(final @NotNull MarriagePlayer player, String newSurname)
	{
		if(player.getPartners().size() == 1)
		{
			Marriage marriage = player.getMarriageData();
			if (marriage != null) marriage.setSurname(newSurname, player);
		}
		else
		{
			player.send(CommonMessages.getMessageNotMarried());
		}
	}

	private void executeOwnSurnamePoly(final @NotNull MarriagePlayer player, final @NotNull MarriagePlayer player2, String newSurname)
	{
		if(player.isMarried())
		{
			if(player.isPartner(player2))
			{
				Marriage marriage = player.getMarriageData(player2);
				if (marriage != null) marriage.setSurname(newSurname, player);
			}
			else
			{
				player.send(CommonMessages.getMessageTargetPartnerNotFound());
			}
		}
		else
		{
			player.send(CommonMessages.getMessageNotMarried());
		}
	}

	private void executePriest(final @NotNull CommandSender sender, final @Nullable MarriagePlayer player, final @NotNull MarriagePlayer player2, String newSurname, String[] args)
	{
		if(!player2.isOnline())
		{
			CommonMessages.getMessagePlayerNotOnline().send(sender, args[0]);
		}
		else if(!player2.isMarried())
		{
			CommonMessages.getMessagePlayerNotMarried().send(sender, args[0]);
		}
		else if(player2.getMultiMarriageData().size() > 1)
		{
			if(player != null && player.isPartner(player2))
			{
				Marriage marriage = player.getMarriageData(player2);
				if (marriage != null) marriage.setSurname(newSurname, player);
			}
			else
			{
				CommonMessages.getMessageMarriageNotExact().send(sender);
			}
		}
		else
		{
			Marriage marriage = player2.getMarriageData();
			if (marriage != null) marriage.setSurname(newSurname, sender);
		}
	}

	@Contract(value = "null->false")
	private boolean canSelfMarry(final @Nullable MarriagePlayer player)
	{
		return player != null && getMarriagePlugin().isSelfMarriageAllowed() && player.hasPermission(Permissions.SELF_MARRY) && player.isMarried();
	}

	@Override
	public void execute(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		if(args.length == 0)
		{
			showHelp(sender, mainCommandAlias);
			return;
		}
		final String newSurname = args[args.length - 1];
		MarriagePlayer player = (sender instanceof Player) ? getMarriagePlugin().getPlayerData((Player) sender) : null;
		boolean priest = player == null || player.isPriest();
		if(priest || canSelfMarry(player))
		{
			if(args.length == 1 && canSelfMarry(player))
			{
				executeOwnSurname(player, newSurname);
			}
			else if(args.length == 2)
			{
				MarriagePlayer player2 = getMarriagePlugin().getPlayerData(args[0]);
				if(priest)
				{
					executePriest(sender, player, player2, newSurname, args);
				}
				else if(canSelfMarry(player))
				{
					executeOwnSurnamePoly(player, player2, newSurname);
				}
				else
				{
					showHelp(sender, mainCommandAlias);
				}
			}
			else if(args.length == 3 && getMarriagePlugin().areMultiplePartnersAllowed() && priest)
			{
				MarriagePlayer player1 = getMarriagePlugin().getPlayerData(args[0]), player2 = getMarriagePlugin().getPlayerData(args[1]);
				if(!player1.isMarried())
				{
					CommonMessages.getMessagePlayerNotMarried().send(sender, player1.getName());
				}
				else if(!player2.isMarried())
				{
					CommonMessages.getMessagePlayerNotMarried().send(sender, player2.getName());
				}
				else if(!player1.isPartner(player2))
				{
					CommonMessages.getMessagePlayersNotMarried().send(sender);
				}
				else
				{
					Marriage marriage = player1.getMarriageData(player2);
					if (marriage != null) marriage.setSurname(newSurname, sender);
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

	@Override
	public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		if(args.length == 1 || (getMarriagePlugin().areMultiplePartnersAllowed() && args.length == 2))
		{
			MarriagePlayer player = (sender instanceof Player) ? getMarriagePlugin().getPlayerData((Player) sender) : null;
			if(player == null || player.isPriest())
			{
				return Utils.getPlayerNamesStartingWithVisibleOnly(args[args.length - 1], sender, Permissions.BYPASS_VANISH);
			}
			else if(player.getPartners().size() > 1)
			{
				return player.getMatchingPartnerNames(args[args.length - 1]);
			}
		}
		return null;
	}

	@Override
	public List<HelpData> getHelp(@NotNull CommandSender requester)
	{
		List<HelpData> help = new ArrayList<>(2);
		MarriagePlayer player = (requester instanceof Player) ? getMarriagePlugin().getPlayerData((Player) requester) : null;
		if(canSelfMarry(player))
		{
			if(player.getPartners().size() > 1)
			{
				help.add(new HelpData(getTranslatedName(), helpMMParam, descSelf));
			}
			else
			{
				help.add(new HelpData(getTranslatedName(), helpSurnameParam, descSelf));
			}
		}
		if(player == null || player.isPriest())
		{
			help.add(new HelpData(getTranslatedName(), helpPriestParam, getDescription()));
		}
		return help;
	}
}