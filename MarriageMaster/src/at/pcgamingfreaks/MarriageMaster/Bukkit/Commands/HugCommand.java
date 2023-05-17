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

import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.Bukkit.Particles.Particle;
import at.pcgamingfreaks.Bukkit.Particles.ParticleSpawner;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.HugEvent;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarryCommand;
import at.pcgamingfreaks.MarriageMaster.Bukkit.CommonMessages;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Range;
import at.pcgamingfreaks.MarriageMaster.Permissions;
import at.pcgamingfreaks.Message.Placeholder.Processors.FloatPlaceholderProcessor;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import lombok.Getter;

import java.util.List;

public class HugCommand extends MarryCommand
{
	@Getter private static HugCommand instance;

	private final Message messageHugged, messageGotHugged, messageTooFarAway, messageWait;
	private final double range, rangeSquared, visibleRange;
	private final int waitTime, particleCount;
	private final ParticleSpawner particleSpawner;

	public HugCommand(MarriageMaster plugin)
	{
		super(plugin, "hug", plugin.getLanguage().getTranslated("Commands.Description.Hug"), Permissions.HUG, true, false, plugin.getLanguage().getCommandAliases("Hug"));

		range             = plugin.getConfiguration().getRange(Range.Hug);
		rangeSquared      = plugin.getConfiguration().getRangeSquared(Range.Hug);
		messageHugged     = plugin.getLanguage().getMessage("Ingame.Hug.Hugged");
		messageGotHugged  = plugin.getLanguage().getMessage("Ingame.Hug.GotHugged");
		FloatPlaceholderProcessor singleDecimalPointProcessor = new FloatPlaceholderProcessor(1);
		messageTooFarAway = plugin.getLanguage().getMessage("Ingame.Hug.TooFarAway").placeholder("Distance", singleDecimalPointProcessor);
		messageWait       = plugin.getLanguage().getMessage("Ingame.Hug.Wait").placeholder("Time").placeholder("TimeLeft", singleDecimalPointProcessor);
		waitTime          = plugin.getConfiguration().getHugWaitTime();
		particleCount     = plugin.getConfiguration().getHugParticleCount();
		visibleRange      = plugin.getConfiguration().getRange(Range.HugParticleVisible);

		particleSpawner = (particleCount > 0) ? ParticleSpawner.getParticleSpawner() : null;
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
		else if(partner.isOnline() && partner.getPlayerOnline() != null)
		{
			if(getMarriagePlugin().isInRangeSquared((Player) sender, partner.getPlayerOnline(), rangeSquared))
			{
				hug(player, partner);
			}
			else
			{
				messageTooFarAway.send(sender, range);
			}
		}
		else
		{
			CommonMessages.getMessagePartnerOffline().send(sender);
		}
	}

	@Override
	public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		return getMarriagePlugin().getCommandManager().getSimpleTabComplete(sender, args);
	}

	private boolean canHugAgain(MarriagePlayer player)
	{
		return player.getLastHugTime() < System.currentTimeMillis() - waitTime || player.hasPermission(Permissions.BYPASS_DELAY);
	}

	public void hug(MarriagePlayer player, MarriagePlayer partner)
	{
		if(canHugAgain(player))
		{
			HugEvent event = new HugEvent(player, player.getMarriageData(partner));
			Bukkit.getPluginManager().callEvent(event);
			if(!event.isCancelled())
			{
				player.setLastHugTime(System.currentTimeMillis());
				player.sendMessage(messageHugged);
				partner.sendMessage(messageGotHugged);

				if(particleSpawner != null)
				{
					particleSpawner.spawnParticle(player.getPlayerOnline().getLocation(), Particle.VILLAGER_HAPPY, visibleRange, particleCount, 1.0F, 1.0F, 1.0F, 1.0F);
					particleSpawner.spawnParticle(partner.getPlayerOnline().getLocation(), Particle.VILLAGER_HAPPY, visibleRange, particleCount, 1.0F, 1.0F, 1.0F, 1.0F);
				}
			}
		}
		else
		{
			player.send(messageWait, waitTime / 1000, (player.getLastHugTime() - System.currentTimeMillis() + waitTime) / 1000.0);
		}
	}
}