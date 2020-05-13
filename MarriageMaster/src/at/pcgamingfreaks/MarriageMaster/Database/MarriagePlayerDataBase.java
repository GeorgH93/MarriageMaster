/*
 *   Copyright (C) 2020 GeorgH93
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

package at.pcgamingfreaks.MarriageMaster.Database;

import at.pcgamingfreaks.MarriageMaster.API.Home;
import at.pcgamingfreaks.MarriageMaster.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Permissions;
import at.pcgamingfreaks.Message.IMessage;
import at.pcgamingfreaks.UUIDConverter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class MarriagePlayerDataBase<MARRIAGE_PLAYER extends MarriagePlayer, COMMAND_SENDER, HOME extends Home, MARRIAGE extends Marriage<MARRIAGE_PLAYER, COMMAND_SENDER, HOME>, PLAYER, MESSAGE extends IMessage> implements DatabaseElement, MarriagePlayer<MARRIAGE, MARRIAGE_PLAYER, PLAYER, MESSAGE>
{
	@Getter @Setter	private String name;
	private final UUID uuid;
	private final int hash;
	private boolean priest = false, privateChat = false, sharesBackpack = false;
	@Getter private boolean married = false;
	@Getter @Setter	private Object databaseKey = null;
	private MARRIAGE privateChatTarget = null;
	private final Map<MARRIAGE_PLAYER, MARRIAGE> partnersMarriages = new ConcurrentHashMap<>();

	//region Constructor
	protected MarriagePlayerDataBase(final @Nullable UUID uuid, final @NotNull String name)
	{
		this.name = name;
		this.uuid = (uuid != null) ? uuid : UUIDConverter.getUUIDFromNameAsUUID(name, false);
		this.hash = this.uuid.hashCode();
	}

	protected MarriagePlayerDataBase(final @Nullable UUID uuid, final @NotNull String name, boolean priest)
	{
		this(uuid, name);
		this.priest = priest;
	}

	protected MarriagePlayerDataBase(final @Nullable UUID uuid, final @NotNull String name, boolean priest, final boolean sharesBackpack, final @Nullable Object databaseKey)
	{
		this(uuid, name, priest);
		this.sharesBackpack = sharesBackpack;
		this.databaseKey = databaseKey;
	}
	//endregion

	@Override
	public boolean equals(Object otherPlayer)
	{
		return otherPlayer instanceof MarriagePlayerDataBase && uuid.equals(((MarriagePlayerDataBase) otherPlayer).uuid);
	}

	@Override
	public int hashCode()
	{
		return hash;
	}

	//region set data (to update data changed externally)
	public void setPriestData(boolean is)
	{
		priest = is;
	}

	public void setSharesBackpack(boolean state)
	{
		sharesBackpack = state;
	}

	public void addMarriage(MARRIAGE marriage)
	{
		partnersMarriages.put(marriage.getPartner(this), marriage);
		married = true;
	}

	public void removeMarriage(MARRIAGE marriage)
	{
		partnersMarriages.remove(marriage.getPartner(this));
		married = partnersMarriages.size() > 0;
		if(marriage.equals(privateChatTarget)) privateChatTarget = null;
	}
	//endregion

	public abstract @Nullable String getOnlineName();

	//region API Methods
	@Override
	public @NotNull String getDisplayName()
	{
		return name;
	}

	@Override
	public @NotNull UUID getUUID()
	{
		return uuid;
	}

	@Override
	public boolean isPriest()
	{
		return priest || hasPermission(Permissions.PRIEST);
	}

	@Override
	public void setPriest(boolean set)
	{
		priest = set;
		BaseDatabase.getInstance().updatePriestStatus(this);
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
		BaseDatabase.getInstance().updateBackpackShareState(this);
	}

	@Override
	public void setPrivateChatDefault(boolean enable)
	{
		privateChat = enable;
	}

	@Override
	public boolean isPrivateChatDefault()
	{
		return privateChat && isMarried();
	}

	@Override
	public MARRIAGE getPrivateChatTarget()
	{
		return privateChatTarget;
	}

	@Override
	public void setPrivateChatTarget(MARRIAGE_PLAYER target)
	{
		privateChatTarget = getMarriageData(target);
	}

	@Override
	public void setPrivateChatTarget(MARRIAGE target)
	{
		privateChatTarget = target;
	}

	@Override
	public @NotNull List<String> getMatchingPartnerNames(String namePart)
	{
		namePart = namePart.toLowerCase();
		List<String> names = new LinkedList<>();
		for(MARRIAGE_PLAYER partner : getPartners())
		{
			if(partner.getName().toLowerCase().startsWith(namePart))
			{
				names.add(partner.getName());
			}
		}
		return names;
	}

	@Override
	public boolean isPartner(final @NotNull MARRIAGE_PLAYER player)
	{
		return getPartners().contains(player);
	}

	@Override
	public @Nullable MARRIAGE_PLAYER getPartner()
	{
		return (isMarried()) ? getPartners().iterator().next() : null;
	}

	@Override
	public @Nullable MARRIAGE getMarriageData()
	{
		return (isMarried()) ? getMultiMarriageData().iterator().next() : null;
	}

	@Override
	public @Nullable MARRIAGE_PLAYER getPartner(final @NotNull String name)
	{
		for(MARRIAGE_PLAYER partner : getPartners())
		{
			if(partner.getName().equalsIgnoreCase(name)) return partner;
		}
		return null;
	}

	@Override
	public @Nullable MARRIAGE_PLAYER getPartner(final @NotNull UUID uuid)
	{
		for(MARRIAGE_PLAYER partner : getPartners())
		{
			if(partner.getUUID().equals(uuid)) return partner;
		}
		return null;
	}

	@Override
	public @NotNull Collection<MARRIAGE_PLAYER> getPartners()
	{
		return partnersMarriages.keySet();
	}

	@Override
	public @NotNull Collection<MARRIAGE> getMultiMarriageData()
	{
		return partnersMarriages.values();
	}

	@Override
	public MARRIAGE getMarriageData(@NotNull MARRIAGE_PLAYER player)
	{
		return partnersMarriages.get(player);
	}
	//endregion
}