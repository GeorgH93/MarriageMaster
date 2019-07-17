/*
 *   Copyright (C) 2016 GeorgH93
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

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ListCommand extends MarryCommand
{
	private static final Pattern PAGE_REGEX = Pattern.compile("(?<page>\\d+)(?<op>\\+\\d+|-\\d+|\\+\\+|--)");
	private final int entriesPerPage;
	private final Message messageHeadlineMain, messageFooter, messageListFormat, messageNoMarriedPlayers;
	private final boolean useFooter;

	public ListCommand(MarriageMaster plugin)
	{
		super(plugin, "list", plugin.getLanguage().getTranslated("Commands.Description.List"), "marry.list", plugin.getLanguage().getCommandAliases("List"));

		useFooter        = plugin.getConfiguration().useListFooter();
		entriesPerPage   = plugin.getConfiguration().getListEntriesPerPage();

		messageListFormat         = plugin.getLanguage().getMessage("Ingame.List.Format").replaceAll("\\{Player1Name}", "%1\\$s").replaceAll("\\{Player2Name}", "%2\\$s").replaceAll("\\{Player1DisplayName}", "%3\\$s").replaceAll("\\{Player2DisplayName}", "%4\\$s");
		messageHeadlineMain       = plugin.getLanguage().getMessage("Ingame.List.Headline").replaceAll("\\{CurrentPage}", "%1\\$d").replaceAll("\\{MaxPage}", "%2\\$d").replaceAll("\\{MainCommand}", "%3\\$s").replaceAll("\\{SubCommand}", "%4\\$s");
		messageFooter             = plugin.getLanguage().getMessage("Ingame.List.Footer").replaceAll("\\{CurrentPage}", "%1\\$d").replaceAll("\\{MaxPage}", "%2\\$d").replaceAll("\\{MainCommand}", "%3\\$s").replaceAll("\\{SubCommand}", "%4\\$s");
		messageNoMarriedPlayers   = plugin.getLanguage().getMessage("Ingame.List.NoMarriedPlayers");
	}

	@Override
	public void execute(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		int page = 0;
		if(args.length == 1)
		{
			Matcher matcher = PAGE_REGEX.matcher(args[0]);
			if(matcher.matches())
			{
				page = Integer.parseInt(matcher.group("page"));
				if(matcher.group("op") != null)
				{
					switch(matcher.group("op"))
					{
						case "++": page++; break;
						case "--": page--; break;
						default: page += Integer.parseInt(matcher.group("op")); break;
					}
				}
				if(--page < 0) page = 0; // To convert the input to a valid array range
			}
			else
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
			messageHeadlineMain.send(sender, page + 1, availablePages, mainCommandAlias, alias);
			Iterator<? extends Marriage> couplesIterator = couples.iterator();
			for(int i = 0; couplesIterator.hasNext() && i < page * entriesPerPage; i++)
			{
				couplesIterator.next();
			}
			while(couplesIterator.hasNext() && --c >= 0)
			{
				Marriage couple = couplesIterator.next();
				messageListFormat.send(sender, couple.getPartner1().getName(), couple.getPartner2().getName(), couple.getPartner1().getDisplayName(), couple.getPartner2().getDisplayName());
			}
			if(useFooter)
			{
				messageFooter.send(sender, page + 1, availablePages, mainCommandAlias, alias);
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