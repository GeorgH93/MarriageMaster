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
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.MarriageMaster.Bukkit.Placeholder.Replacer.MultiPartner;

import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Placeholder.PlaceholderFormatted;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Placeholder.PlaceholderName;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Placeholder.Replacer.Formatted.PartnerNameFormatted;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Placeholder.Replacer.PlaceholderReplacerBaseValue;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@PlaceholderName(aliases = { "Partner%d", "Partner_%d", "Partner_Name_%d" })
@PlaceholderFormatted(formattedClass = PartnerNameFormatted.class)
public class PartnerNameId extends PlaceholderReplacerBaseValue
{
	private final int id;

	public PartnerNameId(final @NotNull MarriageMaster plugin, final int id)
	{
		super(plugin);
		this.id = id;
	}

	@Override
	protected @Nullable String replaceMarried(MarriagePlayer player)
	{
		Collection<? extends MarriagePlayer> partners = player.getPartners();
		MarriagePlayer partner = player;
		int i = 0;
		for(MarriagePlayer p : partners)
		{
			partner = p;
			if(i >= id) break;
		}
		return partner.getName();
	}

	@Override
	public @NotNull String getName()
	{
		return "PartnerName" + id;
	}

	@Override
	public @NotNull Collection<String> getAliases()
	{
		Collection<String> aliasesCollection = super.getAliases();
		List<String> aliases = new ArrayList<>(aliasesCollection.size());
		for(String alias : aliasesCollection)
		{
			aliases.add(String.format(alias, id));
		}
		return aliases;
	}
}