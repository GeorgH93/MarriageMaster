/*
 *   Copyright (C) 2025 GeorgH93
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

package at.pcgamingfreaks.MarriageMaster.Bukkit;

import java.util.Locale;

public enum SurnameConfirmationMode
{
	None,
	Player,
	BothPlayers,
	//TODO
	//Admin,
	//Priest,
	//PlayerOrPriest,
	//BothPlayersOrPriest
	;

	public static SurnameConfirmationMode formString(String mode)
	{
		if(mode == null) return None;
		mode = mode.toLowerCase(Locale.ENGLISH);
		if(mode.equals("player")) return Player;
		else if(mode.equals("both") || mode.equals("bothplayers")) return BothPlayers;
		return None;
	}
}
