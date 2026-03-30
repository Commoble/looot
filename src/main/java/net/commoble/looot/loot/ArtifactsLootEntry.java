package net.commoble.looot.loot;

import java.util.List;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.commoble.looot.data.Artifact;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class ArtifactsLootEntry extends LootPoolSingletonContainer
{
	public static final MapCodec<ArtifactsLootEntry> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
		Artifact.HOLDERSET_CODEC.fieldOf("artifacts").forGetter(ArtifactsLootEntry::artifacts))
		.and(singletonFields(builder))
		.apply(builder, ArtifactsLootEntry::new));
	
	private final HolderSet<LootPoolEntryContainer> artifacts;

	protected ArtifactsLootEntry(HolderSet<LootPoolEntryContainer> artifacts, int weight, int quality, List<LootItemCondition> conditions, List<LootItemFunction> functions)
	{
		super(weight, quality, conditions, functions);
		this.artifacts = artifacts;
	}

	@Override
	public MapCodec<? extends LootPoolSingletonContainer> codec()
	{
		return CODEC;
	}
	
	public HolderSet<LootPoolEntryContainer> artifacts()
	{
		return this.artifacts;
	}

	@Override
	protected void createItemStack(Consumer<ItemStack> output, LootContext context)
	{
		@Nullable Holder<LootPoolEntryContainer> holder = this.artifacts.getRandomElement(context.getRandom()).orElse(null);
		if (holder != null)
		{
			holder.value().expand(context, entry -> entry.createItemStack(output, context));
		}
	}

}
