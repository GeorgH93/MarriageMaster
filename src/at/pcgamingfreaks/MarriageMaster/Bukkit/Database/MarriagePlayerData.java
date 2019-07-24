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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Database;

import at.pcgamingfreaks.Bukkit.Message.IMessage;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.AcceptPendingRequest;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Home;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Database.MarriagePlayerDataBase;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class MarriagePlayerData extends MarriagePlayerDataBase<MarriagePlayer, CommandSender, Home, Marriage, OfflinePlayer, IMessage> implements MarriagePlayer
{
	private AcceptPendingRequest openRequest = null;
	private List<AcceptPendingRequest> canCloseRequests = new LinkedList<>();

	public MarriagePlayerData(final @NotNull OfflinePlayer player)
	{
		super(player.getUniqueId(), player.getName() != null ? player.getName() : "Unknown");
	}

	public MarriagePlayerData(final @Nullable UUID uuid, final @NotNull String name, final boolean sharesBackpack, final boolean priest, final @Nullable Object databaseKey)
	{
		super(uuid, name, priest, sharesBackpack, databaseKey);
	}

	public void addRequest(AcceptPendingRequest request)
	{
		if(request.getPlayerThatHasToAccept().equals(this))
		{
			openRequest = request;
		}
		for(MarriagePlayer p : request.getPlayersThatCanCancel())
		{
			if(this.equals(p))
			{
				canCloseRequests.add(request);
				break;
			}
		}
	}

	public void closeRequest(AcceptPendingRequest request)
	{
		if(getOpenRequest() != null && getOpenRequest().equals(request))
		{
			openRequest = null;
		}
		getRequestsToCancel().removeIf(acceptPendingRequest -> acceptPendingRequest.equals(request));
	}

	@Override
	public @Nullable String getOnlineName()
	{
		Player bPlayer = getPlayerOnline();
		return bPlayer != null ? bPlayer.getName() : null;
	}

	// API Functions
	@Override
	public @NotNull OfflinePlayer getPlayer()
	{
		return Bukkit.getOfflinePlayer(getUUID());
	}

	@Override
	public @NotNull String getDisplayName()
	{
		Player bukkitPlayer = Bukkit.getPlayer(getUUID());
		return (bukkitPlayer != null) ? bukkitPlayer.getDisplayName() : ChatColor.GRAY + getName();
	}

	@Override
	public boolean hasPermission(@NotNull String permission)
	{
		Player bukkitPlayer = Bukkit.getPlayer(getUUID());
		return bukkitPlayer != null && bukkitPlayer.hasPermission(permission);
	}

	@Override
	public boolean isOnline()
	{
		return Bukkit.getPlayer(getUUID()) != null;
	}

	@Override
	public AcceptPendingRequest getOpenRequest()
	{
		return openRequest;
	}

	@Override
	@NotNull public List<AcceptPendingRequest> getRequestsToCancel()
	{
		return canCloseRequests;
	}

	@Override
	public boolean isPartner(@NotNull OfflinePlayer player)
	{
		return isPartner(MarriageMaster.getInstance().getPlayerData(player));
	}


	@Override
	public Marriage getNearestPartnerMarriageData()
	{
		Marriage nearest = null;
		double distanceNearest = -1;
		for(Marriage marriage : getMultiMarriageData())
		{
			double dist = marriage.getDistance();
			if(dist < distanceNearest || distanceNearest == -1)
			{
				distanceNearest = dist;
				nearest = marriage;
			}
		}
		return nearest;
	}

	@Override
	public void send(@NotNull IMessage message, @Nullable Object... args)
	{
		sendMessage(message, args);
	}

	@Override
	public void sendMessage(@NotNull IMessage message, @Nullable Object... args)
	{
		if(!isOnline()) return;
		//noinspection ConstantConditions
		message.send(getPlayerOnline(), args); // Is only null if the player is not online
	}
}