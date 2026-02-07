package net.commoble.looot.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

public class NameListManager extends MergeableCodecDataManager<NameList, List<String>>
{
	/**
	 * Where the strings loaded from the data are of the form e.g. "word",
	 * the translation keys are of the form e.g. "modid.looot.namewords.prefixes.folders.subfolders.word"
	 */
	public Map<Identifier, List<MutableComponent>> translationKeys = new HashMap<>();
	public NameListManager(String folderName)
	{
		super(folderName, NameList.CODEC, NameList::merge);
	}
	
	@Override
	protected void apply(Map<Identifier, List<String>> processedData, ResourceManager resourceManager, ProfilerFiller profiler)
	{
		super.apply(processedData, resourceManager, profiler);
		
		this.translationKeys = this.bakeTranslationKeys();
	}
	
	protected Map<Identifier, List<MutableComponent>> bakeTranslationKeys()
	{
		Map<Identifier, List<MutableComponent>> output = new HashMap<>();
		this.data.forEach((id, words) ->
		{
			String modid = id.getNamespace();
			String path = id.getPath();
			String header = String.join(".", modid, this.folderName(), path).replace("/", ".");
			List<MutableComponent> keyList = words.stream()
				.map(s -> Component.translatable(header + "." + s))
				.collect(Collectors.toList());
			output.put(id, keyList);
		});
		return output;
	}
}
