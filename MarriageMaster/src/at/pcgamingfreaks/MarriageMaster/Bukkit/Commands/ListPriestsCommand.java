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

import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarryCommand;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.StringUtils;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ListPriestsCommand extends MarryCommand
{
	private final int entriesPerPage;
	private final Message messageHeadlineMain, messageFooter, messageListFormat, messageNoPriestsOnline;
	private final boolean useFooter;

	public ListPriestsCommand(MarriageMaster plugin)
	{
		super(plugin, "listpriests", plugin.getLanguage().getTranslated("Commands.Description.ListPriests"), "marry.listpriests", plugin.getLanguage().getCommandAliases("ListPriests"));

		useFooter        = plugin.getConfiguration().useListFooter();
		entriesPerPage   = plugin.getConfiguration().getListEntriesPerPage();

		messageListFormat         = plugin.getLanguage().getMessage("Ingame.ListPriests.Format").replaceAll("\\{PriestName}", "%1\\$s").replaceAll("\\{PriestDisplayName}", "%2\\$s");
		messageHeadlineMain       = plugin.getLanguage().getMessage("Ingame.ListPriests.Headline").replaceAll("\\{CurrentPage}", "%1\\$d").replaceAll("\\{MaxPage}", "%2\\$d").replaceAll("\\{MainCommand}", "%3\\$s").replaceAll("\\{SubCommand}", "%4\\$s").replaceAll("\\{PrevPage}", "%5\\$d").replaceAll("\\{NextPage}", "%6\\$d");
		messageFooter             = plugin.getLanguage().getMessage("Ingame.ListPriests.Footer").replaceAll("\\{CurrentPage}", "%1\\$d").replaceAll("\\{MaxPage}", "%2\\$d").replaceAll("\\{MainCommand}", "%3\\$s").replaceAll("\\{SubCommand}", "%4\\$s").replaceAll("\\{PrevPage}", "%5\\$d").replaceAll("\\{NextPage}", "%6\\$d");
		messageNoPriestsOnline    = plugin.getLanguage().getMessage("Ingame.ListPriests.NoPriestsOnline");
	}

	private ArrayList<MarriagePlayer> collectOnlinePriests()
	{
		ArrayList<MarriagePlayer> priests = new ArrayList<>();
		for(Player player : plugin.getServer().getOnlinePlayers())
		{
			MarriagePlayer marriagePlayer = getMarriagePlugin().getPlayerData(player);
			if(marriagePlayer.isPriest()) priests.add(marriagePlayer);
		}
		return priests;
	}

	@Override
	public void execute(final @NotNull CommandSender sender, final @NotNull String mainCommandAlias, final @NotNull String alias, final @NotNull String[] args)
	{
		ArrayList<? extends MarriagePlayer> priests = collectOnlinePriests();
		if(priests.size() > 0) // There are priests online
		{
			int page = 0;
			if(args.length == 1)
			{
				try
				{
					page = StringUtils.parsePageNumber(args[0]);
				}
				catch(NumberFormatException ignored)
				{
					((MarriageMaster) getMarriagePlugin()).messageNotANumber.send(sender);
					return;
				}
			}
			int availablePages = (int) Math.ceil(priests.size() / (float)entriesPerPage);
			if(page >= availablePages) page = availablePages - 1;

			messageHeadlineMain.send(sender, page + 1, availablePages, mainCommandAlias, alias, page, page + 2);

			for(int i = page * entriesPerPage, end = Math.min(i + entriesPerPage, priests.size()); i < end; i++)
			{
				MarriagePlayer priest = priests.get(i);
				messageListFormat.send(sender, priest.getName(), priest.getDisplayName());
			}

			if(useFooter) messageFooter.send(sender, page + 1, availablePages, mainCommandAlias, alias, page, page + 2);
		}
		else
		{
			messageNoPriestsOnline.send(sender);
		}
	}

	@Override
	public List<String> tabComplete(final @NotNull CommandSender commandSender, final @NotNull String mainCommandAlias, final @NotNull String alias, final @NotNull String[] args)
	{
		return null;
	}
}