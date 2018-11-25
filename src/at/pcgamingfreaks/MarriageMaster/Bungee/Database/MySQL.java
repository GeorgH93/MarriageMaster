/*
 *   Copyright (C) 2014-2018 GeorgH93
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
import java.util.*;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import at.pcgamingfreaks.MarriageMaster.UUIDConverter;
import at.pcgamingfreaks.MarriageMaster.Bungee.MarriageMaster;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class MySQL extends Database
{
	private HikariDataSource dataSource;
	
	private String tablePlayers, tableMarriages, tableHomes, host, user, password;
	private boolean updatePlayer, onlineUUIDs;
	
	public MySQL(MarriageMaster marriagemaster)
	{
		super(marriagemaster);
		
		//region Load Settings
		onlineUUIDs = marriagemaster.config.getUUIDType().equalsIgnoreCase("auto") || marriagemaster.config.getUUIDType().equalsIgnoreCase("online");
		tablePlayers = plugin.config.getUserTable();
		tableMarriages = plugin.config.getPartnersTable();
		tableHomes = plugin.config.getHomesTable();
		updatePlayer = plugin.config.getUpdatePlayer();
		host = plugin.config.getMySQLHost() + "/" + plugin.config.getMySQLDatabase();
		user = plugin.config.getMySQLUser();
		password = plugin.config.getMySQLPassword();
		//endregion

		HikariConfig poolConfig = new HikariConfig();
		poolConfig.setJdbcUrl("jdbc:mysql://" + plugin.config.getMySQLHost() + "/" + plugin.config.getMySQLDatabase() + "?allowMultiQueries=true" + plugin.config.getMySQLProperties());
		poolConfig.setUsername(plugin.config.getMySQLUser());
		poolConfig.setPassword(plugin.config.getMySQLPassword());
		poolConfig.setMinimumIdle(1);
		poolConfig.setMaximumPoolSize(plugin.config.getMySQLMaxConnections());
		poolConfig.setPoolName("MarriageMaster-Connection-Pool");
		poolConfig.addDataSourceProperty("cachePrepStmts", "true");
		dataSource = new HikariDataSource(poolConfig);

		checkDB();
		CheckUUIDs();
		runStatement("INSERT INTO `" + tablePlayers + "` (`name`,`uuid`) VALUES (?,?) ON DUPLICATE KEY UPDATE `name`=?, `uuid`=?;", "none", "00000000000000000000000000000000", "none", "00000000000000000000000000000000");
		runStatement("INSERT INTO `" + tablePlayers + "` (`name`,`uuid`) VALUES (?,?) ON DUPLICATE KEY UPDATE `name`=?, `uuid`=?;", "Console", "00000000000000000000000000000001", "Console", "00000000000000000000000000000001");
	}

	private void runStatement(final String query, final Object... args)
	{
		try(Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query))
		{
			for(int i = 0; args != null && i < args.length; i++)
			{
				preparedStatement.setObject(i + 1, args[i]);
			}
			preparedStatement.execute();
		}
		catch(SQLException e)
		{
			System.out.print("Query: " + query);
			e.printStackTrace();
		}
	}
	
	private void CheckUUIDs()
	{
		class UpdateData // Helper class for fixing UUIDs
		{
			int id;
			String name, uuid;

			public UpdateData(String name, String uuid, int id)
			{
				this.id = id;
				this.name = name;
				this.uuid = uuid;
			}
		}
		try
		{
			Map<String, UpdateData> toConvert = new HashMap<>();
			List<UpdateData> toUpdate = new LinkedList<>();
			try(Connection connection = dataSource.getConnection(); Statement stmt = connection.createStatement();
			    ResultSet res = stmt.executeQuery("SELECT `player_id`,`name`,`uuid` FROM `" + tablePlayers + "` WHERE `uuid` IS NULL OR `uuid` LIKE '%-%';"))
			{
				while(res.next())
				{
					if(res.isFirst())
					{
						plugin.log.info(plugin.lang.getString("Console.UpdateUUIDs"));
					}
					String uuid = res.getString("uuid");
					if(uuid == null)
					{
						toConvert.put(res.getString("name").toLowerCase(), new UpdateData(res.getString("name"), null, res.getInt("player_id")));
					}
					else
					{
						toUpdate.add(new UpdateData(res.getString("name"), uuid.replaceAll("-", ""), res.getInt("player_id")));
					}
				}
			}
			if(toConvert.size() > 0 || toUpdate.size() > 0)
			{
				if(toConvert.size() > 0)
				{
					Map<String, String> newUUIDs = UUIDConverter.getUUIDsFromNames(toConvert.keySet(), onlineUUIDs, false);
					for(Map.Entry<String, String> entry : newUUIDs.entrySet())
					{
						UpdateData updateData = toConvert.get(entry.getKey().toLowerCase());
						updateData.uuid = entry.getValue();
						toUpdate.add(updateData);
					}
				}
				try(Connection connection = dataSource.getConnection(); PreparedStatement ps = connection.prepareStatement("UPDATE `" + tablePlayers + "` SET `uuid`=? WHERE `player_id`=?;"))
				{
					for(UpdateData updateData : toUpdate)
					{
						ps.setString(1, updateData.uuid);
						ps.setInt(2, updateData.id);
						ps.addBatch();
					}
					ps.executeBatch();
				}
				plugin.log.info(String.format(plugin.lang.getString("Console.UpdatedUUIDs"), toUpdate.size()));
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}

	private String replacePlaceholders(String query)
	{
		return query.replaceAll("(\\{\\w+})", "`$1`").replaceAll("`(\\{\\w+})`_UNIQUE", "`$1_UNIQUE`").replaceAll("`(\\{\\w+})`_idx", "`$1_idx`")
				.replaceAll("fk_`(\\{\\w+})`_`(\\{\\w+})`_`(\\{\\w+})`", "`fk_$1_$2_$3`")
				.replaceAll("\\{TPlayers}", tablePlayers).replaceAll("\\{TMarriages}", tableMarriages).replaceAll("\\{THomes}", tableHomes)
				.replaceAll("\\{FPlayerID}", "player_id").replaceAll("\\{FName}", "name").replaceAll("\\{FUUID}", "uuid").replaceAll("\\{FShareBackpack}", "sharebackpack")
				.replaceAll("\\{FMarryID}", "marry_id").replaceAll("\\{FSurname}", "surname").replaceAll("\\{FPlayer1}", "player1").replaceAll("\\{FPlayer2}", "player2")
				.replaceAll("\\{FPriest}", "priest").replaceAll("\\{FPvPState}", "pvp_state").replaceAll("\\{FDate}", "date").replaceAll("\\{FHomeServer}", "home_server")
				.replaceAll("\\{FHomeX}", "home_x").replaceAll("\\{FHomeY}", "home_y").replaceAll("\\{FHomeZ}", "home_z").replaceAll("\\{FHomeWorld}", "home_world");
	}
	
	private void checkDB()
	{
		try(Connection connection = dataSource.getConnection())
		{
			String  queryTPlayers = replacePlaceholders("CREATE TABLE {TPlayers} (\n{FPlayerID} INT NOT NULL AUTO_INCREMENT,\n{FName} VARCHAR(16) NOT NULL,\n{FUUID} CHAR(36) DEFAULT NULL,\n" +
					                                            "{FShareBackpack} TINYINT(1) NOT NULL DEFAULT 0,\nPRIMARY KEY ({FPlayerID}),\nUNIQUE INDEX {FUUID}_UNIQUE ({FUUID})\n);"),
					queryTMarriages = replacePlaceholders("CREATE TABLE {TMarriages} (\n{FMarryID} INT NOT NULL AUTO_INCREMENT,\n{FPlayer1} INT NOT NULL,\n{FPlayer2} INT NOT NULL,\n" +
							                                      "{FPriest} INT NULL,\n{FSurname} VARCHAR(45) NULL,\n{FPvPState} TINYINT(1) NOT NULL DEFAULT 0,\n{FDate} DATETIME NOT NULL,\n" +
							                                      "PRIMARY KEY ({FMarryID}),\nINDEX {FPlayer1}_idx ({FPlayer1}),\nINDEX {FPlayer2}_idx ({FPlayer2}),\nINDEX {FPriest}_idx ({FPriest})\n);"),
					queryTHomes = replacePlaceholders("CREATE TABLE {THomes} (\n{FMarryID} INT NOT NULL,\n{FHomeX} DOUBLE NOT NULL,\n{FHomeY} DOUBLE NOT NULL,\n{FHomeZ} DOUBLE NOT NULL,\n" +
							                                  "{FHomeWorld} VARCHAR(45) NOT NULL DEFAULT 'world',\n{FHomeServer} VARCHAR(45) DEFAULT NULL,\nPRIMARY KEY ({FMarryID})\n);");
			DBTools.updateDB(connection, queryTPlayers);
			DBTools.updateDB(connection, queryTMarriages);
			DBTools.updateDB(connection, queryTHomes);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public void updatePlayer(final ProxiedPlayer player)
	{
		if(!updatePlayer)
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
						Connection con = DriverManager.getConnection("jdbc:mysql://" + host, user, password);
						ps = con.prepareStatement("SELECT `player_id` FROM `" + tablePlayers + "` WHERE `uuid`=?;");
						ps.setString(1, player.getUniqueId().toString().replace("-", ""));
						ResultSet rs = ps.executeQuery();
						if(rs.next())
						{
							rs.close();
							ps.close();
							ps = con.prepareStatement("UPDATE `" + tablePlayers + "` SET `name`=? WHERE `uuid`=?;");
							ps.setString(1, player.getName());
							ps.setString(2, player.getUniqueId().toString().replace("-", ""));
						}
						else
						{
							rs.close();
							ps.close();
							ps = con.prepareStatement("INSERT INTO `" + tablePlayers + "` (`name`,`uuid`) VALUES (?,?);");
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
	
	private int getPlayerID(ProxiedPlayer player)
	{
		int id = -1;
		try(Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("SELECT `player_id` FROM `" + tablePlayers + "` WHERE `uuid`=?"))
		{
			preparedStatement.setString(1, player.getUniqueId().toString().replace("-", ""));
			preparedStatement.executeQuery();
			try(ResultSet rs = preparedStatement.getResultSet())
			{
				if(rs.next())
				{
					id = rs.getInt(1);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return id;
	}
	
	private String getPlayerName(int pid)
	{
		String name = null;
		try(Connection connection = dataSource.getConnection(); Statement stmt = connection.createStatement())
		{
			stmt.executeQuery("SELECT `name` FROM `" + tablePlayers + "` WHERE `player_id`="+pid);
			try(ResultSet rs = stmt.getResultSet())
			{
				if(rs.next()) name = rs.getString(1);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return name;
	}
	
	private UUID getPlayerUUID(int pid)
	{
		UUID uuid = null;
		try(Connection connection = dataSource.getConnection(); Statement stmt = connection.createStatement())
		{
			stmt.executeQuery("SELECT `uuid` FROM `" + tablePlayers + "` WHERE `player_id`=" + pid);
			try(ResultSet rs = stmt.getResultSet())
			{
				if(rs.next() && rs.getString(1) != null)
				{
					String hexStringWithInsertedHyphens = rs.getString(1).replaceFirst("([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]+)", "$1-$2-$3-$4-$5");
					uuid = UUID.fromString(hexStringWithInsertedHyphens);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return uuid;
	}
	
	@Override
	public String getPartner(ProxiedPlayer player)
	{
		String partner = null;
		try
		{
			int pid = getPlayerID(player);
			try(Connection connection = dataSource.getConnection();
			    PreparedStatement preparedStatement = connection.prepareStatement("SELECT `player1`,`player2` FROM `" + tableMarriages + "` WHERE `player1`=? OR `player2`=?"))
			{
				preparedStatement.setInt(1, pid);
				preparedStatement.setInt(2, pid);
				preparedStatement.executeQuery();
				try(ResultSet rs = preparedStatement.getResultSet())
				{
					if(rs.next())
					{
						if(rs.getInt(1) == pid)
						{
							pid = rs.getInt(2);
						}
						else
						{
							pid = rs.getInt(1);
						}
						partner = getPlayerName(pid);
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return partner;
	}
	
	@Override
	public UUID getPartnerUUID(ProxiedPlayer player)
	{
		UUID partner = null;
		try
		{
			int pid = getPlayerID(player);
			try(Connection connection = dataSource.getConnection();
			    PreparedStatement preparedStatement = connection.prepareStatement("SELECT `player1`,`player2` FROM `" + tableMarriages + "` WHERE `player1`=? OR `player2`=?"))
			{
				preparedStatement.setInt(1, pid);
				preparedStatement.setInt(2, pid);
				preparedStatement.executeQuery();
				try(ResultSet rs = preparedStatement.getResultSet())
				{
					if(rs.next())
					{
						if(rs.getInt(1) == pid)
						{
							pid = rs.getInt(2);
						}
						else
						{
							pid = rs.getInt(1);
						}
						partner = getPlayerUUID(pid);
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return partner;
	}
	
	@Override
	public String getHomeServer(ProxiedPlayer player)
	{
		String HomeServer = null;
		try
		{
			int pid = getPlayerID(player);
			try(Connection connection = dataSource.getConnection();
			    PreparedStatement preparedStatement = connection.prepareStatement("SELECT `home_server` FROM `" + tableHomes + "` INNER JOIN `" + tableMarriages + "` ON `" + tableHomes + "`.`marry_id`=`" + tableMarriages + "`.`marry_id` WHERE `player1`=? OR `player2`=?"))
			{
				preparedStatement.setInt(1, pid);
				preparedStatement.setInt(2, pid);
				preparedStatement.executeQuery();
				try(ResultSet rs = preparedStatement.getResultSet())
				{
					if(rs.next())
					{
						HomeServer = rs.getString(1);
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return HomeServer;
	}
}