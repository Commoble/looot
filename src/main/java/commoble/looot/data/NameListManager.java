package commoble.looot.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;

import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

public class NameListManager extends MergeableCodecDataManager<NameList, List<String>>
{
	/**
	 * Where the strings loaded from the data are of the form e.g. "word",
	 * the translation keys are of the form e.g. "modid.looot.namewords.prefixes.folders.subfolders.word"
	 */
	public Map<ResourceLocation, List<String>> translationKeys = new HashMap<>();
	public NameListManager(String folderName, Logger logger)
	{
		super(folderName, logger, NameList.CODEC, NameList::merge);
	}
	
	@Override
	protected void apply(Map<ResourceLocation, List<String>> processedData, IResourceManager resourceManager, IProfiler profiler)
	{
		super.apply(processedData, resourceManager, profiler);
		
		this.translationKeys = this.bakeTranslationKeys();
	}
	
	protected Map<ResourceLocation, List<String>> bakeTranslationKeys()
	{
		Map<ResourceLocation, List<String>> output = new HashMap<>();
		this.data.forEach((id, words) ->
		{
			String modid = id.getNamespace();
			String path = id.getPath();
			String header = String.join(".", modid, this.folderName, path).replace("/", ".");
			List<String> keyList = words.stream()
				.map(s -> header + "." + s)
				.collect(Collectors.toList());
			output.put(id, keyList);
		});
		return output;
	}
}
