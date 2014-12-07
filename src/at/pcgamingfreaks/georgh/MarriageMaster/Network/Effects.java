package at.pcgamingfreaks.georgh.MarriageMaster.Network;

public enum Effects
{
	Explode(0, "explode"),
	LargeExplosion(1, "largeexplosion"),
	HugeExplosion(2, "hugeexplosion"),
	FireworksSpark(3, "fireworksSpark"),
	Bubble(4, "bubble"),
	Wake(5, "wake"),
	Splash(6, "splash"),
	Suspended(7, "suspended"),
	Townaura(8, "townaura"),
	Crit(9, "crit"),
	MagicCrit(10, "magicCrit"),
	Smoke(11, "smoke"),
	LargeSmoke(12, "largesmoke"),
	MobSpell(13, "mobSpell?"),
	InstantSpell(14, "instantSpell"),
	Spell(15, "spell"),
	WitchMagic(17, "witchMagic"),
	DripWater(18, "dripWater"),
	DripLava(19, "dripLava"),
	AngryVillager(20, "angryVillager"),
	HappyVillager(21, "happyVillager"),
	Depthsuspend(22, "depthsuspend"),
	Note(23, "note"),
	Portal(24, "portal"),
	Enchantmenttable(25, "enchantmenttable"),
	Flame(26, "flame"),
	Lava(27, "lava"),
	Footstep(28, "footstep"),
	Cloud(29, "cloud"),
	Reddust(30, "reddust"),
	Snowballpoof(31, "snowballpoof"),
	Snowshovel(32, "snowshovel"),
	Slime(33, "slime"),
	Heart(34, "heart"),
	Barrier(35, "barrier");
	
	private final int id;
	private final String name;
	
	private Effects(int ID, String NAME)
	{
		id = ID;
		name = NAME;
	}
	
	public String getName()
	{
		return name;
	}
	
	public int getID()
	{
		return id;
	}
}