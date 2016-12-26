/*
 * Copyright (C) 2016 GeorgH93
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.MarriageMaster.Bukkit.Listener;

import at.pcgamingfreaks.MarriageMaster.Bukkit.API.AcceptPendingRequest;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class OpenRequestCloser implements Listener
{
	private MarriageMaster plugin;

	public OpenRequestCloser(MarriageMaster plugin)
	{
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onLeave(PlayerQuitEvent event)
	{
		MarriagePlayer player = plugin.getPlayerData(event.getPlayer());
		if(player.getOpenRequest() != null)
		{
			player.getOpenRequest().disconnect(player);
		}
		for(AcceptPendingRequest request : player.getRequestsToCancel())
		{
			request.disconnect(player);
		}
	}
}