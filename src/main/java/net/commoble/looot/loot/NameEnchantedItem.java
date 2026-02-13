package net.commoble.looot.loot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.commoble.looot.Looot;
import net.commoble.looot.RandomHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.registries.DeferredHolder;

public class NameEnchantedItem extends LootItemConditionalFunction
{
	public static final Identifier ALL = Looot.id("all");
	public static final Identifier UNKNOWN_ENCHANTMENT = Looot.id("unknown_enchantment");
	public static final String VERY_UNKNOWN_ENCHANTMENT_PREFIX = "looot.unknown_enchantment.prefix";
	public static final String VERY_UNKNOWN_ENCHANTMENT_SUFFIX = "looot.unknown_enchantment.suffix";
	public static final String UNKNOWN_DESCRIPTOR = "looot.unknown_descriptor";
	public static final Style DEFAULT_MINOR_STYLE = Style.EMPTY.applyFormat(ChatFormatting.AQUA).withItalic(false);
	public static final Style DEFAULT_MAJOR_STYLE = Style.EMPTY.applyFormat(ChatFormatting.LIGHT_PURPLE).withItalic(false);
	
	public static final ResourceKey<MapCodec<? extends LootItemFunction>> KEY = ResourceKey.create(Registries.LOOT_FUNCTION_TYPE, Looot.id("name_enchanted_item"));
	public static final DeferredHolder<MapCodec<? extends LootItemFunction>, MapCodec<NameEnchantedItem>> HOLDER = DeferredHolder.create(KEY);
	
	public static final MapCodec<NameEnchantedItem> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
			LootItemCondition.TYPED_CODEC.listOf().optionalFieldOf("conditions", List.of()).forGetter(f -> f.predicates),
			Style.Serializer.CODEC.optionalFieldOf("minor_style").forGetter(NameEnchantedItem::minorStyle),
			Style.Serializer.CODEC.optionalFieldOf("major_style").forGetter(NameEnchantedItem::majorStyle),
			Codec.BOOL.optionalFieldOf("ignore_enchantments", false).forGetter(NameEnchantedItem::ignoreEnchantments)
		).apply(builder, NameEnchantedItem::new));
	
	protected final boolean ignoreEnchantments; // if true, will use the "epic name" regardless of the item's enchantments
	protected final Optional<Style> minorStyle;	// style to be used for 1-2 enchantment items, defaults to aqua text
	protected final Optional<Style> majorStyle;	// style to be used for 3+ enchantment items, defaults to light purple text
	
	public NameEnchantedItem(List<LootItemCondition> conditions, Optional<Style> minorStyle, Optional<Style> majorStyle, boolean ignoreEnchantments)
	{
		super(conditions);
		this.minorStyle = minorStyle;
		this.majorStyle = majorStyle;
		this.ignoreEnchantments = ignoreEnchantments;
	}
	
	public Optional<Style> minorStyle()
	{
		return this.minorStyle;
	}
	
	public Optional<Style> majorStyle()
	{
		return this.majorStyle;
	}
	
	public boolean ignoreEnchantments()
	{
		return this.ignoreEnchantments;
	}

	@Override
	public MapCodec<? extends LootItemConditionalFunction> codec()
	{
		return CODEC;
	}

	public static MutableComponent getNameForEnchantment(boolean isPrefix, Holder<Enchantment> enchantment, int level, RandomSource rand)
	{
		// check the defined enchantment name limits for the given enchantment
		int maxKnownLevel = Objects.requireNonNullElse(enchantment.getData(Looot.ENCHANTMENT_NAME_LIMITS), 0);
		
		// cap the given level by the name limit
		int highestNameableLevel = Math.min(maxKnownLevel, level);
		
		// if a mod has declared that they are suppling names for this enchantment, use the appropriate translation key for it
		if (highestNameableLevel > 0)
		{
			String position = isPrefix ? "prefix" : "suffix";
			Identifier id = enchantment.unwrapKey().get().identifier();
			Enchantment.getFullname(enchantment, level);
			return Component.translatable(String.format("enchantment.%s.%s.%s.%d", id.getNamespace(), id.getPath(), position, level));
		}
		else
		{
			// no explicit names for this enchantment, use a fallback table
			List<MutableComponent> names = isPrefix
				? Looot.EPIC_NAME_PREFIXES.translationKeys.get(ALL)
				: Looot.EPIC_NAME_SUFFIXES.translationKeys.get(UNKNOWN_ENCHANTMENT);
			if (names.size() > 0)
			{
				return RandomHelper.getRandomThingFrom(rand, names).copy();
			}
			else
			{
				return isPrefix
					? Component.translatable(VERY_UNKNOWN_ENCHANTMENT_PREFIX)
					: Component.translatable(VERY_UNKNOWN_ENCHANTMENT_SUFFIX);
			}
		}
	}

	@Override
	protected ItemStack run(ItemStack stack, LootContext context)
	{
		ItemEnchantments enchantments = stack.getAllEnchantments(context.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT));
		BinaryOperator<Object2IntMap.Entry<Holder<Enchantment>>> biggestReducer = (a,b) -> b.getIntValue() > a.getIntValue() ? b : a;
		
		RandomSource rand = context.getRandom();
		
		// if number of enchantments is at least three, generate an epic name and ignore the three smallest enchantments in the next phase
		int enchantmentCount = enchantments.size();
		if (this.ignoreEnchantments || enchantmentCount > 2)
		{
			stack.set(DataComponents.CUSTOM_NAME, getEpicName(stack, context).withStyle(this.majorStyle.orElse(DEFAULT_MAJOR_STYLE)));
		}
		else if (enchantmentCount > 0) // 1, or 2 enchantments
		{
			// get the two biggest (if any) and generate prefix/suffix based on them
			Optional<Object2IntMap.Entry<Holder<Enchantment>>> biggest = enchantments.entrySet().stream()
				.reduce(biggestReducer);
			
			Optional<Object2IntMap.Entry<Holder<Enchantment>>> secondBiggest = enchantments.entrySet().stream()
				.filter(entry -> entry.getKey() != biggest.get().getKey())	// if biggest is empty then this won't be evaluated
				.reduce(biggestReducer);
			
			Pair<Optional<Object2IntMap.Entry<Holder<Enchantment>>>, Optional<Object2IntMap.Entry<Holder<Enchantment>>>> twoBiggest = context.getRandom().nextBoolean()
				? Pair.of(biggest, secondBiggest)
				: Pair.of(secondBiggest, biggest);
			
			Optional<MutableComponent> maybePrefix = twoBiggest.getLeft().map(entry -> getNameForEnchantment(true, entry.getKey(), entry.getIntValue(), rand));
			Optional<MutableComponent> maybeSuffix = twoBiggest.getRight().map(entry -> getNameForEnchantment(false, entry.getKey(), entry.getIntValue(), rand));
			
			Component stackText = stack.getItemName();
			MutableComponent formattableStackText = stackText.copy();
			MutableComponent prefixedStackText = maybePrefix.map(prefix -> prefix.append(" ").append(formattableStackText))
				.orElse(formattableStackText);
			MutableComponent suffixedStackText = maybeSuffix.map(suffix -> prefixedStackText.append(" ").append(suffix))
				.orElse(prefixedStackText);
			stack.set(DataComponents.CUSTOM_NAME, suffixedStackText.withStyle(this.minorStyle.orElse(DEFAULT_MINOR_STYLE)));
		}

		return stack;
	}
	
	public static MutableComponent getEpicName(ItemStack stack, LootContext context)
	{
		RandomSource random = context.getRandom();
		Pair<MutableComponent,MutableComponent> words = getRandomWords(stack, random);
		return words.getLeft().copy()
			.append(Component.literal(" "))
			.append(words.getRight().copy());
	}
	
	public static Pair<MutableComponent,MutableComponent> getRandomWords(ItemStack stack, RandomSource rand)
	{
		int indices = rand.nextInt(4);	// 0,1,2,3
		int first = indices / 2;			// 0,0,1,1 = prefix,prefix,noun,noun
		int second = (indices%2) + 1;		// 1,2,1,2 = noun  ,suffix,noun,suffix
		List<List<List<MutableComponent>>> lists = Looot.WORD_MAPS.stream()
			.map(map ->map.translationKeys.entrySet().stream() // stream of EntrySet<ResourceLocation,Set<IFormattableTextComponent>>
				// get all entries such that either the entry is the ALL entry or the entry is a valid tag that contains the item
				.filter(entry -> entry.getKey().equals(ALL) || stack.is(TagKey.create(Registries.ITEM, entry.getKey())))
				.map(Entry::getValue) // stream of Set<IFormattableTextComponent>
				.collect(Collectors.toCollection(ArrayList<List<MutableComponent>>::new)))
			.collect(Collectors.toCollection(ArrayList<List<List<MutableComponent>>>::new));
		IntFunction<MutableComponent> getter = i -> RandomHelper.getRandomThingFromMultipleLists(rand, lists.get(i))
			.orElse(Component.translatable(UNKNOWN_DESCRIPTOR));
		return Pair.of(getter.apply(first), getter.apply(second));
	
	}
}
