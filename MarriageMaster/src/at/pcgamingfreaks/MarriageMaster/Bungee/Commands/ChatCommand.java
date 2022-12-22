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

package at.pcgamingfreaks.MarriageMaster.Bungee.Commands;

import at.pcgamingfreaks.Bungee.Message.Message;
import at.pcgamingfreaks.Command.HelpData;
import at.pcgamingfreaks.MarriageMaster.Bungee.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bungee.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bungee.API.MarryCommand;
import at.pcgamingfreaks.MarriageMaster.Bungee.CommonMessages;
import at.pcgamingfreaks.MarriageMaster.Bungee.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.MagicValues;
import at.pcgamingfreaks.MarriageMaster.Permissions;
import at.pcgamingfreaks.Message.MessageColor;
import at.pcgamingfreaks.Message.MessageFormat;
import at.pcgamingfreaks.Util.StringUtils;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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
		privateMessageFormat    = plugin.getLanguage().getMessage("Ingame.Chat.Format").placeholder("SenderDisplayName").placeholder("ReceiverDisplayName").placeholder("Message").placeholder("SenderName").placeholder("ReceiverName").placeholder("MagicHeart");
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
					CommonMessages.getMessageTargetPartnerNotFound().send(sender);
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
					CommonMessages.getMessagePartnerOffline().send(sender);
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
		return (data.isEmpty()) ? null : data;
	}

	@Override
	public List<HelpData> getHelp(@NotNull CommandSender requester)
	{
		List<HelpData> help = new LinkedList<>();
		help.add(new HelpData(getTranslatedName(), "<" + helpText + ">", getDescription()));
		if(getMarriagePlugin().areMultiplePartnersAllowed())
		{
			help.add(new HelpData(getTranslatedName() + " " + setTargetParameters[0], "<" + CommonMessages.getHelpPartnerNameVariable() + " / " +
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
		List<Marriage> recipients = new LinkedList<>();
		ProxiedPlayer recipient = null;
		if(sender.getPrivateChatTarget() == null)
		{
			MarriagePlayer last = null;
			for(Marriage m : sender.getMultiMarriageData())
			{
				MarriagePlayer p = m.getPartner(sender);
				if(p != null && p.isOnline() && p.getPlayerOnline() != null)
				{
					last = p;
					recipients.add(m);
				}
			}
			if(recipients.size() == 1) recipient = last.getPlayerOnline();
		}
		else
		{
			MarriagePlayer player2 = sender.getPartner();
			if(player2 != null && player2.isOnline())
			{
				recipient = player2.getPlayer();
				recipients.add(sender.getPrivateChatTarget());
			}
		}

		ProxiedPlayer player = sender.getPlayer(); // Get the sending player (for permission checks and sending the message.
		if(!recipients.isEmpty())
		{
			// Fire Event
			/*if(recipients.size() == 1)
			{
				MarryChatEvent marryChatEvent = new MarryChatEvent(sender, recipients.get(0), msg);
				Bukkit.getPluginManager().callEvent(marryChatEvent);
				if(marryChatEvent.isCancelled())
				{
					return;
				}
				msg = marryChatEvent.getMessage();
			}
			else
			{
				MarryChatMultiTargetEvent marryChatEvent = new MarryChatMultiTargetEvent(sender, recipients, msg);
				Bukkit.getPluginManager().callEvent(marryChatEvent);
				if(marryChatEvent.isCancelled() || recipients.isEmpty())
				{
					return;
				}
				msg = marryChatEvent.getMessage();
				if(recipients.size() == 1)
				{
					recipient = recipients.get(0).getPartner(sender).getPlayerOnline();
				}
			}*/

			msg = msg.replace('§', '&'); // Remove all color codes from the message.
			// Checks if person has permission to use color codes
			if(player.hasPermission(Permissions.CHAT_COLOR))
			{
				msg = MessageColor.translateAlternateColorCodes(msg);
			}
			if(player.hasPermission(Permissions.CHAT_FORMAT))
			{
				msg = MessageFormat.translateAlternateFormatCodes(msg);
			}
			if(player.hasPermission(Permissions.CHAT_MAGIC))
			{
				msg = msg.replace("&k", MessageFormat.MAGIC.toString());
			}
			else
			{
				msg = msg.replace(MessageFormat.MAGIC.toString(), "&k");
			}
			// Send the message
			List<ProxiedPlayer> playerRecipients = new ArrayList<>(listeners.size() + recipients.size() + 1); // Copy the listeners since we need to send the message to them anyway
			playerRecipients.addAll(listeners);
			playerRecipients.add(player); // Add the sender to the list
			if(recipient == null) // Add the recipients to the list
			{
				for(Marriage target : recipients)
				{
					//noinspection ConstantConditions
					playerRecipients.add(target.getPartner(sender).getPlayer());
				}
			}
			else
			{
				playerRecipients.add(recipient); // Add the recipient to the list
			}
			String magicHeart = MagicValues.RED_HEART;
			if(recipients.size() == 1) magicHeart = recipients.get(0).getMagicHeart();
			privateMessageFormat.send(playerRecipients, player.getDisplayName(), (recipient != null) ? recipient.getDisplayName() : displayNameAll, msg, player.getName(), (recipient != null) ? recipient.getName() : displayNameAll, magicHeart);
		}
		else
		{
			CommonMessages.getMessagePartnerOffline().send(player);
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