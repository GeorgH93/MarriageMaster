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

package at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events;

import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Event is fired right before two players get married. All the checks are done. We just await your approval. Both have already accepted the marriage!
 */
@SuppressWarnings("unused")
public class MarryEvent extends MarriageMasterCancellableEvent
{
	private final MarriagePlayer player1, player2;
	private final CommandSender priest;
	private final String surname;

	/**
	 * @param player1 The first player that should get married.
	 * @param player2 The second player that should get married.
	 * @param priest  The priest that would like to marry the players.
	 * @param surname The surname for the new couple.
	 */
	public MarryEvent(@NotNull MarriagePlayer player1, @NotNull MarriagePlayer player2, @Nullable CommandSender priest, @Nullable String surname)
	{
		this.player1 = player1;
		this.player2 = player2;
		this.priest  = priest;
		this.surname = surname;
	}

	/**
	 * Checks if the priest is a player.
	 *
	 * @return True if the priest is a player. False if the priest is the console or there is no priest at all. Self marriage will return true.
	 */
	public boolean isPriestAPlayer()
	{
		return (priest instanceof Player);
	}

	/**
	 * Gets the priest that would like to marry the players.
	 *
	 * @return The priest that would like to marry the players. null if there is no priest. Self marriage will return the player that requested the marriage.
	 */
	public @Nullable CommandSender getPriest()
	{
		return priest;
	}

	/**
	 * Gets the priest that would like to marry the players, if it is not one of them.
	 *
	 * @return The priest that would like to marry the players. null if there is no priest. A marriage started by one of the players will return null.
	 */
	public @Nullable CommandSender getPriestIfNotOneOfTheCouple()
	{
		return (player1.getPlayer().equals(priest) || player2.getPlayer().equals(priest)) ? null : priest;
	}

	/**
	 * Gets the first of the two players that should get married.
	 *
	 * @return The first of the two players that should get married.
	 */
	public @NotNull MarriagePlayer getPlayer1()
	{
		return player1;
	}

	/**
	 * Gets the second of the two players that should get married.
	 *
	 * @return The second of the two players that should get married.
	 */
	public @NotNull MarriagePlayer getPlayer2()
	{
		return player2;
	}

	/**
	 * Gets the surname the new couple will have.
	 *
	 * @return The surname of the new couple.
	 */
	public @Nullable String getSurname()
	{
		return surname;
	}


	// Bukkit handler stuff
	private static final HandlerList handlers = new HandlerList();

	@Override
	public @NotNull HandlerList getHandlers()
	{
		return getHandlerList();
	}

	public static HandlerList getHandlerList()
	{
		return handlers;
	}
}