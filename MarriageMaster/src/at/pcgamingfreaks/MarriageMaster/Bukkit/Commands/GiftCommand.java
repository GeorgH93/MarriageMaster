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

import at.pcgamingfreaks.Bukkit.ItemFilter;
import at.pcgamingfreaks.Bukkit.ItemNameResolver;
import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.Bukkit.Message.Placeholder.Processors.*;
import at.pcgamingfreaks.Bukkit.Message.Placeholder.Processors.Wrappers.ItemStackWrapper;
import at.pcgamingfreaks.Bukkit.Util.InventoryUtils;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.AcceptPendingRequest;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.GiftEvent;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarryCommand;
import at.pcgamingfreaks.MarriageMaster.Bukkit.CommonMessages;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Range;
import at.pcgamingfreaks.MarriageMaster.Permissions;
import at.pcgamingfreaks.MarriageMaster.Placeholder.Placeholders;
import at.pcgamingfreaks.MarriageMaster.Placeholder.Processors.NamePlaceholderProcessor;
import at.pcgamingfreaks.Message.Placeholder.Placeholder;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class GiftCommand extends MarryCommand
{
	private final Message messageNoItemInHand, messagePartnerInvFull, messageItemSent, messageItemReceived, messageWorldNotAllowed, messageItemNotAllowed, messageGameModeNotAllowedSender, messageGameModeNotAllowedReceiver;
	private final Message messageRequireConfirmation, messageWaitForConfirmation, messageRequestDenied, messageRequestDeniedPartner, messageRequestCanceled, messageRequestCanceledPartner;
	private final Message messageRequestCanceledDisconnectRequester, messageRequestCanceledDisconnectTarget, messageRequestRefundInvFull, messageRequestPartnerAlreadyHasAnOpenRequest;
	private final double range;
	private final boolean blacklistEnabled, requireConfirmation;
	private final ItemNameResolver itemNameResolver;
	private final Set<String> worldBlacklist;
	private final ItemFilter itemFilter;
	private final Set<GameMode> allowedSendGameModes, allowedReceiveGameModes;

	public GiftCommand(MarriageMaster plugin)
	{
		super(plugin, "gift", plugin.getLanguage().getTranslated("Commands.Description.Gift"), Permissions.GIFT, true, true, plugin.getLanguage().getCommandAliases("Gift"));

		/*if[STANDALONE]
		itemNameResolver = new ItemNameResolver();
		itemNameResolver.load(plugin, plugin.getConfiguration());
		else[STANDALONE]*/
		itemNameResolver = at.pcgamingfreaks.PluginLib.Bukkit.ItemNameResolver.getInstance();
		/*end[STANDALONE]*/

		range                   = plugin.getConfiguration().getRangeSquared(Range.Gift);
		allowedSendGameModes    = plugin.getConfiguration().getGiftAllowedGameModes();
		allowedReceiveGameModes = plugin.getConfiguration().getGiftAllowedReceiveGameModes();
		requireConfirmation     = plugin.getConfiguration().isGiftRequireConfirmationEnabled();
		worldBlacklist          = plugin.getConfiguration().getGiftBlackListedWorlds();
		blacklistEnabled        = !worldBlacklist.isEmpty();

		final Placeholder[] itemNamePlaceholder = { new Placeholder("ItemName", new ItemNamePlaceholderProcessor(itemNameResolver)),
													new Placeholder("ItemDisplayName", new ItemDisplayNamePlaceholderProcessor(itemNameResolver)) };
		final Placeholder currentGameModePlaceholder = new Placeholder("CurrentGameMode", new GameModePlaceholderProcessor()); // TODO translate
		final Placeholder[] itemPlaceholders = { new Placeholder("ItemAmount", ItemAmountPlaceholderProcessor.INSTANCE),
		                                         itemNamePlaceholder[0], itemNamePlaceholder[1],
		                                         new Placeholder("ItemMetaJSON", new ItemMetadataPlaceholderProcessor(plugin.getLogger())) };

		messageGameModeNotAllowedSender   = plugin.getLanguage().getMessage("Ingame.Gift.GameModeNotAllowedSender").replaceAll("\\{AllowedGameModes}", currentGameModePlaceholder.getProcessor().process(allowedSendGameModes)).placeholders(currentGameModePlaceholder);
		messageGameModeNotAllowedReceiver = plugin.getLanguage().getMessage("Ingame.Gift.GameModeNotAllowedReceiver").replaceAll("\\{AllowedGameModes}", currentGameModePlaceholder.getProcessor().process(allowedReceiveGameModes)).placeholders(currentGameModePlaceholder);
		messageNoItemInHand               = plugin.getLanguage().getMessage("Ingame.Gift.NoItemInHand");
		messagePartnerInvFull             = plugin.getLanguage().getMessage("Ingame.Gift.PartnerInvFull");
		messageItemSent                   = plugin.getLanguage().getMessage("Ingame.Gift.ItemSent").placeholders(Placeholders.PLAYER_NAME).placeholders(itemPlaceholders);
		messageItemReceived               = plugin.getLanguage().getMessage("Ingame.Gift.ItemReceived").placeholders(new Placeholder("Name", NamePlaceholderProcessor.INSTANCE), new Placeholder("DisplayName", NamePlaceholderProcessor.INSTANCE)).placeholders(itemPlaceholders);
		messageWorldNotAllowed            = plugin.getLanguage().getMessage("Ingame.Gift.WorldNotAllowed");
		messageItemNotAllowed             = plugin.getLanguage().getMessage("Ingame.Gift.ItemNotAllowed").placeholders(itemNamePlaceholder);

		messageRequireConfirmation                   = plugin.getLanguage().getMessage("Ingame.Gift.Request.Notification").placeholders(Placeholders.PLAYER_NAME).placeholders(itemPlaceholders);
		messageWaitForConfirmation                   = plugin.getLanguage().getMessage("Ingame.Gift.Request.WaitForConfirmation").placeholders(itemPlaceholders);
		messageRequestDenied                         = plugin.getLanguage().getMessage("Ingame.Gift.Request.Denied").placeholders(itemPlaceholders);
		messageRequestDeniedPartner                  = plugin.getLanguage().getMessage("Ingame.Gift.Request.DeniedPartner").placeholders(itemPlaceholders);
		messageRequestCanceled                       = plugin.getLanguage().getMessage("Ingame.Gift.Request.Canceled").placeholders(itemPlaceholders);
		messageRequestCanceledPartner                = plugin.getLanguage().getMessage("Ingame.Gift.Request.CanceledPartner").placeholders(itemPlaceholders);
		messageRequestCanceledDisconnectRequester    = plugin.getLanguage().getMessage("Ingame.Gift.Request.CanceledDisconnectRequester").placeholders(itemPlaceholders);
		messageRequestCanceledDisconnectTarget       = plugin.getLanguage().getMessage("Ingame.Gift.Request.CanceledDisconnectTarget").placeholders(itemPlaceholders);
		messageRequestRefundInvFull                  = plugin.getLanguage().getMessage("Ingame.Gift.Request.RefundInvFull");
		messageRequestPartnerAlreadyHasAnOpenRequest = plugin.getLanguage().getMessage("Ingame.Gift.Request.PartnerAlreadyHasAnOpenRequest");

		if(plugin.getConfiguration().isGiftItemFilterEnabled())
		{
			itemFilter = new ItemFilter(plugin.getConfiguration().isGiftItemFilterModeWhitelist());
			itemFilter.addFilteredMaterials(plugin.getConfiguration().getItemFilterMaterials());
			itemFilter.addFilteredNames(plugin.getConfiguration().getGiftItemFilterNames());
			itemFilter.addFilteredLore(plugin.getConfiguration().getGiftItemFilterLore());
		}
		else
		{
			itemFilter = null;
		}

		// Make sure that the InventoryUtils are fully initialized. Otherwise, this will cause lag spike when the command is first used.
		InventoryUtils.prepareTitleForOpenInventoryWithCustomTitle("ignore this");
	}

	boolean canSend(final @NotNull Player sender)
	{
		if(sender.hasPermission(Permissions.BYPASS_GIFT_GAME_MODE) || allowedSendGameModes.contains(sender.getGameMode()))
		{
			return true;
		}
		messageGameModeNotAllowedSender.send(sender, sender.getGameMode());
		return false;
	}

	boolean canReceive(final @NotNull Player sender, final @NotNull Player receiver)
	{
		if(!allowedReceiveGameModes.contains(receiver.getGameMode()) && !receiver.hasPermission(Permissions.BYPASS_GIFT_GAME_MODE))
		{
			messageGameModeNotAllowedReceiver.send(sender, receiver.getGameMode());
			return false;
		}
		if(blacklistEnabled && !sender.hasPermission(Permissions.BYPASS_GIFT_WORLD) && worldBlacklist.contains(receiver.getWorld().getName().toLowerCase(Locale.ENGLISH)))
		{
			messageWorldNotAllowed.send(sender);
			return false;
		}
		return true;
	}

	@Nullable ItemStack checkAndGetItemInHand(final @NotNull Player sender)
	{
		ItemStack its = InventoryUtils.getItemInMainHand(sender);
		if(its == null || its.getType() == Material.AIR || its.getAmount() == 0)
		{
			messageNoItemInHand.send(sender);
			return null;
		}
		if((itemFilter != null && !sender.hasPermission(Permissions.BYPASS_GIFT_ITEM_FILTER) && itemFilter.isItemBlocked(its)) ||
				(((MarriageMaster) plugin).getBackpacksIntegration() != null && ((MarriageMaster) plugin).getBackpacksIntegration().isBackpackItem(its)))
		{
			messageItemNotAllowed.send(sender, its);
			return null;
		}
		return its;
	}

	@Override
	public void execute(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		MarriagePlayer player = getMarriagePlugin().getPlayerData((Player) sender);
		Player bPlayer = (Player) sender;
		if(!canSend(bPlayer)) return;
		MarriagePlayer partner;
		if(getMarriagePlugin().areMultiplePartnersAllowed() && args.length == 1)
		{
			partner = getMarriagePlugin().getPlayerData(args[0]);
			if(!player.isPartner(partner))
			{
				CommonMessages.getMessageTargetPartnerNotFound().send(sender);
				return;
			}
		}
		else
		{
			Marriage mPD = player.getNearestPartnerMarriageData();
			if(mPD == null) { return; } // Should never happen, but it's always good to be safe!
			partner = player.getNearestPartnerMarriageData().getPartner(player);
		}
		if(partner != null && partner.isOnline())
		{
			executeGift(player, partner, bPlayer);
		}
		else
		{
			CommonMessages.getMessagePartnerOffline().send(sender);
		}
	}

	private void executeGift(final @NotNull MarriagePlayer player, final @NotNull MarriagePlayer partner, final @NotNull Player bPlayer)
	{
		final Player bPartner = partner.getPlayerOnline();
		if(bPartner != null && getMarriagePlugin().isInRangeSquared(bPlayer, bPartner, range))
		{
			ItemStack its = checkAndGetItemInHand(bPlayer);
			if (its == null) return;
			int slot = bPartner.getInventory().firstEmpty();
			if(slot == -1)
			{
				messagePartnerInvFull.send(bPlayer);
				return;
			}
			if(!canReceive(bPlayer, bPartner)) return;
			GiftEvent event = new GiftEvent(player, player.getMarriageData(partner), its);
			Bukkit.getPluginManager().callEvent(event);
			if(!event.isCancelled())
			{
				its = event.getItemStack();
				ItemStackWrapper wrappedItemStack = new ItemStackWrapper(its, plugin.getLogger(), itemNameResolver);
				if(requireConfirmation)
				{
					if(!getMarriagePlugin().getCommandManager().registerAcceptPendingRequest(new GiftRequest(wrappedItemStack, partner, player)))
					{
						messageRequestPartnerAlreadyHasAnOpenRequest.send(bPlayer);
						return;
					}
					messageWaitForConfirmation.send(bPlayer, wrappedItemStack);
					partner.send(messageRequireConfirmation, player, wrappedItemStack);
				}
				else
				{
					messageItemSent.send(bPlayer, partner, wrappedItemStack);
					messageItemReceived.send(bPartner, partner, wrappedItemStack);
					bPartner.getInventory().setItem(slot, its);
				}
				InventoryUtils.setItemInMainHand(bPlayer, null);
			}
		}
		else
		{
			CommonMessages.getMessagePartnerNotInRange().send(bPlayer);
		}
	}

	@Override
	public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		return getMarriagePlugin().getCommandManager().getSimpleTabComplete(sender, args);
	}

	private class GiftRequest extends AcceptPendingRequest
	{
		private final ItemStackWrapper item;
		private final MarriagePlayer recipient, sender;

		public GiftRequest(final @NotNull ItemStackWrapper item, final @NotNull MarriagePlayer recipient, final @NotNull MarriagePlayer sender)
		{
			super(recipient, sender);
			this.item = item;
			this.recipient = recipient;
			this.sender = sender;
		}

		@Override
		protected void onAccept()
		{
			int slot = recipient.getPlayerOnline().getInventory().firstEmpty();
			if(slot == -1)
			{
				sender.send(messagePartnerInvFull);
				refund();
				return;
			}
			recipient.getPlayerOnline().getInventory().setItem(slot, item.getItemStack());
			sender.send(messageItemSent, recipient, item);
			recipient.send(messageItemReceived, sender, item);
		}

		@Override
		protected void onDeny()
		{
			getPlayerThatHasToAccept().send(messageRequestDenied, item);
			sender.send(messageRequestDeniedPartner, item);
			refund();
		}

		@Override
		protected void onCancel(@NotNull MarriagePlayer player)
		{
			player.send(messageRequestCanceled, item);
			getPlayerThatHasToAccept().send(messageRequestCanceledPartner, item);
			refund();
		}

		@Override
		protected void onDisconnect(@NotNull MarriagePlayer player)
		{
			if(sender.equals(player)) getPlayerThatHasToAccept().send(messageRequestCanceledDisconnectRequester, item);
			else player.send(messageRequestCanceledDisconnectTarget, item);
			refund();
		}

		private void refund()
		{
			Player player = sender.getPlayerOnline();
			if(player == null) return;
			Map<Integer, ItemStack> left = player.getInventory().addItem(item.getItemStack());
			if(left.isEmpty()) return;
			left.values().forEach(i -> player.getWorld().dropItemNaturally(player.getLocation(), i));
			messageRequestRefundInvFull.send(player);
		}
	}
}
