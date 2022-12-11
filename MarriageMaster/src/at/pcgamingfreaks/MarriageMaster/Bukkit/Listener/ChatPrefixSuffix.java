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

package at.pcgamingfreaks.MarriageMaster.Bukkit.Listener;

import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Formatter.IMarriageAndPartnerFormatter;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Formatter.PrefixSuffixFormatterImpl;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.MagicValues;
import at.pcgamingfreaks.Message.MessageColor;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.NotNull;

public class ChatPrefixSuffix implements Listener
{
	private static final String HEART = MagicValues.SYMBOL_HEART + MessageColor.WHITE, HEART_GRAY = MessageColor.GRAY + HEART + " ", STATUS_HEART_NM = HEART_GRAY + "%1$s";

	private final MarriageMaster plugin;
	private final IMarriageAndPartnerFormatter prefixFormatter, suffixFormatter;
	private final boolean useStatusHeartPrefix, useStatusHeartSuffix, prefixOnLineBeginning;

	public ChatPrefixSuffix(final @NotNull MarriageMaster plugin)
	{
		this.plugin = plugin;
		if(plugin.getConfiguration().isPrefixEnabled())
		{
			final String prefix = plugin.getConfiguration().getPrefix();
			useStatusHeartPrefix = prefix.contains("{StatusHeart}");
			prefixFormatter = PrefixSuffixFormatterImpl.produceFormatter(prefix.isEmpty() ? "" : prefix + " ");
			prefixOnLineBeginning = plugin.getConfiguration().isPrefixOnLineBeginning();
		}
		else
		{
			prefixFormatter = PrefixSuffixFormatterImpl.produceFormatter("");
			useStatusHeartPrefix = false;
			prefixOnLineBeginning = false;
		}
		String suffix = plugin.getConfiguration().isSuffixEnabled() ? plugin.getConfiguration().getSuffix() : "";
		if(!suffix.isEmpty()) suffix = " " + suffix;
		useStatusHeartSuffix = suffix.contains("{StatusHeart}");
		suffixFormatter = PrefixSuffixFormatterImpl.produceFormatter(suffix);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerChat(final AsyncPlayerChatEvent event)
	{
		final MarriagePlayer player = plugin.getPlayerData(event.getPlayer());
		if(player.isMarried())
		{
			handleMarriedPlayer(event, player);
		}
		else
		{
			handleSinglePlayer(event);
		}
	}

	private void handleMarriedPlayer(final @NotNull AsyncPlayerChatEvent event, final @NotNull MarriagePlayer player)
	{
		String format = event.getFormat();

		final Marriage marriage = player.getMarriageData();
		//noinspection ConstantConditions
		final MarriagePlayer partner = marriage.getPartner(player);
		//noinspection ConstantConditions
		String p = prefixFormatter.format(marriage, partner), s = suffixFormatter.format(marriage, partner);
		if(p.length() == 1) p = "";
		if(s.length() == 1) s = "";
		boolean changed = !p.isEmpty() || !s.isEmpty();
		if(prefixOnLineBeginning)
		{
			format = p + format.replace("%1$s", "%1$s" + s);
		}
		else
		{
			format = format.replace("%1$s", p + "%1$s" + s);
		}

		if(changed) event.setFormat(format);
	}

	private void handleSinglePlayer(final @NotNull AsyncPlayerChatEvent event)
	{
		String format = event.getFormat();
		boolean changed = false;
		if(useStatusHeartPrefix)
		{
			if(prefixOnLineBeginning)
			{
				format = HEART_GRAY + format;
			}
			else
			{
				format = format.replace("%1$s", STATUS_HEART_NM);
			}
			changed = true;
		}
		if(useStatusHeartSuffix)
		{
			if(prefixOnLineBeginning)
			{
				format = HEART_GRAY + format;
			}
			else
			{
				format = format.replace("%1$s", "%1$s " + HEART_GRAY);
			}
			changed = true;
		}
		if(changed) event.setFormat(format);
	}
}