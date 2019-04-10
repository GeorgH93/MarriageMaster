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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Listener;

import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.MarriedPlayerJoinEvent;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.MarriedPlayerQuitEvent;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.LinkedList;
import java.util.List;

public class JoinLeaveInfo implements Listener
{
	private final MarriageMaster plugin;
	private final Message messageOnline, messageOffline, messageNowOnline, messageNowOffline, messageAllOffline, messageOnlineMulti;
	private final String multiOnlineFormat, multiOnlineSeparator;
	private final long delay;

	public JoinLeaveInfo(MarriageMaster plugin)
	{
		this.plugin = plugin;

		delay = plugin.getConfiguration().getJoinInfoDelay();

		messageOnline        = plugin.getLanguage().getMessage("Ingame.JoinLeaveInfo.PartnerOnline");
		messageOffline       = plugin.getLanguage().getMessage("Ingame.JoinLeaveInfo.PartnerOffline");
		messageAllOffline    = plugin.getLanguage().getMessage("Ingame.JoinLeaveInfo.AllPartnersOffline");
		messageOnlineMulti   = plugin.getLanguage().getMessage("Ingame.JoinLeaveInfo.PartnerOnlineMulti");
		messageNowOnline     = plugin.getLanguage().getMessage("Ingame.JoinLeaveInfo.PartnerNowOnline") .replaceAll("\\{Name}", "%1\\$s").replaceAll("\\{DisplayName}", "%2\\$s");
		messageNowOffline    = plugin.getLanguage().getMessage("Ingame.JoinLeaveInfo.PartnerNowOffline").replaceAll("\\{Name}", "%1\\$s").replaceAll("\\{DisplayName}", "%2\\$s");
		multiOnlineFormat    = plugin.getLanguage().getTranslated("Ingame.JoinLeaveInfo.MultiOnlineFormat").replaceAll("\\{Name}", "%1\\$s").replaceAll("\\{DisplayName}", "%2\\$s");
		multiOnlineSeparator = plugin.getLanguage().getTranslated("Ingame.JoinLeaveInfo.MultiOnlineSeparator");
	}

	@EventHandler
	public void onJoin(MarriedPlayerJoinEvent event)
	{
		MarriagePlayer player = event.getPlayer();
		// Now online info
		for(MarriagePlayer partner : player.getPartners())
		{
			if(partner.isOnline())
			{
				partner.send(messageNowOnline, event.getPlayer().getName(), event.getPlayer().getDisplayName());
			}
		}
		// Online partners info
		plugin.getServer().getScheduler().runTaskLater(plugin, (player.getPartners().size() == 1) ? new OnePartnerRunnable(player) : new MultiPartnerRunnable(player), delay);
	}

	@EventHandler
	public void onLeave(MarriedPlayerQuitEvent event)
	{
		MarriagePlayer player = event.getPlayer();
		for(MarriagePlayer partner : player.getPartners())
		{
			if(partner.isOnline())
			{
				partner.send(messageNowOffline, event.getPlayer().getName(), event.getPlayer().getDisplayName());
			}
		}
	}

	private class OnePartnerRunnable implements Runnable
	{
		private MarriagePlayer player;

		public OnePartnerRunnable(MarriagePlayer player)
		{
			this.player = player;
		}

		@Override
		public void run()
		{
			if(player.isOnline())
			{
				MarriagePlayer partner = player.getPartner();
				if(partner != null)
				{
					player.send((partner.isOnline()) ? messageOnline : messageOffline);
				}
			}
		}
	}

	private class MultiPartnerRunnable implements Runnable
	{
		private MarriagePlayer player;

		public MultiPartnerRunnable(MarriagePlayer player)
		{
			this.player = player;
		}

		@Override
		public void run()
		{
			if(player.isOnline())
			{
				List<MarriagePlayer> onlinePartners = new LinkedList<>();
				for(MarriagePlayer partner : player.getPartners())
				{
					if(partner.isOnline())
					{
						onlinePartners.add(partner);
					}
				}
				if(onlinePartners.isEmpty())
				{
					player.send(messageAllOffline);
				}
				else
				{
					StringBuilder stringBuilder = new StringBuilder("");
					for(MarriagePlayer p : onlinePartners)
					{
						if(stringBuilder.length() > 0)
						{
							stringBuilder.append(multiOnlineSeparator);
						}
						stringBuilder.append(String.format(multiOnlineFormat, p.getName(), p.getDisplayName()));
					}
					player.send(messageOnlineMulti, stringBuilder.toString());
				}
			}
		}
	}
}