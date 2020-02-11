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
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;

import com.gmail.nossr50.datatypes.experience.XPGainReason;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.events.experience.McMMOPlayerXpGainEvent;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class McMMOBonusXpListener extends McMMOBonusXpBaseListener<PrimarySkillType> implements Listener
{
	private final Set<XPGainReason> blockedSources = new HashSet<>();
	private final Set<PrimarySkillType> blockedSkills = new HashSet<>();

	public McMMOBonusXpListener(final @NotNull MarriageMaster plugin)
	{
		super(plugin);
		plugin.getConfiguration().getMcMMOBonusXpBlockedSkills().forEach(skill -> blockedSkills.add(PrimarySkillType.valueOf(skill)));
		plugin.getConfiguration().getMcMMOBonusXpBlockedSources().forEach(source -> blockedSources.add(XPGainReason.valueOf(source)));
		plugin.getLogger().info(ConsoleColor.GREEN + "mcMMO v2 hooked" + ConsoleColor.RESET);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onGainXp(McMMOPlayerXpGainEvent event)
	{
		if(blockedSources.contains(event.getXpGainReason()) || blockedSkills.contains(event.getSkill())) return;
		onGainXp(event, event.getSkill());
	}

	@Override
	protected void addXp(final @NotNull McMMOPlayer player, float xp, final @NotNull PrimarySkillType primarySkillType)
	{
		player.addXp(primarySkillType, xp);
	}
}