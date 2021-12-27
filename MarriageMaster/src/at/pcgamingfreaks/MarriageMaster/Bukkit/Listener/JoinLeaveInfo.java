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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Listener;

import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.MarriedPlayerJoinEvent;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.MarriedPlayerQuitEvent;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Listener.JoinLeaveInfoBase;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class JoinLeaveInfo extends JoinLeaveInfoBase implements Listener
{
	private final MarriageMaster plugin;
	private final long delay;

	public JoinLeaveInfo(MarriageMaster plugin)
	{
		super(plugin.getLanguage());
		this.plugin = plugin;
		delay = plugin.getConfiguration().getJoinInfoDelay();
	}

	@EventHandler
	public void onJoin(MarriedPlayerJoinEvent event)
	{
		onJoin(event.getPlayer());
	}

	@EventHandler
	public void onLeave(MarriedPlayerQuitEvent event)
	{
		onLeave(event.getPlayer());
	}

	@Override
	protected void runTaskLater(@NotNull Runnable task)
	{
		plugin.getServer().getScheduler().runTaskLater(plugin, task, delay);
	}
}