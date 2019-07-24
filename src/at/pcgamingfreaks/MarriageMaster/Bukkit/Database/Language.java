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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Database;

import at.pcgamingfreaks.MarriageMaster.Bukkit.Database.Helper.OldFileUpdater;
import at.pcgamingfreaks.MarriageMaster.Database.ILanguage;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class Language extends at.pcgamingfreaks.Bukkit.Language implements ILanguage
{
	private static final int LANG_VERSION = 93, UPGRADE_THRESHOLD = 93, PRE_V2_VERSIONS = 90;

	public Language(@NotNull JavaPlugin plugin)
	{
		super(plugin, LANG_VERSION, UPGRADE_THRESHOLD);
	}

	@Override
	protected void doUpdate() {}

	@Override
	protected void doUpgrade(@NotNull at.pcgamingfreaks.YamlFileManager oldLang)
	{
		if(oldLang.getVersion() < PRE_V2_VERSIONS)
		{
			OldFileUpdater.updateLanguage(oldLang.getYamlE(), getLang());
		}
		else
		{
			super.doUpgrade(oldLang);
		}
	}

	@Override
	public @NotNull String getTranslated(final @NotNull String key)
	{
		return super.getTranslated(key).replaceAll("<heart>", ChatColor.RED + "\u2764").replaceAll("<smallheart>", ChatColor.RED + "\u2665");
	}

	@Override
	public @NotNull String getTranslatedPlaceholder(final @NotNull String key)
	{
		return ChatColor.translateAlternateColorCodes('&', getLangE().getString("Placeholders." + key, "&cPlaceholder not found")).replaceAll("<heart>", ChatColor.RED + "\u2764").replaceAll("<smallheart>", ChatColor.RED + "\u2665");
	}

	@Override
	public @NotNull String getDialog(final @NotNull String key)
	{
		return getLangE().getString("Dialog." + key, "").replaceAll("<heart>", ChatColor.RED + "\u2764").replaceAll("<smallheart>", ChatColor.RED + "\u2665");
	}
}