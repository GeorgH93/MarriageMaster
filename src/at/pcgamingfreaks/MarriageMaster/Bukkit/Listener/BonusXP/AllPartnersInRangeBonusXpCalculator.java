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

import java.util.ArrayList;
import java.util.List;

public final class AllPartnersInRangeBonusXpCalculator<EVENT, XP_TYPE> implements IBonusXpCalculator<EVENT, XP_TYPE>
{
	private final MarriageMaster plugin;
	private final double range, multiplier;
	private final IBonusXpListener<EVENT, XP_TYPE> eventListener;

	protected AllPartnersInRangeBonusXpCalculator(MarriageMaster plugin, double multiplier, IBonusXpListener<EVENT, XP_TYPE> eventListener)
	{
		this.plugin = plugin;
		this.multiplier = multiplier;
		this.eventListener = eventListener;
		range = plugin.getConfiguration().getRangeSquared("BonusXP");
	}

	@Override
	public void process(final EVENT event, final Player eventPlayer, double xp, final XP_TYPE xpType)
	{
		MarriagePlayer player = plugin.getPlayerData(eventPlayer);
		List<Marriage> marriages = new ArrayList<>();
		for(Marriage marriage : player.getMultiMarriageData())
		{
			if(marriage.inRange(range))
			{
				marriages.add(marriage);
			}
		}
		if(marriages.size() > 0)
		{
			xp *= multiplier / (marriages.size() + 1);
			eventListener.setEventExp(event, xp, xpType, player, marriages.get(0));
			for(Marriage marriage : marriages)
			{
				//noinspection ConstantConditions
				eventListener.splitWithPartner(event, marriage.getPartner(player).getPlayerOnline(), xp, xpType, player, marriage);
			}
		}
	}
}