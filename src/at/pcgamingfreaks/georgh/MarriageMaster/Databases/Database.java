/*
 *   Copyright (C) 2014 GeorgH93
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

package at.pcgamingfreaks.georgh.MarriageMaster.Databases;

import java.util.TreeMap;

import org.bukkit.Location;

import at.pcgamingfreaks.georgh.MarriageMaster.MarriageMaster;

public class Database
{
	private MySQL mysql = null;
	private Files files = null;
	private String DBType = null;
	private MarriageMaster marriageMaster;
	
	public Database(MarriageMaster marriagemaster)
	{
		marriageMaster = marriagemaster;
		DBType = marriageMaster.config.GetDatabaseType().toLowerCase();
		switch(DBType)
		{
			case "mysql": mysql = new MySQL(marriageMaster); break;
			default: files = new Files(marriageMaster); break;
		}
	}
	
	public void Recache()
	{
		if(marriageMaster.config.GetDatabaseType().toLowerCase() != DBType)
		{
			switch(DBType)
			{
				case "mysql": mysql = null; break;
				default: files = null; break;
			}
			DBType = marriageMaster.config.GetDatabaseType().toLowerCase();
			switch(DBType)
			{
				case "mysql": mysql = new MySQL(marriageMaster); break;
				default: files = new Files(marriageMaster); break;
			}
		}
		switch(DBType)
		{
			case "mysql": break;
			default: files.Reload();
		}
	}
	
	public boolean GetPvPEnabled(String playername)
	{
		switch(DBType)
		{
			case "mysql": return mysql.GetPvPState(playername);
			default: return files.GetPvPState(playername);
		}
	}
	
	public void SetPvPEnabled(String player1, String player2, boolean state)
	{
		switch(DBType)
		{
			case "mysql": mysql.SetPvPState(player1, state); break;
			default: files.SetPvPState(player1, state); files.SetPvPState(player2, state); break;
		}
	}
	
	public void DivorcePlayer(String player1, String player2)
	{
		switch(DBType)
		{
			case "mysql": mysql.SaveMarriedPlayerDivorce(player1); break;
			default: files.SaveMarriedPlayerDivorce(player1); files.SaveMarriedPlayerDivorce(player2); break;
		}
	}
	
	public void MarryPlayers(String player1, String player2, String priester)
	{
		switch(DBType)
		{
			case "mysql": mysql.SaveMarriedPlayer(player1, player2, priester); break;
			default:
				files.SaveMarriedPlayer(player1, player2, priester);
				files.SaveMarriedPlayer(player2, player1, priester);
			break;
		}
	}
	
	public void SetMarriedHome(Location loc, String player1, String player2)
	{
		switch(DBType)
		{
			case "mysql": mysql.SaveMarryHome(loc, player1); break;
			default:
				files.SaveMarriedHome(loc, player1);
				files.SaveMarriedHome(loc, player2);
			break;
		}
	}
	
	public Location GetMarryHome(String playername)
	{
		switch(DBType)
		{
			case "mysql": return mysql.GetMarriedHome(playername);
			default: return files.GetMarriedHome(playername);
		}
	}
	
	public void SetPriest(String Player)
	{
		switch(DBType)
		{
			case "mysql": mysql.AddPriest(Player); break;
			default: files.AddPriest(Player); break;
		}
	}
	
	public void DelPriest(String Player)
	{
		switch(DBType)
		{
			case "mysql": mysql.DelPriest(Player); break;
			default: files.DelPriest(Player); break;
		}
	}
	
	public boolean IsPriester(String playername)
	{
		switch(DBType)
		{
			case "mysql": return mysql.IsPriest(playername);
			default: return files.IsPriester(playername);
		}
	}
	
	public String GetPartner(String playername)
	{
		switch(DBType)
		{
			case "mysql": return mysql.GetPartner(playername);
			default: return files.GetPartner(playername);
		}
	}
	
	public TreeMap<String, String> GetAllMarriedPlayers()
	{
		switch(DBType)
		{
			case "mysql": return mysql.GetAllMarriedPlayers();
			default: return files.GetAllMarriedPlayers();
		}
	}
}
