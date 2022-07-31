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

package at.pcgamingfreaks.MarriageMaster.Bungee;

import at.pcgamingfreaks.Bungee.Message.Message;
import at.pcgamingfreaks.MarriageMaster.Bungee.Database.Language;

import lombok.Getter;

public class CommonMessages
{
	private CommonMessages() { /* Prevent class instantiation */ }

	@Getter private static Message messageNoPermission, messageNotMarried, messagePartnerOffline, messageNotFromConsole, messageTargetPartnerNotFound, messagePlayerNotMarried, messagePlayersNotMarried;

	@Getter private static String helpPartnerNameVariable;

	public static void loadCommonMessages(Language language)
	{
		final String playerNamePlaceholder = "PlayerName";

		helpPartnerNameVariable      = language.get("Commands.PartnerNameVariable");
		messageNotFromConsole        = language.getMessage("NotFromConsole");
		messageNoPermission          = language.getMessage("Ingame.NoPermission");
		messageNotMarried            = language.getMessage("Ingame.NotMarried");
		messagePartnerOffline        = language.getMessage("Ingame.PartnerOffline");
		messagePlayerNotMarried      = language.getMessage("Ingame.PlayerNotMarried").placeholder(playerNamePlaceholder);
		messagePlayersNotMarried     = language.getMessage("Ingame.PlayersNotMarried");
		messageTargetPartnerNotFound = language.getMessage("Ingame.TargetPartnerNotFound");
	}
}