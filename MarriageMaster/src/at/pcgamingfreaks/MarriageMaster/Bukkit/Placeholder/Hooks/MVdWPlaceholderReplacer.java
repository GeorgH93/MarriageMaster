/*
 *   Copyright (C) 2022 GeorgH93
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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Placeholder.Hooks;

import at.pcgamingfreaks.ConsoleColor;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Placeholder.PlaceholderManager;

import org.jetbrains.annotations.NotNull;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import be.maximvdw.placeholderapi.PlaceholderReplaceEvent;
import be.maximvdw.placeholderapi.PlaceholderReplacer;
import be.maximvdw.placeholderapi.internal.PlaceholderPack;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Locale;
import java.util.logging.Level;

public class MVdWPlaceholderReplacer implements PlaceholderReplacer, PlaceholderAPIHook
{
	private MarriageMaster plugin;
	private PlaceholderManager placeholderManager;

	public MVdWPlaceholderReplacer(MarriageMaster plugin, PlaceholderManager placeholderManager)
	{
		set(plugin, placeholderManager);
		boolean MVdW = true;
		for(String placeholder : placeholderManager.getPlaceholders().keySet())
		{
			MVdW &= PlaceholderAPI.registerPlaceholder(plugin, "marriagemaster_" + placeholder, this);
		}
		if(MVdW)
		{
			this.plugin.getLogger().info(ConsoleColor.GREEN + "MVdW placeholder hook was successfully registered!" + ConsoleColor.RESET);
		}
		else
		{
			this.plugin.getLogger().info(ConsoleColor.RED + "MVdW placeholder hook failed to registered!" + ConsoleColor.RESET);
		}
	}

	public void set(MarriageMaster plugin, PlaceholderManager placeholderManager)
	{
		// Workaround because we can't unregister from MVdWPlaceholders
		this.plugin = plugin;
		this.placeholderManager = placeholderManager;
	}

	@Override
	public String onPlaceholderReplace(PlaceholderReplaceEvent placeholderReplaceEvent)
	{
		if(placeholderManager == null) return "Marriage Master is not running";
		return placeholderManager.replacePlaceholder(placeholderReplaceEvent.getPlayer(), placeholderReplaceEvent.getPlaceholder().substring(15));
	}

	@Override
	public void close()
	{
		placeholderManager = null;
		plugin = null;
	}

	@Override
	public void testPlaceholders(@NotNull BufferedWriter writer) throws IOException
	{
		writer.append("\nMVdWPlaceholder integration test:\n");
		int success = 0;
		try
		{
			Field fieldCustomPlaceholders = PlaceholderAPI.class.getDeclaredField("customPlaceholders");
			fieldCustomPlaceholders.setAccessible(true);
			PlaceholderPack customPlaceholders = (PlaceholderPack) fieldCustomPlaceholders.get(null);
			for(String placeholder : placeholderManager.getPlaceholdersList())
			{
				be.maximvdw.placeholderapi.internal.PlaceholderReplacer<?> replacer = customPlaceholders.getPlaceholderReplacer("{" + placeholder.toLowerCase(Locale.ENGLISH) + "}");
				if(replacer == null || replacer.getArguments() == null || replacer.getArguments().length == 0)
				{
					writer.append(placeholder).append(" not found in MVdWPlaceholder replacer map!\n");
				}
				else if(!(replacer.getArguments()[0] instanceof MVdWPlaceholderReplacer))
				{
					writer.append(placeholder).append(" not registered to Marriage Master! Class: ").append(replacer.getArguments()[0].getClass().getName()).append("\n");
				}
				else
				{
					success++;
				}
			}
			if(success == placeholderManager.getPlaceholdersList().size())
			{
				writer.append("Success!\n\n");
			}
			else
			{
				writer.append("Failed! ").append(String.valueOf(success)).append(" of ").append(String.valueOf(placeholderManager.getPlaceholdersList().size())).append(" placeholders are linked correctly!\n\n");
			}
		}
		catch(NoSuchFieldException | IllegalAccessException | NullPointerException e)
		{
			writer.append("Failed! ").append(e.getMessage()).append("\n\n");
			plugin.getLogger().log(Level.WARNING, "Failed to test MVdWPlaceholder integration!", e);
		}
	}
}
