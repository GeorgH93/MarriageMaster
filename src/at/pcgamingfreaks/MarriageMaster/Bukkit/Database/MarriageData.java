/*
 * Copyright (C) 2016-2018 GeorgH93
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.MarriageMaster.Bukkit.Database;

import at.pcgamingfreaks.Bukkit.Utils;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Home;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Database.DatabaseElement;

import org.apache.commons.lang.Validate;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

public class MarriageData implements Marriage, DatabaseElement
{
	private final MarriagePlayer player1, player2, priest;
	private final int hash;
	private final Date weddingDate;
	private String surname;
	private Home home;
	private boolean pvpEnabled;
	private Object databaseKey;
	private final Object syncDBKey = new Object(); // Just to be able sync the key

	public MarriageData(@NotNull MarriagePlayer player1, @NotNull MarriagePlayer player2, @Nullable MarriagePlayer priest, Date weddingDate, String surname, boolean pvpEnabled, Home home, Object databaseKey)
	{
		Validate.notNull(player1);
		Validate.notNull(player2);
		this.player1 = player1;
		this.player2 = player2;
		this.priest  = priest;
		this.surname = surname;
		this.pvpEnabled = pvpEnabled;
		this.weddingDate = (Date) weddingDate.clone();
		this.databaseKey = databaseKey;
		this.home = home;
		if(player1 instanceof MarriagePlayerData && player2 instanceof MarriagePlayerData)
		{
			((MarriagePlayerData) player1).addMarriage(this);
			((MarriagePlayerData) player2).addMarriage(this);
		}
		hash = player1.hashCode() * 53 + player2.hashCode();
	}

	public MarriageData(MarriagePlayer player1, MarriagePlayer player2, MarriagePlayer priest, Date weddingDate, String surname)
	{
		this(player1, player2, priest, weddingDate, surname, false, null, null);
	}

	public MarriageData(MarriagePlayer player1, MarriagePlayer player2, MarriagePlayer priest, String surname)
	{
		this(player1, player2, priest, new Date(), surname);
	}

	@Override
	public boolean equals(Object otherMarriage)
	{
		return otherMarriage instanceof MarriageData && player1.equals(((MarriageData) otherMarriage).player1) && player2.equals(((MarriageData) otherMarriage).player2);
	}

	@Override
	public int hashCode()
	{
		return hash;
	}

	@Override
	public Object getDatabaseKey()
	{
		synchronized(syncDBKey)
		{
			return databaseKey;
		}
	}

	public void setDatabaseKey(Object key)
	{
		synchronized(syncDBKey)
		{
			databaseKey = key;
		}
	}

	public void setHomeLoc(Home loc)
	{
		home = loc;
	}

	public void updateSurname(String surname)
	{
		String oldSurname = this.surname;
		this.surname = surname;
		MarriageMaster.getInstance().getDatabase().cachedSurnameUpdate(this, oldSurname, false);
	}

	public void updatePvPState(boolean newState)
	{
		pvpEnabled = newState;
	}

	public void updateDivorce()
	{
		((MarriagePlayerData) player1).removeMarriage(this);
		((MarriagePlayerData) player2).removeMarriage(this);
		MarriageMaster.getInstance().getDatabase().cachedDivorce(this, false);
		surname = null;
		home = null;
	}

	// API Functions
	@Override
	public @NotNull MarriagePlayer getPartner1()
	{
		return player1;
	}

	@Override
	public @NotNull MarriagePlayer getPartner2()
	{
		return player2;
	}

	@Override
	public boolean isBothOnline()
	{
		return player1.isOnline() && player2.isOnline();
	}

	@Override
	public MarriagePlayer getPartner(@NotNull MarriagePlayer player)
	{
		return (player1.equals(player)) ? player2 : (player2.equals(player)) ? player1 : null;
	}

	@Override
	public String getSurname()
	{
		return surname;
	}

	@Override
	public boolean setSurname(String surname)
	{
		surname = MarriageMaster.getInstance().getMarriageManager().cleanupSurname(surname);
		if(surname == null || MarriageMaster.getInstance().getMarriageManager().isSurnameValid(surname))
		{
			String oldSurname = this.surname;
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
		MarriageMaster.getInstance().getMarriageManager().setSurname(this, surname, changer.getPlayer().getPlayer());
	}

	@Override
	public boolean isHomeSet()
	{
		return getHome() != null;
	}

	@Override
	public @Nullable Home getHome()
	{
		return home;
	}

	@Override
	public void setHome(@Nullable Home home)
	{
		this.home = home;
		MarriageMaster.getInstance().getDatabase().updateHome(this);
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
		MarriageMaster.getInstance().getDatabase().updatePvPState(this);
	}

	@Override
	public @NotNull Date getWeddingDate()
	{
		return (Date) weddingDate.clone();
	}

	@Override
	public MarriagePlayer getPriest()
	{
		return priest;
	}

	@Override
	public void divorce()
	{
		((MarriagePlayerData) player1).removeMarriage(this);
		((MarriagePlayerData) player2).removeMarriage(this);
		MarriageMaster.getInstance().getDatabase().cachedDivorce(this);
		surname = null;
		home = null;
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
		return (player1.isOnline() && player2.isOnline()) ?  Utils.getDistance(player1.getPlayer().getPlayer(), player2.getPlayer().getPlayer()) : Double.NEGATIVE_INFINITY;
	}

	@Override
	public boolean inRange(double maxDistance)
	{
		return player1.isOnline() && player2.isOnline() && MarriageMaster.getInstance().isInRange(player1.getPlayer().getPlayer(), player2.getPlayer().getPlayer(), maxDistance);
	}

	@Override
	public boolean hasPlayer(@NotNull MarriagePlayer player)
	{
		return getPartner1().equals(player) || getPartner2().equals(player);
	}
}