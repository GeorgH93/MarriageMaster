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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Commands;

import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarryCommand;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Permissions;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ReloadCommand extends MarryCommand
{
	private final Message messageReloading, messageReloaded, messageReloadingDatabase;

	public ReloadCommand(MarriageMaster plugin)
	{
		super(plugin, "reload", plugin.getLanguage().getTranslated("Commands.Description.Reload"), Permissions.RELOAD, plugin.getLanguage().getCommandAliases("Reload"));

		// Load messages
		messageReloading = plugin.getLanguage().getMessage("Ingame.Admin.Reloading");
		messageReloaded  = plugin.getLanguage().getMessage("Ingame.Admin.Reloaded");
		messageReloadingDatabase = plugin.getLanguage().getMessage("Ingame.Admin.ReloadingDatabase");
	}

	@Override
	public void execute(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		if(args.length == 0)
		{
			messageReloading.send(sender);
			((MarriageMaster) getMarriagePlugin()).reload();
			messageReloaded.send(sender);
		}
		if (args.length == 1 && args[0].equalsIgnoreCase("database"))
		{
			messageReloadingDatabase.send(sender);
			((MarriageMaster) getMarriagePlugin()).getDatabase().resync();
			plugin.getServer().getScheduler().runTaskLater(plugin, () -> messageReloaded.send(sender), 20);
		}
	}

	@Override
	public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		if(args.length == 1 && "database".startsWith(args[0]))
		{
			List<String> tab = new ArrayList<>(1);
			tab.add("database");
			return tab;
		}
		return null;
	}
}