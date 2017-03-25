/*
 *   Copyright (C) 2014-2017 GeorgH93
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

import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.UUIDConverter;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("SqlResolve")
public class MySQL extends Database implements Listener
{
	private HikariDataSource dataSource;

	private final String tablePlayers, tablePriests, tablePartners, tableHome, uuidOrName;
	private final boolean updatePlayer, onlineUUIDs;

	private Map<String, Integer> namesToID = new ConcurrentHashMap<>();
	private Map<Player, Integer> playersToID = new ConcurrentHashMap<>();

	public MySQL(MarriageMaster marriagemaster)
	{
		super(marriagemaster);

		// Load Settings
		onlineUUIDs = (marriagemaster.config.getUUIDType().equalsIgnoreCase("auto") && Bukkit.getServer().getOnlineMode()) || marriagemaster.config.getUUIDType().equalsIgnoreCase("online");
		tablePlayers = plugin.config.getUserTable();
		tablePriests = plugin.config.getPriestsTable();
		tablePartners = plugin.config.getPartnersTable();
		tableHome = plugin.config.getHomesTable();
		updatePlayer = plugin.config.getUpdatePlayer();

		HikariConfig poolConfig = new HikariConfig();
		poolConfig.setJdbcUrl("jdbc:mysql://" + plugin.config.getMySQLHost() + "/" + plugin.config.getMySQLDatabase() + "?allowMultiQueries=true" + plugin.config.getMySQLProperties());
		poolConfig.setUsername(plugin.config.getMySQLUser());
		poolConfig.setPassword(plugin.config.getMySQLPassword());
		poolConfig.setMinimumIdle(1);
		poolConfig.setMaximumPoolSize(plugin.config.getMySQLMaxConnections());
		poolConfig.setPoolName("MarriageMaster-Connection-Pool");
		poolConfig.addDataSourceProperty("cachePrepStmts", "true");
		dataSource = new HikariDataSource(poolConfig);

		uuidOrName = (plugin.UseUUIDs) ? "uuid" : "name";
		// Finished Loading Settings
		CheckDB();
		if(plugin.UseUUIDs)
		{
			checkUUIDs();
			runStatement("INSERT INTO `" + tablePlayers + "` (`name`,`uuid`) VALUES (?,?) ON DUPLICATE KEY UPDATE `name`=?, `uuid`=?;", "none", "00000000000000000000000000000000", "none", "00000000000000000000000000000000");
			runStatement("INSERT INTO `" + tablePlayers + "` (`name`,`uuid`) VALUES (?,?) ON DUPLICATE KEY UPDATE `name`=?, `uuid`=?;", "Console", "00000000000000000000000000000001", "Console", "00000000000000000000000000000001");
		}
		else
		{
			runStatement("INSERT IGNORE INTO `" + tablePlayers + "` (`name`) VALUES (?);", "none");
			runStatement("INSERT IGNORE INTO `" + tablePlayers + "` (`name`) VALUES (?);", "Console");
		}

		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		unload(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerKick(PlayerKickEvent event)
	{
		unload(event.getPlayer());
	}

	private void unload(Player player)
	{
		playersToID.remove(player);
	}

	private void checkUUIDs()
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
		try(Connection connection = dataSource.getConnection())
		{
			Map<String, UpdateData> toConvert = new HashMap<>();
			List<UpdateData> toUpdate = new LinkedList<>();
			try(Statement stmt = connection.createStatement(); ResultSet res = stmt.executeQuery("SELECT `player_id`,`name`,`uuid` FROM `" + tablePlayers + "` WHERE `uuid` IS NULL OR `uuid` LIKE '%-%';"))
			{
				while(res.next())
				{
					if(res.isFirst())
					{
						plugin.log.info(plugin.lang.get("Console.UpdateUUIDs"));
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
				try(PreparedStatement ps = connection.prepareStatement("UPDATE `" + tablePlayers + "` SET `uuid`=? WHERE `player_id`=?;"))
				{
					for(UpdateData updateData : toUpdate)
					{
						ps.setString(1, updateData.uuid);
						ps.setInt(2, updateData.id);
						ps.addBatch();
						ps.executeBatch();
					}
				}
				plugin.log.info(String.format(plugin.lang.get("Console.UpdatedUUIDs"), toUpdate.size()));
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}

	public void Disable()
	{
		dataSource.close();
		namesToID.clear();
		playersToID.clear();
	}

	private void CheckDB()
	{
		try(Connection connection = dataSource.getConnection(); Statement stmt = connection.createStatement())
		{
			stmt.execute("CREATE TABLE IF NOT EXISTS `" + tablePlayers + "` (`player_id` INT NOT NULL AUTO_INCREMENT, `name` VARCHAR(20) NOT NULL" + ((plugin.UseUUIDs) ? "" : " UNIQUE") +", PRIMARY KEY (`player_id`));");
			if(plugin.UseUUIDs)
			{
				try
				{
					stmt.execute("ALTER TABLE `" + tablePlayers + "` ADD COLUMN `uuid` CHAR(32) UNIQUE;");
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
			}
			if(plugin.config.getUseMinepacks())
			{
				try
				{
					stmt.execute("ALTER TABLE `" + tablePlayers + "` ADD COLUMN `sharebackpack` TINYINT(1) NOT NULL DEFAULT false;");
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
			}
			stmt.execute("CREATE TABLE IF NOT EXISTS `" + tablePriests + "` (`priest_id` INT NOT NULL, PRIMARY KEY (`priest_id`));");
			stmt.execute("CREATE TABLE IF NOT EXISTS `" + tablePartners + "` (`marry_id` INT NOT NULL AUTO_INCREMENT, `player1` INT NOT NULL, `player2` INT NOT NULL, `priest` INT NULL, `pvp_state` TINYINT(1) NOT NULL DEFAULT false, `date` DATETIME NOT NULL, PRIMARY KEY (`marry_id`) );");
			if(plugin.config.getSurname())
			{
				try
				{
					stmt.execute("ALTER TABLE `" + tablePartners + "` ADD COLUMN `Surname` VARCHAR(35) UNIQUE;");
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
			}
			stmt.execute("CREATE TABLE IF NOT EXISTS " + tableHome + " (`marry_id` INT NOT NULL, `home_x` DOUBLE NOT NULL, `home_y` DOUBLE NOT NULL, `home_z` DOUBLE NOT NULL, `home_world` VARCHAR(45) NOT NULL DEFAULT 'world', PRIMARY KEY (`marry_id`) );");
			try
			{
				stmt.execute("ALTER TABLE `" + tableHome + "` ADD COLUMN `home_server` VARCHAR(45);");
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
			// Clean up database
			stmt.execute("DELETE FROM `" + tablePartners + "` WHERE player1=player2");
			stmt.execute("DELETE FROM `" + tablePartners + "` WHERE NOT EXISTS(SELECT NULL FROM `" + tablePlayers + "` WHERE `player_id`=`player1` OR `player_id`=`player2`)");
			stmt.execute("DELETE FROM `" + tablePriests + "` WHERE NOT EXISTS(SELECT NULL FROM `" + tablePlayers + "` WHERE `priest_id`=`player_id`)");
			stmt.execute("DELETE FROM `" + tableHome + "` WHERE NOT EXISTS(SELECT NULL FROM `" + tablePartners + "` WHERE `" + tableHome + "`.`marry_id`=`" + tablePartners + "`.`marry_id`)");
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}

	public void UpdatePlayer(final Player player)
	{
		if(!updatePlayer)
		{
			return;
		}
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable()
		{
			@Override
			public void run()
			{
				try(Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT `player_id` FROM `" + tablePlayers + "` WHERE `" + uuidOrName + "`=?;"))
				{
					ps.setString(1, getUUIDorName(player));
					try(ResultSet rs = ps.executeQuery())
					{
						if(rs.next())
						{
							playersToID.put(player, rs.getInt(1));
							if(plugin.UseUUIDs)
							{
								runStatementAsync("UPDATE `" + tablePlayers + "` SET `name`=? WHERE `uuid`=?;", player.getName(), player.getUniqueId().toString().replace("-", ""));
							}
						}
						else
						{
							try(PreparedStatement ps2 = con.prepareStatement("INSERT INTO `" + tablePlayers + "` (`name`" + ((plugin.UseUUIDs) ? ",`uuid`" : "") + ") VALUES (?" + ((plugin.UseUUIDs) ? ",?" : "") + ");", Statement.RETURN_GENERATED_KEYS))
							{
								ps2.setString(1, player.getName());
								if(plugin.UseUUIDs)
								{
									ps2.setString(2, player.getUniqueId().toString().replace("-", ""));
								}
								ps2.executeUpdate();
								try(ResultSet generatedKeys = ps2.getGeneratedKeys())
								{
									if(generatedKeys.next())
									{
										playersToID.put(player, generatedKeys.getInt(1));
									}
								}
							}
						}
					}
				}
				catch(SQLException e)
				{
					plugin.log.info("Failed to add user: " + player.getName());
					e.printStackTrace();
				}
			}
		});
	}

	private void runStatementAsync(final String query, final Object... args)
	{
		Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable()
		{
			@Override
			public void run()
			{
				runStatement(query, args);
			}
		});
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

	private int GetPlayerID(Player player)
	{
		int id = -1;
		Integer ID = playersToID.get(player);
		if(ID != null)
		{
			return ID;
		}
		try(Connection connection = dataSource.getConnection(); PreparedStatement ps = connection.prepareStatement("SELECT `player_id` FROM `" + tablePlayers + "` WHERE `" + uuidOrName + "`=?"))
		{
			ps.setString(1, getUUIDorName(player));
			try(ResultSet rs = ps.executeQuery())
			{
				if(rs.next())
				{
					id = rs.getInt(1);
					playersToID.put(player, id);
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return id;
	}

	private int GetPlayerID(String player)
	{
		int id = -1;
		Integer ID = namesToID.get(player);
		if(ID != null)
		{
			return ID;
		}
		try(Connection connection = dataSource.getConnection(); PreparedStatement ps = connection.prepareStatement("SELECT `player_id` FROM `" + tablePlayers + "` WHERE `name`=?"))
		{
			ps.setString(1, player);
			ps.executeQuery();
			try(ResultSet rs = ps.getResultSet())
			{
				if(rs.next())
				{
					id = rs.getInt(1);
					namesToID.put(player, id);
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return id;
	}

	private String GetPlayerName(int pid)
	{
		String name = null;
		try(Connection connection = dataSource.getConnection(); Statement stmt = connection.createStatement())
		{
			stmt.executeQuery("SELECT `name` FROM `" + tablePlayers + "` WHERE `player_id`=" + pid);
			try(ResultSet rs = stmt.getResultSet())
			{
				if(rs.next())
				{
					name = rs.getString(1);
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return name;
	}

	private String getUUIDorName(Player player)
	{
		return (plugin.UseUUIDs) ? player.getUniqueId().toString().replace("-", "") : player.getName();
	}

	public void SetPriest(Player priest)
	{
		int pid = GetPlayerID(priest);
		runStatementAsync("INSERT INTO `" + tablePriests + "` (`priest_id`) VALUE (?)", pid);
		if(plugin.pluginchannel != null) plugin.pluginchannel.sendMessage("updatePlayer", "setPriest", pid);
	}

	public void DelPriest(Player priest)
	{
		int pid = GetPlayerID(priest);
		runStatementAsync("DELETE FROM `" + tablePriests + "` WHERE `priest_id`=?;", pid);
		if(plugin.pluginchannel != null) plugin.pluginchannel.sendMessage("updatePlayer", "delPriest", pid);
	}

	public boolean IsPriest(Player priest)
	{
		try(Connection connection = dataSource.getConnection();
		    PreparedStatement ps = connection.prepareStatement("SELECT priest_id FROM `" + tablePriests + "` WHERE `priest_id` IN (SELECT `player_id` FROM `" + tablePlayers + "` WHERE `" + uuidOrName + "`=?);"))
		{
			ps.setString(1, getUUIDorName(priest));
			try(ResultSet rs = ps.executeQuery())
			{
				if(rs.next())
				{
					return true;
				}
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		return false;
	}

	public boolean GetPvPEnabled(Player player)
	{
		boolean res = false;
		try(Connection connection = dataSource.getConnection(); PreparedStatement ps = connection.prepareStatement("SELECT `pvp_state` FROM `" + tablePartners + "` WHERE `player1`=? OR `player2`=?"))
		{
			int pid = GetPlayerID(player);
			ps.setInt(1, pid);
			ps.setInt(2, pid);
			ps.executeQuery();
			try(ResultSet rs = ps.getResultSet())
			{
				if(rs.next())
				{
					res = rs.getBoolean(1);
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return res;
	}

	public String GetPartner(Player player)
	{
		String partner = null;
		int partnerID = -1;
		int pid = GetPlayerID(player);
		try(Connection connection = dataSource.getConnection(); PreparedStatement ps = connection.prepareStatement("SELECT `player1`,`player2` FROM `" + tablePartners + "` WHERE `player1`=? OR `player2`=?"))
		{
			ps.setInt(1, pid);
			ps.setInt(2, pid);
			ps.executeQuery();
			ResultSet rs = ps.getResultSet();
			if(rs.next())
			{
				partnerID = (rs.getInt(1) == pid) ? rs.getInt(2) : rs.getInt(1);
			}
			rs.close();
			ps.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		if(partnerID >= 0)
		{
			partner = GetPlayerName(partnerID);
		}
		return partner;
	}

	public void GetAllMarriedPlayers(final Callback<TreeMap<String, String>> finished)
	{
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			@Override
			public void run()
			{
				final TreeMap<String, String> MarryMap_out = new TreeMap<>();
				try(Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement();
				    ResultSet rs = statement.executeQuery("SELECT `mp1`.`name`,`mp2`.`name` FROM `" + tablePartners + "` INNER JOIN `" + tablePlayers + "` AS mp1 ON `player1`=`mp1`.`player_id` INNER JOIN `" + tablePlayers + "` AS mp2 ON `player2`=`mp2`.`player_id`"))
				{
					while(rs.next())
					{
						MarryMap_out.put(rs.getString(1), rs.getString(2));
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				Bukkit.getScheduler().runTask(plugin, new Runnable() {
					@Override
					public void run()
					{
						finished.onResult(MarryMap_out);
					}
				});
			}
		});
	}

	public void DelMarryHome(Player player)
	{
		DelMarryHome(GetPlayerID(player));
	}

	public void DelMarryHome(String player)
	{
		DelMarryHome(GetPlayerID(player));
	}

	private void DelMarryHome(int pid)
	{
		runStatementAsync("DELETE FROM `" + tableHome + "` WHERE `marry_id`=(SELECT `marry_id` FROM `" + tablePartners + "` WHERE `player1`=? OR `player2`=?);", pid, pid);
	}

	public void GetMarryHome(String player, Callback<Location> result)
	{
		GetMarryHome(GetPlayerID(player), result);
	}

	public void GetMarryHome(Player player, Callback<Location> result)
	{
		GetMarryHome(GetPlayerID(player), result);
	}

	private void GetMarryHome(final int pid, final Callback<Location> result)
	{
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable()
		{
			@Override
			public void run()
			{
				try(Connection connection = dataSource.getConnection();
						PreparedStatement pstmt = connection.prepareStatement("SELECT `home_x`,`home_y`,`home_z`,`home_world` FROM `" + tableHome + "` INNER JOIN `" + tablePartners + "` ON `" + tableHome + "`.`marry_id`=`" + tablePartners + "`.`marry_id` WHERE `player1`=? OR `player2`=?"))
				{
					pstmt.setInt(1, pid);
					pstmt.setInt(2, pid);
					pstmt.executeQuery();
					try(ResultSet rs = pstmt.getResultSet())
					{
						if(rs.next())
						{
							World world = plugin.getServer().getWorld(rs.getString(4));
							final Location loc = (world == null) ? null : new Location(world, rs.getDouble(1), rs.getDouble(2), rs.getDouble(3));
							Bukkit.getScheduler().runTask(plugin, new Runnable() { @Override public void run() { result.onResult(loc); }});
							return;
						}
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				Bukkit.getScheduler().runTask(plugin, new Runnable() { @Override public void run() { result.onResult(null); }});
			}
		});
	}

	public void SetMarryHome(Location loc, Player player)
	{
		int pid = GetPlayerID(player);
		runStatementAsync("REPLACE INTO `" + tableHome + "` (`marry_id`,`home_x`,`home_y`,`home_z`,`home_world`,`home_server`) SELECT `marry_id`,?,?,?,?,? FROM `" + tablePartners + "` WHERE `player1`=? OR `player2`=?;", loc.getX(), loc.getY(), loc.getZ(), loc.getWorld().getName(), plugin.HomeServer, pid, pid);
	}

	public void MarryPlayers(Player player, Player otherPlayer, Player priest, String surname)
	{
		MarryPlayers(player, otherPlayer, GetPlayerID(priest), surname);
	}

	public void MarryPlayers(final Player player, final Player otherPlayer, final String priest, final String surname)
	{
		MarryPlayers(player, otherPlayer, GetPlayerID(priest), surname);
	}

	public void MarryPlayers(final Player player, final Player otherPlayer, final int priest, final String surname)
	{
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			@Override
			public void run()
			{
				Object[] args = new Object[(plugin.config.getSurname()) ? 5 : 4];
				int player1 = GetPlayerID(player);
				args[0] = player1;
				args[1] = GetPlayerID(otherPlayer);
				args[2] = priest;
				args[3] = new Timestamp(Calendar.getInstance().getTime().getTime());
				if(plugin.config.getSurname())
				{
					args[4] = LimitText(surname, 34);
					runStatement("INSERT INTO `" + tablePartners + "` (`player1`, `player2`, `priest`, `date`, `surname`) VALUES (?,?,?,?,?);", args);
				}
				else
				{
					runStatement("INSERT INTO `" + tablePartners + "` (`player1`, `player2`, `priest`, `date`) VALUES (?,?,?,?);", args);
				}
				if(plugin.pluginchannel != null)
				{
					try(Connection connection = dataSource.getConnection(); PreparedStatement ps = connection.prepareStatement("SELECT `marry_id` FROM `" + tablePartners + "` WHERE `player1`=? OR `player2`=?"))
					{
						ps.setInt(1, player1);
						ps.setInt(2, player1);
						try(ResultSet rs = ps.executeQuery())
						{
							if(rs.next())
							{
								final int marryID = rs.getInt(1);
								Bukkit.getScheduler().runTask(plugin, new Runnable()
								{
									@Override
									public void run()
									{
										plugin.pluginchannel.sendMessage("updateMarriage", "marry", marryID);
									}
								});
							}
						}
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		});
	}

	public void DivorcePlayer(final Player player)
	{
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			@Override
			public void run()
			{
				int pid = GetPlayerID(player);
				try(Connection connection = dataSource.getConnection(); PreparedStatement ps = connection.prepareStatement("SELECT `marry_id` FROM `" + tablePartners + "` WHERE `player1`=? OR `player2`=?"))
				{
					ps.setInt(1, pid);
					ps.setInt(2, pid);
					try(ResultSet rs = ps.executeQuery())
					{
						if(rs.next())
						{
							final int marryID = rs.getInt(1);
							runStatementAsync("DELETE `p`,`h` FROM `" + tablePartners + "` AS `p` LEFT OUTER JOIN `" + tableHome + "` AS `h` USING (`marry_id`) WHERE `p`.`marry_id`=?;", marryID);
							if(plugin.pluginchannel != null)
							{
								Bukkit.getScheduler().runTask(plugin, new Runnable()
								{
									@Override
									public void run()
									{
										plugin.pluginchannel.sendMessage("updateMarriage", "divorce", marryID);
									}
								});
							}
						}
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	public void SetPvPEnabled(final Player player, final boolean state)
	{
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			@Override
			public void run()
			{
				int pid = GetPlayerID(player);
				try(Connection connection = dataSource.getConnection(); PreparedStatement ps = connection.prepareStatement("SELECT `marry_id` FROM `" + tablePartners + "` WHERE `player1`=? OR `player2`=?"))
				{
					ps.setInt(1, pid);
					ps.setInt(2, pid);
					try(ResultSet rs = ps.executeQuery())
					{
						if(rs.next())
						{
							final int marryID = rs.getInt(1);
							runStatement("UPDATE `" + tablePartners + "` SET `pvp_state`=? WHERE `marry_id`=?", state, marryID);
							if(plugin.pluginchannel != null)
							{
								Bukkit.getScheduler().runTask(plugin, new Runnable()
								{
									@Override
									public void run()
									{
										plugin.pluginchannel.sendMessage("updateMarriage", "pvpState", marryID, state);
									}
								});
							}
						}
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	public String GetSurname(Player player)
	{
		String surname = null;
		try(Connection connection = dataSource.getConnection(); PreparedStatement pstmt = connection.prepareStatement("SELECT `surname` FROM `" + tablePartners + "` WHERE `player1`=? OR `player2`=?"))
		{
			int pid = GetPlayerID(player);
			pstmt.setInt(1, pid);
			pstmt.setInt(2, pid);
			pstmt.executeQuery();
			try(ResultSet rs = pstmt.getResultSet())
			{
				if(rs.next())
				{
					surname = rs.getString(1);
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return surname;
	}

	public void SetSurname(final Player player, final String surname)
	{
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			@Override
			public void run()
			{
				int pid = GetPlayerID(player);

				try(Connection connection = dataSource.getConnection(); PreparedStatement ps = connection.prepareStatement("SELECT `marry_id` FROM `" + tablePartners + "` WHERE `player1`=? OR `player2`=?"))
				{
					ps.setInt(1, pid);
					ps.setInt(2, pid);
					try(ResultSet rs = ps.executeQuery())
					{
						if(rs.next())
						{
							final int marryID = rs.getInt(1);
							final String text = LimitText(surname, 34);
							runStatement("UPDATE `" + tablePartners + "` SET `surname`=? WHERE `marry_id`=?", text, marryID);
							if(plugin.pluginchannel != null)
							{
								Bukkit.getScheduler().runTask(plugin, new Runnable()
								{
									@Override
									public void run()
									{
										plugin.pluginchannel.sendMessage("updateMarriage", "surname", marryID, text);
									}
								});
							}
						}
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	public void SetShareBackpack(Player player, boolean allow)
	{
		int pid = GetPlayerID(player);
		runStatementAsync("UPDATE `" + tablePlayers + "` SET `sharebackpack`=? WHERE `player_id`=?;", allow, pid);
		if(plugin.pluginchannel != null) plugin.pluginchannel.sendMessage("updatePlayer", "shareBackpack", pid, allow);
	}

	public boolean GetPartnerShareBackpack(Player player)
	{
		boolean result = false;
		try(Connection connection = dataSource.getConnection(); PreparedStatement ps = connection.prepareStatement("SELECT `sharebackpack` FROM `" + tablePlayers + "` WHERE `" + uuidOrName + "`=?;"))
		{
			ps.setString(1, getUUIDorName(player));
			try(ResultSet rs = ps.executeQuery())
			{
				if(rs.next())
				{
					result = rs.getBoolean(1);
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}
}
