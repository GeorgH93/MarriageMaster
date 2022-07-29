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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Listener;

import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.DivorcedEvent;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.MarriedEvent;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Database.Config;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CommandExecutor implements Listener
{
	private final MarriageMaster plugin;
	private final Collection<String> commandsOnMarry, commandsOnMarryWithPriest, commandsOnDivorce, commandsOnDivorceWithPriest;

	private static @NotNull String handlePlaceholders(@NotNull String command, boolean withPriest)
	{
		command = command.replace("{Player1}", "%1$s").replace("{Player2}", "%2$s");
		if (withPriest) command = command.replace("{Priest}", "%3$s");
		return command;
	}

	private static @NotNull Collection<String> prepareCommandList(@NotNull Collection<String> commands, final boolean withPriest)
	{
		List<String> commandList = new ArrayList<>(commands.size());
		commands.forEach(command -> commandList.add(handlePlaceholders(command, withPriest)));
		return commandList;
	}

	public CommandExecutor(final @NotNull MarriageMaster plugin)
	{
		this.plugin = plugin;
		final Config config = plugin.getConfiguration();
		this.commandsOnMarry = prepareCommandList(config.getCommandExecutorOnMarry(), false);
		this.commandsOnMarryWithPriest = prepareCommandList(config.getCommandExecutorOnMarryWithPriest(), true);
		this.commandsOnDivorce = prepareCommandList(config.getCommandExecutorOnDivorce(), false);
		this.commandsOnDivorceWithPriest = prepareCommandList(config.getCommandExecutorOnDivorceWithPriest(), true);
	}

	@EventHandler
	public void onMarry(final @NotNull MarriedEvent event)
	{
		final Marriage marriage = event.getMarriageData();
		if(marriage.getPriest() == null || marriage.getPriest().equals(marriage.getPartner1()) || marriage.getPriest().equals(marriage.getPartner2()))
		{
			for(String command : commandsOnMarry)
			{
				plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), String.format(command, marriage.getPartner1().getName(), marriage.getPartner2().getName()));
			}
		}
		else
		{
			for(String command : commandsOnMarryWithPriest)
			{
				plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), String.format(command, marriage.getPartner1().getName(), marriage.getPartner2().getName(), marriage.getPriest().getName()));
			}
		}
	}

	@EventHandler
	public void onDivorce(final @NotNull DivorcedEvent event)
	{
		if(event.getPriest() == null)
		{
			for(String command : commandsOnDivorce)
			{
				plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), String.format(command, event.getPlayer1().getName(), event.getPlayer2().getName()));
			}
		}
		else
		{
			for(String command : commandsOnDivorceWithPriest)
			{
				plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), String.format(command, event.getPlayer1().getName(), event.getPlayer2().getName(), event.getPriest().getName()));
			}
		}
	}
}