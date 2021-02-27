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

package at.pcgamingfreaks.MarriageMaster.Bungee.Database;

import at.pcgamingfreaks.MarriageMaster.Database.ILanguage;
import at.pcgamingfreaks.MarriageMaster.MagicValues;
import at.pcgamingfreaks.Message.MessageColor;
import at.pcgamingfreaks.YamlFileManager;

import net.md_5.bungee.api.plugin.Plugin;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class Language extends at.pcgamingfreaks.Bungee.Language implements ILanguage
{
	public Language(final @NotNull Plugin plugin)
	{
		super(plugin, MagicValues.LANG_VERSION, MagicValues.LANG_VERSION);
	}

	@Override
	protected void doUpdate()
	{}

	@Override
	protected void doUpgrade(final @NotNull YamlFileManager oldLang)
	{
		Map<String, String> remappings = new HashMap<>();
		remappings.put("Command.Main", "Command.Marry");
		super.doUpgrade(oldLang, remappings);
	}

	@Override
	public @NotNull String getTranslated(final @NotNull String key)
	{
		return super.getTranslated(key).replaceAll("<heart>", MessageColor.RED + MagicValues.SYMBOL_HEART).replaceAll("<smallheart>", MessageColor.RED + MagicValues.SYMBOL_SMALL_HEART);
	}

	@Override
	public @NotNull String getTranslatedPlaceholder(@NotNull String key)
	{
		throw new RuntimeException("Placeholders are not available on BungeeCord!");
	}

	@Override
	public @NotNull String getDialog(@NotNull String key)
	{
		throw new RuntimeException("Dialog is not available on BungeeCord!");
	}

}