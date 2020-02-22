/*
 *   Copyright (C) 2019 GeorgH93
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

import at.pcgamingfreaks.MarriageMaster.Bukkit.API.DelayableTeleportAction;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Commands.HomeCommand;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Commands.TpCommand;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Database.PluginChannelCommunicatorBase;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;

public class PluginChannelCommunicator extends PluginChannelCommunicatorBase implements PluginMessageListener, Listener
{
	@Getter @Setter(AccessLevel.PRIVATE) private static String serverName = null;

	private final MarriageMaster plugin;
	private final long delayTime;
	private boolean serverNameUpdated = false;
	@Setter private TpCommand tpCommand = null;
	@Setter private HomeCommand homeCommand = null;
	
	public PluginChannelCommunicator(MarriageMaster plugin)
	{
		super(plugin.getLogger(), plugin.getDatabase());
		this.plugin = plugin;
		delayTime = plugin.getConfiguration().getTPDelayTime() * 20L;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, CHANNEL_BUNGEE_CORD);
		plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, CHANNEL_BUNGEE_CORD, this);
		plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, CHANNEL_MARRIAGE_MASTER);
		plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, CHANNEL_MARRIAGE_MASTER, this);

		setServerName(plugin.getConfiguration().getServerName());
	}

	@Override
	public void close()
	{
		HandlerList.unregisterAll(this);
		plugin.getServer().getMessenger().unregisterIncomingPluginChannel(plugin);
		plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(plugin);
	}

	@Override
	protected void receiveUnknownChannel(@NotNull String channel, @NotNull byte[] bytes)
	{
		if (channel.equals(CHANNEL_BUNGEE_CORD))
		{
			try(DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes)))
			{
				if(in.readUTF().equalsIgnoreCase("GetServer"))
				{
					String server = in.readUTF();
					if(!server.equals(serverName)) // Only save if the name of the server has changed
					{
						setServerName(server);
						plugin.getConfiguration().setServerName(server);
					}
					serverNameUpdated = true;
				}
			}
			catch (IOException e)
			{
				logger.warning("Failed reading message from the bungee!");
				e.printStackTrace();
			}
		}
	}

	@Override
	protected boolean receiveMarriageMaster(@NotNull String cmd, @NotNull DataInputStream inputStream) throws IOException
	{
		switch(cmd)
		{
			case "update": plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "marry update"); break;
			case "reload": plugin.reload(); break;
			case "home":
				{
					MarriagePlayer toTP = plugin.getPlayerData(UUID.fromString(inputStream.readUTF()));
					Marriage marriage = toTP.getMarriageData(plugin.getPlayerData(UUID.fromString(inputStream.readUTF())));
					if(marriage == null || !toTP.isOnline()) return true;
					homeCommand.doTheTP(toTP, marriage);
				}
				break;
			case "delayHome":
				{
					Player player = plugin.getServer().getPlayer(UUID.fromString(inputStream.readUTF()));
					if(player != null) plugin.doDelayableTeleportAction(new DelayedAction("home", player, inputStream.readUTF()));
				}
				break;
			case "tp":
				{
					Player player = plugin.getServer().getPlayer(UUID.fromString(inputStream.readUTF()));
					Player target = plugin.getServer().getPlayer(UUID.fromString(inputStream.readUTF()));
					if(player != null && target != null && tpCommand != null) tpCommand.doTheTP(player, target);
				}
				break;
			case "delayTP":
				{
					Player player = plugin.getServer().getPlayer(UUID.fromString(inputStream.readUTF()));
					if(player != null) plugin.doDelayableTeleportAction(new DelayedAction("tp", player, inputStream.readUTF()));
				}
				break;
			//region Sync settings
			case "UseUUIDs":
				{
					String useUUIDsString = inputStream.readUTF();
					boolean useUUIdDs = Boolean.parseBoolean(useUUIDsString);
					if(!useUUIdDs)
					{
						logger.warning("Your BungeeCord version of Marriage Master is outdated! And you have disabled the UseUUIDs setting on BungeeCord! Changing config ...");
					}
				}
				break;
			case "UseUUIDSeparators":
				{
					boolean useUUIDSeparators = Boolean.parseBoolean(inputStream.readUTF());
					if(useUUIDSeparators != plugin.getConfiguration().useUUIDSeparators())
					{
						logger.warning("UseUUIDSeparators setting does not match value on BungeeCord! Changing config ...");
						plugin.getConfiguration().setUseUUIDSeparators(useUUIDSeparators);
						logger.info("UseUUIDSeparators setting has been set to " + useUUIDSeparators + " to match BungeeCord setting. Please restart the server or reload the plugin.");
					}
				}
				break;
			case "UUID_Type":
				{
					String type = inputStream.readUTF();
					if((type.equals("online") && !plugin.getConfiguration().useOnlineUUIDs()) || (type.equals("offline") && plugin.getConfiguration().useOnlineUUIDs()))
					{
						logger.warning("UUID_Type setting does not match value on BungeeCord! Changing config ...");
						plugin.getConfiguration().setUUIDType(type);
						logger.info("UUID_Type setting has been set to " + type + " to match BungeeCord setting. Please restart the server or reload the plugin.");
					}
				}
				break;
			//endregion
			default: return false;
		}
		return true;
	}

	@Override
	public void onPluginMessageReceived(@NotNull final String channel, @NotNull Player player, @NotNull byte[] bytes)
	{
		receive(channel, bytes);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerLoginEvent(PlayerJoinEvent event)
	{
		if(!serverNameUpdated)
		{
			sendMessage(CHANNEL_BUNGEE_CORD, buildStringMessage("GetServer"));
		}
		// If the server is empty and a player joins the server we have to do a re-sync
		if(plugin.getServer().getOnlinePlayers().size() == 1)
		{
			database.resync();
		}
	}

	//region send methods
	@Override
	public void sendMessage(final @NotNull byte[] data)
	{
		sendMessage(CHANNEL_MARRIAGE_MASTER, data);
	}

	private void sendMessage(String channel, byte[] data)
	{
		if(plugin.getServer().getOnlinePlayers().size() > 0)
		{
			plugin.getServer().getOnlinePlayers().iterator().next().sendPluginMessage(plugin, channel, data);
		}
		else
		{
			logger.warning("Failed to send PluginMessage, there is no player online!");
		}
	}
	//endregion

	//region helper classes
	private class DelayedAction implements DelayableTeleportAction
	{
		@Getter private final Player player;
		private final String command, partnerUUID;

		public DelayedAction(String command, Player player, String partnerUUID)
		{
			this.command = command;
			this.player = player;
			this.partnerUUID = partnerUUID;
		}

		@Override
		public void run()
		{
			sendMessage(command, player.getUniqueId().toString(), partnerUUID);
		}

		@Override
		public long getDelay()
		{
			return delayTime;
		}
	}
	//endregion
}