/*
 *   Copyright (C) 2023 GeorgH93
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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Placeholder;

import at.pcgamingfreaks.Bukkit.Placeholder.PlaceholderReplacer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Placeholder.Replacer.*;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Placeholder.Replacer.MultiPartner.*;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Range;
import at.pcgamingfreaks.Reflection;

import java.util.logging.Level;

public class PlaceholderManager extends at.pcgamingfreaks.Bukkit.Placeholder.PlaceholderManager
{
	public PlaceholderManager(MarriageMaster plugin)
	{
		super(plugin);
	}

	@Override
	protected void generatePlaceholdersMap()
	{
		MarriageMaster plugin = (MarriageMaster) getPlugin();
		registerPlaceholder(new HasHome(plugin));
		registerPlaceholder(new Heart(plugin));
		registerPlaceholder(new Home(plugin));
		registerPlaceholder(new HomeCoordinates(plugin));
		registerPlaceholder(new HomeWorld(plugin));
		registerPlaceholder(new HomeX(plugin));
		registerPlaceholder(new HomeY(plugin));
		registerPlaceholder(new HomeZ(plugin));
		registerPlaceholder(new IsMarried(plugin));
		registerPlaceholder(new IsPriest(plugin));
		registerPlaceholder(new PartnerCount(plugin));
		registerPlaceholder(new PartnerDisplayName(plugin));
		registerPlaceholder(new PartnerName(plugin));
		registerPlaceholder(new Prefix(plugin));
		registerPlaceholder(new Suffix(plugin));
		registerPlaceholder(new Surname(plugin));
		registerPlaceholder(new StatusHeart(plugin));
		registerPlaceholder(new MagicHeart(plugin));
		for(Range range : Range.values())
		{
			if(range == Range.Marry) continue;
			registerPlaceholder(new IsInRange(plugin, range));
		}

		if(plugin.areMultiplePartnersAllowed())
		{
			registerPlaceholder(new NearestPrefix(plugin));
			registerPlaceholder(new NearestSuffix(plugin));
			registerPlaceholder(new NearestPartnerName(plugin));
			registerPlaceholder(new NearestPartnerDisplayName(plugin));
			registerPlaceholder(new NearestSurname(plugin));
			registerPlaceholder(new NearestHasHome(plugin));
			registerPlaceholder(new NearestHome(plugin));
			registerPlaceholder(new NearestHomeCoordinates(plugin));
			registerPlaceholder(new NearestHomeX(plugin));
			registerPlaceholder(new NearestHomeY(plugin));
			registerPlaceholder(new NearestHomeZ(plugin));
			registerPlaceholder(new NearestHomeWorld(plugin));
			registerPlaceholder(new PartnerList(plugin));
			registerPlaceholder(new PartnerDisplayNameList(plugin));
			for(int i = 0; i < 10; i++)
			{
				registerPlaceholder(new PartnerNameId(plugin, i));
				registerPlaceholder(new PartnerDisplayNameId(plugin, i));
			}
		}
	}

	public void registerPlaceholder(MarriagePlaceholderReplacer placeholder)
	{
		if(placeholder.getClass().isAnnotationPresent(PlaceholderFormatted.class))
		{
			try
			{
				PlaceholderFormatted pfa = placeholder.getClass().getAnnotation(PlaceholderFormatted.class);
				if(placeholder.getFormat() != null && placeholder.getFormat().matches(pfa.formatRuleDetectionRegex()))
					placeholder = (MarriagePlaceholderReplacer) Reflection.getConstructor(pfa.formattedClass(), MarriageMaster.class).newInstance(getPlugin());
			}
			catch(Exception e)
			{
				final PlaceholderReplacer placeholderReplacer = placeholder;
				getPlugin().getLogger().log(Level.SEVERE, e, () -> "Failed to register placeholder '" + placeholderReplacer.getName() + "'!");
				return;
			}
		}
		super.registerPlaceholder(placeholder);
	}
}