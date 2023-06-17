package commoble.looot.loot;

import org.apache.commons.lang3.ArrayUtils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * let's say we want to run one or more loot functions if a generated item belongs to an itemtag<br>
 * this can't be done by the vanilla loot conditions or functions, so it's a good candidate for making a new feature<br>
 * unfortunately, loot conditions can't observe the itemstack itself, so we have to write the condition as a loot function instead
 */
public class ApplyFunctionsIfTagged extends LootItemConditionalFunction
{
	public static final String ID = "apply_functions_if_tagged";
	public static final String TAG_KEY = "tag";
	public static final String FUNCTIONS_KEY = "functions";
	public static final LootItemFunctionType TYPE = new LootItemFunctionType(new ApplyFunctionsIfTagged.Serializer());

	private final TagKey<Item> tag;
	private final LootItemFunction[] subFunctions;

	public ApplyFunctionsIfTagged(LootItemCondition[] conditions, ResourceLocation tagName, LootItemFunction[] subFunctions)
	{
		super(conditions);
		this.tag = TagKey.create(Registries.ITEM, tagName);
		this.subFunctions = subFunctions;
	}

	@Override
	public LootItemFunctionType getType()
	{
		return TYPE;
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

		if (stack.is(this.tag))
		{
			// mash all the functions into one function for simplicity's sake
			newStack = LootItemFunctions.compose(this.subFunctions).apply(newStack, context);
		}
		return newStack;
	}

	// builders are used for autogenerating loot tables from code
	public static LootItemConditionalFunction.Builder<?> getBuilder(ResourceLocation tag, LootItemFunction ... subFunctions)
	{
		return simpleBuilder((conditions) -> {
			return new ApplyFunctionsIfTagged(conditions, tag, subFunctions);
		});
	}

	// The serializer is used for generating loot table jsons from code
	// The deserializer is used for reading a loot table json into code
	public static class Serializer extends LootItemConditionalFunction.Serializer<ApplyFunctionsIfTagged>
	{
		// writing to json is very similar to writing to NBT
		@Override
		public void serialize(JsonObject baseObject, ApplyFunctionsIfTagged applicator,
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
		public ApplyFunctionsIfTagged deserialize(JsonObject baseObject,
				JsonDeserializationContext deserializationContext, LootItemCondition[] conditions)
		{
			// get the tag from the json
			ResourceLocation tagRL = new ResourceLocation(GsonHelper.getAsString(baseObject, TAG_KEY));

			// get the functions from the json
			LootItemFunction[] subFunctions = GsonHelper.getAsObject(baseObject, FUNCTIONS_KEY, new LootItemFunction[0],
					deserializationContext, LootItemFunction[].class);

			return new ApplyFunctionsIfTagged(conditions, tagRL, subFunctions);
		}
	}
}
