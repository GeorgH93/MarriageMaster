/*
 *   Copyright (C) 2019 GeorgH93
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

package at.pcgamingfreaks.MarriageMaster.Database;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface DatabaseConfiguration extends DatabaseConnectionConfiguration
{
	@NotNull String getDatabaseType();

	boolean useUUIDs();

	boolean useUUIDSeparators();

	boolean getUseOnlineUUIDs();

	@NotNull String getSQLTableUser();

	@NotNull String getSQLTableHomes();

	@NotNull String getSQLTablePriests();

	@NotNull String getSQLTableMarriages();

	@Contract("_, !null -> !null")
	@Nullable String getSQLField(@NotNull String field, @Nullable String defaultValue);

	@NotNull String getUnCacheStrategie();

	long getUnCacheInterval();

	long getUnCacheDelay();
}

