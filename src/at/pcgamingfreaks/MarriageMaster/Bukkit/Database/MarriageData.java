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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Database;

import at.pcgamingfreaks.Bukkit.Utils;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Home;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Database.MarriageDataBase;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

public class MarriageData extends MarriageDataBase<MarriagePlayer, CommandSender, Home> implements Marriage
{
	//region Constructors
	public MarriageData(final @NotNull MarriagePlayer player1, final @NotNull MarriagePlayer player2, final @Nullable MarriagePlayer priest, final @NotNull Date weddingDate, final @Nullable String surname,
	                    final boolean pvpEnabled, final @Nullable Home home, final @Nullable Object databaseKey)
	{
		super(player1, player2, priest, weddingDate, surname, pvpEnabled, home, databaseKey);
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
		surname = MarriageMaster.getInstance().getMarriageManager().cleanupSurname(surname);
		if(surname == null || MarriageMaster.getInstance().getMarriageManager().isSurnameValid(surname))
		{
			String oldSurname = this.getSurname();
			this.surname = surname;
			MarriageMaster.getInstance().getDatabase().cachedSurnameUpdate(this, oldSurname);
			return true;
		}
		return false;
	}

	@Override
	public void setSurname(String surname, @NotNull CommandSender changer)
	{
		MarriageMaster.getInstance().getMarriageManager().setSurname(this, surname, changer);
	}

	@Override
	public void setSurname(String surname, @NotNull MarriagePlayer changer)
	{
		setSurname(surname, changer.getPlayerOnline());
	}

	@Override
	public void divorce() //TODO move to base class
	{
		updateDivorce();;
		MarriageMaster.getInstance().getDatabase().cachedDivorce(this, false);
	}

	@Override
	public void divorce(@NotNull CommandSender divorcedBy)
	{
		MarriageMaster.getInstance().getMarriageManager().divorce(this, divorcedBy);
	}

	@Override
	public void divorce(@NotNull MarriagePlayer divorcedBy)
	{
		MarriageMaster.getInstance().getMarriageManager().divorce(this, divorcedBy);
	}

	@Override
	public double getDistance()
	{
		return (isBothOnline()) ?  Utils.getDistance(getPartner1().getPlayerOnline(), getPartner2().getPlayerOnline()) : Double.NEGATIVE_INFINITY;
	}

	@Override
	public boolean inRange(double maxDistance)
	{
		return isBothOnline() && MarriageMaster.getInstance().isInRange(getPartner1().getPlayerOnline(), getPartner2().getPlayerOnline(), maxDistance);
	}

	@Override
	public boolean inRangeSquared(double maxDistanceSquared)
	{
		return isBothOnline() && MarriageMaster.getInstance().isInRangeSquared(getPartner1().getPlayerOnline(), getPartner2().getPlayerOnline(), maxDistanceSquared);
	}
	//endregion
}