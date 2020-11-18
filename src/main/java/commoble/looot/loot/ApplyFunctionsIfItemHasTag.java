package commoble.looot.loot;

import java.util.function.BiFunction;

import org.apache.commons.lang3.ArrayUtils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;

import commoble.looot.Looot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootFunction;
import net.minecraft.loot.LootFunctionType;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.loot.functions.ILootFunction;
import net.minecraft.loot.functions.LootFunctionManager;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

//let's say we want to run one or more loot functions if a generated item belongs to an itemtag
//this can't be done by the vanilla loot conditions or functions, so it's a good candidate for making a new feature
//unfortunately, loot conditions can't observe the itemstack itself, so we have to write the condition as a loot function instead
public class ApplyFunctionsIfItemHasTag extends LootFunction
{
	public static final ResourceLocation ID = new ResourceLocation(Looot.MODID, "apply_functions_if_item_has_tag");
	public static final String TAG_KEY = "tag";
	public static final String FUNCTIONS_KEY = "functions";
	public static final LootFunctionType TYPE = new LootFunctionType(new ApplyFunctionsIfItemHasTag.Serializer());

	private final ITag<Item> tag;
	private final ILootFunction[] subFunctions;

	public ApplyFunctionsIfItemHasTag(ILootCondition[] conditions, ResourceLocation tagName, ILootFunction[] subFunctions)
	{
		super(conditions);
		this.tag = ItemTags.makeWrapperTag(tagName.toString());
		this.subFunctions = subFunctions;
	}

	@Override
	public LootFunctionType getFunctionType()
	{
		return TYPE;
	}

	// this is the actual function that gets applied to the itemstack in the loot
	// table (called by the loot framework)
	// here, we apply a sequence of loot functions if the given itemstack belongs to
	// an item tag
	// (the tag and the functions are specified by the loot table json)
	@Override
	protected ItemStack doApply(ItemStack stack, LootContext context)
	{
		ItemStack newStack = stack;

		// mash all the functions into one function for simplicity's sake
		BiFunction<ItemStack, LootContext, ItemStack> combinedFunction = LootFunctionManager.combine(this.subFunctions);
		if (this.tag.contains(stack.getItem()))
		{
			newStack = combinedFunction.apply(newStack, context);
		}
		return newStack;
	}

	// builders are used for autogenerating loot tables from code
	public static LootFunction.Builder<?> getBuilder(ResourceLocation tag, ILootFunction ... subFunctions)
	{
		return builder((conditions) -> {
			return new ApplyFunctionsIfItemHasTag(conditions, tag, subFunctions);
		});
	}

	// The serializer is used for generating loot table jsons from code
	// The deserializer is used for reading a loot table json into code
	public static class Serializer extends LootFunction.Serializer<ApplyFunctionsIfItemHasTag>
	{
		// writing to json is very similar to writing to NBT
		@Override
		public void serialize(JsonObject baseObject, ApplyFunctionsIfItemHasTag applicator,
				JsonSerializationContext serializationContext)
		{
			super.serialize(baseObject, applicator, serializationContext);
			if (applicator.subFunctions.length > 0)
			{
				// write the tag name into json
				baseObject.add(TAG_KEY, new JsonPrimitive(applicator.tag.toString()));

				// write the subfunctions into json
				// this is very easy as all loot functions already have their own serialization
				// behaviour defined
				if (!ArrayUtils.isEmpty(applicator.subFunctions))
				{
					baseObject.add(FUNCTIONS_KEY, serializationContext.serialize(applicator.subFunctions));
				}
			}

		}

		@Override
		public ApplyFunctionsIfItemHasTag deserialize(JsonObject baseObject,
				JsonDeserializationContext deserializationContext, ILootCondition[] conditions)
		{
			// get the tag from the json
			ResourceLocation tagRL = new ResourceLocation(JSONUtils.getString(baseObject, TAG_KEY));

			// get the functions from the json
			ILootFunction[] subFunctions = JSONUtils.deserializeClass(baseObject, FUNCTIONS_KEY, new ILootFunction[0],
					deserializationContext, ILootFunction[].class);

			return new ApplyFunctionsIfItemHasTag(conditions, tagRL, subFunctions);
		}
	}
}
