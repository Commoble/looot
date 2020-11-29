package commoble.looot.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import commoble.looot.Looot;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

public class EnchantmentNameLimitManager extends MergeableCodecDataManager<EnchantmentNameLimits, Map<Enchantment, Integer>>
{
	public static final Identifier ID = new Identifier(Looot.MODID, "enchantment_name_limits");
	public static final String FOLDER_NAME = "looot/enchantment_name_limits";
	static final Logger LOGGER = LogManager.getLogger();
	
	public @Nonnull Object2IntOpenHashMap<Enchantment> limits = new Object2IntOpenHashMap<>();

	public EnchantmentNameLimitManager()
	{
		super(ID, FOLDER_NAME, LOGGER, EnchantmentNameLimits.CODEC, EnchantmentNameLimitManager::mergeData);
	}

	@Override
	public void apply(Map<Identifier, Map<Enchantment, Integer>> processedData, ResourceManager resourceManager, Profiler profiler)
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
	
	static Object2IntOpenHashMap<Enchantment> combineMergedData(Map<Identifier, Map<Enchantment, Integer>> processedData)
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
