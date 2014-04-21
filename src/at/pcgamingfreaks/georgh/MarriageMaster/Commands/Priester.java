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

package at.pcgamingfreaks.georgh.MarriageMaster.Commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import at.pcgamingfreaks.georgh.MarriageMaster.MarriageMaster;
import at.pcgamingfreaks.georgh.MarriageMaster.Marry_Requests;

public class Priester 
{
	private MarriageMaster marriageMaster;
	
	public Priester(MarriageMaster marriagemaster) 
	{
		marriageMaster = marriagemaster;
	}
	
	public void Marry(CommandSender priester, String[] args)
	{
		Player player = Bukkit.getServer().getPlayer(args[0]);
		if(player == null || (player != null && !player.isOnline()))
		{
			priester.sendMessage(ChatColor.RED + String.format(marriageMaster.lang.Get("Ingame.PlayerNotOn"), args[0]));
			return;
		}
		Player otherPlayer = Bukkit.getServer().getPlayer(args[1]);
		if(otherPlayer == null || (otherPlayer != null && !otherPlayer.isOnline()))
		{
			priester.sendMessage(ChatColor.RED + String.format(marriageMaster.lang.Get("Ingame.PlayerNotOn"), args[1]));
			return;
		}
		if(player == priester || otherPlayer == priester)
		{
			priester.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Priest.NotYourSelf"));
			return;
		}
		if(player.getName().equalsIgnoreCase(otherPlayer.getName()))
		{
			priester.sendMessage(ChatColor.RED + String.format(marriageMaster.lang.Get("Priest.NotWithHimself"),player.getDisplayName()+ChatColor.RED));
			return;
		}
		String a1 = marriageMaster.DB.GetPartner(player);
		String a2 = marriageMaster.DB.GetPartner(otherPlayer);
		if((a1 != null && !a1.isEmpty()) || (a2 != null && !a2.isEmpty()))
		{
			priester.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Priest.AlreadyMarried"));
			return;
		}
		if(marriageMaster.config.UseEconomy() && !marriageMaster.economy.Marry(player, otherPlayer, marriageMaster.config.GetEconomyMarry()))
		{
			return;
		}
		else
		{
			marriageMaster.DB.MarryPlayers(player, otherPlayer, "Console");
			priester.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.Married"), player.getDisplayName()+ChatColor.GREEN, otherPlayer.getDisplayName()+ChatColor.GREEN));
			player.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.HasMarried"), "Console", otherPlayer.getDisplayName()+ChatColor.GREEN));
			otherPlayer.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.HasMarried"), "Console", player.getDisplayName()+ChatColor.GREEN));
			if(marriageMaster.config.GetAnnouncementEnabled())
			{
				marriageMaster.getServer().broadcastMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.BroadcastMarriage"), "Console", player.getDisplayName()+ChatColor.GREEN, otherPlayer.getDisplayName()+ChatColor.GREEN));
			}
		}
	}

	public void Marry(Player priester, String[] args) 
	{
		Player player = Bukkit.getServer().getPlayer(args[0]);
		if(player == null || (player != null && !player.isOnline()))
		{
			priester.sendMessage(ChatColor.RED + String.format(marriageMaster.lang.Get("Ingame.PlayerNotOn"), args[0]));
			return;
		}
		Player otherPlayer = Bukkit.getServer().getPlayer(args[1]);
		if(otherPlayer == null || (otherPlayer != null && !otherPlayer.isOnline()))
		{
			priester.sendMessage(ChatColor.RED + String.format(marriageMaster.lang.Get("Ingame.PlayerNotOn"), args[1]));
			return;
		}
		if(player.getName().equalsIgnoreCase(otherPlayer.getName()))
		{
			priester.sendMessage(ChatColor.RED + String.format(marriageMaster.lang.Get("Priest.NotWithHimself"),player.getDisplayName()+ChatColor.RED));
		}
		else
		{
			if(InRadius(player, otherPlayer, priester))
			{
				String a1 = marriageMaster.DB.GetPartner(player);
				String a2 = marriageMaster.DB.GetPartner(otherPlayer);
				if((a1 != null && !a1.isEmpty()) || (a2 != null && !a2.isEmpty()))
				{
					priester.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Priest.AlreadyMarried"));
					return;
				}
				if(marriageMaster.config.UseEconomy() && !marriageMaster.economy.Marry(player, otherPlayer, marriageMaster.config.GetEconomyMarry()))
				{
					return;
				}
				else
				{
					MarryPlayer(priester, player, otherPlayer);
				}
			}
			else
			{
				priester.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Priest.NotInRange"));
			}
		}
	}
	
	public void SelfMarry(Player player, String otP)
	{
		Player otherPlayer = Bukkit.getServer().getPlayer(otP);
		if(player == null || (player != null && !player.isOnline()))
		{
			player.sendMessage(ChatColor.RED + String.format(marriageMaster.lang.Get("Ingame.PlayerNotOn"), otP));
			return;
		}
		if(marriageMaster.InRadius(player, otherPlayer, marriageMaster.config.GetRange("Marry")))
		{
			if(!marriageMaster.HasPartner(player) && !marriageMaster.HasPartner(otherPlayer))
			{
				marriageMaster.mr.add(new Marry_Requests(null, player, otherPlayer));
				otherPlayer.sendMessage(String.format(marriageMaster.lang.Get("Ingame.MarryConfirm"),player.getDisplayName()+ChatColor.WHITE));
			}
			else
			{
				player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.AlreadyMarried"));
				return;
			}
		}
		else
		{
			player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Priest.NotInRange"));
		}
	}
	
	private void SelfMarryAccept(Marry_Requests m)
	{
		if(marriageMaster.config.UseEconomy() && !marriageMaster.economy.Marry(m.p1, m.p2, marriageMaster.config.GetEconomyMarry()))
		{
			return;
		}
		marriageMaster.DB.MarryPlayers(m.p1, m.p2, "none");
		m.p1.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Ingame.HasMarried"), m.p2.getDisplayName()+ChatColor.GREEN));
		m.p2.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Ingame.HasMarried"), m.p1.getDisplayName()+ChatColor.GREEN));
		if(marriageMaster.config.GetAnnouncementEnabled())
		{
			marriageMaster.getServer().broadcastMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Ingame.BroadcastMarriage"), m.p1.getDisplayName()+ChatColor.GREEN, m.p2.getDisplayName()+ChatColor.GREEN));
		}
	}
	
	public void AcceptMarriage(Player player)
	{
		for (Marry_Requests m : marriageMaster.mr)
		{
    		if(m.p1 == player || m.p2 == player)
    		{
    			if(m.priest == null)
    			{
    				if(m.p2 == player)
    				{
    					marriageMaster.mr.remove(m);
    					SelfMarryAccept(m);
    				}
    				return;
    			}
    			if(!m.HasAccepted(player))
    			{
    				marriageMaster.mr.remove(m);
    				m.Accept(player);
    				if(marriageMaster.config.UseConfirmation() && marriageMaster.config.UseConfirmationAutoDialog())
					{
    					player.chat(marriageMaster.lang.Get("Dialog.YesIWant"));
					}
    				if(m.BothAcceoted(player))
    				{
    					SaveMarry(m.priest, m.p1, m.p2);
    				}
    				else
    				{
    					if(marriageMaster.config.UseConfirmation() && marriageMaster.config.UseConfirmationAutoDialog())
    					{
    						m.priest.chat(String.format(marriageMaster.lang.Get("Dialog.AndDoYouWant"), m.p2.getName(), m.p1.getName()));
    					}
    					m.p2.sendMessage(marriageMaster.lang.Get("Priest.Confirm"));
    					marriageMaster.mr.add(m);
    				}
    			}
    			else
    			{
    				player.sendMessage(marriageMaster.lang.Get("Priest.AlreadyAccepted"));
    			}
    			return;
    		}
    	}
    	player.sendMessage(marriageMaster.lang.Get("Priest.NoRequest"));
	}
	
	private void SaveMarry(Player priest, Player player, Player otherPlayer)
	{
		marriageMaster.DB.MarryPlayers(player, otherPlayer, priest);
		priest.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.Married"), player.getDisplayName()+ChatColor.GREEN, otherPlayer.getDisplayName()+ChatColor.GREEN));
		player.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.HasMarried"), priest.getDisplayName()+ChatColor.GREEN, otherPlayer.getDisplayName()+ChatColor.GREEN));
		otherPlayer.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.HasMarried"), priest.getDisplayName()+ChatColor.GREEN, player.getDisplayName()+ChatColor.GREEN));
		if(marriageMaster.config.UseConfirmation() && marriageMaster.config.UseConfirmationAutoDialog())
		{
			priest.chat(marriageMaster.lang.Get("Dialog.Married"));
		}
		if(marriageMaster.config.GetAnnouncementEnabled())
		{
			marriageMaster.getServer().broadcastMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.BroadcastMarriage"), priest.getDisplayName()+ChatColor.GREEN, player.getDisplayName()+ChatColor.GREEN, otherPlayer.getDisplayName()+ChatColor.GREEN));
		}
	}

	private void MarryPlayer(Player priest, Player player, Player otherPlayer) 
	{
		if(!marriageMaster.config.UseConfirmation())
		{
			SaveMarry(priest, player, otherPlayer);
		}
		else
		{
			marriageMaster.mr.add(new Marry_Requests(priest, player, otherPlayer));
			if(marriageMaster.config.UseConfirmationAutoDialog())
			{
				priest.chat(String.format(marriageMaster.lang.Get("Dialog.DoYouWant"), player.getName(), otherPlayer.getName()));
			}
			player.sendMessage(marriageMaster.lang.Get("Priest.Confirm"));
		}
	}

	private boolean InRadius(Player player, Player otherPlayer, Player priest) 
	{
		return marriageMaster.InRadius(player, priest, marriageMaster.config.GetRange("Marry")) && marriageMaster.InRadius(otherPlayer, priest, marriageMaster.config.GetRange("Marry"));
	}
	
	public void setPriest(String[] args, CommandSender sender)
	{
		Player player = marriageMaster.getServer().getPlayer(args[1]);
		if(player != null && player.isOnline())
		{
			if(marriageMaster.IsPriester(player))
			{
				marriageMaster.DB.DelPriest(player);
				player.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.UnMadeYouAPriest"), sender.getName()));
				sender.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.UnMadeAPriest"), player.getDisplayName()+ChatColor.GREEN));
			}
			else
			{
				marriageMaster.DB.SetPriest(player);
				player.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.MadeYouAPriest"), sender.getName()));
				sender.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.MadeAPriest"), player.getDisplayName()+ChatColor.GREEN));
			}
		}
		else
		{
			sender.sendMessage(ChatColor.RED + String.format(marriageMaster.lang.Get("Ingame.PlayerNotOn"), args[1]));
		}
	}

	public void setPriest(String[] args, Player sender) 
	{
		Player player = marriageMaster.getServer().getPlayer(args[1]);
		if(player != null && player.isOnline())
		{
			if(marriageMaster.IsPriester(player))
			{
				marriageMaster.DB.DelPriest(player);
				player.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.UnMadeYouAPriest"), sender.getDisplayName()+ChatColor.GREEN));
				sender.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.UnMadeAPriest"), player.getDisplayName()+ChatColor.GREEN));
			}
			else
			{
				marriageMaster.DB.SetPriest(player);
				player.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.MadeYouAPriest"), sender.getDisplayName()+ChatColor.GREEN));
				sender.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.MadeAPriest"), player.getDisplayName()+ChatColor.GREEN));
			}
		}
		else
		{
			sender.sendMessage(ChatColor.RED + String.format(marriageMaster.lang.Get("Ingame.PlayerNotOn"), args[1]));
		}
	}
	
	public void Divorce(Player priester, String[] args)
	{
		Player player = Bukkit.getServer().getPlayer(args[1]);
		if(player == null || (player != null && !player.isOnline()))
		{
			priester.sendMessage(ChatColor.RED + String.format(marriageMaster.lang.Get("Ingame.PlayerNotOn"), args[1]));
			return;
		}
		String otP = marriageMaster.DB.GetPartner(player);
		if(otP == null || otP.isEmpty())
		{
			priester.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Priest.PlayerNotMarried"));
			return;
		}
		Player otherPlayer = Bukkit.getServer().getPlayer(otP);
		if(otherPlayer == null || (otherPlayer != null && !otherPlayer.isOnline()))
		{
			if(marriageMaster.config.CheckPerm(priester, "marry.offlinedivorce", false))
			{
				marriageMaster.DB.DivorcePlayer(player);
				priester.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.Divorced"), player.getDisplayName()+ChatColor.GREEN,otP));
				player.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.DivorcedPlayer"), priester.getDisplayName()+ChatColor.GREEN, otP));
			}
			else
			{
				priester.sendMessage(ChatColor.RED + String.format(marriageMaster.lang.Get("Priest.PartnerOffline"), args[1],otP));
				return;
			}
		}
		if(InRadius(player, otherPlayer, priester))
		{ 
			if(marriageMaster.config.UseEconomy() && !marriageMaster.economy.Divorce(player, otherPlayer, marriageMaster.config.GetEconomyDivorce()))
			{
				return;
			}
			else
			{
				marriageMaster.DB.DivorcePlayer(player);
				priester.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.Divorced"), player.getDisplayName()+ChatColor.GREEN,otherPlayer.getDisplayName()+ChatColor.GREEN));
				player.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.DivorcedPlayer"), priester.getDisplayName()+ChatColor.GREEN, otherPlayer.getDisplayName()+ChatColor.GREEN));
				otherPlayer.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.DivorcedPlayer"), priester.getDisplayName()+ChatColor.GREEN, player.getDisplayName()+ChatColor.GREEN));
			}
		}
		else
		{
			priester.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Priest.NotInRange"));
		}
	}
	
	public void Divorce(CommandSender priester, String[] args)
	{
		Player player = Bukkit.getServer().getPlayer(args[1]);
		if(player == null || (player != null && !player.isOnline()))
		{
			priester.sendMessage(ChatColor.RED + String.format(marriageMaster.lang.Get("Ingame.PlayerNotOn"), args[1]));
			return;
		}
		String otP = marriageMaster.DB.GetPartner(player);
		if(otP == null || otP.isEmpty())
		{
			priester.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Priest.PlayerNotMarried"));
			return;
		}
		marriageMaster.DB.DivorcePlayer(player);
		priester.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.Divorced"), player.getName(),otP));
		player.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.DivorcedPlayer"), ChatColor.GRAY+"Console"+ChatColor.GRAY, otP));
		Player otherPlayer = Bukkit.getServer().getPlayer(otP);
		if(otherPlayer != null && otherPlayer.isOnline())
		{
			otherPlayer.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.DivorcedPlayer"), ChatColor.GRAY+"Console"+ChatColor.GRAY, player.getName()));
		}
	}
}