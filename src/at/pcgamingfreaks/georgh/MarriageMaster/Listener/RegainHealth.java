package at.pcgamingfreaks.georgh.MarriageMaster.Listener;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;

import at.pcgamingfreaks.georgh.MarriageMaster.MarriageMaster;

public class RegainHealth implements Listener
{
	private MarriageMaster marriageMaster;

	public RegainHealth(MarriageMaster marriageMaster) 
	{
		this.marriageMaster = marriageMaster;
	}

	@EventHandler
	public void onHeal(EntityRegainHealthEvent event) 
	{
		Player player = null;
		
		if (event.getEntity() instanceof Player)
		{
			player = (Player) event.getEntity();
		}
		
		if(player != null)
		{
			if(this.marriageMaster.config.GetHealthRegainEnabled())
			{
				int amount = this.marriageMaster.config.GetHealthRegainAmount();
				
				String partner = this.marriageMaster.DB.GetPartner(player.getName());
				if(partner != null)
				{
					Player otherPlayer = this.marriageMaster.getServer().getPlayer(partner);
					
					if(otherPlayer != null)
					{
						if(otherPlayer.isOnline())
						{
							if(this.getRadius(player, otherPlayer))
							{
								event.setAmount((double)amount);
							}
						}
					}
				}
			}
		}
	}
	
	private boolean getRadius(Player player, Player otherPlayer) 
	{
		Location pl = player.getLocation();
		Location opl = otherPlayer.getLocation();
		
		if(pl.distance(opl) <= 2 && opl.distance(pl) <= 2)
		{
			return true;
		}
		
		return false;
	}
}
