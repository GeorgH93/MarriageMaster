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

package at.pcgamingfreaks.MarriageMaster.Bukkit.API;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * The command manager is responsible for managing all the command stuff of the plugin.
 * It provides functions to register/unregister sub-commands, requests that need to be accepted and switches translated in the language file.
 */
@SuppressWarnings("unused")
public interface CommandManager extends at.pcgamingfreaks.MarriageMaster.API.CommandManager<MarryCommand, CommandSender>
{
	/**
	 * Registers an accept pending request.
	 *
	 * @param request The request to be registered.
	 * @return True if it was possible to register the request (the person to accept has no open requests or is online). False if not.
	 */
	boolean registerAcceptPendingRequest(@NotNull AcceptPendingRequest request);
}