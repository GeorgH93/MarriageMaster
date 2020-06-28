/*
 *   Copyright (C) 2020 GeorgH93
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

package at.pcgamingfreaks.MarriageMaster.Database.Backend;

import at.pcgamingfreaks.ConsoleColor;
import at.pcgamingfreaks.Database.ConnectionProvider.ConnectionProvider;
import at.pcgamingfreaks.Database.DBTools;
import at.pcgamingfreaks.MarriageMaster.API.Home;
import at.pcgamingfreaks.MarriageMaster.Database.*;
import at.pcgamingfreaks.MarriageMaster.Database.FilesMigrator.MigrationMarriage;
import at.pcgamingfreaks.MarriageMaster.Database.FilesMigrator.MigrationPlayer;
import at.pcgamingfreaks.MarriageMaster.Database.Helper.DbElementStatementWithKeyFirstRunnable;
import at.pcgamingfreaks.MarriageMaster.Database.Helper.DbElementStatementWithKeyRunnable;
import at.pcgamingfreaks.MarriageMaster.Database.Helper.StructMarriageSQL;
import at.pcgamingfreaks.Message.MessageColor;
import at.pcgamingfreaks.UUIDConverter;

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

//@SuppressWarnings("JpaQueryApiInspection")
@SuppressWarnings("unchecked")
public abstract class SQL<MARRIAGE_PLAYER extends MarriagePlayerDataBase, MARRIAGE extends MarriageDataBase, HOME extends Home> extends DatabaseBackend<MARRIAGE_PLAYER, MARRIAGE, HOME> implements SQLBasedDatabase
{
	protected static final long RETRY_DELAY = 5; // 5Ticks = 250ms, should be more than enough to get the player id, especially since the id's should have already been loaded a long time ago.
	private static final Random RANDOM = new Random();

	protected final ConnectionProvider connectionProvider; // Connection provider

	//region Query related variables
	// Table Names
	protected String tablePlayers = "marry_players", tableMarriages = "marry_partners", tablePriests = "marry_priests", tableHomes = "marry_home";
	//region Field Names
	protected String fieldPlayerID = "player_id", fieldName = "name", fieldUUID = "uuid", fieldShareBackpack = "sharebackpack", fieldPriestID = "player_id", fieldSurname = "surname"; // Player
	protected String fieldMarryID = "marry_id", fieldPlayer1 = "player1", fieldPlayer2 = "player2", fieldPriest = "priest", fieldPVPState = "pvp_state", fieldDate = "date", fieldColor = "color"; // Marriage
	protected String fieldHomeX = "home_x", fieldHomeY = "home_y", fieldHomeZ = "home_z", fieldHomeWorld = "home_world", fieldHomeServer = "home_server"; // Home
	//endregion
	//region Querys
	@Language("SQL") protected String queryDelHome = "DELETE FROM {THomes} WHERE {FMarryID}=?;", queryUpdateHome = "REPLACE INTO {THomes} ({FMarryID},{FHomeX},{FHomeY},{FHomeZ},{FHomeWorld},{FHomeServer}) VALUES (?,?,?,?,?,?);";
	@Language("SQL") protected String queryPvPState = "UPDATE {TMarriages} SET {FPvPState}=? WHERE {FMarryID}=?;", querySetSurname = "UPDATE {TMarriages} SET {FSurname}=? WHERE {FMarryID}=?;";
	@Language("SQL") protected String queryDelMarriage = "DELETE FROM {TMarriages} WHERE {FMarryID}=?;", querySetBackpackShareState = "UPDATE {TPlayers} SET {FShareBackpack}=? WHERE {FPlayerID}=?;";
	@Language("SQL") protected String queryMarry = "INSERT INTO {TMarriages} ({FPlayer1},{FPlayer2},{FPriest},{FPvPState},{FDate}) VALUES (?,?,?,?,?);", queryLoadHome = "SELECT * FROM {THomes} WHERE {FMarryID}=?";
	@Language("SQL") protected String querySetPriest = "REPLACE INTO {TPriests} ({FPriestID}) VALUE (?);", queryRemovePriest = "DELETE FROM {TPriests} WHERE {FPriestID}=?;";
	@Language("SQL") protected String queryLoadPlayer = "SELECT * FROM {TPlayers} WHERE {FUUID}=?;", queryAddPlayer = "INSERT IGNORE INTO {TPlayers} ({FName},{FUUID},{FShareBackpack}) VALUES (?,?,?);";
	@Language("SQL") protected String queryLoadHomes = "SELECT * FROM {THomes};", queryIsPriest = "SELECT * FROM {TPriests} WHERE {FPriestID}=?;", queryLoadPlayerFromId = "SELECT * FROM {TPlayers} WHERE {FPlayerID}=?;";
	@Language("SQL") protected String queryLoadPlayersFromID = "SELECT * FROM {TPlayers} WHERE {FPlayerID} IN ({IDs});", queryLoadPriests = "SELECT {FPriestID} FROM {TPriests};";
	@Language("SQL") protected String queryLoadMarriages = "SELECT * FROM {TMarriages};", queryLoadMarriage = "SELECT * FROM {TMarriages} WHERE {FMarryID}=?";
	@Language("SQL") protected String queryGetUnsetOrInvalidUUIDs = "SELECT {FPlayerID},{FName},{FUUID} FROM {TPlayers} WHERE {FUUID} IS NULL OR {FUUID} ", queryFixUUIDs = "UPDATE {TPlayers} SET {FUUID}=? WHERE {FPlayerID}=?;";
	@Language("SQL") protected String queryPlayerID = "SELECT {FPlayerID} FROM {TPlayers} WHERE {FUUID}=?;", queryUpdatePlayer = "UPDATE {TPlayers} SET {FName}=? WHERE {FPlayerID}=?;";
	@Language("SQL") protected String queryUpdateMarriageColor = "UPDATE {TMarriages} SET {FColor}=? WHERE {FMarryID}=?;";
	//endregion
	//endregion

	protected SQL(final @NotNull IPlatformSpecific<MARRIAGE_PLAYER, MARRIAGE, HOME> platform, final @NotNull DatabaseConfiguration dbConfig, final boolean bungee, final boolean surname,
	              final @NotNull Cache<MARRIAGE_PLAYER, MARRIAGE> cache, final @NotNull Logger logger, @NotNull ConnectionProvider connectionProvider)
	{
		super(platform, dbConfig, bungee, surname, cache, logger);
		this.connectionProvider = connectionProvider;
		loadTableAndFieldNames(dbConfig);
		buildQuerys();
	}

	@Override
	public void startup() throws Exception
	{
		getConnection().close(); // Test if we can get a connection. This way we can easily check if the settings are correct.
		checkDatabase();
		super.startup();
	}

	@Override
	public void close()
	{
		try
		{
			connectionProvider.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		super.close();
	}

	/**
	 * Override this if you don't want to use the internal connection handling.
	 *
	 * @return A JDBC connection to a SQL database.
	 * @throws SQLException If there was a problem.
	 */
	@Override
	public @NotNull Connection getConnection() throws SQLException
	{
		return connectionProvider.getConnection();
	}

	protected @NotNull String getEngine()
	{
		return "";
	}

	protected void loadTableAndFieldNames(final @NotNull DatabaseConfiguration dbConfig)
	{
		// Load table names from config
		tablePlayers   = dbConfig.getSQLTableUser();
		tablePriests   = dbConfig.getSQLTablePriests();
		tableMarriages = dbConfig.getSQLTableMarriages();
		tableHomes     = dbConfig.getSQLTableHomes();
		// Load field names from config
		fieldPlayerID       = dbConfig.getSQLField("PlayerID",     fieldPlayerID);
		fieldName           = dbConfig.getSQLField("PlayerName",   fieldName);
		fieldUUID           = dbConfig.getSQLField("PlayerUUID",   fieldUUID);
		fieldShareBackpack  = dbConfig.getSQLField("PlayerShareBackpack", fieldShareBackpack);
		fieldPriestID       = dbConfig.getSQLField("PriestID",     fieldPriestID);
		fieldMarryID        = dbConfig.getSQLField("MarryID",      fieldMarryID);
		fieldPlayer1        = dbConfig.getSQLField("MarryPlayer1", fieldPlayer1);
		fieldPlayer1        = dbConfig.getSQLField("MarryPlayer2", fieldPlayer1);
		fieldPriest         = dbConfig.getSQLField("MarryPriest",  fieldPriest);
		fieldDate           = dbConfig.getSQLField("MarryDate",    fieldDate);
		fieldPVPState       = dbConfig.getSQLField("MarryPvPState",fieldPVPState);
		fieldColor          = dbConfig.getSQLField("MarryColor",   fieldColor);
		fieldHomeX          = dbConfig.getSQLField("HomeX",        fieldHomeX);
		fieldHomeY          = dbConfig.getSQLField("HomeY",        fieldHomeY);
		fieldHomeZ          = dbConfig.getSQLField("HomeZ",        fieldHomeZ);
		fieldHomeWorld      = dbConfig.getSQLField("HomeWorld",    fieldHomeWorld);
		fieldHomeServer     = dbConfig.getSQLField("HomeServer",   fieldHomeServer);
	}

	protected void buildQuerys()
	{
		if(!useBungee)
		{
			queryUpdateHome = queryUpdateHome.replaceAll(",\\{FHomeServer}", "").replace("(?,?,?,?,?,?)", "(?,?,?,?,?)");
		}
		if(surnameEnabled)
		{
			queryMarry         = "INSERT INTO {TMarriages} ({FPlayer1},{FPlayer2},{FPriest},{FPvPState},{FDate},{FSurname}) VALUES (?,?,?,?,?,?);";
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
		queryLoadMarriage  = replacePlaceholders(queryLoadMarriage);
		queryLoadMarriages = replacePlaceholders(queryLoadMarriages);
		queryLoadPlayerFromId       = replacePlaceholders(queryLoadPlayerFromId);
		queryLoadPlayersFromID      = replacePlaceholders(queryLoadPlayersFromID);
		querySetBackpackShareState  = replacePlaceholders(querySetBackpackShareState);
		queryGetUnsetOrInvalidUUIDs = replacePlaceholders(queryGetUnsetOrInvalidUUIDs);
		queryUpdateMarriageColor    = replacePlaceholders(queryUpdateMarriageColor);
	}

	protected @Language("SQL") @NotNull String replacePlaceholders(final @Language("SQL") @NotNull String query)
	{
		return query.replaceAll("(\\{\\w+})", "`$1`").replaceAll("`(\\{\\w+})`_(\\w+)", "`$1_$2`").replaceAll("fk_`(\\{\\w+})`_`(\\{\\w+})`_`(\\{\\w+})`", "`fk_$1_$2_$3`") // Fix name formatting
				.replaceAll("\\{TPlayers}", tablePlayers).replaceAll("\\{TMarriages}", tableMarriages).replaceAll("\\{TPriests}", tablePriests).replaceAll("\\{THomes}", tableHomes) // Table names
				.replaceAll("\\{FPlayerID}", fieldPlayerID).replaceAll("\\{FName}", fieldName).replaceAll("\\{FUUID}", fieldUUID).replaceAll("\\{FShareBackpack}", fieldShareBackpack) // Player fields
				.replaceAll("\\{FMarryID}", fieldMarryID).replaceAll("\\{FSurname}", fieldSurname).replaceAll("\\{FPlayer1}", fieldPlayer1).replaceAll("\\{FPlayer2}", fieldPlayer2) // Marriage fields
				.replaceAll("\\{FPriest}", fieldPriest).replaceAll("\\{FPvPState}", fieldPVPState).replaceAll("\\{FDate}", fieldDate).replaceAll("\\{FHomeServer}", fieldHomeServer).replaceAll("\\{FColor}", fieldColor)
				.replaceAll("\\{FHomeX}", fieldHomeX).replaceAll("\\{FHomeY}", fieldHomeY).replaceAll("\\{FHomeZ}", fieldHomeZ).replaceAll("\\{FHomeWorld}", fieldHomeWorld) // Home fields
				.replaceAll("\\{FPriestID}", fieldPriestID);
	}

	protected abstract void checkDatabase();

	//region Helper functions for async querys
	protected void runAsync(final @NotNull Runnable runnable, final @NotNull DatabaseElement databaseElement)
	{
		runAsync(runnable, (databaseElement.getDatabaseKey() == null) ? RETRY_DELAY : 0);
	}

	protected void runStatementAsyncIncludeKey(final @Language("SQL") @NotNull String query, final @NotNull DatabaseElement databaseElement, final @NotNull Object... args)
	{
		runAsync(new DbElementStatementWithKeyRunnable(this, databaseElement, query, args), databaseElement);
	}

	protected void runStatementAsyncIncludeKeyFirst(final @Language("SQL") @NotNull String query, final @NotNull DatabaseElement databaseElement, final @NotNull Object... args)
	{
		runAsync(new DbElementStatementWithKeyFirstRunnable(this, databaseElement, query, args), databaseElement);
	}
	//endregion

	@Override
	public void checkUUIDs()
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
						logger.info(MESSAGE_UPDATE_UUIDS);
					}
					String uuid = res.getString(fieldUUID);
					if(uuid == null)
					{
						toConvert.put(res.getString(fieldName), new UpdateUUIDsHelper(res.getString(fieldName), null, res.getInt(fieldPlayerID)));
					}
					else
					{
						uuid = (useUUIDSeparators) ? uuid.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5") : uuid.replaceAll("-", "");
						toUpdate.add(new UpdateUUIDsHelper(res.getString(fieldName), uuid, res.getInt(fieldPlayerID)));
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
				boolean ok = false;
				do
				{
					try(PreparedStatement ps = connection.prepareStatement(queryFixUUIDs))
					{
						for(UpdateUUIDsHelper updateData : toUpdate)
						{
							DBTools.setParameters(ps, updateData.getUUID(), updateData.getId());
							ps.addBatch();
						}
						ps.executeBatch();
						ok = true;
					}
					catch(SQLException ignored)
					{
						Iterator<UpdateUUIDsHelper> updateUUIDsHelperIterator = toUpdate.iterator();
						while(updateUUIDsHelperIterator.hasNext())
						{
							UpdateUUIDsHelper updateData = updateUUIDsHelperIterator.next();
							try(PreparedStatement ps = connection.prepareStatement(queryLoadPlayer))
							{
								ps.setString(1, updateData.getUUID());
								try(ResultSet rs = ps.executeQuery())
								{
									if(rs.next())
									{
										logger.warning("User " + updateData.getName() + " (db id: " + updateData.getId() + ") has the same UUID as " + rs.getString(fieldName) +
												                           " (db id: " + rs.getInt(fieldPlayerID) + "), UUID: " + updateData.getUUID());
										updateUUIDsHelperIterator.remove();
									}
								}
							}
						}
					}
				} while(!ok);

				logger.info(String.format(MESSAGE_UPDATED_UUIDS, toUpdate.size()));
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void loadAll()
	{
		Set<StructMarriageSQL> marriagesSet = new HashSet<>();
		Set<Integer> playerToLoad = new FilteredHashSet<>(element -> element >= 0);
		try(Connection connection = getConnection())
		{
			// Load marriages
			logger.info("Loading marriages ...");
			try(PreparedStatement ps = connection.prepareStatement(queryLoadMarriages); ResultSet rs = ps.executeQuery())
			{
				while(rs.next())
				{
					marriagesSet.add(new StructMarriageSQL(rs.getInt(fieldMarryID), rs.getInt(fieldPlayer1), rs.getInt(fieldPlayer2), rs.getInt(fieldPriest), rs.getBoolean(fieldPVPState), rs.getString(fieldColor),
					                                       surnameEnabled ? rs.getString(fieldSurname) : null, rs.getTimestamp(fieldDate)));
					playerToLoad.add(rs.getInt(fieldPlayer1));
					playerToLoad.add(rs.getInt(fieldPlayer2));
					if(rs.getObject(fieldPriest) != null) playerToLoad.add(rs.getInt(fieldPriest));
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			logger.info("Marriages loaded");
			// Load priests
			logger.info("Loading priests ...");
			Set<Integer> priests = new HashSet<>();
			try(PreparedStatement ps = connection.prepareStatement(queryLoadPriests); ResultSet rs = ps.executeQuery())
			{
				while(rs.next())
				{
					priests.add(rs.getInt(fieldPriestID));
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			logger.info("Priests loaded");
			// Load players
			logger.info("Loading players ...");
			//TODO validate the performance loss for individual querys to load the data
			if(!playerToLoad.isEmpty())
			{
				StringBuilder stringBuilder = new StringBuilder();
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
							MARRIAGE_PLAYER player = platform.producePlayer(getUUIDFromIdentifier(rs.getString(fieldUUID)), rs.getString(fieldName), rs.getBoolean(fieldShareBackpack),
							                                                priests.contains(rs.getInt(fieldPlayerID)), rs.getInt(fieldPlayerID));
							cache.cache(player);
						}
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			logger.info("Players loaded");
			// Load the marriages into the cache
			logger.info("Writing marriages into cache ...");
			for(StructMarriageSQL sm : marriagesSet)
			{
				MARRIAGE_PLAYER player1 = (MARRIAGE_PLAYER) cache.getPlayerFromDbKey(sm.p1ID), player2 = (MARRIAGE_PLAYER) cache.getPlayerFromDbKey(sm.p2ID);
				if(player1 != null && player2 != null)
				{
					cache.cache(platform.produceMarriage((MARRIAGE_PLAYER) cache.getPlayerFromDbKey(sm.p1ID), (MARRIAGE_PLAYER) cache.getPlayerFromDbKey(sm.p2ID),
					                                     (MARRIAGE_PLAYER) cache.getPlayerFromDbKey(sm.priest), sm.date, sm.surname, sm.pvp, sm.color, null, sm.marryID));
				}
				else
				{
					logger.warning("Player " + (player1 == null ? "1" : "2") + " for marriage " + sm.marryID + " has not been loaded. Skipping");
				}
			}
			logger.info("Marriages loaded into cache");
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		loadHomes();
	}

	public void loadMarriage(final int marriageId)
	{
		runAsync(() -> {
			try(Connection connection = getConnection(); PreparedStatement ps = connection.prepareStatement(queryLoadMarriage))
			{
				ps.setInt(1, marriageId);
				try(ResultSet rs =  ps.executeQuery())
				{
					if(rs.next())
					{
						MARRIAGE_PLAYER player1 = playerFromId(connection, rs.getInt(fieldPlayer1)), player2 = playerFromId(connection, rs.getInt(fieldPlayer2));
						MARRIAGE_PLAYER priest = (rs.getObject(fieldPriest) == null) ? null : playerFromId(connection, rs.getInt(fieldPriest));
						if(player1 == null || player2 == null)
						{
							logger.warning("Failed to load marriage (id: " + marriageId + ") cause one of it's players could not be loaded successful!");
							return;
						}
						String surname = surnameEnabled ? rs.getString(fieldSurname) : null;
						String color = rs.getString(fieldColor);
						MARRIAGE marriage = platform.produceMarriage(player1, player2, priest, rs.getTimestamp(fieldDate), surname, rs.getBoolean(fieldPVPState), color == null ? null : MessageColor.valueOf(color), null, marriageId);
						cache.cache(marriage);
						loadHome(marriage);
					}
				}
			}
			catch(SQLException e)
			{
				e.printStackTrace();
			}
		});
	}

	protected @Nullable MARRIAGE_PLAYER playerFromId(final @NotNull Connection connection, final int id) throws SQLException
	{
		if(cache.isPlayerFromDbKeyLoaded(id)) return (MARRIAGE_PLAYER) cache.getPlayerFromDbKey(id);
		// No cache for the player, load him
		try(PreparedStatement ps = connection.prepareStatement(queryLoadPlayerFromId))
		{
			ps.setInt(1, id);
			try(ResultSet rs = ps.executeQuery())
			{
				if(rs.next())
				{
					MARRIAGE_PLAYER player = platform.producePlayer(getUUIDFromIdentifier(rs.getString(fieldUUID)), rs.getString(fieldName),
					                                                false, rs.getBoolean(fieldShareBackpack), rs.getInt(fieldPlayerID));
					cache.cache(player);
					return player;
				}
			}
		}
		return null;
	}

	protected void loadHomes()
	{
		logger.info("Loading homes ...");
		Map<Integer, HOME> homes = new HashMap<>();
		try(Connection connection = getConnection(); PreparedStatement ps = connection.prepareStatement(queryLoadHomes); ResultSet rs = ps.executeQuery())
		{
			while(rs.next())
			{
				String homeServer = (useBungee) ? rs.getString(fieldHomeServer) : null;
				homes.put(rs.getInt(fieldMarryID), platform.produceHome("", rs.getString(fieldHomeWorld), homeServer, rs.getDouble(fieldHomeX), rs.getDouble(fieldHomeY), rs.getDouble(fieldHomeZ)));
			}
			for(Object marriage : cache.getLoadedMarriages())
			{
				MARRIAGE m = (MARRIAGE) marriage;
				if(m.getDatabaseKey() != null && m.getDatabaseKey() instanceof Integer)
				{
					m.setHomeData(homes.get(m.getDatabaseKey()));
				}
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		homes.clear();
		logger.info("Homes loaded");
	}

	public void loadHome(final MARRIAGE marriage)
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
							String homeServer = (useBungee) ? rs.getString(fieldHomeServer) : null;
							marriage.setHomeData(platform.produceHome("", rs.getString(fieldHomeWorld), homeServer, rs.getDouble(fieldHomeX), rs.getDouble(fieldHomeY), rs.getDouble(fieldHomeZ)));
						}
						else
						{
							marriage.setHomeData(null);
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
	public void load(final @NotNull MARRIAGE_PLAYER player)
	{
		runAsync(() -> doLoad(player));
	}

	protected void doLoad(final @NotNull MARRIAGE_PLAYER player)
	{
		try(Connection connection = getConnection())
		{
			if(player.getDatabaseKey() == null)
			{
				if(!queryPlayer(player, connection)) add(player, connection);
			}
			else if(player.isOnline())
			{
				update(player, connection);
			}
		}
		catch(SQLException e)
		{
			if(e instanceof SQLTransactionRollbackException) runAsync(() -> doLoad(player), RANDOM.nextInt(4)); // Retry on deadlock
			else e.printStackTrace();
		}
	}

	protected void add(final @NotNull MARRIAGE_PLAYER player, final @NotNull Connection connection) throws SQLException
	{
		try(PreparedStatement psAdd = connection.prepareStatement(queryAddPlayer, Statement.RETURN_GENERATED_KEYS))
		{
			int i = 1;
			psAdd.setString(i++, player.getName());
			psAdd.setString(i++, getUsedPlayerIdentifier(player));
			psAdd.setBoolean(i++, player.isSharingBackpack());
			if(psAdd.getParameterMetaData().getParameterCount() == i)
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

	protected void update(final MARRIAGE_PLAYER player, final Connection connection) throws SQLException
	{
		String onlineName = player.getOnlineName();
		if(onlineName == null || onlineName.equals(player.getName())) return;
		player.setName(onlineName);
		DBTools.runStatement(connection, queryUpdatePlayer, onlineName, player.getDatabaseKey());
	}

	protected boolean queryPlayer(final MARRIAGE_PLAYER player, final Connection connection) throws SQLException
	{
		int dbId;
		try(PreparedStatement ps = connection.prepareStatement(queryLoadPlayer))
		{
			ps.setString(1, getUsedPlayerIdentifier(player));
			try(ResultSet rs = ps.executeQuery())
			{
				if(rs.next())
				{
					dbId = rs.getInt(fieldPlayerID);
					player.setDatabaseKey(dbId);
					cache.addDbKey(player);
					player.setSharesBackpack(rs.getBoolean(fieldShareBackpack));
					if(player.isOnline() && !rs.getString(fieldName).equals(player.getPlayer()))
					{
						update(player, connection);
					}
				}
				else return false;
			}
		}
		try(PreparedStatement ps = connection.prepareStatement(queryIsPriest))
		{
			ps.setInt(1, dbId);
			try(ResultSet rs = ps.executeQuery())
			{
				player.setPriestData(rs.next());
			}
		}
		return true;
	}

	@Override
	public void updateBackpackShareState(final @NotNull MARRIAGE_PLAYER player)
	{
		runStatementAsyncIncludeKey(querySetBackpackShareState, player, player.isSharingBackpack());
	}

	@Override
	public void updatePriestStatus(final @NotNull MARRIAGE_PLAYER player)
	{
		runStatementAsyncIncludeKey(player.isPriest() ? querySetPriest : queryRemovePriest, player);
	}

	@Override
	public void updateHome(final @NotNull MARRIAGE marriage)
	{
		Home home = marriage.getHome();
		if(home == null)
		{
			runStatementAsyncIncludeKey(queryDelHome, marriage);
		}
		else
		{
			if(useBungee)
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
	public void updatePvPState(final @NotNull MARRIAGE marriage)
	{
		runStatementAsyncIncludeKey(queryPvPState, marriage, marriage.isPVPEnabled());
	}

	@Override
	public void updateMarriageColor(final @NotNull MARRIAGE marriage)
	{
		runStatementAsyncIncludeKey(queryUpdateMarriageColor, marriage, marriage.getColor() == null ? null : marriage.getColor().name());
	}

	@Override
	public void divorce(final @NotNull MARRIAGE marriage)
	{
		runStatementAsyncIncludeKey(queryDelMarriage, marriage);
	}

	@Override
	public void marry(final @NotNull MARRIAGE marriage)
	{
		//TODO test if the player id is available
		runAsync(() -> {
			try(Connection connection = getConnection(); PreparedStatement ps = connection.prepareStatement(queryMarry, Statement.RETURN_GENERATED_KEYS))
			{
				DBTools.setParameters(ps, ((MARRIAGE_PLAYER) marriage.getPartner1()).getDatabaseKey(), ((MARRIAGE_PLAYER) marriage.getPartner2()).getDatabaseKey(),
				                      (marriage.getPriest() != null) ? ((MARRIAGE_PLAYER) marriage.getPriest()).getDatabaseKey() : null, marriage.isPVPEnabled(), new Timestamp(marriage.getWeddingDate().getTime()));
				if(surnameEnabled) ps.setString(6, marriage.getSurname());
				ps.executeUpdate();
				try(ResultSet rs = ps.getGeneratedKeys())
				{
					if(rs.next())
					{
						marriage.setDatabaseKey(rs.getInt(1));
						cache.addDbKey(marriage);
						if(marriageSavedCallback != null) marriageSavedCallback.run(marriage);
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
	public void updateSurname(final @NotNull MARRIAGE marriage)
	{
		runStatementAsyncIncludeKey(querySetSurname, marriage, marriage.getSurname());
	}

	@Override
	public void migratePlayer(final @NotNull MigrationPlayer player)
	{
		try(Connection connection = getConnection(); PreparedStatement ps = connection.prepareStatement(queryAddPlayer, Statement.RETURN_GENERATED_KEYS))
		{
			int i = 1;
			ps.setString(i++, player.name);
			ps.setString(i++, useUUIDSeparators ? player.uuid.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5") : player.uuid);
			ps.setBoolean(i, player.shareBackpack);
			ps.executeUpdate();
			try(ResultSet rs = ps.getGeneratedKeys())
			{
				if(rs.next())
				{
					player.id = rs.getInt(1);
				}
				else
				{
					logger.info("No auto ID for player \"" + player.name + "\", try to load id from database ...");
					try(PreparedStatement ps2 = connection.prepareStatement(queryLoadPlayer))
					{
						ps2.setString(1, player.uuid);
						try(ResultSet rs2 = ps2.executeQuery())
						{
							if(rs2.next())
							{
								player.id = rs.getInt(fieldPlayerID);
							}
							else
							{
								logger.warning(ConsoleColor.RED + "No ID for player \"" + player.name + "\", there is something wrong with this player! You should check that!" + ConsoleColor.RESET);
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
			logger.warning(ConsoleColor.RED + "Failed adding player \"" + player.name + "\"!" + ConsoleColor.RESET);
		}
	}

	@Override
	public void migrateMarriage(final @NotNull MigrationMarriage marriage)
	{
		if(marriage.player1 == null || marriage.player2 == null || marriage.player1.id < 0 || marriage.player2.id < 0) return;
		try(Connection connection = getConnection(); PreparedStatement ps = connection.prepareStatement(queryMarry, Statement.RETURN_GENERATED_KEYS))
		{
			DBTools.setParameters(ps, marriage.player1.id, marriage.player2.id, marriage.priest == null ? null : marriage.priest.id, marriage.pvpState, new Timestamp(System.currentTimeMillis()));
			if(surnameEnabled) ps.setString(6, marriage.surname);
			ps.executeUpdate();
			try(ResultSet rs = ps.getGeneratedKeys())
			{
				if(rs.next())
				{
					marriage.id = rs.getInt(1);
					if(marriage.home != null && marriage.id >= 0)
					{
						try
						{
							DBTools.runStatement(connection, queryUpdateHome, marriage.id, marriage.home.x, marriage.home.y, marriage.home.z, marriage.home.world);
						}
						catch(SQLException e)
						{
							e.printStackTrace();
							logger.warning(ConsoleColor.RED + "Failed adding home for marriage \"" + marriage.player1.name + " <-> " + marriage.player2.name + "\"!" + ConsoleColor.RESET);
						}
					}
				}
				else
				{
					logger.warning("No ID for marriage \"" + marriage.player1.name + "<->" + marriage.player2.name + "\"!");
				}
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			logger.warning(ConsoleColor.RED + "Failed adding marriage \"" + marriage.player1.name + " <-> " + marriage.player2.name + "\"!" + ConsoleColor.RESET);
		}
	}
}