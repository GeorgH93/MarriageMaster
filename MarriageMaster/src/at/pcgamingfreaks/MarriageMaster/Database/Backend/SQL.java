/*
 *   Copyright (C) 2023 GeorgH93
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
 *   along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.MarriageMaster.Database.Backend;

import at.pcgamingfreaks.ConsoleColor;
import at.pcgamingfreaks.DataHandler.HasPlaceholders;
import at.pcgamingfreaks.DataHandler.ILoadableStringFieldsHolder;
import at.pcgamingfreaks.DataHandler.IStringFieldsWithPlaceholdersHolder;
import at.pcgamingfreaks.DataHandler.Loadable;
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

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

//@SuppressWarnings("JpaQueryApiInspection")
@SuppressWarnings("unchecked")
public abstract class SQL<MARRIAGE_PLAYER extends MarriagePlayerDataBase, MARRIAGE extends MarriageDataBase, HOME extends Home>
		extends DatabaseBackend<MARRIAGE_PLAYER, MARRIAGE, HOME>
		implements SQLBasedDatabase, IStringFieldsWithPlaceholdersHolder, ILoadableStringFieldsHolder
{
	protected static final long RETRY_DELAY = 5; // 5Ticks = 250ms, should be more than enough to get the player id, especially since the id's should have already been loaded a long time ago.
	private static final Random RANDOM = new Random();

	protected final @NotNull DatabaseConfiguration dbConfig;
	protected final ConnectionProvider connectionProvider; // Connection provider

	//region Query related variables
	// Table Names
	@Loadable protected String tableUser = "marry_players", tablePartner = "marry_partners", tablePriests = "marry_priests", tableHome = "marry_home";
	//region Field Names
	@Loadable protected String fieldPlayerID = "player_id", fieldPriestID = "player_id", fieldMarryID = "marry_id";
	@Loadable (metadata = "Player") protected String fieldName = "name", fieldUUID = "uuid", fieldShareBackpack = "sharebackpack"; // Player
	@Loadable (metadata = "Marry") protected String fieldPlayer1 = "player1", fieldPlayer2 = "player2", fieldPriest = "priest", fieldSurname = "surname", fieldPVPState = "pvp_state", fieldDate = "date", fieldColor = "color"; // Marriage
	@Loadable protected String fieldHomeX = "home_x", fieldHomeY = "home_y", fieldHomeZ = "home_z", fieldHomeWorld = "home_world", fieldHomeServer = "home_server", fieldHomeYaw = "home_yaw", fieldHomePitch = "home_pitch"; // Home
	//endregion
	//region Queries
	@HasPlaceholders @Language("SQL") protected String queryDelHome = "DELETE FROM {THomes} WHERE {FMarryID}=?;", queryUpdateHome = "REPLACE INTO {THomes} ({FMarryID},{FHomeX},{FHomeY},{FHomeZ},{FHomeYaw},{FHomePitch},{FHomeWorld},{FHomeServer}) VALUES (?,?,?,?,?,?,?,?);";
	@HasPlaceholders @Language("SQL") protected String queryPvPState = "UPDATE {TMarriages} SET {FPvPState}=? WHERE {FMarryID}=?;", querySetSurname = "UPDATE {TMarriages} SET {FSurname}=? WHERE {FMarryID}=?;";
	@HasPlaceholders @Language("SQL") protected String queryDelMarriage = "DELETE FROM {TMarriages} WHERE {FMarryID}=?;", querySetBackpackShareState = "UPDATE {TPlayers} SET {FShareBackpack}=? WHERE {FPlayerID}=?;";
	@HasPlaceholders @Language("SQL") protected String queryMarry = "INSERT INTO {TMarriages} ({FPlayer1},{FPlayer2},{FPriest},{FPvPState},{FDate}) VALUES (?,?,?,?,?);", queryLoadHome = "SELECT * FROM {THomes} WHERE {FMarryID}=?";
	@HasPlaceholders @Language("SQL") protected String querySetPriest = "REPLACE INTO {TPriests} ({FPriestID}) VALUE (?);", queryRemovePriest = "DELETE FROM {TPriests} WHERE {FPriestID}=?;";
	@HasPlaceholders @Language("SQL") protected String queryLoadPlayer = "SELECT * FROM {TPlayers} WHERE {FUUID}=?;", queryAddPlayer = "INSERT IGNORE INTO {TPlayers} ({FName},{FUUID},{FShareBackpack}) VALUES (?,?,?);";
	@HasPlaceholders @Language("SQL") protected String queryLoadHomes = "SELECT * FROM {THomes};", queryIsPriest = "SELECT * FROM {TPriests} WHERE {FPriestID}=?;", queryLoadPlayerFromId = "SELECT * FROM {TPlayers} WHERE {FPlayerID}=?;";
	@HasPlaceholders @Language("SQL") protected String queryLoadPlayersFromID = "SELECT * FROM {TPlayers} WHERE {FPlayerID} IN ({IDs});", queryLoadPriests = "SELECT {FPriestID} FROM {TPriests};";
	@HasPlaceholders @Language("SQL") protected String queryLoadMarriages = "SELECT * FROM {TMarriages};", queryLoadMarriage = "SELECT * FROM {TMarriages} WHERE {FMarryID}=?";
	@HasPlaceholders @Language("SQL") protected String queryUpdatePlayer = "UPDATE {TPlayers} SET {FName}=? WHERE {FPlayerID}=?;";
	@HasPlaceholders @Language("SQL") protected String queryUpdateMarriageColor = "UPDATE {TMarriages} SET {FColor}=? WHERE {FMarryID}=?;";
	//endregion
	//endregion

	protected SQL(final @NotNull IPlatformSpecific<MARRIAGE_PLAYER, MARRIAGE, HOME> platform, final @NotNull DatabaseConfiguration dbConfig, final boolean bungee, final boolean surname,
	              final @NotNull Cache<MARRIAGE_PLAYER, MARRIAGE> cache, final @NotNull Logger logger, @NotNull ConnectionProvider connectionProvider)
	{
		super(platform, dbConfig, bungee, surname, cache, logger);
		this.dbConfig = dbConfig;
		this.connectionProvider = connectionProvider;
		loadTableAndFieldNames();
		buildQueries();
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
			logger.log(Level.WARNING, "Failed to close sql connection provider!", e);
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

	protected void loadTableAndFieldNames()
	{
		loadFields();
	}

	protected void buildQueries()
	{
		if(!useBungee)
		{
			queryUpdateHome = queryUpdateHome.replace(",{FHomeServer}", "").replace("(?,?,?,?,?,?,?,?)", "(?,?,?,?,?,?,?)");
		}
		if(surnameEnabled)
		{
			queryMarry = "INSERT INTO {TMarriages} ({FPlayer1},{FPlayer2},{FPriest},{FPvPState},{FDate},{FSurname}) VALUES (?,?,?,?,?,?);";
		}
		replacePlaceholders();
	}

	@Override
	public @Language("SQL") @NotNull String replacePlaceholders(final @Language("SQL") @NotNull String query)
	{
		return query.replaceAll("(\\{\\w+})", "`$1`").replaceAll("`(\\{\\w+})`_(\\w+)", "`$1_$2`").replaceAll("fk_`(\\{\\w+})`_`(\\{\\w+})`_`(\\{\\w+})`", "`fk_$1_$2_$3`") // Fix name formatting
				.replace("{TPlayers}", tableUser).replace("{TMarriages}", tablePartner).replace("{TPriests}", tablePriests).replace("{THomes}", tableHome) // Table names
				.replace("{FPlayerID}", fieldPlayerID).replace("{FName}", fieldName).replace("{FUUID}", fieldUUID).replace("{FShareBackpack}", fieldShareBackpack) // Player fields
				.replace("{FMarryID}", fieldMarryID).replace("{FSurname}", fieldSurname).replace("{FPlayer1}", fieldPlayer1).replace("{FPlayer2}", fieldPlayer2) // Marriage fields
				.replace("{FPriest}", fieldPriest).replace("{FPvPState}", fieldPVPState).replace("{FDate}", fieldDate).replace("{FColor}", fieldColor)
				.replace("{FHomeServer}", fieldHomeServer).replace("{FHomeX}", fieldHomeX).replace("{FHomeY}", fieldHomeY).replace("{FHomeZ}", fieldHomeZ) // Home fields
				.replace("{FHomeYaw}", fieldHomeYaw).replace("{FHomePitch}", fieldHomePitch).replace("{FHomeWorld}", fieldHomeWorld)
				.replace("{FPriestID}", fieldPriestID);
	}

	@Override
	public String loadField(final @NotNull String fieldName, final @NotNull String metadata, final @Nullable String currentValue)
	{
		if(fieldName.startsWith("field"))
		{
			return dbConfig.getSQLField(metadata + fieldName.substring(5), currentValue);
		}
		else
		{
			return dbConfig.getSQLTable(metadata + fieldName.substring(5), currentValue);
		}
	}

	protected abstract void checkDatabase();

	//region Helper functions for async queries
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

	protected void runStatementAsyncIncludeKey(final @Nullable Runnable callback, final @Language("SQL") @NotNull String query, final @NotNull DatabaseElement databaseElement, final @NotNull Object... args)
	{
		DbElementStatementWithKeyRunnable runnable = new DbElementStatementWithKeyRunnable(this, databaseElement, query, args);
		runnable.callback = callback;
		runAsync(runnable, databaseElement);
	}

	protected void runStatementAsyncIncludeKeyFirst(final @Nullable Runnable callback, final @Language("SQL") @NotNull String query, final @NotNull DatabaseElement databaseElement, final @NotNull Object... args)
	{
		DbElementStatementWithKeyFirstRunnable runnable = new DbElementStatementWithKeyFirstRunnable(this, databaseElement, query, args);
		runnable.callback = callback;
		runAsync(runnable, databaseElement);
	}
	//endregion

	@Override
	public void checkUUIDs()
	{
		try(Connection connection = getConnection())
		{
			DBTools.validateUUIDs(logger, connection, tableUser, fieldName, fieldUUID, fieldPlayerID, useUUIDSeparators, useOnlineUUIDs);
		}
		catch(SQLException e)
		{
			logger.log(Level.SEVERE, "Failed to update player UUIDs in database!", e);
		}
	}

	private Set<StructMarriageSQL> loadMarriages(final @NotNull Connection connection, final @NotNull Set<Integer> playerToLoad)
	{
		Set<StructMarriageSQL> marriagesSet = new HashSet<>();
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
			logger.log(Level.SEVERE, "Failed to load marriages!", e);
			platform.spawnDatabaseLoadingErrorMessage("Failed to load marriages - " + e.getMessage());
		}
		logger.info("Marriages loaded");
		return marriagesSet;
	}

	private Set<Integer> loadPriests(final @NotNull Connection connection)
	{
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
			logger.log(Level.SEVERE, "Failed to load priests!", e);
			platform.spawnDatabaseLoadingErrorMessage("Failed to load priests - " + e.getMessage());
		}
		logger.info("Priests loaded");
		return priests;
	}

	private void loadPlayers(final @NotNull Connection connection, final @NotNull Set<Integer> playerToLoad, final @NotNull Set<Integer> priests)
	{
		logger.info("Loading players ...");
		//TODO validate the performance loss for individual queries to load the data
		if(!playerToLoad.isEmpty())
		{
			StringBuilder stringBuilder = new StringBuilder();
			for(int pid : playerToLoad)
			{
				if(stringBuilder.length() > 0) stringBuilder.append(',');
				stringBuilder.append(pid);
			}
			try(Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(queryLoadPlayersFromID.replace("`{IDs}`", stringBuilder.toString())))
			{
				while(rs.next())
				{
					MARRIAGE_PLAYER player = platform.producePlayer(getUUIDFromIdentifier(rs.getString(fieldUUID)), rs.getString(fieldName), rs.getBoolean(fieldShareBackpack),
					                                                priests.contains(rs.getInt(fieldPlayerID)), rs.getInt(fieldPlayerID));
					cache.cache(player);
				}
			}
			catch(Exception e)
			{
				logger.log(Level.SEVERE, "Failed to load players!", e);
				platform.spawnDatabaseLoadingErrorMessage("Failed to load players - " + e.getMessage());
			}
		}
		logger.info("Players loaded");
	}

	@Override
	public void loadAll()
	{
		try(Connection connection = getConnection())
		{
			Set<Integer> playerToLoad = new FilteredHashSet<>(element -> element >= 0);
			Set<StructMarriageSQL> marriagesSet = loadMarriages(connection, playerToLoad); // Load marriages
			Set<Integer> priests = loadPriests(connection); // Load priests
			loadPlayers(connection, playerToLoad, priests); // Load players

			// Load the marriages into the cache
			logger.info("Writing marriages into cache ...");
			for(StructMarriageSQL sm : marriagesSet)
			{
				MARRIAGE_PLAYER player1 = cache.getPlayerFromDbKey(sm.p1ID), player2 = cache.getPlayerFromDbKey(sm.p2ID);
				if(player1 != null && player2 != null)
				{
					cache.cache(platform.produceMarriage(cache.getPlayerFromDbKey(sm.p1ID), cache.getPlayerFromDbKey(sm.p2ID),
					                                     cache.getPlayerFromDbKey(sm.priest), sm.date, sm.surname, sm.pvp, sm.color, null, sm.marryID));
				}
				else
				{
					logger.log(Level.WARNING, "Player {0} for marriage {1} has not been loaded. Skipping", new Object[]{ (player1 == null ? 1 : 2), sm.marryID });
				}
			}
			logger.info("Marriages loaded into cache");
		}
		catch(SQLException e)
		{
			logger.log(Level.SEVERE, "Failed loading plugin data from database with unknown error!", e);
			platform.spawnDatabaseLoadingErrorMessage(e.getMessage());
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
							logger.log(Level.WARNING, "Failed to load marriage (id: {0}) because one of its players could not be loaded successful!", marriageId);
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
				logger.log(Level.SEVERE, "Failed to load marriage!", e);
			}
		});
	}

	protected @Nullable MARRIAGE_PLAYER playerFromId(final @NotNull Connection connection, final int id) throws SQLException
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
				homes.put(rs.getInt(fieldMarryID), platform.produceHome(rs.getString(fieldHomeWorld), homeServer, rs.getDouble(fieldHomeX), rs.getDouble(fieldHomeY), rs.getDouble(fieldHomeZ), rs.getFloat(fieldHomeYaw), rs.getFloat(fieldHomePitch)));
			}
			for(MARRIAGE marriage : cache.getLoadedMarriages())
			{
				if(marriage.getDatabaseKey() instanceof Integer)
				{
					marriage.setHomeData(homes.get(marriage.getDatabaseKey()));
				}
			}
		}
		catch(SQLException e)
		{
			logger.log(Level.SEVERE, "Failed to load homes!", e);
			platform.spawnDatabaseLoadingErrorMessage("Failed to load homes - " + e.getMessage());
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
							marriage.setHomeData(platform.produceHome(rs.getString(fieldHomeWorld), homeServer, rs.getDouble(fieldHomeX), rs.getDouble(fieldHomeY), rs.getDouble(fieldHomeZ), rs.getFloat(fieldHomeYaw), rs.getFloat(fieldHomePitch)));
						}
						else
						{
							marriage.setHomeData(null);
						}
					}
				}
				catch(SQLException e)
				{
					logger.log(Level.SEVERE, e, () -> "Failed to load home for marriage " + marriage.getPartner1().getName() + " - " + marriage.getPartner2().getName());
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
		catch(SQLTransactionRollbackException e)
		{
			runAsync(() -> doLoad(player), RANDOM.nextInt(4)); // Retry on deadlock
		}
		catch(SQLException e)
		{
			logger.log(Level.SEVERE, e, () -> "Failed to load player data for " + player.getName() + " (" + player.getUUID() + ")!");
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
					update(player, connection);
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
	public void updateBackpackShareState(final @NotNull MARRIAGE_PLAYER player, final @Nullable Consumer<MarriagePlayerDataBase> updateCallback)
	{
		runStatementAsyncIncludeKey((updateCallback != null) ? () -> updateCallback.accept(player) : null, querySetBackpackShareState, player, player.isSharingBackpack());
	}

	@Override
	public void updatePriestStatus(final @NotNull MARRIAGE_PLAYER player, final @Nullable Consumer<MarriagePlayerDataBase> updateCallback)
	{
		runStatementAsyncIncludeKey((updateCallback != null) ? () -> updateCallback.accept(player) : null, player.isPriest() ? querySetPriest : queryRemovePriest, player);
	}

	@Override
	public void updateHome(final @NotNull MARRIAGE marriage, final @Nullable Consumer<MarriageDataBase> updateCallback)
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
				runStatementAsyncIncludeKeyFirst((updateCallback != null) ? () -> updateCallback.accept(marriage) : null,
				                                 queryUpdateHome, marriage, home.getX(), home.getY(), home.getZ(), home.getYaw(), home.getPitch(), home.getWorldName(), home.getHomeServer());
			}
			else
			{
				runStatementAsyncIncludeKeyFirst(queryUpdateHome, marriage, home.getX(), home.getY(), home.getZ(), home.getYaw(), home.getPitch(), home.getWorldName());
			}
		}
	}

	@Override
	public void updatePvPState(final @NotNull MARRIAGE marriage, final @Nullable Consumer<MarriageDataBase> updateCallback)
	{
		runStatementAsyncIncludeKey((updateCallback != null) ? () -> updateCallback.accept(marriage) : null, queryPvPState, marriage, marriage.isPVPEnabled());
	}

	@Override
	public void updateMarriageColor(final @NotNull MARRIAGE marriage, final @Nullable Consumer<MarriageDataBase> updateCallback)
	{
		runStatementAsyncIncludeKey((updateCallback != null) ? () -> updateCallback.accept(marriage) : null, queryUpdateMarriageColor, marriage, marriage.getColor().name());
	}

	@Override
	public void divorce(final @NotNull MARRIAGE marriage)
	{
		runStatementAsyncIncludeKey(queryDelMarriage, marriage);
	}

	@Override
	public void marry(final @NotNull MARRIAGE marriage, final @Nullable Consumer<MarriageDataBase> updateCallback)
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
						if(updateCallback != null) updateCallback.accept(marriage);
					}
				}
			}
			catch(SQLException e)
			{
				logger.log(Level.SEVERE, e, () -> "Failed to save marriage " + marriage.getPartner1().getName() + " - " + marriage.getPartner2().getName());
			}
		});
	}

	@Override
	public void updateSurname(final @NotNull MARRIAGE marriage, final @Nullable Consumer<MarriageDataBase> updateCallback)
	{
		runStatementAsyncIncludeKey((updateCallback != null) ? () -> updateCallback.accept(marriage) : null, querySetSurname, marriage, marriage.getSurname());
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
					logger.log(Level.INFO, "No auto ID for player \"{0}\", try to load id from database ...", player.name);
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
								logger.log(Level.WARNING, ConsoleColor.RED + "No ID for player \"{0}\", there is something wrong with this player! You should check that!" + ConsoleColor.RESET, player.name);
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
			logger.log(Level.WARNING, e, () -> ConsoleColor.RED + "Failed migrating player \"" + player.name + "\"!" + ConsoleColor.RESET);
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
							logger.log(Level.WARNING, e, () -> ConsoleColor.RED + "Failed adding home for marriage \"" + marriage.player1.name + " <-> " + marriage.player2.name + "\"!" + ConsoleColor.RESET);
						}
					}
				}
				else
				{
					logger.log(Level.WARNING, "No ID for marriage \"{0} <-> {1}\"!", new Object[]{marriage.player1.name, marriage.player2.name});
				}
			}
		}
		catch(SQLException e)
		{
			logger.log(Level.WARNING, e, () -> ConsoleColor.RED + "Failed adding marriage \"" + marriage.player1.name + " <-> " + marriage.player2.name + "\"!" + ConsoleColor.RESET);
		}
	}

	@Override
	public Logger getLogger()
	{
		return logger;
	}
}
