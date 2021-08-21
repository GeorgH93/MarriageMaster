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
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.MarriageMaster.Bukkit.API;

import at.pcgamingfreaks.Bukkit.Message.IMessage;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface MarriagePlayer extends at.pcgamingfreaks.MarriageMaster.API.MarriagePlayer<Marriage, MarriagePlayer, OfflinePlayer, Player, IMessage>
{
	/**
	 * Gets the marriage with the partner that is the nearest to the player.
	 *
	 * @return The marriage with the nearest partner, random marriage if none of the partners is online. null if not married.
	 */
	@Nullable Marriage getNearestPartnerMarriageData();

	/**
	 * Gets the marriage with the partner that is the nearest to the player.
	 *
	 * @return The marriage with the nearest partner, random marriage if none of the partners is online. null if not married.
	 */
	default @Nullable Marriage getNearestPartnerMarriage()
	{
		return getNearestPartnerMarriageData();
	}

	/**
	 * Gets the partner that is the nearest to the player.
	 *
	 * @return The partner nearest to the player, a random partner if none of the partners is online. null if not married.
	 */
	@Nullable MarriagePlayer getNearestPartner();

	/**
	 * Gets the open request the player can accept or deny.
	 *
	 * @return The open request. Null = no open request
	 */
	@Nullable AcceptPendingRequest getOpenRequest();

	/**
	 * Gets all open requests the player can cancel.
	 *
	 * @return The requests the player can cancel.
	 */
	@NotNull List<AcceptPendingRequest> getRequestsToCancel();

	/**
	 * Gets the display name of a player. But also checks if the given player can see the player. If not the offline display name will be used.
	 *
	 * @param player The player that should be checked for visibility
	 * @return The display name of the player. If the player is offline or the given player can't see the player, the offline display name will be used.
	 */
	@NotNull String getDisplayNameCheckVanished(@NotNull Player player);

	/**
	 * Gets the time that the player has last kissed its partner.
	 *
	 * @return The time the player has last kissed its partner in milliseconds since midnight 1970-01-01 UTC.
	 */
	long getLastKissTime();

	/**
	 * Sets the time that the player has last kissed its partner.
	 *
	 * @param kissTime The time that the player has last kissed its partner in milliseconds since midnight 1970-01-01 UTC.
	 */
	void setLastKissTime(long kissTime);

	/**
	 * Makes the player kiss its partner.
	 *
	 * @param partner The partner that should be kissed.
	 */
	void kiss(MarriagePlayer partner);

	/**
	 * Gets the time that the player has last hugged its partner.
	 * The time is reset on server restart.
	 *
	 * @return The time the player has last hugged its partner in milliseconds since midnight 1970-01-01 UTC.
	 */
	long getLastHugTime();

	/**
	 * Sets the time that the player has last hugged its partner.
	 * The time is reset on server restart.
	 *
	 * @param hugTime The time that the player has last hugged its partner in milliseconds since midnight 1970-01-01 UTC.
	 */
	void setLastHugTime(long hugTime);

	/**
	 * Makes the player hug its partner.
	 *
	 * @param partner The partner that should be hugged.
	 */
	void hug(MarriagePlayer partner);
}