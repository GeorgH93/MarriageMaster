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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Listener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;

public class PluginChannel implements PluginMessageListener
{
	private MarriageMaster plugin;
	
	public PluginChannel(MarriageMaster MM)
	{
		plugin = MM;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] bytes)
	{
		try
	    {
			if (!channel.equals("MarriageMaster"))
			{
	            return;
	        }
			ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
		    DataInputStream in = new DataInputStream(stream);
		    String[] args = in.readUTF().split("\\|");
			switch(args[0].toLowerCase())
			{
				case "update": plugin.AsyncUpdate(plugin.getServer().getConsoleSender()); break;
				case "reload": plugin.reload(); break;
				case "home": if(args.length == 2) { plugin.home.TP(plugin.getServer().getPlayerExact(args[1])); } break;
				case "delayHome": if(args.length == 2) { plugin.home.BungeeHomeDelay(plugin.getServer().getPlayerExact(args[1])); } break;
				case "TP": if(args.length == 2) { plugin.tp.TP(plugin.getServer().getPlayerExact(args[1])); } break;
				case "delayTP": if(args.length == 2) { plugin.tp.BungeeTPDelay(plugin.getServer().getPlayerExact(args[1])); } break;
			}
		}
		catch (Exception e)
		{
			plugin.log.warning("Faild reading message from the bungee!");
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
	        plugin.getServer().getOnlinePlayers()[0].sendPluginMessage(plugin, "MarriageMaster", stream.toByteArray());
	        out.close();
		}
		catch(Exception e)
		{
			plugin.log.warning("Faild sending data to bungee!");
			e.printStackTrace();
		}
    }
}