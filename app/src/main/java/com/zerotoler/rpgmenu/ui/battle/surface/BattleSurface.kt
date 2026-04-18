package com.zerotoler.rpgmenu.ui.battle.surface

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.zerotoler.rpgmenu.domain.engine.BattleEngine
import com.zerotoler.rpgmenu.domain.model.battle.BattleRenderSnapshot

@Composable
fun BattleSurface(
    engine: BattleEngine?,
    paused: Boolean,
    onSnapshot: (BattleRenderSnapshot) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snapshotCb = remember(onSnapshot) { onSnapshot }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            BattleSurfaceView(ctx).apply {
                setEngine(engine, snapshotCb)
                setPaused(paused)
            }
        },
        update = { view ->
            view.setEngine(engine, snapshotCb)
            view.setPaused(paused)
        },
    )

    DisposableEffect(engine) {
        onDispose {
            // Let SurfaceView stop its thread in surfaceDestroyed; nothing needed here.
        }
    }
}

