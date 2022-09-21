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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ReloadCommand extends MarryCommand
{
	private final String DATABASE = "database";
	private final Message messageReloading, messageReloaded, messageReloadingDatabase;

	public ReloadCommand(MarriageMaster plugin)
	{
		super(plugin, "breload", plugin.getLanguage().getTranslated("Commands.Description.Reload"), Permissions.RELOAD, plugin.getLanguage().getCommandAliases("BReload"));

		// Load messages
		messageReloading = plugin.getLanguage().getMessage("Ingame.Admin.Reloading");
		messageReloaded  = plugin.getLanguage().getMessage("Ingame.Admin.Reloaded");
		messageReloadingDatabase = plugin.getLanguage().getMessage("Ingame.Admin.ReloadingDatabase");
	}

	@Override
	public void execute(@NotNull final CommandSender sender, @NotNull String s, @NotNull String s1, @NotNull String[] args)
	{
		if(args.length == 0)
		{
			messageReloading.send(sender);
			((MarriageMaster) getMarriagePlugin()).reload();
			messageReloaded.send(sender);
		}
		if (args.length == 1 && args[0].equalsIgnoreCase(DATABASE))
		{
			messageReloadingDatabase.send(sender);
			((MarriageMaster) getMarriagePlugin()).getDatabase().resync();
			plugin.getProxy().getScheduler().schedule(plugin, () -> messageReloaded.send(sender), 1, TimeUnit.SECONDS);
		}
	}

	@Override
	public List<String> tabComplete(@NotNull CommandSender commandSender, @NotNull String s, @NotNull String s1, @NotNull String[] args)
	{
		if(args.length == 1 && DATABASE.startsWith(args[0]))
		{
			List<String> tab = new ArrayList<>(1);
			tab.add(DATABASE);
			return tab;
		}
		return null;
	}
}