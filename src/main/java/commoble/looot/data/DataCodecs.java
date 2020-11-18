package commoble.looot.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

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
		return ResourceLocation.CODEC.flatXmap(id -> getRegistryObjectOrError(registry, id), obj -> DataResult.success(obj.getRegistryName()));
	}
	
	static <T extends IForgeRegistryEntry<T>> DataResult<T> getRegistryObjectOrError(IForgeRegistry<T> registry, ResourceLocation id)
	{
		if (registry.containsKey(id))
		{
			return DataResult.success(registry.getValue(id));
		}
		else
		{
			return DataResult.error(String.format("Failed to decode registry object: no %s registered for id %s", registry.getRegistryName().toString(), id.toString()));
		}
	}
}
