package commoble.looot.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class DataCodecs
{
	public static final Codec<Enchantment> ENCHANTMENT = makeRegistryEntryCodec(Registry.ENCHANTMENT);
	
	public static final <T> Codec<T> makeRegistryEntryCodec(Registry<T> registry)
	{
		return Identifier.CODEC.flatXmap(id -> getRegistryObjectOrError(registry, id), obj -> DataResult.success(registry.getId(obj)));
	}
	
	static <T> DataResult<T> getRegistryObjectOrError(Registry<T> registry, Identifier id)
	{
		if (registry.containsId(id))
		{
			return DataResult.success(registry.get(id));
		}
		else
		{
			return DataResult.error(String.format("Failed to decode registry object: no %s registered for id %s", registry.getKey().getValue().toString(), id.toString()));
		}
	}
}
