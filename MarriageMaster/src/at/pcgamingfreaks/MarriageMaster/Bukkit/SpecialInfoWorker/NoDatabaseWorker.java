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

package at.pcgamingfreaks.MarriageMaster.Bukkit.SpecialInfoWorker;

import at.pcgamingfreaks.Bukkit.Command.RegisterablePluginCommand;
import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.Bukkit.Message.MessageBuilder;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Permissions;
import at.pcgamingfreaks.Message.MessageColor;
import at.pcgamingfreaks.Reflection;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

/**
 * This worker will inform the admin that the plugin failed to connect to the database, he hopefully is able to solve the problem.
 * It registers a new command that only allows to reload the plugin to prevent unnecessary downtime for the server cause of restarts.
 */
public class NoDatabaseWorker extends SpecialInfoBase implements  CommandExecutor
{
	private final MarriageMaster plugin;
	private final RegisterablePluginCommand command;
	private final Message messageDBProblem;

	public NoDatabaseWorker(MarriageMaster plugin)
	{
		super(plugin, Permissions.RELOAD);
		this.plugin = plugin;
		Bukkit.getPluginManager().registerEvents(this, plugin);
		command = new RegisterablePluginCommand(plugin, "marry");
		command.registerCommand();
		command.setExecutor(this);
		messageDBProblem = new MessageBuilder("Marriage Master", MessageColor.GOLD).append(" failed to connect to it's database!", MessageColor.RED).appendNewLine()
				.append("Please check your configuration and reload the plugin (", MessageColor.RED).append("/marry reload", MessageColor.BLUE).command("/marry reload").append(")!", MessageColor.RED).getMessage();
	}

	@Override
	protected void sendMessage(Player player)
	{
		messageDBProblem.send(player);
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
			if(commandSender.hasPermission(Permissions.RELOAD))
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
					plugin.getLogger().log(Level.SEVERE, "Failed to reload plugin!", e);
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