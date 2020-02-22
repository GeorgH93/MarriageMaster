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

package at.pcgamingfreaks.MarriageMaster.Database.FilesMigrator;

public class MigrationMarriage
{
	public MigrationPlayer player1, player2, priest;
	public String surname;
	public boolean pvpState;
	public Home home;
	public int id;

	public MigrationMarriage(MigrationPlayer p1, MigrationPlayer p2, MigrationPlayer p, String surname, boolean pvp, Home h)
	{
		player1 = p1;
		player2 = p2;
		priest = p;
		this.surname = surname;
		pvpState = pvp;
		home = h;
		id = -1;
	}
}