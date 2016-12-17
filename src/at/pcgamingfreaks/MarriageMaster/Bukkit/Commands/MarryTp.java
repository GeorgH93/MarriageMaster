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

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;

public class MarryTp 
{
	private MarriageMaster plugin;
	
	private long delayTime;
	
	public MarryTp(MarriageMaster marriagemaster)
	{
		plugin = marriagemaster;
		
		delayTime = plugin.config.TPDelayTime() * 20L;
	}

	@SuppressWarnings("deprecation")
	public void TP(Player player)
	{
		String partner = plugin.DB.GetPartner(player);
		if(partner != null && !partner.isEmpty())
		{
			Player otherPlayer = plugin.getServer().getPlayerExact(partner);
			if(otherPlayer != null && otherPlayer.isOnline())
			{
				if(player.canSee(otherPlayer))
				{
					if(!plugin.config.GetBlacklistedWorlds().contains(otherPlayer.getWorld().getName()))
					{
						if(plugin.economy == null || plugin.economy.Teleport(player))
						{
							DoTP(player, otherPlayer);
						}
					}
					else
					{
						player.sendMessage(ChatColor.RED + plugin.lang.get("Ingame.WorldNotAllowed"));
					}
				}
				else
				{
					player.sendMessage(ChatColor.RED + plugin.lang.get("Ingame.NoTPInVanish"));
				}
			}
			else
			{
				player.sendMessage(ChatColor.RED + plugin.lang.get("Ingame.PartnerOffline"));
			}
		}
		else
		{
			player.sendMessage(ChatColor.RED + plugin.lang.get("Ingame.NotMarried"));
		}
	}

	private void DoTP(final Player player, final Player otherPlayer)
	{
		if(plugin.config.DelayTP() && !plugin.CheckPerm(player, "marry.skiptpdelay", false))
		{
			final Location p_loc = player.getLocation();
			final double p_hea = (double)player.getHealth();
			player.sendMessage(ChatColor.GOLD + String.format(plugin.lang.get("Ingame.TPDontMove"), plugin.config.TPDelayTime()));
			Bukkit.getScheduler().runTaskLater(plugin, new Runnable() { @Override public void run() {
				if(player.isOnline())
				{
					if(otherPlayer != null && otherPlayer.isOnline())
					{
						if(p_hea <= player.getHealth() && p_loc.getX() == player.getLocation().getX() && p_loc.getY() == player.getLocation().getY() && p_loc.getZ() == player.getLocation().getZ() && p_loc.getWorld().equals(player.getLocation().getWorld()))
						{
							if(!plugin.config.getCheckTPSafety() || player.isFlying() || !otherPlayer.isFlying())
							{
								player.teleport(otherPlayer);
								player.sendMessage(ChatColor.GREEN + plugin.lang.get("Ingame.TP"));
								otherPlayer.sendMessage(ChatColor.GREEN + plugin.lang.get("Ingame.TPto"));
							}
							else
							{
								Location l = getSaveLoc(otherPlayer.getLocation());
								if(l != null)
								{
									player.teleport(l);
									player.sendMessage(ChatColor.GREEN + plugin.lang.get("Ingame.TP"));
									otherPlayer.sendMessage(ChatColor.GREEN + plugin.lang.get("Ingame.TPto"));
								}
								else
								{
									player.sendMessage(ChatColor.RED + plugin.lang.get("Ingame.TPUnsafe"));
									otherPlayer.sendMessage(ChatColor.GOLD + plugin.lang.get("Ingame.TPtoUnsafe"));
								}
							}
						}
						else
						{
							player.sendMessage(ChatColor.RED + plugin.lang.get("Ingame.TPMoved"));
						}
					}
					else
					{
						player.sendMessage(ChatColor.RED + plugin.lang.get("Ingame.PartnerOffline"));
				}}}}, delayTime);
		}
		else
		{
			player.teleport(otherPlayer);
			player.sendMessage(ChatColor.GREEN + plugin.lang.get("Ingame.TP"));
			otherPlayer.sendMessage(ChatColor.GREEN + plugin.lang.get("Ingame.TPto"));
		}
	}
	
	private Location getSaveLoc(Location loc)
	{
		World w = loc.getWorld();
		Material mat;
		int x = loc.getBlockX(), y = loc.getBlockY() - 1, z = loc.getBlockZ(), miny = -1;
		Block b, b1 = w.getBlockAt(x, y + 1, z), b2 = w.getBlockAt(x, y + 2, z);
		loc = null;
		for(; y > 0 && y > miny && loc == null; y--)
		{
			b = w.getBlockAt(x, y, z);
			if(b != null && !b.isEmpty())
			{
				if(miny == -1)
				{
					miny = y - 10;
				}
				mat = b.getType();
				if(!(mat.equals(Material.FIRE) || mat.equals(Material.AIR) || mat.equals(Material.LAVA) || mat.equals(Material.CACTUS)) && ((b1 == null || b1.isEmpty()) && (b2 == null || b2.isEmpty())))
				{
					loc = b.getLocation();
					loc.setY(loc.getY() + 1);
				}
			}
			b2 = b1;
			b1 = b;
		}
		return loc;
	}
	
	public void BungeeTPDelay(final Player player)
	{
		if(player != null)
		{
			final double p_hea = player.getHealth();
			final Location p_loc = player.getLocation();
			player.sendMessage(ChatColor.GOLD + String.format(plugin.lang.get("Ingame.TPDontMove"), plugin.config.TPDelayTime()));
			Bukkit.getScheduler().runTaskLater(plugin, new Runnable()
				{
					@Override
					public void run()
					{
						if(player.isOnline())
						{
							if(p_hea <= player.getHealth() && p_loc.getX() == player.getLocation().getX() && p_loc.getY() == player.getLocation().getY() && p_loc.getZ() == player.getLocation().getZ() && p_loc.getWorld().equals(player.getLocation().getWorld()))
							{
								plugin.pluginchannel.sendMessage("tp|" + player.getName());
							}
							else
							{
								player.sendMessage(ChatColor.RED + plugin.lang.get("Ingame.TPMoved"));
							}
						}
					}}, delayTime);
		}
	}
}
