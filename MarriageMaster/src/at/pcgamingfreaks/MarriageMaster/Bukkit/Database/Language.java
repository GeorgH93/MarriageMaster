/*
 *   Copyright (C) 2022 GeorgH93
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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Database;

import at.pcgamingfreaks.Config.YamlFileManager;
import at.pcgamingfreaks.ConsoleColor;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Database.ILanguage;
import at.pcgamingfreaks.MarriageMaster.MagicValues;
import at.pcgamingfreaks.Message.MessageColor;
import at.pcgamingfreaks.Version;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class Language extends at.pcgamingfreaks.Bukkit.Config.Language implements ILanguage
{
	public static final String NO_PLACEHOLDER = "&cMarriage Master placeholder not found! Check your language file!";
	private static final String PLACEHOLDERS_KEY = "Placeholders.", HEART = MessageColor.RED + MagicValues.SYMBOL_HEART + MessageColor.RESET, SMALLHEART = MessageColor.RED + MagicValues.SYMBOL_SMALL_HEART + MessageColor.RESET;
	private static final String PLACEHOLDER_HEART = "<heart>", PLACEHOLDER_SMALLHEART = "<smallheart>";

	public Language(@NotNull MarriageMaster plugin)
	{
		super(plugin, new Version(MagicValues.LANG_VERSION));
	}

	@Override
	protected void doUpgrade(@NotNull YamlFileManager oldLang)
	{
		if(oldLang.version().olderThan(new Version(MagicValues.LANG_PRE_V2_VERSIONS)))
		{
			getLogger().warning(ConsoleColor.RED + "Your language file is from v1.x and is not compatible with versions newer than 2.5!" + ConsoleColor.RESET);
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
	public @NotNull String getTranslated(@NotNull String path)
	{
		return super.getTranslated(path).replace(PLACEHOLDER_HEART, HEART).replace(PLACEHOLDER_SMALLHEART, SMALLHEART);
	}

	public boolean isPlaceholderSet(final @NotNull String key)
	{
		return getLangE().isSet(PLACEHOLDERS_KEY + key);
	}

	@Override
	public @NotNull String getTranslatedPlaceholder(final @NotNull String key)
	{
		return MessageColor.translateAlternateColorAndFormatCodes(getLangE().getString(PLACEHOLDERS_KEY + key, NO_PLACEHOLDER)).replace(PLACEHOLDER_HEART, MagicValues.SYMBOL_HEART).replace(PLACEHOLDER_SMALLHEART, MagicValues.SYMBOL_SMALL_HEART);
	}

	@Override
	public @NotNull String getDialog(final @NotNull String key)
	{
		return getLangE().getString("Dialog." + key, "").replace(PLACEHOLDER_HEART, HEART).replace(PLACEHOLDER_SMALLHEART, SMALLHEART);
	}
}