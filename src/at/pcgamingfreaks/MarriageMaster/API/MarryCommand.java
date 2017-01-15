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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public abstract class MarryCommand <PLUGIN extends MarriageMasterPlugin, COMMAND_SENDER>
{
	private String name, description, permission = null;
	private List<String> aliases;
	private static MarriageMasterPlugin marriagePlugin;
	private static Method showHelp;

	public MarryCommand(@NotNull String name, @NotNull String description, @Nullable String permission, @Nullable String... aliases)
	{
		this.name = name.toLowerCase();
		if(aliases != null)
		{
			this.aliases = new ArrayList<>(aliases.length + 1);
			for(String alias : aliases)
			{
				if(alias != null) this.aliases.add(alias.toLowerCase());
			}
		}
		else
		{
			this.aliases = new ArrayList<>(1);
		}
		if(!this.aliases.contains(this.name))
		{
			this.aliases.add(this.name);
		}
		this.description = description;
		this.permission = permission;
	}

	//region Getters
	/**
	 * Gets a list with the aliases of the command.
	 *
	 * @return List of aliases of the command.
	 */
	public @NotNull List<String> getAliases()
	{
		return this.aliases;
	}

	/**
	 * Gets the name of the command.
	 *
	 * @return The commands name.
	 */
	public @NotNull String getName()
	{
		return this.name;
	}

	/**
	 * Gets the name of the command in the used language.
	 *
	 * @return The commands name translated into the used language.
	 */
	public @NotNull String getTranslatedName()
	{
		return this.aliases.get(0);
	}

	/**
	 * Gets the permission that is needed for the command.
	 *
	 * @return The permission for the command.
	 */
	public @Nullable String getPermission()
	{
		return this.permission;
	}

	/**
	 * Gets the description of the command.
	 *
	 * @return The commands description.
	 */
	public @NotNull String getDescription()
	{
		return description;
	}

	/**
	 * Gets the instance of the marriage master plugin.
	 *
	 * @return The instance of the marriage master plugin.
	 */
	protected @NotNull PLUGIN getMarriagePlugin()
	{
		//noinspection unchecked
		return (PLUGIN) marriagePlugin;
	}
	//endregion

	//region Command Management Stuff
	public final void closeCommand()
	{
		close();
		name = null;
		aliases.clear();
		aliases = null;
		description = null;
	}

	/**
	 * The command got closed. Close stuff you no longer need.
	 */
	public void close() {}

	/**
	 * Allows to register commands that have to do with the current command (to be registered at the same time as the command).
	 * Example: /marry chat and /marry chattoggle.
	 *
	 * Also allows to execute code after the command is registered.
	 */
	public void registerSubCommands() {}

	/**
	 * Allows to un-register commands that have been registered at the same time as another command.
	 */
	public void unRegisterSubCommands() {}
	//endregion

	/**
	 * Checks if a user can use the command. Checks permissions, marriage status and player/console.
	 *
	 * @param sender The player/console that should be checked.
	 * @return True if he can use the command, false if not.
	 */
	public abstract boolean canUse(@NotNull COMMAND_SENDER sender);

	//region Command Help Stuff
	/**
	 * Executes some basic checks and gets the help.
	 *
	 * @param requester The command sender that requested help.
	 * @return The list of help data elements.
	 */
	public @Nullable List<HelpData> doGetHelp(@NotNull COMMAND_SENDER requester)
	{
		if(canUse(requester))
		{
			return getHelp(requester);
		}
		return null;
	}

	/**
	 * Shows the help to a given command sender.
	 *
	 * @param sendTo         The command sender that requested help.
	 * @param usedMarryAlias The used marry alias to replace the /marry with the used alias.
	 */
	public void showHelp(@NotNull COMMAND_SENDER sendTo, @NotNull String usedMarryAlias)
	{
		try
		{
			showHelp.invoke(getMarriagePlugin().getCommandManager(), sendTo, usedMarryAlias, doGetHelp(sendTo));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Gets the help for a given command sender.
	 *
	 * @param requester The command sender that requested help.
	 * @return All the help data for this command.
	 */
	public abstract @Nullable List<HelpData> getHelp(@NotNull COMMAND_SENDER requester);
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
	public abstract void doExecute(@NotNull COMMAND_SENDER sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args);

	/**
	 * Executes the given command returning its success.
	 *
	 * @param sender           Source of the command.
	 * @param mainCommandAlias Alias of the plugins main command which has been used.
	 * @param alias            Alias of the command which has been used.
	 * @param args             Passed command arguments.
	 */
	public abstract void execute(@NotNull COMMAND_SENDER sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args);
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
	public abstract List<String> doTabComplete(@NotNull COMMAND_SENDER sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args);

	/**
	 * Generates list for tab completion.
	 *
	 * @param sender  Source of the command.
	 * @param mainCommandAlias Alias of the plugins main command which was used.
	 * @param alias   The alias that is used.
	 * @param args    The arguments passed to the command, including final partial argument to be completed and command label.
	 * @return A List of possible completions for the final argument or null as default for the command executor.
	 */
	public abstract List<String> tabComplete(@NotNull COMMAND_SENDER sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args);
	//endregion
}