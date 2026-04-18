package com.zerotoler.rpgmenu.domain.usecase

import com.zerotoler.rpgmenu.data.repository.LoadoutRepository
import com.zerotoler.rpgmenu.data.repository.PartCatalogRepository
import com.zerotoler.rpgmenu.domain.model.CombatType
import com.zerotoler.rpgmenu.domain.model.battlesession.TeamTopConfig

/**
 * Projects the three PARTS loadout slots into battle-prep configs (read-only snapshot).
 */
class GetBattleReadyTeamUseCase(
    private val loadoutRepository: LoadoutRepository,
    private val catalogRepository: PartCatalogRepository,
    private val computeBuildStatsUseCase: ComputeBuildStatsUseCase,
) {
    suspend operator fun invoke(teamId: String): List<TeamTopConfig> =
        (0..2).map { slot -> buildSlot(teamId, slot) }

    private suspend fun buildSlot(teamId: String, slotIndex: Int): TeamTopConfig {
        val lo = loadoutRepository.getLoadoutOnce(teamId, slotIndex)
        val cap = lo?.battleCapId?.let { catalogRepository.getPartById(it) }
        val ring = lo?.weightRingId?.let { catalogRepository.getPartById(it) }
        val drv = lo?.driverId?.let { catalogRepository.getPartById(it) }
        val preview = computeBuildStatsUseCase(cap, ring, drv)
        val complete = cap != null && ring != null && drv != null
        val power = listOfNotNull(
            preview.totalStats.attack,
            preview.totalStats.defense,
            preview.totalStats.stamina,
            preview.totalStats.health,
        ).sum().toFloat()
        val capName = cap?.name?.takeIf { !it.isBlank() }
        val displayName = lo?.name?.takeIf { !it.isBlank() } ?: capName ?: "Top ${slotIndex + 1}"
        val type = cap?.combatType?.takeIf { it != CombatType.UNKNOWN } ?: CombatType.BALANCE
        return TeamTopConfig(
            slotIndex = slotIndex,
            battleCapId = lo?.battleCapId,
            weightRingId = lo?.weightRingId,
            driverId = lo?.driverId,
            displayName = displayName,
            powerScore = power,
            dominantCombatType = type,
            isComplete = complete,
        )
    }
}
