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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Listener.BonusXP;

import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Range;

import org.bukkit.entity.Player;

public final class NearestPartnerBonusXpCalculator<EVENT, XP_TYPE> implements IBonusXpCalculator<EVENT, XP_TYPE>
{
	private final MarriageMaster plugin;
	private final double range, multiplier;
	private final boolean split;
	private final IBonusXpListener<EVENT, XP_TYPE> eventListener;

	protected NearestPartnerBonusXpCalculator(MarriageMaster plugin, double multiplier, boolean split, IBonusXpListener<EVENT, XP_TYPE> eventListener)
	{
		this.plugin = plugin;
		this.split = split;
		this.multiplier = multiplier * (split ? 0.5f : 1.0f);
		this.eventListener = eventListener;
		range = plugin.getConfiguration().getRangeSquared(Range.BonusXP);
	}

	@Override
	public void process(EVENT event, Player eventPlayer, double xp, XP_TYPE xpType)
	{
		MarriagePlayer player = plugin.getPlayerData(eventPlayer);
		Marriage marriage = player.getNearestPartnerMarriageData();
		if(marriage != null)
		{
			MarriagePlayer partner = marriage.getPartner(player);
			if(partner != null && partner.isOnline() && marriage.inRangeSquared(range))
			{
				xp *= multiplier;
				eventListener.setEventExp(event, xp, xpType, player, marriage);
				if(split)
				{
					eventListener.splitWithPartner(event, partner.getPlayerOnline(), xp, xpType, player, marriage);
				}
			}
		}
	}
}