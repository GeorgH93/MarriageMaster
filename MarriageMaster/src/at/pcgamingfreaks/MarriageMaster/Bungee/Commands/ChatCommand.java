/*
 *   Copyright (C) 2020 GeorgH93
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

package at.pcgamingfreaks.MarriageMaster.Bungee.Commands;

import at.pcgamingfreaks.Bungee.Message.Message;
import at.pcgamingfreaks.Command.HelpData;
import at.pcgamingfreaks.MarriageMaster.Bungee.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bungee.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bungee.API.MarryCommand;
import at.pcgamingfreaks.MarriageMaster.Bungee.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.MagicValues;
import at.pcgamingfreaks.MarriageMaster.Permissions;
import at.pcgamingfreaks.Message.MessageColor;
import at.pcgamingfreaks.StringUtils;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class ChatCommand extends MarryCommand implements Listener
{
	private final Message messageJoined, messageLeft, messageListeningStarted, messageListeningStopped, privateMessageFormat, messageTargetSet;
	private final String displayNameAll, helpText;
	private LinkedList<ProxiedPlayer> listeners = new LinkedList<>();
	private MarryCommand chatToggleCommand, chatListenCommand;
	private final String[] setTargetParameters;

	public ChatCommand(@NotNull MarriageMaster plugin)
	{
		super(plugin, "chat", plugin.getLanguage().getTranslated("Commands.Description.Chat"), Permissions.CHAT, true, true, plugin.getLanguage().getCommandAliases("Chat"));

		messageJoined           = plugin.getLanguage().getMessage("Ingame.Chat.Joined");
		messageLeft             = plugin.getLanguage().getMessage("Ingame.Chat.Left");
		messageListeningStarted = plugin.getLanguage().getMessage("Ingame.Chat.ListeningStarted");
		messageListeningStopped = plugin.getLanguage().getMessage("Ingame.Chat.ListeningStopped");
		messageTargetSet        = plugin.getLanguage().getMessage("Ingame.Chat.TargetSet");
		privateMessageFormat    = plugin.getLanguage().getMessage("Ingame.Chat.Format").replaceAll("\\{SenderDisplayName}", "%1\\$s").replaceAll("\\{ReceiverDisplayName}", "%2\\$s").replaceAll("\\{Message}", "%3\\$s").replaceAll("\\{SenderName}", "%4\\$s").replaceAll("\\{ReceiverName}", "%5\\$s").replaceAll("\\{MagicHeart}", "%6\\$s");
		displayNameAll          = plugin.getLanguage().getTranslated("Ingame.Chat.DisplayNameAll");
		helpText                = "<" + plugin.getLanguage().getTranslated("Commands.MessageVariable") + ">";
		setTargetParameters     = plugin.getLanguage().getCommandAliases("ChatSetTarget");

		plugin.getProxy().getPluginManager().registerListener(plugin, this);
	}

	@Override
	public void afterRegister()
	{
		chatToggleCommand = new ChatToggleCommand(((MarriageMaster) getMarriagePlugin()), this);
		((CommandManagerImplementation) getMarriagePlugin().getCommandManager()).registerSubCommand(chatToggleCommand);
		chatListenCommand = new ChatListenCommand(((MarriageMaster) getMarriagePlugin()), this);
		((CommandManagerImplementation) getMarriagePlugin().getCommandManager()).registerSubCommand(chatListenCommand);
	}

	@Override
	public void beforeUnregister()
	{
		((CommandManagerImplementation) getMarriagePlugin().getCommandManager()).registerSubCommand(chatToggleCommand);
		chatToggleCommand.close();
		((CommandManagerImplementation) getMarriagePlugin().getCommandManager()).registerSubCommand(chatListenCommand);
		chatListenCommand.close();
	}

	@Override
	public void close()
	{
		listeners.clear();
		listeners = null;
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
	public void execute(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String s1, @NotNull String[] args)
	{
		MarriagePlayer player = getMarriagePlugin().getPlayerData((ProxiedPlayer) sender);
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
			else if(getMarriagePlugin().areMultiplePartnersAllowed() && args.length == 2 && StringUtils.arrayContainsIgnoreCase(setTargetParameters, args[0]))
			{
				ProxiedPlayer target = plugin.getProxy().getPlayer(args[1]);
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
					StringBuilder stringBuilder = new StringBuilder();
					for(int i = 0; i < args.length; i++)
					{
						if(i > 0)
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
	public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String s, @NotNull String s1, @NotNull String[] args)
	{
		List<String> data = new LinkedList<>();
		if(args.length > 0)
		{
			if(args.length == 2 && StringUtils.arrayContainsIgnoreCase(setTargetParameters, args[0]))
			{
				MarriagePlayer player = getMarriagePlugin().getPlayerData((ProxiedPlayer) sender);
				data = player.getMatchingPartnerNames(args[1]);
			}
			else
			{
				String arg = args[args.length - 1].toLowerCase(Locale.ENGLISH);
				for(ProxiedPlayer player : plugin.getProxy().getPlayers())
				{
					if(player.getName().toLowerCase(Locale.ENGLISH).startsWith(arg))
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
		help.add(new HelpData(getTranslatedName(), "<" + helpText + ">", getDescription()));
		if(getMarriagePlugin().areMultiplePartnersAllowed())
		{
			help.add(new HelpData(getTranslatedName() + " " + setTargetParameters[0], "<" + ((MarriageMaster) getMarriagePlugin()).helpPartnerNameVariable + " / " +
					getMarriagePlugin().getCommandManager().getAllSwitchTranslation() + ">", getDescription()));
		}
		return help;
	}

	@SuppressWarnings("unused")
	@EventHandler(priority = EventPriority.HIGH)
	public void onChat(ChatEvent event)
	{
		if(!(event.getSender() instanceof ProxiedPlayer) || event.isCommand()) return;
		MarriagePlayer player = getMarriagePlugin().getPlayerData((ProxiedPlayer) event.getSender());
		if(player.isPrivateChatDefault())
		{
			event.setCancelled(true);
			doChat(player, event.getMessage());
		}
	}

	@SuppressWarnings("unused")
	@EventHandler(priority = EventPriority.LOWEST)
	public void onLeave(PlayerDisconnectEvent event)
	{
		listeners.remove(event.getPlayer());
	}

	private void toggleChatSetting(MarriagePlayer player)
	{
		if(player.isPrivateChatDefault())
		{
			player.setPrivateChatDefault(false);
			player.send(messageLeft);
		}
		else
		{
			player.setPrivateChatDefault(true);
			player.send(messageJoined);
		}
	}

	private void toggleListenChat(ProxiedPlayer player, boolean on)
	{
		if(on)
		{
			messageListeningStarted.send(player);
			listeners.add(player);
		}
		else
		{
			messageListeningStopped.send(player);
			listeners.remove(player);
		}
	}

	private void toggleListenChat(ProxiedPlayer player)
	{
		toggleListenChat(player, !listeners.contains(player));
	}

	private void doChat(final MarriagePlayer sender, String msg)
	{
		List<Marriage> receivers = new LinkedList<>();
		ProxiedPlayer receiver = null;
		if(getMarriagePlugin().areMultiplePartnersAllowed() && sender.getPrivateChatTarget() == null)
		{
			MarriagePlayer p = null;
			for(Marriage m : sender.getMultiMarriageData())
			{
				p = m.getPartner(sender);
				if(p != null && p.isOnline())
				{
					receivers.add(m);
				}
			}
			if(receivers.size() == 1 && p != null)
			{
				receiver = p.getPlayer();
			}
		}
		else
		{
			MarriagePlayer player2 = sender.getPartner();
			if(player2 != null && player2.isOnline())
			{
				receiver = player2.getPlayer();
				receivers.add(sender.getPrivateChatTarget());
			}
		}

		ProxiedPlayer player = sender.getPlayer(); // Get the sending player (for permission checks and sending the message.
		if(!receivers.isEmpty())
		{
			// Fire Event
			/*if(receivers.size() == 1)
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
					receiver = receivers.get(0).getPartner(sender).getPlayerOnline();
				}
			}*/

			msg = msg.replace('§', '&'); // Remove all color codes from the message.
			// Checks if person has permission to use color codes
			if(player.hasPermission(Permissions.CHAT_COLOR))
			{
				msg = MessageColor.translateAlternateColorCodes('&', msg);
			}
			if(player.hasPermission(Permissions.CHAT_FORMAT))
			{
				msg = msg.replaceAll("&l", "§l").replaceAll("&m", "§m").replaceAll("&n", "§n").replaceAll("&o", "§o").replaceAll("&r", "§r");
			}
			else
			{
				msg = msg.replaceAll("§l", "&l").replaceAll("§m", "&m").replaceAll("§n", "&n").replaceAll("§o", "&o").replaceAll("§r", "&r");
			}
			if(player.hasPermission(Permissions.CHAT_MAGIC))
			{
				msg = msg.replaceAll("&k", "§k");
			}
			else
			{
				msg = msg.replaceAll("§k", "&k");
			}
			// Send the message
			List<ProxiedPlayer> playerReceivers = new LinkedList<>(listeners); // Copy the listeners since we need to send the message to them anyway
			playerReceivers.add(player); // Add the sender to the list
			if(receiver == null) // Add the receivers to the list
			{
				for(Marriage target : receivers)
				{
					//noinspection ConstantConditions
					playerReceivers.add(target.getPartner(sender).getPlayer());
				}
			}
			else
			{
				playerReceivers.add(receiver); // Add the receiver to the list
			}
			String magicHeart = MagicValues.RED_HEART;
			if(receivers.size() == 1) magicHeart = receivers.get(0).getMagicHeart();
			privateMessageFormat.send(playerReceivers, player.getDisplayName(), (receiver != null) ? receiver.getDisplayName() : displayNameAll, msg, player.getName(), (receiver != null) ? receiver.getName() : displayNameAll, magicHeart);
		}
		else
		{
			((MarriageMaster) getMarriagePlugin()).messagePartnerOffline.send(player);
		}
	}


	public static class ChatToggleCommand extends MarryCommand
	{
		private ChatCommand chatCommand;

		public ChatToggleCommand(MarriageMaster plugin, ChatCommand chatCommand)
		{
			super(plugin, "chattoggle", plugin.getLanguage().getTranslated("Commands.Description.ChatToggle"), Permissions.CHAT, true, plugin.getLanguage().getCommandAliases("ChatToggle"));

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
			chatCommand.toggleChatSetting(getMarriagePlugin().getPlayerData((ProxiedPlayer) sender));
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
			super(plugin, "listenchat", plugin.getLanguage().getTranslated("Commands.Description.ChatListen"), Permissions.LISTEN_CHAT, true, plugin.getLanguage().getCommandAliases("ChatListen"));

			this.chatCommand = chatCommand;
		}

		@Override
		public void execute(@NotNull CommandSender cmdSender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
		{
			ProxiedPlayer sender = (ProxiedPlayer) cmdSender;
			if(args.length == 1)
			{
				if(getMarriagePlugin().getCommandManager().isToggleSwitch(args[0]))
				{
					chatCommand.toggleListenChat(sender);
				}
				else if(getMarriagePlugin().getCommandManager().isOnSwitch(args[0]))
				{
					chatCommand.toggleListenChat(sender);
				}
				else if(getMarriagePlugin().getCommandManager().isOffSwitch(args[0]))
				{
					chatCommand.toggleListenChat(sender);
				}
				else
				{
					showHelp(sender, mainCommandAlias);
				}
			}
			else
			{
				chatCommand.toggleListenChat(sender);
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