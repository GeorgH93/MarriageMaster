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

package at.pcgamingfreaks.georgh.MarriageMaster.Economy;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import at.pcgamingfreaks.georgh.MarriageMaster.MarriageMaster;

public class HEconomy 
{
	private MarriageMaster marriageMaster;
    public Economy econ;
	
	public HEconomy(MarriageMaster marriagemaster)
	{
		marriageMaster = marriagemaster;
		
		if(marriageMaster.getServer().getPluginManager().getPlugin("Vault") == null)
		{
			return;
	    }
		
        RegisteredServiceProvider<Economy> rsp = marriageMaster.getServer().getServicesManager().getRegistration(Economy.class);
        
        if (rsp == null) 
        {
            return;
        }
    
        this.econ = rsp.getProvider();
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
			player.sendMessage(String.format(ChatColor.RED + marriageMaster.lang.Get("Economy.NoEnough")));
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
			player.sendMessage(String.format(ChatColor.RED + marriageMaster.lang.Get("Economy.NoEnough")));
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
			player.sendMessage(String.format(ChatColor.RED + marriageMaster.lang.Get("Economy.NoEnough")));
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
				otherPlayer.sendMessage(String.format(ChatColor.RED + marriageMaster.lang.Get("Economy.NoEnough")));
				player.sendMessage(String.format(ChatColor.RED + marriageMaster.lang.Get("Economy.PartnerNoEnough")));
				return false;
			}
			if(response2.transactionSuccess())
			{
				player.sendMessage(String.format(ChatColor.RED + marriageMaster.lang.Get("Economy.NoEnough")));
				otherPlayer.sendMessage(String.format(ChatColor.RED + marriageMaster.lang.Get("Economy.PartnerNoEnough")));
				return false;
			}

			otherPlayer.sendMessage(String.format(ChatColor.RED + marriageMaster.lang.Get("Economy.NoEnough")));
			player.sendMessage(String.format(ChatColor.RED + marriageMaster.lang.Get("Economy.NoEnough")));
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
				otherPlayer.sendMessage(String.format(ChatColor.RED + marriageMaster.lang.Get("Economy.NoEnough")));
				player.sendMessage(String.format(ChatColor.RED + marriageMaster.lang.Get("Economy.PartnerNoEnough")));
				return false;
			}
			if(response2.transactionSuccess())
			{
				player.sendMessage(String.format(ChatColor.RED + marriageMaster.lang.Get("Economy.NoEnough")));
				otherPlayer.sendMessage(String.format(ChatColor.RED + marriageMaster.lang.Get("Economy.PartnerNoEnough")));
				return false;
			}

			otherPlayer.sendMessage(String.format(ChatColor.RED + "You dont have enough money"));
			player.sendMessage(String.format(ChatColor.RED + "You dont have enough money"));
			return false;
		}
	}
}
