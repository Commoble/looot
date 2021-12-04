package commoble.looot.loot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import commoble.looot.Looot;
import commoble.looot.util.RandomHelper;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;

public class NameEnchantedItem extends LootItemConditionalFunction
{
	public static final ResourceLocation ID = new ResourceLocation(Looot.MODID, "name_enchanted_item");
	public static final LootItemFunctionType TYPE = new LootItemFunctionType(new NameEnchantedItem.Serializer());
	public static final ResourceLocation ALL = new ResourceLocation(Looot.MODID, "all");
	public static final ResourceLocation UNKNOWN_ENCHANTMENT = new ResourceLocation(Looot.MODID, "unknown_enchantment");
	public static final TranslatableComponent VERY_UNKNOWN_ENCHANTMENT_PREFIX = new TranslatableComponent("looot.unknown_enchantment.prefix");
	public static final TranslatableComponent VERY_UNKNOWN_ENCHANTMENT_SUFFIX = new TranslatableComponent("looot.unknown_enchantment.suffix");
	public static final TranslatableComponent UNKNOWN_DESCRIPTOR = new TranslatableComponent("looot.unknown_descriptor");
	public static final Style DEFAULT_MINOR_STYLE = Style.EMPTY.applyFormat(ChatFormatting.AQUA);
	public static final Style DEFAULT_MAJOR_STYLE = Style.EMPTY.applyFormat(ChatFormatting.LIGHT_PURPLE);
	
	protected final boolean ignoreEnchantments; // if true, will use the "epic name" regardless of the item's enchantments
	protected final Optional<Style> minorStyle;	// style to be used for 1-2 enchantment items, defaults to aqua text
	protected final Optional<Style> majorStyle;	// style to be used for 3+ enchantment items, defaults to light purple text
	
	public NameEnchantedItem(LootItemCondition[] conditionsIn, Optional<Style> minorStyle, Optional<Style> majorStyle, boolean ignoreEnchantments)
	{
		super(conditionsIn);
		this.minorStyle = minorStyle;
		this.majorStyle = majorStyle;
		this.ignoreEnchantments = ignoreEnchantments;
	}

	@Override
	public LootItemFunctionType getType()
	{
		return TYPE;
	}
	
	public static MutableComponent getNameForEnchantment(boolean isPrefix, Enchantment enchantment, int level, Random rand)
	{
		// check the defined enchantment name limits for the given enchantment
		int maxKnownLevel = Looot.INSTANCE.enchantmentNameLimits.limits.getOrDefault(enchantment, 0);
		
		// cap the given level by the name limit
		int highestNameableLevel = Math.min(maxKnownLevel, level);
		
		// if a mod has declared that they are suppling names for this enchantment, use the appropriate translation key for it
		if (highestNameableLevel > 0)
		{
			String position = isPrefix ? ".prefix." : ".suffix.";
			return new TranslatableComponent(enchantment.getDescriptionId()+position+level);
		}
		else
		{
			// no explicit names for this enchantment, use a fallback table
			List<MutableComponent> names = isPrefix
				? Looot.INSTANCE.epicNamePrefixes.translationKeys.get(ALL)
				: Looot.INSTANCE.epicNameSuffixes.translationKeys.get(UNKNOWN_ENCHANTMENT);
			if (names.size() > 0)
			{
				return RandomHelper.getRandomThingFrom(rand, names);
			}
			else
			{
				return isPrefix
					? VERY_UNKNOWN_ENCHANTMENT_PREFIX
					: VERY_UNKNOWN_ENCHANTMENT_SUFFIX;
			}
		}
	}

	@Override
	protected ItemStack run(ItemStack stack, LootContext context)
	{
		Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
		BinaryOperator<Map.Entry<Enchantment, Integer>> biggestReducer = (a,b) -> b.getValue() > a.getValue() ? b : a;
//		BinaryOperator<Map.Entry<Enchantment, Integer>> smallestReducer = (a,b) -> b.getValue() < a.getValue() ? b : a;
		
		Random rand = context.getRandom();
//		Function<Boolean, Function<? super Map.Entry<Enchantment, Integer>, ? extends IFormattableTextComponent>> mapperGetter =
////			position -> entry -> new TranslationTextComponent(entry.getKey().getName()+position+entry.getValue().toString());
//			position -> entry -> getNameForEnchantment(position, entry.getKey(), entry.getValue(), rand);
		
		// if number of enchantments is at least three, generate an epic name and ignore the three smallest enchantments in the next phase
		int enchantmentCount = enchantments.size();
		if (this.ignoreEnchantments || enchantmentCount > 2)
		{
			stack.setHoverName(getEpicName(stack, context).withStyle(this.majorStyle.orElse(DEFAULT_MAJOR_STYLE)));
		}
		else if (enchantmentCount > 0) // 1, or 2 enchantments
		{
			// get the two biggest (if any) and generate prefix/suffix based on them
			Optional<Map.Entry<Enchantment,Integer>> biggest = enchantments.entrySet().stream()
				.reduce(biggestReducer);
			
			Optional<Map.Entry<Enchantment, Integer>> secondBiggest = enchantments.entrySet().stream()
				.filter(entry -> entry != biggest.get())	// if biggest is empty then this won't be evaluated
				.reduce(biggestReducer);
			
			Pair<Optional<Map.Entry<Enchantment, Integer>>, Optional<Map.Entry<Enchantment, Integer>>> twoBiggest = context.getRandom().nextBoolean()
				? Pair.of(biggest, secondBiggest)
				: Pair.of(secondBiggest, biggest);
			
			Optional<MutableComponent> maybePrefix = twoBiggest.getLeft().map(entry -> getNameForEnchantment(true, entry.getKey(), entry.getValue(), rand));
			Optional<MutableComponent> maybeSuffix = twoBiggest.getRight().map(entry -> getNameForEnchantment(false, entry.getKey(), entry.getValue(), rand));
			
			Component stackText = stack.getHoverName();
			if (stackText instanceof MutableComponent)
			{
				MutableComponent formattableStackText = (MutableComponent)stackText;
				MutableComponent prefixedStackText = maybePrefix.map(prefix -> prefix.append(" ").append(formattableStackText))
					.orElse(formattableStackText);
				MutableComponent suffixedStackText = maybeSuffix.map(suffix -> prefixedStackText.append(" ").append(suffix))
					.orElse(prefixedStackText);
				stack.setHoverName(suffixedStackText.withStyle(this.minorStyle.orElse(DEFAULT_MINOR_STYLE)));
			}
		}

		return stack;
	}

	public static class Serializer extends LootItemConditionalFunction.Serializer<NameEnchantedItem>
	{
		public static final Style.Serializer STYLE_SERIALIZER = new Style.Serializer();
		
		@Override
		public NameEnchantedItem deserialize(JsonObject object, JsonDeserializationContext deserializationContext, LootItemCondition[] conditionsIn)
		{
			JsonElement minorStyleElement = object.get("minor_style");
			JsonElement majorStyleElement = object.get("major_style");
			Function<JsonElement,Style> styleDeserializer = element -> STYLE_SERIALIZER.deserialize(element, Style.class, deserializationContext);
			Optional<Style> minorStyle = Optional.ofNullable(minorStyleElement)
				.map(styleDeserializer);
			Optional<Style> majorStyle = Optional.ofNullable(majorStyleElement)
				.map(styleDeserializer);
			boolean ignoreEnchantments = GsonHelper.getAsBoolean(object, "ignore_enchantments", false);
			return new NameEnchantedItem(conditionsIn, minorStyle, majorStyle, ignoreEnchantments);
		}

		@Override
		public void serialize(JsonObject jsonObject, NameEnchantedItem lootFunction, JsonSerializationContext serializer)
		{
			super.serialize(jsonObject, lootFunction, serializer);
			lootFunction.minorStyle.ifPresent(style -> jsonObject.add("minor_style", STYLE_SERIALIZER.serialize(style, Style.class, serializer)));
			lootFunction.majorStyle.ifPresent(style -> jsonObject.add("major_style", STYLE_SERIALIZER.serialize(style, Style.class, serializer)));
			if (lootFunction.ignoreEnchantments)
			{
				jsonObject.addProperty("ignore_enchantments", true); // defaults to false, so we only serialize if true
			}
		}
	}
	
	public static MutableComponent getEpicName(ItemStack stack, LootContext context)
	{
		Random random = context.getRandom();
		Pair<MutableComponent,MutableComponent> words = getRandomWords(stack, random);
		return words.getLeft().copy()
			.append(new TextComponent(" "))
			.append(words.getRight().copy());
	}
	
	public static Pair<MutableComponent,MutableComponent> getRandomWords(ItemStack stack, Random rand)
	{
		Item item = stack.getItem();
		TagCollection<Item> tags = ItemTags.getAllTags();
		int indices = rand.nextInt(4);	// 0,1,2,3
		int first = indices / 2;			// 0,0,1,1 = prefix,prefix,noun,noun
		int second = (indices%2) + 1;		// 1,2,1,2 = noun  ,suffix,noun,suffix
		List<List<List<MutableComponent>>> lists = Looot.INSTANCE.wordMaps.stream()
			.map(map ->map.translationKeys.entrySet().stream() // stream of EntrySet<ResourceLocation,Set<IFormattableTextComponent>>
				// get all entries such that either the entry is the ALL entry or the entry is a valid tag that contains the item
				.filter(entry -> entry.getKey().equals(ALL) || isTagValidForItem(tags.getTag(entry.getKey()), item))
				.map(entry -> entry.getValue()) // stream of Set<IFormattableTextComponent>
				.collect(Collectors.toCollection(ArrayList<List<MutableComponent>>::new)))
			.collect(Collectors.toCollection(ArrayList<List<List<MutableComponent>>>::new));
		IntFunction<MutableComponent> getter = i -> RandomHelper.getRandomThingFromMultipleLists(rand, lists.get(i))
			.orElse(UNKNOWN_DESCRIPTOR);
		return Pair.of(getter.apply(first), getter.apply(second));
	
	}
	
	static boolean isTagValidForItem(@Nullable Tag<Item> tag, Item item)
	{
		return tag != null && tag.contains(item);
	}
}
