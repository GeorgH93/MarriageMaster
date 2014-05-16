/*
 *   Copyright (C) 2014 GeorgH93
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

package at.pcgamingfreaks.georgh.MarriageMaster;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

public class MMEconomy 
{
	private MarriageMaster marriageMaster;
    public Economy econ = null;
    
    private boolean setupEconomy()
    {
    	if(marriageMaster.getServer().getPluginManager().getPlugin("Vault") == null)
    	{
    		return false;
    	}
        RegisteredServiceProvider<Economy> economyProvider = marriageMaster.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null)
        {
        	econ = economyProvider.getProvider();
        }
        return (econ != null);
    }
	
	public MMEconomy(MarriageMaster marriagemaster)
	{
		marriageMaster = marriagemaster;
		
		if(marriageMaster.config.UseEconomy() && !setupEconomy())
		{
			marriageMaster.config.SetEconomyOff();
			marriageMaster.log.info("Console.NoEcoPL");
		}
	}

	public boolean HomeTeleport(Player player, double money)
	{
		EconomyResponse response = econ.withdrawPlayer(player.getName(), money);
		
		if(response.transactionSuccess()) 
		{
			player.sendMessage(String.format(ChatColor.GREEN + marriageMaster.lang.Get("Economy.HomeTPPaid"), econ.format(response.amount), econ.getBalance(player.getName())));
			return true;
		} 
		else 
		{
			player.sendMessage(String.format(ChatColor.RED + marriageMaster.lang.Get("Economy.NotEnough")));
			return false;
		}
	}
	
	public boolean SetHome(Player player, double money)
	{
		EconomyResponse response = econ.withdrawPlayer(player.getName(), money);
		
		if(response.transactionSuccess()) 
		{
			player.sendMessage(String.format(ChatColor.GREEN + marriageMaster.lang.Get("Economy.SetHomePaid"), econ.format(response.amount), econ.getBalance(player.getName())));
			return true;
		} 
		else 
		{
			player.sendMessage(String.format(ChatColor.RED + marriageMaster.lang.Get("Economy.NotEnough")));
			return false;
		}
	}
	
	public boolean Gift(Player player, double money)
	{
		EconomyResponse response = econ.withdrawPlayer(player.getName(), money);
		
		if(response.transactionSuccess()) 
		{
			player.sendMessage(String.format(ChatColor.GREEN + marriageMaster.lang.Get("Economy.GiftPaid"), econ.format(response.amount), econ.getBalance(player.getName())));
			return true;
		} 
		else 
		{
			player.sendMessage(String.format(ChatColor.RED + marriageMaster.lang.Get("Economy.NotEnough")));
			return false;
		}
	}
	
	public boolean Teleport(Player player, double money)
	{
		EconomyResponse response = econ.withdrawPlayer(player.getName(), money);
		
		if(response.transactionSuccess()) 
		{
			player.sendMessage(String.format(ChatColor.GREEN + marriageMaster.lang.Get("TPPaid"), econ.format(response.amount), econ.getBalance(player.getName())));
			return true;
		} 
		else 
		{
			player.sendMessage(String.format(ChatColor.RED + marriageMaster.lang.Get("Economy.NotEnough")));
			return false;
		}
	}
	
	public boolean Divorce(Player player, Player otherPlayer, double money)
	{
		EconomyResponse response = econ.withdrawPlayer(player.getName(), money/2);
		EconomyResponse response2 = econ.withdrawPlayer(otherPlayer.getName(), money/2);
		if(response.transactionSuccess() && response2.transactionSuccess()) 
		{
			otherPlayer.sendMessage(String.format(ChatColor.GREEN + marriageMaster.lang.Get("Economy.DivorcePaid"), econ.format(response.amount), econ.getBalance(otherPlayer.getName())));
			player.sendMessage(String.format(ChatColor.GREEN + marriageMaster.lang.Get("Economy.DivorcePaid"), econ.format(response.amount), econ.getBalance(player.getName())));
			return true;
		} 
		else 
		{
			if(response.transactionSuccess())
			{
				otherPlayer.sendMessage(String.format(ChatColor.RED + marriageMaster.lang.Get("Economy.NotEnough")));
				player.sendMessage(String.format(ChatColor.RED + marriageMaster.lang.Get("Economy.PartnerNotEnough")));
				return false;
			}
			if(response2.transactionSuccess())
			{
				player.sendMessage(String.format(ChatColor.RED + marriageMaster.lang.Get("Economy.NotEnough")));
				otherPlayer.sendMessage(String.format(ChatColor.RED + marriageMaster.lang.Get("Economy.PartnerNotEnough")));
				return false;
			}
			otherPlayer.sendMessage(String.format(ChatColor.RED + marriageMaster.lang.Get("Economy.NotEnough")));
			player.sendMessage(String.format(ChatColor.RED + marriageMaster.lang.Get("Economy.NotEnough")));
			return false;
		}
	}
	
	public boolean Marry(Player player, Player otherPlayer, double money)
	{
		EconomyResponse response = econ.withdrawPlayer(player.getName(), money/2);
		EconomyResponse response2 = econ.withdrawPlayer(otherPlayer.getName(), money/2);
		
		if(response.transactionSuccess() && response2.transactionSuccess()) 
		{
			otherPlayer.sendMessage(String.format(ChatColor.GREEN + marriageMaster.lang.Get("Economy.MarriagePaid"), econ.format(response.amount), econ.getBalance(otherPlayer.getName())));
			player.sendMessage(String.format(ChatColor.GREEN + marriageMaster.lang.Get("Economy.MarriagePaid"), econ.format(response.amount), econ.getBalance(player.getName())));
			return true;
		} 
		else 
		{
			if(response.transactionSuccess())
			{
				otherPlayer.sendMessage(String.format(ChatColor.RED + marriageMaster.lang.Get("Economy.NotEnough")));
				player.sendMessage(String.format(ChatColor.RED + marriageMaster.lang.Get("Economy.PartnerNotEnough")));
				return false;
			}
			if(response2.transactionSuccess())
			{
				player.sendMessage(String.format(ChatColor.RED + marriageMaster.lang.Get("Economy.NotEnough")));
				otherPlayer.sendMessage(String.format(ChatColor.RED + marriageMaster.lang.Get("Economy.PartnerNotEnough")));
				return false;
			}
			otherPlayer.sendMessage(String.format(ChatColor.RED + marriageMaster.lang.Get("Economy.NotEnough")));
			player.sendMessage(String.format(ChatColor.RED + marriageMaster.lang.Get("Economy.NotEnough")));
			return false;
		}
	}
}
