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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * {@link AcceptPendingRequest} has to be accepted by one player and can be cancelled by any number of players.
 * The requests must be registered in the {@link CommandManager}.
 */
@SuppressWarnings("unused")
public abstract class AcceptPendingRequest<T extends MarriagePlayer>
{
	private final T playerThatHasToAccept;
	private final T[] playersThatCanCancel;
	@SuppressWarnings("unused")
	private static Method closeMethod; // Workaround so that we can compile the API without the main plugin

	/**
	 * Creates a new instance of an {@link AcceptPendingRequest}.
	 *
	 * @param hasToAccept The player that has to accept the request.
	 * @param canCancel The players that can cancel the request.
	 */
	@SafeVarargs
	public AcceptPendingRequest(@NotNull T hasToAccept, @Nullable T... canCancel)
	{
		playerThatHasToAccept = hasToAccept;
		playersThatCanCancel = canCancel;
	}

	/**
	 * Gets the player that has to accept or deny the request.
	 *
	 * @return The player that has to accept or deny the request.
	 */
	public final @NotNull T getPlayerThatHasToAccept()
	{
		return playerThatHasToAccept;
	}

	/**
	 * Gets the players that can cancel the request.
	 *
	 * @return All the {@link at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer} for the players that can cancel the request.
	 */
	public final @Nullable T[] getPlayersThatCanCancel()
	{
		return playersThatCanCancel;
	}

	/**
	 * Checks if a given player can accept a request.
	 *
	 * @param player The player that should be checked.
	 * @return True if the player can accept the request, false if not.
	 */
	public final boolean canAccept(@NotNull T player)
	{
		return playerThatHasToAccept.equals(player);
	}

	/**
	 * Checks if a given player can deny a request.
	 *
	 * @param player The player that should be checked.
	 * @return True if the player can deny the request, false if not.
	 */
	public final boolean canDeny(@NotNull T player)
	{
		return playerThatHasToAccept.equals(player);
	}

	/**
	 * Checks if a given player can cancel a request.
	 *
	 * @param player The player that should be checked.
	 * @return True if the player can cancel the request, false if not.
	 */
	public final boolean canCancel(@NotNull T player)
	{
		if(playersThatCanCancel == null) return false;
		for(T p : playersThatCanCancel)
		{
			if(p.equals(player))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Accepts the request.
	 *
	 * @param player The player that accepts the request.
	 */
	public void accept(@NotNull T player)
	{
		if(canAccept(player))
		{
			close();
			onAccept();
		}
	}

	/**
	 * Denies the request.
	 *
	 * @param player The player that denies the request.
	 */
	public void deny(@NotNull T player)
	{
		if(canDeny(player))
		{
			close();
			onDeny();
		}
	}

	/**
	 * Cancels the request.
	 *
	 * @param player The player that cancels the request.
	 */
	public void cancel(@NotNull T player)
	{
		if(canCancel(player))
		{
			close();
			onCancel(player);
		}
	}

	/**
	 * Cancels the request because of a player disconnect.
	 *
	 * @param player The player that has left the server.
	 */
	public void disconnect(@NotNull T player)
	{
		if(canAccept(player) || canCancel(player))
		{
			close();
			onDisconnect(player);
		}
	}

	/**
	 * Will be executed when the request has been accepted.
	 */
	protected abstract void onAccept();

	/**
	 * Will be executed when the request has been denied.
	 */
	protected abstract void onDeny();

	/**
	 * Will be executed when the request has been cancelled.
	 *
	 * @param player The player that has cancelled the request.
	 */
	protected abstract void onCancel(@NotNull T player);

	/**
	 * Will be executed when the request has been cancelled because one of the players has left the game.
	 *
	 * @param player The player that has left the game.
	 */
	protected abstract void onDisconnect(@NotNull T player);

	/**
	 * Call this method as soon as the request has been accepted!
	 * If you don't close the request the receiving player can't receive any new requests and the accept/deny method may be called more than once!
	 * You shouldn't have to care about this method at all as long as you don't override the default accept/deny/cancel functions. They call the method for you.
	 */
	public final void close()
	{
		// Workaround so that we can compile the api without the main plugin
		try
		{
			closeMethod.invoke(playerThatHasToAccept);
			if(playersThatCanCancel == null) return;
			for(T p : playersThatCanCancel)
			{
				if(!p.equals(playerThatHasToAccept))
				{
					closeMethod.invoke(p);
				}
			}
		}
		catch(IllegalAccessException | InvocationTargetException e)
		{
			e.printStackTrace();
		}
	}
}