/*
 *   Copyright (C) 2022 GeorgH93
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

package at.pcgamingfreaks.MarriageMaster.API;

import at.pcgamingfreaks.Message.IMessage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public interface MarriagePlayer<MARRIAGE extends Marriage, MARRIAGE_PLAYER extends MarriagePlayer, PLAYER_OFFLINE, PLAYER_ONLINE, MESSAGE extends IMessage>
{
	/**
	* Gets the Bukkit offline player that is represented by this marriage player data.
	*
	* @return The Bukkit player represented by this marriage player.
	*/
	@NotNull PLAYER_OFFLINE getPlayer();

	/**
	 * Gets the bukkit player represented by the marriage player.
	 *
	 * @return The represented player. Null if the player is offline.
	 */
	@Nullable PLAYER_ONLINE getPlayerOnline();

	/**
	 * Gets the name of the player represented by this marriage player data.
	 *
	 * @return The name of the player represented by this marriage player.
	 */
	@NotNull String getName();

	/**
	 * Gets the UUID of the player.
	 *
	 * @return The UUID of the player.
	 */
	@NotNull UUID getUUID();

	/**
	 * Checks if the player is online.
	 *
	 * @return True if the player is online, false if not.
	 */
	boolean isOnline();

	boolean canSee(PLAYER_ONLINE player);

	boolean canSee(MARRIAGE_PLAYER player);

	/**
	 * Gets the display name of the player represented by this marriage player data.
	 *
	 * @return The display name of the player represented by this marriage player.
	 */
	@NotNull String getDisplayName();

	/**
	 * Checks if a player has a certain permission.
	 *
	 * @param permission The permission to check.
	 * @return True if the player has the permission. False if not.
	 */
	boolean hasPermission(@NotNull String permission);

	/**
	 * Checks if the represented player is a priest.
	 * If the player is online the check will include the players permissions (Permissions.PRIEST permission).
	 * If the player is offline only the database will be checked.
	 *
	 * @return True if the player is a priest. False if not.
	 */
	boolean isPriest();

	void setPriest(boolean set);

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
	 * Checks if the player has set his private marry chat as default chat.
	 * This method is thread safe!
	 *
	 * @return True when messages written in the chat will be used as private messages. False if not.
	 */
	boolean isPrivateChatDefault();

	/**
	 * Sets how chat messages get treated.
	 * This method is thread safe!
	 *
	 * @param enable True if chat messages should be treated as private messages. False if not.
	 */
	void setPrivateChatDefault(boolean enable);

	/**
	 * Gets the partner that should receive the private message.
	 * This method is thread safe!
	 *
	 * @return Returns the marriage for the targeted partner.
	 */
	@Nullable MARRIAGE getPrivateChatTarget();

	/**
	 * Allows to change the target of private messages (when multipartner marriage is enabled).
	 * This method is thread safe!
	 *
	 * @param target The target which should receive the private messages in the future.
	 */
	void setPrivateChatTarget(@Nullable MARRIAGE_PLAYER target);

	/**
	 * Allows to change the target of private messages (when multipartner marriage is enabled).
	 * This method is thread safe!
	 *
	 * @param target The target which should receive the private messages in the future.
	 */
	void setPrivateChatTarget(@Nullable MARRIAGE target);

	/**
	 * Checks if a player is married.
	 *
	 * @return True if the player is married. False if not.
	 */
	boolean isMarried();

	/**
	 * Checks if the player is married to a given player.
	 *
	 * @param player The player to be checked.
	 * @return True if they are married. False if not.
	 */
	boolean isPartner(@NotNull PLAYER_OFFLINE player);

	/**
	 * Checks if the player is married to a given player.
	 *
	 * @param player The player to be checked.
	 * @return True if they are married. False if not.
	 */
	boolean isPartner(@NotNull MARRIAGE_PLAYER player);

	/**
	 * Gets the partner of the player.
	 * If multi marriage is enabled this will deliver 1 partner. If you need all of his partners use the getPartners function.
	 *
	 * @return The partner of the player. null if not married.
	 */
	@Nullable MARRIAGE_PLAYER getPartner();

	/**
	 * Gets the partner of the player from his name.
	 *
	 * @param name The name of the partner to be retrieved.
	 * @return The partner of the player. null if not married.
	 */
	@Nullable MARRIAGE_PLAYER getPartner(String name);

	/**
	 * Gets the partner of the player from his uuid.
	 *
	 * @param uuid The UUID of the partner to be retrieved.
	 * @return The partner of the player. null if not married.
	 */
	@Nullable MARRIAGE_PLAYER getPartner(UUID uuid);

	/**
	 * Gets the marriage data for the player.
	 *
	 * @return The marriage data of the player. null if he is not married.
	 */
	@Nullable MARRIAGE getMarriageData();

	/**
	 * Gets all the partners of the player.
	 * If multi marriage is not enabled this will also return the partner of the player.
	 *
	 * @return A {@link Collection} containing all partners of a player. Empty list if not married.
	 */
	@NotNull Collection<? extends MARRIAGE_PLAYER> getPartners();

	/**
	 * Gets the marriage data for all the players partners.
	 * If multi marriage is not enabled this will also return the marriage data for the player.
	 *
	 * @return A {@link Collection} containing the marriage data of the player for all his partners. Empty list if not married.
	 */
	@NotNull Collection<? extends MARRIAGE> getMultiMarriageData();

	/**
	 * Checks if the player is married to the other player and returns the corresponding marriage data if they are.
	 *
	 * @param player The player the marriage should get for.
	 * @return The marriage data for this partner. null if not married to this player.
	 */
	@Nullable MARRIAGE getMarriageData(@NotNull MARRIAGE_PLAYER player);

	/**
	 * Gets the partners of the player matching a given string.
	 *
	 * @param namePart Part of the partners name. Null to get all.
	 * @return The partners matching the given string.
	 */
	@NotNull List<String> getMatchingPartnerNames(@Nullable String namePart);

	/**
	 * Gets a list of all the players partners that are currently online.
	 *
	 * @return List of all online partners.
	 */
	default @NotNull Collection<? extends MARRIAGE_PLAYER> getOnlinePartners()
	{
		List<MARRIAGE_PLAYER> onlinePartners = new LinkedList<>();
		getPartners().forEach(partner -> { if(partner.isOnline()) onlinePartners.add(partner); });
		return onlinePartners;
	}

	/**
	 * Sends a message to the player.
	 *
	 * @param message The message to be sent to the player.
	 * @param args The arguments for the placeholders of the message.
	 */
	void send(@NotNull MESSAGE message, @Nullable Object... args);

	/**
	 * Sends a message to the player.
	 *
	 * @param message The message to be sent to the player.
	 * @param args The arguments for the placeholders of the message.
	 */
	void sendMessage(@NotNull MESSAGE message, @Nullable Object... args);
}