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
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import at.pcgamingfreaks.georgh.MarriageMaster.MarriageMaster;
import net.milkbowl.vault.economy.EconomyResponse;

public class Economy extends BaseEconomy
{	
	public Economy(MarriageMaster marriagemaster)
	{
		super(marriagemaster);
	}

	public boolean HomeTeleport(Player player, double money)
	{
		EconomyResponse response = econ.withdrawPlayer(player, money);
		
		if(response.transactionSuccess()) 
		{
			player.sendMessage(String.format(ChatColor.GREEN + marriageMaster.lang.Get("Economy.HomeTPPaid"), econ.format(response.amount), econ.getBalance(player)));
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
		EconomyResponse response = econ.withdrawPlayer(player, money);
		
		if(response.transactionSuccess()) 
		{
			player.sendMessage(String.format(ChatColor.GREEN + marriageMaster.lang.Get("Economy.SetHomePaid"), econ.format(response.amount), econ.getBalance(player)));
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
		EconomyResponse response = econ.withdrawPlayer(player, money);
		
		if(response.transactionSuccess()) 
		{
			player.sendMessage(String.format(ChatColor.GREEN + marriageMaster.lang.Get("Economy.GiftPaid"), econ.format(response.amount), econ.getBalance(player)));
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
		EconomyResponse response = econ.withdrawPlayer(player, money);
		
		if(response.transactionSuccess()) 
		{
			player.sendMessage(String.format(ChatColor.GREEN + marriageMaster.lang.Get("TPPaid"), econ.format(response.amount), econ.getBalance(player)));
			return true;
		} 
		else 
		{
			player.sendMessage(String.format(ChatColor.RED + marriageMaster.lang.Get("Economy.NotEnough")));
			return false;
		}
	}
	
	public boolean Divorce(CommandSender priest, Player player, Player otherPlayer, double money)
	{
		EconomyResponse response = econ.withdrawPlayer(player, money/2);
		EconomyResponse response2 = econ.withdrawPlayer(otherPlayer, money/2);
		if(response.transactionSuccess() && response2.transactionSuccess()) 
		{
			otherPlayer.sendMessage(String.format(ChatColor.GREEN + marriageMaster.lang.Get("Economy.DivorcePaid"), econ.format(response.amount), econ.getBalance(otherPlayer)));
			player.sendMessage(String.format(ChatColor.GREEN + marriageMaster.lang.Get("Economy.DivorcePaid"), econ.format(response.amount), econ.getBalance(player)));
			return true;
		} 
		else 
		{
			if(priest != null)
			{
				priest.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Economy.DivNotEnoPriestI"));
			}
			if(response.transactionSuccess())
			{
				econ.depositPlayer(player, money/2);
				otherPlayer.sendMessage(String.format(ChatColor.RED + marriageMaster.lang.Get("Economy.NotEnough")));
				player.sendMessage(String.format(ChatColor.RED + marriageMaster.lang.Get("Economy.PartnerNotEnough")));
				return false;
			}
			if(response2.transactionSuccess())
			{
				econ.depositPlayer(otherPlayer, money/2);
				player.sendMessage(String.format(ChatColor.RED + marriageMaster.lang.Get("Economy.NotEnough")));
				otherPlayer.sendMessage(String.format(ChatColor.RED + marriageMaster.lang.Get("Economy.PartnerNotEnough")));
				return false;
			}
			otherPlayer.sendMessage(String.format(ChatColor.RED + marriageMaster.lang.Get("Economy.NotEnough")));
			player.sendMessage(String.format(ChatColor.RED + marriageMaster.lang.Get("Economy.NotEnough")));
			return false;
		}
	}
	
	public boolean Marry(CommandSender priest, Player player, Player otherPlayer, double money)
	{
		EconomyResponse response = econ.withdrawPlayer(player, money/2);
		EconomyResponse response2 = econ.withdrawPlayer(otherPlayer, money/2);
		
		if(response.transactionSuccess() && response2.transactionSuccess()) 
		{
			otherPlayer.sendMessage(String.format(ChatColor.GREEN + marriageMaster.lang.Get("Economy.MarriagePaid"), econ.format(response.amount), econ.getBalance(otherPlayer)));
			player.sendMessage(String.format(ChatColor.GREEN + marriageMaster.lang.Get("Economy.MarriagePaid"), econ.format(response.amount), econ.getBalance(player)));
			return true;
		} 
		else 
		{
			if(priest != null)
			{
				priest.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Economy.NotEnoughPriestInfo"));
			}
			if(response.transactionSuccess())
			{
				econ.depositPlayer(player, money/2);
				otherPlayer.sendMessage(String.format(ChatColor.RED + marriageMaster.lang.Get("Economy.NotEnough")));
				player.sendMessage(String.format(ChatColor.RED + marriageMaster.lang.Get("Economy.PartnerNotEnough")));
				return false;
			}
			if(response2.transactionSuccess())
			{
				econ.depositPlayer(otherPlayer, money/2);
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
