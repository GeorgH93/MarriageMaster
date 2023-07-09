/*
 *   Copyright (C) 2023 GeorgH93
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

import at.pcgamingfreaks.Bukkit.Placeholder.PlaceholderName;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Placeholder.PlaceholderFormatted;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Placeholder.Replacer.Formatted.HomeZFormatted;

import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;

@PlaceholderName(aliases = "Home_Z")
@PlaceholderFormatted(formattedClass = HomeZFormatted.class)
public class HomeZ extends PlaceholderReplacerBaseValueHome
{
	ThreadLocal<DecimalFormat> format = ThreadLocal.withInitial(() -> new DecimalFormat("#.0"));

	public HomeZ(MarriageMaster plugin)
	{
		super(plugin);
	}

	@Override
	protected @Nullable String replaceMarried(MarriagePlayer player)
	{
		Marriage marriageData = player.getMarriageData();
		//noinspection ConstantConditions
		return marriageData.isHomeSet() ? format.get().format(marriageData.getHome().getLocation().getZ()) : valueNoHome;
	}
}