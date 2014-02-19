package at.pcgamingfreaks.georgh.MarriageMaster.Commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import at.pcgamingfreaks.georgh.MarriageMaster.MarriageMaster;

public class Home 
{
	private MarriageMaster marriageMaster;

	public Home(MarriageMaster marriagemaster) 
	{
		marriageMaster = marriagemaster;
	}

	public void TP(Player player)
	{
		Location loc = marriageMaster.DB.GetMarryHome(player.getName());
		
		if(loc != null)
		{
			if(marriageMaster.config.GetEconomyStatus())
			{
				if(marriageMaster.economy.HomeTeleport(player, marriageMaster.config.GetEconomyHomeTp()))
				{
					TPHome(player, loc);
				}
			}
			else
			{
				TPHome(player, loc);
			}
		}
		else
		{
			player.sendMessage(ChatColor.GREEN + marriageMaster.lang.Get("Ingame.NoHome"));
		}
	}
	
	private void TPHome(Player player, Location loc)
	{
		player.teleport(loc);
		player.sendMessage(ChatColor.GREEN + marriageMaster.lang.Get("Ingame.HomeTP"));
	}

	public void SetHome(Player player)
	{
		Location home = player.getLocation();
		if(marriageMaster.config.GetEconomyStatus())
		{
			if(marriageMaster.economy.HomeTeleport(player, marriageMaster.config.GetEconomySetHome()))
			{
				SetMarryHome(player, home);
			}
		}
		else
		{
			SetMarryHome(player, home);
		}
	}
	
	private void SetMarryHome(Player player, Location home)
	{
		String otherPlayer = marriageMaster.DB.GetPartner(player.getName());
		marriageMaster.DB.SetMarriedHome(home, player.getName(), otherPlayer);
		player.sendMessage(ChatColor.GREEN + marriageMaster.lang.Get("Ingame.HomeSet"));
	}
}
