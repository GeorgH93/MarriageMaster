/*
 * Copyright (C) 2016 GeorgH93
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.MarriageMaster.Bukkit.Listener;

import at.pcgamingfreaks.ConsoleColor;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.DelayableTeleportAction;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Commands.HomeCommand;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Commands.TpCommand;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Database.MarriageData;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Database.SQL;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;

import org.bukkit.event.EventHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;

public class PluginChannelCommunicator implements PluginMessageListener, Listener
{
	private static final String CHANNEL_MARRIAGE_MASTER = "MarriageMaster", CHANNEL_BUNGEE_CORD = "BungeeCord";
	private static String serverName = null;

	private final MarriageMaster plugin;
	private final Queue<byte[]> messageQueue = new LinkedList<>();
	private final long delayTime;
	private final SQL database;
	private final Logger logger;
	private TpCommand tpCommand = null;
	private HomeCommand homeCommand = null;
	
	public PluginChannelCommunicator(MarriageMaster plugin)
	{
		this.plugin = plugin;
		logger = plugin.getLogger();
		if(plugin.getDatabase() instanceof SQL)
		{
			database = (SQL) plugin.getDatabase();
		}
		else
		{
			database = null;
			logger.warning(ConsoleColor.RED + "The used database backend dose not extend the SQL-Database provider! There is something going wrong!" + ConsoleColor.RESET);
		}
		delayTime = plugin.getConfiguration().getTPDelayTime() * 20L;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, CHANNEL_BUNGEE_CORD);
		plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, CHANNEL_BUNGEE_CORD, this);
		plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, CHANNEL_MARRIAGE_MASTER);
		plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, CHANNEL_MARRIAGE_MASTER, this);
	}

	public void close()
	{
		plugin.getServer().getMessenger().unregisterIncomingPluginChannel(plugin);
		plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(plugin);
	}

	public void setTpCommand(TpCommand instance)
	{
		tpCommand = instance;
	}

	public void setHomeCommand(HomeCommand instance)
	{
		homeCommand = instance;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] bytes)
	{
		try(DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes)))
	    {
			if (channel.equals(CHANNEL_MARRIAGE_MASTER))
			{
			    String[] args = in.readUTF().split("\\|");
				switch(args[0])
				{
					case "update": plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "marry update"); break;
					case "reload": plugin.reload(); break;
					case "home":
						if((args.length == 2 || (plugin.isPolygamyAllowed() && args.length == 3)) && homeCommand != null)
						{
							MarriagePlayer toTP = plugin.getPlayerData(args[1]);
							Marriage marriage = (args.length == 2) ? toTP.getMarriageData() : toTP.getMarriageData(plugin.getPlayerData(args[2]));
							if(marriage == null || !toTP.isOnline()) return;
							homeCommand.doTheTP(toTP, marriage);
						}
						break;
					case "delayHome":
						if(args.length == 2 || (plugin.isPolygamyAllowed() && args.length == 3))
						{
							Player target = plugin.getServer().getPlayerExact(args[1]);
							if(target != null) plugin.doDelayableTeleportAction(new DelayedAction("home", target, (args.length == 2) ? null : args[2]));
						}
						break;
					case "TP":
						if((args.length == 2 || (plugin.isPolygamyAllowed() && args.length == 3)) && tpCommand != null)
						{
							MarriagePlayer toTP = plugin.getPlayerData(args[1]);
							Marriage marriage = (args.length == 2) ? toTP.getMarriageData() : toTP.getMarriageData(plugin.getPlayerData(args[2]));
							//noinspection ConstantConditions
							if(marriage == null || !toTP.isOnline() || !marriage.getPartner(toTP).isOnline()) return;
							//noinspection ConstantConditions
							tpCommand.doTheTP(toTP.getPlayer().getPlayer(), marriage.getPartner(toTP).getPlayer().getPlayer());
						}
						break;
					case "delayTP":
						if(args.length == 2 || (plugin.isPolygamyAllowed() && args.length == 3))
						{
							Player target = plugin.getServer().getPlayerExact(args[1]);
							if(target != null) plugin.doDelayableTeleportAction(new DelayedAction("tp", target, (args.length == 2) ? null : args[2]));
						}
						break;
					case "updateHome":
						if(args.length == 2 && database != null)
						{
							MarriageData marriage = database.getCache().getMarriageFromDbKey(Integer.parseInt(args[1]));
							database.loadHome(marriage);
						}
						break;
					case "updatePvP":
						if(args.length == 3)
						{
							MarriageData marriage = database.getCache().getMarriageFromDbKey(Integer.parseInt(args[1]));
							if(marriage != null)
							{
								marriage.updatePvPState(Boolean.parseBoolean(args[2]));
							}
						}
						break;
					case "updateSurname":
						if(args.length == 3 && database != null)
						{
							MarriageData marriage = database.getCache().getMarriageFromDbKey(Integer.parseInt(args[1]));
							if(marriage != null)
							{
								marriage.updateSurname(args[2]);
							}
						}
						break;
					case "marry":
						if(args.length == 2)
						{
							database.loadMarriage(Integer.parseInt(args[1]));
						}
						break;
					case "divorce":
						if(args.length == 2)
						{
							MarriageData marriage = database.getCache().getMarriageFromDbKey(Integer.parseInt(args[1]));
							if(marriage != null)
							{
								marriage.updateDivorce();
							}
						}
						break;
					default:
						logger.info(ConsoleColor.YELLOW + "Received unknown command via plugin channel! Command: " + args[0] + "   " + ConsoleColor.RESET);
						logger.info("There are two likely reasons for that. 1. You are running an outdated version of the plugin. 2. Someone has connected to your server without directly, check you setup!");
						break;
				}
	        }
			else if (channel.equals(CHANNEL_BUNGEE_CORD))
			{
			    switch(in.readUTF())
			    {
			    	case "GetServer":
			    		if(serverName != null)
					    {
						    serverName = in.readUTF();
					    }
					    break;
			    }
			}
		}
		catch (IOException e)
		{
			logger.warning("Failed reading message from the bungee!");
			e.printStackTrace();
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerLoginEvent(PlayerJoinEvent event)
	{
		if(serverName == null)
		{
			sendMessage(CHANNEL_BUNGEE_CORD, "GetServer");
		}
		// If the server is empty and a player joins the server we have to do a resync
		if(plugin.getServer().getOnlinePlayers().size() == 0) //TODO validate
		{
			database.resync();
		}
	}

	public static String getServerName()
	{
		return serverName;
	}

	//region send methods
	public void sendMessage(String message)
	{
		sendMessage(CHANNEL_MARRIAGE_MASTER, message);
	}

	private void sendMessage(String channel, String message)
	{
		try(ByteArrayOutputStream stream = new ByteArrayOutputStream(); DataOutputStream out = new DataOutputStream(stream))
		{
			out.writeUTF(message);
			out.flush();
			messageQueue.add(stream.toByteArray());
		}
		catch(IOException ignored) {}
		while(plugin.getServer().getOnlinePlayers().size() > 0 && messageQueue.peek() != null)
		{
			plugin.getServer().getOnlinePlayers().iterator().next().sendPluginMessage(plugin, channel, messageQueue.poll());
		}
	}
	//endregion

	//region helper classes
	private class DelayedAction implements DelayableTeleportAction
	{
		private final Player player;
		private final String command, optionalParam;

		public DelayedAction(String command, Player player, String optionalParam)
		{
			this.command = command;
			this.player = player;
			this.optionalParam = optionalParam;
		}

		@Override
		public void run()
		{
			sendMessage(command + '|' + player.getName() + ((optionalParam != null) ? '|' + optionalParam : ""));
		}

		@Override
		public @NotNull Player getPlayer()
		{
			return player;
		}

		@Override
		public long getDelay()
		{
			return delayTime;
		}
	}
	//endregion
}