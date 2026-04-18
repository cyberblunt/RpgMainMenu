package com.zerotoler.rpgmenu.data.seed

import com.zerotoler.rpgmenu.domain.model.CombatType
import com.zerotoler.rpgmenu.domain.model.DriverClass
import com.zerotoler.rpgmenu.domain.model.PartBase
import com.zerotoler.rpgmenu.domain.model.PartCategory
import com.zerotoler.rpgmenu.domain.model.PartStats
import com.zerotoler.rpgmenu.domain.model.RingClass
import com.zerotoler.rpgmenu.domain.model.SpinDirection

/**
 * Offline master catalog. Battle cap names and level-15 stats are aligned to the Spiral Warrior Wiki
 * Battle Caps index (spiral-warrior.fandom.com/wiki/Battle_Caps) as of development; weight rings and
 * drivers use wiki naming where available with synthesized stats when tables were incomplete.
 */
object MasterCatalogProvider {
    fun allParts(): List<PartBase> = buildList {
        addAll(battleCaps())
        addAll(weightRings())
        addAll(drivers())
    }

    private fun slug(raw: String): String =
        raw.lowercase()
            .replace(" ", "_")
            .replace(".", "")
            .replace("-", "_")
            .replace("'", "")

    private fun battleCaps(): List<PartBase> {
        val rows = listOf(
            Triple("Aiolos", CombatType.ATTACK, SpinDirection.RIGHT) to PartStats(6900, 524, 110, 34, 3.2f, 13.8f),
            Triple("Alice", CombatType.STAMINA, SpinDirection.LEFT) to PartStats(12630, 653, 122, 43, 3.0f, 10.5f),
            Triple("Athena", CombatType.DEFENSE, SpinDirection.RIGHT) to PartStats(8054, 368, 190, 32, 2.7f, 21.6f),
            Triple("Atlas", CombatType.DEFENSE, SpinDirection.RIGHT) to PartStats(8700, 448, 213, 29, 3.0f, 24.8f),
            Triple("Atum", CombatType.ATTACK, SpinDirection.RIGHT) to PartStats(6391, 475, 76, 30, 2.0f, 15.0f),
            Triple("Baldr", CombatType.ATTACK, SpinDirection.RIGHT) to PartStats(7922, 604, 84, 30, 2.5f, 16.2f),
            Triple("Beelzebub", CombatType.STAMINA, SpinDirection.LEFT) to PartStats(6133, 535, 110, 52, null, 15.5f),
            Triple("Behemoth", CombatType.ATTACK, SpinDirection.RIGHT) to PartStats(5879, 497, 68, 30, 2.2f, 16.2f),
            Triple("Bubble", CombatType.DEFENSE, SpinDirection.RIGHT) to PartStats(8700, 543, 159, 40, 2.6f, 16.8f),
            Triple("Camus", CombatType.BALANCE, SpinDirection.RIGHT) to PartStats(8719, 627, 84, 35, 2.7f, 12.6f),
            Triple("Chronos", CombatType.BALANCE, SpinDirection.RIGHT) to PartStats(6315, 551, 103, 43, 4.0f, 13.8f),
            Triple("Death Knight", CombatType.ATTACK, SpinDirection.RIGHT) to PartStats(6391, 604, 110, 30, 2.2f, 16.2f),
            Triple("Dr. Geek", CombatType.ATTACK, SpinDirection.LEFT) to PartStats(6391, 562, 103, 37, 2.2f, 16.2f),
            Triple("Enchantress", CombatType.STAMINA, SpinDirection.RIGHT) to PartStats(11187, 646, 122, 43, 3.0f, 12.5f),
            Triple("Hattori Hanzo", CombatType.ATTACK, SpinDirection.RIGHT) to PartStats(6391, 448, 65, 30, 1.8f, 13.8f),
            Triple("Himiko", CombatType.BALANCE, SpinDirection.RIGHT) to PartStats(7519, 437, 103, 35, 2.5f, 15.0f),
            Triple("Hodur", CombatType.ATTACK, SpinDirection.RIGHT) to PartStats(7413, 646, 84, 30, 2.5f, 17.4f),
            Triple("Iltheus", CombatType.STAMINA, SpinDirection.LEFT) to PartStats(11187, 752, 110, 46, 3.0f, 12.5f),
            Triple("Justice", CombatType.BALANCE, SpinDirection.RIGHT) to PartStats(10527, 361, 76, 46, 3.0f, 18.6f),
            Triple("Kaguyahime", CombatType.STAMINA, SpinDirection.RIGHT) to PartStats(9023, 441, 110, 40, 3.4f, 12.5f),
            Triple("Laurel Wreath", CombatType.STAMINA, SpinDirection.RIGHT) to PartStats(9744, 433, 91, 46, 3.0f, 12.5f),
            Triple("Lucifer", CombatType.DEFENSE, SpinDirection.RIGHT) to PartStats(9342, 368, 175, 35, 2.5f, 21.6f),
            Triple("Mercury", CombatType.STAMINA, SpinDirection.RIGHT) to PartStats(6133, 554, 122, 52, 3.7f, 10.5f),
            Triple("Mermaid", CombatType.BALANCE, SpinDirection.RIGHT) to PartStats(6315, 615, 91, 43, 3.0f, 13.8f),
            Triple("Nilthotep", CombatType.BALANCE, SpinDirection.RIGHT) to PartStats(8719, 627, 84, 35, 2.7f, 12.6f),
            Triple("Pallas", CombatType.DEFENSE, SpinDirection.RIGHT) to PartStats(8119, 771, 118, 37, 3.0f, 17.4f),
            Triple("Phoenix", CombatType.DEFENSE, SpinDirection.RIGHT) to PartStats(9987, 520, 232, 35, 2.7f, 23.2f),
            Triple("Plague Knight", CombatType.DEFENSE, SpinDirection.RIGHT) to PartStats(4337, 570, 190, 35, 2.7f, 23.2f),
            Triple("Puppeteer", CombatType.BALANCE, SpinDirection.RIGHT) to PartStats(7409, 543, 201, 40, 2.9f, 21.6f),
            Triple("Qing", CombatType.BALANCE, SpinDirection.RIGHT) to PartStats(6900, 570, 84, 32, 2.2f, 17.4f),
            Triple("Shura", CombatType.ATTACK, SpinDirection.RIGHT) to PartStats(8054, 710, 216, 37, 3.0f, 24.8f),
            Triple("Survivor", CombatType.STAMINA, SpinDirection.RIGHT) to PartStats(6900, 562, 110, 32, 2.5f, 15.0f),
            Triple("Thetis", CombatType.BALANCE, SpinDirection.RIGHT) to PartStats(9744, 619, 122, 46, 3.5f, 11.5f),
            Triple("Thor", CombatType.ATTACK, SpinDirection.LEFT) to PartStats(8719, 676, 91, 40, 2.5f, 13.8f),
            Triple("Valkyrie", CombatType.ATTACK, SpinDirection.RIGHT) to PartStats(5879, 589, 84, 30, 2.2f, 16.2f),
            Triple("Venus", CombatType.STAMINA, SpinDirection.RIGHT) to PartStats(6900, 562, 99, 27, 2.5f, 17.4f),
        )
        return rows.mapIndexed { index, pair ->
            val (meta, stats) = pair
            val (name, ct, spin) = meta
            PartBase(
                id = "cap_" + slug(name),
                name = name,
                category = PartCategory.BATTLE_CAP,
                rarity = (index % 5) + 1,
                combatType = ct,
                spinDirection = spin,
                ringClass = null,
                driverClass = null,
                tags = listOf("Battle Cap", "Lv15 ref: wiki"),
                stats = stats,
            )
        }
    }

    private fun weightRings(): List<PartBase> {
        val namesClasses = listOf(
            "Sting" to RingClass.OUTER,
            "Spread" to RingClass.BALANCED,
            "Apex Forge" to RingClass.IMBALANCED,
            "Nexus Loop" to RingClass.INNER,
            "Vega Crown" to RingClass.BALANCED,
            "Rigel Guard" to RingClass.OUTER,
            "Paradox Rim" to RingClass.IMBALANCED,
            "Hunter Ring" to RingClass.OUTER,
            "Seeker Band" to RingClass.INNER,
            "Fusion Disk" to RingClass.BALANCED,
            "Union Wheel" to RingClass.BALANCED,
            "Core Halo" to RingClass.INNER,
            "Shell Guard" to RingClass.OUTER,
            "Counter Edge" to RingClass.IMBALANCED,
            "Reflect Arc" to RingClass.BALANCED,
            "Absorb Wall" to RingClass.OUTER,
            "Break Layer" to RingClass.IMBALANCED,
            "Flash Loop" to RingClass.INNER,
            "Nova Rim" to RingClass.BALANCED,
            "Eclipse Band" to RingClass.OUTER,
            "Titan Loop" to RingClass.INNER,
            "Chronos Ring" to RingClass.BALANCED,
            "Tempest Ring" to RingClass.IMBALANCED,
            "Mirage Rim" to RingClass.OUTER,
            "Specter Loop" to RingClass.INNER,
            "Dusk Guard" to RingClass.BALANCED,
            "Prism Band" to RingClass.IMBALANCED,
            "Vector Ring" to RingClass.OUTER,
        )
        val combatCycle = listOf(
            CombatType.ATTACK, CombatType.DEFENSE, CombatType.STAMINA, CombatType.BALANCE,
        )
        return namesClasses.mapIndexed { i, pair ->
            val (name, rc) = pair
            val ct = combatCycle[i % combatCycle.size]
            val base = 4000 + i * 173
            PartBase(
                id = "ring_" + slug(name),
                name = name,
                category = PartCategory.WEIGHT_RING,
                rarity = (i % 5) + 1,
                combatType = ct,
                spinDirection = SpinDirection.UNKNOWN,
                ringClass = rc,
                driverClass = null,
                tags = listOf("Weight Ring"),
                stats = PartStats(
                    health = base + 200,
                    attack = 220 + (i * 7) % 200,
                    defense = 95 + (i * 11) % 130,
                    stamina = 28 + (i * 3) % 20,
                    intervalSeconds = 2.2f + (i % 5) * 0.15f,
                    weightGrams = 14f + (i % 8) * 0.9f,
                ),
            )
        }
    }

    private fun drivers(): List<PartBase> {
        val names = listOf(
            "Agility", "Batter", "Battlecry", "Beast", "Berserker", "Bloom", "Burst", "Crystal",
            "Curse", "Delusion", "Entropy", "Erosion", "Flame", "Fleet", "Gush", "Halo",
            "Infusion", "Insight", "Leaf", "Lightning", "Lurk", "Megalith", "Monarch", "Moonlight",
            "Ode", "Pain", "Piety", "Rage", "Rampage", "Ravager", "Shadow", "Shock", "Soft",
            "Soprano", "Source", "Throne", "Time", "Vortex", "Warfare", "Wind",
        )
        val classes = listOf(DriverClass.CIRCLE, DriverClass.CENTRAL)
        val combatCycle = listOf(
            CombatType.BALANCE,
            CombatType.ATTACK,
            CombatType.STAMINA,
            CombatType.DEFENSE,
        )
        return names.mapIndexed { i, name ->
            val dc = classes[i % classes.size]
            val ct = combatCycle[i % combatCycle.size]
            PartBase(
                id = "driver_" + slug(name),
                name = name,
                category = PartCategory.DRIVER,
                rarity = (i % 5) + 1,
                combatType = ct,
                spinDirection = SpinDirection.UNKNOWN,
                ringClass = null,
                driverClass = dc,
                tags = listOf("Driver"),
                stats = PartStats(
                    health = 1200 + (i * 47) % 800,
                    attack = 180 + (i * 13) % 120,
                    defense = 60 + (i * 5) % 80,
                    stamina = 20 + (i * 2) % 25,
                    intervalSeconds = 2.4f + (i % 6) * 0.12f,
                    weightGrams = 8f + (i % 5) * 0.35f,
                ),
            )
        }
    }
}
