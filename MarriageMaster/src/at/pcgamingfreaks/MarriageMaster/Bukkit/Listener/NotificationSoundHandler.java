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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Listener;

import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.DivorcedEvent;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.MarriedEvent;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class NotificationSoundHandler implements Listener
{
	private final Sound marriedNotificationSound, divorceNotificationSound;

	public static boolean enableSound(final @NotNull MarriageMaster plugin)
	{
		return plugin.getConfiguration().getMarriedNotificationSound() != null || plugin.getConfiguration().getDivorcedNotificationSound() != null;
	}

	public NotificationSoundHandler(final @NotNull MarriageMaster plugin)
	{
		marriedNotificationSound = plugin.getConfiguration().getMarriedNotificationSound();
		divorceNotificationSound = plugin.getConfiguration().getDivorcedNotificationSound();
	}

	@EventHandler
	public void onPlayerMarried(MarriedEvent event)
	{
		if (marriedNotificationSound == null) return;
		for(Player player : Bukkit.getServer().getOnlinePlayers())
		{
			player.playSound(player.getLocation(), marriedNotificationSound, 1, 0);
		}
	}

	@EventHandler
	public void onPlayerDivorced(DivorcedEvent event)
	{
		if (divorceNotificationSound == null) return;
		for(Player player : Bukkit.getServer().getOnlinePlayers())
		{
			player.playSound(player.getLocation(), divorceNotificationSound, 1, 0);
		}
	}
}
