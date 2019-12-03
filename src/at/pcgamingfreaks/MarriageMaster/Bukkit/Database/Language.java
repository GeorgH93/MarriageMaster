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
	private static final int LANG_VERSION = 97, UPGRADE_THRESHOLD = 97, PRE_V2_VERSIONS = 90;
	private static final String PLACEHOLDERS_KEY = "Placeholders.", HEART = "\u2764", SMALLHEART = "\u2665";

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
		return super.getTranslated(key).replaceAll("<heart>", ChatColor.RED + HEART).replaceAll("<smallheart>", ChatColor.RED + SMALLHEART);
	}

	public boolean isPlaceholderSet(final @NotNull String key)
	{
		return getLangE().isSet(PLACEHOLDERS_KEY + key);
	}

	@Override
	public @NotNull String getTranslatedPlaceholder(final @NotNull String key)
	{
		return ChatColor.translateAlternateColorCodes('&', getLangE().getString(PLACEHOLDERS_KEY + key, "&cPlaceholder not found")).replaceAll("<heart>", HEART).replaceAll("<smallheart>", SMALLHEART);
	}

	@Override
	public @NotNull String getDialog(final @NotNull String key)
	{
		return getLangE().getString("Dialog." + key, "").replaceAll("<heart>", ChatColor.RED + HEART).replaceAll("<smallheart>", ChatColor.RED + SMALLHEART);
	}
}