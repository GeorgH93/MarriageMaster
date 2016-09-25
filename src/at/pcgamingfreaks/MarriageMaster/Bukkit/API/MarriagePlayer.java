/*
 *   Copyright (C) 2016 GeorgH93
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

import at.pcgamingfreaks.Bukkit.Message.Message;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public interface MarriagePlayer extends at.pcgamingfreaks.MarriageMaster.API.MarriagePlayer<Marriage, MarriagePlayer, AcceptPendingRequest, OfflinePlayer, Message>
{
	/**
	 * Checks if the player is sharing his backpack with his partner.
	 *
	 * @return True if the player is sharing the backpack. False if not.
	 */
	boolean isSharingBackpack();

	/**
	 * Sets the sharing status of the players backpack.
	 *
	 * @param share True if the backpack should be shared. False if not.
	 */
	void setShareBackpack(boolean share);

	/**
	 * Gets the partner that is the nearest to the player. Only if both players are in the same world.
	 *
	 * @return The nearest partner. null if not married, partner is offline or partner is in an other world.
	 */
	@Nullable Marriage getNearestPartnerMarriageData();
}