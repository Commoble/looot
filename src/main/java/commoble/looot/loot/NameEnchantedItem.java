package commoble.looot.loot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.BinaryOperator;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import commoble.looot.Looot;
import commoble.looot.util.RandomHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootFunction;
import net.minecraft.loot.LootFunctionType;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ITagCollection;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class NameEnchantedItem extends LootFunction
{
	public static final ResourceLocation ID = new ResourceLocation(Looot.MODID, "name_enchanted_item");
	public static final LootFunctionType TYPE = new LootFunctionType(new NameEnchantedItem.Serializer());
	public static final ResourceLocation ALL = new ResourceLocation(Looot.MODID, "all");
	public static final ResourceLocation UNKNOWN_ENCHANTMENT = new ResourceLocation(Looot.MODID, "unknown_enchantment");
	public static final TranslationTextComponent VERY_UNKNOWN_ENCHANTMENT_PREFIX = new TranslationTextComponent("looot.unknown_enchantment.prefix");
	public static final TranslationTextComponent VERY_UNKNOWN_ENCHANTMENT_SUFFIX = new TranslationTextComponent("looot.unknown_enchantment.suffix");
	public static final TranslationTextComponent UNKNOWN_DESCRIPTOR = new TranslationTextComponent("looot.unknown_descriptor");
	
	public NameEnchantedItem(ILootCondition[] conditionsIn)
	{
		super(conditionsIn);
	}

	@Override
	public LootFunctionType getFunctionType()
	{
		return TYPE;
	}
	
	public static IFormattableTextComponent getNameForEnchantment(boolean isPrefix, Enchantment enchantment, int level, Random rand)
	{
		// check the defined enchantment name limits for the given enchantment
		int maxKnownLevel = Looot.INSTANCE.enchantmentNameLimits.limits.getOrDefault(enchantment, 0);
		
		// cap the given level by the name limit
		int highestNameableLevel = Math.min(maxKnownLevel, level);
		
		// if a mod has declared that they are suppling names for this enchantment, use the appropriate translation key for it
		if (highestNameableLevel > 0)
		{
			String position = isPrefix ? ".prefix." : ".suffix.";
			return new TranslationTextComponent(enchantment.getName()+position+level);
		}
		else
		{
			// no explicit names for this enchantment, use a fallback table
			List<IFormattableTextComponent> names = isPrefix
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
	protected ItemStack doApply(ItemStack stack, LootContext context)
	{
		Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
		BinaryOperator<Map.Entry<Enchantment, Integer>> biggestReducer = (a,b) -> b.getValue() > a.getValue() ? b : a;
//		BinaryOperator<Map.Entry<Enchantment, Integer>> smallestReducer = (a,b) -> b.getValue() < a.getValue() ? b : a;
		
		Random rand = context.getRandom();
//		Function<Boolean, Function<? super Map.Entry<Enchantment, Integer>, ? extends IFormattableTextComponent>> mapperGetter =
////			position -> entry -> new TranslationTextComponent(entry.getKey().getName()+position+entry.getValue().toString());
//			position -> entry -> getNameForEnchantment(position, entry.getKey(), entry.getValue(), rand);
		
		// if number of enchantments is at least three, generate an epic name and ignore the three smallest enchantments in the next phase
		if (enchantments.size() > 2)
		{
			stack.setDisplayName(getEpicName(stack, context).mergeStyle(TextFormatting.LIGHT_PURPLE));
		}
		else // 0, 1, or 2 enchantments
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
			
			Optional<IFormattableTextComponent> maybePrefix = twoBiggest.getLeft().map(entry -> getNameForEnchantment(true, entry.getKey(), entry.getValue(), rand));
			Optional<IFormattableTextComponent> maybeSuffix = twoBiggest.getRight().map(entry -> getNameForEnchantment(false, entry.getKey(), entry.getValue(), rand));
			
			ITextComponent stackText = stack.getDisplayName();
			if (stackText instanceof IFormattableTextComponent)
			{
				IFormattableTextComponent formattableStackText = (IFormattableTextComponent)stackText;
				IFormattableTextComponent prefixedStackText = maybePrefix.map(prefix -> prefix.appendString(" ").append(formattableStackText))
					.orElse(formattableStackText);
				IFormattableTextComponent suffixedStackText = maybeSuffix.map(suffix -> prefixedStackText.appendString(" ").append(suffix))
					.orElse(prefixedStackText);
				stack.setDisplayName(suffixedStackText);
			}
		}

		return stack;
	}

	public static class Serializer extends LootFunction.Serializer<NameEnchantedItem>
	{
		@Override
		public NameEnchantedItem deserialize(JsonObject object, JsonDeserializationContext deserializationContext, ILootCondition[] conditionsIn)
		{
			return new NameEnchantedItem(conditionsIn);
		}
		
	}
	
	public static IFormattableTextComponent getEpicName(ItemStack stack, LootContext context)
	{
		Random random = context.getRandom();
		Pair<IFormattableTextComponent,IFormattableTextComponent> words = getRandomWords(stack, random);
		return words.getLeft()
			.append(new StringTextComponent(" "))
			.append(words.getRight());
	}
	
	public static Pair<IFormattableTextComponent,IFormattableTextComponent> getRandomWords(ItemStack stack, Random rand)
	{
		Item item = stack.getItem();
		ITagCollection<Item> tags = ItemTags.getCollection();
		int indices = rand.nextInt(4);	// 0,1,2,3
		int first = indices / 2;			// 0,0,1,1 = prefix,prefix,noun,noun
		int second = (indices%2) + 1;		// 1,2,1,2 = noun  ,suffix,noun,suffix
		List<List<List<IFormattableTextComponent>>> lists = Looot.INSTANCE.wordMaps.stream()
			.map(map ->map.translationKeys.entrySet().stream() // stream of EntrySet<ResourceLocation,Set<IFormattableTextComponent>>
				// get all entries such that either the entry is the ALL entry or the entry is a valid tag that contains the item
				.filter(entry -> entry.getKey().equals(ALL) || isTagValidForItem(tags.get(entry.getKey()), item))
				.map(entry -> entry.getValue()) // stream of Set<IFormattableTextComponent>
				.collect(Collectors.toCollection(ArrayList<List<IFormattableTextComponent>>::new)))
			.collect(Collectors.toCollection(ArrayList<List<List<IFormattableTextComponent>>>::new));
		IntFunction<IFormattableTextComponent> getter = i -> RandomHelper.getRandomThingFromMultipleLists(rand, lists.get(i))
			.orElse(UNKNOWN_DESCRIPTOR);
		return Pair.of(getter.apply(first), getter.apply(second));
	
	}
	
	static boolean isTagValidForItem(@Nullable ITag<Item> tag, Item item)
	{
		return tag != null && tag.contains(item);
	}
}
