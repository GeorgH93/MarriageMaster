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

package at.pcgamingfreaks.MarriageMaster.Database.Helper;

import at.pcgamingfreaks.Database.DBTools;
import at.pcgamingfreaks.MarriageMaster.Database.Backend.SQLBasedDatabase;
import at.pcgamingfreaks.MarriageMaster.Database.DatabaseElement;

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.util.Arrays;

public class DbElementStatementWithKeyRunnable extends DbElementStatementRunnable
{
	public DbElementStatementWithKeyRunnable(@NotNull SQLBasedDatabase database, @NotNull DatabaseElement databaseElement, @NonNls @Language("SQL") String query, @Nullable Object... args)
	{
		super(database, databaseElement, query, args);
	}

	@Override
	protected void runStatement(Connection connection)
	{
		if(args == null)
		{
			DBTools.runStatementWithoutException(connection, query, databaseElement.getDatabaseKey());
		}
		else
		{
			Object[] comboArgs = Arrays.copyOf(args, args.length + 1);
			comboArgs[args.length] = databaseElement.getDatabaseKey();
			DBTools.runStatementWithoutException(connection, query, comboArgs);
		}
	}
}