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

package at.pcgamingfreaks.MarriageMaster.Database;

import at.pcgamingfreaks.ConsoleColor;
import at.pcgamingfreaks.Database.ConnectionProvider.ConnectionProvider;
import at.pcgamingfreaks.MarriageMaster.API.Home;
import at.pcgamingfreaks.MarriageMaster.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.API.MarriageMasterPlugin;
import at.pcgamingfreaks.MarriageMaster.Database.Backend.DatabaseBackend;
import at.pcgamingfreaks.MarriageMaster.Database.Backend.MySQL;
import at.pcgamingfreaks.MarriageMaster.Database.Backend.SQL;
import at.pcgamingfreaks.MarriageMaster.Database.Backend.SQLite;
import at.pcgamingfreaks.MarriageMaster.Database.FilesMigrator.Converter;

import org.jetbrains.annotations.NotNull;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class BaseDatabase<MARRIAGE_MASTER extends MarriageMasterPlugin, MARRIAGE_PLAYER_DATA extends MarriagePlayerDataBase, MARRIAGE_DATA extends MarriageDataBase, HOME extends Home>
{
	@Getter @Setter(AccessLevel.PRIVATE) private static BaseDatabase instance;

	//region Messages
	protected static final String MESSAGE_FILES_NO_LONGER_SUPPORTED = ConsoleColor.RED + "File based storage is no longer supported." + ConsoleColor.YELLOW + " Migrating to SQLite." + ConsoleColor.RESET;
	protected static final String MESSAGE_UNKNOWN_DB_TYPE = ConsoleColor.RED + "Unknown database type \"{0}\"!" + ConsoleColor.RESET;
	protected static final String MESSAGE_CLEANING_DB_CACHE = "Cleaning database cache.", MESSAGE_DB_CACHE_CLEANED = "Database cache cleaned.";
	//endregion

	protected final boolean bungee;
	protected final Logger logger;
	protected final MARRIAGE_MASTER plugin;
	protected final Cache<MARRIAGE_PLAYER_DATA, MARRIAGE_DATA> cache = new Cache<>();
	protected final DatabaseBackend<MARRIAGE_PLAYER_DATA, MARRIAGE_DATA, HOME> backend;
	protected final IPlatformSpecific<MARRIAGE_PLAYER_DATA, MARRIAGE_DATA, HOME> platform;
	protected final Runnable loadRunnable;
	private PluginChannelCommunicatorBase communicatorBase = null;

	//region callbacks
	private Consumer<MarriageDataBase> callbackMarry = null;
	private Consumer<MarriageDataBase> callbackUpdateHome = null;
	private Consumer<MarriageDataBase> callbackUpdatePvP = null;
	private Consumer<MarriageDataBase> callbackUpdateColor = null;
	private Consumer<MarriageDataBase> callbackUpdateSurname = null;
	private Consumer<MarriagePlayerDataBase> callbackUpdateBackpack = null;
	private Consumer<MarriagePlayerDataBase> callbackUpdatePriest = null;
	//endregion

	protected BaseDatabase(final @NotNull MARRIAGE_MASTER plugin, final @NotNull Logger logger, final @NotNull IPlatformSpecific<MARRIAGE_PLAYER_DATA, MARRIAGE_DATA, HOME> platform,
	                       final @NotNull DatabaseConfiguration dbConfig, final @NotNull String pluginName, final @NotNull File dataFolder, final boolean bungee, final boolean bungeeSupportRequired)
	{
		this.plugin = plugin;
		this.logger = logger;
		this.platform = platform;
		this.bungee = bungee;
		backend = getDatabaseBackend(platform, dbConfig, bungee, plugin.isSurnamesEnabled(), cache, logger, pluginName, dataFolder, bungeeSupportRequired);
		if(available())
		{
			setInstance(this);
			loadRunnable = new LoadRunnable();
		}
		else loadRunnable = null;
	}

	public DatabaseBackend<MARRIAGE_PLAYER_DATA, MARRIAGE_DATA, HOME> getDatabaseBackend(final @NotNull IPlatformSpecific<MARRIAGE_PLAYER_DATA, MARRIAGE_DATA, HOME> platform,
	                                                                                     final @NotNull DatabaseConfiguration dbConfig, final boolean bungee, final boolean surnameEnabled,
	                                                                                     final @NotNull Cache<MARRIAGE_PLAYER_DATA, MARRIAGE_DATA> cache, final @NotNull Logger logger,
	                                                                                     final @NotNull String pluginName, final @NotNull File dataFolder, final boolean bungeeSupportRequired)
	{
		try
		{
			String dbType = dbConfig.getDatabaseType();
			ConnectionProvider connectionProvider = platform.getExternalConnectionProvider(dbType, logger);
			DatabaseBackend<MARRIAGE_PLAYER_DATA, MARRIAGE_DATA, HOME> db;
			switch((connectionProvider != null) ? connectionProvider.getDatabaseType() : dbType)
			{
				case "mysql": case "mariadb": db = new MySQL<>(platform, dbConfig, bungee, surnameEnabled, cache, logger, connectionProvider, pluginName); break;
				case "sqlite": db = new SQLite<>(platform, dbConfig, bungee, surnameEnabled, cache, logger, connectionProvider, pluginName, dataFolder); break;
				case "file":
				case "files":
				case "flat": logger.info(MESSAGE_FILES_NO_LONGER_SUPPORTED);
					db = new SQLite<>(platform, dbConfig, bungee, surnameEnabled, cache, logger, null, pluginName, dataFolder);
					db.startup();
					Converter.runConverter(logger, dbConfig, db, dataFolder);
					return db;
				default: logger.log(Level.WARNING, MESSAGE_UNKNOWN_DB_TYPE, dbType); return null;
			}
			if(!db.supportsBungeeCord())
			{
				if(bungeeSupportRequired)
				{
					logger.severe("Database type not supported on BungeeCord!");
					db.close();
					return null;
				}
				if(bungee)
				{
					logger.warning("The used database does not support multi-server setups! Please consider switching to MySQL!");
				}
			}
			db.startup();
			return db;
		}
		catch(Exception ignored)
		{
			logger.severe("Failed to initialize database backend!");
		}
		return null;
	}

	public boolean available()
	{
		return backend != null;
	}

	public final boolean useBungee()
	{
		return bungee;
	}

	protected void close()
	{
		setInstance(null);
		logger.info(MESSAGE_CLEANING_DB_CACHE);
		cache.close();
		logger.info(MESSAGE_DB_CACHE_CLEANED);
		if(backend != null) backend.close();
	}

	void setCommunicatorBase(PluginChannelCommunicatorBase communicatorBase)
	{
		this.communicatorBase = communicatorBase;
		if (communicatorBase != null)
		{
			callbackMarry = communicatorBase::marry;
			callbackUpdateHome = communicatorBase::updateHome;
			callbackUpdatePvP = communicatorBase::updatePvP;
			callbackUpdateColor = communicatorBase::updateMarriageColor;
			callbackUpdateSurname = communicatorBase::updateSurname;
			callbackUpdateBackpack = communicatorBase::updateBackpackShareState;
			callbackUpdatePriest = communicatorBase::updatePriestStatus;
		}
		else
		{
			callbackMarry = null;
			callbackUpdateHome = null;
			callbackUpdatePvP = null;
			callbackUpdateColor = null;
			callbackUpdateSurname = null;
			callbackUpdateBackpack = null;
			callbackUpdatePriest = null;
		}
	}

	public Cache<MARRIAGE_PLAYER_DATA, MARRIAGE_DATA> getCache()
	{
		return cache;
	}

	public Collection<String> getSurnames()
	{
		return cache.getSurnames();
	}

	public void cachedSurnameUpdate(MARRIAGE_DATA marriage, String oldSurname)
	{
		cachedSurnameUpdate(marriage, oldSurname, true);
	}

	public void cachedSurnameUpdate(MARRIAGE_DATA marriage, String oldSurname, boolean updateDatabase)
	{
		if(updateDatabase) updateSurname(marriage);
		if(oldSurname != null && !oldSurname.isEmpty())
		{
			cache.removeSurname(oldSurname);
		}
		if(marriage.getSurname() != null && !marriage.getSurname().isEmpty())
		{
			cache.addSurname(marriage);
		}
	}

	public void cachedDivorce(final @NotNull MARRIAGE_DATA marriage)
	{
		cachedDivorce(marriage, true);
	}

	public void cachedDivorce(final @NotNull MARRIAGE_DATA marriage, boolean updateDatabase)
	{
		if(updateDatabase) divorce(marriage);
		cache.unCache(marriage);
	}

	public void cachedMarry(final @NotNull MARRIAGE_DATA marriage)
	{
		marry(marriage);
		cache.cache(marriage);
	}

	public abstract MARRIAGE_PLAYER_DATA getPlayer(UUID uuid);

	protected abstract void loadOnlinePlayers();

	protected void load(final @NotNull MARRIAGE_PLAYER_DATA player)
	{
		backend.load(player);
	}

	//region data management methods
	public void loadMarriage(final int marriageId)
	{
		if(backend instanceof SQL) ((SQL)backend).loadMarriage(marriageId);
	}

	public void loadHome(final MARRIAGE_DATA marriage)
	{
		if(backend instanceof SQL) ((SQL)backend).loadHome(marriage);
	}

	public void updateHome(final MARRIAGE_DATA marriage)
	{
		backend.updateHome(marriage, callbackUpdateHome);
	}

	public void updatePvPState(final MARRIAGE_DATA marriage)
	{
		backend.updatePvPState(marriage, callbackUpdatePvP);
	}

	public void updateMarriageColor(final MARRIAGE_DATA marriage)
	{
		backend.updateMarriageColor(marriage, callbackUpdateColor);
	}

	public void updateBackpackShareState(final MARRIAGE_PLAYER_DATA player)
	{
		backend.updateBackpackShareState(player, callbackUpdateBackpack);
	}

	public void updatePriestStatus(final MARRIAGE_PLAYER_DATA player)
	{
		backend.updatePriestStatus(player, callbackUpdatePriest);
		if(bungee && communicatorBase != null) communicatorBase.updatePriestStatus(player);
	}

	protected void updateSurname(final MARRIAGE_DATA marriage)
	{
		backend.updateSurname(marriage, callbackUpdateSurname);
		if(bungee && communicatorBase != null) communicatorBase.updateSurname(marriage);
	}

	protected void marry(final MARRIAGE_DATA marriage)
	{
		backend.marry(marriage, callbackMarry);
	}

	protected void divorce(final MARRIAGE_DATA marriage)
	{
		backend.divorce(marriage);
		if(bungee && communicatorBase != null) communicatorBase.divorce(marriage);
	}

	public void resync()
	{
		logger.info("Performing resync with database");
		cache.clear();
		platform.runAsync(loadRunnable, 0); // Performs the resync async
	}
	//endregion

	private class LoadRunnable implements Runnable
	{
		@Override
		public void run()
		{
			backend.loadAll();
			cache.reCacheSurnames();
			loadOnlinePlayers();
		}
	}
}