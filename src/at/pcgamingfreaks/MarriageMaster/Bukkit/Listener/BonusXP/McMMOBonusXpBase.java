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

import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.events.experience.McMMOPlayerXpGainEvent;
import com.gmail.nossr50.util.player.UserManager;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

abstract class McMMOBonusXpBase<XP_TYPE> extends BonusXpBase<McMMOPlayerXpGainEvent, XP_TYPE> implements Listener
{
	protected McMMOBonusXpBase(final @NotNull MarriageMaster plugin)
	{
		super(plugin, plugin.getConfiguration().getMcMMOBonusXpMultiplier(), plugin.getConfiguration().isMcMMOBonusXPSplitEnabled());
	}

	protected void onGainXp(final @NotNull McMMOPlayerXpGainEvent event, final @NotNull XP_TYPE type)
	{
		process(event, event.getPlayer(), event.getRawXpGained(), type);
	}

	protected abstract void addXp(McMMOPlayer player, float xp, XP_TYPE type);

	@Override
	protected void setEventExp(McMMOPlayerXpGainEvent event, double xp, XP_TYPE xpType, MarriagePlayer player, Marriage marriage)
	{
		event.setRawXpGained((float) xp);
	}

	@Override
	protected void splitWithPartner(McMMOPlayerXpGainEvent event, Player partner, double xp, XP_TYPE type, MarriagePlayer player, Marriage marriage)
	{
		addXp(UserManager.getPlayer(partner), (float) xp, type);
	}
}