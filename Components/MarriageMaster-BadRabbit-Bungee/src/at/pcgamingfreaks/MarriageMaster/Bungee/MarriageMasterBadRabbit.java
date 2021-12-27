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

package at.pcgamingfreaks.MarriageMaster.Bungee;

import at.pcgamingfreaks.BadRabbit.Bungee.BadRabbit;
import at.pcgamingfreaks.MarriageMaster.MagicValues;
import at.pcgamingfreaks.Version;

import net.md_5.bungee.api.plugin.Plugin;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public class MarriageMasterBadRabbit extends BadRabbit
{
	private boolean standalone = true;

	@Override
	public void onLoad()
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
				standalone = false;
			}
		}
		else
		{
			getLogger().info("PCGF-PluginLib not installed. Switching to standalone mode!");
		}

		if(standalone) loadIMessageClasses();

		super.onLoad();
	}

	@Override
	protected @NotNull Plugin createInstance() throws Exception
	{
		Plugin newPluginInstance;
		if(standalone)
		{
			Class<?> standaloneClass = Class.forName("at.pcgamingfreaks.MarriageMasterStandalone.Bungee.MarriageMaster");
			newPluginInstance = (Plugin) standaloneClass.newInstance();
		}
		else
		{
			Class<?> standaloneClass = Class.forName("at.pcgamingfreaks.MarriageMaster.Bungee.MarriageMaster");
			newPluginInstance = (Plugin) standaloneClass.newInstance();
		}
		return newPluginInstance;
	}

	void loadIMessageClasses()
	{
		try
		{
			File standaloneBonusJarFile = File.createTempFile("IMessage", ".jar");
			Class<?> utilsClass = Class.forName("at.pcgamingfreaks.MarriageMasterStandalone.libs.at.pcgamingfreaks.Utils");
			Method extractMethod = utilsClass.getDeclaredMethod("extractFile", Class.class, Logger.class, String.class, File.class);
			extractMethod.invoke(null, this.getClass(), getLogger(), "IMessage.jar", standaloneBonusJarFile);
			standaloneBonusJarFile.deleteOnExit();

			Class<?> pluginClassLoaderClass = Class.forName("net.md_5.bungee.api.plugin.PluginClassloader");
			Constructor<?> pluginClassLoaderConstructor = pluginClassLoaderClass.getDeclaredConstructors()[0];
			pluginClassLoaderConstructor.setAccessible(true);
			URLClassLoader standaloneBonusClassLoader;
			switch(pluginClassLoaderConstructor.getParameterCount())
			{
				case 1:
					standaloneBonusClassLoader = (URLClassLoader) pluginClassLoaderConstructor.newInstance((Object) (new URL[] { standaloneBonusJarFile.toURI().toURL() }));
					break;
				case 3:
					standaloneBonusClassLoader = (URLClassLoader) pluginClassLoaderConstructor.newInstance(getProxy(), getDescription(), new URL[] { standaloneBonusJarFile.toURI().toURL() });
					break;
				case 4:
					standaloneBonusClassLoader = (URLClassLoader) pluginClassLoaderConstructor.newInstance(getProxy(), getDescription(), standaloneBonusJarFile, null);
					break;
				default:
					throw new IllegalStateException("The PluginClassloader uses an unexpected format for it's constructor!");
			}
			Object loaders = getField(getClass().getClassLoader().getClass(), "allLoaders").get(null);
			getMethod(loaders.getClass(), "add", Object.class).invoke(loaders, standaloneBonusClassLoader);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}