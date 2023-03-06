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

package at.pcgamingfreaks.MarriageMaster.Paper;

import at.pcgamingfreaks.MarriageMaster.MagicValues;
import at.pcgamingfreaks.PCGF_PluginLibVersionDetection;
import at.pcgamingfreaks.Version;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.JarLibrary;
import lombok.SneakyThrows;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Logger;

@SuppressWarnings({ "UnstableApiUsage", "unused" })
public class MarriageMasterBootstrap implements PluginBootstrap, PluginLoader
{
	private static final String MAIN_CLASS_NORMAL = "at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster";
	private static final String MAIN_CLASS_STANDALONE = "at.pcgamingfreaks.MarriageMasterStandalone.Bukkit.MarriageMaster";

	@Override
	public void bootstrap(@NotNull PluginProviderContext context)
	{
	}

	@Override
	public @NotNull JavaPlugin createPlugin(@NotNull PluginProviderContext context)
	{
		try
		{
			if(checkPcgfPluginLib(context) && patchPluginMeta(context))
			{
				Class<?> normalClass = Class.forName(MAIN_CLASS_NORMAL);
				return (JavaPlugin) normalClass.newInstance();
			}
			else
			{
				Class<?> standaloneClass = Class.forName(MAIN_CLASS_STANDALONE);
				return (JavaPlugin) standaloneClass.newInstance();
			}
		}
		catch(Exception e)
		{
			throw new RuntimeException("Failed to create MarriageMaster plugin instance!", e);
		}
	}

	private boolean patchPluginMeta(final @NotNull PluginProviderContext context)
	{
		try
		{
			Class<?> pluginMetaClass = context.getConfiguration().getClass();
			Field mainField = pluginMetaClass.getDeclaredField("main");
			mainField.setAccessible(true);
			mainField.set(context.getConfiguration(), MAIN_CLASS_NORMAL);
			return true;
		}
		catch(Exception e)
		{
			try
			{
				context.getLogger().error("Failed to patch main class in PluginMeta! Falling back to Standalone mode!", e);
			}
			catch(Throwable ignored)
			{
				System.out.println("[MarriageMaster] Failed to patch main class in PluginMeta! Falling back to Standalone mode!");
				e.printStackTrace();
			}
		}
		return false;
	}

	private boolean checkPcgfPluginLib(final @NotNull PluginProviderContext context)
	{
		String version = PCGF_PluginLibVersionDetection.getVersionBukkit();
		if (version != null)
		{
			if (new Version(version).olderThan(new Version(MagicValues.MIN_PCGF_PLUGIN_LIB_VERSION)))
			{
				logInfo("PCGF-PluginLib to old! Switching to standalone mode!", context);
			}
			else
			{
				logInfo("PCGF-PluginLib installed. Switching to normal mode!", context);
				return true;
			}
		}
		else
		{
			logInfo("PCGF-PluginLib not installed. Switching to standalone mode!", context);
		}
		return false;
	}

	//TODO remove this stupid code once the paper API stabilizes to a point where once can expect at least the logger class ot not randomly change
	private void logInfo(final @NotNull String message, final @NotNull PluginProviderContext context)
	{
		try
		{
			context.getLogger().info(message);
		}
		catch(Throwable t)
		{
			System.out.println("[MarriageMaster] Failed to log message: " + message);
		}
	}

	@Override
	@SneakyThrows
	public void classloader(@NotNull PluginClasspathBuilder pluginClasspathBuilder)
	{
		try
		{
			String path = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
			if(Arrays.stream(new File(path).getParentFile().listFiles()).filter(f -> f.getName().toLowerCase(Locale.ROOT).contains("pcgf_pluginlib")).count() > 0) return;
		}
		catch(Exception ignored) {}
		if (PCGF_PluginLibVersionDetection.getVersionBukkit() != null) return; // Plugin lib is available, no need to load additional dependencies

		File tempJarFile = File.createTempFile("IMessage", ".jar");
		Class<?> utilsClass = Class.forName("at.pcgamingfreaks.MarriageMasterStandalone.libs.at.pcgamingfreaks.Utils");
		Method extractMethod = utilsClass.getDeclaredMethod("extractFile", Class.class, Logger.class, String.class, File.class);
		extractMethod.invoke(null, this.getClass(), Logger.getLogger(getClass().getSimpleName()), "IMessage.jar", tempJarFile);
		pluginClasspathBuilder.addLibrary(new JarLibrary(tempJarFile.toPath()));
		tempJarFile.deleteOnExit();
	}
}