/*
 *   Copyright (C) 2014-2015 GeorgH93
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

public class Database
{
	protected MarriageMaster plugin;
	
	public Database(MarriageMaster marriagemaster) { plugin = marriagemaster; }
	
	public void Recache() {}
	
	public void Disable() {}
	
	public void UpdatePlayer(Player player) {}
	
	public boolean GetPvPEnabled(Player player) { return false; }
	
	public void SetPvPEnabled(Player player, boolean state) {}
	
	public void DivorcePlayer(Player player) {}
	
	public void MarryPlayers(Player player1, Player player2, String priest, String surname) {}
	
	public void MarryPlayers(Player player1, Player player2, Player priest, String surname) { MarryPlayers(player1, player2, priest.getName(), surname); }
	
	public void SetMarryHome(Location loc, Player player) {}
	
	public Location GetMarryHome(Player player) { return player.getLocation(); }
	
	public void SetPriest(Player player) {}
	
	public void DelPriest(Player player) {}
	
	public boolean IsPriest(Player player) { return false; }
	
	public String GetPartner(Player player) { return player.getName(); }
	
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
	
	public String GetSurname(Player player) { return null; }
	
	public void SetSurname(Player player, String Surname) {}
	
	public void SetShareBackpack(Player player, boolean allow) {}
	
	public boolean GetPartnerShareBackpack(Player player) { return false; }
	
	public TreeMap<String, String> GetAllMarriedPlayers() { return null;}


	public static Database getDatabase(String DBType, MarriageMaster pl)
	{
		switch(DBType)
		{
			case "mysql": return new MySQL(pl);
			default: return new Files(pl);
		}
	}
}