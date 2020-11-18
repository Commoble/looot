package commoble.looot.data;

import java.util.List;

import org.apache.logging.log4j.Logger;

public class NameListManager extends MergeableCodecDataManager<NameList, List<String>>
{
	public NameListManager(String folderName, Logger logger)
	{
		super(folderName, logger, NameList.CODEC, NameList::merge);
	}

	
}
