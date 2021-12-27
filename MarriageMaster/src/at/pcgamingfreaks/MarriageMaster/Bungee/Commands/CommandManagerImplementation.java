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

package at.pcgamingfreaks.MarriageMaster.Bungee.Commands;

import at.pcgamingfreaks.Bungee.Command.CommandExecutorWithSubCommandsGeneric;
import at.pcgamingfreaks.Bungee.Message.Message;
import at.pcgamingfreaks.Command.HelpData;
import at.pcgamingfreaks.ConsoleColor;
import at.pcgamingfreaks.MarriageMaster.Bungee.API.CommandManager;
import at.pcgamingfreaks.MarriageMaster.Bungee.API.MarryCommand;
import at.pcgamingfreaks.MarriageMaster.Bungee.MarriageMaster;
import at.pcgamingfreaks.Reflection;
import at.pcgamingfreaks.StringUtils;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class CommandManagerImplementation extends CommandExecutorWithSubCommandsGeneric<MarryCommand> implements Listener, CommandManager
{
	private MarriageMaster plugin;
	private HashSet<String> commandAliases = new HashSet<>();
	private String[] switchesOn, switchesOff, switchesToggle, switchesAll, switchesRemove;
	private Message helpFormat;

	public CommandManagerImplementation(MarriageMaster plugin)
	{
		this.plugin = plugin;
	}

	public void init()
	{
		// Loading data
		switchesOn      = plugin.getLanguage().getSwitch("On",     "on");
		switchesOff     = plugin.getLanguage().getSwitch("Off",    "off");
		switchesToggle  = plugin.getLanguage().getSwitch("Toggle", "toggle");
		switchesAll     = plugin.getLanguage().getSwitch("All",    "all");
		switchesRemove  = plugin.getLanguage().getSwitch("Remove", "remove");

		// Registering the marriage command with our translated aliases
		commandAliases.add("marry");
		plugin.getLanguage().getCommandAliases("marry");
		for(String alias : plugin.getLanguage().getCommandAliases("marry"))
		{
			commandAliases.add(alias.toLowerCase());
		}
		plugin.getProxy().getPluginManager().registerListener(plugin, this);

		helpFormat = plugin.getLanguage().getMessage("Commands.HelpFormat").replaceAll("\\{MainCommand\\}", "%1\\$s").replaceAll("\\{SubCommand\\}", "%2\\$s").replaceAll("\\{Parameters\\}", "%3\\$s").replaceAll("\\{Description\\}", "%4\\$s");

		// Setting the help format for the marry commands as well as the no permissions and not from console message
		try
		{
			// Show help function
			Reflection.setStaticField(MarryCommand.class, "showHelp", this.getClass().getDeclaredMethod("sendHelp", CommandSender.class, String.class, Collection.class));
			Reflection.setStaticField(MarryCommand.class, "marriagePlugin", plugin); // Plugin instance
			Reflection.setStaticField(MarryCommand.class, "messageNoPermission", plugin.messageNoPermission); // No permission message
			Reflection.setStaticField(MarryCommand.class, "messageNotMarried", plugin.messageNotMarried); // Not married message
			Reflection.setStaticField(MarryCommand.class, "messageNotFromConsole", plugin.messageNotFromConsole); // Not from console message
			Reflection.setStaticField(MarryCommand.class, "helpPartnerSelector", "<" + plugin.helpPartnerNameVariable + ">"); // Help partner selector
		}
		catch(Exception e)
		{
			plugin.getLogger().warning(ConsoleColor.RED + "Unable to set the help format. Default format will be used.\nMore details:" + ConsoleColor.RESET);
			e.printStackTrace();
		}

		// Init MarriageMaster commands
		if(plugin.getConfig().isChatHandlerEnabled())
		{
			registerSubCommand(new ChatCommand(plugin));
		}
		if(plugin.getConfig().isTPHandlerEnabled())
		{
			registerSubCommand(new TpCommand(plugin));
		}
		if(plugin.getConfig().isHomeHandlerEnabled())
		{
			registerSubCommand(new HomeCommand(plugin));
		}
		if(plugin.getConfig().useUpdater())
		{
			registerSubCommand(new UpdateCommand(plugin));
		}
		//registerSubCommand(new ReloadCommand(plugin));
	}

	@Override
	public void close()
	{
		plugin.getProxy().getPluginManager().unregisterListener(this);
		super.close();
	}

	@SuppressWarnings("unused")
	public void sendHelp(CommandSender target, String marryAlias, Collection<HelpData> data)
	{
		for(HelpData d : data)
		{
			helpFormat.send(target, marryAlias, d.getTranslatedSubCommand(), d.getParameter(), d.getDescription());
		}
	}

	@SuppressWarnings("unused")
	@EventHandler(priority = EventPriority.LOWEST)
	public void onChat(ChatEvent event)
	{
		if(event.isCommand())
		{
			String[] args = event.getMessage().split("\\s+");
			String cmd = args[0].toLowerCase().substring(1);
			if(commandAliases.contains(cmd) && args.length > 1)
			{
				args = Arrays.copyOfRange(args, 1, args.length);
				if(args.length > 0)
				{
					MarryCommand mC = subCommandMap.get(args[0].toLowerCase());
					if(mC != null)
					{
						//TODO: Command for the console
						mC.doExecute((CommandSender) event.getSender(), cmd, args[0], (args.length > 1) ? Arrays.copyOfRange(args, 1, args.length) : new String[0]);
						event.setCancelled(true);
					}
				}
			}
		}
	}

	@Override
	public boolean isOnSwitch(String str)
	{
		return StringUtils.arrayContainsIgnoreCase(switchesOn, str);
	}

	@Override
	public boolean isOffSwitch(String str)
	{
		return StringUtils.arrayContainsIgnoreCase(switchesOff, str);
	}

	@Override
	public boolean isToggleSwitch(String str)
	{
		return StringUtils.arrayContainsIgnoreCase(switchesToggle, str);
	}

	@Override
	public boolean isAllSwitch(String str)
	{
		return StringUtils.arrayContainsIgnoreCase(switchesAll, str);
	}

	@Override
	public boolean isRemoveSwitch(@Nullable String str)
	{
		return StringUtils.arrayContainsIgnoreCase(switchesRemove, str);
	}

	@Override
	public @NotNull String getOnSwitchTranslation()
	{
		return switchesOn[0];
	}

	@Override
	public @NotNull String getOffSwitchTranslation()
	{
		return switchesOff[0];
	}

	@Override
	public @NotNull String getToggleSwitchTranslation()
	{
		return switchesToggle[0];
	}

	@Override
	public @NotNull String getAllSwitchTranslation()
	{
		return switchesAll[0];
	}

	@Override
	public @NotNull String getRemoveSwitchTranslation()
	{
		return switchesRemove[0];
	}

	@Override
	public @Nullable List<String> getSimpleTabComplete(@NotNull CommandSender sender, String... args)
	{
		List<String> names = null;
		if(sender instanceof ProxiedPlayer && plugin.areMultiplePartnersAllowed() && args != null && args.length == 1)
		{
			names = plugin.getPlayerData((ProxiedPlayer) sender).getMatchingPartnerNames(args[0]);
			if(names.isEmpty())
			{
				names = null;
			}
		}
		return names;
	}
}