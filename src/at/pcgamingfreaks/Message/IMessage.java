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

package at.pcgamingfreaks.Message;

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface IMessage<PLAYER, COMMAND_SENDER>
{
	/**
	 * Replaces strings within this message.
	 * This can be used to replace placeholders with static texts or with whitespaces for string format.
	 * The function is used the same way like String.replaceAll.
	 *
	 * @param regex       The regular expression to which the strings are to be matched.
	 * @param replacement The string which would replace the found expression.
	 * @return            This message instance (for chaining).
	 */
	@NotNull IMessage replaceAll(@NotNull @Language("RegExp") String regex, @NotNull String replacement);

	//region Send methods
	/**
	 * Sends the message to a target.
	 *
	 * @param target The target that should receive the message.
	 * @param args   An optional array of arguments.
	 *                  If this is used they will be passed together with the message itself to the String.format() function, before the message gets send to the client.
	 *                  This can be used to add variable data into the message.
	 */
	void send(@NotNull COMMAND_SENDER target, @Nullable Object... args);

	/**
	 * Sends the message to a {@link Collection} of targets.
	 *
	 * @param targets The targets that should receive the message.
	 * @param args    An optional array of arguments.
	 *                   If this is used they will be passed together with the message itself to the String.format() function, before the message gets send to the client.
	 *                   This can be used to add variable data into the message.
	 */
	void send(@NotNull Collection<? extends PLAYER> targets, @Nullable Object... args);

	/**
	 * Sends the message to all online players on the server, as well as the console.
	 *
	 * @param args    An optional array of arguments.
	 *                   If this is used they will be passed together with the message itself to the String.format() function, before the message gets send to the client.
	 *                   This can be used to add variable data into the message.
	 */
	void broadcast(@Nullable Object... args);
	//endregion
}