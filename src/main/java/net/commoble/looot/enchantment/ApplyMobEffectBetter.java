package net.commoble.looot.enchantment;

import java.util.Optional;

import org.jspecify.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.commoble.looot.Looot;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.effects.ApplyMobEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredHolder;

/// ApplyMobEffect but it also allows the rest of the MobEffectInstance parameters to be defined
/// @param applyMobEffect base ApplyMobEffect
/// @param ambient true if the mob effect is applied to the entity from some external source
/// @param visible true if particles should be shown for the effect
/// @param showIcon true if the icon for this effect should be shown at all times
/// @param hiddenEffect if present, generates a hidden effect which the primary effect will be replaced with when it expires 
public record ApplyMobEffectBetter(
	ApplyMobEffect applyMobEffect,
	boolean ambient,
	boolean visible,
	boolean showIcon,
	Optional<ApplyMobEffectBetter> hiddenEffect
) implements EnchantmentEntityEffect
{
	/// minecraft:enchantment_entity_effect_type / looot:apply_mob_effect_better
	public static final ResourceKey<MapCodec<? extends EnchantmentEntityEffect>> KEY = ResourceKey.create(Registries.ENCHANTMENT_ENTITY_EFFECT_TYPE, Looot.id("apply_mob_effect_better"));
	/// holder
	public static final DeferredHolder<MapCodec<? extends EnchantmentEntityEffect>, MapCodec<ApplyMobEffectBetter>> HOLDER = DeferredHolder.create(KEY);
	
	/// ```json
	/// {
	///  	"type": "looot:apply_mob_effect_better",
	/// 	"to_apply": "modid:some_mob_effect", // can be a holderset
	/// 	"min_duration": 10.0, // min duration of effect in seconds, may be a LevelBasedValue
	/// 	"max_duration": 10.0, // max duration of effect in seconds, may be a LevelBasedValue
	/// 	"min_amplifier": 0.0,	// min level of effect, may be a LevelBasedValue
	/// 	"max_amplifier": 0.0,	// max level of effect, may be a LevelBasedValue
	/// 	"ambient": false,	// optional, defaults false; affects display of icon
	/// 	"visible": true,	// optional, defaults true; whether to show effect particles
	/// 	"show_icon": true,	// optional, defaults true
	/// 	"hidden_effect": { // optional sub-ApplyMobEffectBetter object
	/// 		"to_apply": "etc",
	/// 		// if present, generates another effect to replace the primary effect when it expires
	/// 	}
	/// }
	/// ```
	public static final MapCodec<ApplyMobEffectBetter> CODEC = MapCodec.recursive("apply_mob_effect", recursiveCodec -> RecordCodecBuilder.mapCodec(builder -> builder.group(
			ApplyMobEffect.CODEC.forGetter(ApplyMobEffectBetter::applyMobEffect),
			Codec.BOOL.optionalFieldOf("ambient", false).forGetter(ApplyMobEffectBetter::ambient),
			Codec.BOOL.optionalFieldOf("visible", true).forGetter(ApplyMobEffectBetter::visible),
			Codec.BOOL.optionalFieldOf("show_icon", true).forGetter(ApplyMobEffectBetter::showIcon),
			recursiveCodec.optionalFieldOf("hidden_effect").forGetter(ApplyMobEffectBetter::hiddenEffect)
		).apply(builder, ApplyMobEffectBetter::new)));
	
	@Override
	public void apply(ServerLevel serverLevel, int enchantmentLevel, EnchantedItemInUse item, Entity entity, Vec3 position)
	{
		if (entity instanceof LivingEntity living)
		{
			RandomSource random = living.getRandom();
			@Nullable MobEffectInstance effectInstance = this.createMobEffectInstance(random, enchantmentLevel);
			if (effectInstance != null)
			{
				living.addEffect(effectInstance);
			}
		}
	}
	
	private @Nullable MobEffectInstance createMobEffectInstance(RandomSource random, int enchantmentLevel)
	{
		ApplyMobEffect baseEffectGenerator = this.applyMobEffect;
		@Nullable Holder<MobEffect> mobEffect = baseEffectGenerator.toApply().getRandomElement(random).orElse(null);
		if (mobEffect == null)
			return null;
		
		int ticks = Math.round(Mth.randomBetween(random, baseEffectGenerator.minDuration().calculate(enchantmentLevel), baseEffectGenerator.maxDuration().calculate(enchantmentLevel)) * 20.0F);
		int amplifier = Math.max(0, Math.round(Mth.randomBetween(random, baseEffectGenerator.minAmplifier().calculate(enchantmentLevel), baseEffectGenerator.maxAmplifier().calculate(enchantmentLevel))));
		@Nullable MobEffectInstance hiddenEffect = this.hiddenEffect.map(effect -> effect.createMobEffectInstance(random, enchantmentLevel)).orElse(null);
		return new MobEffectInstance(mobEffect, ticks, amplifier, this.ambient, this.visible, this.showIcon, hiddenEffect);
	}

	@Override
	public MapCodec<? extends EnchantmentEntityEffect> codec()
	{
		return CODEC;
	}

}
