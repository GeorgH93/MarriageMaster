/*
 *   Copyright (C) 2022 GeorgH93
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

import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.events.experience.McMMOPlayerXpGainEvent;
import com.gmail.nossr50.util.player.UserManager;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

abstract class McMMOBonusXpBaseListener<XP_TYPE> implements Listener, IBonusXpListener<McMMOPlayerXpGainEvent, XP_TYPE>
{
	protected final MarriageMaster plugin;
	private final IBonusXpCalculator<McMMOPlayerXpGainEvent, XP_TYPE> calculator;

	protected McMMOBonusXpBaseListener(final @NotNull MarriageMaster plugin)
	{
		this.plugin = plugin;
		if(plugin.getConfiguration().isMcMMOBonusXPSplitWithAllEnabled())
			calculator = new AllPartnersInRangeBonusXpCalculator<>(plugin, plugin.getConfiguration().getMcMMOBonusXpMultiplier(), this);
		else
			calculator = new NearestPartnerBonusXpCalculator<>(plugin, plugin.getConfiguration().getMcMMOBonusXpMultiplier(), plugin.getConfiguration().isMcMMOBonusXPSplitEnabled(), this);
	}

	protected void onGainXp(final @NotNull McMMOPlayerXpGainEvent event, final @NotNull XP_TYPE type)
	{
		calculator.process(event, event.getPlayer(), event.getRawXpGained(), type);
	}

	protected abstract void addXp(McMMOPlayer player, float xp, XP_TYPE type);

	@Override
	public void setEventExp(McMMOPlayerXpGainEvent event, double xp, XP_TYPE xpType, MarriagePlayer player, Marriage marriage)
	{
		event.setRawXpGained((float) xp);
	}

	@Override
	public void splitWithPartner(McMMOPlayerXpGainEvent event, Player partner, double xp, XP_TYPE type, MarriagePlayer player, Marriage marriage)
	{
		McMMOPlayer mcMMOPartner = UserManager.getPlayer(partner);
		if(mcMMOPartner == null) return;
		addXp(mcMMOPartner, (float) xp, type);
	}
}