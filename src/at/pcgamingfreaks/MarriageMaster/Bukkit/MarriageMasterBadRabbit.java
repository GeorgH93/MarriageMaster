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

package at.pcgamingfreaks.MarriageMaster.Bukkit;

import at.pcgamingfreaks.BadRabbit.Bukkit.BadRabbit;
import at.pcgamingfreaks.BadRabbit.Bukkit.PluginClassLoaderBypass;
import at.pcgamingfreaks.MarriageMaster.MethodTypeReplacer;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;

@SuppressWarnings("unused")
public class MarriageMasterBadRabbit extends BadRabbit
{
	@Override
	protected @NotNull JavaPlugin createInstance() throws Exception
	{
		JavaPlugin newPluginInstance;
		if(Bukkit.getPluginManager().getPlugin("PCGF_PluginLib") == null)
		{
			//region modify api
			ModifyAPI modifyApiLoader = new ModifyAPI(getClassLoader());
			modifyApiLoader.loadClass("at.pcgamingfreaks.MarriageMaster.API.CommandManager");
			//endregion

			getLogger().info("PCGF-PluginLib not installed. Switching to standalone mode!");
			Class<?> standaloneClass = Class.forName("at.pcgamingfreaks.MarriageMasterStandalone.Bukkit.MarriageMaster");
			newPluginInstance = (JavaPlugin) standaloneClass.newInstance();
		}
		else
		{
			getLogger().info("PCGF-PluginLib installed. Switching to normal mode!");
			newPluginInstance = new MarriageMaster();
		}
		return newPluginInstance;
	}

	private static class ModifyAPI extends PluginClassLoaderBypass
	{
		ModifyAPI(ClassLoader classLoader) throws Exception
		{
			super(classLoader);
		}

		@Override
		protected byte[] loadJarData(@NotNull String name, @NotNull InputStream stream) throws Exception
		{
			System.out.println(name);
			switch(name)
			{
				case "at.pcgamingfreaks.MarriageMaster.API.CommandManager":
					return MethodTypeReplacer.replace("at/pcgamingfreaks/MarriageMaster/API/MarryCommand", "at/pcgamingfreaks/MarriageMasterStandalone/API/MarryCommand", stream);
			}
			return super.loadJarData(name, stream);
		}
	}
}