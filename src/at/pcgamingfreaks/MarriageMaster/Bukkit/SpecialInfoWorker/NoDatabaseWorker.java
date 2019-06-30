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

package at.pcgamingfreaks.MarriageMaster.Bukkit.SpecialInfoWorker;

import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.Bukkit.Message.MessageBuilder;
import at.pcgamingfreaks.Bukkit.RegisterablePluginCommand;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.Message.MessageColor;
import at.pcgamingfreaks.Reflection;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

/**
 * This worker will inform the admin that the plugin failed to connect to the database, he hopefully is able to solve the problem.
 * It registers a new command that only allows to reload the plugin to prevent unnecessary downtime for the server cause of restarts.
 */
public class NoDatabaseWorker implements Listener, CommandExecutor
{
	private MarriageMaster plugin;
	private RegisterablePluginCommand command;
	private Message messageDBProblem;

	public NoDatabaseWorker(MarriageMaster plugin)
	{
		this.plugin = plugin;
		Bukkit.getPluginManager().registerEvents(this, plugin);
		command = new RegisterablePluginCommand(plugin, "marry");
		command.registerCommand();
		command.setExecutor(this);
		messageDBProblem = new MessageBuilder("Marriage Master", MessageColor.GOLD).append(" failed to connect to it's database!", MessageColor.RED).appendNewLine()
				.append("Please check your configuration and reload the plugin (", MessageColor.RED).append("/marry reload", MessageColor.BLUE).command("/marry reload").append(")!", MessageColor.RED).getMessage();
	}

	@EventHandler
	public void onJoin(final PlayerJoinEvent event)
	{
		if(event.getPlayer().hasPermission("marry.reload")) // If the player has the right to reload the config he hopefully also has access to the config or at least know the person that has access.
		{
			Bukkit.getScheduler().runTaskLater(plugin, () -> {
				if(event.getPlayer().isOnline())
				{
					messageDBProblem.send(event.getPlayer());
				}
			}, 3*20L);
		}
	}

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command cmd, @NotNull String s, String[] strings)
	{
		if(strings.length != 1 || !strings[0].equalsIgnoreCase("reload"))
		{
			commandSender.sendMessage(MessageColor.RED + "Only \"/marry reload\" is available at the moment!");
		}
		else
		{
			if(commandSender.hasPermission("marry.reload"))
			{
				command.unregisterCommand();
				HandlerList.unregisterAll(this);
				try
				{
					plugin.getConfiguration().reload();
					Reflection.getMethod(plugin.getClass(), "load").invoke(plugin);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			else
			{
				commandSender.sendMessage(MessageColor.RED + "You don't have the permission to do that!");
			}
		}
		return true;
	}
}