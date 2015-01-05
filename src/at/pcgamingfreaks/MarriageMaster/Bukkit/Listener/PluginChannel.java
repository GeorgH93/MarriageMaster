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
import java.io.DataInputStream;

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
		    String data = in.readUTF();
		    String[] args = data.split("\\|");
			String cmd = args[0].toLowerCase();
			switch(cmd)
			{
				case "update": plugin.AsyncUpdate(plugin.getServer().getConsoleSender()); break;
				case "reload": plugin.reload(); break;
			}
		}
		catch (Exception e)
		{
			plugin.log.warning("Faild reading message from the bungee!");
			e.printStackTrace();
		}
	}
}