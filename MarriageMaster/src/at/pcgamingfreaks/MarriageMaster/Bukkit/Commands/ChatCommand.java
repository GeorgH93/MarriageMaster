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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Commands;

import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.Command.HelpData;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.MarryChatEvent;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.MarryChatMultiTargetEvent;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarryCommand;
import at.pcgamingfreaks.MarriageMaster.Bukkit.CommonMessages;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.MagicValues;
import at.pcgamingfreaks.MarriageMaster.Permissions;
import at.pcgamingfreaks.Message.MessageColor;
import at.pcgamingfreaks.Message.MessageFormat;
import at.pcgamingfreaks.Util.StringUtils;

import org.bukkit.Bukkit;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChatCommand extends MarryCommand implements Listener
{
	private final Message messageJoined, messageLeft, messageListeningStarted, messageListeningStopped, messageTargetSet;
	private final String displayNameAll, helpParameterMessage, privateMessageFormat, setTargetDescription;
	private final Set<Player> listeners = ConcurrentHashMap.newKeySet();
	private final String[] setTargetParameters, switchesToggle;
	private final boolean allowChatSurveillance;
	private MarryCommand chatToggleCommand, chatListenCommand;

	public ChatCommand(MarriageMaster plugin)
	{
		super(plugin, "chat", plugin.getLanguage().getTranslated("Commands.Description.Chat"), Permissions.CHAT, true, true, plugin.getLanguage().getCommandAliases("Chat"));

		messageJoined           = plugin.getLanguage().getMessage("Ingame.Chat.Joined");
		messageLeft             = plugin.getLanguage().getMessage("Ingame.Chat.Left");
		messageListeningStarted = plugin.getLanguage().getMessage("Ingame.Chat.ListeningStarted");
		messageListeningStopped = plugin.getLanguage().getMessage("Ingame.Chat.ListeningStopped");
		messageTargetSet        = plugin.getLanguage().getMessage("Ingame.Chat.TargetSet");
		privateMessageFormat    = plugin.getLanguage().getMessage("Ingame.Chat.Format").getFallback().replace("{SenderDisplayName}", "%1$s").replace("{ReceiverDisplayName}", "%2$s").replace("{Message}", "%3$s").replace("{SenderName}", "%4$s").replace("{ReceiverName}", "%5$s").replace("{MagicHeart}", "%6$s");
		displayNameAll          = plugin.getLanguage().getTranslated("Ingame.Chat.DisplayNameAll");
		helpParameterMessage    = "<" + plugin.getLanguage().getTranslated("Commands.MessageVariable") + ">";
		setTargetDescription = plugin.getLanguage().getTranslated("Commands.Description.ChatSetTarget");
		//noinspection SpellCheckingInspection
		setTargetParameters     = plugin.getLanguage().getCommandAliases("ChatSetTarget", new String[] { "target", "settarget" });
		switchesToggle  = plugin.getLanguage().getSwitch("Toggle", "toggle");
		allowChatSurveillance = plugin.getConfiguration().isChatSurveillanceEnabled();
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@Override
	public void afterRegister()
	{
		MarriageMaster plugin = ((MarriageMaster) getMarriagePlugin());
		chatToggleCommand = new ChatToggleCommand(plugin, this);
		plugin.getCommandManager().registerSubCommand(chatToggleCommand);
		if(allowChatSurveillance)
		{
			chatListenCommand = new ChatListenCommand(plugin, this);
			plugin.getCommandManager().registerSubCommand(chatListenCommand);
		}
	}

	@Override
	public void beforeUnregister()
	{
		MarriageMaster plugin = ((MarriageMaster) getMarriagePlugin());
		plugin.getCommandManager().unRegisterSubCommand(chatToggleCommand);
		chatToggleCommand.close();
		if(allowChatSurveillance)
		{
			plugin.getCommandManager().unRegisterSubCommand(chatListenCommand);
			chatListenCommand.close();
		}
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

	private boolean isTargetSelection(final @NotNull String[] args)
	{
		return getMarriagePlugin().areMultiplePartnersAllowed() && args.length == 2 && StringUtils.arrayContainsIgnoreCase(setTargetParameters, args[0]);
	}

	private void handleTargetSelection(final @NotNull MarriagePlayer player, final @NotNull String[] args)
	{
		Player target = Bukkit.getPlayer(args[1]);
		if(target != null && player.isPartner(target))
		{
			player.setPrivateChatTarget(getMarriagePlugin().getPlayerData(target));
			player.send(messageTargetSet);
		}
		else if(getMarriagePlugin().getCommandManager().isAllSwitch(args[1]))
		{
			player.setPrivateChatTarget((Marriage) null);
			player.send(messageTargetSet);
		}
		else
		{
			player.send(CommonMessages.getMessageTargetPartnerNotFound());
		}
	}

	@Override
	public void execute(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		((MarriageMaster) plugin).getScheduler().runAsync(task -> {
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
				else if (isTargetSelection(args))
				{
					handleTargetSelection(player, args);
				}
				else
				{
					//noinspection ConstantConditions
					if(player.getPrivateChatTarget() == null || player.getPrivateChatTarget().getPartner(player).isOnline())
					{
						doChat(player, String.join(" ", args)); // Doing the actual chat message logic
					}
					else
					{
						CommonMessages.getMessagePartnerOffline().send(sender);
					}
				}
			}
		});
	}

	@Override
	public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		if(args.length == 0) return null;

		if(isTargetSelection(args))
		{ // Handle target selection
			MarriagePlayer player = getMarriagePlugin().getPlayerData((Player) sender);
			return player.getMatchingPartnerNames(args[1]);
		}

		List<String> data = new ArrayList<>();
		if(args.length == 1)
		{
			StringUtils.startsWithIgnoreCase(switchesToggle, args[0], data);
			StringUtils.startsWithIgnoreCase(setTargetParameters, args[0], data);
		}
		Player playerSender = (Player) sender;
		String arg = args[args.length - 1].toLowerCase(Locale.ENGLISH);
		for(Player player : Bukkit.getOnlinePlayers())
		{
			if(player.getName().toLowerCase(Locale.ENGLISH).startsWith(arg) && playerSender.canSee(player))
			{
				data.add(player.getName());
			}
		}
		return data;
	}

	@Override
	public List<HelpData> getHelp(@NotNull CommandSender requester)
	{
		List<HelpData> help = new ArrayList<>();
		help.add(new HelpData(getTranslatedName(), helpParameterMessage, getDescription()));
		if(getMarriagePlugin().areMultiplePartnersAllowed())
		{
			help.add(new HelpData(getTranslatedName() + " " + setTargetParameters[0], "<" + CommonMessages.getHelpPartnerNameVariable() + " / " +
							getMarriagePlugin().getCommandManager().getAllSwitchTranslation() + ">", setTargetDescription));
		}
		return help;
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onChat(AsyncPlayerChatEvent event)
	{
		final MarriagePlayer player = getMarriagePlugin().getPlayerData(event.getPlayer());
		if(player.isPrivateChatDefault())
		{
			event.setCancelled(true);
			doChat(player, event.getMessage());
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerLeave(PlayerQuitEvent event)
	{
		if(allowChatSurveillance) listeners.remove(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		if(allowChatSurveillance && event.getPlayer().hasPermission(Permissions.LISTEN_CHAT_AUTO_JOIN))
		{
			listeners.add(event.getPlayer());
		}
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

	private Player getRecipient(final MarriagePlayer sender, List<Marriage> recipients)
	{
		Player recipient = null;
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
			if(recipients.size() == 1 && last != null) recipient = last.getPlayerOnline();
		}
		else
		{
			//noinspection ConstantConditions
			OfflinePlayer player2 = sender.getPrivateChatTarget().getPartner(sender).getPlayer();
			if(player2.isOnline() && player2.getPlayer() != null)
			{
				recipient = player2.getPlayer();
				recipients.add(sender.getPrivateChatTarget());
			}
		}
		return recipient;
	}

	private @NotNull List<Player> collectRecipientPlayers(final MarriagePlayer sender, final Player recipient, final List<Marriage> recipients)
	{
		List<Player> playerRecipients;
		if(recipient == null) // Add the recipients to the list
		{
			playerRecipients = new ArrayList<>(listeners.size() + recipients.size() + 1);
			for(Marriage target : recipients)
			{
				//noinspection ConstantConditions
				Player p = target.getPartner(sender).getPlayerOnline();
				if(!listeners.contains(p)) playerRecipients.add(p);
			}
		}
		else
		{
			playerRecipients = new ArrayList<>(listeners.size() + 2);
			if(!listeners.contains(recipient)) playerRecipients.add(recipient); // Add the recipient to the list if not one of the listeners
		}
		if(!listeners.contains(sender.getPlayerOnline())) playerRecipients.add(sender.getPlayerOnline()); // Add the sender to the list
		playerRecipients.addAll(listeners); // Copy the listeners since we need to send the message too
		return  playerRecipients;
	}

	private void doChat(final MarriagePlayer sender, String msg)
	{
		List<Marriage> recipients = new ArrayList<>(sender.getMultiMarriageData().size());
		Player recipient = getRecipient(sender, recipients);


		if(recipients.isEmpty())
		{
			sender.send(CommonMessages.getMessagePartnerOffline());
			return;
		}

		String magicHeart = MagicValues.RED_HEART;

		//region Fire Event
		if(recipients.size() == 1)
		{
			MarryChatEvent marryChatEvent = new MarryChatEvent(sender, recipients.get(0), msg);
			Bukkit.getPluginManager().callEvent(marryChatEvent);
			if(marryChatEvent.isCancelled()) return;
			msg = marryChatEvent.getMessage();
			magicHeart = recipients.get(0).getMagicHeart();
		}
		else
		{
			MarryChatMultiTargetEvent marryChatEvent = new MarryChatMultiTargetEvent(sender, recipients, msg);
			Bukkit.getPluginManager().callEvent(marryChatEvent);
			if(marryChatEvent.isCancelled() || recipients.isEmpty()) return;
			msg = marryChatEvent.getMessage();
			if(recipients.size() == 1)
			{
				//noinspection ConstantConditions
				recipient = recipients.get(0).getPartner(sender).getPlayerOnline();
				magicHeart = recipients.get(0).getMagicHeart();
			}
		}
		//endregion

		msg = cleanupMessage(msg, sender);

		// Send the message
		List<Player> playerRecipients = collectRecipientPlayers(sender, recipient, recipients);
		String recipientDisplayName = (recipient != null) ? recipient.getDisplayName() : displayNameAll, recipientName = (recipient != null) ? recipient.getName() : displayNameAll;
		String formattedMessage = String.format(privateMessageFormat, sender.getDisplayName(), recipientDisplayName, msg, sender.getName(), recipientName, magicHeart);
		Message message = new Message(formattedMessage);
		message.send(playerRecipients);
		if(allowChatSurveillance) plugin.getServer().getConsoleSender().sendMessage(formattedMessage);
	}

	private static @NotNull String cleanupMessage(@NotNull String msg, final @NotNull MarriagePlayer sender)
	{
		msg = msg.replace('§', '&'); // Remove all color codes from the message.
		// Checks if person has permission to use color codes
		if(sender.hasPermission(Permissions.CHAT_COLOR))
		{
			msg = MessageColor.translateAlternateColorCodes(msg);
		}
		if(sender.hasPermission(Permissions.CHAT_FORMAT))
		{
            msg = MessageFormat.translateAlternateFormatCodes(msg);
		}
		if(sender.hasPermission(Permissions.CHAT_MAGIC))
		{
			msg = msg.replace("&k", MessageFormat.MAGIC.toString());
		}
		else
		{
			msg = msg.replace(MessageFormat.MAGIC.toString(), "&k");
		}
		return msg;
	}


	public static class ChatToggleCommand extends MarryCommand
	{
		private ChatCommand chatCommand;

		public ChatToggleCommand(MarriageMaster plugin, ChatCommand chatCommand)
		{
			super(plugin, "chattoggle", plugin.getLanguage().getTranslated("Commands.Description.ChatToggle"), Permissions.CHAT, true, false, plugin.getLanguage().getCommandAliases("ChatToggle"));

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
			return EMPTY_TAB_COMPLETE_LIST;
		}

		@Override
		public void close()
		{
			chatCommand = null;
		}
	}
}
