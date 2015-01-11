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
import java.util.concurrent.TimeUnit;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import at.pcgamingfreaks.MarriageMaster.Bungee.MarriageMaster;

public class TP extends BaseCommand
{
	private HashSet<String> blocked;
	private boolean delayed;
	private BaseComponent[] Message_TPBlocked;
	
	public TP(MarriageMaster MM)
	{
		super(MM);
		
		blocked = plugin.config.getTPFromServersBlocked();
		delayed = plugin.config.getTPDelayed();
		
		// Load Messages
		Message_TPBlocked = plugin.lang.getReady("Ingame.TPBlocked");
	}
	
	public boolean execute(ProxiedPlayer player, String cmd, String[] args)
	{
		if(player.hasPermission("marry.tp"))
		{
			UUID partner = plugin.DB.GetPartnerUUID(player);
			if(partner != null)
			{
				ProxiedPlayer Partner = plugin.getProxy().getPlayer(partner);
				if(Partner != null)
				{
					if(!blocked.contains(Partner.getServer().getInfo().getName().toLowerCase()))
					{
						if(delayed)
						{
							plugin.sendPluginMessage("delayTP|" + player.getName(), player.getServer().getInfo());
						}
						else
						{
							SendTP(player, Partner);
						}
					}
					else
					{
						player.sendMessage(Message_TPBlocked);
					}
				}
				else
				{
					player.sendMessage(plugin.Message_PartnerOffline);
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
	
	public void SendTP(String splayer)
	{
		ProxiedPlayer player = plugin.getProxy().getPlayer(splayer);
		if(player != null)
		{
			ProxiedPlayer Pplayer = plugin.DB.GetPartnerPlayer(player);
			if(Pplayer != null)
			{
				SendTP(player, Pplayer);
			}
		}
	}
	
	public void SendTP(final ProxiedPlayer player, final ProxiedPlayer partner)
	{
		if(!player.getServer().getInfo().getName().equals(partner.getServer().getInfo().getName()))
		{
			player.connect(partner.getServer().getInfo());
			plugin.getProxy().getScheduler().schedule(plugin, new Runnable() {
				@Override public void run() { plugin.sendPluginMessage("TP|" + player.getName(), partner.getServer().getInfo()); }}, 1L, TimeUnit.SECONDS);
		}
		else
		{
			plugin.sendPluginMessage("TP|" + player.getName(), partner.getServer().getInfo());
		}
	}
}