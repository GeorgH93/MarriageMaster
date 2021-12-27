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

package at.pcgamingfreaks.MarriageMaster.API;

import at.pcgamingfreaks.Message.MessageColor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

public interface Marriage <MARRIAGE_PLAYER extends MarriagePlayer, COMMAND_SENDER, HOME extends Home>
{
	/**
	 * Gets one of the married players.
	 *
	 * @return The first player of the married couple.
	 */
	@NotNull MARRIAGE_PLAYER getPartner1();

	/**
	 * Gets one of the married players.
	 *
	 * @return The second player of the married couple.
	 */
	@NotNull MARRIAGE_PLAYER getPartner2();

	/**
	 * Checks if both players of the marriage are online.
	 *
	 * @return True if both are online, false if not.
	 */
	boolean isBothOnline();

	/**
	 * Gets the other player of the married players.
	 *
	 * @param player The player for which the partner should be retrieved.
	 * @return The other player of the married couple. Null if the given player is not in this marriage.
	 */
	@Nullable MARRIAGE_PLAYER getPartner(@NotNull MarriagePlayer player);

	/**
	 * Gets the surname of the married players.
	 *
	 * @return The surname of the couple. Null if no surname is set.
	 */
	@Nullable String getSurname();

	/**
	 * Gets the surname of the married players.
	 *
	 * @return The surname of the couple. Empty string if no surname is set.
	 */
	default @NotNull String getSurnameString()
	{
		String surname = getSurname();
		return surname != null ? surname : "";
	}

	/**
	 * Sets the surname of the married players.
	 *
	 * @param surname The new surname for the couple. null to remove surname.
	 * @return If the surname was changed successfully. false if the surname is already in use or is too short/long.
	 */
	boolean setSurname(@Nullable String surname);

	/**
	 * Sets the surname of the married players.
	 *
	 * @param surname The new surname for the couple. null to remove surname.
	 * @param changer The command sender who has changed the surname.
	 */
	void setSurname(@Nullable String surname, @NotNull COMMAND_SENDER changer);

	/**
	 * Sets the surname of the married players.
	 *
	 * @param surname The new surname for the couple. null to remove surname.
	 * @param changer The {@link MarriagePlayer} who has changed the surname.
	 */
	void setSurname(@Nullable String surname, @NotNull MARRIAGE_PLAYER changer);

	/**
	 * Checks if the players have set a home point.
	 *
	 * @return True if the home is set. False if not.
	 */
	boolean isHomeSet();

	/**
	 * Check if PVP is enabled between the married players.
	 *
	 * @return Whether or not PVP is enabled between the married players.
	 */
	boolean isPVPEnabled();

	/**
	 * Sets the PVP state between the two married players.
	 *
	 * @param pvpEnabled Whether or not PVP should get enabled between the married players.
	 */
	void setPVPEnabled(boolean pvpEnabled);

	/**
	 * @return Returns the date and time when the players have married.
	 */
	@NotNull Date getWeddingDate();

	/**
	 * @return The priest who has married the two players. null if there was no priest (they got married via console or self marry).
	 */
	@Nullable MARRIAGE_PLAYER getPriest();

	/**
	 * Forces divorce of two players, no checks!
	 */
	void divorce();

	void divorce(@NotNull MARRIAGE_PLAYER divorcedBy);

	void divorce(@NotNull COMMAND_SENDER divorcedBy);

	/**
	 * Checks if a given player is involved in the marriage.
	 *
	 * @param player The player to check against.
	 * @return True if the given player is part of this marriage, false if not.
	 */
	boolean hasPlayer(@NotNull MARRIAGE_PLAYER player);

	/**
	 * Gets the home of the couple.
	 *
	 * @return The {@link HOME} of the couple. null if no home is set.
	 */
	@Nullable HOME getHome();

	/**
	 * Sets the home of the couple.
	 *
	 * @param home The {@link HOME} for the marriage. null to delete home.
	 */
	void setHome(@Nullable HOME home);

	/**
	 * Gets the prefix that will be added to every private (marriage) chat message.
	 * This string can contain color codes which will be send even if the player sending the message doesn't have the permission to use colors in the private chat.
	 *
	 * @return The prefix that will be added to every private (marriage) chat message.
	 */
	@NotNull String getMarriageChatMessagePrefix();

	/**
	 * Sets the prefix that will be added to every private (marriage) chat message.
	 * This string can contain color codes which will be send even if the player sending the message doesn't have the permission to use colors in the private chat.
	 * Prefix is limited to 20 chars, everything beyond will be ignored.
	 */
	void setMarriageChatMessagePrefix(@NotNull String prefix);

	/**
	 * Sets the color of the marriage, since there are only 16 colors available in Minecraft it is possible that two or more marriages have the same color.
	 *
	 * @param color the color that should be set for the marriage.
	 */
	void setColor(final @Nullable MessageColor color);

	/**
	 * Gets the color of the marriage, since there are only 16 colors available in Minecraft it is possible that two or more marriages have the same color.
	 * The default color is random. But can be changed.
	 *
	 * @return The color of the marriage.
	 */
	@NotNull MessageColor getColor();

	@NotNull String getMagicHeart();
}