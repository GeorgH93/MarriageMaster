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

package at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events;

import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Marriage;

import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.Getter;
import lombok.Setter;

/**
 * Event is fired before the surname of a couple is changed.
 */
@Getter
public class SurnameChangeEvent extends MarriageMasterCancellableEvent
{
	private final Marriage marriageData;
	@Setter @Nullable private String newSurname;
	@NotNull private final CommandSender changedBy;

	// Bukkit handler stuff
	private static final HandlerList handlers = new HandlerList();

	public SurnameChangeEvent(Marriage marriageData, @Nullable String newSurname, @NotNull CommandSender changedBy) {
		this.marriageData = marriageData;
		this.newSurname = newSurname;
		this.changedBy = changedBy;
	}

	@Override
	public @NotNull HandlerList getHandlers()
	{
		return getHandlerList();
	}

	public static HandlerList getHandlerList()
	{
		return handlers;
	}
}