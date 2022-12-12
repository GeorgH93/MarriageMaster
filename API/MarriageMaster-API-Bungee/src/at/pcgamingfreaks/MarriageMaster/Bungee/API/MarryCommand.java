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

package at.pcgamingfreaks.MarriageMaster.Bungee.API;

import at.pcgamingfreaks.Bungee.Command.SubCommand;
import at.pcgamingfreaks.Bungee.Message.Message;
import at.pcgamingfreaks.Command.HelpData;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

@SuppressWarnings({"unused", "FieldCanBeLocal", "FieldMayBeFinal"})
public abstract class MarryCommand extends SubCommand implements at.pcgamingfreaks.MarriageMaster.API.MarryCommand<MarriageMasterPlugin, CommandSender>
{
	private static MarriageMasterPlugin marriagePlugin = null;
	protected final Plugin plugin;

	private static String helpPartnerSelector    = "<partner name>";
	private static Message messageNoPermission   = new Message(ChatColor.RED + "You don't have the permission to do that.");
	private static Message messageNotFromConsole = new Message(ChatColor.RED + "This command can't be used from console!");
	private static Message messageNotMarried     = new Message(ChatColor.RED + "You are not married!");
	private boolean playerOnly = false, mustBeMarried = false, partnerSelectorInHelp = false;
	private static Method showHelp;

	//region Constructors
	/**
	 * Creates a new command instance.
	 *
	 * @param plugin      The plugin owning the command.
	 * @param name        The command used.
	 * @param description The description of the command.
	 * @param aliases     List of aliases for that command.
	 */
	protected MarryCommand(@NotNull Plugin plugin, @NotNull String name, @NotNull String description, @Nullable String... aliases)
	{
		this(plugin, name, description, null, aliases);
	}

	/**
	 * Creates a new command instance.
	 *
	 * @param plugin      The plugin owning the command.
	 * @param name        The command used.
	 * @param description The description of the command.
	 * @param permission  The permission to be checked for this command. Players without the permission neither can use the command nor will they see it in help.
	 * @param aliases     List of aliases for that command.
	 */
	protected MarryCommand(@NotNull Plugin plugin, @NotNull String name, @NotNull String description, @Nullable String permission, @Nullable String... aliases)
	{
		super(name, description, permission, aliases);
		this.plugin = plugin;
	}

	/**
	 * Creates a new command instance.
	 *
	 * @param plugin      The plugin owning the command.
	 * @param name        The command used.
	 * @param description The description of the command.
	 * @param permission  The permission to be checked for this command. Players without the permission neither can use the command nor will they see it in help.
	 * @param playerOnly  Limits the command to players, console can't use and can't see the command.
	 * @param aliases     List of aliases for that command.
	 */
	protected MarryCommand(@NotNull Plugin plugin, @NotNull String name, @NotNull String description, @Nullable String permission, boolean playerOnly, @Nullable String... aliases)
	{
		this(plugin, name, description, permission, aliases);
		this.playerOnly = playerOnly;
	}

	/**
	 * Creates a new command instance.
	 *
	 * @param plugin        The plugin owning the command.
	 * @param name          The command used.
	 * @param description   The description of the command.
	 * @param permission    The permission to be checked for this command. Players without the permission neither can use the command nor will they see it in help.
	 * @param mustBeMarried Limits this command to players that are married, not married players will get a error message when using it and it won't appear in their help list.
	 * @param partnerSelectorInHelpForMoreThanOnePartner If the help should contain a partner name parameter (for polygamy).
	 * @param aliases       List of aliases for that command.
	 */
	protected MarryCommand(@NotNull Plugin plugin, @NotNull String name, @NotNull String description, @Nullable String permission, boolean mustBeMarried, boolean partnerSelectorInHelpForMoreThanOnePartner, @Nullable String... aliases)
	{
		this(plugin, name, description, permission, true, aliases);
		this.mustBeMarried = mustBeMarried;
		this.partnerSelectorInHelp = partnerSelectorInHelpForMoreThanOnePartner;
	}
	//endregion

	/**
	 * Gets the instance of the marriage master plugin.
	 *
	 * @return The instance of the marriage master plugin.
	 */
	@Override
	public @NotNull MarriageMasterPlugin getMarriagePlugin()
	{
		return marriagePlugin;
	}

	//region Command Stuff
	/**
	 * Executes some basic checks and runs the command afterwards.
	 *
	 * @param sender           Source of the command.
	 * @param mainCommandAlias Alias of the plugins main command which was used.
	 * @param alias            Alias of the command which was used.
	 * @param args             Passed command arguments.
	 */
	@Override
	public void doExecute(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String... args)
	{
		if(playerOnly && !(sender instanceof ProxiedPlayer))
		{
			messageNotFromConsole.send(sender);
		}
		else if(getPermission() != null && !sender.hasPermission(getPermission()))
		{
			messageNoPermission.send(sender);
		}
		else if(mustBeMarried && sender instanceof ProxiedPlayer && !getMarriagePlugin().getPlayerData((ProxiedPlayer) sender).isMarried())
		{
			messageNotMarried.send(sender);
		}
		else
		{
			execute(sender, mainCommandAlias, alias, args);
		}
	}

	/**
	 * Executes some basic checks and generates list for tab completion.
	 *
	 * @param sender           Source of the command.
	 * @param mainCommandAlias Alias of the plugins main command which was used.
	 * @param alias            The alias used.
	 * @param args             The arguments passed to the command, including final partial argument to be completed and command label.
	 * @return A List of possible completions for the final argument, or null to default to the command executor.
	 */
	@Override
	public List<String> doTabComplete(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String... args)
	{
		if(playerOnly && !(sender instanceof ProxiedPlayer))
		{
			messageNotFromConsole.send(sender);
		}
		else if(getPermission() == null || sender.hasPermission(getPermission()))
		{
			return tabComplete(sender, mainCommandAlias, alias, args);
		}
		return null;
	}

	/**
	 * Gets the help for a given command sender.
	 *
	 * @param requester The command sender that requested help.
	 * @return All the help data for this command.
	 */
	@Override
	public @Nullable List<HelpData> getHelp(@NotNull CommandSender requester)
	{
		List<HelpData> help = new LinkedList<>();
		if(partnerSelectorInHelp && requester instanceof ProxiedPlayer && getMarriagePlugin().getPlayerData((ProxiedPlayer) requester).getPartners().size() > 1)
		{
			help.add(new HelpData(getTranslatedName(), null, getDescription()));
		}
		else
		{
			help.add(new HelpData(getTranslatedName(), helpPartnerSelector, getDescription()));
		}
		return help;
	}

	/**
	 * Shows the help to a given command sender.
	 *
	 * @param sendTo         The command sender that requested help.
	 * @param usedMarryAlias The used marry alias to replace the /marry with the used alias.
	 */
	@Override
	public void showHelp(@NotNull CommandSender sendTo, @NotNull String usedMarryAlias)
	{
		try
		{
			showHelp.invoke(getMarriagePlugin().getCommandManager(), sendTo, usedMarryAlias, doGetHelp(sendTo));
		}
		catch(Exception e)
		{
			plugin.getLogger().log(Level.SEVERE, e, () -> "Failed to display help for user '" + sendTo.getName() + "'");
		}
	}

	/**
	 * Checks if a user can use the command. Checks permission, marriage status and player/console.
	 *
	 * @param sender The player/console that should be checked.
	 * @return True if he can use the command, false if not.
	 */
	@Override
	public boolean canUse(@NotNull CommandSender sender)
	{
		return (!playerOnly || sender instanceof ProxiedPlayer) && (getPermission() == null || sender.hasPermission(getPermission()) && (!mustBeMarried || (sender instanceof ProxiedPlayer && getMarriagePlugin().getPlayerData((ProxiedPlayer) sender).isMarried())));
	}
	//endregion
}