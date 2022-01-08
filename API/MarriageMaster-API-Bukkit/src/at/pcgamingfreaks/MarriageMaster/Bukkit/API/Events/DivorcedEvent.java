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
 * Event is fired after two players got divorced.
 */
public class DivorcedEvent extends MarriageMasterEvent
{
	private final MarriagePlayer player1, player2;
	private final CommandSender priest;

	/**
	 * @param player1 The first player that just got divorced.
	 * @param player2 The second player that just got divorced.
	 */
	public DivorcedEvent(@NotNull MarriagePlayer player1, @NotNull MarriagePlayer player2)
	{
		this(player1, player2, null);
	}

	/**
	 * @param player1 The first player that just got divorced.
	 * @param player2 The second player that just got divorced.
	 * @param priest The priest that has divorced the players.
	 */
	public DivorcedEvent(@NotNull MarriagePlayer player1, @NotNull MarriagePlayer player2, @Nullable CommandSender priest)
	{
		this.player1 = player1;
		this.player2 = player2;
		this.priest = priest;
	}

	/**
	 * Checks if the priest is a player.
	 *
	 * @return True if the priest is a player. False if the priest is the console or there is no priest at all. Self marriage will return true.
	 */
	@SuppressWarnings("unused")
	public boolean isPriestAPlayer()
	{
		return priest instanceof Player;
	}

	/**
	 * Gets the priest that has divorced the players.
	 *
	 * @return The priest that has divorced the players. null if there is no priest. Self divorce will return the player that requested the divorce.
	 */
	public @Nullable CommandSender getPriest()
	{
		return priest;
	}

	/**
	 * Gets the first of the two players that got divorced.
	 *
	 * @return The first of the two players that got divorced.
	 */
	public @NotNull MarriagePlayer getPlayer1()
	{
		return player1;
	}

	/**
	 * Gets the second of the two players that got divorced.
	 *
	 * @return The second of the two players that got divorced.
	 */
	public @NotNull MarriagePlayer getPlayer2()
	{
		return player2;
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