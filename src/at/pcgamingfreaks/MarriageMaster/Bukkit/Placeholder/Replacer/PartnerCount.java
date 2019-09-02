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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Placeholder.Replacer;

import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Placeholder.PlaceholderFormatted;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Placeholder.PlaceholderName;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Placeholder.Replacer.Formatted.PartnerCountFormatted;

import org.bukkit.OfflinePlayer;

@PlaceholderName(aliases = "Partner_Count")
@PlaceholderFormatted(formattedClass = PartnerCountFormatted.class)
public class PartnerCount extends PlaceholderReplacerBaseValue
{
	public PartnerCount(MarriageMaster plugin)
	{
		super(plugin);
	}

	@Override
	public String replace(OfflinePlayer player)
	{
		MarriagePlayer playerData = plugin.getPlayerData(player);
		return playerData.getPartners().size() + "";
	}
}