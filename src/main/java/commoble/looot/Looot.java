package commoble.looot;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;

import commoble.looot.data.EnchantmentNameLimitManager;
import commoble.looot.data.NameListManager;
import commoble.looot.loot.AddTableModifier;
import commoble.looot.loot.ApplyFunctionsIfTagged;
import commoble.looot.loot.NameEnchantedItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@Mod(Looot.MODID)
public class Looot
{
	public static final String MODID = "looot";
	public static Looot INSTANCE = null;
	static Logger LOGGER = LogManager.getLogger();
	
	public EnchantmentNameLimitManager enchantmentNameLimits = new EnchantmentNameLimitManager();
	public NameListManager epicNamePrefixes = new NameListManager("looot/namewords/prefixes");
	public NameListManager epicNameNouns = new NameListManager("looot/namewords/nouns");
	public NameListManager epicNameSuffixes = new NameListManager("looot/namewords/suffixes");
	public List<NameListManager> wordMaps = ImmutableList.of(this.epicNamePrefixes, this.epicNameNouns, this.epicNameSuffixes);
	
	public Looot()
	{
		INSTANCE = this;
		
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		IEventBus forgeBus = MinecraftForge.EVENT_BUS;
		
		forgeBus.addListener(this::onAddReloadListeners);
		
		DeferredRegister<LootItemFunctionType> lootItemFunctions = makeDeferredRegister(modBus, Registries.LOOT_FUNCTION_TYPE);
		DeferredRegister<Codec<? extends IGlobalLootModifier>> lootModifierSerializers = makeDeferredRegister(modBus, ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS);
		
		lootItemFunctions.register(Names.APPLY_FUNCTIONS_IF_TAGGED, () -> ApplyFunctionsIfTagged.TYPE);
		lootItemFunctions.register(Names.NAME_ENCHANTED_ITEM, () -> NameEnchantedItem.TYPE);
		lootModifierSerializers.register(Names.ADD_TABLE, () -> AddTableModifier.CODEC);
	}
	
	void onAddReloadListeners(AddReloadListenerEvent event)
	{
		event.addListener(this.enchantmentNameLimits);
		event.addListener(this.epicNamePrefixes);
		event.addListener(this.epicNameNouns);
		event.addListener(this.epicNameSuffixes);
	}
	
	private static <T> DeferredRegister<T> makeDeferredRegister(IEventBus modBus, ResourceKey<Registry<T>> registryKey)
	{
		DeferredRegister<T> register = DeferredRegister.create(registryKey, MODID);
		register.register(modBus);
		return register;
	}
}
