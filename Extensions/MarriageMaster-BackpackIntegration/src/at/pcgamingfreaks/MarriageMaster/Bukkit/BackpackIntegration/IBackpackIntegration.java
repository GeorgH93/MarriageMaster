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

package at.pcgamingfreaks.MarriageMaster.Bukkit.BackpackIntegration;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface IBackpackIntegration
{
	/**
	 * Opens the backpack of another player.
	 *
	 * @param opener The player that opens the backpack.
	 * @param owner The owner of the backpack.
	 * @param editable Weather or not the player that has opened the backpack should be able to edit the content of the backpack.
	 */
	void openBackpack(final @NotNull Player opener, final @NotNull Player owner, boolean editable);

	/**
	 * Disables the integration with the backpack plugin.
	 */
	void close();

	/**
	 * Gets the name of the used backpack plugin.
	 *
	 * @return The name of the backpack plugin.
	 */
	@NotNull String getName();

	/**
	 * Gets the version of the used backpack plugin.
	 *
	 * @return The version of the backpack plugin.
	 */
	@NotNull String getVersion();

	boolean isBackpackItem(final ItemStack item);
}