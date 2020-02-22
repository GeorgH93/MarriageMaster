/*
 *   Copyright (C) 2020 GeorgH93
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

package at.pcgamingfreaks.MarriageMaster.Bungee;

import at.pcgamingfreaks.BadRabbit.Bungee.BadRabbit;
import at.pcgamingfreaks.MarriageMaster.MagicValues;
import at.pcgamingfreaks.Version;

import net.md_5.bungee.api.plugin.Plugin;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class MarriageMasterBadRabbit extends BadRabbit
{
	@Override
	protected @NotNull Plugin createInstance() throws Exception
	{
		Plugin newPluginInstance = null, pcgfPluginLib = getProxy().getPluginManager().getPlugin("PCGF_PluginLib");
		if(pcgfPluginLib != null)
		{
			if(new Version(pcgfPluginLib.getDescription().getVersion()).olderThan(new Version(MagicValues.MIN_PCGF_PLUGIN_LIB_VERSION)))
			{
				getLogger().info("PCGF-PluginLib to old! Switching to standalone mode!");
			}
			else
			{
				getLogger().info("PCGF-PluginLib installed. Switching to normal mode!");
				newPluginInstance = new MarriageMaster();
			}
		}
		else
		{
			getLogger().info("PCGF-PluginLib not installed. Switching to standalone mode!");
		}
		if(newPluginInstance == null)
		{
			Class<?> standaloneClass = Class.forName("at.pcgamingfreaks.MarriageMasterStandalone.Bungee.MarriageMaster");
			newPluginInstance = (Plugin) standaloneClass.newInstance();
		}
		return newPluginInstance;
	}
}