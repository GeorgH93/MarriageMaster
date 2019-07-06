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

package at.pcgamingfreaks.MarriageMaster.Database;

import at.pcgamingfreaks.MarriageMaster.API.Home;
import at.pcgamingfreaks.MarriageMaster.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.API.MarriagePlayer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

public abstract class MarriageDataBase<MARRIAGE_PLAYER extends MarriagePlayer, COMMAND_SENDER, HOME extends Home> implements Marriage<MARRIAGE_PLAYER, COMMAND_SENDER, HOME>, DatabaseElement
{
	private final MARRIAGE_PLAYER player1, player2;
	@Getter private final MARRIAGE_PLAYER priest;
	private final int hash;
	private final Date weddingDate;
	@Getter protected String surname;
	@Getter @NotNull private String chatPrefix;
	@Getter private HOME home;
	private boolean pvpEnabled;
	@Getter @Setter	private Object databaseKey;

	//region Constructors
	public MarriageDataBase(final @NotNull MARRIAGE_PLAYER player1, final @NotNull MARRIAGE_PLAYER player2, final @Nullable MARRIAGE_PLAYER priest, final @NotNull Date weddingDate, final @Nullable String surname,
	                        final boolean pvpEnabled, final @Nullable HOME home, final @Nullable Object databaseKey)
	{
		this.player1 = player1;
		this.player2 = player2;
		this.priest  = priest;
		this.surname = surname;
		this.pvpEnabled = pvpEnabled;
		this.weddingDate = (Date) weddingDate.clone();
		this.databaseKey = databaseKey;
		this.home = home;
		this.chatPrefix = ""; // TODO implement in db
		if(player1 instanceof MarriagePlayerDataBase && player2 instanceof MarriagePlayerDataBase)
		{
			((MarriagePlayerDataBase) player1).addMarriage(this);
			((MarriagePlayerDataBase) player2).addMarriage(this);
		}
		hash = player1.hashCode() * 53 + player2.hashCode();
	}

	public MarriageDataBase(final @NotNull MARRIAGE_PLAYER player1, final @NotNull MARRIAGE_PLAYER player2, final @Nullable MARRIAGE_PLAYER priest, final @NotNull Date weddingDate, final @Nullable String surname)
	{
		this(player1, player2, priest, weddingDate, surname, false, null, null);
	}

	public MarriageDataBase(final @NotNull MARRIAGE_PLAYER player1, final @NotNull MARRIAGE_PLAYER player2, final @Nullable MARRIAGE_PLAYER priest, final @Nullable String surname)
	{
		this(player1, player2, priest, new Date(), surname);
	}
	//endregion

	@Override
	public boolean equals(Object otherMarriage)
	{
		return otherMarriage instanceof MarriageDataBase && player1.equals(((MarriageDataBase) otherMarriage).player1) && player2.equals(((MarriageDataBase) otherMarriage).player2);
	}

	@Override
	public int hashCode()
	{
		return hash;
	}

	//region data setter
	public void setHomeData(HOME home)
	{
		this.home = home;
	}

	public void updateDivorce()
	{
		((MarriagePlayerDataBase) getPartner1()).removeMarriage(this);
		((MarriagePlayerDataBase) getPartner2()).removeMarriage(this);
	}
	//endregion

	//region API Functions
	@Override
	public @NotNull MARRIAGE_PLAYER getPartner1()
	{
		return player1;
	}

	@Override
	public @NotNull MARRIAGE_PLAYER getPartner2()
	{
		return player2;
	}

	@Override
	public boolean isBothOnline()
	{
		return player1.isOnline() && player2.isOnline();
	}

	@Override
	public @Nullable MARRIAGE_PLAYER getPartner(@NotNull MarriagePlayer player)
	{
		return (player1.equals(player)) ? player2 : (player2.equals(player)) ? player1 : null;
	}

	@Override
	public boolean isHomeSet()
	{
		return getHome() != null;
	}

	@Override
	public void setHome(HOME home)
	{
		this.home = home;
		BaseDatabase.getInstance().updateHome(this);
	}

	@Override
	public @NotNull Date getWeddingDate()
	{
		return (Date) weddingDate.clone();
	}

	@Override
	public boolean isPVPEnabled()
	{
		return pvpEnabled;
	}

	@Override
	public void setPVPEnabled(boolean pvpEnabled)
	{
		this.pvpEnabled = pvpEnabled;
		BaseDatabase.getInstance().updatePvPState(this);
	}

	@Override
	public boolean hasPlayer(@NotNull MARRIAGE_PLAYER player)
	{
		return getPartner1().equals(player) || getPartner2().equals(player);
	}

	@Override
	public @NotNull String getMarriageChatMessagePrefix()
	{
		return chatPrefix;
	}

	@Override
	public void setMarriageChatMessagePrefix(@NotNull String chatPrefix)
	{
		this.chatPrefix = chatPrefix.substring(0, Math.min(20, chatPrefix.length()));
	}

	@Override
	public void divorce()
	{
		updateDivorce();
		//TODO set in db
	}
	//endregion
}