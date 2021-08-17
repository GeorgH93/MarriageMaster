/*
 *   Copyright (C) 2020 GeorgH93
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

package at.pcgamingfreaks.MarriageMaster.Bungee.SpecialInfoWorker;

import at.pcgamingfreaks.Bungee.Message.MessageBuilder;
import at.pcgamingfreaks.Bungee.Message.Message;
import at.pcgamingfreaks.MarriageMaster.Bungee.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Permissions;
import at.pcgamingfreaks.Message.MessageColor;

import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class DbErrorLoadingDataInfo implements Listener
{
	private final MarriageMaster plugin;
	private final Message messageDbError;

	public DbErrorLoadingDataInfo(final @NotNull MarriageMaster plugin, final @NotNull String msg)
	{
		this.plugin = plugin;
		plugin.getProxy().getPluginManager().registerListener(plugin, this);
		// I don't load this message from the config. He just upgraded, the messages in the config would probably be exactly the same.
		messageDbError = new MessageBuilder("[Marriage Master] ", MessageColor.GOLD).append("There was a problem loading data from the database! Please check your log file.", MessageColor.RED)
				.appendNewLine().append("Error: " + msg, MessageColor.RED).getMessage();
	}

	@SuppressWarnings("unused")
	@EventHandler
	public void onJoin(final PostLoginEvent event)
	{
		if(event.getPlayer() != null && event.getPlayer().hasPermission(Permissions.RELOAD))
		{
			plugin.getProxy().getScheduler().schedule(plugin, () -> {
				// If he has the permissions he probably also has access to the configs or at least he knows someone who has access to the configs.
				if(event.getPlayer() != null && event.getPlayer().hasPermission(Permissions.RELOAD))
				{
					messageDbError.send(event.getPlayer());
				}
			}, 10, TimeUnit.SECONDS);
		}
	}
}