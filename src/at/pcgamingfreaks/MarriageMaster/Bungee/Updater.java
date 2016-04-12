/*
 *   Copyright (C) 2016 GeorgH93
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

import at.pcgamingfreaks.MarriageMaster.Updater.UpdateProviders.BukkitUpdateProvider;
import at.pcgamingfreaks.MarriageMaster.Updater.UpdateProviders.UpdateProvider;

import net.md_5.bungee.api.plugin.Plugin;

public class Updater extends at.pcgamingfreaks.MarriageMaster.Updater.Updater
{
	private final Plugin plugin;

	public Updater(Plugin plugin, boolean announceDownloadProgress, int bukkitPluginID)
	{
		this(plugin, announceDownloadProgress, new BukkitUpdateProvider(bukkitPluginID));
	}

	public Updater(Plugin plugin, boolean announceDownloadProgress, UpdateProvider updateProvider)
	{
		super(plugin.getDataFolder().getParentFile(), announceDownloadProgress, plugin.getLogger(), updateProvider, plugin.getDescription().getVersion(), plugin.getDescription().getFile().getName());
		this.plugin = plugin;
	}

	@Override
	protected void runSync(Runnable runnable)
	{
		runnable.run(); // BungeeCord runs everything async
	}

	@Override
	protected void runAsync(Runnable runnable)
	{
		plugin.getProxy().getScheduler().runAsync(plugin, runnable);
	}


	@Override
	protected String getAuthor()
	{
		return plugin.getDescription().getAuthor() != null ? plugin.getDescription().getAuthor() : "";
	}

	@Override
	public void waitForAsyncOperation() {} // We can't wait for the async operation to finish, BungeeCord doesn't allow us to start threads
}