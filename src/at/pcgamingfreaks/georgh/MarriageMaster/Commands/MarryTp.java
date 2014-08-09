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
import org.bukkit.Location;
import org.bukkit.entity.Player;

import at.pcgamingfreaks.georgh.MarriageMaster.MarriageMaster;

public class MarryTp 
{
	private MarriageMaster marriageMaster;
	
	public MarryTp(MarriageMaster marriagemaster)
	{
		marriageMaster = marriagemaster;
	}

	@SuppressWarnings("deprecation")
	public void TP(Player player)
	{
		String partner = marriageMaster.DB.GetPartner(player);
		if(partner != null && !partner.isEmpty())
		{
			Player otherPlayer = marriageMaster.getServer().getPlayer(partner);
			if(otherPlayer != null && otherPlayer.isOnline())
			{
				if(player.canSee(otherPlayer))
				{
					if(!marriageMaster.config.GetBlacklistedWorlds().contains(otherPlayer.getWorld().getName()))
					{
						if(!marriageMaster.economy.on || marriageMaster.economy.Teleport(player, marriageMaster.config.GetEconomyTp()))
						{
							DoTP(player, otherPlayer);
						}
					}
					else
					{
						player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.WorldNotAllowed"));
					}
				}
				else
				{
					player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NoTPInVanish"));
				}
			}
			else
			{
				player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.PartnerOffline"));
			}
		}
		else
		{
			player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NotMarried"));
		}
	}

	private void DoTP(Player player, Player otherPlayer) 
	{
		if(marriageMaster.config.DelayTP() && !marriageMaster.config.CheckPerm(player, "marry.skiptpdelay", false))
		{
			final Location p_loc = player.getLocation();
			final Player p = player, otp = otherPlayer;
			final double p_hea = (double)player.getHealth();
			p.sendMessage(ChatColor.GOLD + String.format(marriageMaster.lang.Get("Ingame.TPDontMove"), marriageMaster.config.TPDelayTime()));
			Bukkit.getScheduler().runTaskLater(marriageMaster, new Runnable() { @Override public void run() {
				if(p != null && p.isOnline())
				{
					if(otp != null && otp.isOnline())
					{
						if(p_hea <= p.getHealth() && p_loc.getX() == p.getLocation().getX() && p_loc.getY() == p.getLocation().getY() && p_loc.getZ() == p.getLocation().getZ() && p_loc.getWorld().equals(p.getLocation().getWorld()))
						{
							p.teleport(otp);
							p.sendMessage(ChatColor.GREEN + marriageMaster.lang.Get("Ingame.TP"));
							otp.sendMessage(ChatColor.GREEN + marriageMaster.lang.Get("Ingame.TPto"));
						}
						else
						{
							p.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.TPMoved"));
						}
					}
					else
					{
						p.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.PartnerOffline"));
				}}}}, marriageMaster.config.TPDelayTime() * 20L);
		}
		else
		{
			player.teleport(otherPlayer);
			player.sendMessage(ChatColor.GREEN + marriageMaster.lang.Get("Ingame.TP"));
			otherPlayer.sendMessage(ChatColor.GREEN + marriageMaster.lang.Get("Ingame.TPto"));
		}
	}
}
