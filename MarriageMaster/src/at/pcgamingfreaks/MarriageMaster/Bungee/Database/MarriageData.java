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

import at.pcgamingfreaks.MarriageMaster.API.Home;
import at.pcgamingfreaks.MarriageMaster.Bungee.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bungee.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Database.MarriageDataBase;
import at.pcgamingfreaks.Message.MessageColor;

import net.md_5.bungee.api.CommandSender;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

public class MarriageData extends MarriageDataBase<MarriagePlayer, CommandSender, Home> implements Marriage
{
	//region Constructors
	public MarriageData(final @NotNull MarriagePlayer player1, final @NotNull MarriagePlayer player2, final @Nullable MarriagePlayer priest, final @NotNull Date weddingDate, final @Nullable String surname,
	                    final boolean pvpEnabled, final @Nullable MessageColor color, final @Nullable Home home, final @Nullable Object databaseKey)
	{
		super(player1, player2, priest, weddingDate, surname, pvpEnabled, color, home, databaseKey);
	}

	public MarriageData(final @NotNull MarriagePlayer player1, final @NotNull MarriagePlayer player2, final @Nullable MarriagePlayer priest, final @NotNull Date weddingDate, final @Nullable String surname)
	{
		super(player1, player2, priest, weddingDate, surname);
	}

	public MarriageData(final @NotNull MarriagePlayer player1, final @NotNull MarriagePlayer player2, final @Nullable MarriagePlayer priest, final @Nullable String surname)
	{
		super(player1, player2, priest, surname);
	}
	//endregion

	//region API Functions
	@Override
	public boolean setSurname(String surname)
	{
		return false;
	}

	@Override
	public void setSurname(String surname, @NotNull CommandSender changer) {}

	@Override
	public void setSurname(String surname, @NotNull MarriagePlayer changer) {}

	@Override
	public void divorce(@NotNull CommandSender divorcedBy) {}

	@Override
	public void divorce(@NotNull MarriagePlayer divorcedBy) {}
	//endregion
}