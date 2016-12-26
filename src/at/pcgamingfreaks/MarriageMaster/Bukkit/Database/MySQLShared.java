/*
 *   Copyright (C) 2016 GeorgH93
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

import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.PluginLib.Database.DatabaseConnectionPool;

import com.zaxxer.hikari.HikariConfig;

import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;

public class MySQLShared extends MySQL
{
	private DatabaseConnectionPool pool;

	protected MySQLShared(MarriageMaster marriageMaster, DatabaseConnectionPool pool)
	{
		super(marriageMaster);
		this.pool = pool;
	}

	@Override
	protected HikariConfig getPoolConfig()
	{
		return null;
	}

	@Override
	@NotNull
	public Connection getConnection() throws SQLException
	{
		return pool.getConnection();
	}
}