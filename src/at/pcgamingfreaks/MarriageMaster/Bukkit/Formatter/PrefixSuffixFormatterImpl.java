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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Formatter;

import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

public class PrefixSuffixFormatterImpl implements at.pcgamingfreaks.MarriageMaster.Bukkit.API.PrefixSuffixFormatter
{
	private final IMarriageAndPartnerFormatter prefixFormatter, suffixFormatter;

	public PrefixSuffixFormatterImpl(MarriageMaster plugin)
	{
		prefixFormatter = producePrefixFormatter(plugin.getConfiguration().getPrefix());
		suffixFormatter = produceSuffixFormatter(plugin.getConfiguration().getSuffix());
	}

	public static IMarriageAndPartnerFormatter producePrefixFormatter(final @NotNull String prefixFormat)
	{
		if(StringUtils.indexOfAny(prefixFormat, new String[]{ "{Surname}", "{PartnerName}", "{PartnerDisplayName}", "{StatusHeart}", "{MagicHeart}" }) != -1)
		{
			return new PrefixFormatter(prefixFormat.replaceAll("\\{Surname}", "%1\\$s").replaceAll("\\{PartnerName}", "%2\\$s").replaceAll("\\{PartnerDisplayName}", "%3\\$s")
					                                               .replaceAll("\\{StatusHeart}", "%4\\$s").replaceAll("\\{MagicHeart}", "%5\\$s"));
		}
		else return new StaticStringFormatter(prefixFormat);
	}

	public static IMarriageAndPartnerFormatter produceSuffixFormatter(final @NotNull String suffixFormat)
	{
		if(StringUtils.indexOfAny(suffixFormat, new String[]{ "{Surname}", "{PartnerName}", "{PartnerDisplayName}" }) != -1)
		{
			return new SuffixFormatter(suffixFormat.replaceAll("\\{Surname}", "%1\\$s").replaceAll("\\{PartnerName}", "%2\\$s").replaceAll("\\{PartnerDisplayName}", "%3\\$s"));
		}
		else return new StaticStringFormatter(suffixFormat);
	}

	@Override
	public String formatPrefix(final @NotNull Marriage marriage, final @NotNull MarriagePlayer partner)
	{
		return prefixFormatter.format(marriage, partner);
	}

	@Override
	public String formatSuffix(final @NotNull Marriage marriage, final @NotNull MarriagePlayer partner)
	{
		return suffixFormatter.format(marriage, partner);
	}
}