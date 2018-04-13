/*
 *   Copyright (C) 2016 GeorgH93
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

import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.DelayableTeleportAction;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.TPEvent;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarryCommand;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class TpCommand extends MarryCommand
{
	private final Message messageTeleport, messageTeleportTo, messageUnsafe, messageToUnsafe, messagePartnerVanished, messageWorldNotAllowed;
	private final Set<String> blacklistedWorlds;
	private final boolean safetyCheck;
	private final long delayTime;

	public TpCommand(MarriageMaster plugin)
	{
		super(plugin, "tp", plugin.getLanguage().getTranslated("Commands.Description.Tp"), "marry.tp", true, true, plugin.getLanguage().getCommandAliases("Tp"));

		blacklistedWorlds = plugin.getConfiguration().getTPBlackListedWorlds();
		safetyCheck       = plugin.getConfiguration().getSafetyCheck();
		delayTime         = plugin.getConfiguration().getTPDelayTime() * 20L;

		messageTeleport        = plugin.getLanguage().getMessage("Ingame.TP.Teleport");
		messageTeleportTo      = plugin.getLanguage().getMessage("Ingame.TP.TeleportTo");
		messageUnsafe          = plugin.getLanguage().getMessage("Ingame.TP.Unsafe");
		messageToUnsafe        = plugin.getLanguage().getMessage("Ingame.TP.ToUnsafe");
		messagePartnerVanished = plugin.getLanguage().getMessage("Ingame.TP.PartnerVanished");
		messageWorldNotAllowed = plugin.getLanguage().getMessage("Ingame.TP.WorldNotAllowed");

		if(plugin.getPluginChannelCommunicator() != null)
		{
			plugin.getPluginChannelCommunicator().setTpCommand(this);
		}
	}

	@Override
	public void execute(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		MarriagePlayer player = getMarriagePlugin().getPlayerData((Player) sender);
		MarriagePlayer partner = (getMarriagePlugin().isPolygamyAllowed() && args.length >= 1) ? player.getPartner(args[0]) : player.getNearestPartnerMarriageData().getPartner(player);
		if(partner == null)
		{
			((MarriageMaster) getMarriagePlugin()).messageTargetPartnerNotFound.send(sender);
		}
		else if(partner.isOnline())
		{
			TPEvent event = new TPEvent(player, player.getMarriageData(partner));
			Bukkit.getPluginManager().callEvent(event);
			if(!event.isCancelled())
			{
				getMarriagePlugin().doDelayableTeleportAction(new TPToPartner((Player) sender, partner.getPlayer().getPlayer()));
			}
		}
		else
		{
			((MarriageMaster) getMarriagePlugin()).messagePartnerOffline.send(sender);
		}
	}

	@Override
	public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		return getMarriagePlugin().getCommandManager().getSimpleTabComplete(sender, args);
	}

	private Location getSaveLoc(Location loc)
	{
		Material mat;
		World w = loc.getWorld();
		int x = loc.getBlockX(), y = loc.getBlockY() - 1, z = loc.getBlockZ(), miny = y - 10;
		Block b, b1 = w.getBlockAt(x, y + 1, z), b2 = w.getBlockAt(x, y + 2, z);
		for(loc = null; y > 0 && y > miny && loc == null; y--)
		{
			b = w.getBlockAt(x, y, z);
			if(b != null && !b.isEmpty())
			{
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

	public void doTheTP(Player player, Player partner)
	{
		if(player.canSee(partner))
		{
			//noinspection SpellCheckingInspection
			if(!blacklistedWorlds.contains(partner.getLocation().getWorld().getName().toLowerCase()) || player.hasPermission("marry.bypass.worldblacklist"))
			{
				Location loc = partner.getLocation();
				if(safetyCheck && (loc = getSaveLoc(loc)) == null)
				{
					messageUnsafe.send(player);
					messageToUnsafe.send(partner);
				}
				else
				{
					player.teleport(loc);
					messageTeleport.send(player);
					messageTeleportTo.send(partner);
				}
			}
			else
			{
				messageWorldNotAllowed.send(player);
			}
		}
		else
		{
			messagePartnerVanished.send(player);
		}
	}

	private class TPToPartner implements DelayableTeleportAction
	{
		private final Player player, partner;

		public TPToPartner(Player player1, Player player2)
		{
			player  = player1;
			partner = player2;
		}

		@Override
		public void run()
		{
			doTheTP(player, partner);
		}

		@Override
		public @NotNull Player getPlayer()
		{
			return player;
		}

		@Override
		public long getDelay()
		{
			return delayTime;
		}
	}
}