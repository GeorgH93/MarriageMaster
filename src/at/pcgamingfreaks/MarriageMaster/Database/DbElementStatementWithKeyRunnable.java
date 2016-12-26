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

package at.pcgamingfreaks.MarriageMaster.Database;

import at.pcgamingfreaks.Database.DBTools;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.util.Arrays;

public class DbElementStatementWithKeyRunnable extends DbElementStatementRunnable
{
	public DbElementStatementWithKeyRunnable(@NotNull SQLBasedDatabase database, @NotNull DatabaseElement databaseElement, @NotNull String query, @Nullable Object... args)
	{
		super(database, databaseElement, query, args);
	}

	@Override
	protected void runStatement(Connection connection)
	{
		if(args == null)
		{
			DBTools.runStatement(connection, query, databaseElement.getDatabaseKey());
		}
		else
		{
			Object[] comboArgs = Arrays.copyOf(args, args.length + 1);
			comboArgs[args.length] = databaseElement.getDatabaseKey();
			DBTools.runStatement(connection, query, comboArgs);
		}
	}
}