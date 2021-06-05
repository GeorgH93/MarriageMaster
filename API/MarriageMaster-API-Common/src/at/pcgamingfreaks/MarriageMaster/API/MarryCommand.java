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
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.MarriageMaster.API;

import at.pcgamingfreaks.Command.HelpData;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("unused")
public interface MarryCommand <PLUGIN extends MarriageMasterPlugin, COMMAND_SENDER>
{
	//region Getters
	/**
	 * Gets a list with the aliases of the command.
	 *
	 * @return List of aliases of the command.
	 */
	@NotNull List<String> getAliases();

	/**
	 * Gets the name of the command.
	 *
	 * @return The commands name.
	 */
	@NotNull String getName();

	/**
	 * Gets the name of the command in the used language.
	 *
	 * @return The commands name translated into the used language.
	 */
	@NotNull String getTranslatedName();

	/**
	 * Gets the permission that is needed for the command.
	 *
	 * @return The permission for the command.
	 */
	@Nullable String getPermission();

	/**
	 * Gets the description of the command.
	 *
	 * @return The commands description.
	 */
	@NotNull String getDescription();

	/**
	 * Gets the instance of the marriage master plugin.
	 *
	 * @return The instance of the marriage master plugin.
	 */
	@NotNull PLUGIN getMarriagePlugin();
	//endregion

	//region Command Management Stuff
	/**
	 * The command got closed. Close stuff you no longer need.
	 */
	void close();

	/**
	 * Allows to register commands that have to do with the current command (to be registered at the same time as the command).
	 * Example: /marry chat and /marry chattoggle.
	 *
	 * Also allows to execute code after the command is registered.
	 */
	void registerSubCommands();

	/**
	 * Allows to un-register commands that have been registered at the same time as another command.
	 */
	void unRegisterSubCommands();
	//endregion

	/**
	 * Checks if a user can use the command. Checks permissions, marriage status and player/console.
	 *
	 * @param sender The player/console that should be checked.
	 * @return True if he can use the command, false if not.
	 */
	boolean canUse(@NotNull COMMAND_SENDER sender);

	//region Command Help Stuff
	/**
	 * Executes some basic checks and gets the help.
	 *
	 * @param requester The command sender that requested help.
	 * @return The list of help data elements.
	 */
	@Nullable List<HelpData> doGetHelp(@NotNull COMMAND_SENDER requester);

	/**
	 * Shows the help to a given command sender.
	 *
	 * @param sendTo         The command sender that requested help.
	 * @param usedMarryAlias The used marry alias to replace the /marry with the used alias.
	 */
	void showHelp(@NotNull COMMAND_SENDER sendTo, @NotNull String usedMarryAlias);

	/**
	 * Gets the help for a given command sender.
	 *
	 * @param requester The command sender that requested help.
	 * @return All the help data for this command.
	 */
	@Nullable List<HelpData> getHelp(@NotNull COMMAND_SENDER requester);
	//endregion

	//region Command Execution Stuff
	/**
	 * Executes some basic checks and runs the command afterwards.
	 *
	 * @param sender           Source of the command.
	 * @param mainCommandAlias Alias of the plugins main command which has been used.
	 * @param alias            Alias of the command which has been used.
	 * @param args             Passed command arguments.
	 */
	void doExecute(@NotNull COMMAND_SENDER sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args);

	/**
	 * Executes the given command returning its success.
	 *
	 * @param sender           Source of the command.
	 * @param mainCommandAlias Alias of the plugins main command which has been used.
	 * @param alias            Alias of the command which has been used.
	 * @param args             Passed command arguments.
	 */
	void execute(@NotNull COMMAND_SENDER sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args);
	//endregion

	//region Tab Complete Stuff
	/**
	 * Executes some basic checks and generates list for tab completion.
	 *
	 * @param sender           Source of the command.
	 * @param mainCommandAlias Alias of the plugins main command which has been used.
	 * @param alias            The alias that has been used.
	 * @param args             The arguments passed to the command, including final partial argument to be completed and command label.
	 * @return A List of possible completions for the final argument or null as default for the command executor.
	 */
	List<String> doTabComplete(@NotNull COMMAND_SENDER sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args);

	/**
	 * Generates list for tab completion.
	 *
	 * @param sender  Source of the command.
	 * @param mainCommandAlias Alias of the plugins main command which was used.
	 * @param alias   The alias that is used.
	 * @param args    The arguments passed to the command, including final partial argument to be completed and command label.
	 * @return A List of possible completions for the final argument or null as default for the command executor.
	 */
	List<String> tabComplete(@NotNull COMMAND_SENDER sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args);
	//endregion
}