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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Placeholder.Replacer;

import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Range;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.Getter;

public class IsInRange extends PlaceholderReplacerBaseBoolean
{
	@Getter private final String name;
	private final double range;

	public IsInRange(final @NotNull MarriageMaster plugin, final @NotNull Range range)
	{
		super(plugin);
		this.name = this.getClass().getSimpleName() + range.name();
		this.range = plugin.getConfiguration().getRangeSquared(range);
	}

	@Override
	protected @Nullable String replaceMarried(MarriagePlayer player)
	{
		//noinspection ConstantConditions
		return toString(player.getNearestPartnerMarriage().inRangeSquared(range));
	}
}