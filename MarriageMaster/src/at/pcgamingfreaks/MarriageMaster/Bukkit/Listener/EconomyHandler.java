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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Listener;

import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.ConsoleColor;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.*;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.Message.Placeholder.Placeholder;
import at.pcgamingfreaks.Message.Placeholder.Processors.FloatPlaceholderProcessor;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyHandler implements Listener
{
	private final MarriageMaster plugin;
	private double costMarry, costDivorce, costTp, costHome, costSetHome, costGift, costChangeSurname;
	private Message messageNotEnough, messagePartnerNotEnough, messageMarriagePaid, messageDivorcePaid, messagePriestMarryNotEnough, messagePriestDivorceNotEnough, messageTpPaid, messageSetHomePaid, messageHomePaid;
	private Message messageGiftPaid, messageSurnameChangePaid;
	private Economy econ = null;

	public EconomyHandler(MarriageMaster plugin)
	{
		this.plugin = plugin;
		if(!setupEconomy(plugin))
		{
			plugin.getLogger().info(ConsoleColor.RED + "Failed to connect with Vault's economy provider. Disable economy." + ConsoleColor.RESET);
			return;
		}

		// Load costs
		costMarry         = plugin.getConfiguration().getEconomyValue("Marry") / 2.0;
		costDivorce       = plugin.getConfiguration().getEconomyValue("Divorce") / 2.0;
		costTp            = plugin.getConfiguration().getEconomyValue("Tp");
		costGift          = plugin.getConfiguration().getEconomyValue("Gift");
		costHome          = plugin.getConfiguration().getEconomyValue("HomeTp");
		costSetHome       = plugin.getConfiguration().getEconomyValue("SetHome");
		costChangeSurname = plugin.getConfiguration().getEconomyValue("ChangeSurname");
		// Load messages
		int digits = econ.fractionalDigits();
		FloatPlaceholderProcessor floatPlaceholderProcessor = new FloatPlaceholderProcessor(digits < 0 ? 2 : digits);
		Placeholder[] ecoPlaceholders = { new Placeholder("Cost", floatPlaceholderProcessor, Placeholder.AUTO_INCREMENT_INDIVIDUALLY),
										  new Placeholder("Remaining", floatPlaceholderProcessor, Placeholder.AUTO_INCREMENT_INDIVIDUALLY),
										  new Placeholder("CurrencyName", Placeholder.AUTO_INCREMENT_INDIVIDUALLY) };
		messageNotEnough              = getMessage(plugin, "NotEnough").placeholders(ecoPlaceholders);
		messagePartnerNotEnough       = getMessage(plugin, "PartnerNotEnough");
		messagePriestMarryNotEnough   = getMessage(plugin, "PriestMarryNotEnough");
		messagePriestDivorceNotEnough = getMessage(plugin, "PriestDivorceNotEnough");
		messageMarriagePaid           = getMessage(plugin, "MarriagePaid").placeholders(ecoPlaceholders);
		messageDivorcePaid            = getMessage(plugin, "DivorcePaid") .placeholders(ecoPlaceholders);
		messageTpPaid                 = getMessage(plugin, "TpPaid")      .placeholders(ecoPlaceholders);
		messageHomePaid               = getMessage(plugin, "HomeTPPaid")  .placeholders(ecoPlaceholders);
		messageSetHomePaid            = getMessage(plugin, "SetHomePaid") .placeholders(ecoPlaceholders);
		messageGiftPaid               = getMessage(plugin, "GiftPaid")    .placeholders(ecoPlaceholders);
		messageSurnameChangePaid      = getMessage(plugin, "SurnameChangePaid").placeholders(ecoPlaceholders);

		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	private Message getMessage(MarriageMaster plugin, String key)
	{
		return plugin.getLanguage().getMessage("Ingame.Economy." + key);
	}

	private boolean setupEconomy(MarriageMaster plugin)
	{
		if(plugin.getServer().getPluginManager().getPlugin("Vault") == null) return false;
		RegisteredServiceProvider<Economy> economyProvider = plugin.getServer().getServicesManager().getRegistration(Economy.class);
		return (econ = (economyProvider != null) ? economyProvider.getProvider() : null) != null;
	}

	private boolean hasPlayerEnoughMoney(MarriagePlayer player, double cost)
	{
		return econ.has(player.getPlayer(), cost);
	}

	private boolean billPlayer(MarriagePlayer player, double cost)
	{
		return econ.withdrawPlayer(player.getPlayer(), cost).transactionSuccess();
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	private boolean basicBillPlayer(MarriagePlayer player, double cost, Message successMessage)
	{
		if(hasPlayerEnoughMoney(player, cost) && billPlayer(player, cost))
		{
			if(player.isOnline()) player.send(successMessage, cost, econ.getBalance(player.getPlayer()), econ.currencyNamePlural());
			return true;
		}
		else
		{
			if(player.isOnline()) player.send(messageNotEnough, cost, econ.getBalance(player.getPlayer()), econ.currencyNamePlural());
			return false;
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onMarry(MarryEvent event)
	{
		if(costMarry <= 0) return;
		if(!billMarryOrDivorce(event.getPlayer1(), event.getPlayer2(), event.getPriestIfNotOneOfTheCouple(), costMarry, messageMarriagePaid, messagePriestMarryNotEnough))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onDivorce(DivorceEvent event)
	{
		if(costDivorce <= 0) return;
		if(!billMarryOrDivorce(event.getMarriageData().getPartner1(), event.getMarriageData().getPartner2(), event.getPriestIfNotOneOfTheCouple(), costDivorce, messageDivorcePaid, messagePriestDivorceNotEnough))
		{
			event.setCancelled(true);
		}
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	private boolean billMarryOrDivorce(MarriagePlayer player1, MarriagePlayer player2, CommandSender priest, double cost, Message success, Message failPriest)
	{
		boolean failedPlayer2 = false;
		if(hasPlayerEnoughMoney(player1, cost) && hasPlayerEnoughMoney(player2, cost))
		{
			if(billPlayer(player1, cost))
			{
				if(billPlayer(player2, cost))
				{
					player1.send(success, cost, econ.getBalance(player1.getPlayer()), econ.currencyNamePlural());
					player2.send(success, cost, econ.getBalance(player2.getPlayer()), econ.currencyNamePlural());
					return true;
				}
				else
				{ // It should not happen, it's just there to be on the save site
					econ.depositPlayer(player1.getPlayer(), cost); // Failed to bill player 2. Return the billed money to player 1.
					failedPlayer2 = true;
				}
			}
		}
		if(!hasPlayerEnoughMoney(player2, cost) || failedPlayer2)
		{
			player1.send(messagePartnerNotEnough);
			player2.send(messageNotEnough, cost, econ.currencyNamePlural());
		}
		else
		{
			player2.send(messagePartnerNotEnough);
			player1.send(messageNotEnough, cost, econ.currencyNamePlural());
		}
		if(priest != null) failPriest.send(priest);
		return false;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onTeleport(TPEvent event)
	{
		if(costTp <= 0) return;
		if(!basicBillPlayer(event.getPlayer(), costTp, messageTpPaid)) event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onGift(GiftEvent event)
	{
		if(costGift <= 0) return;
		if(!basicBillPlayer(event.getPlayer(), costGift, messageGiftPaid)) event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onHome(HomeTPEvent event)
	{
		if(costHome <= 0) return;
		if(!basicBillPlayer(event.getPlayer(), costHome, messageHomePaid)) event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onSetHome(HomeSetEvent event)
	{
		if(costSetHome <= 0) return;
		if(!basicBillPlayer(event.getPlayer(), costSetHome, messageSetHomePaid)) event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onChangeSurname(SurnameChangeEvent event)
	{
		if(costChangeSurname <= 0 || !(event.getChangedBy() instanceof Player)) return;
		if(!basicBillPlayer(plugin.getPlayerData((Player) event.getChangedBy()), costChangeSurname, messageSurnameChangePaid)) event.setCancelled(true);
	}
}