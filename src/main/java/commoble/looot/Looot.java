package commoble.looot;

import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import commoble.looot.data.EnchantmentNameLimitManager;
import commoble.looot.data.MergeableCodecDataManager;
import commoble.looot.data.NameList;
import commoble.looot.data.NameListManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Looot.MODID)
public class Looot
{
	public static final String MODID = "looot";
	public static Looot INSTANCE = null;
	static Logger LOGGER = LogManager.getLogger();
	
	public EnchantmentNameLimitManager enchantmentNameLimits = new EnchantmentNameLimitManager();
	public MergeableCodecDataManager<NameList, Set<String>> epicNamePrefixes = new NameListManager("looot/namewords/prefixes", LOGGER);
	public MergeableCodecDataManager<NameList, Set<String>> epicNameNounds = new NameListManager("looot/namewords/nouns", LOGGER);
	public MergeableCodecDataManager<NameList, Set<String>> epicNameSuffixes = new NameListManager("looot/namewords/suffixes", LOGGER);
	
	public Looot()
	{
		INSTANCE = this;
		
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		IEventBus forgeBus = MinecraftForge.EVENT_BUS;
		
		forgeBus.addListener(this::onAddReloadListeners);
	}
	
	void onAddReloadListeners(AddReloadListenerEvent event)
	{
		event.addListener(this.enchantmentNameLimits);
	}
}
