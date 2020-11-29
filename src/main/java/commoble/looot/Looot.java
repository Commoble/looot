package commoble.looot;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;

import commoble.looot.data.EnchantmentNameLimitManager;
import commoble.looot.data.NameListManager;
import commoble.looot.loot.ApplyFunctionsIfTagged;
import commoble.looot.loot.NameEnchantedItem;

import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;

public class Looot implements ModInitializer
{
	public static final String MODID = "looot";
	public static Looot INSTANCE = null;
	static Logger LOGGER = LogManager.getLogger(MODID);
	
	public EnchantmentNameLimitManager enchantmentNameLimits = new EnchantmentNameLimitManager();
	public NameListManager epicNamePrefixes = new NameListManager(new Identifier(MODID, "prefixes"), "looot/namewords/prefixes", LOGGER);
	public NameListManager epicNameNouns = new NameListManager(new Identifier(MODID, "nouns"), "looot/namewords/nouns", LOGGER);
	public NameListManager epicNameSuffixes = new NameListManager(new Identifier(MODID, "suffixes"), "looot/namewords/suffixes", LOGGER);
	public List<NameListManager> wordMaps = ImmutableList.of(this.epicNamePrefixes, this.epicNameNouns, this.epicNameSuffixes);

	@Override
	public void onInitialize()
	{
		INSTANCE = this;
		Registry.register(Registry.LOOT_FUNCTION_TYPE, ApplyFunctionsIfTagged.ID, ApplyFunctionsIfTagged.TYPE);
		Registry.register(Registry.LOOT_FUNCTION_TYPE, NameEnchantedItem.ID, NameEnchantedItem.TYPE);
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(this.enchantmentNameLimits);
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(this.epicNamePrefixes);
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(this.epicNameNouns);
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(this.epicNameSuffixes);
	}
}
