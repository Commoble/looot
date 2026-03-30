package net.commoble.looot.enchantment;

import org.jspecify.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import net.commoble.looot.Looot;
import net.minecraft.advancements.criterion.EntitySubPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * EntitySubPredicate which matches entities which are dead.
 * Can be used with a post_attack enchantment component to produce
 * effects which apply when an enchanted weapon kills a mob.
 */
public enum DeadEntityPredicate implements EntitySubPredicate
{
	/// singleton instance
	INSTANCE;
	
	/// minecraft:entity_sub_predicate_type / looot:dead
	public static final ResourceKey<MapCodec<? extends EntitySubPredicate>> KEY = ResourceKey.create(Registries.ENTITY_SUB_PREDICATE_TYPE, Looot.id("dead"));
	/// holder
	public static final DeferredHolder<MapCodec<? extends EntitySubPredicate>, MapCodec<DeadEntityPredicate>> HOLDER = DeferredHolder.create(KEY);
	
	/// ```json
	/// {
	/// 	"type": "looot:dead"
	/// }
	/// ```
	public static final MapCodec<DeadEntityPredicate> CODEC = MapCodec.unit(INSTANCE);

	@Override
	public MapCodec<? extends EntitySubPredicate> codec()
	{
		return CODEC;
	}

	@Override
	public boolean matches(Entity entity, ServerLevel level, @Nullable Vec3 position)
	{
		return entity instanceof LivingEntity living && living.isDeadOrDying();
	}

}
