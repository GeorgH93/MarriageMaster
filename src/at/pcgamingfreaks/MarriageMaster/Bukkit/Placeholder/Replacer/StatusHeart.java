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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Placeholder.Replacer;

import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

public class StatusHeart extends PlaceholderReplacerBase
{
	private static final String heartRed = ChatColor.RED + "\u2764" + ChatColor.WHITE, heartGray = ChatColor.GRAY + "\u2764" + ChatColor.WHITE;

	public StatusHeart(MarriageMaster plugin)
	{
		super(plugin);
	}

	@Override
	public String replace(OfflinePlayer player)
	{
		return plugin.getPlayerData(player).isMarried() ? heartRed : heartGray;
	}
}