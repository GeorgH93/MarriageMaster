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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The command manager is responsible for managing all the command stuff of the plugin.
 * It provides functions to register/unregister sub-commands, requests that need to be accepted and switches translated in the language file.
 */
@SuppressWarnings("unused")
public interface CommandManager<MARRY_COMMAND extends MarryCommand, COMMAND_SENDER>
{
	/**
	 * Checks if a string is an on switch in the used language file.
	 *
	 * @param str The string that should be checked.
	 * @return True if it is an on switch, false if not.
	 */
	boolean isOnSwitch(@Nullable String str);

	/**
	 * Checks if a string is an off switch in the used language file.
	 *
	 * @param str The string that should be checked.
	 * @return True if it is an off switch, false if not.
	 */
	boolean isOffSwitch(@Nullable String str);

	/**
	 * Checks if a string is a toggle switch in the used language file.
	 *
	 * @param str The string that should be checked.
	 * @return True if it is a toggle switch, false if not.
	 */
	boolean isToggleSwitch(@Nullable String str);

	/**
	 * Checks if a string is an all switch in the used language file.
	 *
	 * @param str The string that should be checked.
	 * @return True if it is an all switch, false if not.
	 */
	boolean isAllSwitch(@Nullable String str);

	/**
	 * Checks if a string is a remove switch in the used language file.
	 *
	 * @param str The string that should be checked.
	 * @return True if it is a remove switch, false if not.
	 */
	boolean isRemoveSwitch(@Nullable String str);

	/**
	 * Gets the translation for the on switch.
	 *
	 * @return The translation for on.
	 */
	@NotNull String getOnSwitchTranslation();

	/**
	 * Gets the translation for the off switch.
	 *
	 * @return The translation for off.
	 */
	@NotNull String getOffSwitchTranslation();

	/**
	 * Gets the translation for the toggle switch.
	 *
	 * @return The translation for toggle.
	 */
	@NotNull String getToggleSwitchTranslation();

	/**
	 * Gets the translation for the all switch.
	 *
	 * @return The translation for all.
	 */
	@NotNull String getAllSwitchTranslation();

	/**
	 * Gets the translation for the remove switch.
	 *
	 * @return The translation for remove.
	 */
	@NotNull String getRemoveSwitchTranslation();

	/**
	 * Creates the tab complete list for polygamy and the partner names.
	 *
	 * @param sender The player that should receive the tab complete.
	 * @param args   The given args.
	 * @return The list of names for the tab complete event. Null if there is no matching partner.
	 */
	@Nullable List<String> getSimpleTabComplete(@NotNull COMMAND_SENDER sender, @Nullable String... args);

	/**
	 * Registers a new sub-command for /marry.
	 * The method is not available in standalone mode!
	 *
	 * @param command The command that should be registered.
	 */
	void registerSubCommand(@NotNull MARRY_COMMAND command);

	/**
	 * Unregisters a sub-command for /marry.
	 * The method is not available in standalone mode!
	 *
	 * @param command The command that should be unregistered.
	 */
	void unRegisterSubCommand(@NotNull MARRY_COMMAND command);
}