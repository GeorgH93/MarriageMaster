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

import at.pcgamingfreaks.ConsoleColor;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.enums.ExpSource;
import com.sucy.skill.api.event.PlayerExperienceGainEvent;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashSet;
import java.util.Locale;

public class SkillApiBonusXp extends BonusXpBase<PlayerExperienceGainEvent, Object> implements Listener
{
	private final HashSet<ExpSource> blockedSources = new HashSet<>();

	public SkillApiBonusXp(MarriageMaster plugin)
	{
		super(plugin, plugin.getConfiguration().getSkillApiBonusXpMultiplier(), plugin.getConfiguration().isSkillApiBonusXPSplitEnabled());
		blockedSources.add(ExpSource.COMMAND);
		for(String source : plugin.getConfiguration().getSkillApiBonusXpBlockedSources())
		{
			try
			{
				blockedSources.add(ExpSource.valueOf(source.toUpperCase(Locale.ENGLISH)));
			}
			catch(IllegalArgumentException ignored)
			{
				plugin.getLogger().info("Unknown SkillAPI XP Source: " + source);
			}
		}
		plugin.getLogger().info(ConsoleColor.GREEN + "SkillAPI hooked" + ConsoleColor.RESET);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onGainExperience(PlayerExperienceGainEvent event)
	{
		if(blockedSources.contains(event.getSource())) return;
		process(event, event.getPlayerData().getPlayer(), event.getExp(), null);
	}


	@Override
	protected void setEventExp(PlayerExperienceGainEvent event, double xp, Object o, MarriagePlayer player, Marriage marriage)
	{
		event.setExp((int) Math.round(xp));
	}

	@Override
	protected void splitWithPartner(PlayerExperienceGainEvent event, Player partner, double xp, Object o, MarriagePlayer player, Marriage marriage)
	{
		SkillAPI.getPlayerData(partner).giveExp((int) Math.round(xp), ExpSource.COMMAND);
	}
}