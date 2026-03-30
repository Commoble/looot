## 26.1.0.0
* Updated to MC 26.1 (requires neoforge 26.1.0.17-beta or higher)
* Removed `looot:add_table` loot modifier as it has been superceded by neoforge's `neoforge:add_table` loot modifier
* Renamed `looot:apply_functions_if_tagged` to `looot:apply_functions_to_items`, it now accepts a holderset instead of a tag:
```json
{
	"type": "looot:apply_functions_if_tagged",
	"conditions: [
		// list of conditions, optional field
	],
	"items": "#tag_id", // accepts an item id, list of item ids, or tag id hashtag
	"functions": [
		// list of function objects
	]
}
```
* Enchantment name limits are now a datamap under `data/looot/data_maps/enchantment/name_limits.json`, file format has not changed
* Updated lots of tags to c system
* Added new datapack registry looot/artifact
	* Files in this registry have paths `data/modid/looot/artifact/path.json` whose object would have id `modid:path`
	* This is a datapack registry and therefore supports tags but not /reload
	* Objects in this registry are LootPoolEntryContainers and have the same format as loot pool entries; for example:
```json
{
	"type": "minecraft:item",
	"name": "minecraft:stone_sword",
	"functions": [
		// etc
	]
}
```
* Added loot pool entry type `looot:artifacts` which specifies one or more artifacts, randomly selects one, and uses it to generate loot

```json
{
	"type": "looot:artifacts",
	"artifacts": "#modid:common_artifacts", // can be an artifact id, list of ids, or #tag id
	"weight": 1
}
```

* Added `/looot give_artifact` command to give a specified artifact to a specified player
* Added entity sub-predicate type "looot:dead" which matches entities which are dead
* Added enchantment entity effect type "looot:add_food" which adds (or subtracts) food/saturation from affected players:

```json
{
	"type": "looot:add_food",
	"nutrition": {"type": "linear", "base": 1, "per_level_above_first": 1}, // how many hunger points to restore to the player (half-drumsticks). May be negative to subtract instead of add.
	"saturation": {"type": "linear", "base": 0.2, "per_level_above_first": 0.2} // how much saturation to restore to the player (or subtract, if negative)
}
```

* Added enchantment entity effect type "looot:affect_nearby_entities" which applies a specified entity effect to entities around the target entity:

```json
{
	"type": "looot:affect_nearby_entities",
	"radius": 5.0, // radius in blocks / meters to apply affect
	"spherical": true, // if true, applies in a sphere; defaults false if not specified, applying in a cube instead
	"include_target": true, // defaults false if not specified
	"keep_original_position": true, // if true, applies affects using position of primary target instead of area targets; defaults false if not specified
	"predicate": {
		// optional EntityPredicate object to filter entities in area
	},
	"effect": {
		// sub-effect to apply to nearby entities
	}
}
```

* Added enchantment entity effect type "looot:apply_mob_effect_better", which is similar to the vanilla apply_mob_effect effect but with additional fields:

```json
{
	"type": "looot:apply_mob_effect_better",
	"to_apply": "modid:some_mob_effect", // can be a holderset
	"min_duration": 10.0, // min duration of effect in seconds, may be a LevelBasedValue
	"max_duration": 10.0, // max duration of effect in seconds, may be a LevelBasedValue
	"min_amplifier": 0.0,	// min level of effect, may be a LevelBasedValue
	"max_amplifier": 0.0,	// max level of effect, may be a LevelBasedValue
	"ambient": false,	// optional, defaults false; affects display of icon
	"visible": true,	// optional, defaults true; whether to show effect particles
	"show_icon": true,	// optional, defaults true
	"hidden_effect": { // optional sub-ApplyMobEffectBetter object
		"to_apply": "etc",
		// if present, generates another effect to replace the primary effect when it expires
	}
}
```

## 1.20.1-1.2.0.4
* Fix infinite loop in add_table loot modifier
* Fix #forge:weapons tag to refer to vanilla #swords tag instead of no-longer-existing #forge:tools/swords

## 1.20.1-1.2.0.3
* Fixed TagLoader error on server start due to forge:tools/polearms tag referencing forge:tools/axes and forge:tools/hoes tags, which no longer exist. Polearms tag now references minecraft:axes and minecraft:hoes tags.

## 1.20.1-1.2.0.2
* Updated to Minecraft 1.20.1

## 1.19.2-1.2.0.1
* Fixed crash on server start due to MergeableCodecDataManager referring to missing ImmutableMap class

## 1.19.2-1.2.0.0
* Updated to 1.19.2. Now requires forge 43.1.0 or higher.
* Added "looot:add_table" global loot modifier, which rolls loot from a specified secondary loot table and adds the result to the target loot. Example loot modifier json that adds loot to pillager outpost chests:
```
{
	"type": "looot:add_table",
	"conditions":
	[
		{
			"condition": "forge:loot_table_id",
			"loot_table_id": "minecraft:chests/pillager_outpost"
		}
	],
	"table": "workshopsofdoom:subtables/extra_pillager_outpost_loot"
}
```
* Removed the following tags as forge now provides equivalent or similar tags:
  * forge:armor -> superceded by the forge:armors tag included with forge
  * forge:axes -> forge:tools/axes
  * forge:boots -> forge:armors/boots
  * forge:bows -> forge:tools/bows
  * forge:chestplates -> forge:armors/chestplates
  * forge:crossbows -> forge:tools/crossbows
  * forge:helmets -> forge:armors/helmets
  * forge:hoes -> forge:tools/hoes
  * forge:leggings -> forge:armors/leggings
  * forge:pickaxes -> forge:tools/pickaxes
  * forge:rods/fishing -> forge:tools/fishing_rods
  * forge:shovels -> forge:tools/shovels
  * forge:swords -> forge:tools/swords
  * forge:tools -> forge:tools (be aware that forge's version of this tag includes swords, bows, crossbows, and tridents, but lacks flint and steel and shears)
  * forge:tridents -> forge:tools/tridents
* Renamed the following tags:
  * forge:armor/chainmail -> forge:armors/chainmail
  * forge:armor/diamond -> forge:armors/diamond
  * forge:armor/golden -> forge:armors/golden
  * forge:armor/iron -> forge:armors/iron
  * forge:armor/leather -> forge:armors/leather
  * forge:armor/netherite -> forge:armors/netherite
  * forge:boots_and_leggings -> forge:armors/boots_and_leggings
  * forge:chestplates_and_helmets -> forge:armors/chestplates_and_helmets
  * forge:missile_weapons -> forge:tools/missile_weapons
  * forge:polearms -> forge:tools/polearms

## 1.18.2-1.1.1.0
* Updated to 1.18.2. Now requires forge 40.1.0 or higher. Be aware that previous versions of looot will not work on 1.18.2, and looot 1.1.1.0 will not work on minecraft 1.18.1 or older.

## 1.18-1.1.0.0
* Updated to 1.18
* Random generated names are no longer italic

## 1.16.4-1.0.0.3
* Fixed a stack overflow crash that could occur if the epic name generator chose the same word twice

## 1.16.4-1.0.0.2
* Added minecraft:power to nameable enchantments

## 1.16.4-1.0.0.1
* Added fishing rods to forge:rods/fishing item tag so the name generator generates fishing-rod-specific names for fishing rods

## 1.16.4-1.0.0.0
* Made exist