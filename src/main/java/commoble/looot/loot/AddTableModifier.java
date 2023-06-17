package commoble.looot.loot;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

public class AddTableModifier extends LootModifier
{
	/**
	 * Only used internally for registration, should not be referred to by anything else
	 */
	@ApiStatus.Internal
	public static final Codec<AddTableModifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
					IGlobalLootModifier.LOOT_CONDITIONS_CODEC.fieldOf("conditions").forGetter(glm -> glm.conditions),
					ResourceLocation.CODEC.fieldOf("table").forGetter(AddTableModifier::table)
				).apply(instance, AddTableModifier::new));
	
	private final ResourceLocation table;

	protected AddTableModifier(LootItemCondition[] conditionsIn, ResourceLocation table)
	{
		super(conditionsIn);
		this.table = table;
	}
	
	public ResourceLocation table()
	{
		return this.table;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context)
	{
		LootTable extraTable = context.getResolver().getLootTable(this.table);
		extraTable.getRandomItems(context, generatedLoot::add); // don't run loot modifiers for subtables
		return generatedLoot;
	}

	@Override
	public Codec<? extends IGlobalLootModifier> codec()
	{
		return null;
	}
}