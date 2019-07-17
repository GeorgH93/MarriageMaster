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

import lombok.Getter;

/**
 * Helper class for converting names to UUID's and for fixing UUID's that are in the wrong format.
 */
public class UpdateUUIDsHelper
{
	@Getter private final int id;
	@Getter private String name;
	private String uuid;

	public UpdateUUIDsHelper(String name, String uuid, int id)
	{
		this.id = id;
		this.name = name;
		this.uuid = uuid;
	}

	public String getUUID()
	{
		return uuid;
	}

	public void setUUID(String uuid)
	{
		this.uuid = uuid;
	}
}