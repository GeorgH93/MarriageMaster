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

import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;

import org.jetbrains.annotations.Nullable;

public class PartnerDisplayNameList extends PlaceholderReplacerBaseValue
{
	private final String valueSeparator;

	public PartnerDisplayNameList(MarriageMaster plugin)
	{
		super(plugin);
		valueSeparator = getNotMarriedPlaceholderValue("Separator");
	}

	@Override
	protected @Nullable String replaceMarried(MarriagePlayer player)
	{
		StringBuilder stringBuilder = new StringBuilder();
		String separator = "";
		for(Marriage marriage : player.getMultiMarriageData())
		{
			//noinspection ConstantConditions
			stringBuilder.append(separator).append(marriage.getPartner(player).getDisplayName());
			separator = valueSeparator;
		}
		return stringBuilder.toString();
	}
}