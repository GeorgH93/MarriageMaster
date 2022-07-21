/*
 *   Copyright (C) 2022 GeorgH93
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
import at.pcgamingfreaks.Bukkit.Message.Sender.SendMethod;
import at.pcgamingfreaks.Bukkit.Util.Utils;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarryCommand;
import at.pcgamingfreaks.MarriageMaster.Bukkit.CommonMessages;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Database.MarriagePlayerData;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Permissions;
import at.pcgamingfreaks.MarriageMaster.Placeholder.Placeholders;
import at.pcgamingfreaks.Message.MessageComponent;
import at.pcgamingfreaks.Message.Placeholder.Processors.PassthroughMessageComponentPlaceholderProcessor;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PartnersCommand extends MarryCommand
{
	private final Message messageHeadlineMain, messageFooter, messageListFormat, messageNotMarried;

	public PartnersCommand(final @NotNull MarriageMaster plugin)
	{
		super(plugin, "partners", plugin.getLanguage().getTranslated("Commands.Description.Partners"), Permissions.PARTNERS, plugin.getLanguage().getCommandAliases("Partners"));

		messageListFormat         = plugin.getLanguage().getMessage("Ingame.Partners.Format").placeholder("Player1Name").placeholder("Player2Name").placeholder("Player1DisplayName", PassthroughMessageComponentPlaceholderProcessor.INSTANCE).placeholder("Player2DisplayName", PassthroughMessageComponentPlaceholderProcessor.INSTANCE).placeholder("Surname").placeholder("MagicHeart");
		messageHeadlineMain       = plugin.getLanguage().getMessage("Ingame.Partners.Headline").placeholders(Placeholders.PLAYER_NAME);
		messageFooter             = plugin.getLanguage().getMessage("Ingame.Partners.Footer").placeholders(Placeholders.PLAYER_NAME);
		messageNotMarried         = plugin.getLanguage().getMessage("Ingame.Partners.NotMarried").placeholders(Placeholders.PLAYER_NAME);

		if (!plugin.getConfiguration().useListFooter())
		{
			messageFooter.setSendMethod(SendMethod.DISABLED);
		}
	}

	@Override
	public void execute(final @NotNull CommandSender sender, final @NotNull String mainCommandAlias, final @NotNull String alias, final @NotNull String[] args)
	{
		boolean self = false;
		MarriagePlayer filterPlayer;
		if (args.length > 0 && sender.hasPermission(Permissions.PARTNERS_OTHERS))
		{
			filterPlayer = getMarriagePlugin().getPlayerData(args[0]);
		}
		else if (sender instanceof Player)
		{
			filterPlayer = getMarriagePlugin().getPlayerData((Player) sender);
			self = true;
		}
		else
		{
			showHelp(sender, mainCommandAlias);
			return;
		}

		if (filterPlayer.isMarried())
		{
			messageHeadlineMain.send(sender, filterPlayer);
			for(Marriage couple : filterPlayer.getMultiMarriageData())
			{
				MarriagePlayer partner = couple.getPartner(filterPlayer);
				if (partner == null) continue;
				MessageComponent p1DName = ((MarriagePlayerData) filterPlayer).getDisplayNameMessageComponentCheckVanished(sender);
				MessageComponent p2DName = ((MarriagePlayerData) partner).getDisplayNameMessageComponentCheckVanished(sender);
				messageListFormat.send(sender, filterPlayer.getName(), partner.getName(), p1DName, p2DName, couple.getSurnameString(), couple.getMagicHeart());
			}
			messageFooter.send(sender, filterPlayer);
		}
		else
		{
			if (self)
			{
				CommonMessages.getMessageNotMarried().send(sender);
			}
			else
			{
				messageNotMarried.send(sender, filterPlayer);
			}
		}
	}

	@Override
	public List<String> tabComplete(final @NotNull CommandSender sender, final @NotNull String mainCommandAlias, final @NotNull String alias, final @NotNull String[] args)
	{
		if (sender.hasPermission(Permissions.PARTNERS_OTHERS) && args.length == 1)
		{
			return Utils.getPlayerNamesStartingWithVisibleOnly(args[0], sender, Permissions.BYPASS_VANISH);
		}
		return EMPTY_TAB_COMPLETE_LIST;
	}
}