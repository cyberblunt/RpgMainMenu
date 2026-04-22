package com.zerotoler.rpgmenu.ui.battle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zerotoler.rpgmenu.data.repository.BattleSessionRepository
import com.zerotoler.rpgmenu.data.repository.RoundResolveDestination
import com.zerotoler.rpgmenu.domain.engine.BattleStatNormalization
import com.zerotoler.rpgmenu.domain.engine.BattleEngine
import com.zerotoler.rpgmenu.domain.model.battle.BattleOutcome
import com.zerotoler.rpgmenu.domain.model.battle.BattleRenderSnapshot
import com.zerotoler.rpgmenu.domain.model.battle.VisualParticle
import com.zerotoler.rpgmenu.domain.model.battlesession.BattleRoundResult
import com.zerotoler.rpgmenu.domain.usecase.BuildBattleTopFromSelectedLoadoutUseCase
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RealBattleViewModel(
    private val repository: BattleSessionRepository,
    private val buildBattleTop: BuildBattleTopFromSelectedLoadoutUseCase,
) : ViewModel() {

    // Temporary equalization to keep player/enemy in comparable conditions during tuning.
    private val debugEqualizeStats = true

    private val resolving = AtomicBoolean(false)
    private val outcomeRecorded = AtomicBoolean(false)

    private val visualParticles = mutableListOf<VisualParticle>()

    private val _ui = MutableStateFlow(
        RealBattleUiState(
            render = BattleRenderSnapshot.empty(),
            engine = null,
            playerDisplayName = "",
            enemyDisplayName = "",
            isBusy = false,
            fatalError = null,
            autoBattle = false,
            visualParticles = emptyList(),
        ),
    )
    val uiState: StateFlow<RealBattleUiState> = _ui.asStateFlow()

    private var engine: BattleEngine? = null

    init {
        viewModelScope.launch {
            try {
                val snap = repository.session.value
                val roundIdx = snap?.activeBattleRoundIndex
                if (snap == null || roundIdx == null) {
                    _ui.update { it.copy(fatalError = "missing_session") }
                    return@launch
                }
                val round = snap.rounds[roundIdx]
                val slot = round.selectedPlayerSlotIndex
                val playerTop = snap.playerTops.getOrNull(slot ?: -1)
                if (slot == null || playerTop == null || !playerTop.isComplete) {
                    _ui.update { it.copy(fatalError = "bad_loadout") }
                    return@launch
                }
                val pStats = withContext(Dispatchers.IO) {
                    buildBattleTop.fromPlayerTop(playerTop)
                }
                val eStats = withContext(Dispatchers.IO) {
                    buildBattleTop.fromOpponentTopParts(round.opponentTop)
                }
                val (pNorm, eNorm) = if (debugEqualizeStats) {
                    BattleStatNormalization.equalizeBothForDebug(pStats, eStats)
                } else {
                    pStats to eStats
                }
                val eng = BattleEngine(
                    playerBase = pNorm,
                    enemyBase = eNorm,
                    sessionSeed = snap.sessionId.hashCode(),
                    roundIndex = roundIdx,
                )
                eng.onBattleCollision = { wx, wy, _, attackDash ->
                    synchronized(visualParticles) {
                        createSparks(visualParticles, wx.toFloat(), wy.toFloat(), attackDash)
                    }
                }
                engine = eng
                _ui.update {
                    it.copy(
                        render = eng.buildSnapshot(),
                        engine = eng,
                        playerDisplayName = playerTop.displayName,
                        enemyDisplayName = round.opponentTop.name,
                        fatalError = null,
                        visualParticles = emptyList(),
                    )
                }
            } catch (_: Throwable) {
                _ui.update { it.copy(fatalError = "load_failed") }
            }
        }
    }

    fun setPaused(@Suppress("UNUSED_PARAMETER") pause: Boolean) {
        // Gameplay loop is owned by SurfaceView; this remains for lifecycle wiring.
    }

    fun onSnapshot(snapshot: BattleRenderSnapshot) {
        val dt = 1f / 60f
        val particlesCopy = synchronized(visualParticles) {
            var i = 0
            while (i < visualParticles.size) {
                val p = visualParticles[i]
                p.x += p.vx * dt
                p.y += p.vy * dt
                p.life -= p.decay
                if (p.life <= 0f) {
                    visualParticles.removeAt(i)
                } else {
                    i++
                }
            }
            visualParticles.toList()
        }
        _ui.update { it.copy(render = snapshot, visualParticles = particlesCopy) }
    }

    fun toggleAutoBattle() {
        _ui.update { it.copy(autoBattle = !it.autoBattle) }
    }

    fun onSuperAbility() {
        engine?.activateSuperAbility()
    }

    fun cancelBattle() {
        viewModelScope.launch {
            repository.cancelBattleRound()
        }
    }

    fun confirmResultNavigate(
        onContinueSelection: () -> Unit,
        onSessionComplete: () -> Unit,
    ) {
        val eng = engine ?: return
        val o = eng.currentOutcome()
        if (o == BattleOutcome.NONE) return
        if (!outcomeRecorded.compareAndSet(false, true)) return
        val roundResult = when (o) {
            BattleOutcome.PLAYER_WIN -> BattleRoundResult.WIN
            BattleOutcome.PLAYER_LOSS -> BattleRoundResult.LOSS
            BattleOutcome.NONE -> return
        }
        viewModelScope.launch {
            if (!resolving.compareAndSet(false, true)) return@launch
            _ui.update { it.copy(isBusy = true) }
            try {
                val dest = runCatching { repository.resolveCurrentRound(roundResult) }.getOrNull() ?: return@launch
                when (dest) {
                    RoundResolveDestination.CONTINUE_TO_NEXT_ROUND -> onContinueSelection()
                    RoundResolveDestination.SESSION_COMPLETE -> onSessionComplete()
                }
            } finally {
                _ui.update { it.copy(isBusy = false) }
                resolving.set(false)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
    }

    private fun createSparks(
        particles: MutableList<VisualParticle>,
        px: Float,
        py: Float,
        isAttackDash: Boolean,
    ) {
        val count = if (isAttackDash) 10 else 25
        val color = if (isAttackDash) (0xFFFF0000.toInt()) else (0xFFFFFFFF.toInt())
        repeat(count) {
            val angle = Math.random() * Math.PI * 2
            val speed = Math.random() * 5.0
            particles.add(
                VisualParticle(
                    x = px,
                    y = py,
                    vx = (Math.cos(angle) * speed).toFloat(),
                    vy = (Math.sin(angle) * speed).toFloat(),
                    life = 1.0f,
                    decay = (Math.random() * 0.04 + 0.01).toFloat(),
                    size = (Math.random() * 4 + 1).toFloat(),
                    colorArgb = color,
                ),
            )
        }
    }
}
