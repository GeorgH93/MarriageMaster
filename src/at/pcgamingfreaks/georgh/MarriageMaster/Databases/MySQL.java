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
import org.bukkit.World;
import org.bukkit.entity.Player;

import at.pcgamingfreaks.georgh.MarriageMaster.MarriageMaster;

public class MySQL extends Database
{
	private Connection conn = null;
	
	private String Table_Players, Table_Priests, Table_Partners, Table_Home, Host, User, Password;
	private boolean UpdatePlayer;
	
	public MySQL(MarriageMaster marriagemaster)
	{
		super(marriagemaster);
		// Load Settings
		Table_Players = marriageMaster.config.getUserTable();
		Table_Priests = marriageMaster.config.getPriestsTable();
		Table_Partners = marriageMaster.config.getPartnersTable();
		Table_Home = marriageMaster.config.getHomesTable();
		UpdatePlayer = marriageMaster.config.getUpdatePlayer();
		Host = marriageMaster.config.GetMySQLHost() + "/" + marriageMaster.config.GetMySQLDatabase();
		User = marriageMaster.config.GetMySQLUser();
		Password = marriageMaster.config.GetMySQLPassword();
		// Finished Loading Settings
		CheckDB();
		if(marriageMaster.UseUUIDs)
		{
			CheckUUIDs();
		}
		AddPlayer("none", "00000000000000000000000000000000");
		AddPlayer("Console", "00000000000000000000000000000001");
	}
	
	private void CheckUUIDs()
	{
		try
		{
			List<String> converter = new ArrayList<String>();
			Statement stmt = GetConnection().createStatement();
			ResultSet res = stmt.executeQuery("SELECT name FROM " + Table_Players + " WHERE uuid IS NULL");
			while(res.next())
			{
				if(res.isFirst())
				{
					marriageMaster.log.info(marriageMaster.lang.Get("Console.UpdateUUIDs"));
				}
				converter.add("UPDATE " + Table_Players + " SET uuid='" + UUIDConverter.getUUIDFromName(res.getString(1)) + "' WHERE name='" + res.getString(1).replace("\\", "\\\\").replace("'", "\\'") + "'");
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
				conn = DriverManager.getConnection("jdbc:mysql://" + Host + "?allowMultiQueries=true", User, Password);
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
			stmt.execute("CREATE TABLE IF NOT EXISTS `" + Table_Players + "` (`player_id` INT NOT NULL AUTO_INCREMENT, `name` VARCHAR(20) NOT NULL UNIQUE, PRIMARY KEY (`player_id`));");
			if(marriageMaster.UseUUIDs)
			{
				try
				{
					stmt.execute("ALTER TABLE `" + Table_Players + "` ADD COLUMN `uuid` CHAR(32) UNIQUE;");
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
			if(marriageMaster.config.getUseMinepacks())
			{
				try
				{
					stmt.execute("ALTER TABLE `" + Table_Players + "` ADD COLUMN `sharebackpack` TINYINT(1) NOT NULL DEFAULT false;");
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
			stmt.execute("CREATE TABLE IF NOT EXISTS `" + Table_Priests + "` (`priest_id` INT NOT NULL, PRIMARY KEY (`priest_id`));");
			stmt.execute("CREATE TABLE IF NOT EXISTS `" + Table_Partners + "` (`marry_id` INT NOT NULL AUTO_INCREMENT, `player1` INT NOT NULL, `player2` INT NOT NULL, `priest` INT NULL, `pvp_state` TINYINT(1) NOT NULL DEFAULT false, `date` DATETIME NOT NULL, PRIMARY KEY (`marry_id`) );");
			if(marriageMaster.config.getSurname())
			{
				try
				{
					stmt.execute("ALTER TABLE `" + Table_Partners + "` ADD COLUMN `Surname` VARCHAR(35) UNIQUE;");
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
			stmt.execute("CREATE TABLE IF NOT EXISTS " + Table_Home + " (`marry_id` INT NOT NULL, `home_x` DOUBLE NOT NULL, `home_y` DOUBLE NOT NULL, `home_z` DOUBLE NOT NULL, `home_world` VARCHAR(45) NOT NULL DEFAULT 'world', PRIMARY KEY (`marry_id`) );");
			stmt.execute("DELETE FROM " + Table_Partners + " WHERE player1=player2");
			stmt.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public void UpdatePlayer(final Player player)
	{
		if(!UpdatePlayer)
		{
			return;
		}
		marriageMaster.getServer().getScheduler().runTaskAsynchronously(marriageMaster, new Runnable()
		{
			@Override
			public void run()
		    {
				try
				{
					PreparedStatement ps;
					Connection con = DriverManager.getConnection("jdbc:mysql://" + Host, User, Password);
					ps = con.prepareStatement("SELECT `player_id` FROM `" + Table_Players + "` WHERE " + ((marriageMaster.UseUUIDs) ? "`uuid`=?;" : "`name`=?;"));
					if(marriageMaster.UseUUIDs)
					{
						ps.setString(1, player.getUniqueId().toString().replace("-", ""));
					}
					else
					{
						ps.setString(1, player.getName());
					}
					ResultSet rs = ps.executeQuery();
					if(rs.next())
					{
						rs.close();
						ps.close();
						if(!marriageMaster.UseUUIDs)
						{
							con.close();
							return;
						}
						ps = con.prepareStatement("UPDATE `" + Table_Players + "` SET `name`=? WHERE `uuid`=?;");
						ps.setString(1, player.getName());
						ps.setString(2, player.getUniqueId().toString().replace("-", ""));
					}
					else
					{
						rs.close();
						ps.close();
						ps = con.prepareStatement("INSERT INTO `" + Table_Players + "` (`name`" + ((marriageMaster.UseUUIDs) ? ",`uuid`" : "") + ") VALUES (?" + ((marriageMaster.UseUUIDs) ? ",?" : "") + ");");
						ps.setString(1, player.getName());
						if(marriageMaster.UseUUIDs)
						{
							ps.setString(2, player.getUniqueId().toString().replace("-", ""));
						}
					}
					ps.execute();
					ps.close();
					con.close();
				}
				catch (SQLException e)
			    {
					marriageMaster.log.info("Failed to add user: " + player.getName());
			        e.printStackTrace();
			    }
		    }});
	}
	
	public void AddPlayer(String player, String UUID)
	{
		try
		{
			PreparedStatement ps = GetConnection().prepareStatement("SELECT `player_id` FROM `" + Table_Players + "` WHERE " + ((marriageMaster.UseUUIDs) ? "`uuid`=?;" : "`name`=?;"));
			if(marriageMaster.UseUUIDs)
			{
				ps.setString(1, UUID);
			}
			else
			{
				ps.setString(1, player);
			}
			ResultSet rs = ps.executeQuery();
			if(rs.next())
			{
				rs.close();
				ps.close();
				return;
			}
			else
			{
				rs.close();
				ps.close();
				ps = GetConnection().prepareStatement("INSERT INTO `" + Table_Players + "` (`name`" + ((marriageMaster.UseUUIDs) ? ",`uuid`" : "") + ") VALUES (?" + ((marriageMaster.UseUUIDs) ? ",?" : "") + ");");
				ps.setString(1, player);
				if(marriageMaster.UseUUIDs)
				{
					ps.setString(2, UUID);
				}
			}
			ps.execute();
			ps.close();
		}
		catch (SQLException e)
	    {
			marriageMaster.log.info("Failed to add user: " + player);
	        e.printStackTrace();
	    }
	}
	
	private int GetPlayerID(Player player)
	{
		int id = -1;
		try
		{
			PreparedStatement pstmt;
			if(marriageMaster.UseUUIDs)
			{
				pstmt = GetConnection().prepareStatement("SELECT `player_id` FROM `" + Table_Players + "` WHERE `uuid`=?");
				pstmt.setString(1, player.getUniqueId().toString().replace("-", ""));
			}
			else
			{
				pstmt = GetConnection().prepareStatement("SELECT `player_id` FROM `" + Table_Players + "` WHERE `name`=?");
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
			stmt.executeQuery("SELECT `name` FROM `" + Table_Players + "` WHERE `player_id`="+pid);
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

	public void SetPriest(Player priest)
	{
		try
		{
			PreparedStatement pstmt;
			if(marriageMaster.UseUUIDs)
			{
				pstmt = GetConnection().prepareStatement("INSERT INTO `" + Table_Priests + "` SELECT `player_id` FROM `" + Table_Players + "` WHERE `uuid`=?");
				pstmt.setString(1, priest.getUniqueId().toString().replace("-", ""));
			}
			else
			{
				pstmt = GetConnection().prepareStatement("INSERT INTO `" + Table_Priests + "` SELECT `player_id` FROM `" + Table_Players + "` WHERE `name`=?");
				pstmt.setString(1, priest.getName());
			}
			pstmt.execute();
			pstmt.close();
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
			PreparedStatement pstmt;
			if(marriageMaster.UseUUIDs)
			{
				pstmt = GetConnection().prepareStatement("DELETE FROM `" + Table_Priests + "` WHERE `priest_id` IN (SELECT `player_id` FROM `" + Table_Players + "` WHERE `uuid`=?);");
				pstmt.setString(1, priest.getUniqueId().toString().replace("-", ""));
			}
			else
			{
				pstmt = GetConnection().prepareStatement("DELETE FROM `" + Table_Priests + "` WHERE `priest_id` IN (SELECT `player_id` FROM `" + Table_Players + "` WHERE `name`=?);");
				pstmt.setString(1, priest.getName());
			}
			pstmt.execute();
			pstmt.close();
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
			PreparedStatement pstmt;
			if(marriageMaster.UseUUIDs)
			{
				pstmt = GetConnection().prepareStatement("SELECT priest_id FROM `" + Table_Priests + "` WHERE `priest_id` IN (SELECT `player_id` FROM `" + Table_Players + "` WHERE `uuid`=?);");
				pstmt.setString(1, priest.getUniqueId().toString().replace("-", ""));
			}
			else
			{
				pstmt = GetConnection().prepareStatement("SELECT priest_id FROM `" + Table_Priests + "` WHERE `priest_id` IN (SELECT `player_id` FROM `" + Table_Players + "` WHERE `name`=?);");
				pstmt.setString(1, priest.getName());
			}
			ResultSet rs = pstmt.executeQuery();
			if(rs.next())
			{
				return true;
			}
			rs.close();
			pstmt.close();
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
			PreparedStatement pstmt = GetConnection().prepareStatement("SELECT `pvp_state` FROM `" + Table_Partners + "` WHERE `player1`=? OR `player2`=?");
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
			PreparedStatement pstmt = GetConnection().prepareStatement("SELECT `player1`,`player2` FROM `" + Table_Partners + "` WHERE `player1`=? OR `player2`=?");
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
			ResultSet rs = GetConnection().createStatement().executeQuery("SELECT `mp1`.`name`,`mp2`.`name` FROM `" + Table_Partners + "` INNER JOIN `" + Table_Players + "` AS mp1 ON `player1`=`mp1`.`player_id` INNER JOIN `" + Table_Players + "` AS mp2 ON `player2`=`mp2`.`player_id`");
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
			PreparedStatement pstmt = GetConnection().prepareStatement("SELECT `home_x`,`home_y`,`home_z`,`home_world` FROM `" + Table_Home + "` INNER JOIN `" + Table_Partners + "` ON `" + Table_Home + "`.`marry_id`=`" + Table_Partners + "`.`marry_id` WHERE `player1`=? OR `player2`=?");
			pstmt.setInt(1, pid);
			pstmt.setInt(2, pid);
			pstmt.executeQuery();
			ResultSet rs = pstmt.getResultSet();
			World world = marriageMaster.getServer().getWorld(rs.getString(4));
			if(world == null)
			{
				return null;
			}
			if(rs.next())
			{
				loc = new Location(world, rs.getDouble(1), rs.getDouble(2), rs.getDouble(3));
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
			PreparedStatement pstmt = GetConnection().prepareStatement("SELECT `marry_id` FROM " + Table_Partners + " WHERE `player1`=? OR `player2`=?");
			pstmt.setInt(1, pid);
			pstmt.setInt(2, pid);
			pstmt.executeQuery();
			ResultSet rs = pstmt.getResultSet();
			if(rs.next())
			{
				mid = rs.getInt(1);
				pstmt = GetConnection().prepareStatement("REPLACE INTO `" + Table_Home + "` (`marry_id`,`home_x`,`home_y`,`home_z`,`home_world`) VALUES ("+mid+",?,?,?,?);");
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

	public void MarryPlayers(Player player, Player otherPlayer, Player priest, String surname)
	{
		try
		{
			PreparedStatement pstmt;
			if(marriageMaster.config.getSurname())
			{
				pstmt = GetConnection().prepareStatement("INSERT INTO `" + Table_Partners + "` (`player1`, `player2`, `priest`, `date`, `surname`) VALUES (?,?,?,?,?);");
				pstmt.setString(5, LimitText(surname, 34));
			}
			else
			{
				pstmt = GetConnection().prepareStatement("INSERT INTO `" + Table_Partners + "` (`player1`, `player2`, `priest`, `date`) VALUES (?,?,?,?);");
			}
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
	
	public void MarryPlayers(Player player, Player otherPlayer, String priest, String surname)
	{
		PreparedStatement pstmt;
		int priestid = -1;
		try
		{
			pstmt = GetConnection().prepareStatement("SELECT `player_id` FROM `" + Table_Players + "` WHERE `name`=?");
			pstmt.setString(1, priest);
			pstmt.executeQuery();
			ResultSet rs = pstmt.getResultSet();
			if(rs.next())
			{
				priestid = rs.getInt(1);
			}
			rs.close();
			pstmt.close();
			if(marriageMaster.config.getSurname())
			{
				pstmt = GetConnection().prepareStatement("INSERT INTO `" + Table_Partners + "` (`player1`, `player2`, `priest`, `date`, `surname`) VALUES (?,?,?,?,?);");
				pstmt.setString(5, LimitText(surname, 34));
			}
			else
			{
				pstmt.close();
				pstmt = GetConnection().prepareStatement("INSERT INTO `" + Table_Partners + "` (`player1`, `player2`, `priest`, `date`) VALUES (?,?,?,?);");
			}
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
			PreparedStatement pstmt = GetConnection().prepareStatement("SELECT `marry_id` FROM `" + Table_Partners + "` WHERE `player1`=? OR `player2`=?");
			pstmt.setInt(1, pid);
			pstmt.setInt(2, pid);
			pstmt.executeQuery();
			ResultSet rs = pstmt.getResultSet();
			if(rs.next())
			{
				mid = rs.getInt(1);
				pstmt = GetConnection().prepareStatement("DELETE FROM `" + Table_Partners + "` WHERE `marry_id`=?; DELETE FROM `" + Table_Home + "` WHERE `marry_id`=?;");
				pstmt.setInt(1, mid);
				pstmt.setInt(2, mid);
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
			PreparedStatement pstmt = GetConnection().prepareStatement("UPDATE `" + Table_Partners + "` SET `pvp_state`=? WHERE `player1`=? OR `player2`=?");
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
	
	public String GetSurname(Player player)
	{
		try
		{
			int pid = GetPlayerID(player);
			PreparedStatement pstmt = GetConnection().prepareStatement("SELECT `surname` FROM `" + Table_Partners + "` WHERE `player1`=? OR `player2`=?");
			pstmt.setInt(1, pid);
			pstmt.setInt(2, pid);
			pstmt.executeQuery();
			ResultSet rs = pstmt.getResultSet();
			if(rs.next())
			{
				return rs.getString(1);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public void SetSurname(Player player, String surname)
	{
		try
		{
			int pid = GetPlayerID(player);
			PreparedStatement pstmt = GetConnection().prepareStatement("UPDATE `" + Table_Partners + "` SET `surname`=? WHERE `player1`=? OR `player2`=?");
			pstmt.setString(1, LimitText(surname, 34));
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
	
	public String LimitText(String text, int len)
	{
		if(text != null && text.length() > len)
		{
			return text.substring(0, len);
		}
		return text;
	}
	
	public void SetShareBackpack(Player player, boolean allow)
	{
		try
		{
			PreparedStatement pstmt = GetConnection().prepareStatement("UPDATE `" + Table_Players + "` SET `sharebackpack`=? WHERE " + ((marriageMaster.UseUUIDs) ? "`uuid`" : "`name`") + "=?;");
			if(marriageMaster.UseUUIDs)
			{
				pstmt.setString(2, player.getUniqueId().toString().replace("-", ""));
			}
			else
			{
				pstmt.setString(2, player.getName());
			}
			pstmt.setBoolean(1, allow);
			pstmt.execute();
			pstmt.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public boolean GetPartnerShareBackpack(Player player)
	{
		boolean result = false;
		try
		{
			PreparedStatement pstmt = GetConnection().prepareStatement("SELECT `sharebackpack` FROM `" + Table_Players + "` WHERE " + ((marriageMaster.UseUUIDs) ? "`uuid`" : "`name`") + "=?;");
			if(marriageMaster.UseUUIDs)
			{
				pstmt.setString(1, player.getUniqueId().toString().replace("-", ""));
			}
			else
			{
				pstmt.setString(1, player.getName());
			}
			ResultSet rs = pstmt.executeQuery();
			if(rs.next())
			{
				result = rs.getBoolean(1);
			}
			rs.close();
			pstmt.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}
}