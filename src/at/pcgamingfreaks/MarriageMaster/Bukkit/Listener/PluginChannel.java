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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Listener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;

import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;

public class PluginChannel implements PluginMessageListener, Listener
{
	private MarriageMaster plugin;
	
	public PluginChannel(MarriageMaster MM)
	{
		plugin = MM;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onPlayerLoginEvent(PlayerJoinEvent event)
	{
		if(plugin.HomeServer == null)
		{
			try
			{
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				DataOutputStream out = new DataOutputStream(stream);
				out.writeUTF("GetServer");
				out.flush();
				event.getPlayer().sendPluginMessage(plugin, "BungeeCord", stream.toByteArray());
				out.close();
			}
			catch(Exception e)
			{
				plugin.log.warning("Failed sending request to bungee!");
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] bytes)
	{
		try
	    {
			DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));
			if (channel.equals("MarriageMaster"))
			{
			    String[] args = in.readUTF().split("\\|");
				switch(args[0])
				{
					case "update": plugin.AsyncUpdate(plugin.getServer().getConsoleSender()); break;
					case "reload": plugin.reload(); break;
					case "home": if(args.length == 2) { plugin.home.TP(plugin.getServer().getPlayerExact(args[1])); } break;
					case "delayHome": if(args.length == 2) { plugin.home.BungeeHomeDelay(plugin.getServer().getPlayerExact(args[1])); } break;
					case "TP": if(args.length == 2) { plugin.tp.TP(plugin.getServer().getPlayerExact(args[1])); } break;
					case "delayTP": if(args.length == 2) { plugin.tp.BungeeTPDelay(plugin.getServer().getPlayerExact(args[1])); } break;
				}
				in.close();
	        }
			else if (channel.equals("BungeeCord"))
			{
			    switch(in.readUTF())
			    {
			    	case "GetServer": plugin.HomeServer = in.readUTF(); break;
			    }
			    in.close();
			}
		}
		catch (Exception e)
		{
			plugin.log.warning("Failed reading message from the bungee!");
			e.printStackTrace();
		}
	}
	
	public void sendMessage(String message)
	{
		try
		{
	        ByteArrayOutputStream stream = new ByteArrayOutputStream();
	        DataOutputStream out = new DataOutputStream(stream);
	        out.writeUTF(message);
	        out.flush();
			Player sendWith = getPlayerToSendWith();
			if(sendWith!=null)
			{
				sendWith.sendPluginMessage(plugin, "MarriageMaster", stream.toByteArray());
			}
	        out.close();
		}
		catch(Exception e)
		{
			plugin.log.warning("Failed sending data to bungee!");
			e.printStackTrace();
		}
    }

	private Player getPlayerToSendWith()
	{
		try
		{
			Object onlinePlayers = Bukkit.getServer().getClass().getMethod("getOnlinePlayers").invoke(Bukkit.getServer());
			if(onlinePlayers instanceof Player[]) return ((Player[]) onlinePlayers)[0];
			else if(onlinePlayers instanceof Collection<?>) return (Player)((Collection<?>) onlinePlayers).iterator().next();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
}