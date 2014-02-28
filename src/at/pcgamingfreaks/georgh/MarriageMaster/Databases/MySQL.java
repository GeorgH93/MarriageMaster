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

import java.sql.*;
import java.util.Calendar;
import java.util.TreeMap;

import org.bukkit.Location;

import at.pcgamingfreaks.georgh.MarriageMaster.MarriageMaster;

public class MySQL 
{
	private MarriageMaster marriageMaster;

	private Connection con = null;
	
	public MySQL(MarriageMaster marriagemaster) 
	{
		marriageMaster = marriagemaster;
		try
		{
			con = DriverManager.getConnection("jdbc:mysql://" + marriageMaster.config.GetMySQLHost() + "/" + marriageMaster.config.GetMySQLDatabase(), marriageMaster.config.GetMySQLUser(), marriageMaster.config.GetMySQLPassword());
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		CheckDB();
	}
	
	private void CheckDB()
	{
		try
		{
			Statement stmt = con.createStatement();
			stmt.execute("CREATE TABLE IF NOT EXISTS marry_players (`player_id` INT NOT NULL AUTO_INCREMENT, `name` VARCHAR(20) NOT NULL UNIQUE, PRIMARY KEY (`player_id`));");
			stmt.execute("CREATE TABLE IF NOT EXISTS marry_priests (`priest_id` INT NOT NULL, PRIMARY KEY (`priest_id`));");
			stmt.execute("CREATE TABLE IF NOT EXISTS marry_partners (`marry_id` INT NOT NULL AUTO_INCREMENT, `player1` INT NOT NULL, `player2` INT NOT NULL, `priest` INT NULL,  `pvp_state` TINYINT(1)  NOT NULL DEFAULT false, `date` DATETIME NOT NULL, PRIMARY KEY (`marry_id`) );");
			stmt.execute("CREATE TABLE IF NOT EXISTS marry_home (`marry_id` INT NOT NULL, `home_x` DOUBLE NOT NULL, `home_y` DOUBLE NOT NULL, `home_z` DOUBLE NOT NULL, `home_world` VARCHAR(45) NOT NULL DEFAULT 'world', PRIMARY KEY (`marry_id`) );");
			stmt.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	private int GetPlayerID(String player)
	{
		int id = -1;
		try
		{
			PreparedStatement pstmt = con.prepareStatement("SELECT player_id FROM marry_players WHERE `name`=?");
			pstmt.setString(1, player);
			pstmt.executeQuery();
			ResultSet rs = pstmt.getResultSet();
			if(rs.next())
			{
				id = rs.getInt(1);
			}
			rs.close();
			pstmt.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return id;
	}
	
	private String GetPlayerName(int pid)
	{
		String name = null;
		try
		{
			Statement stmt = con.createStatement();
			stmt.executeQuery("SELECT `name` FROM marry_players WHERE `player_id`="+pid);
			ResultSet rs = stmt.getResultSet();
			if(rs.next())
			{
				name = rs.getString(1);
			}
			rs.close();
			stmt.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return name;
	}
	
	private void AddPlayer(String player)
	{
		try
		{
			PreparedStatement pstmt = con.prepareStatement("INSERT INTO marry_players (`name`) VALUES (?);");
			pstmt.setString(1, player);
			pstmt.execute();
			pstmt.close();
		}
		catch (SQLException e)
		{
			if(e.getErrorCode() != 1068)
			{
				e.printStackTrace();
			}
		}
	}

	public void AddPriest(String priestname)
	{
		AddPlayer(priestname);
		try
		{
			Statement stmt = con.createStatement();
			stmt.execute("INSERT INTO marry_priests VALUES ("+GetPlayerID(priestname)+");");
			stmt.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public void DelPriest(String priestname)
	{
		try
		{
			Statement stmt = con.createStatement();
			stmt.execute("DELETE FROM marry_priests WHERE priest_id="+GetPlayerID(priestname)+";");
			stmt.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public boolean IsPriest(String priestname)
	{
		try
		{
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT priest_id FROM marry_priests WHERE priest_id="+GetPlayerID(priestname)+";");
			if(rs.next())
			{
				return true;
			}
			rs.close();
			stmt.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean GetPvPState(String playername)
	{
		boolean res = false;
		try
		{
			int pid = GetPlayerID(playername);
			PreparedStatement pstmt = con.prepareStatement("SELECT pvp_state FROM marry_partners WHERE `player1`=? OR `player2`=?");
			pstmt.setInt(1, pid);
			pstmt.setInt(2, pid);
			pstmt.executeQuery();
			ResultSet rs = pstmt.getResultSet();
			if(rs.next())
			{
				res = rs.getBoolean(1);
			}
			rs.close();
			pstmt.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return res;
	}
	
	public String GetPartner(String playername)
	{
		String partner = null;
		try
		{
			int pid = GetPlayerID(playername);
			PreparedStatement pstmt = con.prepareStatement("SELECT player1,player2 FROM marry_partners WHERE player1=? OR player2=?");
			pstmt.setInt(1, pid);
			pstmt.setInt(2, pid);
			pstmt.executeQuery();
			ResultSet rs = pstmt.getResultSet();
			if(rs.next ())
			{
				if(rs.getInt(1) == pid)
				{
					pid = rs.getInt(2);
				}
				else
				{
					pid = rs.getInt(1);
				}
				partner = GetPlayerName(pid);
			}
			rs.close();
			pstmt.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return partner;
	}
	
	public TreeMap<String, String> GetAllMarriedPlayers()
	{
		TreeMap<String, String> MarryMap_out = new TreeMap<String, String>();
		try
		{
			ResultSet rs = con.createStatement().executeQuery("SELECT mp1.name,mp2.name FROM marry_partners INNER JOIN marry_players AS mp1 ON player1=mp1.player_id INNER JOIN marry_players AS mp2 ON player2=mp2.player_id");
			while(rs.next())
			{
				MarryMap_out.put(rs.getString(1), rs.getString(2));
			}
			rs.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return MarryMap_out;
	}
	
	public Location GetMarriedHome(String playername)
	{
		Location loc = null;
		try
		{
			int pid = GetPlayerID(playername);
			PreparedStatement pstmt = con.prepareStatement("SELECT home_x,home_y,home_z,home_world FROM marry_home INNER JOIN marry_partners ON marry_home.marry_id=marry_partners.marry_id WHERE `player1`=? OR `player2`=?");
			pstmt.setInt(1, pid);
			pstmt.setInt(2, pid);
			pstmt.executeQuery();
			ResultSet rs = pstmt.getResultSet();
			if(rs.next())
			{
				loc = new Location(marriageMaster.getServer().getWorld(rs.getString(4)), rs.getDouble(1), rs.getDouble(2), rs.getDouble(3));
			}
			rs.close();
			pstmt.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return loc;
	}
	
	public void SaveMarryHome(Location loc, String playername)
	{
		try
		{
			int pid = GetPlayerID(playername), mid = -1;
			PreparedStatement pstmt = con.prepareStatement("SELECT marry_id FROM marry_partners WHERE player1=? OR player2=?");
			pstmt.setInt(1, pid);
			pstmt.setInt(2, pid);
			pstmt.executeQuery();
			ResultSet rs = pstmt.getResultSet();
			if(rs.next())
			{
				mid = rs.getInt(1);
				pstmt = con.prepareStatement("REPLACE INTO marry_home (marry_id,home_x,home_y,home_z,home_world) VALUES ("+mid+",?,?,?,?);");
				pstmt.setDouble(1, loc.getX());
				pstmt.setDouble(2, loc.getY());
				pstmt.setDouble(3, loc.getZ());
				pstmt.setString(4, loc.getWorld().getName());
				pstmt.execute();
			}
			rs.close();
			pstmt.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void SaveMarriedPlayer(String playername, String otherPlayer, String priest)
	{
		AddPlayer(playername);
		AddPlayer(otherPlayer);
		AddPlayer(priest);
		try
		{
			PreparedStatement pstmt = con.prepareStatement("INSERT INTO marry_partners (player1, player2, priest, `date`) VALUES (?,?,?,?);");
			pstmt.setInt(1, GetPlayerID(playername));
			pstmt.setInt(2, GetPlayerID(otherPlayer));
			pstmt.setInt(3, GetPlayerID(priest));
			pstmt.setTimestamp(4, new Timestamp(Calendar.getInstance().getTime().getTime()));
			pstmt.execute();
			pstmt.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void SaveMarriedPlayerDivorce(String playername)
	{
		try
		{
			int pid = GetPlayerID(playername), mid = -1;
			PreparedStatement pstmt = con.prepareStatement("SELECT marry_id FROM marry_partners WHERE player1=? OR player2=?");
			pstmt.setInt(1, pid);
			pstmt.setInt(2, pid);
			pstmt.executeQuery();
			ResultSet rs = pstmt.getResultSet();
			if(rs.next())
			{
				mid = rs.getInt(1);
				pstmt = con.prepareStatement("DELETE FROM marry_partners WHERE marry_id=?;");
				pstmt.setInt(1, mid);
				pstmt.execute();
				pstmt = con.prepareStatement("DELETE FROM marry_home WHERE marry_id=?;");
				pstmt.setInt(1, mid);
				pstmt.execute();
				pstmt.close();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void SetPvPState(String playername, boolean state)
	{
		try
		{
			int pid = GetPlayerID(playername);
			PreparedStatement pstmt = con.prepareStatement("UPDATE marry_partners SET pvp_state=? WHERE player1=? OR player2=?");
			pstmt.setBoolean(1,state);
			pstmt.setInt(2, pid);
			pstmt.setInt(3, pid);
			pstmt.execute();
			pstmt.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}