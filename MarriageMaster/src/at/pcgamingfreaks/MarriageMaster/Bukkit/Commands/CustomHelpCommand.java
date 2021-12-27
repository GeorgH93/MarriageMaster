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

import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.Command.HelpData;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarryCommand;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CustomHelpCommand extends MarryCommand
{
	private final List<Message> messagesDefault, messagesMarried, messagesPriest;

	public CustomHelpCommand(MarriageMaster plugin)
	{
		super(plugin, "!!!custom_help!!!", "", null, true);
		messagesDefault = getMessages(plugin, "Default");
		messagesMarried = getMessages(plugin, "Married");
		messagesPriest  = getMessages(plugin, "Priest");
	}

	private static List<Message> getMessages(final @NotNull MarriageMaster plugin, final @NotNull String type)
	{
		List<String> stringMessages = plugin.getLanguage().getLangE().getStringList("Language.Ingame.Help.CustomHelp." + type, new ArrayList<>(0));
		List<Message> messages = new ArrayList<>(stringMessages.size());
		for(String message : stringMessages)
		{
			messages.add(new Message(plugin.getLanguage().translateColorCodes(message)));
		}
		return messages;
	}

	@Override
	public void execute(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		MarriagePlayer marriagePlayer = getMarriagePlugin().getPlayerData((Player) sender);
		Player player = (Player) sender;
		List<Message> messages = marriagePlayer.isMarried() ? messagesMarried : (marriagePlayer.isPriest() ? messagesPriest : messagesDefault);
		for(Message msg : messages)
		{
			msg.send(player);
		}
	}

	@Override
	public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		return null;
	}

	@Override
	public @Nullable List<HelpData> getHelp(@NotNull CommandSender requester)
	{
		return null;
	}
}