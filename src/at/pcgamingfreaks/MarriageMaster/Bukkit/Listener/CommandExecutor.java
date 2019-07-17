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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Listener;

import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.DivorcedEvent;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.MarriedEvent;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Database.Config;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Collection;
import java.util.LinkedList;

public class CommandExecutor implements Listener
{
	private final MarriageMaster plugin;
	private final Collection<String> commandsOnMarry, commandsOnMarryWithPriest, commandsOnDivorce, commandsOnDivorceWithPriest;

	public CommandExecutor(final MarriageMaster plugin)
	{
		this.plugin = plugin;
		this.commandsOnMarry = new LinkedList<>();
		this.commandsOnMarryWithPriest = new LinkedList<>();
		this.commandsOnDivorce = new LinkedList<>();
		this.commandsOnDivorceWithPriest = new LinkedList<>();
		final Config config = plugin.getConfiguration();
		config.getCommandExecutorOnMarry().forEach(command -> commandsOnMarry.add(command.replaceAll("\\{Player1}", "%1\\$s").replaceAll("\\{Player2}", "%2\\$s")));
		config.getCommandExecutorOnMarryWithPriest().forEach(command -> commandsOnMarryWithPriest.add(command.replaceAll("\\{Player1}", "%1\\$s").replaceAll("\\{Player2}", "%2\\$s").replaceAll("\\{Priest}", "%3\\$s")));
		config.getCommandExecutorOnDivorce().forEach(command -> commandsOnDivorce.add(command.replaceAll("\\{Player1}", "%1\\$s").replaceAll("\\{Player2}", "%2\\$s")));
		config.getCommandExecutorOnDivorceWithPriest().forEach(command -> commandsOnDivorceWithPriest.add(command.replaceAll("\\{Player1}", "%1\\$s").replaceAll("\\{Player2}", "%2\\$s").replaceAll("\\{Priest}", "%3\\$s")));
	}

	@EventHandler
	public void onMarry(final MarriedEvent event)
	{
		final Marriage marriage = event.getMarriageData();
		if(marriage.getPriest() == null)
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
	public void onDivorce(final DivorcedEvent event)
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