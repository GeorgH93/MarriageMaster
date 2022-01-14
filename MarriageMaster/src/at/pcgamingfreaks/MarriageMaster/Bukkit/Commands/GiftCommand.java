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
import at.pcgamingfreaks.Bukkit.Util.InventoryUtils;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.AcceptPendingRequest;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.GiftEvent;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarryCommand;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Range;
import at.pcgamingfreaks.MarriageMaster.Permissions;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

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

		messageGameModeNotAllowedSender   = plugin.getLanguage().getMessage("Ingame.Gift.GameModeNotAllowedSender").replaceAll("\\{AllowedGameModes}", "%1\\$s").replaceAll("\\{CurrentGameMode}", "%2\\$s");
		messageGameModeNotAllowedReceiver = plugin.getLanguage().getMessage("Ingame.Gift.GameModeNotAllowedReceiver").replaceAll("\\{AllowedGameModes}", "%1\\$s").replaceAll("\\{CurrentGameMode}", "%2\\$s");
		messageNoItemInHand               = plugin.getLanguage().getMessage("Ingame.Gift.NoItemInHand");
		messagePartnerInvFull             = plugin.getLanguage().getMessage("Ingame.Gift.PartnerInvFull");
		messageItemSent                   = plugin.getLanguage().getMessage("Ingame.Gift.ItemSent").replaceAll("\\{Name}", "%1\\$s").replaceAll("\\{DisplayName}", "%2\\$s").replaceAll("\\{ItemAmount}", "%3\\$d").replaceAll("\\{ItemName}", "%4\\$s").replaceAll("\\{ItemMetaJSON}", "%5\\$s");
		messageItemReceived               = plugin.getLanguage().getMessage("Ingame.Gift.ItemReceived").replaceAll("\\{Name}", "%1\\$s").replaceAll("\\{DisplayName}", "%2\\$s").replaceAll("\\{ItemAmount}", "%3\\$d").replaceAll("\\{ItemName}", "%4\\$s").replaceAll("\\{ItemMetaJSON}", "%5\\$s");
		messageWorldNotAllowed            = plugin.getLanguage().getMessage("Ingame.Gift.WorldNotAllowed");
		messageItemNotAllowed             = plugin.getLanguage().getMessage("Ingame.Gift.ItemNotAllowed").replaceAll("\\{ItemName}", "%1\\$s");

		messageRequireConfirmation                   = plugin.getLanguage().getMessage("Ingame.Gift.Request.Notification").replaceAll("\\{Name}", "%1\\$s").replaceAll("\\{DisplayName}", "%2\\$s").replaceAll("\\{ItemAmount}", "%3\\$d").replaceAll("\\{ItemName}", "%4\\$s").replaceAll("\\{ItemMetaJSON}", "%5\\$s");
		messageWaitForConfirmation                   = plugin.getLanguage().getMessage("Ingame.Gift.Request.WaitForConfirmation").replaceAll("\\{ItemAmount}", "%1\\$d").replaceAll("\\{ItemName}", "%2\\$s").replaceAll("\\{ItemMetaJSON}", "%3\\$s");
		messageRequestDenied                         = plugin.getLanguage().getMessage("Ingame.Gift.Request.Denied").replaceAll("\\{ItemAmount}", "%1\\$d").replaceAll("\\{ItemName}", "%2\\$s").replaceAll("\\{ItemMetaJSON}", "%3\\$s");
		messageRequestDeniedPartner                  = plugin.getLanguage().getMessage("Ingame.Gift.Request.DeniedPartner").replaceAll("\\{ItemAmount}", "%1\\$d").replaceAll("\\{ItemName}", "%2\\$s").replaceAll("\\{ItemMetaJSON}", "%3\\$s");
		messageRequestCanceled                       = plugin.getLanguage().getMessage("Ingame.Gift.Request.Canceled").replaceAll("\\{ItemAmount}", "%1\\$d").replaceAll("\\{ItemName}", "%2\\$s").replaceAll("\\{ItemMetaJSON}", "%3\\$s");
		messageRequestCanceledPartner                = plugin.getLanguage().getMessage("Ingame.Gift.Request.CanceledPartner").replaceAll("\\{ItemAmount}", "%1\\$d").replaceAll("\\{ItemName}", "%2\\$s").replaceAll("\\{ItemMetaJSON}", "%3\\$s");
		messageRequestCanceledDisconnectRequester    = plugin.getLanguage().getMessage("Ingame.Gift.Request.CanceledDisconnectRequester").replaceAll("\\{ItemAmount}", "%1\\$d").replaceAll("\\{ItemName}", "%2\\$s").replaceAll("\\{ItemMetaJSON}", "%3\\$s");
		messageRequestCanceledDisconnectTarget       = plugin.getLanguage().getMessage("Ingame.Gift.Request.CanceledDisconnectTarget").replaceAll("\\{ItemAmount}", "%1\\$d").replaceAll("\\{ItemName}", "%2\\$s").replaceAll("\\{ItemMetaJSON}", "%3\\$s");
		messageRequestRefundInvFull                  = plugin.getLanguage().getMessage("Ingame.Gift.Request.RefundInvFull");
		messageRequestPartnerAlreadyHasAnOpenRequest = plugin.getLanguage().getMessage("Ingame.Gift.Request.PartnerAlreadyHasAnOpenRequest");

		range                   = plugin.getConfiguration().getRangeSquared(Range.Gift);
		allowedSendGameModes    = plugin.getConfiguration().getGiftAllowedGameModes();
		allowedReceiveGameModes = plugin.getConfiguration().getGiftAllowedReceiveGameModes();
		requireConfirmation     = plugin.getConfiguration().isGiftRequireConfirmationEnabled();
		worldBlacklist          = plugin.getConfiguration().getGiftBlackListedWorlds();
		blacklistEnabled        = !worldBlacklist.isEmpty();

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

		/*if[STANDALONE]
		itemNameResolver = new ItemNameResolver();
		if (at.pcgamingfreaks.Bukkit.MCVersion.isOlderThan(at.pcgamingfreaks.Bukkit.MCVersion.MC_1_13))
		{
			at.pcgamingfreaks.Bukkit.Language itemNameLanguage = new at.pcgamingfreaks.Bukkit.Language(plugin, 1, 1, java.io.File.separator + "lang", "items_", "legacy_items_");
			itemNameLanguage.setFileDescription("item name language");
			itemNameLanguage.load(plugin.getConfiguration().getLanguage(), at.pcgamingfreaks.YamlFileUpdateMethod.OVERWRITE);
			itemNameResolver.loadLegacy(itemNameLanguage, plugin.getLogger());
		}
		else
		{
			at.pcgamingfreaks.Bukkit.Language itemNameLanguage = new at.pcgamingfreaks.Bukkit.Language(plugin, 2, java.io.File.separator + "lang", "items_");
			itemNameLanguage.setFileDescription("item name language");
			itemNameLanguage.load(plugin.getConfiguration().getLanguage(), at.pcgamingfreaks.YamlFileUpdateMethod.OVERWRITE);
			itemNameResolver.load(itemNameLanguage, plugin.getLogger());
		}
		else[STANDALONE]*/
		itemNameResolver = at.pcgamingfreaks.PluginLib.Bukkit.ItemNameResolver.getInstance();
		/*end[STANDALONE]*/

		// Make sure that the InventoryUtils are fully initialized. Otherwise, this will cause lag spike when the command is first used.
		InventoryUtils.prepareTitleForOpenInventoryWithCustomTitle("ignore this");
	}

	@Override
	public void execute(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		MarriagePlayer player = getMarriagePlugin().getPlayerData((Player) sender);
		Player bPlayer = (Player) sender;
		if(allowedSendGameModes.contains(bPlayer.getGameMode()) || bPlayer.hasPermission(Permissions.BYPASS_GIFT_GAME_MODE))
		{
			MarriagePlayer partner;
			if(getMarriagePlugin().areMultiplePartnersAllowed() && args.length == 1)
			{
				partner = getMarriagePlugin().getPlayerData(args[0]);
				if(!player.isPartner(partner))
				{
					((MarriageMaster) getMarriagePlugin()).messageTargetPartnerNotFound.send(sender);
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
				final Player bPartner = partner.getPlayerOnline();
				if(bPartner != null && getMarriagePlugin().isInRangeSquared(bPlayer, bPartner, range))
				{
					ItemStack its = InventoryUtils.getItemInMainHand(bPlayer);
					if(its == null || its.getType() == Material.AIR || its.getAmount() == 0)
					{
						messageNoItemInHand.send(sender);
						return;
					}
					int slot = bPartner.getInventory().firstEmpty();
					if(slot == -1)
					{
						messagePartnerInvFull.send(sender);
						return;
					}
					if(!allowedReceiveGameModes.contains(bPartner.getGameMode()) && !bPartner.hasPermission(Permissions.BYPASS_GIFT_GAME_MODE))
					{
						messageGameModeNotAllowedReceiver.send(sender, allowedReceiveGameModes.toString().toLowerCase(Locale.ENGLISH), bPlayer.getGameMode().name().toLowerCase(Locale.ENGLISH));
						return;
					}
					if(blacklistEnabled && !sender.hasPermission(Permissions.BYPASS_GIFT_WORLD) && worldBlacklist.contains(bPartner.getWorld().getName().toLowerCase(Locale.ENGLISH)))
					{
						player.send(messageWorldNotAllowed);
						return;
					}
					if((itemFilter != null && !sender.hasPermission(Permissions.BYPASS_GIFT_ITEM_FILTER) && itemFilter.isItemBlocked(its)) ||
							(((MarriageMaster) plugin).getBackpacksIntegration() != null && ((MarriageMaster) plugin).getBackpacksIntegration().isBackpackItem(its)))
					{
						player.send(messageItemNotAllowed, itemNameResolver.getName(its));
						return;
					}
					GiftEvent event = new GiftEvent(player, player.getMarriageData(partner), its);
					Bukkit.getPluginManager().callEvent(event);
					if(!event.isCancelled())
					{
						its = event.getItemStack();
						final String itemJson = InventoryUtils.convertItemStackToJson(its, plugin.getLogger());
						final String itemName = itemNameResolver.getName(its);
						if(requireConfirmation)
						{
							if(!getMarriagePlugin().getCommandManager().registerAcceptPendingRequest(new GiftRequest(its, itemJson, itemName, partner, player)))
							{
								messageRequestPartnerAlreadyHasAnOpenRequest.send(sender);
								return;
							}
							messageWaitForConfirmation.send(sender, its.getAmount(), itemName, itemJson);
							partner.send(messageRequireConfirmation, player.getName(), player.getDisplayName(), its.getAmount(), itemName, itemJson);
						}
						else
						{
							bPartner.getInventory().setItem(slot, its);
							messageItemSent.send(sender, bPartner.getName(), bPartner.getDisplayName(), its.getAmount(), itemName, itemJson);
							messageItemReceived.send(bPartner, bPlayer.getName(), bPlayer.getDisplayName(), its.getAmount(), itemName, itemJson);
						}
						InventoryUtils.setItemInMainHand(bPlayer, null);
					}
				}
				else
				{
					((MarriageMaster) getMarriagePlugin()).messagePartnerNotInRange.send(sender);
				}
			}
			else
			{
				((MarriageMaster) getMarriagePlugin()).messagePartnerOffline.send(sender);
			}
		}
		else
		{
			messageGameModeNotAllowedSender.send(sender, allowedSendGameModes.toString().toLowerCase(Locale.ENGLISH), bPlayer.getGameMode().name().toLowerCase(Locale.ENGLISH));
		}
	}

	@Override
	public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		return getMarriagePlugin().getCommandManager().getSimpleTabComplete(sender, args);
	}

	private class GiftRequest extends AcceptPendingRequest
	{
		private final ItemStack item;
		private final String itemJson, itemName;
		private final MarriagePlayer recipient, sender;

		public GiftRequest(final @NotNull ItemStack item, final @NotNull String itemJson, final @NotNull String itemName, final @NotNull MarriagePlayer recipient, final @NotNull MarriagePlayer sender)
		{
			super(recipient, sender);
			this.item = item;
			this.recipient = recipient;
			this.sender = sender;
			this.itemName = itemName;
			this.itemJson = itemJson;
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
			recipient.getPlayerOnline().getInventory().setItem(slot, item);
			sender.send(messageItemSent, recipient.getName(), recipient.getDisplayName(), item.getAmount(), itemName, itemJson);
			recipient.send(messageItemReceived, sender.getName(), sender.getDisplayName(), item.getAmount(), itemName, itemJson);
		}

		@Override
		protected void onDeny()
		{
			getPlayerThatHasToAccept().send(messageRequestDenied, item.getAmount(), itemName, itemJson);
			sender.send(messageRequestDeniedPartner, item.getAmount(), itemName, itemJson);
			refund();
		}

		@Override
		protected void onCancel(@NotNull MarriagePlayer player)
		{
			player.send(messageRequestCanceled, item.getAmount(), itemName, itemJson);
			getPlayerThatHasToAccept().send(messageRequestCanceledPartner, item.getAmount(), itemName, itemJson);
			refund();
		}

		@Override
		protected void onDisconnect(@NotNull MarriagePlayer player)
		{
			if(sender.equals(player)) getPlayerThatHasToAccept().send(messageRequestCanceledDisconnectRequester, item.getAmount(), itemName, itemJson);
			else player.send(messageRequestCanceledDisconnectTarget, item.getAmount(), itemName, itemJson);
			refund();
		}

		private void refund()
		{
			Player player = sender.getPlayerOnline();
			if(player == null) return;
			Map<Integer, ItemStack> left = player.getInventory().addItem(item);
			if(left.isEmpty()) return;
			left.values().forEach(i -> player.getWorld().dropItemNaturally(player.getLocation(), i));
			messageRequestRefundInvFull.send(player);
		}
	}
}
