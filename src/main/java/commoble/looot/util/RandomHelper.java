package commoble.looot.util;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import javax.annotation.Nullable;

public class RandomHelper
{
	@Nullable
	public static <T> T getRandomThingFrom(Random rand, List<T> things)
	{
		int size = things.size();
		return size > 0
			? things.get(rand.nextInt(size))
			: null;
	}
	
	@SafeVarargs
	@Nullable
	public static <T> T chooseRandomThing(Random rand, T...things)
	{
		int size = things.length;
		return size > 0
			? things[rand.nextInt(things.length)]
			: null;
	}
	
	/** Returns a random thing from a list of lists of things, weighted appropriate as if it were a single list of things,
	 * returns empty optional if all lists are empty 
	 * @param rand
	 * @param lists
	 * @return
	 */
	public static <T> Optional<T> getRandomThingFromMultipleLists(Random rand, List<? extends List<T>> lists)
	{
		// get sum of sizes of lists
		int totalSize = lists.stream().reduce(0, (size, list) -> size + list.size(), (sizeA,sizeB) -> sizeA+sizeB);
		int index = rand.nextInt(totalSize);
		for (List<T> list : lists)
		{
			int sizeOfThisList = list.size();
			if (index < sizeOfThisList)
			{
				return Optional.of(list.get(index));
			}
			index = index - sizeOfThisList;
		}
		return Optional.empty();
	}
}
