/*
 *   Copyright (C) 2022 GeorgH93
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

import at.pcgamingfreaks.MarriageMaster.API.Home;
import at.pcgamingfreaks.MarriageMaster.Database.*;
import at.pcgamingfreaks.MarriageMaster.Database.FilesMigrator.MigrationMarriage;
import at.pcgamingfreaks.MarriageMaster.Database.FilesMigrator.MigrationPlayer;

import org.jetbrains.annotations.NotNull;

import lombok.Setter;

import java.util.UUID;
import java.util.logging.Logger;

public abstract class DatabaseBackend<MARRIAGE_PLAYER extends MarriagePlayerDataBase, MARRIAGE extends MarriageDataBase, HOME extends Home>
{
	protected static final String MESSAGE_UPDATE_UUIDS = "Start updating database to UUIDs ...", MESSAGE_UPDATED_UUIDS = "Updated %d accounts to UUIDs.";
	protected final IPlatformSpecific<MARRIAGE_PLAYER, MARRIAGE, HOME> platform;
	protected final boolean useBungee, useUUIDSeparators, useOnlineUUIDs, surnameEnabled;
	protected final Cache<MARRIAGE_PLAYER, MARRIAGE> cache;
	protected final Logger logger;
	@Setter protected MarriageSavedCallback marriageSavedCallback = null;

	protected DatabaseBackend(final @NotNull IPlatformSpecific<MARRIAGE_PLAYER, MARRIAGE, HOME> platform, final @NotNull DatabaseConfiguration dbConfig, final boolean useBungee, final boolean surnameEnabled,
	                          final @NotNull Cache<MARRIAGE_PLAYER, MARRIAGE> cache, final @NotNull Logger logger)
	{
		this.platform = platform;
		this.useBungee = useBungee;
		this.logger = logger;
		this.surnameEnabled = surnameEnabled;
		this.cache = cache;
		useUUIDSeparators = dbConfig.useUUIDSeparators();
		useOnlineUUIDs = dbConfig.useOnlineUUIDs();
	}

	public void close()	{}

	public void startup() throws Exception
	{
		checkUUIDs();
	}

	public abstract boolean supportsBungeeCord();

	protected String getUsedPlayerIdentifier(MARRIAGE_PLAYER player)
	{
		return useUUIDSeparators ? player.getUUID().toString() : player.getUUID().toString().replace("-", "");
	}

	protected @NotNull UUID getUUIDFromIdentifier(final @NotNull String identifier)
	{
		return UUID.fromString((useUUIDSeparators) ? identifier : identifier.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
	}

	protected void runAsync(final @NotNull Runnable runnable)
	{
		runAsync(runnable, 0);
	}

	protected void runAsync(final @NotNull Runnable runnable, final long delay)
	{
		platform.runAsync(runnable, delay);
	}

	public abstract @NotNull String getDatabaseTypeName();

	public abstract void checkUUIDs();

	public abstract void loadAll();

	public abstract void load(final @NotNull MARRIAGE_PLAYER player);

	public abstract void updateHome(final @NotNull MARRIAGE marriage);

	public abstract void updatePvPState(final @NotNull MARRIAGE marriage);

	public abstract void updateBackpackShareState(final @NotNull MARRIAGE_PLAYER player);

	public abstract void updatePriestStatus(final @NotNull MARRIAGE_PLAYER player);

	public abstract void updateSurname(final @NotNull MARRIAGE marriage);

	public abstract void updateMarriageColor(final @NotNull MARRIAGE marriage);

	public abstract void marry(final @NotNull MARRIAGE marriage);

	public abstract void divorce(final @NotNull MARRIAGE marriage);

	public abstract void migratePlayer(final @NotNull MigrationPlayer player);

	public abstract void migrateMarriage(final @NotNull MigrationMarriage marriage);
}