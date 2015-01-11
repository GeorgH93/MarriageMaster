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

package at.pcgamingfreaks.MarriageMaster.Bungee.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.md_5.bungee.api.connection.ProxiedPlayer;

import at.pcgamingfreaks.MarriageMaster.Bungee.MarriageMaster;

public class MySQL extends Database
{
	private Connection conn = null;
	
	private String Table_Players, Table_Partners, Table_Home, Host, User, Password;
	private boolean UpdatePlayer;
	
	public MySQL(MarriageMaster marriagemaster)
	{
		super(marriagemaster);
		
		// Load Settings
		Table_Players = plugin.config.getUserTable();
		Table_Partners = plugin.config.getPartnersTable();
		Table_Home = plugin.config.getHomesTable();
		UpdatePlayer = plugin.config.getUpdatePlayer();
		Host = plugin.config.getMySQLHost() + "/" + plugin.config.getMySQLDatabase();
		User = plugin.config.getMySQLUser();
		Password = plugin.config.getMySQLPassword();
		// Finished Loading Settings
		CheckDB();
		CheckUUIDs();
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
					plugin.log.info(plugin.lang.getString("Console.UpdateUUIDs"));
				}
				converter.add("UPDATE " + Table_Players + " SET uuid='" + UUIDConverter.getUUIDFromName(res.getString(1)) + "' WHERE name='" + res.getString(1).replace("\\", "\\\\").replace("'", "\\'") + "'");
			}
			if(converter.size() > 0)
			{
				for (String string : converter)
				{
					stmt.execute(string);
				}
				plugin.log.info(String.format(plugin.lang.getString("Console.UpdatedUUIDs"), converter.size()));
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
				conn = DriverManager.getConnection("jdbc:mysql://" + Host + "?allowMultiQueries=true&autoReconnect=true", User, Password);
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
			try
			{
				stmt.execute("ALTER TABLE `" + Table_Players + "` ADD COLUMN `uuid` CHAR(32) UNIQUE;");
			}
			catch(SQLException e)
			{
				if(e.getErrorCode() == 1142)
				{
					plugin.log.warning(e.getMessage());
				}
				else if(e.getErrorCode() != 1060)
				{
					e.printStackTrace();
				}
			}
			stmt.execute("CREATE TABLE IF NOT EXISTS `" + Table_Partners + "` (`marry_id` INT NOT NULL AUTO_INCREMENT, `player1` INT NOT NULL, `player2` INT NOT NULL, `priest` INT NULL, `pvp_state` TINYINT(1) NOT NULL DEFAULT false, `date` DATETIME NOT NULL, PRIMARY KEY (`marry_id`) );");
			stmt.execute("CREATE TABLE IF NOT EXISTS " + Table_Home + " (`marry_id` INT NOT NULL, `home_x` DOUBLE NOT NULL, `home_y` DOUBLE NOT NULL, `home_z` DOUBLE NOT NULL, `home_world` VARCHAR(45) NOT NULL DEFAULT 'world', PRIMARY KEY (`marry_id`) );");
			try
			{
				stmt.execute("ALTER TABLE `" + Table_Home + "` ADD COLUMN `home_server` VARCHAR(45) UNIQUE;");
			}
			catch(SQLException e)
			{
				if(e.getErrorCode() == 1142)
				{
					plugin.log.warning(e.getMessage());
				}
				else if(e.getErrorCode() != 1060)
				{
					e.printStackTrace();
				}
			}
			stmt.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public void UpdatePlayer(final ProxiedPlayer player)
	{
		if(!UpdatePlayer)
		{
			return;
		}
		plugin.getProxy().getScheduler().runAsync(plugin, new Runnable()
			{
				@Override
				public void run()
			    {
					try
					{
						PreparedStatement ps;
						Connection con = DriverManager.getConnection("jdbc:mysql://" + Host, User, Password);
						ps = con.prepareStatement("SELECT `player_id` FROM `" + Table_Players + "` WHERE `uuid`=?;");
						ps.setString(1, player.getUniqueId().toString().replace("-", ""));
						ResultSet rs = ps.executeQuery();
						if(rs.next())
						{
							rs.close();
							ps.close();
							ps = con.prepareStatement("UPDATE `" + Table_Players + "` SET `name`=? WHERE `uuid`=?;");
							ps.setString(1, player.getName());
							ps.setString(2, player.getUniqueId().toString().replace("-", ""));
						}
						else
						{
							rs.close();
							ps.close();
							ps = con.prepareStatement("INSERT INTO `" + Table_Players + "` (`name`,`uuid`) VALUES (?,?);");
							ps.setString(1, player.getName());
							ps.setString(2, player.getUniqueId().toString().replace("-", ""));
						}
						ps.execute();
						ps.close();
						con.close();
					}
					catch (SQLException e)
				    {
						plugin.log.info("Failed to add user: " + player.getName());
				        e.printStackTrace();
				    }
			    }});
	}
	
	public void AddPlayer(String player, String UUID)
	{
		try
		{
			PreparedStatement ps = GetConnection().prepareStatement("SELECT `player_id` FROM `" + Table_Players + "` WHERE `uuid`=?;");
			ps.setString(1, UUID);
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
				ps = GetConnection().prepareStatement("INSERT INTO `" + Table_Players + "` (`name`,`uuid`) VALUES (?,?);");
				ps.setString(1, player);
				ps.setString(2, UUID);
			}
			ps.execute();
			ps.close();
		}
		catch (SQLException e)
	    {
			plugin.log.info("Failed to add user: " + player);
	        e.printStackTrace();
	    }
	}
	
	private int GetPlayerID(ProxiedPlayer player)
	{
		int id = -1;
		try
		{
			PreparedStatement pstmt;
			pstmt = GetConnection().prepareStatement("SELECT `player_id` FROM `" + Table_Players + "` WHERE `uuid`=?");
			pstmt.setString(1, player.getUniqueId().toString().replace("-", ""));
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
	
	private UUID GetPlayerUUID(int pid)
	{
		UUID uuid = null;
		try
		{
			Statement stmt = GetConnection().createStatement();
			stmt.executeQuery("SELECT `uuid` FROM `" + Table_Players + "` WHERE `player_id`=" + pid);
			ResultSet rs = stmt.getResultSet();
			if(rs.next())
			{
				String hexStringWithInsertedHyphens = rs.getString(1).replaceFirst("([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]+)", "$1-$2-$3-$4-$5");
				uuid = UUID.fromString(hexStringWithInsertedHyphens);
			}
			rs.close();
			stmt.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return uuid;
	}
	
	public String GetPartner(ProxiedPlayer player)
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
	
	public UUID GetPartnerUUID(ProxiedPlayer player)
	{
		UUID partner = null;
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
				partner = GetPlayerUUID(pid);
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
	
	public String getHomeServer(ProxiedPlayer player)
	{
		String HomeServer = null;
		try
		{
			int pid = GetPlayerID(player);
			PreparedStatement pstmt = GetConnection().prepareStatement("SELECT `home_server` FROM `" + Table_Home + "` INNER JOIN `" + Table_Partners + "` ON `" + Table_Home + "`.`marry_id`=`" + Table_Partners + "`.`marry_id` WHERE `player1`=? OR `player2`=?");
			pstmt.setInt(1, pid);
			pstmt.setInt(2, pid);
			pstmt.executeQuery();
			ResultSet rs = pstmt.getResultSet();
			if(rs.next())
			{
				HomeServer = rs.getString(1);
			}
			rs.close();
			pstmt.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return HomeServer;
	}
}