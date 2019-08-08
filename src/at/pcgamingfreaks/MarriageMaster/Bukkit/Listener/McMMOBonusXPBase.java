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

import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;

import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.events.experience.McMMOPlayerXpGainEvent;
import com.gmail.nossr50.util.player.UserManager;

import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

abstract class McMMOBonusXPBase<XP_TYPE> implements Listener
{
	protected final MarriageMaster plugin;
	protected final double range;
	protected final float multiplier;
	protected final boolean split;

	protected McMMOBonusXPBase(final @NotNull MarriageMaster marriagemaster)
	{
		plugin = marriagemaster;
		range = plugin.getConfiguration().getRangeSquared("BonusXP");
		split = plugin.getConfiguration().isMcMMOBonusXPSplitEnabled();
		multiplier = plugin.getConfiguration().getMcMMOBonusXpMultiplier() * (split ? 0.5f : 1.0f);
	}

	protected void onGainXp(final @NotNull McMMOPlayerXpGainEvent event, final @NotNull XP_TYPE type)
	{
		MarriagePlayer player = plugin.getPlayerData(event.getPlayer());
		Marriage marriage = player.getNearestPartnerMarriageData();
		if(marriage != null)
		{
			MarriagePlayer partner = marriage.getPartner(player);
			if(partner != null && partner.isOnline() && marriage.inRangeSquared(range))
			{
				float xp = event.getRawXpGained() * multiplier;
				event.setRawXpGained(xp);
				if(split)
				{
					addXp(UserManager.getPlayer(partner.getPlayerOnline()), xp, type);
				}
			}
		}
	}

	protected abstract void addXp(McMMOPlayer player, float xp, XP_TYPE type);
}