package at.pcgamingfreaks.georgh.MarriageMaster.Listener;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import at.pcgamingfreaks.georgh.MarriageMaster.MarriageMaster;

public class Death implements Listener 
{
	private MarriageMaster marriageMaster;

	public Death(MarriageMaster marriageMaster) 
	{
		this.marriageMaster = marriageMaster;
	}

	@EventHandler
	public void onEntityDeath(EntityDeathEvent event)
    {
		if(this.marriageMaster.config.GetBonusXPEnabled())
		{
			if(event.getEntityType() != EntityType.PLAYER)
			{
				Player killer = event.getEntity().getKiller();
				if(killer != null)
				{
					String partner = this.marriageMaster.DB.GetPartner(killer.getName());
					if(partner != null)
					{
						Player otherPlayer = this.marriageMaster.getServer().getPlayer(partner);
						
						if(otherPlayer != null && otherPlayer.isOnline())
						{
							if(this.getRadius(killer, otherPlayer))
							{
								int amount = this.marriageMaster.config.GetBonusXPAmount();
								int droppedXp = event.getDroppedExp();
								
								int xp = (droppedXp / 2) * amount;
								
								otherPlayer.giveExp(xp);
								killer.giveExp(xp);
								
								event.setDroppedExp(0);
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
		
		if(pl.distance(opl) <= 10 && opl.distance(pl) <= 10)
		{
			return true;
		}
		
		return false;
	}
}
