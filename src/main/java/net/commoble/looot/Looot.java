package net.commoble.looot;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.commoble.looot.data.Artifact;
import net.commoble.looot.data.NameListManager;
import net.commoble.looot.enchantment.AddFood;
import net.commoble.looot.enchantment.AffectNearbyEntities;
import net.commoble.looot.enchantment.ApplyMobEffectBetter;
import net.commoble.looot.enchantment.DeadEntityPredicate;
import net.commoble.looot.loot.ApplyFunctionsToItems;
import net.commoble.looot.loot.ArtifactsLootEntry;
import net.commoble.looot.loot.NameEnchantedItem;
import net.minecraft.advancements.criterion.EntitySubPredicate;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

@Mod(Looot.MODID)
public class Looot
{
	public static final String MODID = "looot";

	private static final DeferredRegister<MapCodec<? extends EnchantmentEntityEffect>> ENCHANTMENT_ENTITY_EFFECT_TYPES = defreg(Registries.ENCHANTMENT_ENTITY_EFFECT_TYPE);
	private static final DeferredRegister<MapCodec<? extends EntitySubPredicate>> ENTITY_SUB_PREDICATES = defreg(Registries.ENTITY_SUB_PREDICATE_TYPE);
	private static final DeferredRegister<MapCodec<? extends LootPoolEntryContainer>> LOOT_ENTRY_TYPES = defreg(Registries.LOOT_POOL_ENTRY_TYPE);
	private static final DeferredRegister<MapCodec<? extends LootItemFunction>> LOOT_ITEM_FUNCTION_TYPES = defreg(Registries.LOOT_FUNCTION_TYPE);

	public static final NameListManager EPIC_NAME_PREFIXES = new NameListManager("looot/namewords/prefixes");
	public static final NameListManager EPIC_NAME_NOUNS = new NameListManager("looot/namewords/nouns");
	public static final NameListManager EPIC_NAME_SUFFIXES = new NameListManager("looot/namewords/suffixes");
	public static final List<NameListManager> WORD_MAPS = ImmutableList.of(EPIC_NAME_PREFIXES, EPIC_NAME_NOUNS, EPIC_NAME_SUFFIXES);
	public static final DataMapType<Enchantment, Integer> ENCHANTMENT_NAME_LIMITS = DataMapType.builder(id("name_limits"), Registries.ENCHANTMENT, Codec.INT).build();
	
	public Looot(IEventBus modBus)
	{
		IEventBus forgeBus = NeoForge.EVENT_BUS;
		
		modBus.addListener(this::onRegisterDataPackRegistries);
		modBus.addListener(this::onRegisterDataMaps);
		forgeBus.addListener(this::onRegisterCommands);
		forgeBus.addListener(this::onAddReloadListeners);
		
		ENTITY_SUB_PREDICATES.register(DeadEntityPredicate.KEY.identifier().getPath(), () -> DeadEntityPredicate.CODEC);

		ENCHANTMENT_ENTITY_EFFECT_TYPES.register(AddFood.KEY.identifier().getPath(), () -> AddFood.CODEC);
		ENCHANTMENT_ENTITY_EFFECT_TYPES.register(ApplyMobEffectBetter.KEY.identifier().getPath(), () -> ApplyMobEffectBetter.CODEC);
		ENCHANTMENT_ENTITY_EFFECT_TYPES.register(AffectNearbyEntities.KEY.identifier().getPath(), () -> AffectNearbyEntities.CODEC);
		
		LOOT_ENTRY_TYPES.register("artifacts", () -> ArtifactsLootEntry.CODEC);
		
		LOOT_ITEM_FUNCTION_TYPES.register(ApplyFunctionsToItems.KEY.identifier().getPath(), () -> ApplyFunctionsToItems.CODEC);
		LOOT_ITEM_FUNCTION_TYPES.register(NameEnchantedItem.KEY.identifier().getPath(), () -> NameEnchantedItem.CODEC);
	}
	
	private void onRegisterDataPackRegistries(DataPackRegistryEvent.NewRegistry event)
	{
		event.dataPackRegistry(Artifact.KEY, LootPoolEntries.CODEC);
	}
	
	private void onRegisterDataMaps(RegisterDataMapTypesEvent event)
	{
		event.register(ENCHANTMENT_NAME_LIMITS);
	}
	

	
	private void onRegisterCommands(RegisterCommandsEvent event)
	{
		event.getDispatcher().register(Commands.literal(MODID)
			.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
			.then(Commands.literal("give_artifact")
				.then(Commands.argument("targets", EntityArgument.players())
					.then(Commands.argument("artifact", ResourceKeyArgument.key(Artifact.KEY))
						.executes(context -> {
							CommandSourceStack source = context.getSource();
							var err = new DynamicCommandExceptionType(value -> Component.literal(String.format("There is no artifact with type \"%s\"", value)));
							ResourceKey<LootPoolEntryContainer> key = ResourceKeyArgument.getRegistryKey(context, "artifact", Artifact.KEY, err);
							LootPoolEntryContainer container = context
								.getSource()
								.registryAccess()
								.lookupOrThrow(Artifact.KEY)
								.get(key)
								.orElseThrow(() -> err.create(key.identifier()))
								.value();
							Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "targets");
							for (ServerPlayer serverPlayer : players)
							{
								LootParams lootParams = new LootParams.Builder(source.getLevel())
									.withOptionalParameter(LootContextParams.THIS_ENTITY, source.getEntity())
									.withParameter(LootContextParams.ORIGIN, source.getPosition())
									.create(LootContextParamSets.COMMAND);
								LootContext lootContext = new LootContext.Builder(lootParams)
									.withOptionalRandomSource(source.getLevel().getRandom())
									.create(Optional.empty());
								container.expand(lootContext, entry -> {
									entry.createItemStack(stack -> {
										boolean added = serverPlayer.getInventory().add(stack.copy());
										source.sendSuccess(() -> Component.translatable("commands.give.success.single",
											added ? stack.count() : 0,
											stack.getDisplayName(),
											serverPlayer.getDisplayName()),
											false);
									}, lootContext);
								});
							}
							return players.size();
						})))));
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
