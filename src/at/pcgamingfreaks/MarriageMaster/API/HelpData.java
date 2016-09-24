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

package at.pcgamingfreaks.MarriageMaster.API;

import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Stores all the data that should be displayed when the help for a command is shown.
 */
@SuppressWarnings("unused")
public class HelpData
{
	private String translatedSubCommand, parameter, description;

	/**
	 * Creates a new instance of the {@link HelpData} object to store the data for the sub-command.
	 *
	 * @param translatedSubCommand The translated name of the sub-command.
	 * @param parameter The parameters to be displayed. null or "" for no parameters.
	 * @param description The description of the sub-command.
	 */
	public HelpData(@NotNull String translatedSubCommand, @Nullable String parameter, @NotNull String description)
	{
		this.translatedSubCommand = translatedSubCommand;
		this.parameter = (parameter == null) ? "" : parameter;
		this.description = description;
	}

	/**
	 * Gets the parameter of the sub-command.
	 *
	 * @return The parameter to be displayed.
	 */
	public @NotNull String getParameter()
	{
		return parameter;
	}

	/**
	 * Sets the parameter string of the help data.
	 *
	 * @param parameter The parameters to be displayed. null or "" for no parameters.
	 */
	public void setParameter(@Nullable String parameter)
	{
		this.parameter = (parameter == null) ? "" : parameter;
	}

	/**
	 * Gets the description of the sub-command.
	 *
	 * @return The description to be displayed.
	 */
	public @NotNull String getDescription()
	{
		return description;
	}

	/**
	 * Sets the description of the sub-command.
	 *
	 * @param description The description to be displayed.
	 */
	public void setDescription(@NotNull String description)
	{
		Validate.notEmpty(description, "The description of a command must not be null or empty.");
		this.description = description;
	}

	/**
	 * Gets the translated name of the sub-command.
	 *
	 * @return The translated name of the sub-command to be displayed.
	 */
	public @NotNull String getTranslatedSubCommand()
	{
		return translatedSubCommand;
	}

	/**
	 * Sets the translated name of the sub command.
	 *
	 * @param translatedSubCommand The translated name of the sub command to be displayed.
	 */
	public void setTranslatedSubCommand(@NotNull String translatedSubCommand)
	{
		Validate.notEmpty(translatedSubCommand, "The sub command must not be null or empty.");
		this.translatedSubCommand = translatedSubCommand;
	}
}