/*
 * Copyright (C) 2016 GeorgH93
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.MarriageMaster.Bukkit.Listener;

import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.PrefixSuffixFormatter;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatPrefixSuffix implements Listener, PrefixSuffixFormatter
{
	private static final String HEART = "\u2764" + ChatColor.WHITE, HEART_RED = ChatColor.RED + HEART, HEART_GRAY = ChatColor.GRAY + HEART;
	private static final char[] CHAT_COLORS = new char[]{ '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	private final MarriageMaster plugin;
	private final String prefix, suffix;
	private final boolean useStatusHeart, useMagicHeart, prefixOnLineBeginning;

	public ChatPrefixSuffix(MarriageMaster marriagemaster)
	{
		plugin = marriagemaster;
		if(plugin.getConfiguration().isPrefixEnabled() && plugin.getConfiguration().getPrefix() != null)
		{
			prefix = plugin.getConfiguration().getPrefix().replaceAll("\\{Surname}", "%1\\$s").replaceAll("\\{PartnerName}", "%2\\$s").replaceAll("\\{PartnerDisplayName}", "%3\\$s")
					.replaceAll("\\{StatusHeart}", "%4\\$s").replaceAll("\\{MagicHeart}", "%5\\$s");
			useStatusHeart = plugin.getConfiguration().getPrefix().contains("{StatusHeart}");
			useMagicHeart  = plugin.getConfiguration().getPrefix().contains("{MagicHeart}");
			prefixOnLineBeginning = plugin.getConfiguration().isPrefixOnLineBeginning();
		}
		else
		{
			prefix = null;
			useStatusHeart = false;
			useMagicHeart  = false;
			prefixOnLineBeginning = false;
		}
		if(plugin.getConfiguration().isSuffixEnabled() && plugin.getConfiguration().getSuffix() != null)
		{
			suffix = plugin.getConfiguration().getSuffix().replaceAll("\\{Surname}", "%1\\$s").replaceAll("\\{PartnerName}", "%2\\$s").replaceAll("\\{PartnerDisplayName}", "%3\\$s");
		}
		else suffix = null;
		if(plugin.getConfiguration().isPrefixEnabled() || plugin.getConfiguration().isSuffixEnabled()) plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@Override
	public String formatPrefix(Marriage marriage, MarriagePlayer partner)
	{
		if(prefix != null)
		{
			return String.format(prefix, marriage.getSurname(), partner.getName(), partner.getDisplayName(), HEART_RED, ((useMagicHeart) ? (ChatColor.COLOR_CHAR + CHAT_COLORS[marriage.hashCode() & 15] + HEART) : ""));
		}
		return "";
	}

	@Override
	public String formatSuffix(Marriage marriage, MarriagePlayer partner)
	{
		if(suffix != null)
		{
			return String.format(suffix, marriage.getSurname(), partner.getName(), partner.getDisplayName());
		}
		return "";
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerChat(AsyncPlayerChatEvent event)
	{
		String format = event.getFormat();
		boolean changed = false;
		MarriagePlayer player = plugin.getPlayerData(event.getPlayer());
		if(player.isMarried())
		{
			Marriage marriage = player.getMarriageData();
			//noinspection ConstantConditions
			MarriagePlayer partner = marriage.getPartner(player);
			String p = formatPrefix(marriage, partner), s = formatSuffix(marriage, partner);
			changed = !p.equals("") || !s.equals("");
			if(changed)
			{
				if(prefixOnLineBeginning)
				{
					format = p + ' ' + format.replace("%1$s", "%1$s " + s);
				}
				else
				{
					format = format.replace("%1$s", p + " %1$s " + s);
				}
			}
		}
		else if(useStatusHeart)
		{
			if(prefixOnLineBeginning)
			{
				format = HEART_GRAY + ' ' + format;
			}
			else
			{
				format = format.replace("%1$s", HEART_GRAY + " %1$s");
			}
			changed = true;
		}
		if(changed)
		{
			event.setFormat(format);
		}
	}
}