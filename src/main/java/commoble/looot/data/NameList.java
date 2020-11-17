package commoble.looot.data;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class NameList
{
	public static final Codec<NameList> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.BOOL.optionalFieldOf("replace", false).forGetter(NameList::getReplace),
			Codec.STRING.listOf().fieldOf("values").forGetter(NameList::getValues)
		).apply(instance, NameList::new));

	private final boolean replace;

	public boolean getReplace()
	{
		return this.replace;
	}

	private final List<String> values;

	public List<String> getValues()
	{
		return this.values;
	}

	public NameList(final boolean replace, final List<String> values)
	{
		this.replace = replace;
		this.values = values;
	}
	
	public static Set<String> merge(List<NameList> raws)
	{
		Set<String> set = new HashSet<>();
		for (NameList raw : raws)
		{
			if (raw.getReplace())
			{
				set = new HashSet<>();
			}
			set.addAll(raw.getValues());
		}
		
		return set;
	}
	

}
