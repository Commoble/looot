package commoble.looot.examplemod;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(LoootExampleMod.MODID)
public class LoootExampleMod
{
	public static final String MODID = "looot-example-mod";
	
	public LoootExampleMod()
	{
		MinecraftForge.EVENT_BUS.addListener(this::onBlockBroke);
	}
	
	void onBlockBroke(BlockEvent.BreakEvent event)
	{
		System.out.println("Example Mod Loaded!");
	}
}
