package commoble.looot;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;

import commoble.looot.data.EnchantmentNameLimitManager;
import commoble.looot.data.MergeableCodecDataManager;
import commoble.looot.data.NameList;
import commoble.looot.data.NameListManager;
import commoble.looot.data.loot.ApplyFunctionsIfItemHasTag;
import commoble.looot.data.loot.NameEnchantedItem;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Looot.MODID)
public class Looot
{
	public static final String MODID = "looot";
	public static Looot INSTANCE = null;
	static Logger LOGGER = LogManager.getLogger();
	
	public EnchantmentNameLimitManager enchantmentNameLimits = new EnchantmentNameLimitManager();
	public MergeableCodecDataManager<NameList, List<String>> epicNamePrefixes = new NameListManager("looot/namewords/prefixes", LOGGER);
	public MergeableCodecDataManager<NameList, List<String>> epicNameNouns = new NameListManager("looot/namewords/nouns", LOGGER);
	public MergeableCodecDataManager<NameList, List<String>> epicNameSuffixes = new NameListManager("looot/namewords/suffixes", LOGGER);
	public List<MergeableCodecDataManager<NameList, List<String>>> wordMaps = ImmutableList.of(this.epicNamePrefixes, this.epicNameNouns, this.epicNameSuffixes);
	
	public Looot()
	{
		INSTANCE = this;
		
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		IEventBus forgeBus = MinecraftForge.EVENT_BUS;
		
		modBus.addListener(this::onCommonSetup);
		forgeBus.addListener(this::onAddReloadListeners);
	}
	
	// modloading events are multithreaded
	void onCommonSetup(FMLCommonSetupEvent event)
	{
		// enqueue stuff to run on the main thread after the event
		event.enqueueWork(this::afterCommonSetup);
	}
	
	// runs on the main thread, so it's safe to touch not-thread-safe registries here
	void afterCommonSetup()
	{
		// register stuff to vanilla registries where forge registries don't exist
		Registry.register(Registry.LOOT_FUNCTION_TYPE, ApplyFunctionsIfItemHasTag.ID, ApplyFunctionsIfItemHasTag.TYPE);
		Registry.register(Registry.LOOT_FUNCTION_TYPE, NameEnchantedItem.ID, NameEnchantedItem.TYPE);
	}
	
	void onAddReloadListeners(AddReloadListenerEvent event)
	{
		event.addListener(this.enchantmentNameLimits);
		event.addListener(this.epicNamePrefixes);
		event.addListener(this.epicNameNouns);
		event.addListener(this.epicNameSuffixes);
	}
}
