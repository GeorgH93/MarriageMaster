/*
 *   Copyright (C) 2016 GeorgH93
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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Listener;

import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.ConsoleColor;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.*;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyManager implements Listener
{
	private MarriageMaster plugin;
	private double costMarry, costDivorce, costTp, costHome, costSetHome, costGift;
	private Message messageNotEnough, messagePartnerNotEnough, messageMarriagePaid, messageDivorcePaid, messagePriestMarryNotEnough, messagePriestDivorceNotEnough, messageTpPaid, messageSetHomePaid, messageHomePaid, messageGiftPaid;
	private Economy econ = null;

	public EconomyManager(MarriageMaster plugin)
	{
		this.plugin = plugin;

		if(!setupEconomy())
		{
			plugin.getLogger().info(ConsoleColor.RED + "Failed to connect with Vault's economy provider. Disable economy." + ConsoleColor.RESET);
			return;
		}
		// Load costs
		costMarry   = plugin.getConfiguration().getEconomyValue("Marry") / 2.0;
		costDivorce = plugin.getConfiguration().getEconomyValue("Divorce") / 2.0;
		costTp      = plugin.getConfiguration().getEconomyValue("Tp");
		costGift    = plugin.getConfiguration().getEconomyValue("Gift");
		costHome    = plugin.getConfiguration().getEconomyValue("HomeTp");
		costSetHome = plugin.getConfiguration().getEconomyValue("SetHome");
		// Load messages
		messageNotEnough              = getMessage("NotEnough").replaceAll("\\{Cost}", "%1\\$.2f").replaceAll("\\{CurrencyName\\}", "%2\\$s");
		messagePartnerNotEnough       = getMessage("PartnerNotEnough");
		messagePriestMarryNotEnough   = getMessage("PriestMarryNotEnough");
		messagePriestDivorceNotEnough = getMessage("PriestDivorceNotEnough");
		messageMarriagePaid           = getMessage("MarriagePaid").replaceAll("\\{Cost}", "%1\\$.2f").replaceAll("\\{Remaining}", "%2\\$.2f").replaceAll("\\{CurrencyName}", "%3\\$s");
		messageDivorcePaid            = getMessage("DivorcePaid") .replaceAll("\\{Cost}", "%1\\$.2f").replaceAll("\\{Remaining}", "%2\\$.2f").replaceAll("\\{CurrencyName}", "%3\\$s");
		messageTpPaid                 = getMessage("TpPaid")      .replaceAll("\\{Cost}", "%1\\$.2f").replaceAll("\\{Remaining}", "%2\\$.2f").replaceAll("\\{CurrencyName}", "%3\\$s");
		messageHomePaid               = getMessage("HomeTPPaid")  .replaceAll("\\{Cost}", "%1\\$.2f").replaceAll("\\{Remaining}", "%2\\$.2f").replaceAll("\\{CurrencyName}", "%3\\$s");
		messageSetHomePaid            = getMessage("SetHomePaid") .replaceAll("\\{Cost}", "%1\\$.2f").replaceAll("\\{Remaining}", "%2\\$.2f").replaceAll("\\{CurrencyName}", "%3\\$s");
		messageGiftPaid               = getMessage("GiftPaid")    .replaceAll("\\{Cost}", "%1\\$.2f").replaceAll("\\{Remaining}", "%2\\$.2f").replaceAll("\\{CurrencyName}", "%3\\$s");
	}

	private Message getMessage(String key)
	{
		return plugin.getLanguage().getMessage("Ingame.Economy." + key);
	}

	private boolean setupEconomy()
	{
		if(plugin.getServer().getPluginManager().getPlugin("Vault") == null)
		{
			return false;
		}
		RegisteredServiceProvider<Economy> economyProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null)
		{
			econ = economyProvider.getProvider();
		}
		return (econ != null);
	}

	private boolean hasPlayerEnoughMoney(MarriagePlayer player, double cost)
	{
		return econ.has(player.getPlayer(), cost);
	}

	private boolean billPlayer(MarriagePlayer player, double cost)
	{
		return econ.withdrawPlayer(player.getPlayer(), cost).transactionSuccess();
	}

	private boolean basicBillPlayer(MarriagePlayer player, double cost, Message successMessage)
	{
		if(hasPlayerEnoughMoney(player, costTp) && billPlayer(player, costTp))
		{
			if(player.isOnline()) successMessage.send(player.getPlayer().getPlayer(), cost, econ.getBalance(player.getPlayer()), econ.currencyNamePlural());
			return true;
		}
		else
		{
			if(player.isOnline()) messageNotEnough.send(player.getPlayer().getPlayer(), cost, econ.currencyNamePlural());
			return false;
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onMarry(MarryEvent event)
	{
		if(costMarry <= 0) return;
		if(billMarryOrDivorce(event.getPlayer1(), event.getPlayer2(), (!event.isPriestAPlayer() || !(event.getPlayer1().equals(event.getPriest()) || event.getPlayer2().equals(event.getPriest()))) ? event.getPriest() : null, costMarry, messageMarriagePaid, messagePriestMarryNotEnough))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onDivorce(DivorceEvent event)
	{
		if(costDivorce <= 0) return;
		if(billMarryOrDivorce(event.getMarriageData().getPartner1(), event.getMarriageData().getPartner2(), (!event.isPriestAPlayer() || !event.getMarriageData().hasPlayer((MarriagePlayer) event.getPriest())) ? event.getPriest() : null, costDivorce, messageDivorcePaid, messagePriestDivorceNotEnough))
		{
			event.setCancelled(true);
		}
	}
	
	private boolean billMarryOrDivorce(MarriagePlayer player1, MarriagePlayer player2, CommandSender priest, double cost, Message success, Message failPriest)
	{
		boolean f2 = false;
		if(hasPlayerEnoughMoney(player1, cost) && hasPlayerEnoughMoney(player2, cost))
		{
			if(billPlayer(player1, cost))
			{
				if(billPlayer(player2, cost))
				{
					success.send(player1.getPlayer().getPlayer(), cost, econ.getBalance(player1.getPlayer()), econ.currencyNamePlural());
					success.send(player2.getPlayer().getPlayer(), cost, econ.getBalance(player2.getPlayer()), econ.currencyNamePlural());
					return true;
				}
				else
				{
					econ.depositPlayer(player1.getPlayer(), cost); // Failed to bill player 2. Return the billed money to player 1.
					f2 = true;
				}
			}
		}
		if(f2 || hasPlayerEnoughMoney(player2, cost))
		{
			messagePartnerNotEnough.send(player1.getPlayer().getPlayer());
			messageNotEnough.send(player2.getPlayer().getPlayer(), cost);
		}
		else
		{
			messagePartnerNotEnough.send(player2.getPlayer().getPlayer());
			messageNotEnough.send(player1.getPlayer().getPlayer(), cost);
		}
		if(priest != null)
		{
			failPriest.send(priest);
		}
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
}