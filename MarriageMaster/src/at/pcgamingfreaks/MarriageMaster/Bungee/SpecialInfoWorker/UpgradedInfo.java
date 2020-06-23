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

import at.pcgamingfreaks.Bungee.Message.Message;
import at.pcgamingfreaks.Bungee.Message.MessageBuilder;
import at.pcgamingfreaks.MarriageMaster.Bungee.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Permissions;
import at.pcgamingfreaks.Message.MessageColor;
import at.pcgamingfreaks.Message.MessageFormat;

import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.concurrent.TimeUnit;

public class UpgradedInfo implements Listener
{
	private final MarriageMaster plugin;
	private final Message messagePluginUpgraded;

	public UpgradedInfo(MarriageMaster plugin)
	{
		this.plugin = plugin;
		plugin.getProxy().getPluginManager().registerListener(plugin, this);
		// I don't load this message from the config. He just upgraded, the messages in the config would probably be exactly the same.
		messagePluginUpgraded = new MessageBuilder("Marriage Master", MessageColor.GOLD).append(" has been upgraded to ", MessageColor.AQUA)
				.append(plugin.getDescription().getVersion(), MessageColor.BLUE, MessageFormat.BOLD).append(".", MessageColor.AQUA).appendNewLine()
				.append("All of your settings and changes to the language file have been copied into the new files.", MessageColor.AQUA).appendNewLine()
				.append("However it's recommended to manually check them to make sure that all of your settings are how you like them (there are many new options).", MessageColor.AQUA).getMessage();
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
					messagePluginUpgraded.send(event.getPlayer());
				}
			}, 10, TimeUnit.SECONDS);
		}
	}
}