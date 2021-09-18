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
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.MarriageMaster.Bukkit.Commands;

import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.Bukkit.Message.MessageBuilder;
import at.pcgamingfreaks.Command.HelpData;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarryCommand;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Permissions;
import at.pcgamingfreaks.Message.MessageClickEvent;
import at.pcgamingfreaks.Message.MessageColor;
import at.pcgamingfreaks.Message.MessageFormat;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.SneakyThrows;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class DebugCommand extends MarryCommand
{
	private final MarriageMaster plugin;
	private final Message messageDone, messageStart;
	private BufferedWriter writer = null;

	public DebugCommand(final @NotNull MarriageMaster plugin)
	{
		super(plugin, "debug", "Just for debug reasons", Permissions.RELOAD, true);
		this.plugin = plugin;

		MessageBuilder builder = new MessageBuilder("Please do not interact with your game for the next minute!", MessageColor.GOLD);
		builder.appendNewLine().append("The plugin will now collect data about your server and plugins.").appendNewLine();
		builder.append("This will involve opening inventory's.").appendNewLine();
		builder.append("Please do not interact with your game till this is over!", MessageColor.RED, MessageFormat.BOLD);
		messageStart = builder.getMessage();

		builder = new MessageBuilder("All data has been collected!", MessageColor.GREEN, MessageFormat.BOLD).appendNewLine();
		builder.append("You can now interact with your game again.").appendNewLine();
		builder.append("The collected data can be found in your plugins directory inside the 'debug.txt' file.").appendNewLine();
		builder.append("Please upload this fiel to ");
		builder.append("https://pastebin.com/", MessageColor.YELLOW, MessageFormat.UNDERLINE).onClick(MessageClickEvent.ClickEventAction.OPEN_URL, "https://pastebin.com/");
		builder.append(" and send the link to the developer.");
		messageDone = builder.getMessage();
	}

	@Override
	@SneakyThrows
	public void execute(@NotNull CommandSender commandSender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		if(writer != null) return;
		Player sender = (Player) commandSender;
		messageStart.send(sender);

		File debugFile = new File(plugin.getDataFolder(), "debug.txt");
		if(debugFile.exists()) //noinspection ResultOfMethodCallIgnored
			debugFile.delete();
		writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(debugFile), StandardCharsets.UTF_8));

		writer.append(plugin.getDescription().getName()).append(" Version: ").append(plugin.getDescription().getVersion());
		writer.append("\nServer: ").append(Bukkit.getServer().getBukkitVersion()).append(" (").append(Bukkit.getServer().getVersion()).append(")");
		writer.append("\nJava: ").append(System.getProperty("java.version"));
		writer.append("\n\nPlugins:\n");
		for(Plugin p : Bukkit.getServer().getPluginManager().getPlugins())
		{
			writer.append(p.getName()).append(' ').append(p.getDescription().getVersion()).append('\n');
		}
		writer.append("\nPlugin Config:\n");
		try(BufferedReader configReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(plugin.getDataFolder(), "config.yml")), StandardCharsets.UTF_8)))
		{
			String line;
			while((line = configReader.readLine()) != null)
			{
				if(line.isEmpty()) continue;
				if(line.contains("Host") || line.contains("Password") || line.contains("User")) line = line.replaceAll("^(\\s+\\w+):.*$", "$1: ********");
				writer.append(line).append('\n');
			}
		}
		writer.append("\n\n\nSelf-test results:\n");
		plugin.getPlaceholderManager().testPlaceholders(writer);

		writer.flush();
		writer.close();
		writer = null;
		messageDone.send(sender);
	}

	@Override
	public List<String> tabComplete(@NotNull CommandSender commandSender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		return null;
	}

	@Override
	public @Nullable List<HelpData> getHelp(@NotNull CommandSender requester)
	{
		return null;
	}

}