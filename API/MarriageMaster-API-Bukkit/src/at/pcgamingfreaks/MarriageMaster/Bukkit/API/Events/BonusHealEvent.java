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

package at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events;

import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;

import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Event is fired right before a player gets a healing bonus. All the checks are done. We just await for your approval.
 */
@SuppressWarnings("unused")
public class BonusHealEvent extends MarriageMasterCancellableEvent
{
	private final MarriagePlayer player;
	private final Marriage marriageData;
	private double amount;
	private final EntityRegainHealthEvent.RegainReason regainReason;

	/**
	 * @param player The player that gets a healing bonus.
	 * @param marriageData The marriage data of the player.
	 * @param amount The amount of health the entity will regain.
	 * @param regainReason The RegainReason detailing the reason for the entity regaining health.
	 */
	public BonusHealEvent(@NotNull MarriagePlayer player, @NotNull Marriage marriageData, final double amount, @NotNull final EntityRegainHealthEvent.RegainReason regainReason)
	{
		this.player = player;
		this.marriageData = marriageData;
		this.amount = amount;
		this.regainReason = regainReason;
	}

	/**
	 * Gets the player that gets a healing bonus.
	 *
	 * @return The player that gets a healing bonus.
	 */
	public @NotNull MarriagePlayer getPlayer()
	{
		return player;
	}

	/**
	 * Gets the marriage data of the player.
	 *
	 * @return The marriage data of the player
	 */
	public @NotNull Marriage getMarriageData()
	{
		return marriageData;
	}


	/**
	 * Gets the amount of regained health.
	 * This value already contains the bonus.
	 *
	 * @return The amount of health regained
	 */
	public double getAmount() {
		return amount;
	}

	/**
	 * Sets the amount of regained health.
	 *
	 * @param amount The amount of health the entity will regain.
	 */
	public void setAmount(double amount) {
		this.amount = amount;
	}

	/**
	 * Gets the reason for why the player is regaining health.
	 *
	 * @return A RegainReason detailing the reason for the entity regaining health.
	 */
	public @NotNull EntityRegainHealthEvent.RegainReason getRegainReason() {
		return regainReason;
	}


	// Bukkit handler stuff
	private static final HandlerList handlers = new HandlerList();

	@Override
	public @NotNull HandlerList getHandlers()
	{
		return getHandlerList();
	}

	public static HandlerList getHandlerList()
	{
		return handlers;
	}
}