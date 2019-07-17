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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Commands;

import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.HugEvent;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarryCommand;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HugCommand extends MarryCommand
{
	private final Message messageHugged, messageGotHugged, messageTooFarAway;
	private final double range, rangeSquared;

	public HugCommand(MarriageMaster plugin)
	{
		super(plugin, "hug", plugin.getLanguage().getTranslated("Commands.Description.Hug"), "marry.hug", true, false, plugin.getLanguage().getCommandAliases("Hug"));

		range             = plugin.getConfiguration().getRange("Hug");
		rangeSquared      = plugin.getConfiguration().getRangeSquared("Hug");
		messageHugged     = plugin.getLanguage().getMessage("Ingame.Hug.Hugged");
		messageGotHugged  = plugin.getLanguage().getMessage("Ingame.Hug.GotHugged");
		messageTooFarAway = plugin.getLanguage().getMessage("Ingame.Hug.TooFarAway").replaceAll("\\{Distance\\}", "%.1f");
	}

	@Override
	public void execute(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		MarriagePlayer player = getMarriagePlugin().getPlayerData((Player) sender);
		MarriagePlayer partner = (getMarriagePlugin().areMultiplePartnersAllowed() && args.length >= 1) ? player.getPartner(args[0]) : player.getNearestPartnerMarriageData().getPartner(player);
		if(partner == null)
		{
			player.sendMessage(((MarriageMaster) getMarriagePlugin()).messageTargetPartnerNotFound);
		}
		else if(partner.isOnline() && partner.getPlayerOnline() != null)
		{
			if(getMarriagePlugin().isInRangeSquared((Player) sender, partner.getPlayerOnline(), rangeSquared))
			{
				HugEvent event = new HugEvent(player, player.getMarriageData(partner));
				Bukkit.getPluginManager().callEvent(event);
				if(!event.isCancelled())
				{
					player.sendMessage(messageHugged);
					partner.sendMessage(messageGotHugged);
				}
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
}