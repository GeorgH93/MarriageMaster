/*
 *   Copyright (C) 2023 GeorgH93
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
 *   along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.MarriageMaster.Bukkit.Commands;

import at.pcgamingfreaks.Bukkit.MCVersion;
import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.AcceptPendingRequest;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.DelayableTeleportAction;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.TPEvent;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarryCommand;
import at.pcgamingfreaks.MarriageMaster.Bukkit.CommonMessages;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Permissions;
import at.pcgamingfreaks.MarriageMaster.Placeholder.Placeholders;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class TpCommand extends MarryCommand
{
	private static final Set<Material> BAD_MATS = new HashSet<>();

	static
	{
		BAD_MATS.add(Material.FIRE);
		BAD_MATS.add(Material.LAVA);
		BAD_MATS.add(Material.CACTUS);
	}

	private final Message messageTeleport, messageTeleportTo, messageUnsafe, messageToUnsafe, messagePartnerVanished, messageWorldNotAllowed;
	private final Message messageRequireConfirmation, messageWaitForConfirmation, messageRequestDenied, messageRequestDeniedPartner, messageRequestCanceled, messageRequestCanceledPartner;
	private final Message messageRequestCanceledDisconnectRequester, messageRequestCanceledDisconnectTarget;
	private final Set<String> blacklistedWorlds;
	private final boolean safetyCheck, requireConfirmation, findSafeLocation;
	private final long delayTime;

	public TpCommand(MarriageMaster plugin)
	{
		super(plugin, "tp", plugin.getLanguage().getTranslated("Commands.Description.Tp"), Permissions.TP, true, true, plugin.getLanguage().getCommandAliases("Tp"));

		blacklistedWorlds   = plugin.getConfiguration().getTPBlackListedWorlds();
		safetyCheck         = plugin.getConfiguration().getSafetyCheck();
		requireConfirmation = plugin.getConfiguration().getRequireConfirmation();
		findSafeLocation    = plugin.getConfiguration().getFindSafeLocation();
		delayTime           = plugin.getConfiguration().getTPDelayTime() * 20L;

		//region loading messages
		messageTeleport        = plugin.getLanguage().getMessage("Ingame.TP.Teleport");
		messageTeleportTo      = plugin.getLanguage().getMessage("Ingame.TP.TeleportTo");
		messageUnsafe          = plugin.getLanguage().getMessage("Ingame.TP.Unsafe");
		messageToUnsafe        = plugin.getLanguage().getMessage("Ingame.TP.ToUnsafe");
		messagePartnerVanished = plugin.getLanguage().getMessage("Ingame.TP.PartnerVanished");
		messageWorldNotAllowed = plugin.getLanguage().getMessage("Ingame.TP.WorldNotAllowed");

		messageRequireConfirmation                = plugin.getLanguage().getMessage("Ingame.TP.Request.Notification").placeholders(Placeholders.PLAYER_NAME);
		messageWaitForConfirmation                = plugin.getLanguage().getMessage("Ingame.TP.Request.WaitForConfirmation");
		messageRequestDenied                      = plugin.getLanguage().getMessage("Ingame.TP.Request.Denied");
		messageRequestDeniedPartner               = plugin.getLanguage().getMessage("Ingame.TP.Request.DeniedPartner");
		messageRequestCanceled                    = plugin.getLanguage().getMessage("Ingame.TP.Request.Canceled");
		messageRequestCanceledPartner             = plugin.getLanguage().getMessage("Ingame.TP.Request.CanceledPartner");
		messageRequestCanceledDisconnectRequester = plugin.getLanguage().getMessage("Ingame.TP.Request.CanceledDisconnectRequester");
		messageRequestCanceledDisconnectTarget    = plugin.getLanguage().getMessage("Ingame.TP.Request.CanceledDisconnectTarget");
		//endregion

		if(plugin.getPluginChannelCommunicator() != null)
		{
			plugin.getPluginChannelCommunicator().setTpCommand(this);
		}
	}

	@Override
	public void execute(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		MarriagePlayer player = getMarriagePlugin().getPlayerData((Player) sender);
		MarriagePlayer partner = (getMarriagePlugin().areMultiplePartnersAllowed() && args.length >= 1) ? player.getPartner(args[0]) : player.getNearestPartnerMarriageData().getPartner(player);
		if(partner == null)
		{
			CommonMessages.getMessageTargetPartnerNotFound().send(sender);
		}
		else if(partner.isOnline())
		{
			if(requireConfirmation && !(player.hasPermission(Permissions.BYPASS_TP_CONFIRMATION) || partner.hasPermission(Permissions.AUTO_ACCEPT_TP_REQUEST)))
			{
				messageWaitForConfirmation.send(sender);
				getMarriagePlugin().getCommandManager().registerAcceptPendingRequest(new TpRequest(player, partner));
				partner.send(messageRequireConfirmation, player);
			}
			else
			{
				tp(player, partner);
			}
		}
		else
		{
			CommonMessages.getMessagePartnerOffline().send(sender);
		}
	}

	private void tp(@NotNull final MarriagePlayer player, @NotNull final MarriagePlayer partner)
	{
		TPEvent event = new TPEvent(player, player.getMarriageData(partner));
		Bukkit.getPluginManager().callEvent(event);
		if(!event.isCancelled())
		{
			getMarriagePlugin().doDelayableTeleportAction(new TpToPartner(player, partner.getPlayerOnline()));
		}
	}

	@Override
	public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		return getMarriagePlugin().getCommandManager().getSimpleTabComplete(sender, args);
	}

	private Material getMaterial(final @Nullable Block block)
	{
		if (block == null) return Material.AIR;
		return block.getType();
	}

	private boolean checkBlock(Block block, Material mat)
	{
		return (block == null || (MCVersion.isNewerOrEqualThan(MCVersion.MC_1_13) ? (block.isPassable() && !BAD_MATS.contains(mat)) : block.isEmpty()));
	}

	private @Nullable Location getSafeLoc(@NotNull Location loc)
	{
		World w = loc.getWorld();
		if(w == null) return null;
		int y = loc.getBlockY();
		if(loc.getY() - y < 0.001) y--;
		int x = loc.getBlockX(), z = loc.getBlockZ(), miny = MCVersion.isOlderThan(MCVersion.MC_1_17) ? Math.max(y - 10, 1) : y - 10;
		Block b, b1 = w.getBlockAt(x, y + 1, z), b2 = w.getBlockAt(x, y + 2, z);
		Material mat, matB1 = getMaterial(b1), matB2 = getMaterial(b2);
		for(loc = null; y > miny && loc == null; y--)
		{
			b = w.getBlockAt(x, y, z);
			mat = getMaterial(b);
			if(b != null && !b.isEmpty() && ((!BAD_MATS.contains(mat) && mat != Material.AIR) && checkBlock(b1, matB1) && checkBlock(b2, matB2)))
			{
				loc = b.getLocation();
				loc.setX(loc.getX() + 0.5);
				loc.setY(loc.getY() + 1);
				loc.setZ(loc.getZ() + 0.5);
			}
			b2 = b1;
			matB2 = matB1;
			b1 = b;
			matB1 = mat;
			if (!findSafeLocation) break;
		}
		return loc;
	}

	public void doTheTP(final @NotNull Player player, final @NotNull Player partner)
	{
		if(player.canSee(partner))
		{
			Location loc = partner.getLocation();
			if(!blacklistedWorlds.contains(loc.getWorld().getName().toLowerCase(Locale.ENGLISH)) || player.hasPermission(Permissions.BYPASS_WORLD_BLACKLIST))
			{
				if(!player.isFlying() && safetyCheck && (loc = getSafeLoc(loc)) == null)
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

	@AllArgsConstructor
	private class TpToPartner implements DelayableTeleportAction
	{
		@Getter private final MarriagePlayer player;
		private final Player partner;

		@Override
		public void run()
		{
			doTheTP(player.getPlayerOnline(), partner);
		}

		@Override
		public long getDelay()
		{
			return delayTime;
		}
	}

	private class TpRequest extends AcceptPendingRequest
	{
		MarriagePlayer player;

		public TpRequest(MarriagePlayer player, MarriagePlayer partner)
		{
			super(partner, player);
			this.player = player;
		}

		@Override
		protected void onAccept()
		{
			tp(player,getPlayerThatHasToAccept());
		}

		@Override
		protected void onDeny()
		{
			getPlayerThatHasToAccept().send(messageRequestDenied);
			player.send(messageRequestDeniedPartner);
		}

		@Override
		protected void onCancel(@NotNull MarriagePlayer marriagePlayer)
		{
			player.send(messageRequestCanceled);
			getPlayerThatHasToAccept().send(messageRequestCanceledPartner);
		}

		@Override
		protected void onDisconnect(@NotNull MarriagePlayer marriagePlayer)
		{
			if(marriagePlayer.equals(player)) getPlayerThatHasToAccept().send(messageRequestCanceledDisconnectRequester);
			else player.send(messageRequestCanceledDisconnectTarget);
		}
	}
}