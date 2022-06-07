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

package at.pcgamingfreaks.MarriageMaster.Placeholder.Processors;

import at.pcgamingfreaks.MarriageMaster.Database.MarriagePlayerDataBase;
import at.pcgamingfreaks.Message.MessageComponent;
import at.pcgamingfreaks.Message.Placeholder.Processors.IFormattedPlaceholderProcessor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DisplayNamePlaceholderProcessor implements IFormattedPlaceholderProcessor
{
	private static final MessageComponent NULL_COMPONENT = new MessageComponent("null");

	public static final DisplayNamePlaceholderProcessor INSTANCE = new DisplayNamePlaceholderProcessor();

	@Override
	public @NotNull MessageComponent processFormatted(@Nullable Object parameter)
	{
		if (parameter == null) return NULL_COMPONENT;
		return ((MarriagePlayerDataBase) parameter).getDisplayNameMessageComponent();
	}

	@Override
	public @NotNull String process(@Nullable Object parameter)
	{
		if (parameter == null) return "null";
		return ((MarriagePlayerDataBase) parameter).getDisplayName();
	}
}