/*
 *   Copyright (C) 2021 GeorgH93
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
 *   along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.MarriageMaster.Bungee.Listener;

import at.pcgamingfreaks.MarriageMaster.Bungee.Commands.HomeCommand;
import at.pcgamingfreaks.MarriageMaster.Bungee.Commands.TpCommand;
import at.pcgamingfreaks.MarriageMaster.Bungee.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Database.PluginChannelCommunicatorBase;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class PluginChannelCommunicator extends PluginChannelCommunicatorBase implements Listener
{
	private final Plugin plugin;
	private HomeCommand home = null;
	private TpCommand tp = null;

	public PluginChannelCommunicator(MarriageMaster plugin)
	{
		super(plugin.getLogger(), plugin.getDatabase());
		this.plugin = plugin;
		plugin.getProxy().registerChannel(CHANNEL_MARRIAGE_MASTER);
		plugin.getProxy().getPluginManager().registerListener(plugin, this);

		sendMessage(buildStringMessage("UseUUIDs", "true"), true);
		sendMessage(buildStringMessage("UseUUIDSeparators", plugin.getConfig().useUUIDSeparators() + ""), true);
		sendMessage(buildStringMessage("UUID_Type", plugin.getConfig().useOnlineUUIDs() ? "online" : "offline"), true);
	}

	public void setHomeCommand(HomeCommand home)
	{
		this.home = home;
	}

	public void setTpCommand(TpCommand tp)
	{
		this.tp = tp;
	}

	@SuppressWarnings("unused")
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPluginMessage(PluginMessageEvent event)
	{
		if(!(event.getSender() instanceof Server)) return;
		if(receive(event.getTag(), event.getData()))
		{
			//forward message to all other servers
			ServerInfo source = ((Server) event.getSender()).getInfo();
			for(Map.Entry<String, ServerInfo> server : plugin.getProxy().getServers().entrySet())
			{
				if(server.getValue().equals(source)) continue;
				server.getValue().sendData(CHANNEL_MARRIAGE_MASTER, event.getData(), false);
			}
		}
	}

	@Override
	protected void receiveUnknownChannel(@NotNull String channel, @NotNull byte[] bytes) {} //Nothing to process here

	@Override
	protected boolean receiveMarriageMaster(@NotNull String cmd, @NotNull DataInputStream inputStream) throws IOException
	{
		switch(cmd)
		{
			case "home": home.sendHome(UUID.fromString(inputStream.readUTF()), UUID.fromString(inputStream.readUTF())); break;
			case "tp": tp.sendTP(UUID.fromString(inputStream.readUTF()), UUID.fromString(inputStream.readUTF())); break;
		}
		return true;
	}

	protected void sendMessage(final @NotNull byte[] data, boolean queue)
	{
		for(Map.Entry<String, ServerInfo> server : plugin.getProxy().getServers().entrySet())
		{
			server.getValue().sendData(CHANNEL_MARRIAGE_MASTER, data, queue);
		}
	}

	@Override
	protected void sendMessage(final @NotNull byte[] data)
	{
		sendMessage(data, false);
	}

	public void sendMessage(final @NotNull ServerInfo server, String... msg)
	{
		server.sendData(CHANNEL_MARRIAGE_MASTER, buildStringMessage(msg));
	}

	@Override
	public void close()
	{
		super.close();
		plugin.getProxy().unregisterChannel(CHANNEL_MARRIAGE_MASTER);
	}
}