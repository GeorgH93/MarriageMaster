/*
 *   Copyright (C) 2021 GeorgH93
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

import at.pcgamingfreaks.Database.Cache.IUnCacheStrategyConfig;
import at.pcgamingfreaks.Database.DatabaseConnectionConfiguration;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public interface DatabaseConfiguration extends DatabaseConnectionConfiguration, IUnCacheStrategyConfig
{
	default @NotNull String getDatabaseType()
	{
		return getConfigE().getString("Database.Type", "MySQL").toLowerCase(Locale.ENGLISH);
	}

	default boolean useUUIDSeparators()
	{
		return getConfigE().getBoolean("Database.UseUUIDSeparators", false);
	}

	boolean useOnlineUUIDs();

	@Contract("_, !null -> !null")
	default @NotNull String getSQLTable(final @NotNull String table, final @Nullable String defaultValue)
	{
		return getConfigE().getString("Database.SQL.Tables." + table, defaultValue);
	}

	@Contract("_, !null -> !null")
	default @Nullable String getSQLField(final @NotNull String field, final @Nullable String defaultValue)
	{
		return getConfigE().getString("Database.SQL.Tables.Fields." + field, defaultValue);
	}

	default void setDatabaseType(final @NotNull String type) {}
}

