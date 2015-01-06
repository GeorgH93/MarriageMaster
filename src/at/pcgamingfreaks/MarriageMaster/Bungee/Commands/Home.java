/*
 *   Copyright (C) 2014-2015 GeorgH93
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

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import at.pcgamingfreaks.MarriageMaster.Bungee.MarriageMaster;

public class Home extends BaseCommand
{
	private HashSet<String> blocked;
	private boolean delayed;
	private BaseComponent[] Message_NoHome, Message_HomeBlocked;
	
	public Home(MarriageMaster MM)
	{
		super(MM);
		
		blocked = plugin.config.getHomeFromServersBlocked();
		delayed = plugin.config.getHomeDelayed();
		
		// Load Messages
		Message_NoHome = plugin.lang.getReady("Ingame.NoHome");
		Message_HomeBlocked = plugin.lang.getReady("Ingame.HomeBlocked");
	}
	
	public boolean execute(ProxiedPlayer player, String cmd, String[] args)
	{
		if(player.hasPermission("marry.home"))
		{
			UUID partner = plugin.DB.GetPartnerUUID(player);
			if(partner != null)
			{
				String homeserver = plugin.DB.getHomeServer(player);
				if(homeserver != null)
				{
					ServerInfo si = plugin.getProxy().getServerInfo(homeserver);
					if(si != null)
					{
						if(!blocked.contains(homeserver.toLowerCase()))
						{
							if(delayed && !player.hasPermission("marry.skiptpdelay"))
							{
								plugin.sendPluginMessage("delayHome|" + player.getName(), player.getServer().getInfo());
							}
							else
							{
								SendHome(player, si);
							}
						}
						else
						{
							player.sendMessage(Message_HomeBlocked);
						}
					}
					else
					{
						player.sendMessage(Message_NoHome);
					}
				}
				else
				{
					player.sendMessage(Message_NoHome);
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
	
	public void SendHome(String splayer)
	{
		ProxiedPlayer player = plugin.getProxy().getPlayer(splayer);
		if(player != null)
		{
			String homeserver = plugin.DB.getHomeServer(player);
			if(homeserver != null)
			{
				ServerInfo si = plugin.getProxy().getServerInfo(homeserver);
				if(si != null)
				{
					SendHome(player, si);
				}
			}
		}
	}
	
	public void SendHome(ProxiedPlayer player, ServerInfo homeServer)
	{
		if(player.getServer().getInfo().equals(homeServer))
		{
			player.connect(homeServer);
		}
		plugin.sendPluginMessage("home|" + player.getName(), homeServer);
	}
}