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

package at.pcgamingfreaks.MarriageMaster.API;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

@SuppressWarnings("unused")
public interface MarriageMasterPlugin<PLAYER, MARRIAGE_PLAYER extends  MarriagePlayer, MARRIAGE extends Marriage, MARRIAGE_MANAGER extends MarriageManager, COMMAND_MANAGER extends CommandManager, DELAYABLE_TELEPORT_ACTION extends DelayableTeleportAction>
{
	/**
	 * Checks if the plugin is running in standalone mode.
	 *
	 * @return True if the plugin is running in standalone mode, false if not.
	 */
	default boolean isRunningInStandaloneMode()
	{
		return false;
	}

	/**
	 * Retrieves the {@link MarriagePlayer} from a player.
	 *
	 * @param player The player for which the {@link MarriagePlayer} should be retrieved.
	 * @return The {@link MarriagePlayer} of the Bukkit player.
	 */
	@NotNull MARRIAGE_PLAYER getPlayerData(@NotNull PLAYER player);

	/**
	 * Retrieves the {@link MarriagePlayer} from an {@link UUID}.
	 *
	 * @param uuid The {@link UUID} for which the {@link MarriagePlayer} should be retrieved.
	 * @return The {@link MarriagePlayer} for {@link UUID}.
	 */
	@NotNull MARRIAGE_PLAYER getPlayerData(@NotNull UUID uuid);

	/**
	 * Retrieves the {@link MarriagePlayer} from a name.
	 *
	 * <b>Warning:</b> Players may change their name, this method will only search the last known name for all of the players.
	 * It's highly recommended to only use this method for user inputs!
	 *
	 * @param name The name of the player for whom the {@link MarriagePlayer} should be retrieved.
	 * @return The {@link MarriagePlayer} of the player.
	 */
	@NotNull MARRIAGE_PLAYER getPlayerData(@NotNull String name);

	/**
	 * Retrieves all the registered marriages.
	 *
	 * @return A {@link Collection} containing all the registered marriages.
	 */
	@NotNull Collection<? extends MARRIAGE> getMarriages();

	/**
	 * Checks if polygamy (marrying more then one player) is enabled on the server.
	 *
	 * @return True if polygamy is enabled. False if not.
	 */
	boolean isPolygamyAllowed();

	/**
	 * Checks if players can marry other players directly without the need for a priest.
	 *
	 * @return True if players can marry without a priest. False if not.
	 */
	boolean isSelfMarriageAllowed();

	/**
	 * Checks if players can divorce form their partners directly without the need for a priest.
	 *
	 * @return True if players can divorce himself without a priest. False if not.
	 */
	boolean isSelfDivorceAllowed();

	/**
	 * Checks if surnames are enabled.
	 *
	 * @return True if surnames are enabled. False if not.
	 */
	boolean isSurnamesEnabled();

	/**
	 * Checks if surnames are forced (every married player has to have a surname).
	 *
	 * @return True if surnames are forced, false if not.
	 */
	boolean isSurnamesForced();

	/**
	 * Function to run the tp delay with movement/health checking.
	 *
	 * @param action The action that should be executed.
	 */
	void doDelayableTeleportAction(@NotNull DELAYABLE_TELEPORT_ACTION action);

	/**
	 * Gets the marriage manager. Needed to do stuff like marry, divorce or change surnames.
	 *
	 * @return The marriage manager.
	 */
	@NotNull MARRIAGE_MANAGER getMarriageManager();

	/**
	 * Gets the command manager of the plugin. Needed to register sub-commands.
	 *
	 * @return The Marriage Master Command Manager. null if the plugin is running in standalone mdoe.
	 */
	@Nullable COMMAND_MANAGER getCommandManager();
}