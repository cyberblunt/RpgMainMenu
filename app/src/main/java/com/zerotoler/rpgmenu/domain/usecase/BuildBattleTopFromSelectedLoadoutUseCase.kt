package com.zerotoler.rpgmenu.domain.usecase

import com.zerotoler.rpgmenu.data.repository.PartCatalogRepository
import com.zerotoler.rpgmenu.domain.model.CombatType
import com.zerotoler.rpgmenu.domain.model.DriverClass
import com.zerotoler.rpgmenu.domain.model.PartBase
import com.zerotoler.rpgmenu.domain.model.PartCategory
import com.zerotoler.rpgmenu.domain.model.RingClass
import com.zerotoler.rpgmenu.domain.model.battlesession.OpponentBattleTop
import com.zerotoler.rpgmenu.domain.model.battlesession.TeamTopConfig
import com.zerotoler.rpgmenu.domain.model.battle.BattleTopStats
import com.zerotoler.rpgmenu.domain.model.battle.StabilizationLevel
import com.zerotoler.rpgmenu.domain.model.battle.TopOwner
import kotlin.math.sqrt
import kotlinx.coroutines.flow.first

/**
 * Builds battle stats from PARTS loadout (player) or opponent session config — single projection path.
 */
class BuildBattleTopFromSelectedLoadoutUseCase(
    private val catalogRepository: PartCatalogRepository,
    private val computeBuildStatsUseCase: ComputeBuildStatsUseCase,
) {
    private companion object {
        // Gameplay requirement: tops on the arena must become 3x larger.
        const val TOP_RADIUS_SCALE = 3f
        const val MIN_TOP_RADIUS = 0.18f // ~= 0.06f * 3
        const val MAX_TOP_RADIUS = 0.35f // safety clamp for stable spawn/bounce inside arena
    }


    suspend fun fromPlayerTop(config: TeamTopConfig): BattleTopStats {
        val lo = config
        val cap = lo.battleCapId?.let { catalogRepository.getPartById(it) }
        val ring = lo.weightRingId?.let { catalogRepository.getPartById(it) }
        val drv = lo.driverId?.let { catalogRepository.getPartById(it) }
        val preview = computeBuildStatsUseCase(cap, ring, drv)
        val h = (preview.totalStats.health ?: 42).toFloat()
        val atk = (preview.totalStats.attack ?: 12).toFloat()
        val def = (preview.totalStats.defense ?: 12).toFloat()
        val stm = (preview.totalStats.stamina ?: 12).toFloat()
        // Gameplay requirement for Beyblade-style physics:
        // clamp to physically-supported tournament-ish range (g).
        val weight = (preview.totalStats.weightGrams ?: 52f).coerceIn(32.5f, 54.5f)
        val type = lo.dominantCombatType
        return finalize(
            id = "player_${lo.slotIndex}",
            owner = TopOwner.PLAYER,
            name = lo.displayName,
            archetype = type,
            ringClass = ring?.ringClass ?: RingClass.UNKNOWN,
            driverClass = drv?.driverClass ?: DriverClass.UNKNOWN,
            baseHealth = h,
            baseAtk = atk,
            baseDef = def,
            baseStm = stm,
            weightGrams = weight,
        )
    }

    fun fromOpponentTop(opp: OpponentBattleTop): BattleTopStats {
        // Backwards-compatible non-suspending fallback for callers not yet migrated.
        // Uses UNKNOWN parts metadata; still produces a playable top.
        val pl = opp.powerLevelHint.coerceIn(6, 24).toFloat()
        val h = 38f + pl * 2.2f
        return finalize(
            id = opp.id,
            owner = TopOwner.ENEMY,
            name = opp.name,
            archetype = opp.archetype,
            ringClass = RingClass.UNKNOWN,
            driverClass = DriverClass.UNKNOWN,
            baseHealth = h,
            baseAtk = 10f + pl * 0.9f,
            baseDef = 10f + pl * 0.85f,
            baseStm = 10f + pl * 0.8f,
            weightGrams = (48f + pl * 0.6f).coerceIn(32.5f, 54.5f),
        )
    }

    /**
     * Builds an enemy top using the same "3-part" structure as the player:
     * battle cap + weight ring + driver — selected deterministically from the catalog.
     *
     * This guarantees opponents are part-based without cloning the player's loadout.
     */
    suspend fun fromOpponentTopParts(opp: OpponentBattleTop): BattleTopStats {
        val allParts = catalogRepository.getAllPartsFlow().first()

        val seed = opp.id.hashCode() xor (opp.powerLevelHint * 131)
        val profile = when (opp.archetype) {
            CombatType.ATTACK -> EnemyProfile.ATTACK
            CombatType.DEFENSE -> EnemyProfile.DEFENSE
            CombatType.STAMINA -> EnemyProfile.STAMINA
            CombatType.BALANCE, CombatType.UNKNOWN -> EnemyProfile.BALANCE
        }

        val cap = pickEnemyPart(
            candidates = allParts.filter { it.category == PartCategory.BATTLE_CAP },
            profile = profile,
            seed = seed,
            salt = 11,
        )
        val ring = pickEnemyPart(
            candidates = allParts.filter { it.category == PartCategory.WEIGHT_RING },
            profile = profile,
            seed = seed,
            salt = 17,
        )
        val drv = pickEnemyPart(
            candidates = allParts.filter { it.category == PartCategory.DRIVER },
            profile = profile,
            seed = seed,
            salt = 23,
        )

        val preview = computeBuildStatsUseCase(cap, ring, drv)
        val pl = opp.powerLevelHint.coerceIn(6, 24).toFloat()
        val baseH = (preview.totalStats.health ?: (38 + pl * 2.2f).toInt()).toFloat()
        val baseAtk = (preview.totalStats.attack ?: (10 + pl * 0.9f).toInt()).toFloat()
        val baseDef = (preview.totalStats.defense ?: (10 + pl * 0.85f).toInt()).toFloat()
        val baseStm = (preview.totalStats.stamina ?: (10 + pl * 0.8f).toInt()).toFloat()
        val weight = (preview.totalStats.weightGrams ?: (48f + pl * 0.6f)).coerceIn(32.5f, 54.5f)

        return finalize(
            id = opp.id,
            owner = TopOwner.ENEMY,
            name = opp.name,
            archetype = opp.archetype,
            ringClass = ring?.ringClass ?: RingClass.UNKNOWN,
            driverClass = drv?.driverClass ?: DriverClass.UNKNOWN,
            baseHealth = baseH,
            baseAtk = baseAtk,
            baseDef = baseDef,
            baseStm = baseStm,
            weightGrams = weight,
        )
    }

    private enum class EnemyProfile { ATTACK, DEFENSE, STAMINA, BALANCE }

    private fun pickEnemyPart(
        candidates: List<PartBase>,
        profile: EnemyProfile,
        seed: Int,
        salt: Int,
    ): PartBase? {
        if (candidates.isEmpty()) return null

        // Score then choose from top-N deterministically to keep variety.
        val scored = candidates.map { it to score(it, profile) }
            .sortedByDescending { it.second }

        val n = minOf(8, scored.size)
        val r = pseudoRandom(seed, salt)
        val idx = (r * n).toInt().coerceIn(0, n - 1)
        return scored[idx].first
    }

    private fun score(p: PartBase, profile: EnemyProfile): Float {
        val h = (p.stats.health ?: 0).toFloat()
        val a = (p.stats.attack ?: 0).toFloat()
        val d = (p.stats.defense ?: 0).toFloat()
        val s = (p.stats.stamina ?: 0).toFloat()
        val iv = p.stats.intervalSeconds ?: 0f
        return when (profile) {
            EnemyProfile.ATTACK -> a * 3f + s + d * 0.5f
            EnemyProfile.DEFENSE -> d * 3f + h * 0.4f + a * 0.3f
            EnemyProfile.STAMINA -> s * 3f + iv * 10f + a * 0.2f
            EnemyProfile.BALANCE -> a + d + s + h * 0.1f + iv * 2f
        }
    }

    private fun pseudoRandom(seed: Int, salt: Int): Float {
        val x = (seed * 73856093 xor salt * 19349663) and 0x7fffffff
        return (x % 10000) / 10000f
    }

    private fun finalize(
        id: String,
        owner: TopOwner,
        name: String,
        archetype: CombatType,
        ringClass: RingClass,
        driverClass: DriverClass,
        baseHealth: Float,
        baseAtk: Float,
        baseDef: Float,
        baseStm: Float,
        weightGrams: Float,
    ): BattleTopStats {
        var atk = baseAtk
        var def = baseDef
        var stmEff = 1f
        var balance = 1f
        var wall = 1f
        var coll = 1f
        var massMul = 1f
        when (archetype) {
            CombatType.ATTACK -> {
                atk *= 1.08f
                coll *= 1.22f
                balance *= 0.88f
                stmEff *= 0.9f
            }
            CombatType.DEFENSE -> {
                def *= 1.12f
                massMul *= 1.18f
                coll *= 0.92f
                wall *= 1.15f
            }
            CombatType.STAMINA -> {
                stmEff *= 1.25f
                coll *= 0.88f
                balance *= 1.05f
            }
            CombatType.BALANCE -> {
                balance *= 1.05f
            }
            CombatType.UNKNOWN -> {}
        }
        val wG = weightGrams.coerceIn(32.5f, 54.5f)
        val mass = (wG * massMul / 50f).coerceIn(0.65f, 2.1f)
        val baseRadius = (0.055f + sqrt(mass.toDouble()).toFloat() * 0.028f).coerceIn(0.06f, 0.11f)
        val radius = (baseRadius * TOP_RADIUS_SCALE).coerceIn(MIN_TOP_RADIUS, MAX_TOP_RADIUS)
        val maxHp = baseHealth.coerceIn(40f, 220f)
        val maxStm = (baseStm * 4.5f).coerceIn(55f, 200f)
        val stabilization = stabilizationLevelFor(driverClass = driverClass, ringClass = ringClass)
        return BattleTopStats(
            id = id,
            owner = owner,
            displayName = name,
            archetype = archetype,
            ringClass = ringClass,
            driverClass = driverClass,
            stabilizationLevel = stabilization,
            weightGrams = wG,
            radius = radius,
            mass = mass,
            maxHealth = maxHp,
            maxStamina = maxStm,
            attack = atk,
            defense = def,
            staminaEfficiency = stmEff.coerceIn(0.55f, 1.6f),
            balanceFactor = balance.coerceIn(0.65f, 1.35f),
            wallGrip = wall.coerceIn(0.75f, 1.35f),
            collisionPower = coll.coerceIn(0.75f, 1.45f),
        )
    }

    private fun stabilizationLevelFor(driverClass: DriverClass, ringClass: RingClass): StabilizationLevel {
        return when (driverClass) {
            DriverClass.CENTRAL -> StabilizationLevel.CENTER
            DriverClass.CIRCLE -> when (ringClass) {
                RingClass.INNER, RingClass.BALANCED -> StabilizationLevel.INNER_RING
                RingClass.OUTER, RingClass.IMBALANCED -> StabilizationLevel.OUTER_RING
                RingClass.UNKNOWN -> StabilizationLevel.INNER_RING
            }
            DriverClass.UNKNOWN -> when (ringClass) {
                RingClass.OUTER, RingClass.IMBALANCED -> StabilizationLevel.OUTER_RING
                else -> StabilizationLevel.INNER_RING
            }
        }
    }
}
