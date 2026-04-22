package com.zerotoler.rpgmenu.ui.parts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zerotoler.rpgmenu.data.repository.InventoryRepository
import com.zerotoler.rpgmenu.data.repository.LoadoutRepository
import com.zerotoler.rpgmenu.data.repository.PartCatalogRepository
import com.zerotoler.rpgmenu.data.repository.PlayerProgressRepository
import com.zerotoler.rpgmenu.data.repository.TeamRepository
import com.zerotoler.rpgmenu.data.seed.StarterSetProvider
import com.zerotoler.rpgmenu.domain.model.CombatType
import com.zerotoler.rpgmenu.domain.model.InventoryFilterState
import com.zerotoler.rpgmenu.domain.model.InventorySortMode
import com.zerotoler.rpgmenu.domain.model.Loadout
import com.zerotoler.rpgmenu.domain.model.PartBase
import com.zerotoler.rpgmenu.domain.model.PartCategory
import com.zerotoler.rpgmenu.domain.model.PlayerPartState
import com.zerotoler.rpgmenu.domain.model.SpinDirection
import com.zerotoler.rpgmenu.domain.model.Team
import com.zerotoler.rpgmenu.data.db.entity.WalletEntity
import com.zerotoler.rpgmenu.domain.usecase.AutoConfigLoadoutUseCase
import com.zerotoler.rpgmenu.domain.usecase.ComputeBuildStatsUseCase
import com.zerotoler.rpgmenu.domain.usecase.EquipPartUseCase
import com.zerotoler.rpgmenu.domain.usecase.GetVisiblePartsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private data class TeamLoadoutPack(
    val teamId: String,
    val loadouts: List<Loadout>,
    val catalog: List<PartBase>,
    val players: List<PlayerPartState>,
)

/** [combine] supports at most five flows per call; bundle five inputs before merging [transientError]. */
private data class PartsInventoryInputs(
    val pack: TeamLoadoutPack,
    val teams: List<Team>,
    val slot: Int,
    val category: PartCategory,
    val filter: InventoryFilterState,
)

class PartsInventoryViewModel(
    private val catalogRepository: PartCatalogRepository,
    private val inventoryRepository: InventoryRepository,
    private val loadoutRepository: LoadoutRepository,
    private val teamRepository: TeamRepository,
    private val playerProgressRepository: PlayerProgressRepository,
    private val computeBuildStatsUseCase: ComputeBuildStatsUseCase,
    private val getVisiblePartsUseCase: GetVisiblePartsUseCase,
    private val equipPartUseCase: EquipPartUseCase,
    private val autoConfigLoadoutUseCase: AutoConfigLoadoutUseCase,
) : ViewModel() {

    private val slotIndex = MutableStateFlow(0)
    private val selectedCategory = MutableStateFlow(PartCategory.BATTLE_CAP)
    private val filterState = MutableStateFlow(InventoryFilterState.Default)
    private val transientError = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            playerProgressRepository.observeProfile()
                .map { it.activeLoadoutSlot.coerceIn(0, 2) }
                .distinctUntilChanged()
                .collect { slot ->
                    slotIndex.value = slot
                }
        }
    }

    private val teamLoadoutPack = playerProgressRepository.observeProfile()
        .map { p ->
            p.activeTeamId.takeIf { it.isNotBlank() } ?: StarterSetProvider.DEFAULT_TEAM_ID
        }
        .distinctUntilChanged()
        .flatMapLatest { teamId ->
            combine(
                loadoutRepository.getLoadoutsForTeamFlow(teamId),
                catalogRepository.getAllPartsFlow(),
                inventoryRepository.getAllPlayerStatesFlow(),
            ) { loadouts, catalog, players ->
                TeamLoadoutPack(teamId, loadouts, catalog, players)
            }
        }

    private val inventoryInputs = combine(
        teamLoadoutPack,
        teamRepository.getAllTeamsFlow(),
        slotIndex,
        selectedCategory,
        filterState,
    ) { pack, teams, slot, category, filter ->
        PartsInventoryInputs(pack, teams, slot, category, filter)
    }

    val uiState: StateFlow<PartsInventoryUiState> = combine(
        inventoryInputs,
        transientError,
        playerProgressRepository.observeWallet(),
        playerProgressRepository.observeProfile(),
    ) { inputs, err, wallet, profile ->
        val pack = inputs.pack
        val teams = inputs.teams
        val slot = inputs.slot
        val category = inputs.category
        val filter = inputs.filter
        val teamLabel = teams.find { it.id == pack.teamId }?.displayName
            ?: pack.teamId.removePrefix("team_").uppercase()
        val loadout = pack.loadouts.find { it.slotIndex == slot }
        val byId = pack.catalog.associateBy { it.id }
        val cap = loadout?.battleCapId?.let { byId[it] }
        val ring = loadout?.weightRingId?.let { byId[it] }
        val drv = loadout?.driverId?.let { byId[it] }
        val preview = computeBuildStatsUseCase(cap, ring, drv)
        val playerMap = pack.players.associateBy { it.partId }
        val visible = getVisiblePartsUseCase(
            parts = pack.catalog,
            playerById = playerMap,
            filter = filter,
            loadout = loadout,
            selectedCategory = category,
        )
        val summaries = pack.loadouts
            .sortedBy { it.slotIndex }
            .map { lo ->
                LoadoutSlotSummary(
                    slotIndex = lo.slotIndex,
                    equippedPartCount = listOfNotNull(lo.battleCapId, lo.weightRingId, lo.driverId).size,
                )
            }
        val emptyInv = pack.players.none { it.owned }
        val counts = PartCategory.entries.associateWith { pc ->
            pack.catalog.count { it.category == pc }
        }
        val ownedCounts = PartCategory.entries.associateWith { pc ->
            pack.catalog.count { p -> p.category == pc && playerMap[p.id]?.owned == true }
        }
        val w = wallet ?: WalletEntity(
            id = WalletEntity.SINGLETON_ID,
            gold = 0L,
            gems = 0L,
            chestKeys = 0,
            championshipTickets = 0,
        )
        PartsInventoryUiState(
            activeTeamId = pack.teamId,
            activeTeamDisplayName = teamLabel,
            teams = teams.sortedBy { it.id },
            activeSlotIndex = slot,
            selectedCategory = category,
            filterState = filter,
            visibleParts = visible,
            equippedBattleCap = cap,
            equippedWeightRing = ring,
            equippedDriver = drv,
            buildPreviewState = preview,
            loadoutSlotsSummary = summaries.ifEmpty { (0 until 3).map { LoadoutSlotSummary(it, 0) } },
            categoryCounts = counts,
            ownedCategoryCounts = ownedCounts,
            playerDisplayName = profile.displayName,
            playerLevel = profile.level,
            gold = w.gold,
            gems = w.gems,
            isLoading = false,
            isEmptyInventory = emptyInv,
            errorMessage = err,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PartsInventoryUiState.initial(StarterSetProvider.DEFAULT_TEAM_ID),
    )

    fun selectTeam(teamId: String) {
        viewModelScope.launch {
            playerProgressRepository.updateProfile { it.copy(activeTeamId = teamId) }
        }
    }

    fun selectSlot(index: Int) {
        val safe = index.coerceIn(0, 2)
        slotIndex.value = safe
        viewModelScope.launch {
            playerProgressRepository.updateProfile { it.copy(activeLoadoutSlot = safe) }
        }
    }

    fun selectCategory(category: PartCategory) {
        selectedCategory.value = category
    }

    fun updateSearchQuery(query: String) {
        filterState.update { it.copy(searchQuery = query) }
    }

    fun toggleOwnedOnly() {
        filterState.update { it.copy(showOwnedOnly = !it.showOwnedOnly) }
    }

    fun setRarityFilter(rarity: Int?) {
        filterState.update { it.copy(selectedRarity = rarity) }
    }

    fun setCombatTypeFilter(type: CombatType?) {
        filterState.update { it.copy(selectedCombatType = type) }
    }

    fun setSpinDirectionFilter(direction: SpinDirection?) {
        filterState.update { it.copy(selectedSpinDirection = direction) }
    }

    fun setSortMode(mode: InventorySortMode) {
        filterState.update { it.copy(sortMode = mode) }
    }

    fun clearError() {
        transientError.value = null
    }

    fun equipPart(partId: String) {
        viewModelScope.launch {
            val teamId = currentTeamId()
            val result = equipPartUseCase(
                partId = partId,
                expectedCategory = selectedCategory.value,
                teamId = teamId,
                slotIndex = slotIndex.value,
            )
            if (result.isFailure) {
                transientError.value = result.exceptionOrNull()?.message ?: "Unable to equip"
            } else {
                playerProgressRepository.updateAcademy { a ->
                    val m = a.tasks.toMutableMap()
                    val cur = m[com.zerotoler.rpgmenu.data.seed.AcademyTasks.EQUIP_PART]
                        ?: com.zerotoler.rpgmenu.domain.model.progress.AcademyTaskEntry()
                    m[com.zerotoler.rpgmenu.data.seed.AcademyTasks.EQUIP_PART] =
                        cur.copy(current = (cur.current + 1).coerceAtMost(1))
                    a.copy(tasks = m)
                }
            }
        }
    }

    fun refresh() {
        transientError.value = null
    }

    fun runAutoConfig() {
        viewModelScope.launch {
            val teamId = currentTeamId()
            autoConfigLoadoutUseCase(teamId, slotIndex.value)
        }
    }

    private suspend fun currentTeamId(): String {
        val id = playerProgressRepository.snapshotProfile().activeTeamId
        return id.takeIf { it.isNotBlank() } ?: StarterSetProvider.DEFAULT_TEAM_ID
    }
}
