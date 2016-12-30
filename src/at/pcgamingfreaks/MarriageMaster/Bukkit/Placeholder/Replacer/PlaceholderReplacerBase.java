/*
 *   Copyright (C) 2016 GeorgH93
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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Placeholder.Replacer;

import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Placeholder.PlaceholderReplacer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class PlaceholderReplacerBase implements PlaceholderReplacer
{
	private static final String PLACEHOLDER_NOT_MARRIED_KEY = "NotMarried.";

	protected final MarriageMaster plugin;
	protected final String messageNotFound;
	protected final String valueNotMarried;

	public PlaceholderReplacerBase(@NotNull MarriageMaster plugin)
	{
		this.plugin = plugin;
		this.messageNotFound = plugin.getLanguage().getTranslatedPlaceholder("this.key.should.not.exist");
		valueNotMarried = getNotMarriedPlaceholderValue(this.getClass().getSimpleName());
	}

	protected @Nullable String getNotMarriedPlaceholderValue(@NotNull String placeholder)
	{
		String msg = this.plugin.getLanguage().getTranslatedPlaceholder(PLACEHOLDER_NOT_MARRIED_KEY + placeholder);
		if(!msg.equals(messageNotFound))
		{
			return msg.equals("NULL") ? null : msg;
		}
		msg = this.plugin.getLanguage().getTranslatedPlaceholder(PLACEHOLDER_NOT_MARRIED_KEY + "Default");
		if(msg.equals(messageNotFound) || msg.equals("NULL"))
		{
			msg = null;
		}
		return msg;
	}
}