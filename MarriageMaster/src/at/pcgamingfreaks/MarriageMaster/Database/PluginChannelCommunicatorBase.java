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

package at.pcgamingfreaks.MarriageMaster.Database;

import at.pcgamingfreaks.ConsoleColor;
import at.pcgamingfreaks.Message.MessageColor;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.logging.Logger;

public abstract class PluginChannelCommunicatorBase
{
	protected static final String CHANNEL_MARRIAGE_MASTER = "marriagemaster:main", CHANNEL_BUNGEE_CORD = "BungeeCord";

	protected final Logger logger;
	protected final BaseDatabase database;

	protected PluginChannelCommunicatorBase(final Logger logger, final BaseDatabase database)
	{
		this.logger = logger;
		this.database = database;
		database.setCommunicatorBase(this);
	}

	public void close() {}

	protected static byte[] buildStringMessage(final @NotNull String... msg)
	{
		byte[] data = null;
		try(ByteArrayOutputStream stream = new ByteArrayOutputStream(); DataOutputStream out = new DataOutputStream(stream))
		{
			for(String param : msg)
			{
				out.writeUTF(param);
			}
			out.flush();
			data = stream.toByteArray();
		}
		catch(IOException ignored) {}
		return data;
	}

	/**
	 * Processes the message for information valid for all servers. If the message has not been processed an appropriate method will be called.
	 *
	 * @param channel The used plugin channel.
	 * @param bytes The data sent over the plugin channel.
	 * @return True if the received plugin message was an data update.
	 */
	protected boolean receive(final @NotNull String channel, final @NotNull byte[] bytes)
	{
		if(channel.equals(CHANNEL_MARRIAGE_MASTER))
		{
			try(DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes)))
			{
				String cmd = in.readUTF();
				switch(cmd)
				{
					case "updateHome":
						{
							MarriageDataBase marriage = database.getCache().getMarriageFromDbKey(Integer.parseInt(in.readUTF()));
							database.loadHome(marriage);
						}
						break;
					case "updatePvP":
						{
							MarriageDataBase marriage = database.getCache().getMarriageFromDbKey(Integer.parseInt(in.readUTF()));
							if(marriage != null)
							{
								marriage.updatePvPState(Boolean.parseBoolean(in.readUTF()));
							}
						}
						break;
					case "updateMarriageColor":
						{
							MarriageDataBase marriage = database.getCache().getMarriageFromDbKey(Integer.parseInt(in.readUTF()));
							if(marriage != null)
							{
								String color = in.readUTF();
								marriage.updateColor(color.equalsIgnoreCase("null") ? null : MessageColor.valueOf(color));
							}
						}
					break;
					case "updateSurname":
						{
							MarriageDataBase marriage = database.getCache().getMarriageFromDbKey(Integer.parseInt(in.readUTF()));
							if(marriage != null)
							{
								String surname = in.readUTF();
								marriage.updateSurname(surname.equals("null") ? null : surname);
							}
						}
						break;
					case "updateBackpackShare":
						{
							MarriagePlayerDataBase playerData = database.getCache().getPlayerFromDbKey(Integer.parseInt(in.readUTF()));
							if(playerData != null)
							{
								playerData.setSharesBackpack(Boolean.parseBoolean(in.readUTF()));
							}
						}
						break;
					case "updatePriestStatus":
						{
							MarriagePlayerDataBase playerData = database.getCache().getPlayerFromDbKey(Integer.parseInt(in.readUTF()));
							if(playerData != null)
							{
								playerData.setPriestData(Boolean.parseBoolean(in.readUTF()));
							}
						}
						break;
					case "updateMarry": database.loadMarriage(Integer.parseInt(in.readUTF())); break;
					case "updateDivorce":
						{
							MarriageDataBase marriage = database.getCache().getMarriageFromDbKey(Integer.parseInt(in.readUTF()));
							if(marriage != null)
							{
								marriage.updateDivorce();
								database.cachedDivorce(marriage, false);
							}
						}
						break;
					default:
						if(!receiveMarriageMaster(cmd, in))
						{
							logger.info(ConsoleColor.YELLOW + "Received unknown command via plugin channel! Command: " + cmd + "   " + ConsoleColor.RESET);
							logger.info("There are two likely reasons for that. 1. You are running an outdated version of the plugin. 2. Someone has connected to your server directly, check you setup!");
						}
						break;
				}
				if(cmd.startsWith("update")) return true;
			}
			catch (IOException e)
			{
				logger.warning("Failed reading message from the bungee!");
				e.printStackTrace();
			}
		}
		else
		{
			receiveUnknownChannel(channel, bytes);
		}
		return false;
	}

	protected abstract void receiveUnknownChannel(final @NotNull String channel, final @NotNull byte[] bytes);

	protected abstract boolean receiveMarriageMaster(final @NotNull String cmd, final @NotNull DataInputStream inputStream) throws IOException;

	protected abstract void sendMessage(final @NotNull byte[] data);

	public void sendMessage(final @NotNull String... msg)
	{
		sendMessage(buildStringMessage(msg));
	}

	//region update methods
	public void updateHome(MarriageDataBase marriage)
	{
		if(marriage.getDatabaseKey() != null) sendMessage("updateHome", marriage.getDatabaseKey().toString());
	}

	public void updatePvP(MarriageDataBase marriage)
	{
		if(marriage.getDatabaseKey() != null) sendMessage("updatePvP", marriage.getDatabaseKey().toString(), "" + marriage.isPVPEnabled());
	}

	public void updateMarriageColor(MarriageDataBase marriage)
	{
		if(marriage.getDatabaseKey() != null) sendMessage("updateMarriageColor", marriage.getDatabaseKey().toString(), "" + marriage.getColor());
	}

	public void updateSurname(MarriageDataBase marriage)
	{
		if(marriage.getDatabaseKey() != null) sendMessage("updateSurname", marriage.getDatabaseKey().toString(), (marriage.getSurname() != null) ? marriage.getSurname() : "null");
	}

	public void updateBackpackShareState(MarriagePlayerDataBase player)
	{
		if(player.getDatabaseKey() != null) sendMessage("updateBackpackShare", player.getDatabaseKey().toString(), "" + player.isSharingBackpack());
	}

	public void updatePriestStatus(MarriagePlayerDataBase player)
	{
		if(player.getDatabaseKey() != null) sendMessage("updatePriestStatus", player.getDatabaseKey().toString(), "" + player.isPriest());
	}

	public void marry(MarriageDataBase marriage)
	{
		if(marriage.getDatabaseKey() != null) sendMessage("updateMarry", marriage.getDatabaseKey().toString());
	}

	public void divorce(MarriageDataBase marriage)
	{
		if(marriage.getDatabaseKey() != null) sendMessage("updateDivorce", marriage.getDatabaseKey().toString());
	}
	//endregion
}