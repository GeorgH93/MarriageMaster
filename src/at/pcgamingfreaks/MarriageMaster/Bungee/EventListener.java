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

package at.pcgamingfreaks.MarriageMaster.Bungee;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import at.pcgamingfreaks.MarriageMaster.Bungee.Commands.*;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class EventListener implements Listener
{
	private MarriageMaster plugin;
	
	private BaseComponent[] Message_PartnerOnline, Message_PartnerNowOffline, Message_PartnerNowOnline;
	private boolean JLInfo = true;
	
	// Subcommand map
	private HashMap<String, BaseCommand> marrycommandmap = new HashMap<String, BaseCommand>();
	
	public EventListener(MarriageMaster MM)
	{
		plugin = MM;
		
		JLInfo = plugin.config.getInformOnPartnerJoinEnabled();
		
		// Load Messages
		Message_PartnerOnline = plugin.lang.getReady("Ingame.PartnerOnline");
		Message_PartnerNowOnline = plugin.lang.getReady("Ingame.PartnerNowOnline");
		Message_PartnerNowOffline = plugin.lang.getReady("Ingame.PartnerNowOffline");
	}
	
	public void RegisterMarrySubcommand(BaseCommand command, String... aliases)
	{
		for(String s : aliases)
		{
			marrycommandmap.put(s, command);
		}
	}
	
	@EventHandler(priority=EventPriority.NORMAL)
    public void onChat(ChatEvent event)
    {
		if(event.getSender() instanceof ProxiedPlayer)
		{
			if(event.isCommand())
			{
				String[] args = event.getMessage().split("\\s+");
				String cmd = args[0].toLowerCase();
				if(cmd.equalsIgnoreCase("/marry") && args.length > 1)
				{
					cmd = args[1].toLowerCase();
					try
					{
						event.setCancelled(marrycommandmap.get(cmd).execute((ProxiedPlayer)event.getSender(), cmd,
								((args.length - 1) > 2) ? Arrays.copyOfRange(args, 2, args.length-1) : (new String[0])));
					}
					catch(Exception e) {}
				}
			}
			else
			{
				// Direct chat worker
				event.setCancelled(plugin.chat.CheckDirectChat((ProxiedPlayer)event.getSender(), event.getMessage()));
			}
		}
    }
	
	@EventHandler(priority=EventPriority.NORMAL)
    public void onLogin(PostLoginEvent event)
    {
		if(JLInfo)
		{
			UUID partner = plugin.DB.GetPartnerUUID(event.getPlayer());
			if(partner != null)
			{
				ProxiedPlayer otherPlayer = plugin.getProxy().getPlayer(partner);
				if(otherPlayer != null)
				{
					event.getPlayer().sendMessage(Message_PartnerOnline);
					otherPlayer.sendMessage(Message_PartnerNowOnline);
				}
				else
				{
					event.getPlayer().sendMessage(plugin.Message_PartnerOffline);
				}
			}
		}
		plugin.DB.UpdatePlayer(event.getPlayer());
    }
	
	@EventHandler(priority=EventPriority.NORMAL)
    public void onDisconnect(PlayerDisconnectEvent event)
    {
		if(JLInfo)
		{
			ProxiedPlayer otherPlayer = plugin.DB.GetPlayerPartner(event.getPlayer());
			
			if(otherPlayer != null)
			{
				otherPlayer.sendMessage(Message_PartnerNowOffline);
			}
		}
		plugin.chat.PlayerLeave(event.getPlayer());
    }
	
	@EventHandler(priority=EventPriority.NORMAL)
	public void onPluginMessage(PluginMessageEvent ev)
	{
		try
		{
			if (!(ev.getTag().equals("MarriageMaster") && ev.getSender() instanceof Server))
			{
		        return;
		    }
		    ByteArrayInputStream stream = new ByteArrayInputStream(ev.getData());
		    DataInputStream in = new DataInputStream(stream);
		    plugin.log.info(in.readUTF());
		}
		catch(Exception e)
		{
			plugin.log.warning("Faild reading message from bukkit server!");
			e.printStackTrace();
		}
	}
}