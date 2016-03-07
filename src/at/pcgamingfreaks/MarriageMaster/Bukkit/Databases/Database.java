/*
 *   Copyright (C) 2014-2016 GeorgH93
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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Databases;

import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;

public abstract class Database
{
	protected MarriageMaster plugin;
	
	public Database(MarriageMaster marriagemaster) { plugin = marriagemaster; }
	
	public abstract void Disable();
	
	public abstract void UpdatePlayer(Player player);
	
	public abstract boolean GetPvPEnabled(Player player);
	
	public abstract void SetPvPEnabled(Player player, boolean state);
	
	public abstract void DivorcePlayer(Player player);
	
	public abstract void MarryPlayers(Player player1, Player player2, String priest, String surname);
	
	public void MarryPlayers(Player player1, Player player2, Player priest, String surname) { MarryPlayers(player1, player2, priest.getName(), surname); }
	
	public abstract void DelMarryHome(Player player);
	
	public abstract void DelMarryHome(String player);
	
	public abstract void SetMarryHome(Location loc, Player player);
	
	public abstract void GetMarryHome(String player, Callback<Location> result);
	
	public abstract void GetMarryHome(Player player, Callback<Location> result);
	
	public abstract void SetPriest(Player player);
	
	public abstract void DelPriest(Player player);
	
	public abstract boolean IsPriest(Player player);
	
	public abstract String GetPartner(Player player);
	
	@SuppressWarnings("deprecation")
	public Player GetPlayerPartner(Player player)
	{
		String partner = GetPartner(player);
		if(partner != null)
		{
			return Bukkit.getPlayerExact(partner);
		}
		return null;
	}
	
	public abstract String GetSurname(Player player);
	
	public abstract void SetSurname(Player player, String Surname);
	
	public abstract void SetShareBackpack(Player player, boolean allow);
	
	public abstract boolean GetPartnerShareBackpack(Player player);
	
	public abstract void GetAllMarriedPlayers(Callback<TreeMap<String, String>> loaded);


	public static Database getDatabase(String DBType, MarriageMaster pl)
	{
		switch(DBType)
		{
			case "mysql": return new MySQL(pl);
			default: return new Files(pl);
		}
	}
	
	protected static String LimitText(String text, int len)
	{
		if(text != null && text.length() > len)
		{
			return text.substring(0, len);
		}
		return text;
	}

	public interface Callback<T>
	{
		void onResult(T result);
	}
}