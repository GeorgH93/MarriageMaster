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

package at.pcgamingfreaks.MarriageMaster.Bungee.Database;

import at.pcgamingfreaks.Config.YamlFileManager;
import at.pcgamingfreaks.MarriageMaster.Bungee.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Database.ILanguage;
import at.pcgamingfreaks.MarriageMaster.MagicValues;
import at.pcgamingfreaks.Message.MessageColor;
import at.pcgamingfreaks.Version;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class Language extends at.pcgamingfreaks.Bungee.Config.Language implements ILanguage
{
	public Language(final @NotNull MarriageMaster plugin)
	{
		super(plugin, new Version(MagicValues.LANG_VERSION));
	}

	@Override
	protected void doUpgrade(final @NotNull YamlFileManager oldLang)
	{
		Map<String, String> remapping = new HashMap<>();
		remapping.put("Command.Main", "Command.Marry");
		super.doUpgrade(oldLang, remapping);
	}

	@Override
	public @NotNull String getTranslated(final @NotNull String key)
	{
		return super.getTranslated(key).replace("<heart>", MessageColor.RED + MagicValues.SYMBOL_HEART + MessageColor.RESET).replace("<smallheart>", MessageColor.RED + MagicValues.SYMBOL_SMALL_HEART + MessageColor.RESET);
	}

	@Override
	public @NotNull String getTranslatedPlaceholder(@NotNull String key)
	{
		throw new UnsupportedOperationException("Placeholders are not available on BungeeCord!");
	}

	@Override
	public @NotNull String getDialog(@NotNull String key)
	{
		throw new UnsupportedOperationException("Dialog is not available on BungeeCord!");
	}

}