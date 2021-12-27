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

package at.pcgamingfreaks.MarriageMaster.Bukkit.SpecialInfoWorker;

import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.Bukkit.Message.MessageBuilder;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Permissions;
import at.pcgamingfreaks.Message.MessageColor;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * This worker will inform the admin that the plugin failed to connect to the database, he hopefully is able to solve the problem.
 * It registers a new command that only allows to reload the plugin to prevent unnecessary downtime for the server cause of restarts.
 */
public class DbErrorLoadingDataInfo extends SpecialInfoBase
{
	private final Message messageDbError;

	public DbErrorLoadingDataInfo(final @NotNull MarriageMaster plugin, final @NotNull String msg)
	{
		super(plugin, Permissions.RELOAD);
		Bukkit.getPluginManager().registerEvents(this, plugin);
		messageDbError = new MessageBuilder("[Marriage Master] ", MessageColor.GOLD).append("There was a problem loading data from the database! Please check your log file.", MessageColor.RED)
				.appendNewLine().append("Error: " + msg, MessageColor.RED).getMessage();
	}

	@Override
	protected void sendMessage(Player player)
	{
		messageDbError.send(player);
	}
}