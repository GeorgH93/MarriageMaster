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

package at.pcgamingfreaks.MarriageMaster.API;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public interface MarriageManager<MARRIAGE extends Marriage, MARRIAGE_PLAYER extends MarriagePlayer, COMMAND_SENDER, PLAYER>
{
	/**
	 * Removes all not allowed characters form the surname.
	 *
	 * @param surname The surname to be cleaned.
	 * @return The cleaned surname.
	 */
	@Nullable String cleanupSurname(@Nullable String surname);

	/**
	 * Checks if a surname is available.
	 *
	 * @param surname The surname to be checked.
	 * @return True if the surname is available, false if not.
	 */
	boolean isSurnameAvailable(@NotNull String surname);

	/**
	 * Checks if a surname is valid (not too long or too short and available).
	 *
	 * @param surname The surname to be checked.
	 * @return True if the surname is valid, false if not.
	 */
	boolean isSurnameValid(@NotNull String surname);

	/**
	 * Checks if the length of the surname is not too long or too short.
	 *
	 * @param surname The surname to be checked.
	 * @return True if the length of the surname is valid.
	 */
	boolean isSurnameLengthValid(@NotNull String surname);

	/**
	 * Changes the surname of a married couple.
	 *
	 * @param marriage The marriage where the surname should be changed.
	 * @param surname  The new surname, null to remove the surname.
	 */
	void setSurname(@NotNull MARRIAGE marriage, @Nullable String surname);

	/**
	 * Changes the surname of a married couple.
	 *
	 * @param marriage The marriage where the surname should be changed.
	 * @param surname  The new surname, null to remove the surname.
	 * @param changer  The player or console that has changed the surname.
	 */
	void setSurname(@NotNull MARRIAGE marriage, @Nullable String surname, @NotNull COMMAND_SENDER changer);

	/**
	 * Divorces a married couple. Divorce will be executed by the console.
	 *
	 * @param marriage  The married couple.
	 */
	void divorce(@NotNull MARRIAGE marriage);

	/**
	 * Divorces a married couple.
	 *
	 * @param marriage  The married couple.
	 * @param divorceBy The player or console divorcing the couple.
	 */
	void divorce(@NotNull MARRIAGE marriage, @NotNull COMMAND_SENDER divorceBy);

	/**
	 * Divorces a married couple.
	 *
	 * @param marriage  The married couple.
	 * @param divorceBy The player divorcing the couple.
	 */
	void divorce(@NotNull MARRIAGE marriage, @NotNull MARRIAGE_PLAYER divorceBy);

	/**
	 * Divorces a married couple.
	 *
	 * @param marriage  The married couple.
	 * @param divorceBy The player divorcing the couple.
	 * @param first     The first player to accept the divorce.
	 */
	void divorce(@NotNull MARRIAGE marriage, @NotNull MARRIAGE_PLAYER divorceBy, @NotNull MARRIAGE_PLAYER first);

	/**
	 * Starts a new marriage for two players. All the checks will be done, if enabled the players will have to accept.
	 *
	 * @param player1 The first player to be married.
	 * @param player2 The second player to be married.
	 */
	void marry(@NotNull PLAYER player1, @NotNull PLAYER player2);

	/**
	 * Starts a new marriage for two players. All the checks will be done, if enabled the players will have to accept.
	 *
	 * @param player1 The first player to be married.
	 * @param player2 The second player to be married.
	 * @param priest  The priest that marries the two players.
	 */
	void marry(@NotNull PLAYER player1, @NotNull PLAYER player2, @NotNull COMMAND_SENDER priest);

	/**
	 * Starts a new marriage for two players. All the checks will be done, if enabled the players will have to accept.
	 *
	 * @param player1 The first player to be married.
	 * @param player2 The second player to be married.
	 * @param surname The surname for the new married couple.
	 */
	void marry(@NotNull PLAYER player1, @NotNull PLAYER player2, @Nullable String surname);

	/**
	 * Starts a new marriage for two players. All the checks will be done, if enabled the players will have to accept.
	 *
	 * @param player1 The first player to be married.
	 * @param player2 The second player to be married.
	 * @param priest  The priest that marries the two players.
	 * @param surname The surname for the new married couple.
	 */
	void marry(@NotNull PLAYER player1, @NotNull PLAYER player2, @NotNull COMMAND_SENDER priest, @Nullable String surname);

	/**
	 * Starts a new marriage for two players. All the checks will be done, if enabled the players will have to accept.
	 *
	 * @param player1 The first player to be married.
	 * @param player2 The second player to be married.
	 */
	void marry(@NotNull MARRIAGE_PLAYER player1, @NotNull MARRIAGE_PLAYER player2);

	/**
	 * Starts a new marriage for two players. All the checks will be done, if enabled the players will have to accept.
	 *
	 * @param player1 The first player to be married.
	 * @param player2 The second player to be married.
	 * @param priest  The priest that marries the two players.
	 */
	void marry(@NotNull MARRIAGE_PLAYER player1, @NotNull MARRIAGE_PLAYER player2, @NotNull COMMAND_SENDER priest);

	/**
	 * Starts a new marriage for two players. All the checks will be done, if enabled the players will have to accept.
	 *
	 * @param player1 The first player to be married.
	 * @param player2 The second player to be married.
	 * @param priest  The priest that marries the two players.
	 */
	void marry(@NotNull MARRIAGE_PLAYER player1, @NotNull MARRIAGE_PLAYER player2, @NotNull MARRIAGE_PLAYER priest);

	/**
	 * Starts a new marriage for two players. All the checks will be done, if enabled the players will have to accept.
	 *
	 * @param player1 The first player to be married.
	 * @param player2 The second player to be married.
	 * @param surname The surname for the new married couple.
	 */
	void marry(@NotNull MARRIAGE_PLAYER player1, @NotNull MARRIAGE_PLAYER player2, @Nullable String surname);

	/**
	 * Starts a new marriage for two players. All the checks will be done, if enabled the players will have to accept.
	 *
	 * @param player1 The first player to be married.
	 * @param player2 The second player to be married.
	 * @param priest  The priest that marries the two players.
	 * @param surname The surname for the new married couple.
	 */
	void marry(@NotNull MARRIAGE_PLAYER player1, @NotNull MARRIAGE_PLAYER player2, @NotNull COMMAND_SENDER priest, @Nullable String surname);

	/**
	 * Starts a new marriage for two players. All the checks will be done, if enabled the players will have to accept.
	 *
	 * @param player1 The first player to be married.
	 * @param player2 The second player to be married.
	 * @param priest  The priest that marries the two players.
	 * @param surname The surname for the new married couple.
	 */
	void marry(@NotNull MARRIAGE_PLAYER player1, @NotNull MARRIAGE_PLAYER player2, @NotNull MARRIAGE_PLAYER priest, @Nullable String surname);
}