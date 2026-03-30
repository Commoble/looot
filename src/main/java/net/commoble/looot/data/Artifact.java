package net.commoble.looot.data;

import com.mojang.serialization.Codec;

import net.commoble.looot.Looot;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;

public final class Artifact
{
	private Artifact() {}
	
	public static final ResourceKey<Registry<LootPoolEntryContainer>> KEY = ResourceKey.createRegistryKey(Looot.id("artifact"));
	public static final Codec<Holder<LootPoolEntryContainer>> CODEC = RegistryFileCodec.create(KEY, LootPoolEntries.CODEC);
	public static final Codec<HolderSet<LootPoolEntryContainer>> HOLDERSET_CODEC = HolderSetCodec.create(KEY, CODEC, false);
}
