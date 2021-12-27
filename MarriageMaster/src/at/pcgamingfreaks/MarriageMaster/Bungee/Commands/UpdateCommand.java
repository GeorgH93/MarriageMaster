/*
 *   Copyright (C) 2021 GeorgH93
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

package at.pcgamingfreaks.MarriageMaster.Bungee.Commands;

import at.pcgamingfreaks.Bungee.Message.Message;
import at.pcgamingfreaks.MarriageMaster.Bungee.API.MarryCommand;
import at.pcgamingfreaks.MarriageMaster.Bungee.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Permissions;

import net.md_5.bungee.api.CommandSender;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class UpdateCommand extends MarryCommand
{
	private final Message messageCheckingForUpdates, messageUpdated, messageNoUpdate, messageUpdateFail, messageUpdateAvailable;

	public UpdateCommand(MarriageMaster plugin)
	{
		super(plugin, "update", plugin.getLanguage().getTranslated("Commands.Description.Update"), Permissions.UPDATE, plugin.getLanguage().getCommandAliases("Update"));

		messageCheckingForUpdates   = plugin.getLanguage().getMessage("Ingame.Admin.CheckingForUpdates");
		messageUpdated              = plugin.getLanguage().getMessage("Ingame.Admin.Updated");
		messageNoUpdate             = plugin.getLanguage().getMessage("Ingame.Admin.NoUpdate");
		messageUpdateFail           = plugin.getLanguage().getMessage("Ingame.Admin.UpdateFail");
		messageUpdateAvailable      = plugin.getLanguage().getMessage("Ingame.Admin.UpdateAvailable");
	}

	@Override
	public void execute(@NotNull final CommandSender sender, @NotNull String s, @NotNull String s1, @NotNull String[] args)
	{
		messageCheckingForUpdates.send(sender);
		((MarriageMaster) getMarriagePlugin()).getUpdater().update(result -> {
			switch(result)
			{
				case SUCCESS: messageUpdated.send(sender); break;
				case NO_UPDATE: messageNoUpdate.send(sender); break;
				case UPDATE_AVAILABLE: messageUpdateAvailable.send(sender); break;
				default: messageUpdateFail.send(sender); break;
			}
		});
		/*if_not[STANDALONE]*/
		((at.pcgamingfreaks.PluginLib.Bungee.PluginLib) at.pcgamingfreaks.PluginLib.Bungee.PluginLib.getInstance()).getUpdater().update(); // Make the PluginLib to check for updates too
		/*end[STANDALONE]*/
	}

	@Override
	public List<String> tabComplete(@NotNull CommandSender commandSender, @NotNull String s, @NotNull String s1, @NotNull String[] args)
	{
		return null;
	}
}