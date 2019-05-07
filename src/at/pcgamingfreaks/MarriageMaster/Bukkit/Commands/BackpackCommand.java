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
import at.pcgamingfreaks.Command.HelpData;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarryCommand;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;

import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class BackpackCommand extends MarryCommand
{
	private final Message messageOnlyInSurvival, messageShareOff, messageShareOn, messageOpened, messageShareDenied;
	private final String descriptionOn, descriptionOff;
	private final double range;

	public BackpackCommand(MarriageMaster plugin)
	{
		super(plugin, "backpack", plugin.getLanguage().getTranslated("Commands.Description.Backpack"), "marry.backpack", true, plugin.getLanguage().getCommandAliases("Backpack"));

		messageOnlyInSurvival = plugin.getLanguage().getMessage("Ingame.Backpack.OnlyInSurvival");
		messageShareOn        = plugin.getLanguage().getMessage("Ingame.Backpack.ShareOn");
		messageShareOff       = plugin.getLanguage().getMessage("Ingame.Backpack.ShareOff");
		messageOpened         = plugin.getLanguage().getMessage("Ingame.Backpack.Opened");
		messageShareDenied    = plugin.getLanguage().getMessage("Ingame.Backpack.ShareDenied");
		descriptionOn         = plugin.getLanguage().getTranslated("Commands.Description.BackpackOn");
		descriptionOff        = plugin.getLanguage().getTranslated("Commands.Description.BackpackOff");

		range = plugin.getConfiguration().getRangeSquared("Backpack");
	}

	@Override
	public void execute(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		if(((MarriageMaster) getMarriagePlugin()).getBackpacksIntegration() != null)
		{
			MarriagePlayer player = getMarriagePlugin().getPlayerData((Player) sender);
			if(player.isMarried())
			{
				if(args.length == 1)
				{
					// Set backpack parameter
					if(getMarriagePlugin().getCommandManager().isOnSwitch(args[0]))
					{
						player.setShareBackpack(true);
						messageShareOn.send(sender);
					}
					else if(getMarriagePlugin().getCommandManager().isOffSwitch(args[0]))
					{
						player.setShareBackpack(false);
						messageShareOff.send(sender);
					}
					else if(getMarriagePlugin().getCommandManager().isToggleSwitch(args[0]))
					{
						if(player.isSharingBackpack())
						{
							player.setShareBackpack(false);
							messageShareOff.send(sender);
						}
						else
						{
							player.setShareBackpack(true);
							messageShareOn.send(sender);
						}
					}
					else
					{
						if(getMarriagePlugin().isPolygamyAllowed())
						{
							MarriagePlayer partner = getMarriagePlugin().getPlayerData(args[0]);
							if(player.isPartner(partner))
							{
								openBackpack(player, partner);
							}
							else
							{
								((MarriageMaster) getMarriagePlugin()).messageTargetPartnerNotFound.send(sender);
							}
						}
						else
						{
							showHelp(sender, mainCommandAlias);
						}
					}
				}
				else
				{
					if(player.getNearestPartnerMarriageData() != null)
					{
						openBackpack(player, player.getNearestPartnerMarriageData().getPartner(player));
					}
				}
			}
		}
	}

	private void openBackpack(MarriagePlayer opener, MarriagePlayer partner)
	{
		Player sender = opener.getPlayerOnline();
		if(sender.getGameMode() == GameMode.SURVIVAL || sender.hasPermission("marry.bypass.backpackgamemode")) //TODO use minepacks settings
		{
			Player partnerPlayer = partner.getPlayerOnline();
			if(partnerPlayer == null || !partnerPlayer.isOnline())
			{
				((MarriageMaster) getMarriagePlugin()).messagePartnerOffline.send(sender);
			}
			else if(partner.isSharingBackpack())
			{
				if(getMarriagePlugin().isInRangeSquared(sender, partnerPlayer, range))
				{
					((MarriageMaster) getMarriagePlugin()).getBackpacksIntegration().openBackpack(sender, partnerPlayer, true);
					messageOpened.send(partnerPlayer);
				}
				else
				{
					((MarriageMaster) getMarriagePlugin()).messagePartnerNotInRange.send(sender);
				}
			}
			else
			{
				messageShareDenied.send(sender);
			}
		}
		else
		{
			messageOnlyInSurvival.send(sender);
		}
	}

	@Override
	public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		return getMarriagePlugin().getCommandManager().getSimpleTabComplete(sender, args);
	}

	@Override
	public List<HelpData> getHelp(@NotNull CommandSender requester)
	{
		List<HelpData> help = new LinkedList<>();
		MarriagePlayer player = getMarriagePlugin().getPlayerData((Player) requester);
		if(player.isMarried())
		{
			if(player.getPartners().size() > 1)
			{
				help.add(new HelpData(getTranslatedName(), "<" + ((MarriageMaster) getMarriagePlugin()).helpPartnerNameVariable + ">", getDescription()));
			}
			else
			{
				help.add(new HelpData(getTranslatedName(), null, getDescription()));
			}
		}
		if(player.isSharingBackpack())
		{
			help.add(new HelpData(getTranslatedName() + " " + getMarriagePlugin().getCommandManager().getOffSwitchTranslation(), null, descriptionOff));
		}
		else
		{
			help.add(new HelpData(getTranslatedName() + " " + getMarriagePlugin().getCommandManager().getOnSwitchTranslation(), null, descriptionOn));
		}
		return help;
	}
}