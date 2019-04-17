/*
 *   Copyright (C) 2019 GeorgH93
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

import at.pcgamingfreaks.Bukkit.ItemNameResolver;
import at.pcgamingfreaks.Bukkit.MCVersion;
import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.Bukkit.Utils;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.GiftEvent;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarryCommand;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.PluginLib.Bukkit.PluginLib;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GiftCommand extends MarryCommand
{
	private static final boolean DUAL_WIELDING_MC = MCVersion.isNewerOrEqualThan(MCVersion.MC_1_9);
	private final Message messageGiftsOnlyInSurvival, messageNoItemInHand, messagePartnerInvFull, messageItemSent, messageItemReceived;
	private final double range;
	private final boolean allowedInCreative;
	private final ItemNameResolver itemNameResolver;

	public GiftCommand(MarriageMaster plugin)
	{
		super(plugin, "gift", plugin.getLanguage().getTranslated("Commands.Description.Gift"), "marry.gift", true, true, plugin.getLanguage().getCommandAliases("Gift"));

		messageGiftsOnlyInSurvival = plugin.getLanguage().getMessage("Ingame.Gift.OnlyInSurvival");
		messageNoItemInHand        = plugin.getLanguage().getMessage("Ingame.Gift.NoItemInHand");
		messagePartnerInvFull      = plugin.getLanguage().getMessage("Ingame.Gift.PartnerInvFull");
		messageItemSent            = plugin.getLanguage().getMessage("Ingame.Gift.ItemSent").replaceAll("\\{ItemAmount\\}", "%1\\$d").replaceAll("\\{ItemName\\}", "%2\\$s").replaceAll("\\{ItemMetaJSON\\}", "%3\\$s");
		messageItemReceived        = plugin.getLanguage().getMessage("Ingame.Gift.ItemReceived").replaceAll("\\{ItemAmount\\}", "%1\\$d").replaceAll("\\{ItemName\\}", "%2\\$s").replaceAll("\\{ItemMetaJSON\\}", "%3\\$s");

		range             = plugin.getConfiguration().getRange("Gift");
		allowedInCreative = plugin.getConfiguration().isGiftAllowedInCreative();

		itemNameResolver = ((PluginLib) PluginLib.getInstance()).getItemNameResolver();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void execute(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		MarriagePlayer player = getMarriagePlugin().getPlayerData((Player) sender);
		Player bPlayer = (Player) sender;
		if(bPlayer.getGameMode().equals(GameMode.SURVIVAL) || allowedInCreative || bPlayer.hasPermission("marry.bypass.giftgamemode"))
		{
			MarriagePlayer partner;
			if(getMarriagePlugin().isPolygamyAllowed() && args.length == 1)
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
				if(mPD == null) { return; } // Should never happen, but it's always good to be save!
				partner = player.getNearestPartnerMarriageData().getPartner(player);
			}
			if(partner != null && partner.isOnline())
			{
				final Player bPartner = partner.getPlayerOnline();
				if(bPartner != null && getMarriagePlugin().isInRange(bPlayer, bPartner, range))
				{
					ItemStack its = DUAL_WIELDING_MC ? bPlayer.getInventory().getItemInMainHand() : bPlayer.getInventory().getItemInHand();
					if(its == null || its.getType() == Material.AIR)
					{
						messageNoItemInHand.send(sender);
						return;
					}
					if(bPartner.getInventory().firstEmpty() == -1)
					{
						messagePartnerInvFull.send(sender);
						return;
					}
					GiftEvent event = new GiftEvent(player, player.getMarriageData(partner), its);
					Bukkit.getPluginManager().callEvent(event);
					if(!event.isCancelled())
					{
						its = event.getItemStack();
						bPartner.getInventory().addItem(its);
						if(DUAL_WIELDING_MC) bPlayer.getInventory().setItemInMainHand(null); else bPlayer.getInventory().setItemInHand(null);
						final String itemJson = (Utils.convertItemStackToJson(its, plugin.getLogger()));
						final String itemName = itemNameResolver.getName(its);
						messageItemSent.send(sender, its.getAmount(), itemName, itemJson);
						messageItemReceived.send(bPartner, its.getAmount(), itemName, itemJson);
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
			messageGiftsOnlyInSurvival.send(sender);
		}
	}

	@Override
	public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		return getMarriagePlugin().getCommandManager().getSimpleTabComplete(sender, args);
	}
}