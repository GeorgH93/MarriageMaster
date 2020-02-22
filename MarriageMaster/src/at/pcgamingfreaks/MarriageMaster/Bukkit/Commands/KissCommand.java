/*
 *   Copyright (C) 2020 GeorgH93
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

import at.pcgamingfreaks.Bukkit.MCVersion;
import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.Bukkit.Particles.Particle;
import at.pcgamingfreaks.Bukkit.Particles.ParticleSpawner;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.KissEvent;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarryCommand;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KissCommand extends MarryCommand implements Listener
{
	private final Message messageKissed, messageGotKissed, messageTooFarAway, messageWait;
	private final double interactRange, range, rangeSquared, hearthVisibleRange;
	private final int waitTime, hearthCount;
	private Map<MarriagePlayer, Long> wait;
	private ParticleSpawner particleSpawner;

	public KissCommand(MarriageMaster plugin)
	{
		super(plugin, "kiss", plugin.getLanguage().getTranslated("Commands.Description.Kiss"), "marry.kiss", true, false, plugin.getLanguage().getCommandAliases("Kiss"));

		wait = new HashMap<>();
		particleSpawner = ParticleSpawner.getParticleSpawner();

		range              = plugin.getConfiguration().getRange("Kiss");
		rangeSquared       = plugin.getConfiguration().getRangeSquared("Kiss");
		interactRange      = plugin.getConfiguration().getRangeSquared("KissInteract");
		hearthVisibleRange = plugin.getConfiguration().getRange("HearthVisible");
		waitTime           = plugin.getConfiguration().getKissWaitTime();
		hearthCount        = plugin.getConfiguration().getKissHearthCount();

		messageKissed     = plugin.getLanguage().getMessage("Ingame.Kiss.Kissed");
		messageGotKissed  = plugin.getLanguage().getMessage("Ingame.Kiss.GotKissed");
		messageTooFarAway = plugin.getLanguage().getMessage("Ingame.Kiss.TooFarAway").replaceAll("\\{Distance\\}", "%.1f");
		messageWait       = plugin.getLanguage().getMessage("Ingame.Kiss.Wait").replaceAll("\\{Time\\}", "%1\\$d").replaceAll("\\{TimeLeft\\}", "%2\\$.1f");

		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@Override
	public void close()
	{
		particleSpawner = null;
		wait.clear();
		wait = null;
	}

	@Override
	public void execute(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		MarriagePlayer player = getMarriagePlugin().getPlayerData((Player) sender);
		//noinspection ConstantConditions
		MarriagePlayer partner = (getMarriagePlugin().areMultiplePartnersAllowed() && args.length >= 1) ? player.getPartner(args[0]) : player.getNearestPartnerMarriageData().getPartner(player);
		if(partner == null)
		{
			((MarriageMaster) getMarriagePlugin()).messageTargetPartnerNotFound.send(sender);
		}
		else if(partner.isOnline() && partner.getPlayerOnline() != null)
		{
			if(getMarriagePlugin().isInRangeSquared((Player) sender, partner.getPlayerOnline(), rangeSquared))
			{
				kiss(player, partner);
			}
			else
			{
				messageTooFarAway.send(sender, range);
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

	public void kiss(MarriagePlayer player, MarriagePlayer partner)
	{
		Long time = wait.get(player);
		if(time == null || time < System.currentTimeMillis() - waitTime)
		{
			//noinspection ConstantConditions
			KissEvent event = new KissEvent(player, player.getMarriageData(partner));
			Bukkit.getPluginManager().callEvent(event);
			if(!event.isCancelled())
			{
				if(!player.hasPermission("marry.bypass.delay")) wait.put(player, System.currentTimeMillis());
				player.send(messageKissed);
				partner.send(messageGotKissed);
				if(particleSpawner != null)
				{
					particleSpawner.spawnParticle(player.getPlayerOnline().getLocation(), Particle.HEART, hearthVisibleRange, hearthCount, 1.0F, 1.0F, 1.0F, 1.0F);
					particleSpawner.spawnParticle(partner.getPlayerOnline().getLocation(), Particle.HEART, hearthVisibleRange, hearthCount, 1.0F, 1.0F, 1.0F, 1.0F);
				}
			}
		}
		else
		{
			player.send(messageWait, waitTime / 1000, (time - System.currentTimeMillis() + waitTime) / 1000.0);
		}
	}

	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event)
	{
		if((!MCVersion.isDualWieldingMC() || event.getHand().equals(EquipmentSlot.HAND)) && event.getPlayer().isSneaking() && event.getPlayer().hasPermission("marry.kiss") && event.getRightClicked() instanceof Player)
		{
			MarriagePlayer player = getMarriagePlugin().getPlayerData(event.getPlayer());
			Long time = wait.get(player);
			if(time == null || time < System.currentTimeMillis() - waitTime)
			{
				MarriagePlayer player2 = getMarriagePlugin().getPlayerData((Player) event.getRightClicked());
				if(player.isPartner(player2) && getMarriagePlugin().isInRangeSquared(event.getPlayer(), (Player) event.getRightClicked(), interactRange))
				{
					kiss(player, player2);
				}
			}
		}
	}
}