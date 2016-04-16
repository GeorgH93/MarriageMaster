/*
 *   Copyright (C) 2016 GeorgH93
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

package at.pcgamingfreaks.MarriageMaster.Bungee;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.concurrent.TimeUnit;

public class MarriageMasterV2IsOut implements Listener
{
	public static MarriageMasterV2IsOut instance = null;
	private MarriageMaster plugin;
	private BaseComponent[] messageV2IsThereLine1, messageV2IsThereLine2, spacer;
	private final static String spigotURL = "https://www.spigotmc.org/resources/19273/", bukkitURL = "http://dev.bukkit.org/bukkit-plugins/marriage-master/";

	public MarriageMasterV2IsOut(MarriageMaster plugin)
	{
		if(plugin.config.isV2InfoDisabled()) return;
		instance = this;
		this.plugin = plugin;
		plugin.getProxy().getPluginManager().registerListener(plugin, this);
		spacer = TextComponent.fromLegacyText(ChatColor.GRAY + "#####################################");
		messageV2IsThereLine1 = new ComponentBuilder("Marriage Master V2 has been released! :)").color(ChatColor.GOLD).create();
		messageV2IsThereLine2 = new ComponentBuilder("Please download it from ").color(ChatColor.WHITE)
				.append("spigot.org").underlined(true).bold(true).color(ChatColor.AQUA).event(new ClickEvent(ClickEvent.Action.OPEN_URL, spigotURL)). append(" or ")
				.color(ChatColor.WHITE)
				.append("dev.bukkit.org").underlined(true).bold(true).color(ChatColor.AQUA).event(new ClickEvent(ClickEvent.Action.OPEN_URL, bukkitURL))
				.append("!").color(ChatColor.WHITE).create();
		plugin.getProxy().getConsole().sendMessage(spacer);
		plugin.getProxy().getConsole().sendMessage(messageV2IsThereLine1);
		plugin.getProxy().getConsole().sendMessage(messageV2IsThereLine2);
		plugin.getProxy().getConsole().sendMessage(spacer);
	}

	public void announce(ProxiedPlayer player)
	{
		if(player != null && player.isConnected())
		{
			player.sendMessage(spacer);
			player.sendMessage(messageV2IsThereLine1);
			player.sendMessage(messageV2IsThereLine2);
			player.sendMessage(spacer);
		}
	}

	@SuppressWarnings("unused")
	@EventHandler
	public void onJoin(PostLoginEvent event)
	{
		final ProxiedPlayer player = event.getPlayer();
		if(player.hasPermission("marry.update"))
		{
			plugin.getProxy().getScheduler().schedule(plugin, new Runnable()
			{
				@Override
				public void run()
				{
					announce(player);
				}
			}, 5, TimeUnit.SECONDS);
		}
	}
}