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

/**
 * The interface defines all the functions used for a delayed teleport with health/move check.
 */
@SuppressWarnings("unused")
public interface DelayableTeleportAction<PLAYER>
{
	/**
	 * The action that should be executed when the delay is over and all the checks were successful.
	 * Implement the teleport of the player here.
	 */
	void run();

	/**
	 * Defines the delay at which the action will be executed.
	 *
	 * @return The delay for the execution in ticks (1 second = 20 ticks).
	 *         <= 0 for instant execution without check for movement or damage.
	 */
	long getDelay();

	/**
	 * Defines the player which should be used for the checks.
	 * The given player will also receive the messages of the check.
	 *
	 * @return The player that should be used for the checks.
	 */
	@NotNull PLAYER getPlayer();
}