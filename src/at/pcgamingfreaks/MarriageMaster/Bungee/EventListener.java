/*
 *   Copyright (C) 2014-2018 GeorgH93
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
import java.util.*;
import java.util.concurrent.TimeUnit;

import at.pcgamingfreaks.MarriageMaster.Bungee.Commands.*;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.config.ServerInfo;
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
	private boolean JLInfo;
	private int delay;
	
	// Sub-command map
	private HashMap<String, BaseCommand> marryCommandMap = new HashMap<>();
	
	public EventListener(MarriageMaster MM)
	{
		plugin = MM;
		
		JLInfo = plugin.config.getInformOnPartnerJoinEnabled();
		delay = plugin.config.getDelayMessageForJoiningPlayer();
		
		// Load Messages
		Message_PartnerOnline = plugin.lang.getReady("Ingame.PartnerOnline");
		Message_PartnerNowOnline = plugin.lang.getReady("Ingame.PartnerNowOnline");
		Message_PartnerNowOffline = plugin.lang.getReady("Ingame.PartnerNowOffline");
	}
	
	public void RegisterMarrySubCommand(BaseCommand command, String... aliases)
	{
		for(String s : aliases)
		{
			marryCommandMap.put(s, command);
		}
	}
	
	@SuppressWarnings("unused")
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
						event.setCancelled(marryCommandMap.get(cmd).execute((ProxiedPlayer)event.getSender(), cmd,
						                                                    ((args.length) > 2) ? Arrays.copyOfRange(args, 2, args.length) : (new String[0])));
					}
					catch(Exception ignored) {}
				}
			}
			else
			{
				// Direct chat worker
				if(plugin.chat != null)
				{
					event.setCancelled(plugin.chat.CheckDirectChat((ProxiedPlayer)event.getSender(), event.getMessage()));
				}
			}
		}
    }
	
	@SuppressWarnings("unused")
	@EventHandler(priority=EventPriority.NORMAL)
    public void onLogin(PostLoginEvent event)
    {
		if(JLInfo)
		{
			final UUID partner = plugin.DB.getPartnerUUID(event.getPlayer());
			if(partner != null)
			{
				ProxiedPlayer otherPlayer = plugin.getProxy().getPlayer(partner);
				final ProxiedPlayer player = event.getPlayer();
				if(otherPlayer != null)
				{
					otherPlayer.sendMessage(Message_PartnerNowOnline);
				}
				plugin.getProxy().getScheduler().schedule(plugin, new Runnable() {
					@Override
					public void run()
					{
						ProxiedPlayer otherPlayer = plugin.getProxy().getPlayer(partner);
						if(otherPlayer != null)
						{
							player.sendMessage(Message_PartnerOnline);
						}
						else
						{
							player.sendMessage(plugin.Message_PartnerOffline);
						}
					}
				}, delay, TimeUnit.SECONDS);
			}
		}
		plugin.DB.updatePlayer(event.getPlayer());
    }
	
	@SuppressWarnings("unused")
	@EventHandler(priority=EventPriority.NORMAL)
    public void onDisconnect(PlayerDisconnectEvent event)
    {
		if(JLInfo)
		{
			ProxiedPlayer otherPlayer = plugin.DB.getPartnerPlayer(event.getPlayer());
			
			if(otherPlayer != null)
			{
				otherPlayer.sendMessage(Message_PartnerNowOffline);
			}
		}
		if(plugin.chat != null)
		{
			plugin.chat.PlayerLeave(event.getPlayer());
		}
    }
	
	@SuppressWarnings("unused")
	@EventHandler(priority=EventPriority.NORMAL)
	public void onPluginMessage(PluginMessageEvent ev)
	{
		try
		{
			if (!(ev.getTag().equals(plugin.PLUGIN_CHANNEL) && ev.getSender() instanceof Server))
			{
		        return;
		    }
		    ByteArrayInputStream stream = new ByteArrayInputStream(ev.getData());
		    DataInputStream in = new DataInputStream(stream);
		    String[] args = in.readUTF().split("\\|");
			switch(args[0].toLowerCase())
			{
				case "home": if(plugin.home != null && args.length == 2) { plugin.home.sendHome(args[1]); } break;
				case "tp": if(plugin.tp != null && args.length == 2) { plugin.tp.sendTP(args[1]); } break;
				case "updateMarriage":
				case "updatePlayer":
					Set<Map.Entry<String, ServerInfo>> serverList = plugin.getProxy().getServers().entrySet();
					for(Map.Entry<String, ServerInfo> e : serverList)
					{
						if(!e.getKey().equalsIgnoreCase(((Server) ev.getSender()).getInfo().getName())) e.getValue().sendData(plugin.PLUGIN_CHANNEL, ev.getData(), true);
					}
					break;
			}
		}
		catch(Exception e)
		{
			plugin.log.warning("Failed reading message from bukkit server!");
			e.printStackTrace();
		}
	}
}