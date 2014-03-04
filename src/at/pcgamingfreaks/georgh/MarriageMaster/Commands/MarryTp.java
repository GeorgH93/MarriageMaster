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

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import at.pcgamingfreaks.georgh.MarriageMaster.MarriageMaster;

public class MarryTp 
{
	private MarriageMaster marriageMaster;
	
	public MarryTp(MarriageMaster marriagemaster)
	{
		marriageMaster = marriagemaster;
	}

	public void TP(Player player)
	{		
		String partner = marriageMaster.DB.GetPartner(player.getName());
		if(partner != null && !partner.isEmpty())
		{
			Player otherPlayer = marriageMaster.getServer().getPlayer(partner);
			if(otherPlayer != null && otherPlayer.isOnline())
			{
				if(player.canSee(otherPlayer))
				{
					if(marriageMaster.config.UseEconomy())
					{
						if(marriageMaster.economy.Teleport(player, marriageMaster.config.GetEconomyTp()))
						{
							DoTP(player, otherPlayer);
						}
					}
					else
					{
						DoTP(player, otherPlayer);
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
			player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.PartnerOffline"));
		}
	}

	private void DoTP(Player player, Player otherPlayer) 
	{
		player.teleport(otherPlayer);
		player.sendMessage(ChatColor.GREEN + marriageMaster.lang.Get("Ingame.TP"));
		otherPlayer.sendMessage(ChatColor.GREEN + marriageMaster.lang.Get("Ingame.TPto"));		
	}
}
