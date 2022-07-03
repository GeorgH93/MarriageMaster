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
import at.pcgamingfreaks.Bukkit.Message.Sender.SendMethod;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarryCommand;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Database.MarriagePlayerData;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Permissions;
import at.pcgamingfreaks.MarriageMaster.Placeholder.Placeholders;
import at.pcgamingfreaks.Message.MessageComponent;
import at.pcgamingfreaks.Message.Placeholder.Processors.PassthroughMessageComponentPlaceholderProcessor;
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

	public ListCommand(final @NotNull MarriageMaster plugin)
	{
		super(plugin, "list", plugin.getLanguage().getTranslated("Commands.Description.List"), Permissions.LIST, plugin.getLanguage().getCommandAliases("List"));

		entriesPerPage   = plugin.getConfiguration().getListEntriesPerPage();

		messageListFormat         = plugin.getLanguage().getMessage("Ingame.List.Format").placeholder("Player1Name").placeholder("Player2Name").placeholder("Player1DisplayName", PassthroughMessageComponentPlaceholderProcessor.INSTANCE).placeholder("Player2DisplayName", PassthroughMessageComponentPlaceholderProcessor.INSTANCE).placeholder("Surname").placeholder("MagicHeart");
		messageHeadlineMain       = plugin.getLanguage().getMessage("Ingame.List.Headline").placeholders(Placeholders.PAGE_OPTIONS);
		messageFooter             = plugin.getLanguage().getMessage("Ingame.List.Footer").placeholders(Placeholders.PAGE_OPTIONS);
		messageNoMarriedPlayers   = plugin.getLanguage().getMessage("Ingame.List.NoMarriedPlayers");

		if (!plugin.getConfiguration().useListFooter())
		{
			messageFooter.setSendMethod(SendMethod.DISABLED);
		}
	}

	@Override
	public void execute(final @NotNull CommandSender sender, final @NotNull String mainCommandAlias, final @NotNull String alias, final @NotNull String[] args)
	{
		Collection<? extends Marriage> couples = getMarriagePlugin().getMarriages();
		if(!couples.isEmpty()) // There are married couples
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
			int c = entriesPerPage, availablePages = (int) Math.ceil(couples.size() / (float)entriesPerPage);
			page = Math.min(page, availablePages - 1);
			messageHeadlineMain.send(sender, page + 1, availablePages, mainCommandAlias, alias, page, page + 2);
			Iterator<? extends Marriage> couplesIterator = couples.iterator();
			for(int i = 0; couplesIterator.hasNext() && i < page * entriesPerPage; i++)
			{
				couplesIterator.next();
			}
			while(couplesIterator.hasNext() && --c >= 0)
			{
				Marriage couple = couplesIterator.next();
				MarriagePlayer p1 = couple.getPartner1(), p2 = couple.getPartner2();
				MessageComponent p1DName = ((MarriagePlayerData) p1).getDisplayNameMessageComponentCheckVanished(sender);
				MessageComponent p2DName = ((MarriagePlayerData) p2).getDisplayNameMessageComponentCheckVanished(sender);
				messageListFormat.send(sender, p1.getName(), p2.getName(), p1DName, p2DName, couple.getSurnameString(), couple.getMagicHeart());
			}
			messageFooter.send(sender, page + 1, availablePages, mainCommandAlias, alias, page, page + 2);
		}
		else
		{
			messageNoMarriedPlayers.send(sender);
		}
	}

	@Override
	public List<String> tabComplete(final @NotNull CommandSender sender, final @NotNull String mainCommandAlias, final @NotNull String alias, final @NotNull String[] args)
	{
		return EMPTY_TAB_COMPLETE_LIST;
	}
}