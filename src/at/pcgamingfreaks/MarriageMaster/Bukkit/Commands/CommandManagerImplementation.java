/*
 * Copyright (C) 2016 GeorgH93
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.MarriageMaster.Bukkit.Commands;

import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.Bukkit.RegisterablePluginCommand;
import at.pcgamingfreaks.ConsoleColor;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.AcceptPendingRequest;
import at.pcgamingfreaks.MarriageMaster.API.HelpData;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.CommandManager;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarryCommand;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Database.MarriagePlayerData;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.Reflection;
import at.pcgamingfreaks.StringUtils;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CommandManagerImplementation implements CommandExecutor, TabCompleter, CommandManager
{
	private MarriageMaster plugin;
	private HashMap<String, MarryCommand> marryCommandMap = new HashMap<>();
	private List<MarryCommand> commands = new LinkedList<>();
	private String[] switchesOn, switchesOff, switchesToggle, switchesAll;
	private RegisterablePluginCommand marryCommand;
	private Message helpFormat;

	public CommandManagerImplementation(MarriageMaster marriageMaster)
	{
		plugin = marriageMaster;
	}

	public void init()
	{
		// Loading data
		switchesOn      = plugin.getLanguage().getSwitch("On",     "on");
		switchesOff     = plugin.getLanguage().getSwitch("Off",    "off");
		switchesToggle  = plugin.getLanguage().getSwitch("Toggle", "toggle");
		switchesAll     = plugin.getLanguage().getSwitch("All",    "all");

		// Registering the marriage command with our translated aliases
		marryCommand = new RegisterablePluginCommand(plugin, "marry", plugin.getLanguage().getCommandAliases("marry"));
		marryCommand.registerCommand();
		marryCommand.setExecutor(this);
		marryCommand.setTabCompleter(this);

		helpFormat = plugin.getLanguage().getMessage("Commands.HelpFormat").replaceAll("\\{MainCommand\\}", "%1\\$s").replaceAll("\\{SubCommand\\}", "%2\\$s").replaceAll("\\{Parameters\\}", "%3\\$s").replaceAll("\\{Description\\}", "%4\\$s");

		// Setting the help format for the marry commands as well as the no permissions and not from console message
		try
		{
			// Show help function
			Reflection.setStaticField(at.pcgamingfreaks.MarriageMaster.API.MarryCommand.class, "showHelp", this.getClass().getDeclaredMethod("sendHelp", CommandSender.class, String.class, Collection.class));
			Reflection.setStaticField(at.pcgamingfreaks.MarriageMaster.API.MarryCommand.class, "marriagePlugin", plugin); // Plugin instance
			Reflection.setStaticField(MarryCommand.class, "messageNoPermission", plugin.messageNoPermission); // No permission message
			Reflection.setStaticField(MarryCommand.class, "messageNotFromConsole", plugin.messageNotFromConsole); // Not from console message
			Reflection.setStaticField(MarryCommand.class, "messageNotMarried", plugin.messageNotMarried); // Not married message
			Reflection.setStaticField(MarryCommand.class, "helpPartnerSelector", "<" + plugin.helpPartnerNameVariable + ">"); // Help partner selector
			// Sets the work function for AcceptPendingRequest.close()
			Reflection.setStaticField(at.pcgamingfreaks.MarriageMaster.API.AcceptPendingRequest.class, "closeMethod", MarriagePlayerData.class.getMethod("closeRequest", AcceptPendingRequest.class));
		}
		catch(Exception e)
		{
			plugin.getLogger().warning(ConsoleColor.RED + "Unable to set the help format. Default format will be used.\nMore details:" + ConsoleColor.RESET);
			e.printStackTrace();
		}

		// Init MarriageMaster commands
		registerMarryCommand(new ListCommand(plugin));
		registerMarryCommand(new MarryMarryCommand(plugin));
		registerMarryCommand(new MarryDivorceCommand(plugin));
		registerMarryCommand(new ChatCommand(plugin));
		registerMarryCommand(new TpCommand(plugin));
		registerMarryCommand(new HomeCommand(plugin));
		if(plugin.getConfiguration().getPvPAllowBlocking())
		{
			registerMarryCommand(new PvPCommand(plugin));
		}
		if(plugin.getConfiguration().isGiftEnabled())
		{
			registerMarryCommand(new GiftCommand(plugin));
		}
		if(plugin.getBackpacksIntegration() != null && plugin.getConfiguration().isBackpackShareEnabled())
		{
			registerMarryCommand(new BackpackCommand(plugin));
		}
		if(plugin.getConfiguration().isKissEnabled())
		{
			registerMarryCommand(new KissCommand(plugin));
		}
		registerMarryCommand(new HugCommand(plugin));
		registerMarryCommand(new SeenCommand(plugin));
		if(plugin.getConfiguration().isSurnamesEnabled())
		{
			registerMarryCommand(new SurnameCommand(plugin));
		}
		if(plugin.getConfiguration().isSetPriestCommandEnabled())
		{
			registerMarryCommand(new SetPriestCommand(plugin));
		}
		if(plugin.getConfiguration().useUpdater())
		{
			registerMarryCommand(new UpdateCommand(plugin));
		}
		registerMarryCommand(new ReloadCommand(plugin));
		registerMarryCommand(new HelpCommand(plugin, commands)); // The help command needs the list of all existing commands to show the help
		registerMarryCommand(new RequestAcceptCommand(plugin));
		registerMarryCommand(new RequestDenyCommand(plugin));
		registerMarryCommand(new RequestCancelCommand(plugin));
	}

	public void close()
	{
		marryCommand.unregisterCommand();
		marryCommand = null;
		for(MarryCommand mC : commands)
		{
			mC.close();
		}
		commands.clear();
		commands = null;
		marryCommandMap.clear();
		marryCommandMap = null;
		plugin = null;
	}

	public void sendHelp(CommandSender target, String marryAlias, Collection<HelpData> data)
	{
		for(HelpData d : data)
		{
			helpFormat.send(target, marryAlias, d.getTranslatedSubCommand(), d.getParameter(), d.getDescription());
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args)
	{
		if(args.length > 0)
		{
			MarryCommand mC = marryCommandMap.get(args[0].toLowerCase());
			if(mC != null)
			{
				mC.doExecute(sender, alias, args[0], (args.length > 1) ? Arrays.copyOfRange(args, 1, args.length) : new String[0]);
				return true;
			}
			@SuppressWarnings("deprecation")
			Player target = plugin.getServer().getPlayer(args[0]);
			if(target != null || args.length == 2)
			{
				marryCommandMap.get("marry").doExecute(sender, alias, "marry", args);
				return true;
			}
		}
		marryCommandMap.get("help").doExecute(sender, alias, "help", args);
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
	{
		if(args.length > 0)
		{
			String arg = args[0].toLowerCase();
			MarryCommand marryCommand = marryCommandMap.get(arg);
			if(marryCommand != null)
			{
				return marryCommand.doTabComplete(sender, alias, args[0], (args.length > 1) ? Arrays.copyOfRange(args, 1, args.length) : new String[0]);
			}
			List<String> results = new LinkedList<>();
			if(args.length == 1)
			{
				for(Map.Entry<String, MarryCommand> cmd : marryCommandMap.entrySet())
				{
					if(cmd.getKey().startsWith(arg) && cmd.getValue().canUse(sender))
					{
						results.add(cmd.getKey());
					}
				}
			}
			arg = args[args.length - 1].toLowerCase();
			for(Player player : plugin.getServer().getOnlinePlayers())
			{
				if(player.getName().toLowerCase().startsWith(arg))
				{
					results.add(player.getName());
				}
			}
			return results;
		}
		return null;
	}

	@Override
	public void registerMarryCommand(@NotNull MarryCommand command)
	{
		commands.add(command);
		for(String s : command.getAliases())
		{
			marryCommandMap.put(s.toLowerCase(), command);
		}
		command.registerSubCommands();
	}

	@Override
	public void unRegisterMarryCommand(@NotNull MarryCommand command)
	{
		command.unRegisterSubCommands();
		commands.remove(command);
		for(String s : command.getAliases())
		{
			marryCommandMap.remove(s.toLowerCase());
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
	public @Nullable List<String> getSimpleTabComplete(@NotNull CommandSender sender, String... args)
	{
		List<String> names = null;
		if(sender instanceof Player && plugin.isPolygamyAllowed() && args != null && args.length == 1)
		{
			names = plugin.getPlayerData((Player) sender).getMatchingPartnerNames(args[0]);
			if(names.isEmpty())
			{
				names = null;
			}
		}
		return names;
	}

	@Override
	public boolean registerAcceptPendingRequest(@NotNull AcceptPendingRequest request)
	{
		// Check if the request is valid
		if(request.getPlayerThatHasToAccept().getOpenRequest() != null || !request.getPlayerThatHasToAccept().isOnline() || !(request.getPlayerThatHasToAccept() instanceof MarriagePlayerData))
		{
			return false;
		}
		if(request.getPlayersThatCanCancel() != null)
		{
			for(MarriagePlayer p : request.getPlayersThatCanCancel())
			{
				if(!(p instanceof MarriagePlayerData) || !p.isOnline())
				{
					return false;
				}
			}
			for(MarriagePlayer p : request.getPlayersThatCanCancel())
			{
				if(p == null) continue;
				((MarriagePlayerData) p).addRequest(request);
			}
		}
		((MarriagePlayerData) request.getPlayerThatHasToAccept()).addRequest(request);
		return true;
	}
}