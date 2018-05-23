/*
 *   Copyright (C) 2016-2018 GeorgH93
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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Database;

import at.pcgamingfreaks.ConsoleColor;
import at.pcgamingfreaks.Database.DBTools;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Home;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Database.FilesMigrator.MigrationMarriage;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Database.FilesMigrator.MigrationPlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Database.Helper.StructMarriageSQL;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Database.*;
import at.pcgamingfreaks.UUIDConverter;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.*;

//@SuppressWarnings("JpaQueryApiInspection")
public abstract class SQL extends Database implements SQLBasedDatabase
{
	//TODO: resync on player join with bungee = true
	protected static final long RETRY_DELAY = 5; // 5Ticks = 250ms, should be more than enough to get the player id, especially since the id's should have already been loaded a long time ago.

	private HikariDataSource dataSource; // SQL Connection Pool
	protected boolean bungee;

	//region Query related variables
	// Table Names
	protected String tablePlayers = "marry_players", tableMarriages = "marry_partners", tablePriests = "marry_priests", tableHomes = "marry_home";
	//region Field Names
	protected String fieldPlayerID = "player_id", fieldName = "name", fieldUUID = "uuid", fieldShareBackpack = "sharebackpack", fieldSurname = "surname"; // Player
	protected String fieldMarryID = "marry_id", fieldPlayer1 = "player1", fieldPlayer2 = "player2", fieldPriest = "priest", fieldPVPState = "pvp_state", fieldDate = "date"; // Marriage
	protected String fieldHomeX = "home_x", fieldHomeY = "home_y", fieldHomeZ = "home_z", fieldHomeWorld = "home_world", fieldHomeServer = "home_server"; // Home
	//endregion
	//region Querys
	protected String queryDelHome = "DELETE FROM {THomes} WHERE {FMarryID}=?;", queryUpdateHome = "REPLACE INTO {THomes} ({FMarryID},{FHomeX},{FHomeY},{FHomeZ},{FHomeWorld},{FHomeServer}) VALUES (?,?,?,?,?,?);";
	protected String queryPvPState = "UPDATE {TMarriages} SET {FPvPState}=? WHERE {FMarryID}=?;", querySetSurname = "UPDATE {TMarriages} SET {FSurname}=? WHERE {FMarryID}=?;";
	protected String queryDelMarriage = "DELETE FROM {TMarriages} WHERE {FMarryID}=?;", querySetBackpackShareState = "UPDATE {TPlayers} SET {FShareBackpack}=? WHERE {FPlayerID}=?;";
	protected String queryMarry = "INSERT INTO {TMarriages} ({FPlayer1},{FPlayer2},{FPriest},{FPvPState},{FDate}) VALUES (?,?,?,?,?);", queryLoadHome = "SELECT * FROM {THomes} WHERE {FMarryID}=?";
	protected String querySetPriest = "REPLACE INTO {TPriests} ({FPlayerID}) VALUE (?);", queryRemovePriest = "DELETE FROM {TPriests} WHERE {FPlayerID}=?;";
	protected String queryLoadPlayer = "SELECT * FROM {TPlayers} WHERE {FUUID}=?;", queryAddPlayer = "INSERT IGNORE INTO {TPlayers} ({FName},{FUUID},{FShareBackpack}) VALUES (?,?,?);";
	protected String queryLoadHomes = "SELECT * FROM {THomes};", queryIsPriest = "SELECT * FROM {TPriests} WHERE {FPlayerID}=?;", queryLoadPlayerFromId = "SELECT * FROM {TPlayers} WHERE {FPlayerID}=?;";
	protected String queryLoadPlayersFromID = "SELECT * FROM {TPlayers} WHERE {FPlayerID} IN ({IDs});", queryLoadPriests = "SELECT {FPlayerID} FROM {TPriests};";
	protected String queryLoadMarriages = "SELECT * FROM {TMarriages}", queryLoadMarriage = "SELECT * FROM {TMarriages} WHERE {FMarryID}=?";
	protected String queryGetUnsetOrInvalidUUIDs = "SELECT {FPlayerID},{FName},{FUUID} FROM {TPlayers} WHERE {FUUID} IS NULL OR {FUUID} ", queryFixUUIDs = "UPDATE {TPlayers} SET {FUUID}=? WHERE {FPlayerID}=?;";
	protected String queryPlayerID = "SELECT {FPlayerID} FROM {TPlayers} WHERE {FUUID}=?;", queryUpdatePlayer = "UPDATE {TPlayers} SET {FName}=? WHERE {FPlayerID}=?;";
	//endregion
	//endregion

	protected SQL(MarriageMaster marriageMaster)
	{
		super(marriageMaster);
		try
		{
			HikariConfig poolConfig = getPoolConfig();
			if(poolConfig != null)
			{
				poolConfig.setPoolName("MarriageMaster-Connection-Pool");
				poolConfig.addDataSourceProperty("useUnicode", "true");
				poolConfig.addDataSourceProperty("characterEncoding", "utf-8");
				//noinspection SpellCheckingInspection
				poolConfig.addDataSourceProperty("cachePrepStmts", "true");
				poolConfig.addDataSourceProperty("prepStmtCacheSize", "250");
				poolConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
				dataSource = new HikariDataSource(poolConfig);
			}
		}
		catch(Exception e)
		{
			plugin.getLogger().warning(ConsoleColor.RED + "There was a problem creating the connection pool for the SQL server! Please check your configuration." + ConsoleColor.RESET);
		}
		bungee = supportBungee() && plugin.getConfiguration().isBungeeEnabled();
		loadTableAndFieldNames();
		buildQuerys();
	}

	@Override
	protected void startup() throws Exception
	{
		getConnection().close(); // We test if we can get a connection. This way we can easily check if the settings are correct.
		checkDatabase();
		super.startup();
	}

	@Override
	public void close()
	{
		if (dataSource != null && !dataSource.isClosed())
		{
			dataSource.close();
			dataSource = null;
		}
		super.close();
	}

	@Override
	public boolean useBungee()
	{
		return bungee;
	}

	protected String getEngine()
	{
		return "";
	}

	/**
	 * Gets a config for a Hikari connection pool.
	 *
	 * @return The config for the connection pool. Null to disable the internal connection handling, override the {@link #getConnection() getConnection} method!
	 */
	protected abstract @Nullable HikariConfig getPoolConfig();

	/**
	 * Override this if you don't want to use the internal connection handling.
	 *
	 * @return A JDBC connection to a SQL database.
	 * @throws SQLException If there was a problem.
	 */
	@Override
	public @NotNull Connection getConnection() throws SQLException
	{
		return dataSource.getConnection();
	}

	protected void loadTableAndFieldNames()
	{
		// Load table names from config
		tablePlayers   = plugin.getConfiguration().getSQLTableUser();
		tablePriests   = plugin.getConfiguration().getSQLTablePriests();
		tableMarriages = plugin.getConfiguration().getSQLTableMarriages();
		tableHomes     = plugin.getConfiguration().getSQLTableHomes();
		// Load field names from config
		fieldPlayerID       = plugin.getConfiguration().getSQLField("PlayerID",     fieldPlayerID);
		fieldName           = plugin.getConfiguration().getSQLField("PlayerName",   fieldName);
		fieldUUID           = plugin.getConfiguration().getSQLField("PlayerUUID",   fieldUUID);
		fieldShareBackpack  = plugin.getConfiguration().getSQLField("PlayerShareBackpack", fieldShareBackpack);
		fieldMarryID        = plugin.getConfiguration().getSQLField("MarryID",      fieldMarryID);
		fieldPlayer1        = plugin.getConfiguration().getSQLField("MarryPlayer1", fieldPlayer1);
		fieldPlayer1        = plugin.getConfiguration().getSQLField("MarryPlayer2", fieldPlayer1);
		fieldPriest         = plugin.getConfiguration().getSQLField("MarryPriest",  fieldPriest);
		fieldDate           = plugin.getConfiguration().getSQLField("MarryDate",    fieldDate);
		fieldPVPState       = plugin.getConfiguration().getSQLField("MarryPvPState",fieldPVPState);
		fieldHomeX          = plugin.getConfiguration().getSQLField("HomeX",        fieldHomeX);
		fieldHomeY          = plugin.getConfiguration().getSQLField("HomeY",        fieldHomeY);
		fieldHomeZ          = plugin.getConfiguration().getSQLField("HomeZ",        fieldHomeZ);
		fieldHomeWorld      = plugin.getConfiguration().getSQLField("HomeWorld",    fieldHomeWorld);
		fieldHomeServer     = plugin.getConfiguration().getSQLField("HomeServer",   fieldHomeServer);
	}

	protected void buildQuerys()
	{
		if(!bungee)
		{
			queryUpdateHome = queryUpdateHome.replaceAll(",\\{FHomeServer}", "").replace("(?,?,?,?,?,?)", "(?,?,?,?,?)");
		}
		if(plugin.isSurnamesEnabled())
		{
			queryMarry         = "INSERT INTO {TMarriages} ({FPlayer1},{FPlayer2},{FPriest},{FPvPState},{FDate},{FSurname}) VALUES (?,?,?,?,?,?);";
			queryLoadMarriages = "SELECT {FMarryID},{FPlayer1},{FPlayer2},{FPriest},{FPvPState},{FDate},{FSurname} FROM {TMarriages}";
		}
		if(plugin.getBackpacksIntegration() == null)
		{
			queryAddPlayer  = queryAddPlayer.replace(",{FShareBackpack}) VALUES (?,", ") VALUES (");
		}
		if(!useUUIDs)
		{
			queryAddPlayer  = queryAddPlayer.replace(",{FUUID}", "").replaceAll("\\?,\\?\\);", "?);");
			queryLoadPlayer = queryLoadPlayer.replace("{FUUID}", "{FName}");
			queryPlayerID   = queryPlayerID.replace("{FUUID}", "{FName}");
		}
		queryGetUnsetOrInvalidUUIDs += (useUUIDSeparators) ? "NOT LIKE '%-%-%-%-%';" : "LIKE '%-%';";
		setTableAndFieldNames();
	}

	protected void setTableAndFieldNames()
	{
		queryDelHome       = replacePlaceholders(queryDelHome);
		queryUpdateHome    = replacePlaceholders(queryUpdateHome);
		queryLoadHome      = replacePlaceholders(queryLoadHome);
		queryPvPState      = replacePlaceholders(queryPvPState);
		querySetSurname    = replacePlaceholders(querySetSurname);
		queryDelMarriage   = replacePlaceholders(queryDelMarriage);
		queryMarry         = replacePlaceholders(queryMarry);
		querySetPriest     = replacePlaceholders(querySetPriest);
		queryRemovePriest  = replacePlaceholders(queryRemovePriest);
		queryLoadPlayer    = replacePlaceholders(queryLoadPlayer);
		queryAddPlayer     = replacePlaceholders(queryAddPlayer);
		queryPlayerID      = replacePlaceholders(queryPlayerID);
		queryLoadHomes     = replacePlaceholders(queryLoadHomes);
		queryLoadPriests   = replacePlaceholders(queryLoadPriests);
		queryFixUUIDs      = replacePlaceholders(queryFixUUIDs);
		queryIsPriest      = replacePlaceholders(queryIsPriest);
		queryUpdatePlayer  = replacePlaceholders(queryUpdatePlayer);
		queryLoadMarriages = replacePlaceholders(queryLoadMarriages);
		queryLoadPlayerFromId       = replacePlaceholders(queryLoadPlayerFromId);
		queryLoadPlayersFromID      = replacePlaceholders(queryLoadPlayersFromID);
		querySetBackpackShareState  = replacePlaceholders(querySetBackpackShareState);
		queryGetUnsetOrInvalidUUIDs = replacePlaceholders(queryGetUnsetOrInvalidUUIDs);
	}

	protected String replacePlaceholders(String query)
	{
		return query.replaceAll("(\\{\\w+})", "`$1`").replaceAll("`(\\{\\w+})`_(\\w+)", "`$1_$2`").replaceAll("fk_`(\\{\\w+})`_`(\\{\\w+})`_`(\\{\\w+})`", "`fk_$1_$2_$3`") // Fix name formatting
				.replaceAll("\\{TPlayers}", tablePlayers).replaceAll("\\{TMarriages}", tableMarriages).replaceAll("\\{TPriests}", tablePriests).replaceAll("\\{THomes}", tableHomes) // Table names
				.replaceAll("\\{FPlayerID}", fieldPlayerID).replaceAll("\\{FName}", fieldName).replaceAll("\\{FUUID}", fieldUUID).replaceAll("\\{FShareBackpack}", fieldShareBackpack) // Player fields
				.replaceAll("\\{FMarryID}", fieldMarryID).replaceAll("\\{FSurname}", fieldSurname).replaceAll("\\{FPlayer1}", fieldPlayer1).replaceAll("\\{FPlayer2}", fieldPlayer2) // Marriage fields
				.replaceAll("\\{FPriest}", fieldPriest).replaceAll("\\{FPvPState}", fieldPVPState).replaceAll("\\{FDate}", fieldDate).replaceAll("\\{FHomeServer}", fieldHomeServer)
				.replaceAll("\\{FHomeX}", fieldHomeX).replaceAll("\\{FHomeY}", fieldHomeY).replaceAll("\\{FHomeZ}", fieldHomeZ).replaceAll("\\{FHomeWorld}", fieldHomeWorld); // Home fields
	}

	protected void checkDatabase()
	{
		try(Connection connection = getConnection())
		{
			@Language("SQL") String  queryTPlayers = replacePlaceholders("CREATE TABLE {TPlayers} (\n{FPlayerID} INT UNSIGNED NOT NULL AUTO_INCREMENT,\n{FName} VARCHAR(16) NOT NULL,\n{FUUID} CHAR(36) DEFAULT NULL,\n" +
					"{FShareBackpack} TINYINT(1) NOT NULL DEFAULT 0,\nPRIMARY KEY ({FPlayerID}),\nUNIQUE INDEX {FUUID}_UNIQUE ({FUUID})\n)" + getEngine() + ";"),
					queryTPriests = replacePlaceholders("CREATE TABLE {TPriests} (\n{FPlayerID} INT UNSIGNED NOT NULL,\nPRIMARY KEY ({FPlayerID}),\n" +
							"CONSTRAINT fk_{TPriests}_{TPlayers}_{FPlayerID} FOREIGN KEY ({FPlayerID}) REFERENCES {TPlayers} ({FPlayerID}) ON DELETE CASCADE ON UPDATE CASCADE\n)" + getEngine() + ";"),
					queryTMarriages = replacePlaceholders("CREATE TABLE {TMarriages} (\n{FMarryID} INT UNSIGNED NOT NULL AUTO_INCREMENT,\n{FPlayer1} INT UNSIGNED NOT NULL,\n{FPlayer2} INT UNSIGNED NOT NULL,\n" +
							"{FPriest} INT UNSIGNED NULL,\n{FSurname} VARCHAR(45) NULL,\n{FPvPState} TINYINT(1) NOT NULL DEFAULT 0,\n{FDate} DATETIME NOT NULL DEFAULT NOW(),\n" +
							"PRIMARY KEY ({FMarryID}),\nINDEX {FPlayer1}_idx ({FPlayer1}),\nINDEX {FPlayer2}_idx ({FPlayer2}),\nINDEX {FPriest}_idx ({FPriest}),\n" +
							"CONSTRAINT fk_{TMarriages}_{TPlayers}_{FPlayer1} FOREIGN KEY ({FPlayer1}) REFERENCES {TPlayers} ({FPlayerID}) ON DELETE CASCADE ON UPDATE CASCADE,\n" +
							"CONSTRAINT fk_{TMarriages}_{TPlayers}_{FPlayer2} FOREIGN KEY ({FPlayer2}) REFERENCES {TPlayers} ({FPlayerID}) ON DELETE CASCADE ON UPDATE CASCADE,\n" +
							"CONSTRAINT fk_{TMarriages}_{TPlayers}_{FPriest} FOREIGN KEY ({FPriest}) REFERENCES {TPlayers} ({FPlayerID}) ON DELETE SET NULL ON UPDATE CASCADE\n)" + getEngine() + ";"),
					queryTHomes = replacePlaceholders("CREATE TABLE {THomes} (\n{FMarryID} INT UNSIGNED NOT NULL,\n{FHomeX} DOUBLE NOT NULL,\n{FHomeY} DOUBLE NOT NULL,\n" +
							"{FHomeZ} DOUBLE NOT NULL,\n{FHomeWorld} VARCHAR(45) NOT NULL DEFAULT 'world',\n" + ((bungee) ? "{FHomeServer} VARCHAR(45) DEFAULT NULL,\n" : "") + "PRIMARY KEY ({FMarryID}),\n" +
							"CONSTRAINT fk_{THomes}_{TMarriages}_{FMarryID} FOREIGN KEY ({FMarryID}) REFERENCES {TMarriages} ({FMarryID}) ON DELETE CASCADE ON UPDATE CASCADE\n)" + getEngine() + ";");
			DBTools.updateDB(connection, queryTPlayers);
			DBTools.updateDB(connection, queryTPriests);
			DBTools.updateDB(connection, queryTMarriages);
			DBTools.updateDB(connection, queryTHomes);
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}

	//region Helper functions for async querys
	protected void runAsync(Runnable runnable, @NotNull DatabaseElement databaseElement)
	{
		runAsync(runnable, (databaseElement.getDatabaseKey() == null) ? RETRY_DELAY : 0);
	}

	protected void runStatementAsyncIncludeKey(@NotNull String query, @NotNull DatabaseElement databaseElement, @NotNull Object... args)
	{
		runAsync(new DbElementStatementWithKeyRunnable(this, databaseElement, query, args), databaseElement);
	}

	protected void runStatementAsyncIncludeKeyFirst(@NotNull String query, @NotNull DatabaseElement databaseElement, @NotNull Object... args)
	{
		runAsync(new DbElementStatementWithKeyFirstRunnable(this, databaseElement, query, args), databaseElement);
	}
	//endregion

	@Override
	protected void checkUUIDs()
	{
		try(Connection connection = getConnection())
		{
			Map<String, UpdateUUIDsHelper> toConvert = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			List<UpdateUUIDsHelper> toUpdate = new LinkedList<>();
			try(Statement stmt = connection.createStatement(); ResultSet res = stmt.executeQuery(queryGetUnsetOrInvalidUUIDs))
			{
				while(res.next())
				{
					if(res.isFirst())
					{
						plugin.getLogger().info(MESSAGE_UPDATE_UUIDS);
					}
					String uuid = res.getString(fieldUUID);
					if(uuid == null)
					{
						toConvert.put(res.getString(fieldName), new UpdateUUIDsHelper(null, res.getInt(fieldPlayerID)));
					}
					else
					{
						uuid = (useUUIDSeparators) ? uuid.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5") : uuid.replaceAll("-", "");
						toUpdate.add(new UpdateUUIDsHelper(uuid, res.getInt(fieldPlayerID)));
					}
				}
			}
			if(toConvert.size() > 0 || toUpdate.size() > 0)
			{
				if(toConvert.size() > 0)
				{
					Map<String, String> newUUIDs = UUIDConverter.getUUIDsFromNames(toConvert.keySet(), useOnlineUUIDs, useUUIDSeparators);
					for(Map.Entry<String, String> entry : newUUIDs.entrySet())
					{
						UpdateUUIDsHelper updateData = toConvert.get(entry.getKey());
						updateData.setUUID(entry.getValue());
						toUpdate.add(updateData);
					}
				}
				try(PreparedStatement ps = connection.prepareStatement(queryFixUUIDs))
				{
					for(UpdateUUIDsHelper updateData : toUpdate)
					{
						DBTools.setParameters(ps, updateData.getUUID(), updateData.getId());
						ps.addBatch();
					}
					ps.executeBatch();
				}
				plugin.getLogger().info(String.format(MESSAGE_UPDATED_UUIDS, toUpdate.size()));
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}

	public void resync()
	{
		//TODO fill me
	}

	@Override
	protected void loadAll()
	{
		Set<StructMarriageSQL> marriagesSet = new HashSet<>();
		Set<Integer> playerToLoad = new FilteredHashSet<>(element -> element >= 0);
		try(Connection connection = getConnection())
		{
			// Load marriages
			plugin.getLogger().info("Loading marriages ...");
			try(PreparedStatement ps = connection.prepareStatement(queryLoadMarriages); ResultSet rs = ps.executeQuery())
			{
				while(rs.next())
				{
					marriagesSet.add(new StructMarriageSQL(rs.getInt(fieldMarryID), rs.getInt(fieldPlayer1), rs.getInt(fieldPlayer2), rs.getInt(fieldPriest), rs.getBoolean(fieldPVPState),
					                                       plugin.isSurnamesEnabled() ? rs.getString(fieldSurname) : null, rs.getTimestamp(fieldDate)));
					playerToLoad.add(rs.getInt(fieldPlayer1));
					playerToLoad.add(rs.getInt(fieldPlayer2));
					if(rs.getObject(fieldPriest) != null) playerToLoad.add(rs.getInt(fieldPriest));
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			plugin.getLogger().info("Marriages loaded");
			// Load priests
			plugin.getLogger().info("Loading priests ...");
			Set<Integer> priests = new HashSet<>();
			try(PreparedStatement ps = connection.prepareStatement(queryLoadPriests); ResultSet rs = ps.executeQuery())
			{
				while(rs.next())
				{
					priests.add(rs.getInt(fieldPlayerID));
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			plugin.getLogger().info("Priests loaded");
			// Load players
			plugin.getLogger().info("Loading players ...");
			//TODO validate the performance loss for individual querys to load the data
			if(!playerToLoad.isEmpty())
			{
				StringBuilder stringBuilder = new StringBuilder("");
				for(int ignored : playerToLoad)
				{
					if(stringBuilder.length() > 0)
					{
						stringBuilder.append(',');
					}
					stringBuilder.append('?');
				}
				try(PreparedStatement ps = connection.prepareStatement(queryLoadPlayersFromID.replace("`{IDs}`", stringBuilder.toString())))
				{
					int i = 0;
					for(int pid : playerToLoad)
					{
						ps.setInt(++i, pid);
					}
					try(ResultSet rs = ps.executeQuery())
					{
						while(rs.next())
						{
							MarriagePlayerData player = new MarriagePlayerData(getUUIDFromIdentifier(rs.getString(useUUIDs ? fieldUUID : fieldName)), rs.getString(fieldName), (plugin.getBackpacksIntegration() != null) && rs.getBoolean(fieldShareBackpack), priests.contains(rs.getInt(fieldPlayerID)), rs.getInt(fieldPlayerID));
							cache.cache(player);
						}
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			plugin.getLogger().info("Players loaded");
			// Load the marriages into the cache
			plugin.getLogger().info("Writing marriages into cache ...");
			for(StructMarriageSQL sm : marriagesSet)
			{
				cache.cache(new MarriageData(cache.getPlayerFromDbKey(sm.p1ID), cache.getPlayerFromDbKey(sm.p2ID), cache.getPlayerFromDbKey(sm.priest), sm.date, sm.surname, sm.pvp, null, sm.marryID));
			}
			plugin.getLogger().info("Marriages loaded into cache");
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		loadHomesDelayed();
	}

	public void loadMarriage(final int marriageId)
	{
		runAsync(() -> {
			try(Connection connection = getConnection(); PreparedStatement ps = connection.prepareStatement(queryLoadMarriage))
			{
				ps.setInt(1, marriageId);
				try(ResultSet rs =  ps.executeQuery())
				{
					MarriagePlayerData player1 = playerFromId(connection, rs.getInt(fieldPlayer1)), player2 = playerFromId(connection, rs.getInt(fieldPlayer2));
					MarriagePlayerData priest = (rs.getObject(fieldPriest) == null) ? null : playerFromId(connection, rs.getInt(fieldPriest));
					if(player1 == null || player2 == null)
					{
						plugin.getLogger().warning("Failed to load marriage (id: " + marriageId + ") cause one of it's players could not be loaded successful!");
						return;
					}
					String surname = plugin.isSurnamesEnabled() ? rs.getString(fieldSurname) : null;
					MarriageData marriage = new MarriageData(player1, player2, priest, rs.getTimestamp(fieldDate), surname, rs.getBoolean(fieldPVPState), null, marriageId);
					cache.cache(marriage);
					loadHome(marriage);
				}
			}
			catch(SQLException e)
			{
				e.printStackTrace();
			}
		});
	}

	protected @Nullable MarriagePlayerData playerFromId(@NotNull Connection connection, int id) throws SQLException
	{
		if(cache.isPlayerFromDbKeyLoaded(id)) return cache.getPlayerFromDbKey(id);
		// No cache for the player, load him
		try(PreparedStatement ps = connection.prepareStatement(queryLoadPlayerFromId))
		{
			ps.setInt(1, id);
			try(ResultSet rs = ps.executeQuery())
			{
				if(rs.next())
				{
					MarriagePlayerData player = new MarriagePlayerData(getUUIDFromIdentifier(rs.getString(useUUIDs ? fieldUUID : fieldName)), rs.getString(fieldName),
					                                                   (plugin.getBackpacksIntegration() != null) && rs.getBoolean(fieldShareBackpack), false, rs.getInt(fieldPlayerID));
					cache.cache(player);
					return player;
				}
			}
		}
		return null;
	}

	protected void loadHomesDelayed()
	{
		runAsync(() -> {
			plugin.getLogger().info("Loading homes ...");
			Map<Integer, Home> homes = new HashMap<>();
			try(Connection connection = getConnection(); PreparedStatement ps = connection.prepareStatement(queryLoadHomes); ResultSet rs = ps.executeQuery())
			{
				while(rs.next())
				{
					String homeServer = (bungee) ? rs.getString(fieldHomeServer) : null;
					homes.put(rs.getInt(fieldMarryID), new MarriageHome(rs.getString(fieldHomeWorld), rs.getDouble(fieldHomeX), rs.getDouble(fieldHomeY), rs.getDouble(fieldHomeZ), homeServer));
				}
				for(Marriage marriage : cache.getLoadedMarriages())
				{
					if(marriage instanceof MarriageData)
					{
						MarriageData m = (MarriageData) marriage;
						if(m.getDatabaseKey() != null && m.getDatabaseKey() instanceof Integer)
						{
							m.setHomeLoc(homes.get(m.getDatabaseKey()));
						}
					}
				}
			}
			catch(SQLException e)
			{
				e.printStackTrace();
			}
			homes.clear();
			plugin.getLogger().info("Homes loaded");
		});
	}

	public void loadHome(final MarriageData marriage)
	{
		runAsync(() -> {
			if(marriage.getDatabaseKey() instanceof Integer) // If there is no db key we can't store it
			{
				try(Connection connection = getConnection(); PreparedStatement ps = connection.prepareStatement(queryLoadHome))
				{
					ps.setInt(1, (int) marriage.getDatabaseKey());
					try(ResultSet rs = ps.executeQuery())
					{
						if(rs.next())
						{
							String homeServer = (bungee) ? rs.getString(fieldHomeServer) : null;
							marriage.setHomeLoc(new MarriageHome(rs.getString(fieldHomeWorld), rs.getDouble(fieldHomeX), rs.getDouble(fieldHomeY), rs.getDouble(fieldHomeZ), homeServer));
						}
						else
						{
							marriage.setHomeLoc(null);
						}
					}
				}
				catch(SQLException e)
				{
					e.printStackTrace();
				}
			}
		}, marriage);
	}

	@Override
	protected void load(final @NotNull MarriagePlayerData player)
	{
		if(player.getDatabaseKey() == null || bungee)
		{
			runAsync(() -> {
				try(Connection connection = getConnection())
				{
					if((player.getDatabaseKey() != null && !bungee) || queryPlayer(player, connection))
					{
						if(useUUIDs && player.isOnline())
						{
							update(player, connection);
						}
					}
					else
					{
						add(player, connection);
					}
				}
				catch(SQLException e)
				{
					e.printStackTrace();
				}
			});
		}
	}

	protected void add(final MarriagePlayerData player, final Connection connection) throws SQLException
	{
		try(PreparedStatement psAdd = connection.prepareStatement(queryAddPlayer, Statement.RETURN_GENERATED_KEYS))
		{
			int i = 1;
			psAdd.setString(i++, player.getName());
			if(useUUIDs) psAdd.setString(i++, getUsedPlayerIdentifier(player));
			if(plugin.getBackpacksIntegration() != null) psAdd.setBoolean(i++, player.isSharingBackpack());
			if(psAdd.getParameterMetaData().getParameterCount() == i && useUUIDs)
			{
				psAdd.setString(i, getUsedPlayerIdentifier(player));
			}
			psAdd.executeUpdate();
			try(ResultSet rs = psAdd.getGeneratedKeys())
			{
				if(rs.next())
				{
					player.setDatabaseKey(rs.getInt(1));
					cache.addDbKey(player);
				}
				else
				{
					queryPlayer(player, connection);
				}
			}
		}
	}

	protected void update(final MarriagePlayerData player, final Connection connection) throws SQLException
	{
		String onlineName = player.getOnlineName();
		if(onlineName == null) return;
		player.setName(onlineName);
		DBTools.runStatement(connection, queryUpdatePlayer, onlineName, player.getDatabaseKey());
	}

	protected boolean queryPlayer(final MarriagePlayerData player, final Connection connection) throws SQLException
	{
		try(PreparedStatement psQuery = connection.prepareStatement(queryLoadPlayer))
		{
			psQuery.setString(1, getUsedPlayerIdentifier(player));
			try(ResultSet rs = psQuery.executeQuery())
			{
				if(rs.next())
				{
					player.setDatabaseKey(rs.getInt(fieldPlayerID));
					cache.addDbKey(player);
					if(plugin.getBackpacksIntegration() != null) player.setSharesBackpack(rs.getBoolean(fieldShareBackpack));
					try(PreparedStatement ps = connection.prepareStatement(queryIsPriest))
					{
						ps.setInt(1, rs.getInt(fieldPlayerID));
						try(ResultSet resultSet = ps.executeQuery())
						{
							player.setIsPriest(resultSet.next());
						}
					}
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void updateBackpackShareState(final MarriagePlayerData player)
	{
		runStatementAsyncIncludeKey(querySetBackpackShareState, player, player.isSharingBackpack());
	}

	@Override
	public void updatePriestStatus(final MarriagePlayerData player)
	{
		runStatementAsyncIncludeKey(player.isPriest() ? querySetPriest : queryRemovePriest, player);
	}

	@Override
	public void updateHome(final MarriageData marriage)
	{
		Home home = marriage.getHome();
		if(home == null)
		{
			runStatementAsyncIncludeKey(queryDelHome, marriage);
		}
		else
		{
			if(bungee)
			{
				runStatementAsyncIncludeKeyFirst(queryUpdateHome, marriage, home.getX(), home.getY(), home.getZ(), home.getWorldName(), home.getHomeServer());
			}
			else
			{
				runStatementAsyncIncludeKeyFirst(queryUpdateHome, marriage, home.getX(), home.getY(), home.getZ(), home.getWorldName());
			}
		}
	}

	@Override
	public void updatePvPState(final MarriageData marriage)
	{
		runStatementAsyncIncludeKey(queryPvPState, marriage, marriage.isPVPEnabled());
	}

	@Override
	protected void divorce(final MarriageData marriage)
	{
		runStatementAsyncIncludeKey(queryDelMarriage, marriage);
	}

	@Override
	protected void marry(final MarriageData marriage)
	{
		//TODO test if the player id is available
		runAsync(() -> {
			try(Connection connection = getConnection(); PreparedStatement ps = connection.prepareStatement(queryMarry, Statement.RETURN_GENERATED_KEYS))
			{
				DBTools.setParameters(ps, ((MarriagePlayerData) marriage.getPartner1()).getDatabaseKey(), ((MarriagePlayerData) marriage.getPartner2()).getDatabaseKey(),
				                (marriage.getPriest() != null) ? ((MarriagePlayerData) marriage.getPriest()).getDatabaseKey() : null, marriage.isPVPEnabled(), new Timestamp(marriage.getWeddingDate().getTime()));
				if(plugin.isSurnamesEnabled()) ps.setString(6, marriage.getSurname());
				ps.executeUpdate();
				try(ResultSet rs = ps.getGeneratedKeys())
				{
					if(rs.next())
					{
						marriage.setDatabaseKey(rs.getInt(1));
						cache.addDbKey(marriage);
					}
				}
			}
			catch(SQLException e)
			{
				e.printStackTrace();
			}
		});
	}

	@Override
	protected void updateSurname(final MarriageData marriage)
	{
		runStatementAsyncIncludeKey(querySetSurname, marriage, marriage.getSurname());
	}

	@Override
	public void migratePlayer(MigrationPlayer player)
	{
		try(Connection connection = getConnection(); PreparedStatement ps = connection.prepareStatement(queryAddPlayer, Statement.RETURN_GENERATED_KEYS))
		{
			int i = 1;
			ps.setString(i++, player.name);
			if(useUUIDs) ps.setString(i++, useUUIDSeparators ? player.uuid.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5") : player.uuid);
			if(plugin.getBackpacksIntegration() != null) ps.setBoolean(i, player.shareBackpack);
			ps.executeUpdate();
			try(ResultSet rs = ps.getGeneratedKeys())
			{
				if(rs.next())
				{
					player.id = rs.getInt(1);
				}
				else
				{
					plugin.getLogger().info("No auto ID for player \"" + player.name + "\", try to load id from database ...");
					try(PreparedStatement ps2 = connection.prepareStatement(queryLoadPlayer))
					{
						ps2.setString(1, (useUUIDs ? player.uuid : player.name));
						try(ResultSet rs2 = ps2.executeQuery())
						{
							if(rs2.next())
							{
								player.id = rs.getInt(fieldPlayerID);
							}
							else
							{
								plugin.getLogger().warning(ConsoleColor.RED + "No ID for player \"" + player.name + "\", there is something wrong with this player! You should check that!" + ConsoleColor.RESET);
								return;
							}
						}
					}
				}
			}
			if(player.priest && player.id >= 0)
			{
				try(PreparedStatement addPriest = connection.prepareStatement(querySetPriest))
				{
					addPriest.setInt(1, player.id);
					addPriest.execute();
				}
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			plugin.getLogger().warning(ConsoleColor.RED + "Failed adding player \"" + player.name + "\"!" + ConsoleColor.RESET);
		}
	}

	@Override
	public void migrateMarriage(MigrationMarriage marriage)
	{
		if(marriage.player1 == null || marriage.player2 == null || marriage.player1.id < 0 || marriage.player2.id < 0) return;
		try(Connection connection = getConnection(); PreparedStatement ps = connection.prepareStatement(queryMarry, Statement.RETURN_GENERATED_KEYS))
		{
			DBTools.setParameters(ps, marriage.player1.id, marriage.player2.id, marriage.priest == null ? null : marriage.priest.id, marriage.pvpState, new Timestamp(System.currentTimeMillis()));
			if(plugin.isSurnamesEnabled()) ps.setString(6, marriage.surname);
			ps.executeUpdate();
			try(ResultSet rs = ps.getGeneratedKeys())
			{
				if(rs.next())
				{
					if(marriage.home != null)
					{
						DBTools.runStatement(connection, queryUpdateHome, rs.getInt(fieldMarryID), marriage.home.x, marriage.home.y, marriage.home.z, marriage.home.world);
					}
				}
				else
				{
					plugin.getLogger().warning("No ID for marriage \"" + marriage.player1.name + "<->" + marriage.player2.name + "\"!");
				}
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			plugin.getLogger().warning(ConsoleColor.RED + "Failed adding marriage \"" + marriage.player1.name + "<->" + marriage.player2.name + "\"!" + ConsoleColor.RESET);
		}
	}
}