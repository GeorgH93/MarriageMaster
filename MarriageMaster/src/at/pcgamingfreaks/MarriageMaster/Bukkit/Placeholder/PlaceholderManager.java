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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Placeholder;

import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Placeholder.Hooks.ClipsPlaceholderHook;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Placeholder.Hooks.MVdWPlaceholderReplacer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Placeholder.Hooks.PlaceholderAPIHook;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Placeholder.Replacer.*;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Placeholder.Replacer.MultiPartner.*;
import at.pcgamingfreaks.Reflection;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.*;

public class PlaceholderManager
{
	private static MVdWPlaceholderReplacer mVdWPlaceholderReplacer = null; // The MVdWPlaceholder API dose not allow to unregister hooked placeholders
	private final MarriageMaster plugin;
	private final Map<String, at.pcgamingfreaks.MarriageMaster.Bukkit.Placeholder.PlaceholderReplacer> placeholders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	private final List<PlaceholderAPIHook> hooks = new LinkedList<>();
	private final List<String> placeholdersList = new ArrayList<>();

	public PlaceholderManager(MarriageMaster plugin)
	{
		this.plugin = plugin;
		if(!(isPluginEnabled("MVdWPlaceholderAPI") || isPluginEnabled("PlaceholderAPI"))) return; // No supported placeholder API installed
		generatePlaceholdersMap();
		//region MVdW Placeholders
		if(isPluginEnabled("MVdWPlaceholderAPI"))
		{
			if(mVdWPlaceholderReplacer == null) mVdWPlaceholderReplacer = new MVdWPlaceholderReplacer(plugin, this);
			mVdWPlaceholderReplacer.set(plugin, this); // Workaround cause we can't unregister from MVdWPlaceholders
			hooks.add(mVdWPlaceholderReplacer);
		}
		//endregion
		//region Chips PlaceholderAPI
		if(isPluginEnabled("PlaceholderAPI"))
		{
			hooks.add(new ClipsPlaceholderHook(plugin, this));
		}
		//endregion
	}
	
	private static boolean isPluginEnabled(String pluginName)
	{
		return Bukkit.getPluginManager().isPluginEnabled(pluginName);
	}

	public void close()
	{
		for(PlaceholderAPIHook hook : hooks)
		{
			hook.close();
		}
		if(mVdWPlaceholderReplacer != null) mVdWPlaceholderReplacer.set(null, null);
		hooks.clear();
		placeholdersList.clear();
		placeholders.clear();
	}

	public Map<String, PlaceholderReplacer> getPlaceholders()
	{
		return placeholders;
	}

	public String replacePlaceholder(OfflinePlayer player, String identifier)
	{
		if(player == null) return "Player needed!";
		PlaceholderReplacer replacer = placeholders.get(identifier);
		return replacer == null ? null : replacer.replace(player);
	}

	private void generatePlaceholdersMap()
	{
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
		}
	}

	public void registerPlaceholder(PlaceholderReplacer placeholder)
	{
		if(placeholder.getClass().isAnnotationPresent(PlaceholderFormatted.class))
		{
			try
			{
				PlaceholderFormatted pfa = placeholder.getClass().getAnnotation(PlaceholderFormatted.class);
				if(placeholder.getFormat() != null && placeholder.getFormat().matches(pfa.formatRuleDetectionRegex()))
					placeholder = (PlaceholderReplacer) Reflection.getConstructor(pfa.formattedClass(), MarriageMaster.class).newInstance(plugin);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				return;
			}
		}
		placeholders.put(placeholder.getName(), placeholder);
		final PlaceholderReplacer finalPlaceholder = placeholder;
		placeholder.getAliases().forEach(alias -> placeholders.put(alias, finalPlaceholder));
	}

	public List<String> getPlaceholdersList()
	{
		if(placeholdersList.size() == 0)
		{
			for(String key : placeholders.keySet())
			{
				placeholdersList.add(plugin.getDescription().getName() + '_' + key);
			}
		}
		return placeholdersList;
	}
}