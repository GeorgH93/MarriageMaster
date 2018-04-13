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
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarryCommand;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.Updater.UpdateResult;
import at.pcgamingfreaks.Updater.Updater;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class UpdateCommand extends MarryCommand
{
	private final Message messageCheckingForUpdates, messageUpdated, messageNoUpdate, messageUpdateFail, messageUpdateAvailable;

	public UpdateCommand(MarriageMaster plugin)
	{
		super(plugin, "update", plugin.getLanguage().getTranslated("Commands.Description.Update"), "marry.update", plugin.getLanguage().getCommandAliases("Update"));

		messageCheckingForUpdates   = plugin.getLanguage().getMessage("Ingame.Admin.CheckingForUpdates");
		messageUpdated              = plugin.getLanguage().getMessage("Ingame.Admin.Updated");
		messageNoUpdate             = plugin.getLanguage().getMessage("Ingame.Admin.NoUpdate");
		messageUpdateFail           = plugin.getLanguage().getMessage("Ingame.Admin.UpdateFail");
		messageUpdateAvailable      = plugin.getLanguage().getMessage("Ingame.Admin.UpdateAvailable");
	}

	@Override
	public void execute(@NotNull final CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		messageCheckingForUpdates.send(sender);
		((MarriageMaster) getMarriagePlugin()).update(new Updater.UpdaterResponse() {
			@Override
			public void onDone(UpdateResult result)
			{
				switch(result)
				{
					case SUCCESS: messageUpdated.send(sender); break;
					case NO_UPDATE: messageNoUpdate.send(sender); break;
					case UPDATE_AVAILABLE: messageUpdateAvailable.send(sender); break;
					default: messageUpdateFail.send(sender); break;
				}
			}
		});
	}

	@Override
	public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		return null;
	}
}