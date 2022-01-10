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

package at.pcgamingfreaks.MarriageMaster.Database;

import at.pcgamingfreaks.Message.Message;
import at.pcgamingfreaks.yaml.YAML;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public interface ILanguage
{
	@NotNull YAML getLangE();

	@NotNull String getTranslated(final @NotNull String key);

	@NotNull String getTranslatedPlaceholder(final @NotNull String key);

	@NotNull String getDialog(final @NotNull String key);

	default @NotNull String[] getCommandAliases(final @NotNull String command)
	{
		return getCommandAliases(command, new String[0]);
	}

	default @NotNull String[] getCommandAliases(final @NotNull String command, final @NotNull String[] defaults)
	{
		List<String> aliases = getLangE().getStringList("Command." + command, new LinkedList<>());
		return (!aliases.isEmpty()) ? aliases.toArray(new String[0]) : defaults;
	}

	default @NotNull String[] getSwitch(final @NotNull String key, final @NotNull String defaultSwitch)
	{
		List<String> switches = getLangE().getStringList("Command.Switches." + key, new LinkedList<>());
		if(!switches.contains(defaultSwitch)) switches.add(defaultSwitch);
		return switches.toArray(new String[0]);
	}

	@NotNull Message getMessage(final @NotNull String path);
}