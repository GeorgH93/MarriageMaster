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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Listener.BonusXP;

import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;

import org.bukkit.entity.Player;

public abstract class BonusXpBase<EVENT, XP_TYPE>
{
	protected final MarriageMaster plugin;
	protected final double range, multiplier;
	protected final boolean split;

	protected BonusXpBase(MarriageMaster plugin, double multiplier, boolean split)
	{
		this.plugin = plugin;
		this.split = split;
		this.multiplier = multiplier * (split ? 0.5f : 1.0f);
		range = plugin.getConfiguration().getRangeSquared("BonusXP");
	}

	protected void process(EVENT event, Player eventPlayer, double xp, XP_TYPE xpType)
	{
		MarriagePlayer player = plugin.getPlayerData(eventPlayer);
		Marriage marriage = player.getNearestPartnerMarriageData();
		if(marriage != null)
		{
			MarriagePlayer partner = marriage.getPartner(player);
			if(partner != null && partner.isOnline() && marriage.inRangeSquared(range))
			{
				xp *= multiplier;
				setEventExp(event, xp, xpType, player, marriage);
				if(split)
				{
					splitWithPartner(event, partner.getPlayerOnline(), xp, xpType, player, marriage);
				}
			}
		}
	}

	protected abstract void setEventExp(EVENT event, double xp, XP_TYPE xpType, MarriagePlayer player, Marriage marriage);

	protected abstract void splitWithPartner(EVENT event, Player partner, double xp, XP_TYPE xpType, MarriagePlayer player, Marriage marriage);
}