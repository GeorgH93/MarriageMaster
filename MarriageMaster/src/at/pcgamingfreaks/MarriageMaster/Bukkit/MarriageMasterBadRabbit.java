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

package at.pcgamingfreaks.MarriageMaster.Bukkit;

import at.pcgamingfreaks.BadRabbit.Bukkit.BadRabbit;
import at.pcgamingfreaks.MarriageMaster.MagicValues;
import at.pcgamingfreaks.Version;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public class MarriageMasterBadRabbit extends BadRabbit
{
	private boolean standalone = true;

	@Override
	public void onLoad()
	{
		Plugin pcgfPluginLib = Bukkit.getPluginManager().getPlugin("PCGF_PluginLib");
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
	protected @NotNull JavaPlugin createInstance() throws Exception
	{
		JavaPlugin newPluginInstance = null;
		if(standalone)
		{
			Class<?> standaloneClass = Class.forName("at.pcgamingfreaks.MarriageMasterStandalone.Bukkit.MarriageMaster");
			newPluginInstance = (JavaPlugin) standaloneClass.newInstance();
		}
		else
		{
			newPluginInstance = new MarriageMaster();
		}
		return newPluginInstance;
	}

	void loadIMessageClasses()
	{
		try
		{
			File tempJarFile = File.createTempFile("IMessage", ".jar");
			try
			{
				Class<?> utilsClass = Class.forName("at.pcgamingfreaks.MarriageMasterStandalone.libs.at.pcgamingfreaks.Utils");
				Method extractMethod = utilsClass.getDeclaredMethod("extractFile", Class.class, Logger.class, String.class, File.class);
				extractMethod.invoke(null, this.getClass(), getLogger(), "IMessage.jar", tempJarFile);

				URLClassLoader loader = new URLClassLoader(new URL[] { tempJarFile.toURI().toURL() }, getClassLoader());
				Class<?> iMessage = loader.loadClass("at.pcgamingfreaks.Message.IMessage");
				Class<?> iMessageBukkit = loader.loadClass("at.pcgamingfreaks.Bukkit.Message.IMessage");

				Map<String, Class<?>> classes = (Map<String, Class<?>>) getField(getClassLoader().getClass(), "classes").get(getClassLoader());
				classes.put("at.pcgamingfreaks.Message.IMessage", iMessage);
				classes.put("at.pcgamingfreaks.Bukkit.Message.IMessage", iMessageBukkit);

				loader.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			if(!tempJarFile.delete()) {
				getLogger().warning("Failed to delete temp file '" + tempJarFile.getAbsolutePath() + "'.");
				tempJarFile.deleteOnExit();
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}