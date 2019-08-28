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
		//TODO: change registry of placeholders to annotation
		placeholders.put("StatusHeart", new StatusHeart(plugin));
		placeholders.put("Status_Heart", placeholders.get("StatusHeart"));
		placeholders.put("MagicHeart", new MagicHeart(plugin));
		placeholders.put("Magic_Heart", placeholders.get("MagicHeart"));
		placeholders.put("Heart", new Heart(plugin));
		placeholders.put("Prefix", new Prefix(plugin));
		placeholders.put("Suffix", new Suffix(plugin));
		placeholders.put("Married", placeholders.get("Heart"));
		placeholders.put("IsMarried", new IsMarried(plugin));
		placeholders.put("IsPriest", new IsPriest(plugin));
		placeholders.put("Partner", new PartnerName(plugin));
		placeholders.put("PartnerName", placeholders.get("Partner"));
		placeholders.put("Partner_Name", placeholders.get("Partner"));
		placeholders.put("PartnerDisplayName", new PartnerDisplayName(plugin));
		placeholders.put("Partner_DisplayName", placeholders.get("PartnerDisplayName"));
		placeholders.put("Surname", new Surname(plugin));
		placeholders.put("HasHome", new HasHome(plugin));
		placeholders.put("Has_Home", placeholders.get("HasHome"));
		placeholders.put("HomeX", new HomeX(plugin));
		placeholders.put("Home_X", placeholders.get("HomeX"));
		placeholders.put("HomeY", new HomeY(plugin));
		placeholders.put("Home_Y", placeholders.get("HomeY"));
		placeholders.put("HomeZ", new HomeZ(plugin));
		placeholders.put("Home_Z", placeholders.get("HomeZ"));
		placeholders.put("HomeWorld", new HomeWorld(plugin));
		placeholders.put("Home_World", placeholders.get("HomeWorld"));
		if(plugin.areMultiplePartnersAllowed())
		{
			placeholders.put("NearestPrefix", new NearestPrefix(plugin));
			placeholders.put("Nearest_Prefix", placeholders.get("NearestPrefix"));
			placeholders.put("NearestSuffix", new NearestSuffix(plugin));
			placeholders.put("Nearest_Suffix", placeholders.get("NearestSuffix"));
			placeholders.put("NearestPartnerName", new NearestPartnerName(plugin));
			placeholders.put("Nearest_PartnerName", placeholders.get("NearestPartnerName"));
			placeholders.put("Nearest_Partner_Name", placeholders.get("NearestPartnerName"));
			placeholders.put("NearestPartnerDisplayName", new NearestPartnerDisplayName(plugin));
			placeholders.put("Nearest_PartnerDisplayName", placeholders.get("NearestPartnerDisplayName"));
			placeholders.put("Nearest_Partner_DisplayName", placeholders.get("NearestPartnerDisplayName"));
			placeholders.put("NearestSurname", new NearestSurname(plugin));
			placeholders.put("Nearest_Surname", placeholders.get("NearestSurname"));
			placeholders.put("HasNearestHome", new HasNearestHome(plugin));
			placeholders.put("Has_Nearest_Home", placeholders.get("HasNearestHome"));
			placeholders.put("NearestHomeX", new NearestHomeX(plugin));
			placeholders.put("Nearest_HomeX", placeholders.get("NearestHomeX"));
			placeholders.put("Nearest_Home_X", placeholders.get("NearestHomeX"));
			placeholders.put("NearestHomeY", new NearestHomeY(plugin));
			placeholders.put("Nearest_HomeY", placeholders.get("NearestHomeY"));
			placeholders.put("Nearest_Home_Y", placeholders.get("NearestHomeY"));
			placeholders.put("NearestHomeZ", new NearestHomeZ(plugin));
			placeholders.put("Nearest_HomeZ", placeholders.get("NearestHomeZ"));
			placeholders.put("Nearest_Home_Z", placeholders.get("NearestHomeZ"));
			placeholders.put("NearestHomeWorld", new NearestHomeWorld(plugin));
			placeholders.put("Nearest_HomeWorld", placeholders.get("NearestHomeWorld"));
			placeholders.put("Nearest_Home_World", placeholders.get("NearestHomeWorld"));
			placeholders.put("PartnerCount", new PartnerCount(plugin));
			placeholders.put("Partner_Count", placeholders.get("PartnerCount"));
			placeholders.put("PartnerList", new PartnerList(plugin));
			placeholders.put("Partner_List", placeholders.get("PartnerList"));
			placeholders.put("PartnerNameList", placeholders.get("PartnerList"));
			placeholders.put("Partner_Name_List", placeholders.get("PartnerList"));
			placeholders.put("PartnerDisplayNameList", new PartnerDisplayNameList(plugin));
			placeholders.put("PartnerDisplayName_List", placeholders.get("PartnerDisplayNameList"));
			placeholders.put("Partner_DisplayName_List", placeholders.get("PartnerDisplayNameList"));
		}
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