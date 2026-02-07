package commoble.looot.loot;

import java.util.Arrays;
import java.util.List;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import commoble.looot.Looot;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * let's say we want to run one or more loot functions if a generated item belongs to an itemtag<br>
 * this can't be done by the vanilla loot conditions or functions, so it's a good candidate for making a new feature<br>
 * unfortunately, loot conditions can't observe the itemstack itself, so we have to write the condition as a loot function instead
 */
public class ApplyFunctionsToItems extends LootItemConditionalFunction
{	
	public static final ResourceKey<MapCodec<? extends LootItemFunction>> KEY = ResourceKey.create(Registries.LOOT_FUNCTION_TYPE, Looot.id("apply_functions_if_tagged"));
	public static final DeferredHolder<MapCodec<? extends LootItemFunction>, MapCodec<ApplyFunctionsToItems>> HOLDER = DeferredHolder.create(KEY);
	
	public static final MapCodec<ApplyFunctionsToItems> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
			LootItemCondition.DIRECT_CODEC.listOf().optionalFieldOf("conditions", List.of()).forGetter(f -> f.predicates),
			HolderSetCodec.create(Registries.ITEM, BuiltInRegistries.ITEM.holderByNameCodec(), false).fieldOf("items").forGetter(ApplyFunctionsToItems::items),
			LootItemFunctions.TYPED_CODEC.listOf().fieldOf("functions").forGetter(ApplyFunctionsToItems::functions)
		).apply(builder, ApplyFunctionsToItems::new));

	private final HolderSet<Item> items;
	private final List<LootItemFunction> functions;

	public ApplyFunctionsToItems(List<LootItemCondition> conditions, HolderSet<Item> items, List<LootItemFunction> functions)
	{
		super(conditions);
		this.items = items;
		this.functions = functions;
	}

	@Override
	public MapCodec<? extends LootItemConditionalFunction> codec()
	{
		return CODEC;
	}
	
	public HolderSet<Item> items()
	{
		return this.items;
	}
	
	public List<LootItemFunction> functions()
	{
		return this.functions;
	}

	// this is the actual function that gets applied to the itemstack in the loot
	// table (called by the loot framework)
	// here, we apply a sequence of loot functions if the given itemstack belongs to
	// an item tag
	// (the tag and the functions are specified by the loot table json)
	@Override
	protected ItemStack run(ItemStack stack, LootContext context)
	{
		ItemStack newStack = stack;

		if (stack.is(this.items))
		{
			// mash all the functions into one function for simplicity's sake
			newStack = LootItemFunctions.compose(this.functions).apply(newStack, context);
		}
		return newStack;
	}

	// builders are used for autogenerating loot tables from code
	public static LootItemConditionalFunction.Builder<?> getBuilder(HolderSet<Item> items, LootItemFunction ... functions)
	{
		return simpleBuilder((conditions) -> {
			return new ApplyFunctionsToItems(conditions, items, Arrays.asList(functions));
		});
	}
}
