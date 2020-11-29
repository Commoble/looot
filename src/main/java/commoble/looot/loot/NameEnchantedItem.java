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

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import commoble.looot.Looot;
import commoble.looot.util.RandomHelper;
import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagGroup;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class NameEnchantedItem extends ConditionalLootFunction
{
	public static final Identifier ID = new Identifier(Looot.MODID, "name_enchanted_item");
	public static final LootFunctionType TYPE = new LootFunctionType(new NameEnchantedItem.Serializer());
	public static final Identifier ALL = new Identifier(Looot.MODID, "all");
	public static final Identifier UNKNOWN_ENCHANTMENT = new Identifier(Looot.MODID, "unknown_enchantment");
	public static final TranslatableText VERY_UNKNOWN_ENCHANTMENT_PREFIX = new TranslatableText("looot.unknown_enchantment.prefix");
	public static final TranslatableText VERY_UNKNOWN_ENCHANTMENT_SUFFIX = new TranslatableText("looot.unknown_enchantment.suffix");
	public static final TranslatableText UNKNOWN_DESCRIPTOR = new TranslatableText("looot.unknown_descriptor");
	public static final Style DEFAULT_MINOR_STYLE = Style.EMPTY.withFormatting(Formatting.AQUA);
	public static final Style DEFAULT_MAJOR_STYLE = Style.EMPTY.withFormatting(Formatting.LIGHT_PURPLE);
	
	protected final boolean ignoreEnchantments; // if true, will use the "epic name" regardless of the item's enchantments
	protected final Optional<Style> minorStyle;	// style to be used for 1-2 enchantment items, defaults to aqua text
	protected final Optional<Style> majorStyle;	// style to be used for 3+ enchantment items, defaults to light purple text
	
	public NameEnchantedItem(LootCondition[] conditionsIn, Optional<Style> minorStyle, Optional<Style> majorStyle, boolean ignoreEnchantments)
	{
		super(conditionsIn);
		this.minorStyle = minorStyle;
		this.majorStyle = majorStyle;
		this.ignoreEnchantments = ignoreEnchantments;
	}

	@Override
	public LootFunctionType getType() {
		return TYPE;
	}
	
	public static MutableText getNameForEnchantment(boolean isPrefix, Enchantment enchantment, int level, Random rand)
	{
		// check the defined enchantment name limits for the given enchantment
		int maxKnownLevel = Looot.INSTANCE.enchantmentNameLimits.limits.getOrDefault(enchantment, 0);
		
		// cap the given level by the name limit
		int highestNameableLevel = Math.min(maxKnownLevel, level);
		
		// if a mod has declared that they are suppling names for this enchantment, use the appropriate translation key for it
		if (highestNameableLevel > 0)
		{
			String position = isPrefix ? ".prefix." : ".suffix.";
			return new TranslatableText(enchantment.getTranslationKey()+position+level);
		}
		else
		{
			// no explicit names for this enchantment, use a fallback table
			List<MutableText> names = isPrefix
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
	protected ItemStack process(ItemStack stack, LootContext context)
	{
		Map<Enchantment, Integer> enchantments = EnchantmentHelper.get(stack);
		BinaryOperator<Map.Entry<Enchantment, Integer>> biggestReducer = (a,b) -> b.getValue() > a.getValue() ? b : a;
//		BinaryOperator<Map.Entry<Enchantment, Integer>> smallestReducer = (a,b) -> b.getValue() < a.getValue() ? b : a;
		
		Random rand = context.getRandom();
//		Function<Boolean, Function<? super Map.Entry<Enchantment, Integer>, ? extends MutableText>> mapperGetter =
////			position -> entry -> new TranslatableText(entry.getKey().getName()+position+entry.getValue().toString());
//			position -> entry -> getNameForEnchantment(position, entry.getKey(), entry.getValue(), rand);
		
		// if number of enchantments is at least three, generate an epic name and ignore the three smallest enchantments in the next phase
		int enchantmentCount = enchantments.size();
		if (this.ignoreEnchantments || enchantmentCount > 2)
		{
			stack.setCustomName(getEpicName(stack, context).fillStyle(this.majorStyle.orElse(DEFAULT_MAJOR_STYLE)));
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
			
			Optional<MutableText> maybePrefix = twoBiggest.getLeft().map(entry -> getNameForEnchantment(true, entry.getKey(), entry.getValue(), rand));
			Optional<MutableText> maybeSuffix = twoBiggest.getRight().map(entry -> getNameForEnchantment(false, entry.getKey(), entry.getValue(), rand));
			
			Text stackText = stack.getName();
			if (stackText instanceof MutableText)
			{
				MutableText formattableStackText = (MutableText)stackText;
				MutableText prefixedStackText = maybePrefix.map(prefix -> prefix.append(" ").append(formattableStackText))
					.orElse(formattableStackText);
				MutableText suffixedStackText = maybeSuffix.map(suffix -> prefixedStackText.append(" ").append(suffix))
					.orElse(prefixedStackText);
				stack.setCustomName(suffixedStackText.fillStyle(this.minorStyle.orElse(DEFAULT_MINOR_STYLE)));
			}
		}

		return stack;
	}

	public static class Serializer extends ConditionalLootFunction.Serializer<NameEnchantedItem>
	{
		public static final Style.Serializer STYLE_SERIALIZER = new Style.Serializer();
		
		@Override
		public NameEnchantedItem fromJson(JsonObject object, JsonDeserializationContext deserializationContext, LootCondition[] conditionsIn)
		{
			JsonElement minorStyleElement = object.get("minor_style");
			JsonElement majorStyleElement = object.get("major_style");
			Function<JsonElement,Style> styleDeserializer = element -> STYLE_SERIALIZER.deserialize(element, Style.class, deserializationContext);
			Optional<Style> minorStyle = Optional.ofNullable(minorStyleElement)
				.map(styleDeserializer);
			Optional<Style> majorStyle = Optional.ofNullable(majorStyleElement)
				.map(styleDeserializer);
			boolean ignoreEnchantments = JsonHelper.getBoolean(object, "ignore_enchantments", false);
			return new NameEnchantedItem(conditionsIn, minorStyle, majorStyle, ignoreEnchantments);
		}

		@Override
		public void toJson(JsonObject jsonObject, NameEnchantedItem lootFunction, JsonSerializationContext serializer)
		{
			super.toJson(jsonObject, lootFunction, serializer);
			lootFunction.minorStyle.ifPresent(style -> jsonObject.add("minor_style", STYLE_SERIALIZER.serialize(style, Style.class, serializer)));
			lootFunction.majorStyle.ifPresent(style -> jsonObject.add("major_style", STYLE_SERIALIZER.serialize(style, Style.class, serializer)));
			if (lootFunction.ignoreEnchantments)
			{
				jsonObject.addProperty("ignore_enchantments", true); // defaults to false, so we only serialize if true
			}
		}
	}
	
	public static MutableText getEpicName(ItemStack stack, LootContext context)
	{
		Random random = context.getRandom();
		Pair<MutableText,MutableText> words = getRandomWords(stack, random);
		return words.getLeft()
			.append(new LiteralText(" "))
			.append(words.getRight());
	}
	
	public static Pair<MutableText,MutableText> getRandomWords(ItemStack stack, Random rand)
	{
		Item item = stack.getItem();
		TagGroup<Item> tags = ItemTags.getTagGroup();
		int indices = rand.nextInt(4);	// 0,1,2,3
		int first = indices / 2;			// 0,0,1,1 = prefix,prefix,noun,noun
		int second = (indices%2) + 1;		// 1,2,1,2 = noun  ,suffix,noun,suffix
		List<List<List<MutableText>>> lists = Looot.INSTANCE.wordMaps.stream()
			.map(map ->map.translationKeys.entrySet().stream() // stream of EntrySet<Identifier,Set<MutableText>>
				// get all entries such that either the entry is the ALL entry or the entry is a valid tag that contains the item
				.filter(entry -> entry.getKey().equals(ALL) || isTagValidForItem(tags.getTag(entry.getKey()), item))
				.map(Map.Entry::getValue) // stream of Set<MutableText>
				.collect(Collectors.toCollection(ArrayList<List<MutableText>>::new)))
			.collect(Collectors.toCollection(ArrayList<List<List<MutableText>>>::new));
		IntFunction<MutableText> getter = i -> RandomHelper.getRandomThingFromMultipleLists(rand, lists.get(i))
			.orElse(UNKNOWN_DESCRIPTOR);
		return Pair.of(getter.apply(first), getter.apply(second));
	
	}
	
	static boolean isTagValidForItem(@Nullable Tag<Item> tag, Item item)
	{
		return tag != null && tag.contains(item);
	}
}
