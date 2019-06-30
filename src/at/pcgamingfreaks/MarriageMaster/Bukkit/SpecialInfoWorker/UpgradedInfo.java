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
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.Message.MessageColor;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class UpgradedInfo implements Listener
{
	private final MarriageMaster plugin;
	private final Message messagePluginUpgraded;

	public UpgradedInfo(MarriageMaster plugin)
	{
		this.plugin = plugin;
	    Bukkit.getPluginManager().registerEvents(this, plugin);
		// I don't load this message from the config. He just upgraded, the messages in the config would probably be exactly the same.
		messagePluginUpgraded = new MessageBuilder("Marriage Master", MessageColor.GOLD).append(" has been upgraded to ", MessageColor.AQUA)
				.append(MarriageMaster.getInstance().getDescription().getVersion(), MessageColor.BLUE, MessageColor.BOLD).append(".", MessageColor.AQUA).appendNewLine()
				.append("All of your settings and changes to the language file have been copied into the new format.", MessageColor.AQUA).appendNewLine()
				.append("However it's recommended to manually check them to make sure that all of your settings are still how you like them (there are many new options).", MessageColor.AQUA).getMessage();
	}

	@EventHandler
	public void onJoin(final PlayerJoinEvent event)
	{
		if(event.getPlayer().hasPermission("marry.reload")) // If the player has the right to reload the config he hopefully also has access to the config or at least know the person that has access.
		{
			Bukkit.getScheduler().runTaskLater(plugin, () -> {
				// If he has the permissions he probably also has access to the configs or at least he knows someone who has access to the configs.
				if(event.getPlayer().isOnline() && event.getPlayer().hasPermission("marry.reload"))
				{
					messagePluginUpgraded.send(event.getPlayer());
				}
			}, 3 * 20L); // Run with a 3 seconds delay
		}
	}
}