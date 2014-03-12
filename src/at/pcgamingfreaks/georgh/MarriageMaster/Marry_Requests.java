package at.pcgamingfreaks.georgh.MarriageMaster;

import org.bukkit.entity.Player;

public class Marry_Requests
{
	public Player priest = null, p1 = null, p2 = null;
	private boolean p1a = false, p2a = false;
	
	public Marry_Requests(Player Priest, Player P1, Player P2)
	{
		priest = Priest;
		p1 = P1;
		p2 = P2;
	}
	
	public boolean Accept(Player p)
	{
		if(p == p1)
		{
			p1a = true;
		}
		else if(p == p2)
		{
			p2a = true;
		}
		else
		{
			return false;
		}
		return true;
	}
	
	public boolean HasAccepted(Player p)
	{
		if(p1 == p)
		{
			return p1a;
		}
		else if(p2 == p)
		{
			return p2a;
		}
		else 
		{
			return false;
		}
	}
	
	public boolean BothAcceoted(Player p)
	{
		if(p1a && p2a)
		{
			return true;
		}
		return false;
	}
}