package net.commoble.looot.enchantment;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.commoble.looot.Looot;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.effects.AllOf;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * EnchantmentEntityEffect which applies a sub-effect to entities
 * within a cubical or spherical region around the affected entity.
 * @param radius Radius of the cube/sphere in meters
 * @param spherical Applies affects in a sphere if true, or in a cube if false
 * @param includeTarget Applies affects to primary affected entity if true, or doesn't if false
 * @param keepOriginalPosition If true, applies affects using position of original effect. If false, uses positions of affected nearby entities.
 * @param predicate Optional EntityPredicate for filtering entities in area 
 * @param effect EnchantmentEntityAffect to apply to entities in area; use {@link AllOf.EntityEffects} to apply multiple effects
 */
public record AffectNearbyEntities(
	double radius,
	boolean spherical,
	boolean includeTarget,
	boolean keepOriginalPosition,
	Optional<EntityPredicate> predicate,
	EnchantmentEntityEffect effect 
) implements EnchantmentEntityEffect
{
	/// minecraft:enchantment_entity_effect_type / looot:affect_nearby_entities
	public static final ResourceKey<MapCodec<? extends EnchantmentEntityEffect>> KEY = ResourceKey.create(Registries.ENCHANTMENT_ENTITY_EFFECT_TYPE, Looot.id("affect_nearby_entities"));
	/// holder
	public static final DeferredHolder<MapCodec<? extends EnchantmentEntityEffect>, MapCodec<AffectNearbyEntities>> AFFECT_NEARBY_ENTITIES = DeferredHolder.create(KEY);
	
	/// ```json
	/// {
	/// 	"type": "looot:affect_nearby_entities",
	/// 	"radius": 5.0, // radius in blocks / meters to apply affect
	/// 	"spherical": true, // defaults false if not specified
	/// 	"include_target": true, // defaults false if not specified
	/// 	"keep_original_position": true, // defaults false if not specified
	/// 	"predicate": {
	/// 		// optional EntityPredicate object
	/// 	},
	/// 	"effect": {
	/// 		// sub-effect to apply to nearby entities
	/// 	}
	/// }
	/// ```
	public static final MapCodec<AffectNearbyEntities> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
			Codec.DOUBLE.fieldOf("radius").forGetter(AffectNearbyEntities::radius),
			Codec.BOOL.optionalFieldOf("spherical", false).forGetter(AffectNearbyEntities::spherical),
			Codec.BOOL.optionalFieldOf("include_target", false).forGetter(AffectNearbyEntities::includeTarget),
			Codec.BOOL.optionalFieldOf("keep_original_position", false).forGetter(AffectNearbyEntities::keepOriginalPosition),
			EntityPredicate.CODEC.optionalFieldOf("predicate").forGetter(AffectNearbyEntities::predicate),
			EnchantmentEntityEffect.CODEC.fieldOf("effect").forGetter(AffectNearbyEntities::effect)
		).apply(builder, AffectNearbyEntities::new));

	@Override
	public MapCodec<? extends EnchantmentEntityEffect> codec()
	{
		return CODEC;
	}
	
	@Override
	public void apply(ServerLevel serverLevel, int enchantmentLevel, EnchantedItemInUse item, Entity entity, Vec3 position)
	{
		double diameter = this.radius*2;
		AABB aabb = AABB.ofSize(entity.position(), diameter, diameter, diameter);
		List<Entity> entities = serverLevel.getEntities(this.includeTarget ? null : entity, aabb);
		double radiusSq = this.radius * this.radius;
		for (Entity nearbyEntity : entities)
		{
			Vec3 targetPos = this.keepOriginalPosition ? position : nearbyEntity.position();
			boolean withinRange = !this.spherical || nearbyEntity.distanceToSqr(entity) <= radiusSq; 
			if (withinRange && this.predicate.map(pred -> pred.matches(serverLevel, targetPos, nearbyEntity)).orElse(true))
			{
				this.effect.apply(serverLevel, enchantmentLevel, item, nearbyEntity, targetPos);
			}
		}
	}

}
