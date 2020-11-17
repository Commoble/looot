package commoble.looot;

import net.minecraftforge.fml.common.Mod;

@Mod(Looot.MODID)
public class Looot
{
	public static final String MODID = "looot";
	public static Looot INSTANCE = null;
	
	public Looot()
	{
		INSTANCE = this;
	}
}
