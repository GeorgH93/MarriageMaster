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

import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarryCommand;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Permissions;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class VersionCommand extends MarryCommand
{
	private final MarriageMaster plugin;

	public VersionCommand(MarriageMaster plugin)
	{
		super(plugin, "version", plugin.getLanguage().getTranslated("Commands.Description.Version"), Permissions.VERSION, plugin.getLanguage().getCommandAliases("Version"));
		this.plugin = plugin;
	}

	@Override
	public void execute(@NotNull final CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		sender.sendMessage("##### Start Marriage Master version info #####");
		sender.sendMessage("Marriage Master: " +  plugin.getDescription().getVersion());
		/*if_not[STANDALONE]*/
		sender.sendMessage("PCGF PluginLib: " +  at.pcgamingfreaks.PluginLib.Bukkit.PluginLib.getInstance().getVersion());
		/*end[STANDALONE]*/
		sender.sendMessage("Server: " +  plugin.getServer().getVersion());
		sender.sendMessage("Java: " + System.getProperty("java.version"));
		if(plugin.getBackpacksIntegration() != null)
		{
			sender.sendMessage(plugin.getBackpacksIntegration().getName() + ": " + plugin.getBackpacksIntegration().getVersion());
		}
		Plugin pl;
		if(plugin.getConfiguration().isEconomyEnabled() && (pl = Bukkit.getPluginManager().getPlugin("Vault")) != null && pl.isEnabled())
		{
			sender.sendMessage("Vault: " + pl.getDescription().getVersion());
		}
		if((pl = Bukkit.getPluginManager().getPlugin("MVdWPlaceholderAPI")) != null && pl.isEnabled())
		{
			sender.sendMessage("MVdWPlaceholderAPI: " + pl.getDescription().getVersion());
		}
		if((pl = Bukkit.getPluginManager().getPlugin("PlaceholderAPI")) != null && pl.isEnabled())
		{
			sender.sendMessage("PlaceholderAPI: " + pl.getDescription().getVersion());
		}
		if(plugin.getConfiguration().isSkillApiBonusXPEnabled() && (pl = Bukkit.getPluginManager().getPlugin("SkillAPI")) != null && pl.isEnabled())
		{
			sender.sendMessage("SkillAPI: " + pl.getDescription().getVersion());
		}
		if(plugin.getConfiguration().isMcMMOBonusXPEnabled() && (pl = Bukkit.getPluginManager().getPlugin("mcMMO")) != null && pl.isEnabled())
		{
			sender.sendMessage("mcMMO: " + pl.getDescription().getVersion());
		}
		sender.sendMessage("#####  End Marriage Master version info  #####");
	}

	@Override
	public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		return null;
	}
}