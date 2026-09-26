package net.commoble.looot;

import java.util.List;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.commoble.looot.data.NameListManager;
import net.commoble.looot.enchantment.AddFood;
import net.commoble.looot.enchantment.AffectNearbyEntities;
import net.commoble.looot.enchantment.ApplyMobEffectBetter;
import net.commoble.looot.enchantment.DeadEntityPredicate;
import net.commoble.looot.loot.ApplyFunctionsToItems;
import net.commoble.looot.loot.NameEnchantedItem;
import net.minecraft.advancements.predicates.entity.EntitySubPredicate;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

@Mod(Looot.MODID)
public class Looot
{
	public static final String MODID = "looot";

	private static final DeferredRegister<MapCodec<? extends EnchantmentEntityEffect>> ENCHANTMENT_ENTITY_EFFECT_TYPES = defreg(Registries.ENCHANTMENT_ENTITY_EFFECT_TYPE);
	private static final DeferredRegister<Codec<? extends EntitySubPredicate>> ENTITY_SUB_PREDICATES = defreg(Registries.ENTITY_SUB_PREDICATE_TYPE);
	private static final DeferredRegister<MapCodec<? extends LootItemFunction>> LOOT_ITEM_FUNCTION_TYPES = defreg(Registries.LOOT_FUNCTION_TYPE);

	public static final NameListManager EPIC_NAME_PREFIXES = new NameListManager("looot/namewords/prefixes");
	public static final NameListManager EPIC_NAME_NOUNS = new NameListManager("looot/namewords/nouns");
	public static final NameListManager EPIC_NAME_SUFFIXES = new NameListManager("looot/namewords/suffixes");
	public static final List<NameListManager> WORD_MAPS = ImmutableList.of(EPIC_NAME_PREFIXES, EPIC_NAME_NOUNS, EPIC_NAME_SUFFIXES);
	public static final DataMapType<Enchantment, Integer> ENCHANTMENT_NAME_LIMITS = DataMapType.builder(id("name_limits"), Registries.ENCHANTMENT, Codec.INT).build();
	
	public Looot(IEventBus modBus)
	{
		IEventBus forgeBus = NeoForge.EVENT_BUS;
		
		modBus.addListener(this::onRegisterDataMaps);
		forgeBus.addListener(this::onAddReloadListeners);
		
		ENTITY_SUB_PREDICATES.register(DeadEntityPredicate.KEY.identifier().getPath(), () -> DeadEntityPredicate.CODEC);

		ENCHANTMENT_ENTITY_EFFECT_TYPES.register(AddFood.KEY.identifier().getPath(), () -> AddFood.CODEC);
		ENCHANTMENT_ENTITY_EFFECT_TYPES.register(ApplyMobEffectBetter.KEY.identifier().getPath(), () -> ApplyMobEffectBetter.CODEC);
		ENCHANTMENT_ENTITY_EFFECT_TYPES.register(AffectNearbyEntities.KEY.identifier().getPath(), () -> AffectNearbyEntities.CODEC);
		
		LOOT_ITEM_FUNCTION_TYPES.register(ApplyFunctionsToItems.KEY.identifier().getPath(), () -> ApplyFunctionsToItems.CODEC);
		LOOT_ITEM_FUNCTION_TYPES.register(NameEnchantedItem.KEY.identifier().getPath(), () -> NameEnchantedItem.CODEC);
	}
	
	private void onRegisterDataMaps(RegisterDataMapTypesEvent event)
	{
		event.register(ENCHANTMENT_NAME_LIMITS);
	}
	
	private void onAddReloadListeners(AddServerReloadListenersEvent event)
	{
		event.addListener(id("namewords/prefixes"), EPIC_NAME_PREFIXES);
		event.addListener(id("namewords/nouns"), EPIC_NAME_NOUNS);
		event.addListener(id("namewords/suffixes"), EPIC_NAME_SUFFIXES);
	}
	
	private static <T> DeferredRegister<T> defreg(ResourceKey<Registry<T>> registryKey)
	{
		return defreg(modid -> DeferredRegister.create(registryKey, modid));
	}
	
	private static <T, R extends DeferredRegister<T>> R defreg(Function<String, R> regFactory)
	{
		R register = regFactory.apply(MODID);
		register.register(ModList.get().getModContainerById(MODID).get().getEventBus());
		return register;
	}
	
	public static final Identifier id(String path)
	{
		return Identifier.fromNamespaceAndPath(MODID, path);
	}
}
