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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Database;

import at.pcgamingfreaks.MarriageMaster.Bukkit.API.AcceptPendingRequest;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.MarriageMaster.Database.DatabaseElement;

import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SuppressWarnings({ "unused" })
public class MarriagePlayerData implements MarriagePlayer, DatabaseElement
{
	//private final OfflinePlayer player;
	private String name;
	private final UUID uuid;
	private final int hash;
	private boolean priest = false, sharesBackpack = false, married = false, privateChat = false;
	private Marriage privateChatTarget = null;
	private AcceptPendingRequest openRequest = null;
	private List<AcceptPendingRequest> canCloseRequests = new LinkedList<>();
	private Map<MarriagePlayer, Marriage> partnersMarriages = new HashMap<>();
	private Object databaseKey = null;
	private final Object syncDBKey = new Object(), syncPrivateChatTarget = new Object();

	public MarriagePlayerData(OfflinePlayer player)
	{
		this(player.getUniqueId(), player.getName());
	}

	public MarriagePlayerData(UUID uuid, String name)
	{
		this.uuid = uuid;
		this.name = name;
		this.hash = uuid.hashCode();
	}

	public MarriagePlayerData(UUID uuid, String name, Object databaseKey)
	{
		this(uuid, name);
		this.databaseKey = databaseKey;
	}

	public MarriagePlayerData(UUID uuid, String name, boolean sharesBackpack, boolean priest)
	{
		this(uuid, name);
		this.sharesBackpack = sharesBackpack;
		this.priest = priest;
	}

	public MarriagePlayerData(UUID uuid, String name, boolean sharesBackpack, boolean priest, Object databaseKey)
	{
		this(uuid, name, sharesBackpack, priest);
		this.databaseKey = databaseKey;
		this.name = name;
	}

	protected MarriagePlayerData(MarriagePlayerData toCopy)
	{
		this.hash              = toCopy.hash;
		this.uuid              = toCopy.uuid;
		this.name              = toCopy.name;
		this.priest            = toCopy.priest;
		this.married           = toCopy.married;
		this.sharesBackpack    = toCopy.sharesBackpack;
		this.privateChatTarget = toCopy.privateChatTarget;
		this.openRequest       = toCopy.openRequest;
		this.canCloseRequests  = toCopy.canCloseRequests;
		this.partnersMarriages = toCopy.partnersMarriages;
		this.databaseKey       = toCopy.databaseKey;
	}

	public void addRequest(AcceptPendingRequest request)
	{
		if(request.getPlayerThatHasToAccept().equals(this))
		{
			openRequest = request;
		}
		if(request.getPlayersThatCanCancel() != null)
		{
			for(MarriagePlayer p : request.getPlayersThatCanCancel())
			{
				if(this.equals(p))
				{
					canCloseRequests.add(request);
					break;
				}
			}
		}
	}

	public void closeRequest(AcceptPendingRequest request)
	{
		if(getOpenRequest() != null && getOpenRequest().equals(request))
		{
			openRequest = null;
		}
		Iterator<AcceptPendingRequest> iterator = getRequestsToCancel().iterator();
		while(iterator.hasNext())
		{
			if(iterator.next().equals(request))
			{
				iterator.remove();
			}
		}
	}

	public void addMarriage(Marriage marriage)
	{
		partnersMarriages.put(marriage.getPartner(this), marriage);
		married = true;
	}

	public void removeMarriage(Marriage marriage)
	{
		partnersMarriages.remove(marriage.getPartner(this));
		married = partnersMarriages.size() > 0;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public boolean equals(Object otherPlayer)
	{
		return otherPlayer instanceof MarriagePlayerData && uuid.equals(((MarriagePlayerData) otherPlayer).uuid);
	}

	@Override
	public int hashCode()
	{
		return hash;
	}

	@Override
	public Object getDatabaseKey()
	{
		synchronized(syncDBKey)
		{
			return databaseKey;
		}
	}

	public void setDatabaseKey(Object key)
	{
		synchronized(syncDBKey)
		{
			databaseKey = key;
		}
	}

	public void setSharesBackpack(boolean state)
	{
		sharesBackpack = state;
	}

	public void setIsPriest(boolean is)
	{
		priest = is;
	}

	public @Nullable String getOnlineName()
	{
		Player bPlayer = getPlayer().getPlayer();
		return isOnline() && bPlayer != null ? bPlayer.getName() : null;
	}

	// API Functions
	@Override
	public @NotNull OfflinePlayer getPlayer()
	{
		return Bukkit.getOfflinePlayer(uuid);
	}

	@Override
	public @NotNull String getName()
	{
		return name;
	}

	@Override
	public @NotNull UUID getUUID()
	{
		return uuid;
	}

	@Override
	public @NotNull String getDisplayName()
	{
		Player bukkitPlayer = Bukkit.getPlayer(uuid);
		return (bukkitPlayer != null) ? bukkitPlayer.getDisplayName() : ChatColor.GRAY + name;
	}

	@Override
	public boolean hasPermission(@NotNull String permission)
	{
		Player bukkitPlayer = Bukkit.getPlayer(uuid);
		return bukkitPlayer != null && bukkitPlayer.hasPermission(permission);
	}

	@Override
	public boolean isOnline()
	{
		return Bukkit.getPlayer(uuid) != null;
	}

	@Override
	public boolean isPriest()
	{
		return priest || hasPermission("marry.priest");
	}

	@Override
	public void setPriest(boolean set)
	{
		priest = set;
		if(databaseKey == null)
		{
			MarriagePlayerData player = (MarriagePlayerData) MarriageMaster.getInstance().getPlayerData(getPlayer());
			if(player.getDatabaseKey() != null)
			{
				player.setPriest(set);
				return;
			}
		}
		MarriageMaster.getInstance().getDB().updatePriestStatus(this);
	}

	@Override
	public boolean isPrivateChatDefault()
	{
		return privateChat;
	}

	@Override
	public void setPrivateChatDefault(boolean enable)
	{
		privateChat = enable;
		if(databaseKey == null)
		{
			MarriagePlayerData player = (MarriagePlayerData) MarriageMaster.getInstance().getPlayerData(getPlayer());
			if(player.getDatabaseKey() != null)
			{
				player.setPrivateChatDefault(enable);
			}
		}
	}

	@Override
	public Marriage getPrivateChatTarget()
	{
		synchronized(syncPrivateChatTarget)
		{
			return privateChatTarget;
		}
	}

	@Override
	public void setPrivateChatTarget(MarriagePlayer target)
	{
		synchronized(syncPrivateChatTarget)
		{
			privateChatTarget = getMarriageData(target);
		}
	}

	@Override
	public void setPrivateChatTarget(Marriage target)
	{
		privateChatTarget = target;
	}

	@Override
	public boolean isSharingBackpack()
	{
		return sharesBackpack;
	}

	@Override
	public void setShareBackpack(boolean share)
	{
		sharesBackpack = share;
		if(databaseKey == null)
		{
			MarriagePlayerData player = (MarriagePlayerData) MarriageMaster.getInstance().getPlayerData(getPlayer());
			if(player.getDatabaseKey() != null)
			{
				player.setShareBackpack(share);
				return;
			}
		}
		MarriageMaster.getInstance().getDB().updateBackpackShareState(this);
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
	public boolean isMarried()
	{
		return married;
	}

	@Override
	public boolean isPartner(@NotNull OfflinePlayer player)
	{
		Validate.notNull(player, "The player must not be null.");
		return isPartner(MarriageMaster.getInstance().getPlayerData(player));
	}

	@Override
	public boolean isPartner(@NotNull MarriagePlayer player)
	{
		Validate.notNull(player, "The player must not be null.");
		return getPartners().contains(player);
	}

	@Override
	public MarriagePlayer getPartner()
	{
		return (isMarried()) ? getPartners().iterator().next() : null;
	}

	@Override
	public MarriagePlayer getPartner(String name)
	{
		MarriagePlayer partner = MarriageMaster.getInstance().getPlayerData(name);
		return (isPartner(partner)) ? partner : null;
	}

	@Override
	public Marriage getMarriageData()
	{
		return (isMarried()) ? getMultiMarriageData().iterator().next() : null;
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
	public @NotNull Collection<MarriagePlayer> getPartners()
	{
		return partnersMarriages.keySet();
	}

	@Override
	public @NotNull Collection<Marriage> getMultiMarriageData()
	{
		return partnersMarriages.values();
	}

	@Override
	public Marriage getMarriageData(@NotNull MarriagePlayer player)
	{
		Validate.notNull(player, "The player must not be null!");
		return partnersMarriages.get(player);
	}

	@Override
	public @NotNull List<String> getMatchingPartnerNames(String namePart)
	{
		namePart = namePart.toLowerCase();
		List<String> names = new LinkedList<>();
		for(MarriagePlayer partner : getPartners())
		{
			if(partner.getPlayer().getName().toLowerCase().startsWith(namePart))
			{
				names.add(partner.getPlayer().getName());
			}
		}
		return names;
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
		message.send(getPlayer().getPlayer(), args);
	}
}