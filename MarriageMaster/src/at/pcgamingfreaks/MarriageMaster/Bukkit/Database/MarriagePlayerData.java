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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Database;

import at.pcgamingfreaks.Bukkit.Message.IMessage;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.AcceptPendingRequest;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Home;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Commands.HugCommand;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Commands.KissCommand;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Database.MarriagePlayerDataBase;
import at.pcgamingfreaks.Message.MessageComponent;
import at.pcgf.libs.com.tcoded.folialib.wrapper.task.WrappedTask;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class MarriagePlayerData extends MarriagePlayerDataBase<MarriagePlayer, CommandSender, Home, Marriage, OfflinePlayer, Player, IMessage> implements MarriagePlayer
{
	private AcceptPendingRequest openRequest = null;
	private final List<AcceptPendingRequest> canCloseRequests = new LinkedList<>();
	@Getter	@Setter	private WrappedTask delayedTpTask = null;
	@Getter @Setter private long lastKissTime = 0, lastHugTime = 0;

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
		Player bukkitPlayer = getPlayerOnline();
		return (bukkitPlayer != null) ? bukkitPlayer.getDisplayName() : getOfflineDisplayName();
	}

	@Override
	public @NotNull String getDisplayNameCheckVanished(@NotNull Player player)
	{
		Player bukkitPlayer = getPlayerOnline();
		return (bukkitPlayer != null && player.canSee(bukkitPlayer)) ? bukkitPlayer.getDisplayName() : getOfflineDisplayName();
	}

	public @NotNull MessageComponent getDisplayNameMessageComponentCheckVanished(final @NotNull CommandSender checkFor)
	{
		if(!(checkFor instanceof Player)) return getDisplayNameMessageComponent();
		Player player = getPlayerOnline();
		return player != null && ((Player) checkFor).canSee(player) ? getDisplayNameMessageComponent() : offlineDisplayNameMessageComponent;
	}

	@Override
	public void kiss(MarriagePlayer partner)
	{
		if(isPartner(partner))
		{
			KissCommand.getInstance().kiss(this, partner);
		}
	}

	@Override
	public void hug(MarriagePlayer partner)
	{
		if(isPartner(partner))
		{
			HugCommand.getInstance().hug(this, partner);
		}
	}

	@Override
	public boolean hasPermission(@NotNull String permission)
	{
		Player bukkitPlayer = getPlayerOnline();
		return bukkitPlayer != null && bukkitPlayer.hasPermission(permission);
	}

	@Override
	public boolean isOnline()
	{
		return getPlayerOnline() != null;
	}

	@Override
	public boolean canSee(final @NotNull MarriagePlayer player)
	{
		return canSee(player.getPlayerOnline());
	}

	@Override
	public boolean canSee(final Player player)
	{
		Player onlinePlayer = getPlayerOnline();
		if(onlinePlayer == null || player == null) return true;
		return onlinePlayer.canSee(player);
	}

	@Override
	public long getLastPlayed()
	{
		return getPlayer().getLastPlayed();
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
	public @Nullable Player getPlayerOnline()
	{
		return Bukkit.getPlayer(getUUID());
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
			if((dist > 0 && dist < distanceNearest) || distanceNearest == -1)
			{
				distanceNearest = dist;
				nearest = marriage;
			}
		}
		return nearest;
	}

	@Override
	public @Nullable MarriagePlayer getNearestPartner()
	{
		Marriage nearest = getNearestPartnerMarriage();
		if(nearest == null) return null;
		return nearest.getPartner(this);
	}

	@Override
	public void send(@NotNull Object message, @Nullable Object... args)
	{
		sendMessage(message, args);
	}

	@Override
	public void sendMessage(@NotNull Object message, @Nullable Object... args)
	{
		if(!isOnline()) return;
		if (message instanceof IMessage)
		{
			//noinspection ConstantConditions
			((IMessage) message).send(getPlayerOnline(), args); // Is only null if the player is not online
		}
		else if (message instanceof String)
		{
			getPlayerOnline().sendMessage(String.format((String) message, args));
		}
	}
}