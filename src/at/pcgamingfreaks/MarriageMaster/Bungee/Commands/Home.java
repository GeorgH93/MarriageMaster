/*
 *   Copyright (C) 2014-2016 GeorgH93
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

package at.pcgamingfreaks.MarriageMaster.Bungee.Commands;

import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import at.pcgamingfreaks.MarriageMaster.Bungee.MarriageMaster;

public class Home extends BaseCommand
{
	private HashSet<String> blocked;
	private boolean delayed;
	private BaseComponent[] messageNoHome, messageHomeBlocked;
	
	public Home(MarriageMaster MM)
	{
		super(MM);
		
		blocked = plugin.config.getHomeFromServersBlocked();
		delayed = plugin.config.getHomeDelayed();
		
		// Load Messages
		messageNoHome = plugin.lang.getReady("Ingame.NoHome");
		messageHomeBlocked = plugin.lang.getReady("Ingame.HomeBlocked");
	}
	
	@Override
	public boolean execute(ProxiedPlayer player, String cmd, String[] args)
	{
		if(player.hasPermission("marry.home"))
		{
			UUID partner = plugin.DB.getPartnerUUID(player);
			if(partner != null)
			{
				String homeServer = plugin.DB.getHomeServer(player);
				if(homeServer != null)
				{
					ServerInfo si = plugin.getProxy().getServerInfo(homeServer);
					if(si != null)
					{
						if(!blocked.contains(homeServer.toLowerCase()))
						{
							if(delayed && !player.hasPermission("marry.skiptpdelay"))
							{
								plugin.sendPluginMessage("delayHome|" + player.getName(), player.getServer().getInfo());
							}
							else
							{
								sendHome(player, si);
							}
						}
						else
						{
							player.sendMessage(messageHomeBlocked);
						}
					}
					else
					{
						player.sendMessage(messageNoHome);
					}
				}
				else
				{
					player.sendMessage(messageNoHome);
				}
			}
			else
			{
				player.sendMessage(plugin.Message_NotMarried);
			}
		}
		else
		{
			player.sendMessage(plugin.Message_NoPermission);
		}
		return true;
	}
	
	public void sendHome(String stringPlayer)
	{
		ProxiedPlayer player = plugin.getProxy().getPlayer(stringPlayer);
		if(player != null)
		{
			String homeServer = plugin.DB.getHomeServer(player);
			if(homeServer != null)
			{
				ServerInfo si = plugin.getProxy().getServerInfo(homeServer);
				if(si != null)
				{
					sendHome(player, si);
				}
			}
		}
	}
	
	public void sendHome(final ProxiedPlayer player, final ServerInfo homeServer)
	{
		if(!player.getServer().getInfo().getName().equals(homeServer.getName()))
		{
			player.connect(homeServer);
			plugin.getProxy().getScheduler().schedule(plugin, new Runnable() {
				@Override public void run() { plugin.sendPluginMessage("home|" + player.getName(), homeServer); }}, 1L, TimeUnit.SECONDS);
		}
		else
		{
			plugin.sendPluginMessage("home|" + player.getName(), homeServer);
		}
	}
}