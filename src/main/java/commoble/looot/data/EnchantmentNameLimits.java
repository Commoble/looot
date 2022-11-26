package commoble.looot.data;

import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Registry;
import net.minecraft.world.item.enchantment.Enchantment;

public class EnchantmentNameLimits
{
	@SuppressWarnings("deprecation")
	public static final Codec<EnchantmentNameLimits> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.BOOL.optionalFieldOf("replace", false).forGetter(EnchantmentNameLimits::getReplace),
			Codec.unboundedMap(Registry.ENCHANTMENT.byNameCodec(), Codec.INT).fieldOf("values").forGetter(EnchantmentNameLimits::getValues)
		).apply(instance, EnchantmentNameLimits::new));

	private final boolean replace;

	public boolean getReplace()
	{
		return this.replace;
	}

	private final Map<Enchantment, Integer> values;

	public Map<Enchantment, Integer> getValues()
	{
		return this.values;
	}

	public EnchantmentNameLimits(final boolean replace, final Map<Enchantment, Integer> values)
	{
		this.replace = replace;
		this.values = values;
	}

}
