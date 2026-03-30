package net.commoble.looot.enchantment;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.commoble.looot.Looot;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * EnchantmentEntityEffect which adds nutrition/saturation to affected players.
 * @param nutrition LevelBasedValue of nutrition to add based on enchantment level. May be negative. Rounds down to next integer if value resolves as a float.
 * @param saturation LevelBasedValue of saturation to add based on enchantment level. May be negative.
 */
public record AddFood(LevelBasedValue nutrition, LevelBasedValue saturation) implements EnchantmentEntityEffect
{
	/// minecraft:enchantment_entity_effect_type / looot:add_food
	public static final ResourceKey<MapCodec<? extends EnchantmentEntityEffect>> KEY = ResourceKey.create(Registries.ENCHANTMENT_ENTITY_EFFECT_TYPE, Looot.id("add_food"));
	/// holder
	public static final DeferredHolder<MapCodec<? extends EnchantmentEntityEffect>, MapCodec<AddFood>> HOLDER = DeferredHolder.create(KEY);
	
	/// ```json
	/// {
	/// 	"type": "looot:add_food",
	/// 	"nutrition": {"type": "linear", "base": 1, "per_level_above_first": 1}, // how many hunger points to restore to the player (half-drumsticks). May be negative to subtract instead of add.
	/// 	"saturation": {"type": "linear", "base": 0.2, "per_level_above_first": 0.2} // how much saturation to restore to the player (or subtract, if negative)
	/// }
	/// ```
	public static final MapCodec<AddFood> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
			LevelBasedValue.CODEC.fieldOf("nutrition").forGetter(AddFood::nutrition),
			LevelBasedValue.CODEC.fieldOf("saturation").forGetter(AddFood::saturation)
		).apply(builder, AddFood::new));

	@Override
	public MapCodec<? extends EnchantmentEntityEffect> codec()
	{
		return CODEC;
	}

	@Override
	public void apply(ServerLevel serverLevel, int enchantmentLevel, EnchantedItemInUse item, Entity entity, Vec3 position)
	{
		if (entity instanceof ServerPlayer serverPlayer)
		{
			serverPlayer.getFoodData().eat(new FoodProperties(
				(int)(this.nutrition.calculate(enchantmentLevel)),
				this.saturation.calculate(enchantmentLevel),
				true
			));
		}
	}

}
