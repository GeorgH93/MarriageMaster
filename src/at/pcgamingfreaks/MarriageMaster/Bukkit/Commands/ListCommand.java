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
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarryCommand;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.StringUtils;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class ListCommand extends MarryCommand
{
	private final int entriesPerPage;
	private final Message messageHeadlineMain, messageFooter, messageListFormat, messageNoMarriedPlayers;
	private final boolean useFooter;

	public ListCommand(MarriageMaster plugin)
	{
		super(plugin, "list", plugin.getLanguage().getTranslated("Commands.Description.List"), "marry.list", plugin.getLanguage().getCommandAliases("List"));

		useFooter        = plugin.getConfiguration().useListFooter();
		entriesPerPage   = plugin.getConfiguration().getListEntriesPerPage();

		messageListFormat         = plugin.getLanguage().getMessage("Ingame.List.Format").replaceAll("\\{Player1Name}", "%1\\$s").replaceAll("\\{Player2Name}", "%2\\$s").replaceAll("\\{Player1DisplayName}", "%3\\$s").replaceAll("\\{Player2DisplayName}", "%4\\$s").replaceAll("\\{Surname}", "%5\\$s");
		messageHeadlineMain       = plugin.getLanguage().getMessage("Ingame.List.Headline").replaceAll("\\{CurrentPage}", "%1\\$d").replaceAll("\\{MaxPage}", "%2\\$d").replaceAll("\\{MainCommand}", "%3\\$s").replaceAll("\\{SubCommand}", "%4\\$s").replaceAll("\\{PrevPage}", "%5\\$d").replaceAll("\\{NextPage}", "%6\\$d");
		messageFooter             = plugin.getLanguage().getMessage("Ingame.List.Footer").replaceAll("\\{CurrentPage}", "%1\\$d").replaceAll("\\{MaxPage}", "%2\\$d").replaceAll("\\{MainCommand}", "%3\\$s").replaceAll("\\{SubCommand}", "%4\\$s").replaceAll("\\{PrevPage}", "%5\\$d").replaceAll("\\{NextPage}", "%6\\$d");
		messageNoMarriedPlayers   = plugin.getLanguage().getMessage("Ingame.List.NoMarriedPlayers");
	}

	@Override
	public void execute(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
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
		Collection<? extends Marriage> couples = getMarriagePlugin().getMarriages();
		if(couples.size() > 0) // There are married couples
		{
			int c = entriesPerPage, availablePages = (int) Math.ceil(couples.size() / (float)entriesPerPage);
			if(page >= availablePages)
			{
				page = availablePages - 1;
			}
			messageHeadlineMain.send(sender, page + 1, availablePages, mainCommandAlias, alias, page, page + 2);
			Iterator<? extends Marriage> couplesIterator = couples.iterator();
			for(int i = 0; couplesIterator.hasNext() && i < page * entriesPerPage; i++)
			{
				couplesIterator.next();
			}
			while(couplesIterator.hasNext() && --c >= 0)
			{
				Marriage couple = couplesIterator.next();
				messageListFormat.send(sender, couple.getPartner1().getName(), couple.getPartner2().getName(), couple.getPartner1().getDisplayName(), couple.getPartner2().getDisplayName(), couple.getSurnameString());
			}
			if(useFooter)
			{
				messageFooter.send(sender, page + 1, availablePages, mainCommandAlias, alias, page, page + 2);
			}
		}
		else
		{
			messageNoMarriedPlayers.send(sender);
		}
	}

	@Override
	public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		return null;
	}
}