package commoble.looot.data;

import java.util.Set;

import org.apache.logging.log4j.Logger;

public class NameListManager extends MergeableCodecDataManager<NameList, Set<String>>
{
	public NameListManager(String folderName, Logger logger)
	{
		super(folderName, logger, NameList.CODEC, NameList::merge);
	}

	
}
