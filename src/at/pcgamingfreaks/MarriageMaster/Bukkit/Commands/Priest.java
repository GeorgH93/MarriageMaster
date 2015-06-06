/*
 *   Copyright (C) 2014-2015 GeorgH93
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

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Marry_Requests;

public class Priest
{
	private MarriageMaster plugin;
	
	public Priest(MarriageMaster marriagemaster) 
	{
		plugin = marriagemaster;
	}
	
	@SuppressWarnings("deprecation")
	public void Marry(CommandSender priest, String[] args)
	{
		Player player = Bukkit.getServer().getPlayer(args[0]);
		if(player == null || (player != null && !player.isOnline()))
		{
			priest.sendMessage(ChatColor.RED + String.format(plugin.lang.Get("Ingame.PlayerNotOn"), args[0]));
			return;
		}
		Player otherPlayer = Bukkit.getServer().getPlayer(args[1]);
		if(otherPlayer == null || (otherPlayer != null && !otherPlayer.isOnline()))
		{
			priest.sendMessage(ChatColor.RED + String.format(plugin.lang.Get("Ingame.PlayerNotOn"), args[1]));
			return;
		}
		if(player == priest || otherPlayer == priest)
		{
			priest.sendMessage(ChatColor.RED + plugin.lang.Get("Priest.NotYourSelf"));
			return;
		}
		if(player.getName().equalsIgnoreCase(otherPlayer.getName()))
		{
			priest.sendMessage(ChatColor.RED + String.format(plugin.lang.Get("Priest.NotWithHimself"),player.getDisplayName()+ChatColor.RED));
			return;
		}
		String a1 = plugin.DB.GetPartner(player);
		String a2 = plugin.DB.GetPartner(otherPlayer);
		if((a1 != null && !a1.isEmpty()) || (a2 != null && !a2.isEmpty()))
		{
			priest.sendMessage(ChatColor.RED + plugin.lang.Get("Priest.AlreadyMarried"));
			return;
		}
		if(plugin.economy == null || plugin.economy.Marry(priest, player, otherPlayer))
		{
			plugin.DB.MarryPlayers(player, otherPlayer, "Console", (args.length == 3) ? args[2] : null);
			priest.sendMessage(ChatColor.GREEN + String.format(plugin.lang.Get("Priest.Married"), player.getDisplayName() + ChatColor.GREEN, otherPlayer.getDisplayName() + ChatColor.GREEN));
			player.sendMessage(ChatColor.GREEN + String.format(plugin.lang.Get("Priest.HasMarried"), ChatColor.GRAY + "Console" + ChatColor.GREEN, otherPlayer.getDisplayName() + ChatColor.GREEN));
			otherPlayer.sendMessage(ChatColor.GREEN + String.format(plugin.lang.Get("Priest.HasMarried"), ChatColor.GRAY + "Console" + ChatColor.GREEN, player.getDisplayName() + ChatColor.GREEN));
			if(plugin.config.GetAnnouncementEnabled())
			{
				plugin.getServer().broadcastMessage(ChatColor.GREEN + String.format(plugin.lang.Get("Priest.BroadcastMarriage"), ChatColor.GRAY + "Console" + ChatColor.GREEN, player.getDisplayName() + ChatColor.GREEN, otherPlayer.getDisplayName() + ChatColor.GREEN));
			}
		}
	}

	@SuppressWarnings("deprecation")
	public void Marry(Player priest, String[] args) 
	{
		Player player = Bukkit.getServer().getPlayer(args[0]);
		if(player == null || (player != null && !player.isOnline()))
		{
			priest.sendMessage(ChatColor.RED + String.format(plugin.lang.Get("Ingame.PlayerNotOn"), args[0]));
			return;
		}
		Player otherPlayer = Bukkit.getServer().getPlayer(args[1]);
		if(otherPlayer == null || (otherPlayer != null && !otherPlayer.isOnline()))
		{
			priest.sendMessage(ChatColor.RED + String.format(plugin.lang.Get("Ingame.PlayerNotOn"), args[1]));
			return;
		}
		if(player.getName().equalsIgnoreCase(otherPlayer.getName()))
		{
			priest.sendMessage(ChatColor.RED + String.format(plugin.lang.Get("Priest.NotWithHimself"),player.getDisplayName()+ChatColor.RED));
		}
		else
		{
			if(InRadius(player, otherPlayer, priest))
			{
				String a1 = plugin.DB.GetPartner(player);
				String a2 = plugin.DB.GetPartner(otherPlayer);
				if((a1 != null && !a1.isEmpty()) || (a2 != null && !a2.isEmpty()))
				{
					priest.sendMessage(ChatColor.RED + plugin.lang.Get("Priest.AlreadyMarried"));
					return;
				}
				MarryPlayer(priest, player, otherPlayer, (args.length == 3 ) ? args[2]: null);
			}
			else
			{
				priest.sendMessage(ChatColor.RED + plugin.lang.Get("Priest.NotInRange"));
			}
		}
	}
	
	public void SelfMarry(Player player, String otP)
	{
		SelfMarry(player, otP, null);
	}
	
	@SuppressWarnings("deprecation")
	public void SelfMarry(Player player, String otP, String Surname)
	{
		Player otherPlayer = Bukkit.getServer().getPlayer(otP);
		if(otherPlayer == null || !otherPlayer.isOnline())
		{
			player.sendMessage(ChatColor.RED + String.format(plugin.lang.Get("Ingame.PlayerNotOn"), otP));
			return;
		}
		if(player.equals(otherPlayer))
		{
			player.sendMessage(ChatColor.RED + plugin.lang.Get("Ingame.NotYourself"));
			return;
		}
		if(plugin.InRadiusAllWorlds(player, otherPlayer, plugin.config.GetRange("Marry")))
		{
			if(!plugin.HasPartner(player))
			{
				if(!plugin.HasPartner(otherPlayer))
				{
					if(!HasOpenRequest(player))
					{
						if(!HasOpenRequest(otherPlayer))
						{
							plugin.mr.add(new Marry_Requests(null, player, otherPlayer, Surname));
							player.sendMessage(ChatColor.GREEN + plugin.lang.Get("Ingame.MarryRequestSent"));
							otherPlayer.sendMessage(String.format(plugin.lang.Get("Ingame.MarryConfirm"), player.getDisplayName() + ChatColor.WHITE));
						}
						else
						{
							player.sendMessage(ChatColor.RED + String.format(plugin.lang.Get("Priest.AlreadyOpenRequest"), otherPlayer.getDisplayName() + ChatColor.RED));
						}
					}
					else
					{
						player.sendMessage(ChatColor.RED + plugin.lang.Get("Ingame.AlreadyOpenRequest"));
					}
				}
				else
				{
					player.sendMessage(ChatColor.RED + String.format(plugin.lang.Get("Ingame.OtherAlreadyMarried"), otherPlayer.getDisplayName() + ChatColor.RED));
				}
			}
			else
			{
				player.sendMessage(ChatColor.RED + plugin.lang.Get("Ingame.AlreadyMarried"));
			}
		}
		else
		{
			player.sendMessage(ChatColor.RED + plugin.lang.Get("Priest.NotInRange"));
		}
	}
	
	private boolean HasOpenRequest(Player player)
	{
		for(Marry_Requests m:plugin.mr)
		{
			if(m.p1 == player || m.p2 == player)
			{
				return true;
			}
		}
		return false;
	}
	
	private boolean HasOpenDivorceRequest(Player player)
	{
		for(Marry_Requests m : plugin.bdr)
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
		if(plugin.economy == null || plugin.economy.Marry(null, m.p1, m.p2))
		{
			plugin.DB.MarryPlayers(m.p1, m.p2, "none", m.surname);
			m.p1.sendMessage(ChatColor.GREEN + String.format(plugin.lang.Get("Ingame.HasMarried"), m.p2.getDisplayName() + ChatColor.GREEN));
			m.p2.sendMessage(ChatColor.GREEN + String.format(plugin.lang.Get("Ingame.HasMarried"), m.p1.getDisplayName() + ChatColor.GREEN));
			if(plugin.config.GetAnnouncementEnabled())
			{
				plugin.getServer().broadcastMessage(ChatColor.GREEN + String.format(plugin.lang.Get("Ingame.BroadcastMarriage"), m.p1.getDisplayName() + ChatColor.GREEN, m.p2.getDisplayName() + ChatColor.GREEN));
			}
		}
	}
	
	public void AcceptMarriage(Player player)
	{
		for (Marry_Requests m : plugin.bdr)
		{
    		if(m.p1 == player || m.p2 == player)
    		{
    			if(!m.HasAccepted(player))
    			{
    				plugin.bdr.remove(m);
    				m.Accept(player);
    				if(m.BothAcceoted(player))
    				{
    					SaveDivorce(m.p1, m.priest);
    				}
    				else
    				{
    					m.p2.sendMessage(plugin.lang.Get("Priest.DivorceConfirm"));
    					plugin.bdr.add(m);
    				}
    			}
    			else
    			{
    				player.sendMessage(plugin.lang.Get("Priest.AlreadyAccepted"));
    			}
    			return;
    		}
    	}
		for(Map.Entry<Player, Player> entry : plugin.dr.entrySet())
		{
			if(entry.getKey().equals(player))
			{
				SaveDivorce(entry.getKey(), entry.getValue());
				plugin.dr.remove(player);
				return;
			}
		}
		for (Marry_Requests m : plugin.mr)
		{
    		if(m.p1 == player || m.p2 == player)
    		{
    			if(m.priest == null)
    			{
    				if(m.p2 == player)
    				{
    					plugin.mr.remove(m);
    					SelfMarryAccept(m);
    				}
    				return;
    			}
    			if(!m.HasAccepted(player))
    			{
    				plugin.mr.remove(m);
    				m.Accept(player);
    				if(plugin.config.UseConfirmation() && plugin.config.UseConfirmationAutoDialog())
					{
    					player.chat(plugin.lang.Get("Dialog.YesIWant"));
					}
    				if(m.BothAcceoted(player))
    				{
    					SaveMarry(m.priest, m.p1, m.p2, m.surname);
    				}
    				else
    				{
    					if(plugin.config.UseConfirmation() && plugin.config.UseConfirmationAutoDialog())
    					{
    						m.priest.chat(String.format(plugin.lang.Get("Dialog.AndDoYouWant"), m.p2.getName(), m.p1.getName()));
    					}
    					m.p2.sendMessage(plugin.lang.Get("Priest.Confirm"));
    					plugin.mr.add(m);
    				}
    			}
    			else
    			{
    				player.sendMessage(plugin.lang.Get("Priest.AlreadyAccepted"));
    			}
    			return;
    		}
    	}
    	player.sendMessage(plugin.lang.Get("Priest.NoRequest"));
	}
	
	private void SaveMarry(Player priest, Player player, Player otherPlayer, String surname)
	{
		if(plugin.economy == null || plugin.economy.Marry(priest, player, otherPlayer))
		{
			plugin.DB.MarryPlayers(player, otherPlayer, priest, surname);
			priest.sendMessage(ChatColor.GREEN + String.format(plugin.lang.Get("Priest.Married"), player.getDisplayName() + ChatColor.GREEN, otherPlayer.getDisplayName() + ChatColor.GREEN));
			player.sendMessage(ChatColor.GREEN + String.format(plugin.lang.Get("Priest.HasMarried"), priest.getDisplayName() + ChatColor.GREEN, otherPlayer.getDisplayName() + ChatColor.GREEN));
			otherPlayer.sendMessage(ChatColor.GREEN + String.format(plugin.lang.Get("Priest.HasMarried"), priest.getDisplayName() + ChatColor.GREEN, player.getDisplayName() + ChatColor.GREEN));
			if(plugin.config.UseConfirmation() && plugin.config.UseConfirmationAutoDialog())
			{
				priest.chat(plugin.lang.Get("Dialog.Married"));
			}
			if(plugin.config.GetAnnouncementEnabled())
			{
				plugin.getServer().broadcastMessage(ChatColor.GREEN + String.format(plugin.lang.Get("Priest.BroadcastMarriage"), priest.getDisplayName() + ChatColor.GREEN, player.getDisplayName() + ChatColor.GREEN, otherPlayer.getDisplayName() + ChatColor.GREEN));
			}
		}
	}

	private void MarryPlayer(Player priest, Player player, Player otherPlayer, String surname) 
	{
		if(!plugin.config.UseConfirmation())
		{
			SaveMarry(priest, player, otherPlayer, surname);
		}
		else
		{
			if(HasOpenRequest(player))
			{
				priest.sendMessage(String.format(ChatColor.RED + plugin.lang.Get("Priest.AlreadyOpenRequest"), player.getDisplayName() + ChatColor.RED));
			}
			else if(HasOpenRequest(otherPlayer))
			{
				priest.sendMessage(String.format(ChatColor.RED + plugin.lang.Get("Priest.AlreadyOpenRequest"), otherPlayer.getDisplayName() + ChatColor.RED));
			}
			else
			{
				plugin.mr.add(new Marry_Requests(priest, player, otherPlayer, surname));
				if(plugin.config.UseConfirmationAutoDialog())
				{
					priest.chat(String.format(plugin.lang.Get("Dialog.DoYouWant"), player.getName(), otherPlayer.getName()));
				}
				player.sendMessage(plugin.lang.Get("Priest.Confirm"));
			}
		}
	}

	private boolean InRadius(Player player, Player otherPlayer, Player priest) 
	{
		return plugin.InRadiusAllWorlds(priest, player, plugin.config.GetRange("Marry")) && plugin.InRadiusAllWorlds(priest, otherPlayer, plugin.config.GetRange("Marry"));
	}
	
	public void setPriest(String[] args, CommandSender sender)
	{
		@SuppressWarnings("deprecation")
		Player player = plugin.getServer().getPlayer(args[1]);
		if(player != null && player.isOnline())
		{
			if(plugin.IsPriest(player))
			{
				plugin.DB.DelPriest(player);
				player.sendMessage(ChatColor.GREEN + String.format(plugin.lang.Get("Priest.UnMadeYouAPriest"), sender.getName()));
				sender.sendMessage(ChatColor.GREEN + String.format(plugin.lang.Get("Priest.UnMadeAPriest"), player.getDisplayName() + ChatColor.GREEN));
			}
			else
			{
				plugin.DB.SetPriest(player);
				player.sendMessage(ChatColor.GREEN + String.format(plugin.lang.Get("Priest.MadeYouAPriest"), sender.getName()));
				sender.sendMessage(ChatColor.GREEN + String.format(plugin.lang.Get("Priest.MadeAPriest"), player.getDisplayName() + ChatColor.GREEN));
			}
		}
		else
		{
			sender.sendMessage(ChatColor.RED + String.format(plugin.lang.Get("Ingame.PlayerNotOn"), args[1]));
		}
	}

	@SuppressWarnings("deprecation")
	public void setPriest(String[] args, Player sender) 
	{
		Player player = plugin.getServer().getPlayer(args[1]);
		if(player != null && player.isOnline())
		{
			if(plugin.IsPriest(player))
			{
				plugin.DB.DelPriest(player);
				player.sendMessage(ChatColor.GREEN + String.format(plugin.lang.Get("Priest.UnMadeYouAPriest"), sender.getDisplayName() + ChatColor.GREEN));
				sender.sendMessage(ChatColor.GREEN + String.format(plugin.lang.Get("Priest.UnMadeAPriest"), player.getDisplayName() + ChatColor.GREEN));
			}
			else
			{
				plugin.DB.SetPriest(player);
				player.sendMessage(ChatColor.GREEN + String.format(plugin.lang.Get("Priest.MadeYouAPriest"), sender.getDisplayName() + ChatColor.GREEN));
				sender.sendMessage(ChatColor.GREEN + String.format(plugin.lang.Get("Priest.MadeAPriest"), player.getDisplayName() + ChatColor.GREEN));
			}
		}
		else
		{
			sender.sendMessage(ChatColor.RED + String.format(plugin.lang.Get("Ingame.PlayerNotOn"), args[1]));
		}
	}
	
	@SuppressWarnings("deprecation")
	public void Divorce(Player priest, String[] args)
	{
		Player player = Bukkit.getServer().getPlayer(args[1]);
		if(player == null || (player != null && !player.isOnline()))
		{
			priest.sendMessage(ChatColor.RED + String.format(plugin.lang.Get("Ingame.PlayerNotOn"), args[1]));
			return;
		}
		String otP = plugin.DB.GetPartner(player);
		if(otP == null || otP.isEmpty())
		{
			priest.sendMessage(ChatColor.RED + plugin.lang.Get("Priest.PlayerNotMarried"));
			return;
		}
		Player otherPlayer = Bukkit.getServer().getPlayerExact(otP);
		if(plugin.config.UseConfirmation() && (plugin.dr.containsKey(player) || (plugin.config.getConfirmationBothDivorce() && HasOpenDivorceRequest(player))))
		{
			priest.sendMessage(String.format(ChatColor.RED + plugin.lang.Get("Priest.AlreadyOpenRequest"), player.getDisplayName() + ChatColor.RED));
			return;
		}
		if(otherPlayer == null || !otherPlayer.isOnline())
		{
			if(plugin.CheckPerm(priest, "marry.offlinedivorce", false))
			{
				if(!plugin.config.UseConfirmation())
				{
					plugin.DB.DivorcePlayer(player);
					priest.sendMessage(ChatColor.GREEN + String.format(plugin.lang.Get("Priest.Divorced"), player.getDisplayName() + ChatColor.GREEN, ChatColor.GRAY + otP + ChatColor.GREEN));
					player.sendMessage(ChatColor.GREEN + String.format(plugin.lang.Get("Priest.DivorcedPlayer"), priest.getDisplayName() + ChatColor.GREEN, ChatColor.GRAY + otP + ChatColor.GREEN));
					if(plugin.config.GetAnnouncementEnabled())
					{
						plugin.getServer().broadcastMessage(ChatColor.GREEN + String.format(plugin.lang.Get("Priest.BroadcastDivorce"), priest.getDisplayName() + ChatColor.GREEN, player.getDisplayName() + ChatColor.GREEN, ChatColor.GRAY + otP + ChatColor.GREEN));
					}
				}
				else
				{
					plugin.dr.put(player, priest);
					player.sendMessage(plugin.lang.Get("Priest.DivorceConfirm"));
					priest.sendMessage(plugin.lang.Get("Priest.DivorceRequestSent"));
				}
			}
			else
			{
				priest.sendMessage(ChatColor.RED + String.format(plugin.lang.Get("Priest.PartnerOffline"), args[1], otP));
			}
		}
		else if(InRadius(player, otherPlayer, priest))
		{
			if(!plugin.config.UseConfirmation())
			{
				SaveDivorce(player, priest);
			}
			else
			{
				if(plugin.config.getConfirmationBothDivorce())
				{
					plugin.bdr.add(new Marry_Requests(priest, player, otherPlayer, null));
				}
				else
				{
					plugin.dr.put(player, priest);
				}
				player.sendMessage(plugin.lang.Get("Priest.DivorceConfirm"));
				priest.sendMessage(plugin.lang.Get("Priest.DivorceRequestSent"));
			}
		}
		else
		{
			priest.sendMessage(ChatColor.RED + plugin.lang.Get("Priest.NotInRange"));
		}
	}
	
	@SuppressWarnings("deprecation")
	public void Divorce(CommandSender priest, String[] args)
	{
		Player player = Bukkit.getServer().getPlayer(args[1]);
		if(player == null || (player != null && !player.isOnline()))
		{
			priest.sendMessage(ChatColor.RED + String.format(plugin.lang.Get("Ingame.PlayerNotOn"), args[1]));
			return;
		}
		String otP = plugin.DB.GetPartner(player);
		if(otP == null || otP.isEmpty())
		{
			priest.sendMessage(ChatColor.RED + plugin.lang.Get("Priest.PlayerNotMarried"));
			return;
		}
		plugin.DB.DivorcePlayer(player);
		Player otherPlayer = Bukkit.getServer().getPlayer(otP);
		if(otherPlayer != null && otherPlayer.isOnline())
		{
			priest.sendMessage(ChatColor.GREEN + String.format(plugin.lang.Get("Priest.Divorced"), player.getDisplayName() + ChatColor.GREEN, otherPlayer.getDisplayName() + ChatColor.GREEN));
			player.sendMessage(ChatColor.GREEN + String.format(plugin.lang.Get("Priest.DivorcedPlayer"), ChatColor.GRAY + "Console" + ChatColor.GREEN, otherPlayer.getDisplayName() + ChatColor.GREEN));
			otherPlayer.sendMessage(ChatColor.GREEN + String.format(plugin.lang.Get("Priest.DivorcedPlayer"), ChatColor.GRAY + "Console" + ChatColor.GREEN, player.getDisplayName() + ChatColor.GREEN));
			if(plugin.config.GetAnnouncementEnabled())
			{
				plugin.getServer().broadcastMessage(ChatColor.GREEN + String.format(plugin.lang.Get("Priest.BroadcastDivorce"), ChatColor.GRAY + "Console" + ChatColor.GREEN, player.getDisplayName() + ChatColor.GREEN, otherPlayer.getDisplayName() + ChatColor.GREEN));
			}
		}
		else
		{
			priest.sendMessage(ChatColor.GREEN + String.format(plugin.lang.Get("Priest.Divorced"), player.getDisplayName() + ChatColor.GREEN, ChatColor.GRAY + otP + ChatColor.GREEN));
			player.sendMessage(ChatColor.GREEN + String.format(plugin.lang.Get("Priest.DivorcedPlayer"), ChatColor.GRAY + "Console" + ChatColor.GREEN, ChatColor.GRAY + otP + ChatColor.GREEN));
			if(plugin.config.GetAnnouncementEnabled())
			{
				plugin.getServer().broadcastMessage(ChatColor.GREEN + String.format(plugin.lang.Get("Priest.BroadcastDivorce"), ChatColor.GRAY + "Console" + ChatColor.GREEN, player.getDisplayName() + ChatColor.GREEN, ChatColor.GRAY + otP + ChatColor.GREEN));
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	private void SaveDivorce(Player player, Player priest)
	{
		String otP = plugin.DB.GetPartner(player);
		if(otP == null || otP.isEmpty())
		{
			priest.sendMessage(ChatColor.RED + plugin.lang.Get("Priest.PlayerNotMarried"));
			return;
		}
		Player otherPlayer = Bukkit.getServer().getPlayerExact(otP);
		if(otherPlayer == null || !otherPlayer.isOnline())
		{
			priest.sendMessage(ChatColor.RED + String.format(plugin.lang.Get("Priest.PartnerOffline"), player.getName(), otP));
			return;
		}
		if(plugin.economy == null || plugin.economy.Divorce(priest, player, otherPlayer))
		{
			plugin.DB.DivorcePlayer(player);
			priest.sendMessage(ChatColor.GREEN + String.format(plugin.lang.Get("Priest.Divorced"), player.getDisplayName() + ChatColor.GREEN, otherPlayer.getDisplayName() + ChatColor.GREEN));
			player.sendMessage(ChatColor.GREEN + String.format(plugin.lang.Get("Priest.DivorcedPlayer"), priest.getDisplayName() + ChatColor.GREEN, otherPlayer.getDisplayName() + ChatColor.GREEN));
			otherPlayer.sendMessage(ChatColor.GREEN + String.format(plugin.lang.Get("Priest.DivorcedPlayer"), priest.getDisplayName() + ChatColor.GREEN, player.getDisplayName() + ChatColor.GREEN));
			if(plugin.config.GetAnnouncementEnabled())
			{
				plugin.getServer().broadcastMessage(ChatColor.GREEN + String.format(plugin.lang.Get("Priest.BroadcastDivorce"), priest.getDisplayName() + ChatColor.GREEN, player.getDisplayName() + ChatColor.GREEN, otherPlayer.getDisplayName() + ChatColor.GREEN));
			}
		}
	}

	@SuppressWarnings("deprecation")
	public void SelfDivorce(Player player)
	{
		String partner = plugin.DB.GetPartner(player);
		if(partner == null || partner.isEmpty())
		{
			player.sendMessage(ChatColor.RED + plugin.lang.Get("Ingame.NotMarried"));
			return;
		}
		Player otherPlayer = Bukkit.getServer().getPlayerExact(partner);
		if(otherPlayer == null || !otherPlayer.isOnline())
		{
			if(plugin.CheckPerm(player, "marry.offlinedivorce", false))
			{
				plugin.DB.DivorcePlayer(player);
				player.sendMessage(ChatColor.GREEN + String.format(plugin.lang.Get("Ingame.Divorced"),  ChatColor.GRAY + partner + ChatColor.GREEN));
				if(plugin.config.GetAnnouncementEnabled())
				{
					plugin.getServer().broadcastMessage(ChatColor.GREEN + String.format(plugin.lang.Get("Ingame.BroadcastDivorce"), player.getDisplayName() + ChatColor.GREEN, partner + ChatColor.GREEN));
				}
			}
			else
			{
				player.sendMessage(ChatColor.RED + plugin.lang.Get("Ingame.PartnerOffline"));
			}
		}
		else
		{
			if(plugin.economy == null || plugin.economy.Divorce(null ,player, otherPlayer))
			{
				plugin.DB.DivorcePlayer(player);
				player.sendMessage(ChatColor.GREEN + String.format(plugin.lang.Get("Ingame.Divorced"), otherPlayer.getDisplayName() + ChatColor.GREEN));
				otherPlayer.sendMessage(ChatColor.GREEN + String.format(plugin.lang.Get("Ingame.DivorcedPlayer"), player.getDisplayName() + ChatColor.GREEN));
				if(plugin.config.GetAnnouncementEnabled())
				{
					plugin.getServer().broadcastMessage(ChatColor.GREEN + String.format(plugin.lang.Get("Ingame.BroadcastDivorce"), player.getDisplayName() + ChatColor.GREEN, otherPlayer.getDisplayName() + ChatColor.GREEN));
				}
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	public void SetSurname(CommandSender sender, String p, String surname)
	{
		Player player = Bukkit.getPlayer(p);
		if(player != null && player.isOnline())
		{
			if(plugin.HasPartner(player))
			{
				if(surname.equalsIgnoreCase("null") || surname.equalsIgnoreCase("none") || surname.equalsIgnoreCase("remove"))
				{
					surname = null;
				}
				plugin.DB.SetSurname(player, surname);
				sender.sendMessage(ChatColor.GREEN + plugin.lang.Get("Priest.SurnameSet"));
			}
			else
			{
				sender.sendMessage(ChatColor.RED + plugin.lang.Get("Ingame.NotMarried"));
			}
		}
		else
		{
			sender.sendMessage(ChatColor.RED + String.format(plugin.lang.Get("Ingame.PlayerNotOn"), p));
		}
	}
}