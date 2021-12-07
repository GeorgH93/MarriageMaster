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
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.MarriageMaster.Bukkit.Database;

import at.pcgamingfreaks.MarriageMaster.Bukkit.Database.Helper.OldFileUpdater;
import at.pcgamingfreaks.MarriageMaster.Database.ILanguage;
import at.pcgamingfreaks.MarriageMaster.MagicValues;
import at.pcgamingfreaks.Message.MessageColor;
import at.pcgamingfreaks.Version;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class Language extends at.pcgamingfreaks.Bukkit.Language implements ILanguage
{
	public static final String NO_PLACEHOLDER = "&cMarriage Master placeholder not found! Check your language file!";
	private static final String PLACEHOLDERS_KEY = "Placeholders.", HEART = MagicValues.SYMBOL_HEART, SMALLHEART = MagicValues.SYMBOL_SMALL_HEART;

	public Language(@NotNull JavaPlugin plugin)
	{
		super(plugin, new Version(MagicValues.LANG_VERSION), new Version(MagicValues.LANG_VERSION));
	}

	@Override
	protected void doUpdate() {}

	@Override
	protected void doUpgrade(@NotNull at.pcgamingfreaks.YamlFileManager oldLang)
	{
		if(oldLang.version().olderThan(new Version(MagicValues.LANG_PRE_V2_VERSIONS)))
		{
			OldFileUpdater.updateLanguage(oldLang.getYamlE(), getLang());
		}
		else
		{
			Map<String, String> remapping = new HashMap<>();
			remapping.put("Command.Main", "Command.Marry");
			super.doUpgrade(oldLang, remapping);
			if(oldLang.version().olderThan(new Version(110)))
			{
				fixListFooter("Language.Ingame.List.Footer");
				fixListFooter("Language.Ingame.ListPriests.Footer");
			}
		}
	}

	private void fixListFooter(String listFooterKey)
	{
		String listFooter = getLangE().getString(listFooterKey, null);
		if(listFooter != null && listFooter.startsWith("[{"))
		{
			getLangE().set(listFooterKey, "[\"\"," + listFooter.substring(1));
		}
	}

	@Override
	public @NotNull String translateColorCodes(@NotNull String string)
	{
		return super.translateColorCodes(string).replaceAll("<heart>", MessageColor.RED + HEART).replaceAll("<smallheart>", MessageColor.RED + SMALLHEART);
	}

	public boolean isPlaceholderSet(final @NotNull String key)
	{
		return getLangE().isSet(PLACEHOLDERS_KEY + key);
	}

	@Override
	public @NotNull String getTranslatedPlaceholder(final @NotNull String key)
	{
		return MessageColor.translateAlternateColorCodes('&', getLangE().getString(PLACEHOLDERS_KEY + key, NO_PLACEHOLDER)).replaceAll("<heart>", HEART).replaceAll("<smallheart>", SMALLHEART);
	}

	@Override
	public @NotNull String getDialog(final @NotNull String key)
	{
		return getLangE().getString("Dialog." + key, "").replaceAll("<heart>", MessageColor.RED + HEART).replaceAll("<smallheart>", MessageColor.RED + SMALLHEART);
	}
}