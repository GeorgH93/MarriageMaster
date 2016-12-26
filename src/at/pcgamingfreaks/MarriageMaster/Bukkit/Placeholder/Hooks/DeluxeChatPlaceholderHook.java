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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Placeholder.Hooks;

import at.pcgamingfreaks.ConsoleColor;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Placeholder.PlaceholderManager;

import org.bukkit.entity.Player;

import me.clip.deluxechat.placeholders.DeluxePlaceholderHook;
import me.clip.deluxechat.placeholders.PlaceholderHandler;

public class DeluxeChatPlaceholderHook extends DeluxePlaceholderHook implements PlaceholderAPIHook
{
	private PlaceholderManager placeholderManager;
	private MarriageMaster plugin;

	public DeluxeChatPlaceholderHook(MarriageMaster plugin, PlaceholderManager placeholderManager)
	{
		this.plugin = plugin;
		this.placeholderManager = placeholderManager;
		//noinspection ConstantConditions
		if(PlaceholderHandler.registerPlaceholderHook(plugin, this))
		{
			plugin.getLogger().info(ConsoleColor.GREEN + "DeluxeChat placeholder hook was successfully registered!" + ConsoleColor.RESET);
		}
		else
		{
			plugin.getLogger().info(ConsoleColor.RED + "DeluxeChat placeholder hook failed to registered!" + ConsoleColor.RESET);
		}
	}

	@Override
	public String onPlaceholderRequest(Player player, String identifier)
	{
		return placeholderManager.replacePlaceholder(player, identifier);
	}

	@Override
	public void close()
	{
		PlaceholderHandler.unregisterPlaceholderHook(plugin);
	}
}