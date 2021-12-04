package commoble.looot.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.resources.ResourceLocation;

public class EnchantmentNameLimitManager extends MergeableCodecDataManager<EnchantmentNameLimits, Map<Enchantment, Integer>>
{	
	public static final String FOLDER_NAME = "looot/enchantment_name_limits";
	static final Logger LOGGER = LogManager.getLogger();
	
	public @Nonnull Object2IntOpenHashMap<Enchantment> limits = new Object2IntOpenHashMap<>();

	public EnchantmentNameLimitManager()
	{
		super(FOLDER_NAME, LOGGER, EnchantmentNameLimits.CODEC, EnchantmentNameLimitManager::mergeData);
	}

	@Override
	protected void apply(Map<ResourceLocation, Map<Enchantment, Integer>> processedData, ResourceManager resourceManager, ProfilerFiller profiler)
	{
		super.apply(processedData, resourceManager, profiler);
		this.limits = combineMergedData(processedData);		
	}
	
	static Map<Enchantment, Integer> mergeData(List<EnchantmentNameLimits> raws)
	{
		Map<Enchantment, Integer> map = new HashMap<>();
		for(EnchantmentNameLimits raw : raws)
		{
			Map<Enchantment, Integer> rawMap = raw.getValues();
			if (raw.getReplace())
			{
				map = rawMap;
			}
			else
			{
				mergeSubMapIntoFinalMap(map, rawMap);
			}
		}
		
		return map;
	}
	
	static Object2IntOpenHashMap<Enchantment> combineMergedData(Map<ResourceLocation, Map<Enchantment, Integer>> processedData)
	{
		Object2IntOpenHashMap<Enchantment> data = new Object2IntOpenHashMap<>();
		processedData.values().forEach(subMap -> mergeSubMapIntoFinalMap(data, subMap));
		return data;
	}
	
	static void mergeSubMapIntoFinalMap(Map<Enchantment, Integer> data, Map<Enchantment, Integer> subMap)
	{
		subMap.forEach((key, value) -> data.merge(key, value, Math::max));
	}
}
