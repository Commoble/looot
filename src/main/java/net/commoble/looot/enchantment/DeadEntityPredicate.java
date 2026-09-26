package net.commoble.looot.enchantment;

import org.jspecify.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.commoble.looot.Looot;
import net.minecraft.advancements.predicates.entity.EntitySubPredicate;
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
	public static final ResourceKey<Codec<? extends EntitySubPredicate>> KEY = ResourceKey.create(Registries.ENTITY_SUB_PREDICATE_TYPE, Looot.id("dead"));
	/// holder
	public static final DeferredHolder<Codec<? extends EntitySubPredicate>, Codec<DeadEntityPredicate>> HOLDER = DeferredHolder.create(KEY);
	
	/// ```json
	/// {
	/// 	"type": "looot:dead"
	/// }
	/// ```
	public static final Codec<DeadEntityPredicate> CODEC = MapCodec.unitCodec(INSTANCE);

	@Override
	public boolean matches(Entity entity, ServerLevel level, @Nullable Vec3 position)
	{
		return entity instanceof LivingEntity living && living.isDeadOrDying();
	}

}
