package com.zerotoler.rpgmenu.ui.parts.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zerotoler.rpgmenu.domain.model.VisibleInventoryItem

@Composable
fun PartGrid(
    visibleItems: List<VisibleInventoryItem>,
    onEquipPart: (String) -> Unit,
    onOpenPartDetail: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 78.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(
            items = visibleItems,
            key = { it.part.id },
        ) { row ->
            PartCard(
                item = row,
                onEquip = { onEquipPart(row.part.id) },
                onOpenDetail = onOpenPartDetail?.let { cb -> { cb(row.part.id) } },
            )
        }
    }
}
