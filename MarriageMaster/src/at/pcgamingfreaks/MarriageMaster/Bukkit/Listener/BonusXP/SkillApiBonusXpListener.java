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

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Locale;

public class SkillApiBonusXpListener implements Listener, IBonusXpListener<PlayerExperienceGainEvent, Object>
{
	private final Method setExpMethod;
	private final IBonusXpCalculator<PlayerExperienceGainEvent, Object> calculator;
	private final HashSet<ExpSource> blockedSources = new HashSet<>();

	public SkillApiBonusXpListener(MarriageMaster plugin)
	{
		if(plugin.getConfiguration().isSkillApiBonusXPSplitWithAllEnabled())
			calculator = new AllPartnersInRangeBonusXpCalculator<>(plugin, plugin.getConfiguration().getSkillApiBonusXpMultiplier(), this);
		else
			calculator = new NearestPartnerBonusXpCalculator<>(plugin, plugin.getConfiguration().getSkillApiBonusXpMultiplier(), plugin.getConfiguration().isSkillApiBonusXPSplitEnabled(), this);
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

		//region try to figure out if the used SkillAPI plugin is the premium or the free version
		Method setExpMethod = null;
		try
		{
			//noinspection JavaReflectionMemberAccess
			setExpMethod = PlayerExperienceGainEvent.class.getDeclaredMethod("setExp", double.class);
		}
		catch(NoSuchMethodException ignored) {}
		this.setExpMethod = setExpMethod;
		//endregion

		plugin.getLogger().info(ConsoleColor.GREEN + "SkillAPI hooked" + ConsoleColor.RESET);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onGainExperience(PlayerExperienceGainEvent event)
	{
		if(blockedSources.contains(event.getSource())) return;
		calculator.process(event, event.getPlayerData().getPlayer(), event.getExp(), null);
	}

	@Override
	public void setEventExp(PlayerExperienceGainEvent event, double xp, Object o, MarriagePlayer player, Marriage marriage)
	{
		if(setExpMethod != null)
		{
			try
			{
				setExpMethod.invoke(event, xp);
			}
			catch(Exception e) { e.printStackTrace(); }
		}
		else event.setExp((int) Math.round(xp));
	}

	@Override
	public void splitWithPartner(PlayerExperienceGainEvent event, Player partner, double xp, Object o, MarriagePlayer player, Marriage marriage)
	{
		SkillAPI.getPlayerData(partner).giveExp((int) Math.round(xp), ExpSource.COMMAND);
	}
}