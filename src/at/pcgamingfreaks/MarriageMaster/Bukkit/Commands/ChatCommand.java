/*
 *   Copyright (C) 2016, 2017 GeorgH93
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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Commands;

import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.MarryChatEvent;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.MarryChatMultiTargetEvent;
import at.pcgamingfreaks.MarriageMaster.API.HelpData;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarryCommand;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.StringUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChatCommand extends MarryCommand implements Listener
{
	private final Message messageJoined, messageLeft, messageListeningStarted, messageListeningStopped, privateMessageFormat, messageTargetSet;
	private final String displayNameAll, helpParameterMessage;
	private final Set<Player> listeners = ConcurrentHashMap.newKeySet();
	private MarryCommand chatToggleCommand, chatListenCommand;
	private final String[] setTargetParameters;

	public ChatCommand(MarriageMaster plugin)
	{
		super(plugin, "chat", plugin.getLanguage().getTranslated("Commands.Description.Chat"), "marry.chat", true, true, plugin.getLanguage().getCommandAliases("Chat"));

		messageJoined           = plugin.getLanguage().getMessage("Ingame.Chat.Joined");
		messageLeft             = plugin.getLanguage().getMessage("Ingame.Chat.Left");
		messageListeningStarted = plugin.getLanguage().getMessage("Ingame.Chat.ListeningStarted");
		messageListeningStopped = plugin.getLanguage().getMessage("Ingame.Chat.ListeningStopped");
		messageTargetSet        = plugin.getLanguage().getMessage("Ingame.Chat.TargetSet");
		privateMessageFormat    = plugin.getLanguage().getMessage("Ingame.Chat.Format").replaceAll("\\{SenderDisplayName\\}", "%1\\$s").replaceAll("\\{ReceiverDisplayName\\}", "%2\\$s").replaceAll("\\{Message\\}", "%3\\$s").replaceAll("\\{SenderName\\}", "%4\\$s").replaceAll("\\{ReceiverName\\}", "%5\\$s");
		displayNameAll          = plugin.getLanguage().getTranslated("Ingame.Chat.DisplayNameAll");
		helpParameterMessage    = "<" + plugin.getLanguage().getTranslated("Commands.MessageVariable") + ">";
		//noinspection SpellCheckingInspection
		setTargetParameters     = plugin.getLanguage().getCommandAliases("ChatSetTarget", new String[] { "target", "settarget" });

		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@Override
	public void registerSubCommands()
	{
		chatToggleCommand = new ChatToggleCommand(((MarriageMaster) getMarriagePlugin()), this);
		getMarriagePlugin().getCommandManager().registerMarryCommand(chatToggleCommand);
		chatListenCommand = new ChatListenCommand(((MarriageMaster) getMarriagePlugin()), this);
		getMarriagePlugin().getCommandManager().registerMarryCommand(chatListenCommand);
	}

	@Override
	public void unRegisterSubCommands()
	{
		getMarriagePlugin().getCommandManager().unRegisterMarryCommand(chatToggleCommand);
		chatToggleCommand.close();
		getMarriagePlugin().getCommandManager().unRegisterMarryCommand(chatListenCommand);
		chatListenCommand.close();
	}

	@Override
	public void close()
	{
		listeners.clear();
		if(chatToggleCommand != null)
		{
			chatToggleCommand.close();
			chatToggleCommand = null;
		}
		if(chatListenCommand != null)
		{
			chatListenCommand.close();
			chatListenCommand = null;
		}
	}

	@Override
	public void execute(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		MarriagePlayer player = getMarriagePlugin().getPlayerData((Player) sender);
		if(args.length == 0)
		{
			showHelp(sender, mainCommandAlias);
		}
		else
		{
			if(getMarriagePlugin().getCommandManager().isToggleSwitch(args[0]))
			{
				toggleChatSetting(player);
			}
			else if(getMarriagePlugin().isPolygamyAllowed() && args.length == 2 && StringUtils.arrayContainsIgnoreCase(setTargetParameters, args[0]))
			{
				@SuppressWarnings("deprecation") // We can't expect the players to know their partners uuid
				Player target = Bukkit.getPlayer(args[1]);
				if(target != null && player.isPartner(target))
				{
					player.setPrivateChatTarget(getMarriagePlugin().getPlayerData(target));
					messageTargetSet.send(sender);
				}
				else
				{
					((MarriageMaster) getMarriagePlugin()).messageTargetPartnerNotFound.send(sender);
				}
			}
			else
			{
				//noinspection ConstantConditions
				if(player.getPrivateChatTarget() == null || player.getPrivateChatTarget().getPartner(player).isOnline())
				{
					StringBuilder stringBuilder = new StringBuilder("");
					for(int i = 0; i < args.length; i++)
					{
						if(i != 0)
						{
							stringBuilder.append(" ");
						}
						stringBuilder.append(args[i]);
					}
					doChat(player, stringBuilder.toString()); // Doing the actual chat message logic
				}
				else
				{
					((MarriageMaster) getMarriagePlugin()).messagePartnerOffline.send(sender);
				}
			}
		}
	}

	@Override
	public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		List<String> data = new LinkedList<>();
		if(args.length > 0)
		{
			if(args.length == 2 && StringUtils.arrayContainsIgnoreCase(setTargetParameters, args[0]))
			{
				MarriagePlayer player = getMarriagePlugin().getPlayerData((Player) sender);
				data = player.getMatchingPartnerNames(args[1]);
			}
			else
			{
				String arg = args[args.length - 1].toLowerCase();
				for(Player player : Bukkit.getOnlinePlayers())
				{
					if(player.getName().toLowerCase().startsWith(arg))
					{
						data.add(player.getName());
					}
				}
			}
		}
		return (data.size() == 0) ? null : data;
	}

	@Override
	public List<HelpData> getHelp(@NotNull CommandSender requester)
	{
		List<HelpData> help = new LinkedList<>();
		help.add(new HelpData(getTranslatedName(), helpParameterMessage, getDescription()));
		if(getMarriagePlugin().isPolygamyAllowed())
		{
			help.add(new HelpData(getTranslatedName() + " " + setTargetParameters[0], "<" + ((MarriageMaster) getMarriagePlugin()).helpPartnerNameVariable + " / " +
							getMarriagePlugin().getCommandManager().getAllSwitchTranslation() + ">", getDescription()));
		}
		return help;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onChat(AsyncPlayerChatEvent event)
	{
		final MarriagePlayer player = getMarriagePlugin().getPlayerData(event.getPlayer());
		if(player.isPrivateChatDefault())
		{
			event.setCancelled(true);
			final String msg = event.getMessage();
			// Sync the execution of the chat event with the main thread. We check a lot of stuff with the bukkit api which is not thread save!
			Bukkit.getScheduler().runTask(plugin, () -> doChat(player, msg));
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onLeave(PlayerQuitEvent event)
	{
		listeners.remove(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onJoin(PlayerJoinEvent event)
	{
		//noinspection SpellCheckingInspection
		if(event.getPlayer().hasPermission("marry.listenchat.autojoin"))
		{
			listeners.add(event.getPlayer());
		}
	}

	private void toggleChatSetting(MarriagePlayer player)
	{
		if(player.isPrivateChatDefault())
		{
			player.setPrivateChatDefault(false);
			messageLeft.send(player.getPlayer().getPlayer());
		}
		else
		{
			player.setPrivateChatDefault(true);
			messageJoined.send(player.getPlayer().getPlayer());
		}
	}

	private void toggleListenChat(Player player, boolean on)
	{
		if(on)
		{
			listeners.add(player);
			messageListeningStarted.send(player);
		}
		else
		{
			listeners.remove(player);
			messageListeningStopped.send(player);
		}
	}

	private void toggleListenChat(Player player)
	{
		toggleListenChat(player, !listeners.contains(player));
	}

	private void doChat(final MarriagePlayer sender, String msg)
	{
		List<Marriage> receivers = new LinkedList<>();
		Player receiver = null;
		if(sender.getPrivateChatTarget() == null)
		{
			MarriagePlayer p = null;
			for(Marriage m : sender.getMultiMarriageData())
			{
				p = m.getPartner(sender);
				if(p != null && p.isOnline() && p.getPlayer().getPlayer() != null)
				{
					receivers.add(m);
				}
			}
			if(receivers.size() == 1 && p != null)
			{
				receiver = p.getPlayer().getPlayer();
			}
		}
		else
		{
			//noinspection ConstantConditions
			OfflinePlayer player2 = sender.getPrivateChatTarget().getPartner(sender).getPlayer();
			if(player2.isOnline() && player2.getPlayer() != null)
			{
				receiver = player2.getPlayer();
				receivers.add(sender.getPrivateChatTarget());
			}
		}

		if(!receivers.isEmpty())
		{
			// Fire Event
			if(receivers.size() == 1)
			{
				MarryChatEvent marryChatEvent = new MarryChatEvent(sender, receivers.get(0), msg);
				Bukkit.getPluginManager().callEvent(marryChatEvent);
				if(marryChatEvent.isCancelled())
				{
					return;
				}
				msg = marryChatEvent.getMessage();
			}
			else
			{
				MarryChatMultiTargetEvent marryChatEvent = new MarryChatMultiTargetEvent(sender, receivers, msg);
				Bukkit.getPluginManager().callEvent(marryChatEvent);
				if(marryChatEvent.isCancelled() || receivers.isEmpty())
				{
					return;
				}
				msg = marryChatEvent.getMessage();
				if(receivers.size() == 1)
				{
					//noinspection ConstantConditions
					receiver = receivers.get(0).getPartner(sender).getPlayer().getPlayer();
				}
			}

			msg = msg.replace('§', '&'); // Remove all color codes from the message.
			// Checks if person has permission to use color codes
			if(sender.hasPermission("marry.chat.color"))
			{
				msg = ChatColor.translateAlternateColorCodes('&', msg);
			}
			if(sender.hasPermission("marry.chat.format"))
			{
				msg = msg.replaceAll("&l", "§l").replaceAll("&m", "§m").replaceAll("&n", "§n").replaceAll("&o", "§o").replaceAll("&r", "§r");
			}
			else
			{
				msg = msg.replaceAll("§l", "&l").replaceAll("§m", "&m").replaceAll("§n", "&n").replaceAll("§o", "&o").replaceAll("§r", "&r");
			}
			if(sender.hasPermission("marry.chat.magic"))
			{
				msg = msg.replaceAll("&k", "§k");
			}
			else
			{
				msg = msg.replaceAll("§k", "&k");
			}
			// Send the message
			List<Player> playerReceivers = new LinkedList<>(listeners); // Copy the listeners since we need to send the message to them anyway
			playerReceivers.add(sender.getPlayer().getPlayer()); // Add the sender to the list
			if(receiver == null) // Add the receivers to the list
			{
				for(Marriage target : receivers)
				{
					//noinspection ConstantConditions
					playerReceivers.add(target.getPartner(sender).getPlayer().getPlayer());
				}
			}
			else
			{
				playerReceivers.add(receiver); // Add the receiver to the list
			}
			String receiverDisplayName = (receiver != null) ? receiver.getDisplayName() : displayNameAll, receiverName = (receiver != null) ? receiver.getName() : displayNameAll;
			privateMessageFormat.send(playerReceivers, sender.getDisplayName(), receiverDisplayName, msg, sender.getName(), receiverName);
			privateMessageFormat.send(plugin.getServer().getConsoleSender(), sender.getDisplayName(), receiverDisplayName, msg, sender.getName(), receiverName);
		}
		else
		{
			sender.send(((MarriageMaster) getMarriagePlugin()).messagePartnerOffline);
		}
	}


	public static class ChatToggleCommand extends MarryCommand
	{
		private ChatCommand chatCommand;

		public ChatToggleCommand(MarriageMaster plugin, ChatCommand chatCommand)
		{
			super(plugin, "chattoggle", plugin.getLanguage().getTranslated("Commands.Description.ChatToggle"), "marry.chat", true, plugin.getLanguage().getCommandAliases("ChatToggle"));

			this.chatCommand = chatCommand;
		}

		@Override
		public void close()
		{
			chatCommand = null;
		}

		@Override
		public void execute(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
		{
			chatCommand.toggleChatSetting(getMarriagePlugin().getPlayerData((Player) sender));
		}

		@Override
		public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
		{
			return null;
		}

		@Override
		public List<HelpData> getHelp(@NotNull CommandSender requester)
		{
			return null;
		}
	}

	public static class ChatListenCommand extends MarryCommand
	{
		private ChatCommand chatCommand;

		public ChatListenCommand(MarriageMaster plugin, ChatCommand chatCommand)
		{
			super(plugin, "listenchat", plugin.getLanguage().getTranslated("Commands.Description.ChatListen"), "marry.listenchat", true, plugin.getLanguage().getCommandAliases("ChatListen"));

			this.chatCommand = chatCommand;
		}

		@Override
		public void execute(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
		{
			if(args.length == 1)
			{
				if(getMarriagePlugin().getCommandManager().isToggleSwitch(args[0]))
				{
					chatCommand.toggleListenChat((Player) sender);
				}
				else if(getMarriagePlugin().getCommandManager().isOnSwitch(args[0]))
				{
					chatCommand.toggleListenChat((Player) sender, true);
				}
				else if(getMarriagePlugin().getCommandManager().isOffSwitch(args[0]))
				{
					chatCommand.toggleListenChat((Player) sender, false);
				}
				else
				{
					showHelp(sender, mainCommandAlias);
				}
			}
			else
			{
				chatCommand.toggleListenChat((Player) sender);
			}
		}

		@Override
		public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
		{
			return null;
		}

		@Override
		public void close()
		{
			chatCommand = null;
		}
	}
}