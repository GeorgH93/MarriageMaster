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
	private final MarriageMaster plugin;
	private final String channelMarriageMaster, channelBungee = "BungeeCord";
	
	public PluginChannel(MarriageMaster MM)
	{
		plugin = MM;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);

		String[] GameVersion = Bukkit.getBukkitVersion().split("-");
		GameVersion = GameVersion[0].split("\\.");
		if(Integer.parseInt(GameVersion[1]) > 12)
		{
			channelMarriageMaster = "marriagemaster:main";
		}
		else
		{
			channelMarriageMaster = "MarriageMaster";
		}

		plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, channelMarriageMaster, this);
		plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, channelMarriageMaster);
		plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, channelBungee, this);
		plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, channelBungee);
	}

	@EventHandler
	public void onPlayerLoginEvent(final PlayerJoinEvent event)
	{
		if(plugin.HomeServer == null)
		{
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				public void run() {
					try(ByteArrayOutputStream stream = new ByteArrayOutputStream(); DataOutputStream out = new DataOutputStream(stream))
					{
						out.writeUTF("GetServer");
						out.flush();
						event.getPlayer().sendPluginMessage(plugin, channelBungee, stream.toByteArray());
						plugin.log.info("Sending server name request to bungee");
					}
					catch(Exception e)
					{
						plugin.log.warning("Failed sending request to bungee!");
						e.printStackTrace();
					}
				}}, 5L);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] bytes)
	{
		try(DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes)))
	    {
			if (channel.equals(channelMarriageMaster))
			{
			    String[] args = in.readUTF().split("\\|");
				switch(args[0])
				{
					case "update": plugin.update(); break;
					case "reload": plugin.reload(); break;
					case "home": if(args.length == 2) { plugin.home.TP(plugin.getServer().getPlayerExact(args[1])); } break;
					case "delayHome": if(args.length == 2) { plugin.home.BungeeHomeDelay(plugin.getServer().getPlayerExact(args[1])); } break;
					case "TP": if(args.length == 2) { plugin.tp.TP(plugin.getServer().getPlayerExact(args[1])); } break;
					case "delayTP": if(args.length == 2) { plugin.tp.BungeeTPDelay(plugin.getServer().getPlayerExact(args[1])); } break;
				}
	        }
			else if (channel.equals(channelBungee))
			{
			    switch(in.readUTF())
			    {
			    	case "GetServer": plugin.HomeServer = in.readUTF(); break;
			    }
			}
		}
		catch (Exception e)
		{
			plugin.log.warning("Failed reading message from the bungee!");
			e.printStackTrace();
		}
	}
	
	public void sendMessage(Object... components)
	{
		try(ByteArrayOutputStream stream = new ByteArrayOutputStream(); DataOutputStream out = new DataOutputStream(stream))
		{
			if(components != null && components.length > 0)
			{
				for(Object o : components)
				{
					if(o instanceof Integer) out.writeInt((int) o);
					else if(o instanceof String) out.writeUTF((String) o);
					else if(o instanceof Boolean) out.writeBoolean((boolean) o);
					else if(o instanceof Double) out.writeDouble((double) o);
					else if(o instanceof Float) out.writeFloat((float) o);
					else if(o instanceof Byte) out.writeByte((byte) o);
					else if(o instanceof Long) out.writeLong((long) o);
					else if(o instanceof Short) out.writeShort((short) o);
				}
			}
	        out.flush();
			Player sendWith = getPlayerToSendWith();
			if(sendWith!=null)
			{
				sendWith.sendPluginMessage(plugin, channelMarriageMaster, stream.toByteArray());
			}
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