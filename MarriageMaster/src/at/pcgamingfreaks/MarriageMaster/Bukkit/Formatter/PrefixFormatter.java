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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Formatter;

import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;

import org.jetbrains.annotations.NotNull;

public class PrefixFormatter implements IMarriageAndPartnerFormatter
{
	private final String format;

	public PrefixFormatter(final @NotNull String format)
	{
		this.format = format;
	}

	@Override
	public @NotNull String format(final @NotNull Marriage marriage, final @NotNull MarriagePlayer partner)
	{
		return String.format(format, marriage.getSurnameString(), partner.getName(), partner.getDisplayName(), HEART_RED, marriage.getMarriageColor() + HEART);
	}
}