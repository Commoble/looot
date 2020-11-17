package commoble.looot.data;

import com.mojang.serialization.Codec;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class DataCodecs
{
	public static final Codec<Enchantment> ENCHANTMENT = makeRegistryEntryCodec(ForgeRegistries.ENCHANTMENTS);
	
	public static final <T extends IForgeRegistryEntry<T>> Codec<T> makeRegistryEntryCodec(IForgeRegistry<T> registry)
	{
		return ResourceLocation.CODEC.xmap(registry::getValue, IForgeRegistryEntry::getRegistryName);
	}
}
