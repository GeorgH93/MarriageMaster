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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Placeholder.Replacer;

import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Database.Language;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Placeholder.PlaceholderName;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Placeholder.PlaceholderReplacer;

import org.apache.commons.lang.NotImplementedException;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public abstract class PlaceholderReplacerBase implements PlaceholderReplacer
{
	private static final String PLACEHOLDER_NOT_MARRIED_KEY = "NotMarried", PLACEHOLDER_DEFAULT_KEY = "Default.", NULL_MAGIC = "NULL";

	protected final MarriageMaster plugin;
	protected final String valueNotMarried;

	protected PlaceholderReplacerBase(final @NotNull MarriageMaster plugin)
	{
		this.plugin = plugin;
		valueNotMarried = getPlaceholderValue(PLACEHOLDER_NOT_MARRIED_KEY);
	}

	protected @Nullable String getPlaceholderValue(final @NotNull String placeholderKey)
	{
		return getPlaceholderValue(getName(), placeholderKey);
	}

	protected @Nullable String getPlaceholderValue(final @NotNull String placeholder, final @NotNull String placeholderKey)
	{
		String p = placeholder + "." + placeholderKey, msg;
		if(plugin.getLanguage().isPlaceholderSet(p))
		{
			msg = this.plugin.getLanguage().getTranslatedPlaceholder(p);
		}
		else
		{
			msg = this.plugin.getLanguage().getTranslatedPlaceholder(PLACEHOLDER_DEFAULT_KEY + placeholderKey);
		}
		//noinspection StringEquality
		if(msg == Language.NO_PLACEHOLDER) // == is correct here! We want to check if it's the same instance, not value. No warning should be shown if the text is written in the language file
		{
			plugin.getLogger().warning("No placeholder translation for key: " + p);
		}
		return msg.equals(NULL_MAGIC) ? null : msg;
	}

	@Override
	public @Nullable String replace(OfflinePlayer player)
	{
		MarriagePlayer playerData = plugin.getPlayerData(player);
		if(playerData.isMarried()) return replaceMarried(playerData);
		return valueNotMarried;
	}

	protected @Nullable String replaceMarried(MarriagePlayer player) { throw new NotImplementedException("The replaceMarried method for the placeholder has not been implemented!"); }

	@Override
	public @NotNull String getName()
	{
		if(getClass().isAnnotationPresent(PlaceholderName.class))
		{
			String name = getClass().getAnnotation(PlaceholderName.class).name();
			if(!name.isEmpty()) return name;
		}
		return this.getClass().getSimpleName();
	}

	@Override
	public @NotNull Collection<String> getAliases()
	{
		List<String> aliasesList = new LinkedList<>();
		if(getClass().isAnnotationPresent(PlaceholderName.class))
		{
			String[] aliases = getClass().getAnnotation(PlaceholderName.class).aliases();
			for(String alias : aliases)
			{
				if(!alias.isEmpty()) aliasesList.add(alias);
			}
		}
		return aliasesList;
	}

	@Override
	public @Nullable String getFormat()
	{
		return null;
	}
}