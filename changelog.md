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