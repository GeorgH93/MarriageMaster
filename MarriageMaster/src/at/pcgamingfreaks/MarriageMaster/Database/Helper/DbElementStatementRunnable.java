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

package at.pcgamingfreaks.MarriageMaster.Database.Helper;

import at.pcgamingfreaks.Database.DBTools;
import at.pcgamingfreaks.MarriageMaster.Database.DatabaseElement;
import at.pcgamingfreaks.MarriageMaster.Database.Backend.SQLBasedDatabase;

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.SQLException;

public class DbElementStatementRunnable implements Runnable
{
	protected final DatabaseElement databaseElement;
	protected final SQLBasedDatabase database;
	protected final @Language("SQL") String query;
	protected final Object[] args;

	public DbElementStatementRunnable(@NotNull SQLBasedDatabase database, @NotNull DatabaseElement databaseElement, @NonNls @Language("SQL") String query, @Nullable Object... args)
	{
		this.databaseElement = databaseElement;
		this.database = database;
		this.query = query;
		this.args = args;
	}

	@Override
	public void run()
	{
		if(!(databaseElement.getDatabaseKey() instanceof Integer)) return;
		try(Connection connection = database.getConnection())
		{
			runStatement(connection);
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}

	protected void runStatement(Connection connection)
	{
		DBTools.runStatementWithoutException(connection, query, args);
	}
}