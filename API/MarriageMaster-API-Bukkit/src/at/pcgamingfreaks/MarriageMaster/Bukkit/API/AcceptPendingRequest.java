/*
 *   Copyright (C) 2021 GeorgH93
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

package at.pcgamingfreaks.MarriageMaster.Bukkit.API;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public abstract class AcceptPendingRequest extends at.pcgamingfreaks.MarriageMaster.API.AcceptPendingRequest<MarriagePlayer>
{
	/**
	 * Creates a new instance of an {@link AcceptPendingRequest}.
	 *
	 * @param hasToAccept The player that has to accept the request.
	 * @param canCancel   The players that can cancel the request.
	 */
	protected AcceptPendingRequest(@NotNull MarriagePlayer hasToAccept, @NotNull MarriagePlayer... canCancel)
	{
		super(hasToAccept, canCancel);
	}
}
