/*
 *   Copyright (C) 2019 GeorgH93
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

package at.pcgamingfreaks.MarriageMaster.Bungee.Commands;

import at.pcgamingfreaks.Bungee.Message.Message;
import at.pcgamingfreaks.MarriageMaster.Bungee.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bungee.API.MarryCommand;
import at.pcgamingfreaks.MarriageMaster.Bungee.Listener.PluginChannelCommunicator;
import at.pcgamingfreaks.MarriageMaster.Bungee.MarriageMaster;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class TpCommand extends MarryCommand
{
	private final Set<String> blockedFrom, blockedTo;
	private final boolean delayed;
	private final Message messageBlockedFrom, messageBlockedTo;
	private final PluginChannelCommunicator communicator;

	public TpCommand(MarriageMaster plugin)
	{
		super(plugin, "tp", plugin.getLanguage().getTranslated("Commands.Description.Tp"), "marry.tp", true, true, plugin.getLanguage().getCommandAliases("Tp"));

		blockedFrom = plugin.getConfig().getTPBlackListedServersFrom();
		blockedTo = plugin.getConfig().getTPBlackListedServersTo();
		delayed = plugin.getConfig().isTPDelayed();

		messageBlockedTo    = plugin.getLanguage().getMessage("Ingame.TP.BlockedTo");
		messageBlockedFrom  = plugin.getLanguage().getMessage("Ingame.TP.BlockedFrom");

		communicator = plugin.getPluginChannelCommunicator();
		communicator.setTpCommand(this);
	}

	@Override
	public void close()
	{
		communicator.setTpCommand(null);
	}

	@Override
	public void execute(@NotNull CommandSender sender, @Nullable String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		MarriagePlayer player = getMarriagePlugin().getPlayerData((ProxiedPlayer) sender);
		//noinspection ConstantConditions
		MarriagePlayer partner = (getMarriagePlugin().areMultiplePartnersAllowed() && args.length >= 1) ? player.getPartner(args[0]) : player.getMarriageData().getPartner(player);
		if(partner == null)
		{
			player.send(((MarriageMaster) getMarriagePlugin()).messageTargetPartnerNotFound);
		}
		else if(partner.isOnline())
		{
			if(!blockedFrom.contains(player.getPlayer().getServer().getInfo().getName().toLowerCase()))
			{
				if(!blockedTo.contains(partner.getPlayer().getServer().getInfo().getName().toLowerCase()))
				{
					if(delayed && !sender.hasPermission("marry.bypass.delay"))
					{
						communicator.sendMessage(player.getPlayer().getServer().getInfo(), "delayTP", player.getUUID().toString(), player.getUUID().toString());
					}
					else
					{
						sendTP((ProxiedPlayer) sender, partner.getPlayer());
					}
				}
				else
				{
					player.send(messageBlockedTo);
				}
			}
			else
			{
				player.send(messageBlockedFrom);
			}
		}
		else
		{
			player.send(((MarriageMaster) getMarriagePlugin()).messagePartnerOffline);
		}
	}

	@Override
	public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		return getMarriagePlugin().getCommandManager().getSimpleTabComplete(sender, args);
	}

	public void sendTP(final @NotNull UUID playerUUID, final @NotNull UUID partnerUUID)
	{
		ProxiedPlayer pPlayer = plugin.getProxy().getPlayer(playerUUID);
		if(pPlayer != null)
		{
			MarriagePlayer player = getMarriagePlugin().getPlayerData(pPlayer);
			if(player.isMarried())
			{
				MarriagePlayer partner = player.getPartner(partnerUUID);
				ProxiedPlayer pPartner = (partner != null) ? partner.getPlayer() : null;
				if(pPartner != null)
				{
					sendTP(pPlayer, pPartner);
				}
			}
		}
	}

	public void sendTP(final ProxiedPlayer player, final ProxiedPlayer partner)
	{
		ServerInfo partnerServer = partner.getServer().getInfo();
		if(!player.getServer().getInfo().getName().equals(partnerServer.getName()))
		{
			player.connect(partner.getServer().getInfo());
			plugin.getProxy().getScheduler().schedule(plugin, () -> communicator.sendMessage(partner.getServer().getInfo(), "tp", player.getUniqueId().toString(), partner.getUniqueId().toString()), 1L, TimeUnit.SECONDS);
		}
		else
		{
			communicator.sendMessage(partner.getServer().getInfo(), "tp", player.getUniqueId().toString(), partner.getUniqueId().toString());
		}
	}
}