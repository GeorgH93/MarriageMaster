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

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import at.pcgamingfreaks.georgh.MarriageMaster.MarriageMaster;
import at.pcgamingfreaks.georgh.MarriageMaster.Marry_Requests;

public class Priest
{
	private MarriageMaster marriageMaster;
	
	public Priest(MarriageMaster marriagemaster) 
	{
		marriageMaster = marriagemaster;
	}
	
	public void Marry(CommandSender priest, String[] args)
	{
		Player player = Bukkit.getServer().getPlayer(args[0]);
		if(player == null || (player != null && !player.isOnline()))
		{
			priest.sendMessage(ChatColor.RED + String.format(marriageMaster.lang.Get("Ingame.PlayerNotOn"), args[0]));
			return;
		}
		Player otherPlayer = Bukkit.getServer().getPlayer(args[1]);
		if(otherPlayer == null || (otherPlayer != null && !otherPlayer.isOnline()))
		{
			priest.sendMessage(ChatColor.RED + String.format(marriageMaster.lang.Get("Ingame.PlayerNotOn"), args[1]));
			return;
		}
		if(player == priest || otherPlayer == priest)
		{
			priest.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Priest.NotYourSelf"));
			return;
		}
		if(player.getName().equalsIgnoreCase(otherPlayer.getName()))
		{
			priest.sendMessage(ChatColor.RED + String.format(marriageMaster.lang.Get("Priest.NotWithHimself"),player.getDisplayName()+ChatColor.RED));
			return;
		}
		String a1 = marriageMaster.DB.GetPartner(player);
		String a2 = marriageMaster.DB.GetPartner(otherPlayer);
		if((a1 != null && !a1.isEmpty()) || (a2 != null && !a2.isEmpty()))
		{
			priest.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Priest.AlreadyMarried"));
			return;
		}
		if(!(marriageMaster.config.UseEconomy() && !marriageMaster.economy.Marry(priest, player, otherPlayer, marriageMaster.config.GetEconomyMarry())))
		{
			marriageMaster.DB.MarryPlayers(player, otherPlayer, "Console", (args.length == 3) ? args[2] : null);
			priest.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.Married"), player.getDisplayName()+ChatColor.GREEN, otherPlayer.getDisplayName()+ChatColor.GREEN));
			player.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.HasMarried"), "Console", otherPlayer.getDisplayName()+ChatColor.GREEN));
			otherPlayer.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.HasMarried"), "Console", player.getDisplayName()+ChatColor.GREEN));
			if(marriageMaster.config.GetAnnouncementEnabled())
			{
				marriageMaster.getServer().broadcastMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.BroadcastMarriage"), "Console", player.getDisplayName()+ChatColor.GREEN, otherPlayer.getDisplayName()+ChatColor.GREEN));
			}
		}
	}

	public void Marry(Player priest, String[] args) 
	{
		Player player = Bukkit.getServer().getPlayer(args[0]);
		if(player == null || (player != null && !player.isOnline()))
		{
			priest.sendMessage(ChatColor.RED + String.format(marriageMaster.lang.Get("Ingame.PlayerNotOn"), args[0]));
			return;
		}
		Player otherPlayer = Bukkit.getServer().getPlayer(args[1]);
		if(otherPlayer == null || (otherPlayer != null && !otherPlayer.isOnline()))
		{
			priest.sendMessage(ChatColor.RED + String.format(marriageMaster.lang.Get("Ingame.PlayerNotOn"), args[1]));
			return;
		}
		if(player.getName().equalsIgnoreCase(otherPlayer.getName()))
		{
			priest.sendMessage(ChatColor.RED + String.format(marriageMaster.lang.Get("Priest.NotWithHimself"),player.getDisplayName()+ChatColor.RED));
		}
		else
		{
			if(InRadius(player, otherPlayer, priest))
			{
				String a1 = marriageMaster.DB.GetPartner(player);
				String a2 = marriageMaster.DB.GetPartner(otherPlayer);
				if((a1 != null && !a1.isEmpty()) || (a2 != null && !a2.isEmpty()))
				{
					priest.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Priest.AlreadyMarried"));
					return;
				}
				MarryPlayer(priest, player, otherPlayer, (args.length == 3 ) ? args[2]: null);
			}
			else
			{
				priest.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Priest.NotInRange"));
			}
		}
	}
	
	public void SelfMarry(Player player, String otP)
	{
		SelfMarry(player, otP, null);
	}
	
	public void SelfMarry(Player player, String otP, String Surname)
	{
		Player otherPlayer = Bukkit.getServer().getPlayer(otP);
		if(player == null || (player != null && !player.isOnline()))
		{
			player.sendMessage(ChatColor.RED + String.format(marriageMaster.lang.Get("Ingame.PlayerNotOn"), otP));
			return;
		}
		if(player.equals(otherPlayer))
		{
			player.sendMessage(ChatColor.RED + String.format(marriageMaster.lang.Get("Ingame.NotYourSelf"), otP));
			return;
		}
		if(marriageMaster.InRadius(player, otherPlayer, marriageMaster.config.GetRange("Marry")))
		{
			if(!marriageMaster.HasPartner(player))
			{
				if(!marriageMaster.HasPartner(otherPlayer))
				{
					if(!HasOpenRequest(player))
					{
						if(!HasOpenRequest(otherPlayer))
						{
							marriageMaster.mr.add(new Marry_Requests(null, player, otherPlayer, Surname));
							player.sendMessage(ChatColor.GREEN + marriageMaster.lang.Get("Ingame.MarryRequestSent"));
							otherPlayer.sendMessage(String.format(marriageMaster.lang.Get("Ingame.MarryConfirm"),player.getDisplayName()+ChatColor.WHITE));
						}
						else
						{
							player.sendMessage(ChatColor.RED + String.format(marriageMaster.lang.Get("Priest.AlreadyOpenRequest"),otherPlayer.getDisplayName()+ChatColor.RED));
						}
					}
					else
					{
						player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.AlreadyOpenRequest"));
					}
				}
				else
				{
					player.sendMessage(ChatColor.RED + String.format(marriageMaster.lang.Get("Ingame.OtherAlreadyMarried"),otherPlayer.getDisplayName()+ChatColor.RED));
				}
			}
			else
			{
				player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.AlreadyMarried"));
			}
		}
		else
		{
			player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Priest.NotInRange"));
		}
	}
	
	private boolean HasOpenRequest(Player player)
	{
		for(Marry_Requests m:marriageMaster.mr)
		{
			if(m.p1 == player || m.p2 == player)
			{
				return true;
			}
		}
		return false;
	}
	
	private void SelfMarryAccept(Marry_Requests m)
	{
		if(marriageMaster.config.UseEconomy() && !marriageMaster.economy.Marry(null, m.p1, m.p2, marriageMaster.config.GetEconomyMarry()))
		{
			return;
		}
		marriageMaster.DB.MarryPlayers(m.p1, m.p2, "none", m.surname);
		m.p1.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Ingame.HasMarried"), m.p2.getDisplayName()+ChatColor.GREEN));
		m.p2.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Ingame.HasMarried"), m.p1.getDisplayName()+ChatColor.GREEN));
		if(marriageMaster.config.GetAnnouncementEnabled())
		{
			marriageMaster.getServer().broadcastMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Ingame.BroadcastMarriage"), m.p1.getDisplayName()+ChatColor.GREEN, m.p2.getDisplayName()+ChatColor.GREEN));
		}
	}
	
	public void AcceptMarriage(Player player)
	{
		for(Map.Entry<Player, Player> entry : marriageMaster.dr.entrySet())
		{
			if(entry.getKey().equals(player))
			{
				SaveDivorce(entry.getKey(), entry.getValue());
				marriageMaster.dr.remove(player);
				return;
			}
		}
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
    					SaveMarry(m.priest, m.p1, m.p2, m.surname);
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
	
	private void SaveMarry(Player priest, Player player, Player otherPlayer, String surname)
	{
		if(!(marriageMaster.config.UseEconomy() && !marriageMaster.economy.Marry(priest, player, otherPlayer, marriageMaster.config.GetEconomyMarry())))
		{
			marriageMaster.DB.MarryPlayers(player, otherPlayer, priest, surname);
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
	}

	private void MarryPlayer(Player priest, Player player, Player otherPlayer, String surname) 
	{
		if(!marriageMaster.config.UseConfirmation())
		{
			SaveMarry(priest, player, otherPlayer, surname);
		}
		else
		{
			if(HasOpenRequest(player))
			{
				priest.sendMessage(String.format(ChatColor.RED + marriageMaster.lang.Get("Priest.AlreadyOpenRequest"), player.getDisplayName() + ChatColor.RED));
			}
			else if(HasOpenRequest(otherPlayer))
			{
				priest.sendMessage(String.format(ChatColor.RED + marriageMaster.lang.Get("Priest.AlreadyOpenRequest"), otherPlayer.getDisplayName() + ChatColor.RED));
			}
			else
			{
				marriageMaster.mr.add(new Marry_Requests(priest, player, otherPlayer, surname));
				if(marriageMaster.config.UseConfirmationAutoDialog())
				{
					priest.chat(String.format(marriageMaster.lang.Get("Dialog.DoYouWant"), player.getName(), otherPlayer.getName()));
				}
				player.sendMessage(marriageMaster.lang.Get("Priest.Confirm"));
			}
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
			if(marriageMaster.IsPriest(player))
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
			if(marriageMaster.IsPriest(player))
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
	
	public void Divorce(Player priest, String[] args)
	{
		Player player = Bukkit.getServer().getPlayer(args[1]);
		if(player == null || (player != null && !player.isOnline()))
		{
			priest.sendMessage(ChatColor.RED + String.format(marriageMaster.lang.Get("Ingame.PlayerNotOn"), args[1]));
			return;
		}
		String otP = marriageMaster.DB.GetPartner(player);
		if(otP == null || otP.isEmpty())
		{
			priest.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Priest.PlayerNotMarried"));
			return;
		}
		Player otherPlayer = Bukkit.getServer().getPlayer(otP);
		if(otherPlayer == null || (otherPlayer != null && !otherPlayer.isOnline()))
		{
			if(marriageMaster.config.CheckPerm(priest, "marry.offlinedivorce", false))
			{
				marriageMaster.DB.DivorcePlayer(player);
				priest.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.Divorced"), player.getDisplayName()+ChatColor.GREEN,otP));
				player.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.DivorcedPlayer"), priest.getDisplayName()+ChatColor.GREEN, otP));
			}
			else
			{
				priest.sendMessage(ChatColor.RED + String.format(marriageMaster.lang.Get("Priest.PartnerOffline"), args[1],otP));
				return;
			}
		}
		if(InRadius(player, otherPlayer, priest))
		{ 
			if(!marriageMaster.config.UseConfirmation())
			{
				SaveDivorce(player, priest);
			}
			else
			{
				if(marriageMaster.dr.containsKey(player))
				{
					priest.sendMessage(String.format(ChatColor.RED + marriageMaster.lang.Get("Priest.AlreadyOpenRequest"), player.getDisplayName() + ChatColor.RED));
				}
				else
				{
					marriageMaster.dr.put(player, priest);
					player.sendMessage(marriageMaster.lang.Get("Priest.DivorceConfirm"));
				}
			}
		}
		else
		{
			priest.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Priest.NotInRange"));
		}
	}
	
	public void Divorce(CommandSender priest, String[] args)
	{
		Player player = Bukkit.getServer().getPlayer(args[1]);
		if(player == null || (player != null && !player.isOnline()))
		{
			priest.sendMessage(ChatColor.RED + String.format(marriageMaster.lang.Get("Ingame.PlayerNotOn"), args[1]));
			return;
		}
		String otP = marriageMaster.DB.GetPartner(player);
		if(otP == null || otP.isEmpty())
		{
			priest.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Priest.PlayerNotMarried"));
			return;
		}
		marriageMaster.DB.DivorcePlayer(player);
		priest.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.Divorced"), player.getName(),otP));
		player.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.DivorcedPlayer"), ChatColor.GRAY+"Console"+ChatColor.GRAY, otP));
		Player otherPlayer = Bukkit.getServer().getPlayer(otP);
		if(otherPlayer != null && otherPlayer.isOnline())
		{
			otherPlayer.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.DivorcedPlayer"), ChatColor.GRAY+"Console"+ChatColor.GRAY, player.getName()));
		}
	}
	
	private void SaveDivorce(Player player, Player priest)
	{
		String otP = marriageMaster.DB.GetPartner(player);
		if(otP == null || otP.isEmpty())
		{
			priest.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Priest.PlayerNotMarried"));
			return;
		}
		Player otherPlayer = Bukkit.getServer().getPlayer(otP);
		if(otherPlayer == null || !otherPlayer.isOnline())
		{
			priest.sendMessage(ChatColor.RED + String.format(marriageMaster.lang.Get("Priest.PartnerOffline"), player.getName(), otP));
			return;
		}
		if(!marriageMaster.config.UseEconomy() || marriageMaster.economy.Divorce(priest, player, otherPlayer, marriageMaster.config.GetEconomyDivorce()))
		{
			marriageMaster.DB.DivorcePlayer(player);
			priest.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.Divorced"), player.getDisplayName()+ChatColor.GREEN, otherPlayer.getDisplayName()+ChatColor.GREEN));
			player.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.DivorcedPlayer"), priest.getDisplayName()+ChatColor.GREEN, otherPlayer.getDisplayName()+ChatColor.GREEN));
			otherPlayer.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Priest.DivorcedPlayer"), priest.getDisplayName()+ChatColor.GREEN, player.getDisplayName()+ChatColor.GREEN));
		}
	}

	public void SelfDivorce(Player player)
	{
		String partner = marriageMaster.DB.GetPartner(player);
		if(partner == null || partner.isEmpty())
		{
			player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NotMarried"));
			return;
		}
		Player otherPlayer = Bukkit.getServer().getPlayer(partner);
		if(otherPlayer == null || !otherPlayer.isOnline())
		{
			if(marriageMaster.config.CheckPerm(player, "marry.offlinedivorce", false))
			{
				marriageMaster.DB.DivorcePlayer(player);
				player.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Ingame.Divorced"), player.getDisplayName()+ChatColor.GREEN,partner));
			}
			else
			{
				player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.PartnerOffline"));
			}
		}
		else
		{
			if(marriageMaster.config.UseEconomy() && !marriageMaster.economy.Divorce(null ,player, otherPlayer, marriageMaster.config.GetEconomyDivorce()))
			{
				return;
			}
			else
			{
				marriageMaster.DB.DivorcePlayer(player);
				player.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Ingame.Divorced"), otherPlayer.getDisplayName()+ChatColor.GREEN));
				otherPlayer.sendMessage(ChatColor.GREEN + String.format(marriageMaster.lang.Get("Ingame.DivorcedPlayer"), player.getDisplayName()+ChatColor.GREEN));
			}
		}
	}
	
	public void SetSurname(CommandSender sender, String p, String surname)
	{
		Player player = Bukkit.getPlayer(p);
		if(player != null && player.isOnline())
		{
			if(marriageMaster.HasPartner(player))
			{
				marriageMaster.DB.SetSurname(player, surname);
				sender.sendMessage(ChatColor.GREEN + marriageMaster.lang.Get("Priest.SurnameSet"));
			}
			else
			{
				sender.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NotMarried"));
			}
		}
		else
		{
			sender.sendMessage(ChatColor.RED + String.format(marriageMaster.lang.Get("Ingame.PlayerNotOn"), p));
		}
	}
}