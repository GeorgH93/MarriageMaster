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

import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;

import org.jetbrains.annotations.NotNull;

public abstract class PlaceholderReplacerBaseValueHome extends PlaceholderReplacerBaseValue
{
	private static final String PLACEHOLDER_NO_HOME_KEY = "NoHome";
	protected final String valueNoHome;

	public PlaceholderReplacerBaseValueHome(@NotNull MarriageMaster plugin)
	{
		super(plugin);
		valueNoHome = getPlaceholderValue(PLACEHOLDER_NO_HOME_KEY);
	}
}