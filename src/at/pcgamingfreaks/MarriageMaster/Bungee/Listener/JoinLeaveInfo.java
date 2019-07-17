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

package at.pcgamingfreaks.MarriageMaster.Bungee.Listener;

import at.pcgamingfreaks.MarriageMaster.Bungee.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bungee.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Listener.JoinLeaveInfoBase;

import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class JoinLeaveInfo extends JoinLeaveInfoBase implements Listener
{
	private final MarriageMaster plugin;
	private final long delay;

	public JoinLeaveInfo(MarriageMaster plugin)
	{
		super(plugin.getLanguage());
		this.plugin = plugin;
		delay = plugin.getConfig().getJoinInfoDelay();
	}

	@SuppressWarnings("unused")
	@EventHandler(priority= EventPriority.NORMAL)
	public void onLogin(PostLoginEvent event)
	{
		MarriagePlayer player = plugin.getPlayerData(event.getPlayer());
		if(player.isMarried())
		{
			onJoin(player);
		}
	}

	@SuppressWarnings("unused")
	@EventHandler(priority=EventPriority.NORMAL)
	public void onDisconnect(PlayerDisconnectEvent event)
	{
		MarriagePlayer player = plugin.getPlayerData(event.getPlayer());
		if(player.isMarried())
		{
			onLeave(player);
		}
	}

	@Override
	protected void runTaskLater(@NotNull Runnable task)
	{
		plugin.getProxy().getScheduler().schedule(plugin, task, delay, TimeUnit.SECONDS);
	}
}