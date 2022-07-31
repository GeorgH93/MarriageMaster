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

import java.util.HashSet;
import java.util.Objects;

public class FilteredHashSet<T> extends HashSet<T>
{
	public interface Filter<T>
	{
		boolean isAllowed(T element);
	}

	private final Filter<T> filter;

	public FilteredHashSet(Filter<T> filter)
	{
		this.filter = filter;
	}

	@Override
	public boolean add(T element)
	{
		return filter.isAllowed(element) && super.add(element);
	}

	@Override
	public boolean equals(Object o)
	{
		if(this == o) return true;
		if(o == null || !Objects.equals(getClass(), o.getClass()) || !super.equals(o)) return false;
		FilteredHashSet<?> filteredSet = (FilteredHashSet<?>) o;
		return Objects.equals(filter, filteredSet.filter);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(super.hashCode(), filter);
	}
}