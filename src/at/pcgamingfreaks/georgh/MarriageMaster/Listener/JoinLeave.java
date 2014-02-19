package at.pcgamingfreaks.georgh.MarriageMaster.Listener;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import at.pcgamingfreaks.georgh.MarriageMaster.MarriageMaster;

public class JoinLeave implements Listener 
{
	private MarriageMaster marriageMaster;

	public JoinLeave(MarriageMaster marriagemaster) 
	{
		marriageMaster = marriagemaster;
	}
	
	@EventHandler
	public void PlayerLoginEvent(PlayerJoinEvent event) 
	{
		String otherPlayer = marriageMaster.DB.GetPartner(event.getPlayer().getName());
		if(otherPlayer != null && !otherPlayer.isEmpty())
		{
			Player oPlayer = marriageMaster.getServer().getPlayer(otherPlayer);
			
			if(oPlayer != null)
			{
				event.getPlayer().sendMessage(ChatColor.GREEN + marriageMaster.lang.Get("Ingame.PartnerOnline"));
				oPlayer.sendMessage(ChatColor.GREEN + marriageMaster.lang.Get("Ingame.PartnerNowOnline"));
			}
			else
			{
				event.getPlayer().sendMessage(ChatColor.GREEN + marriageMaster.lang.Get("Ingame.PartnerOffline"));
			}
		}
	}

	@EventHandler
	public void PlayerLeaveEvent(PlayerQuitEvent event)
	{
		String otherPlayer = marriageMaster.DB.GetPartner(event.getPlayer().getName());
		
		if(otherPlayer != null)
		{
			Player oPlayer = marriageMaster.getServer().getPlayer(otherPlayer);
			
			if(oPlayer != null)
			{
				oPlayer.sendMessage(ChatColor.GREEN + marriageMaster.lang.Get("Ingame.PartnerNowOffline"));
			}
		}
	}
}
