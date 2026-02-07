package commoble.looot.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record NameList(boolean replace, List<String> values)
{
	public static final Codec<NameList> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.BOOL.optionalFieldOf("replace", false).forGetter(NameList::replace),
			Codec.STRING.listOf().fieldOf("values").forGetter(NameList::values)
		).apply(instance, NameList::new));

	public NameList(final boolean replace, final List<String> values)
	{
		this.replace = replace;
		this.values = values;
	}
	
	public static List<String> merge(List<NameList> raws)
	{
		Set<String> set = new HashSet<>();
		for (NameList raw : raws)
		{
			if (raw.replace())
			{
				set = new HashSet<>();
			}
			set.addAll(raw.values());
		}
		
		List<String> out = new ArrayList<>(set.size());
		out.addAll(set);
		return out;
	}
	

}
