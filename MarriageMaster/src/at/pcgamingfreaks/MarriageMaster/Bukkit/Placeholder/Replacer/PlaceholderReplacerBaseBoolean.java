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

import java.util.Locale;

public abstract class PlaceholderReplacerBaseBoolean extends PlaceholderReplacerBase
{
	private static final String PLACEHOLDER_BOOLEAN_KEY = "Boolean.", KEY_TRUE = "True", KEY_FALSE = "False";
	protected final String valueTrue, valueFalse;

	public PlaceholderReplacerBaseBoolean(final @NotNull MarriageMaster plugin)
	{
		super(plugin);
		valueTrue = getBooleanPlaceholderValue(this.getClass().getSimpleName(), KEY_TRUE);
		valueFalse = getBooleanPlaceholderValue(this.getClass().getSimpleName(), KEY_FALSE);
	}

	protected @NotNull String getBooleanPlaceholderValue(final @NotNull String placeholder, final @NotNull String booleanKey)
	{
		String val = getPlaceholderValue(placeholder, PLACEHOLDER_BOOLEAN_KEY + booleanKey);
		return (val == null) ? booleanKey.toLowerCase(Locale.ENGLISH) : val;
	}

	protected @NotNull String toString(boolean bool)
	{
		return bool ? valueTrue : valueFalse;
	}
}