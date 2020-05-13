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

package at.pcgamingfreaks.MarriageMaster.Bukkit.API;

import at.pcgamingfreaks.Bukkit.Message.IMessage;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("unused")
public interface MarriagePlayer extends at.pcgamingfreaks.MarriageMaster.API.MarriagePlayer<Marriage, MarriagePlayer, OfflinePlayer, IMessage>
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
	 * Gets the bukkit player represented by the marriage player.
	 *
	 * @return The represented player. Null if the player is offline.
	 */
	@Nullable Player getPlayerOnline();
}