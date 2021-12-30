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

package at.pcgamingfreaks.MarriageMaster.Listener;

import at.pcgamingfreaks.MarriageMaster.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Database.ILanguage;
import at.pcgamingfreaks.Message.Message;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

@SuppressWarnings("unchecked")
public abstract class JoinLeaveInfoBase
{
	private final Message messageOnline, messageOffline, messageNowOnline, messageNowOffline, messageAllOffline, messageOnlineMulti;
	private final String multiOnlineFormat, multiOnlineSeparator;
	
	protected JoinLeaveInfoBase(final @NotNull ILanguage language)
	{
		messageOnline        = language.getMessage("Ingame.JoinLeaveInfo.PartnerOnline");
		messageOffline       = language.getMessage("Ingame.JoinLeaveInfo.PartnerOffline");
		messageAllOffline    = language.getMessage("Ingame.JoinLeaveInfo.AllPartnersOffline");
		messageOnlineMulti   = language.getMessage("Ingame.JoinLeaveInfo.PartnerOnlineMulti").replaceAll("\\{OnlinePartners}", "%1\\$s");
		messageNowOnline     = language.getMessage("Ingame.JoinLeaveInfo.PartnerNowOnline").replaceAll("\\{Name}", "%1\\$s").replaceAll("\\{DisplayName}", "%2\\$s");
		messageNowOffline    = language.getMessage("Ingame.JoinLeaveInfo.PartnerNowOffline").replaceAll("\\{Name}", "%1\\$s").replaceAll("\\{DisplayName}", "%2\\$s");
		multiOnlineFormat    = language.getTranslated("Ingame.JoinLeaveInfo.MultiOnlineFormat").replace("{Name}", "%1$s").replace("{DisplayName}", "%2$s");
		multiOnlineSeparator = language.getTranslated("Ingame.JoinLeaveInfo.MultiOnlineSeparator");
	}

	protected abstract void runTaskLater(final @NotNull Runnable task);

	public void onJoin(MarriagePlayer player)
	{
		// Now online info
		for(Object partner : player.getOnlinePartners())
		{
			MarriagePlayer mpPartner = (MarriagePlayer) partner;
			if(mpPartner.canSee(player)) mpPartner.send(messageNowOnline, player.getName(), player.getDisplayName());
		}
		// Online partners info
		runTaskLater((player.getPartners().size() == 1) ? new OnePartnerRunnable(player) : new MultiPartnerRunnable(player));
	}

	public void onLeave(MarriagePlayer player)
	{
		for(Object partner : player.getOnlinePartners())
		{
			MarriagePlayer mpPartner = (MarriagePlayer) partner;
			if(mpPartner.canSee(player)) mpPartner.send(messageNowOffline, player.getName(), player.getDisplayName());
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
					player.send((partner.isOnline() && player.canSee(partner)) ? messageOnline : messageOffline);
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
				Collection<? extends MarriagePlayer> onlinePartners = player.getOnlinePartners();
				StringBuilder stringBuilder = new StringBuilder();
				String separator = "";
				for(MarriagePlayer p : onlinePartners)
				{
					if(!player.canSee(p)) continue;
					stringBuilder.append(separator);
					separator = multiOnlineSeparator;
					stringBuilder.append(String.format(multiOnlineFormat, p.getName(), p.getDisplayName()));
				}
				if(stringBuilder.length() == 0)
				{
					player.send(messageAllOffline);
				}
				else
				{
					player.send(messageOnlineMulti, stringBuilder.toString());
				}
			}
		}
	}
}