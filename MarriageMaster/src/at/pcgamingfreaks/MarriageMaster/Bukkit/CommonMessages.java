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

package at.pcgamingfreaks.MarriageMaster.Bukkit;

import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Database.Language;

import lombok.Getter;

public class CommonMessages
{
	private CommonMessages() { /* Prevent class instantiation */ }

	@Getter private static String helpPartnerNameVariable, helpPlayerNameVariable;
	@Getter private static Message messageNotANumber, messageNoPermission, messageNotFromConsole, messageNotMarried, messagePartnerOffline, messagePartnerNotInRange, messageTargetPartnerNotFound;
	@Getter private static Message messagePlayerNotFound, messagePlayerNotMarried, messagePlayerNotOnline, messagePlayersNotMarried, messageMoved, messageDontMove, messageMarriageNotExact;

	public static void loadCommonMessages(Language language)
	{
		final String playerNamePlaceholder = "PlayerName";

		helpPlayerNameVariable       = language.get("Commands.PlayerNameVariable");
		helpPartnerNameVariable      = language.get("Commands.PartnerNameVariable");
		messageNotFromConsole        = language.getMessage("NotFromConsole");
		messageNotANumber            = language.getMessage("Ingame.NaN");
		messageNoPermission          = language.getMessage("Ingame.NoPermission");
		messageNotMarried            = language.getMessage("Ingame.NotMarried");
		messagePartnerOffline        = language.getMessage("Ingame.PartnerOffline");
		messagePartnerNotInRange     = language.getMessage("Ingame.PartnerNotInRange");
		messagePlayerNotFound        = language.getMessage("Ingame.PlayerNotFound").placeholder(playerNamePlaceholder);
		messagePlayerNotMarried      = language.getMessage("Ingame.PlayerNotMarried").placeholder(playerNamePlaceholder);
		messagePlayerNotOnline       = language.getMessage("Ingame.PlayerNotOnline").placeholder(playerNamePlaceholder);
		messagePlayersNotMarried     = language.getMessage("Ingame.PlayersNotMarried");
		messageMoved                 = language.getMessage("Ingame.TP.Moved");
		messageDontMove              = language.getMessage("Ingame.TP.DontMove").placeholder("Time");
		messageMarriageNotExact      = language.getMessage("Ingame.MarriageNotExact");
		messageTargetPartnerNotFound = language.getMessage("Ingame.TargetPartnerNotFound");
	}
}