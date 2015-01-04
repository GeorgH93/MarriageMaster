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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Economy;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import net.milkbowl.vault.economy.EconomyResponse;

@SuppressWarnings("deprecation")
public class EconomyOld extends BaseEconomy
{	
	public EconomyOld(MarriageMaster marriagemaster)
	{
		super(marriagemaster);
	}

	public boolean HomeTeleport(Player player)
	{
		if(Costs_Home == 0)
		{
			return true;
		}
		EconomyResponse response = econ.withdrawPlayer(player.getName(), Costs_Home);
		if(response.transactionSuccess()) 
		{
			player.sendMessage(String.format(Message_HomeTPPaid, econ.format(response.amount), econ.getBalance(player.getName())));
			return true;
		} 
		else 
		{
			player.sendMessage(String.format(Message_NotEnough, econ.format(Costs_Home)));
			return false;
		}
	}
	
	public boolean SetHome(Player player)
	{
		if(Costs_SetHome == 0)
		{
			return true;
		}
		EconomyResponse response = econ.withdrawPlayer(player.getName(), Costs_SetHome);
		if(response.transactionSuccess()) 
		{
			player.sendMessage(String.format(Message_SetHomePaid, econ.format(response.amount), econ.getBalance(player.getName())));
			return true;
		} 
		else 
		{
			player.sendMessage(String.format(Message_NotEnough, econ.format(Costs_SetHome)));
			return false;
		}
	}
	
	public boolean Gift(Player player)
	{
		if(Costs_Gift == 0)
		{
			return true;
		}
		EconomyResponse response = econ.withdrawPlayer(player.getName(), Costs_Gift);
		if(response.transactionSuccess()) 
		{
			player.sendMessage(String.format(Message_GiftPaid, econ.format(response.amount), econ.getBalance(player.getName())));
			return true;
		} 
		else 
		{
			player.sendMessage(String.format(Message_NotEnough, econ.format(Costs_Gift)));
			return false;
		}
	}
	
	public boolean Teleport(Player player)
	{
		if(Costs_TP == 0)
		{
			return true;
		}
		EconomyResponse response = econ.withdrawPlayer(player.getName(), Costs_TP);
		if(response.transactionSuccess()) 
		{
			player.sendMessage(String.format(Message_TPPaid, econ.format(response.amount), econ.getBalance(player.getName())));
			return true;
		} 
		else 
		{
			player.sendMessage(String.format(Message_NotEnough, econ.format(Costs_TP)));
			return false;
		}
	}
	
	public boolean Divorce(CommandSender priest, Player player, Player otherPlayer)
	{
		if(Costs_Divorce == 0)
		{
			return true;
		}
		EconomyResponse response = econ.withdrawPlayer(player.getName(), Costs_Divorce/2);
		EconomyResponse response2 = econ.withdrawPlayer(otherPlayer.getName(), Costs_Divorce/2);
		if(response.transactionSuccess() && response2.transactionSuccess()) 
		{
			otherPlayer.sendMessage(String.format(Message_DivorcePaid, econ.format(response.amount), econ.getBalance(otherPlayer.getName())));
			player.sendMessage(String.format(Message_DivorcePaid, econ.format(response.amount), econ.getBalance(player.getName())));
			return true;
		} 
		else 
		{
			if(priest != null)
			{
				priest.sendMessage(Message_DivNotEnoPriestI);
			}
			if(response.transactionSuccess())
			{
				econ.depositPlayer(player.getName(), Costs_Divorce/2);
				otherPlayer.sendMessage(String.format(Message_NotEnough, econ.format(Costs_Divorce/2)));
				player.sendMessage(Message_PartnerNotEnough);
				return false;
			}
			if(response2.transactionSuccess())
			{
				econ.depositPlayer(otherPlayer.getName(), Costs_Divorce/2);
				player.sendMessage(String.format(Message_NotEnough, econ.format(Costs_Divorce/2)));
				otherPlayer.sendMessage(Message_PartnerNotEnough);
				return false;
			}
			otherPlayer.sendMessage(String.format(Message_NotEnough, econ.format(Costs_Divorce/2)));
			player.sendMessage(String.format(Message_NotEnough, econ.format(Costs_Divorce/2)));
			return false;
		}
	}
	
	public boolean Marry(CommandSender priest, Player player, Player otherPlayer)
	{
		if(Costs_Marry == 0)
		{
			return true;
		}
		EconomyResponse response = econ.withdrawPlayer(player.getName(), Costs_Marry/2);
		EconomyResponse response2 = econ.withdrawPlayer(otherPlayer.getName(), Costs_Marry/2);
		if(response.transactionSuccess() && response2.transactionSuccess()) 
		{
			otherPlayer.sendMessage(String.format(Message_MarriagePaid, econ.format(response.amount), econ.getBalance(otherPlayer.getName())));
			player.sendMessage(String.format(Message_MarriagePaid, econ.format(response.amount), econ.getBalance(player.getName())));
			return true;
		} 
		else 
		{
			if(priest != null)
			{
				priest.sendMessage(Message_NotEnoughPriestInfo);
			}
			if(response.transactionSuccess())
			{
				econ.depositPlayer(player.getName(), Costs_Marry/2);
				otherPlayer.sendMessage(String.format(Message_NotEnough, econ.format(Costs_Marry/2)));
				player.sendMessage(Message_PartnerNotEnough);
				return false;
			}
			if(response2.transactionSuccess())
			{
				econ.depositPlayer(otherPlayer.getName(), Costs_Marry/2);
				player.sendMessage(String.format(Message_NotEnough, econ.format(Costs_Marry/2)));
				otherPlayer.sendMessage(Message_PartnerNotEnough);
				return false;
			}
			otherPlayer.sendMessage(String.format(Message_NotEnough, econ.format(Costs_Marry/2)));
			player.sendMessage(String.format(Message_NotEnough, econ.format(Costs_Marry/2)));
			return false;
		}
	}
}
