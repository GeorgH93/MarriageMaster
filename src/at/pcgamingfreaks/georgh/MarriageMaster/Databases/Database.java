package at.pcgamingfreaks.georgh.MarriageMaster.Databases;

import java.util.TreeMap;

import org.bukkit.Location;

import at.pcgamingfreaks.georgh.MarriageMaster.MarriageMaster;

public class Database
{
	//private MySQL mysql;
	private Files files;
	private MarriageMaster marriageMaster;
	
	public Database(MarriageMaster marriagemaster)
	{
		marriageMaster = marriagemaster;
		switch(marriageMaster.config.GetDatabaseType().toLowerCase())
		{
			case "mysql": break;
			default: files = new Files(marriageMaster); break;
		}
	}
	
	public boolean GetPvPEnabled(String playername)
	{
		switch(marriageMaster.config.GetDatabaseType().toLowerCase())
		{
			case "mysql": break;
			default: return files.GetPvPState(playername);
		}
		return true;
	}
	
	public void SetPvPEnabled(String player1, String player2, boolean state)
	{
		switch(marriageMaster.config.GetDatabaseType().toLowerCase())
		{
			case "mysql": break;
			default: files.SetPvPState(player1, state); files.SetPvPState(player2, state); break;
		}
	}
	
	public void DivorcePlayer(String player1, String player2)
	{
		switch(marriageMaster.config.GetDatabaseType().toLowerCase())
		{
			case "mysql": break;
			default: files.SaveMarriedPlayerDivorce(player1); files.SaveMarriedPlayerDivorce(player2); break;
		}
	}
	
	public void MarryPlayers(String player1, String player2, String priester)
	{
		switch(marriageMaster.config.GetDatabaseType().toLowerCase())
		{
			case "mysql": break;
			default:
				files.SaveMarriedPlayer(player1, player2, priester);
				files.SaveMarriedPlayer(player2, player1, priester);
			break;
		}
	}
	
	public void SetMarriedHome(Location loc, String player1, String player2)
	{
		switch(marriageMaster.config.GetDatabaseType().toLowerCase())
		{
			case "mysql": break;
			default:
				files.SaveMarriedHome(loc, player1);
				files.SaveMarriedHome(loc, player2);
			break;
		}
	}
	
	public Location GetMarryHome(String playername)
	{
		switch(marriageMaster.config.GetDatabaseType().toLowerCase())
		{
			case "mysql": break;
			default: return files.LoadMarriedHome(playername);
		}
		return null;
	}
	
	public void SetPriester(String Player)
	{
		switch(marriageMaster.config.GetDatabaseType().toLowerCase())
		{
			case "mysql": break;
			default: files.SavePriester(Player); break;
		}
	}
	
	public boolean IsPriester(String playername)
	{
		switch(marriageMaster.config.GetDatabaseType().toLowerCase())
		{
			case "mysql": break;
			default: return files.IsPriester(playername);
		}
		return false;
	}
	
	public String GetPartner(String playername)
	{
		switch(marriageMaster.config.GetDatabaseType().toLowerCase())
		{
			case "mysql": break;
			default: return files.GetPartner(playername);
		}
		return null;
	}
	
	public TreeMap<String, String> LoadAllMarriedPlayers()
	{
		switch(marriageMaster.config.GetDatabaseType().toLowerCase())
		{
			case "mysql": break;
			default: return files.LoadAllMarriedPlayers();
		}
		return null;
	}
}
