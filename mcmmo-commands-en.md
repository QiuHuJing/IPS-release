# McMMO Commands

McMMO is a classic RPG skill plugin.
It takes core Minecraft game mechanics and expands them into a broader and higher-quality RPG experience.

## Command List

| Command | Parameters | Description | Example |
| :-: | :-: | :-: | :-: |
| `/mcability` | None | Toggle McMMO abilities on/off | - |
| `/mcstats` | None | Show your McMMO skill levels | - |
| `/mcrank` | None | Show your McMMO skill rankings | - |
| `/mctop` | `[skill] [page]` | Show leaderboard for a skill (or power level if none) | `/mctop Mining` |
| `/mcsb` | `<keep \| clear>` | Toggle sidebar scoreboard on/off | - |
| `/<skill>` | - | View skill details | `/Acrobatics` |
| `/<skill>` | `? [page]` | View skill guide | `/Acrobatics ? 2` |

## How to Use

Currently, McMMO has 15 skills, of which **Salvage** and **Smelting** are child skills and cannot be directly leveled.

Different skills grant different abilities. Active abilities require an action to trigger and generally have a cooldown. Passive abilities typically trigger under specific conditions at a certain probability. Skills may require leveling up to unlock abilities. By practicing the corresponding skill, you can increase its level — the higher the level, the longer active ability duration and the higher passive proc rate.

?> In addition to the above, you can also use Mint Coins to purchase McMMO single-skill levels. 1 Mint Coin = 50 levels in any skill. Additionally, Weekend Double XP and XP Boosters can also increase skill XP gain, up to 10x.

Use `/mcability` to **enable/disable** skill activation.

## Skill List

+ [Power Level](#power-level)
+ [Acrobatics](#acrobatics)
+ [Alchemy](#alchemy)
+ [Archery](#archery)
+ [Axes](#axes)
+ [Excavation](#excavation)
+ [Fishing](#fishing)
+ [Herbalism](#herbalism)
+ [Mining](#mining)
+ [Repair](#repair)
+ [Swords](#swords)
+ [Taming](#taming)
+ [Unarmed](#unarmed)
+ [Woodcutting](#woodcutting)
+ [Salvage](#salvage)
+ [Smelting](#smelting)


### Power Level

"Power Level" is the sum total of all your skill levels, displayed below your name and visible to all players. Use `/mctop` to view the server-wide power level leaderboard.


### Acrobatics

"Acrobatics" is a lifestyle skill that reduces damage when attacked or falling. Higher levels mean a greater chance to proc.

#### Experience Sources

Taking fall damage, rolling, or dodging successfully increases Acrobatics XP.

#### Active Abilities

**Roll** — When taking fall damage with a held item, right-click the item to perform a graceful roll and negate fall damage. Cooldown: 20s.

#### Passive Abilities

**Graceful Roll** — Chance to reduce fall damage.

**Dodge** — Chance to halve damage from attacks.

#### Special

Here, "/Acrobatics" can be abbreviated as "/acrobatics", "/ 杂技", etc.


### Alchemy

"Alchemy" is a crafting skill associated with brewing. Higher levels unlock more recipes.

#### Experience Sources

○ Brewing potions (higher-level potions give more XP)
○ Harvesting from Brewing Stand (brewing stands in unloaded chunks won't collect XP — place them next to you)

#### Passive Abilities

**Catalysis** — Speeds up brewing, up to 4x faster.

**Concoctions** — Potions brewed with your own hands last longer (stacks with vanilla extended duration).

#### Recipes

Initially only Tier I ingredients are unlocked. Higher Alchemy levels unlock higher-tier ingredient usage.

See [Alchemy Recipe Table](https://github.com/wiki/Alchemy)。

**Pro Tip**: Underwater breathing potions + night vision potions make ocean monument raids much easier!


### Archery

"Archery" is a combat skill centered on bows.

#### Experience Sources

Damaging monsters or players with bow/crossbow attacks.

#### Active Abilities

**Arrow Salvo** — After drawing a bow fully, left-click to fire multiple arrows at once. Cooldown: 60s.

#### Passive Abilities

**Arrow Retrieval** — Chance to retrieve arrows from corpses after killing with a bow.

**Daze** — Chance to inflict Slowness IV and Nausea on the target for a short duration.

**Ignition** — Chance to set the target on fire when hitting with a bow while sneaking.

**Experience Boost** — Bonus XP per level.

#### Special

**Horseman Bonus** — Deal +__% bonus damage while riding a mount.

**Difficulty Bonus** — Deal +__% bonus damage on Hard difficulty.


### Axes

"Axes" is a combat skill using axes.

#### Experience Sources

Damaging monsters or players with axe attacks.

#### Active Abilities

**Skull Splitter** — Right-click with an axe to deal bonus AoE damage (affects plants within 2 blocks). Cooldown: __s.

**Heavy Blow** — Sneak + right-click with an axe for a heavy blow. Cooldown: __s.

#### Passive Abilities

**Critical Strikes** — Chance to deal double damage.

**Axe Mastery** — Bonus damage with axes.

**Armor Impact** — Chance to deal extra durability damage to enemy armor.

#### Special

**Horseman Bonus** — Deal +__% bonus damage while riding a mount.

**Difficulty Bonus** — Deal +__% bonus damage on Hard difficulty.


### Excavation

"Excavation" is a gathering skill centered on digging.

#### Experience Sources

Digging dirt, grass, sand, gravel, clay, soul sand, mycelium, and podzol.

#### Active Abilities

**Giga Drill Breaker** — Right-click with a shovel to enable super-speed digging for a short time. Cooldown: __s.

#### Passive Abilities

**Treasure Hunt** — Chance to drop rare items when digging.

#### Special

+__% excavation speed increase.


### Fishing

"Fishing" is a gathering skill centered on fishing.

#### Experience Sources

Successfully catching items while fishing grants XP.

Additionally, you can gain XP by shaking items off hostile mobs with the fishing rod.

#### Active Abilities

**Super Bait** — Left-click with a fishing rod to shake items off mobs or catch multiple items at once. Cooldown: __s.

#### Passive Abilities

**Treasure Hunter** — Chance to catch rare loot.

**Magic Hunter** — Chance to catch enchanted items.

**Master Angler** — Chance for +1 fish yield when fishing.

**Shake** — Chance to shake items off hostile mobs using the fishing rod.

**Ice Fishing** — Chance to fish successfully in ice biomes.


### Herbalism

"Herbalism" is a gathering skill centered on farming.

#### Experience Sources

Harvesting and replanting mature crops. Breaking the following plants grants XP: wheat, potatoes, carrots, melons, pumpkins, sugarcane, cacti, brown/red mushrooms, nether wart, lily pads, vines, beetroot, sweet berry bushes, and all types of leaves (regardless of whether they are player-placed).

#### Passive Abilities

**Green Thumb** — When harvesting a mature crop, it will auto-replant. (Crops without seeds, like sugarcane and cacti, drop as usual.)

**Green Terra** — Right-click a block with seeds to instantly transform it and surrounding blocks into the corresponding vegetation. Cooldown: __s.

**Farmers Diet** — +__ hunger restored when eating food obtained from Herbalism.

**Hylian Luck** — Chance to drop rare items.

**Double Drops** — Chance to get double drops.

**Shroom Thumb** — Right-click mycelium with a red/brown mushroom: chance to transform into a Giant Mushroom; right-click a cow with a mushroom: transform into a Mooshroom.

#### Level Rewards

At Lv. 100/250/350/500/750/1000, the Green Terra blocks range expands.

#### Special

This skill can be leveled by breaking leaves in the Nether.


### Mining

"Mining" is a gathering skill centered on mining.

#### Experience Sources

Mining stone and ores grants XP.
○ Smooth stone, cobblestone — +__ each
○ Ores — +__ each (Diamond +__、Emerald +__)

#### Active Abilities

**Super Breaker** — Right-click with a pickaxe: triggers super-speed mining. Triple drop chance applies to all block types. Cooldown: __s.

#### Passive Abilities

**Double Drops** — Chance to get double drops.

**Blast Mining** — Bonus drops crafted with TNT, and no item loss on TNT explosion (O_o).

**Bigger Bombs** — Increase TNT explosion range.

**Demolition Expert** — Decrease TNT damage received.

#### Level Rewards

At Lv. 200/375/500/625/750/875/1000, unlock double drop for additional ores.

#### Special

**Stone Optimization**: Gain 1.5x XP from stone.  
**Above Bedrock**: Gain XP when mining blocks within __ blocks below the Nether ceiling or above the Overworld bedrock.  
**Stone Eater**: XP gain from stone does not decrease.


### Repair

"Repair" is a crafting skill that focuses on repairing items.

#### Experience Sources

Successful repairs grant XP.

#### Active Abilities

**Master Repair** — Right-click an iron block: repairs all items in inventory by one durability level. Cooldown: 60s.

#### Passive Abilities

**Arcane Forging** — Repairs may retain enchants.

**Repair Mastery** — +__% more durability restored per repair.

**Salvage Mastery** — Bonus materials recovered when salvaging.

**Super Repair** — Chance for double repair effectiveness.

#### Special

Use the Vanilla `/Repair` feature to restore items without an anvil.


### Swords

"Swords" is a combat skill using swords.

#### Experience Sources

Damaging monsters or players with sword attacks.

#### Active Abilities

**Serrated Strikes** — Right-click with a sword: applies bleed (damage per tick based on level) to the target. Cooldown: __s.

**Blade Rush** — Sneak + right-click with a sword: throws the sword, dealing up to 10 hearts of damage on return. Cooldown: __s.

#### Passive Abilities

**Bleed** — Chance to inflict bleed (damage per tick based on level) every 2 seconds for a duration based on level.

**Counter Attack** — When hit, chance to reflect damage back with a sword.

**Rupture** — Deal extra damage to enemies who are bleeding.

#### Special

**Horseman Bonus** — Deal +__% bonus damage while riding a mount.  
**Difficulty Bonus** — Deal +__% bonus damage on Hard difficulty.


### Taming

"Taming" is a combat skill focused on tamed animals.

#### Experience Sources

Gain XP when tamed wolves deal damage. (XP counts for you, not the wolf.)

#### Active Abilities

**Call of the Wild** — Right-click: summons a wolf/donkey/llama/polar bear/ocelot/fox/chicken/parrot/horse/cat/turtle. Cooldown: 60s.

**Shield of Nature** — Sneak + right-click: your mount gains damage absorption. Cooldown: 30s.

#### Passive Abilities

**Gore** — Your tamed wolves deal bonus damage and have a chance to apply bleed or critical hit.

**Environmentally Aware** — Tamed wolves gain environmental damage resistance (fall/cacti/lava).

**Thick Fur** — Tamed wolves gain damage resistance and fire resistance.

**Holy Hound** — Tamed wolves regenerate health when taking damage.

**Shock Proof** — Tamed wolves gain explosion damage resistance.

**Sharp Claws** — Your tamed wolves deal bonus damage.

**Fast Food Service** — Your tamed wolves have a chance on hit to heal you.

**Beast Lore** — Right-click a wolf with a bone: view wolf stats; in addition, right-click a tamed wolf with 10 bones to summon it.

#### Special

+__ bonus damage from your mount.


### Unarmed

"Unarmed" is a combat skill with empty hands.

#### Experience Sources

Damaging monsters or players with bare-hand attacks.

#### Active Abilities

**Unarmed Frenzy** — Right-click with an empty hand: rapid consecutive attacks. Cooldown: 60s.

#### Passive Abilities

**Iron Arm** — Bonus unarmed damage.

**Arrow Deflect** — Chance to deflect arrows shot at you.

**Disarm** — Chance to cause the enemy to drop their held item.

**Deflect** — Chance to reflect damage dealt to you back at the attacker.

**Berserker** — Right-click cobblestone: crush it into gravel. Can also destroy sand/gravel/glass.

#### Special

+__ bonus damage on Hard difficulty.


### Woodcutting

"Woodcutting" is a gathering skill centered on chopping trees.

#### Experience Sources

Chopping logs grants XP (jungle giant trees give bonus XP).

#### Active Abilities

**Tree Feller** — Right-click with an axe: instantly chop down all connected logs. Cooldown: __s.

#### Passive Abilities

**Double Drops** — Chance for double drops from logs.

**Splinter** — Deal extra durability damage to enemy armor.

**Leaf Blower** — Leaves decay instantly after the connected log is chopped.

**Nature's Bounty** — Saplings auto-replant when leaves decay.

#### Level Rewards

At Lv. 50/100/250/500/750/1000, increase double drop chance and drop types.

#### Special

+__% logging speed increase.


### Salvage

"Salvage" is a child skill of Fishing and Repair (cannot be directly leveled).

#### Usage

Right-click a Gold Block with a Gold Ingot to salvage items in your inventory for base materials.

#### Special

This is one of the main item transportation channels for PVP server gameplay!


### Smelting

"Smelting" is a child skill of Repair and Mining (cannot be directly leveled).

#### Passive Abilities

**Fuel Efficiency** — Smelting speed bonus.

**Second Smelt** — Smelted items have a chance to yield double output.

**Flux Mining** — While smelting, ores in your hotbar or off-hand have a chance to be automatically smelted when mined.

#### Special

This is one of the main item transportation channels for PVP server gameplay!

---

*Generated from mcmmo-commands.md · Content may be outdated, for reference only*
