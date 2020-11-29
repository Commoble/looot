package commoble.looot.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;

import net.minecraft.resource.ResourceManager;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

public class NameListManager extends MergeableCodecDataManager<NameList, List<String>>
{

	/**
	 * Where the strings loaded from the data are of the form e.g. "word",
	 * the translation keys are of the form e.g. "modid.looot.namewords.prefixes.folders.subfolders.word"
	 */
	public Map<Identifier, List<MutableText>> translationKeys = new HashMap<>();
	public NameListManager(Identifier id, String folderName, Logger logger)
	{
		super(id, folderName, logger, NameList.CODEC, NameList::merge);
	}
	
	@Override
	public void apply(Map<Identifier, List<String>> processedData, ResourceManager resourceManager, Profiler profiler)
	{
		super.apply(processedData, resourceManager, profiler);
		
		this.translationKeys = this.bakeTranslationKeys();
	}
	
	protected Map<Identifier, List<MutableText>> bakeTranslationKeys()
	{
		Map<Identifier, List<MutableText>> output = new HashMap<>();
		this.data.forEach((id, words) ->
		{
			String modid = id.getNamespace();
			String path = id.getPath();
			String header = String.join(".", modid, this.folderName, path).replace("/", ".");
			List<MutableText> keyList = words.stream()
				.map(s -> new TranslatableText(header + "." + s))
				.collect(Collectors.toList());
			output.put(id, keyList);
		});
		return output;
	}

}
