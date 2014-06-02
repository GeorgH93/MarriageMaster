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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TreeMap;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import at.pcgamingfreaks.georgh.MarriageMaster.MarriageMaster;

public class MySQL extends Database
{
	private Connection conn = null;
	
	public MySQL(MarriageMaster marriagemaster)
	{
		super(marriagemaster);
		CheckDB();
		if(marriageMaster.config.UseUUIDs())
		{
			CheckUUIDs();
		}
	}
	
	private void CheckUUIDs()
	{
		try
		{
			List<String> converter = new ArrayList<String>();
			Statement stmt = GetConnection().createStatement();
			ResultSet res = stmt.executeQuery("SELECT name FROM marry_players WHERE uuid IS NULL");
			boolean first = true;
			while(res.next())
			{
				if(first)
				{
					marriageMaster.log.info(marriageMaster.lang.Get("Console.UpdateUUIDs"));
				}
				converter.add("UPDATE marry_players SET uuid='" + UUIDConverter.getUUIDFromName(res.getString(1)) + "' WHERE name='" + res.getString(1).replace("\\", "\\\\").replace("'", "\\'") + "'");
			}
			if(converter.size() > 0)
			{
				for (String string : converter)
				{
					stmt.execute(string);
				}
				marriageMaster.log.info(String.format(marriageMaster.lang.Get("Console.UpdatedUUIDs"),converter.size()));
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	private Connection GetConnection()
	{
		try
		{
			if(conn == null || conn.isClosed())
			{
				conn = DriverManager.getConnection("jdbc:mysql://" + marriageMaster.config.GetMySQLHost() + "/" + marriageMaster.config.GetMySQLDatabase(), marriageMaster.config.GetMySQLUser(), marriageMaster.config.GetMySQLPassword());
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return conn;
	}
	
	private void CheckDB()
	{
		try
		{
			Statement stmt = GetConnection().createStatement();
			stmt.execute("CREATE TABLE IF NOT EXISTS marry_players (`player_id` INT NOT NULL AUTO_INCREMENT, `name` VARCHAR(20) NOT NULL UNIQUE, PRIMARY KEY (`player_id`));");
			if(marriageMaster.config.UseUUIDs())
			{
				try
				{
					stmt.execute("ALTER TABLE `minecraft`.`marry_players` ADD COLUMN `uuid` VARCHAR(35) UNIQUE;");
				}
				catch(SQLException e)
				{
					if(e.getErrorCode() == 1142)
					{
						marriageMaster.log.warning(e.getMessage());
					}
					else if(e.getErrorCode() != 1060)
					{
						e.printStackTrace();
					}
				}
			}
			stmt.execute("CREATE TABLE IF NOT EXISTS marry_priests (`priest_id` INT NOT NULL, PRIMARY KEY (`priest_id`));");
			stmt.execute("CREATE TABLE IF NOT EXISTS marry_partners (`marry_id` INT NOT NULL AUTO_INCREMENT, `player1` INT NOT NULL, `player2` INT NOT NULL, `priest` INT NULL,  `pvp_state` TINYINT(1)  NOT NULL DEFAULT false, `date` DATETIME NOT NULL, PRIMARY KEY (`marry_id`) );");
			stmt.execute("CREATE TABLE IF NOT EXISTS marry_home (`marry_id` INT NOT NULL, `home_x` DOUBLE NOT NULL, `home_y` DOUBLE NOT NULL, `home_z` DOUBLE NOT NULL, `home_world` VARCHAR(45) NOT NULL DEFAULT 'world', PRIMARY KEY (`marry_id`) );");
			stmt.execute("DELETE FROM marry_partners WHERE player1=player2");
			stmt.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	private int GetPlayerID(Player player)
	{
		int id = -1;
		try
		{
			PreparedStatement pstmt;
			if(marriageMaster.config.UseUUIDs())
			{
				pstmt = GetConnection().prepareStatement("SELECT player_id FROM marry_players WHERE `uuid`=?");
				pstmt.setString(1, player.getUniqueId().toString().replace("-", ""));
			}
			else
			{
				pstmt = GetConnection().prepareStatement("SELECT player_id FROM marry_players WHERE `name`=?");
				pstmt.setString(1, player.getName());
			}
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
			Statement stmt = GetConnection().createStatement();
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
	
	private void AddPlayer(Player player)
	{
		try
		{
			PreparedStatement pstmt;
			if(marriageMaster.config.UseUUIDs())
			{
				pstmt = GetConnection().prepareStatement("INSERT INTO marry_players (`name`, `uuid`) VALUES (?,?);");
				pstmt.setString(2, player.getUniqueId().toString().replace("-", ""));
			}
			else
			{
				pstmt = GetConnection().prepareStatement("INSERT INTO marry_players (`name`) VALUES (?);");
			}
			pstmt.setString(1, player.getName());
			pstmt.execute();
			pstmt.close();
		}
		catch (SQLException e)
		{
			if(e.getErrorCode() != 1062)
			{
				e.printStackTrace();
			}
		}
	}
	
	public void UpdatePlayer(Player player)
	{
		if(marriageMaster.config.UseUUIDs())
		{
			try
			{
				PreparedStatement pstmt = GetConnection().prepareStatement("UPDATE marry_players SET `name`=? WHERE uuid=?;");
				pstmt.setString(1, player.getName());
				pstmt.setString(2, player.getUniqueId().toString().replace("-", ""));
				pstmt.execute();
				pstmt.close();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
	}

	public void SetPriest(Player priest)
	{
		AddPlayer(priest);
		try
		{
			Statement stmt = GetConnection().createStatement();
			stmt.execute("INSERT INTO marry_priests VALUES ("+GetPlayerID(priest)+");");
			stmt.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public void DelPriest(Player priest)
	{
		try
		{
			Statement stmt = GetConnection().createStatement();
			stmt.execute("DELETE FROM marry_priests WHERE priest_id="+GetPlayerID(priest)+";");
			stmt.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public boolean IsPriest(Player priest)
	{
		try
		{
			Statement stmt = GetConnection().createStatement();
			ResultSet rs = stmt.executeQuery("SELECT priest_id FROM marry_priests WHERE priest_id="+GetPlayerID(priest)+";");
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
	
	public boolean GetPvPEnabled(Player player)
	{
		boolean res = false;
		try
		{
			int pid = GetPlayerID(player);
			PreparedStatement pstmt = GetConnection().prepareStatement("SELECT pvp_state FROM marry_partners WHERE `player1`=? OR `player2`=?");
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
	
	public String GetPartner(Player player)
	{
		String partner = null;
		try
		{
			int pid = GetPlayerID(player);
			PreparedStatement pstmt = GetConnection().prepareStatement("SELECT player1,player2 FROM marry_partners WHERE player1=? OR player2=?");
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
			ResultSet rs = GetConnection().createStatement().executeQuery("SELECT mp1.name,mp2.name FROM marry_partners INNER JOIN marry_players AS mp1 ON player1=mp1.player_id INNER JOIN marry_players AS mp2 ON player2=mp2.player_id");
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
	
	public Location GetMarryHome(Player player)
	{
		Location loc = null;
		try
		{
			int pid = GetPlayerID(player);
			PreparedStatement pstmt = GetConnection().prepareStatement("SELECT home_x,home_y,home_z,home_world FROM marry_home INNER JOIN marry_partners ON marry_home.marry_id=marry_partners.marry_id WHERE `player1`=? OR `player2`=?");
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
	
	public void SetMarryHome(Location loc, Player player)
	{
		try
		{
			int pid = GetPlayerID(player), mid = -1;
			PreparedStatement pstmt = GetConnection().prepareStatement("SELECT marry_id FROM marry_partners WHERE player1=? OR player2=?");
			pstmt.setInt(1, pid);
			pstmt.setInt(2, pid);
			pstmt.executeQuery();
			ResultSet rs = pstmt.getResultSet();
			if(rs.next())
			{
				mid = rs.getInt(1);
				pstmt = GetConnection().prepareStatement("REPLACE INTO marry_home (marry_id,home_x,home_y,home_z,home_world) VALUES ("+mid+",?,?,?,?);");
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
	
	public void MarryPlayers(Player player, Player otherPlayer, Player priest)
	{
		AddPlayer(player);
		AddPlayer(otherPlayer);
		AddPlayer(priest);
		try
		{
			PreparedStatement pstmt = GetConnection().prepareStatement("INSERT INTO marry_partners (player1, player2, priest, `date`) VALUES (?,?,?,?);");
			pstmt.setInt(1, GetPlayerID(player));
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
	
	public void MarryPlayers(Player player, Player otherPlayer, String priest)
	{
		PreparedStatement pstmt;
		AddPlayer(player);
		AddPlayer(otherPlayer);
		try
		{
			if(marriageMaster.config.UseUUIDs())
			{
				pstmt = GetConnection().prepareStatement("INSERT INTO marry_players (`name`, `uuid`) VALUES (?,?);");
				pstmt.setString(2, (priest=="none")?"00000000000000000000000000000000":"00000000000000000000000000000001");
			}
			else
			{
				pstmt = GetConnection().prepareStatement("INSERT INTO marry_players (`name`) VALUES (?);");
			}
			pstmt.setString(1, priest);
			pstmt.execute();
			pstmt.close();
		}
		catch (SQLException e)
		{
			if(e.getErrorCode() != 23000)
			{
				e.printStackTrace();
			}
		}
		int priestid = -1;
		try
		{
			pstmt = GetConnection().prepareStatement("SELECT player_id FROM marry_players WHERE `name`=?");
			pstmt.setString(1, priest);
			pstmt.executeQuery();
			ResultSet rs = pstmt.getResultSet();
			if(rs.next())
			{
				priestid = rs.getInt(1);
			}
			rs.close();
			pstmt = GetConnection().prepareStatement("INSERT INTO marry_partners (player1, player2, priest, `date`) VALUES (?,?,?,?);");
			pstmt.setInt(1, GetPlayerID(player));
			pstmt.setInt(2, GetPlayerID(otherPlayer));
			pstmt.setInt(3, priestid);
			pstmt.setTimestamp(4, new Timestamp(Calendar.getInstance().getTime().getTime()));
			pstmt.execute();
			pstmt.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void DivorcePlayer(Player player)
	{
		try
		{
			int pid = GetPlayerID(player), mid = -1;
			PreparedStatement pstmt = GetConnection().prepareStatement("SELECT marry_id FROM marry_partners WHERE player1=? OR player2=?");
			pstmt.setInt(1, pid);
			pstmt.setInt(2, pid);
			pstmt.executeQuery();
			ResultSet rs = pstmt.getResultSet();
			if(rs.next())
			{
				mid = rs.getInt(1);
				pstmt = GetConnection().prepareStatement("DELETE FROM marry_partners WHERE marry_id=?;");
				pstmt.setInt(1, mid);
				pstmt.execute();
				pstmt = GetConnection().prepareStatement("DELETE FROM marry_home WHERE marry_id=?;");
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
	
	public void SetPvPEnabled(Player player, boolean state)
	{
		try
		{
			int pid = GetPlayerID(player);
			PreparedStatement pstmt = GetConnection().prepareStatement("UPDATE marry_partners SET pvp_state=? WHERE player1=? OR player2=?");
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