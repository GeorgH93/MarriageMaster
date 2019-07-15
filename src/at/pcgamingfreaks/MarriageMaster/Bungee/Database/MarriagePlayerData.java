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

package at.pcgamingfreaks.MarriageMaster.Bungee.Database;

import at.pcgamingfreaks.Bungee.Message.Message;
import at.pcgamingfreaks.MarriageMaster.API.Home;
import at.pcgamingfreaks.MarriageMaster.Bungee.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bungee.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bungee.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Database.MarriagePlayerDataBase;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class MarriagePlayerData extends MarriagePlayerDataBase<MarriagePlayer, CommandSender, Home, Marriage, ProxiedPlayer, Message> implements MarriagePlayer
{
	public MarriagePlayerData(final @Nullable UUID uuid, final @NotNull String name)
	{
		super(uuid, name);
	}

	public MarriagePlayerData(final @Nullable UUID uuid, final @NotNull String name, final boolean priest)
	{
		super(uuid, name, priest);
	}

	public MarriagePlayerData(final @Nullable UUID uuid, final @NotNull String name, final boolean priest, final boolean sharesBackpack, final @Nullable Object databaseKey)
	{
		super(uuid, name, priest, sharesBackpack, databaseKey);
	}

	@Override
	public @Nullable String getOnlineName()
	{
		ProxiedPlayer player = getPlayer();
		return (player != null) ? player.getName() : null;
	}

	// API Functions
	@Override
	public @NotNull ProxiedPlayer getPlayer()
	{
		//TODO: Bungee has no offline player, so player can become null!
		return ProxyServer.getInstance().getPlayer(getUUID());
	}

	@Override
	public boolean hasPermission(@NotNull String permission)
	{
		return isOnline() && getPlayer().hasPermission(permission);
	}

	@Override
	public boolean isOnline()
	{
		return getPlayer() != null;
	}

	@Override
	public boolean isPartner(@NotNull ProxiedPlayer player)
	{
		return isPartner(MarriageMaster.getInstance().getPlayerData(player));
	}

	@Override
	public void send(@NotNull Message message, @Nullable Object... args)
	{
		sendMessage(message, args);
	}

	@Override
	public void sendMessage(@NotNull Message message, @Nullable Object... args)
	{
		if(!isOnline()) return;
		message.send(getPlayer(), args);
	}
	//endregion
}