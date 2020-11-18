package commoble.looot.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;

import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class NameListManager extends MergeableCodecDataManager<NameList, List<String>>
{
	/**
	 * Where the strings loaded from the data are of the form e.g. "word",
	 * the translation keys are of the form e.g. "modid.looot.namewords.prefixes.folders.subfolders.word"
	 */
	public Map<ResourceLocation, List<IFormattableTextComponent>> translationKeys = new HashMap<>();
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
	
	protected Map<ResourceLocation, List<IFormattableTextComponent>> bakeTranslationKeys()
	{
		Map<ResourceLocation, List<IFormattableTextComponent>> output = new HashMap<>();
		this.data.forEach((id, words) ->
		{
			String modid = id.getNamespace();
			String path = id.getPath();
			String header = String.join(".", modid, this.folderName, path).replace("/", ".");
			List<IFormattableTextComponent> keyList = words.stream()
				.map(s -> new TranslationTextComponent(header + "." + s))
				.collect(Collectors.toList());
			output.put(id, keyList);
		});
		return output;
	}
}
