package com.zerotoler.rpgmenu.ui.parts.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.zerotoler.rpgmenu.domain.model.CombatType
import com.zerotoler.rpgmenu.domain.model.InventoryFilterState
import com.zerotoler.rpgmenu.domain.model.InventorySortMode
import com.zerotoler.rpgmenu.domain.model.SpinDirection
import com.zerotoler.rpgmenu.ui.theme.TextMuted
import com.zerotoler.rpgmenu.ui.theme.TextPrimary

@Composable
fun InventoryToolbar(
    filter: InventoryFilterState,
    onSearchChange: (String) -> Unit,
    onToggleOwnedOnly: () -> Unit,
    onRarityChange: (Int?) -> Unit,
    onCombatChange: (CombatType?) -> Unit,
    onSpinChange: (SpinDirection?) -> Unit,
    onSortChange: (InventorySortMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    var rarityOpen by remember { mutableStateOf(false) }
    var sortOpen by remember { mutableStateOf(false) }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = filter.searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            singleLine = true,
            label = { Text("Search", color = TextMuted) },
            keyboardOptions = KeyboardOptions.Default.copy(
                capitalization = KeyboardCapitalization.None,
            ),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            TextButton(onClick = onToggleOwnedOnly) {
                Text(
                    if (filter.showOwnedOnly) "Owned: ON" else "Owned: ALL",
                    color = TextPrimary,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            Box {
                TextButton(onClick = { rarityOpen = true }) {
                    Text(
                        "Rarity: ${filter.selectedRarity ?: "ALL"}",
                        color = TextPrimary,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
                DropdownMenu(
                    expanded = rarityOpen,
                    onDismissRequest = { rarityOpen = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("All") },
                        onClick = {
                            onRarityChange(null)
                            rarityOpen = false
                        },
                    )
                    (1..5).forEach { r ->
                        DropdownMenuItem(
                            text = { Text(r.toString()) },
                            onClick = {
                                onRarityChange(r)
                                rarityOpen = false
                            },
                        )
                    }
                }
            }
            Box {
                TextButton(onClick = { sortOpen = true }) {
                    Text(
                        "Sort: ${filter.sortMode.name}",
                        color = TextPrimary,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
                DropdownMenu(
                    expanded = sortOpen,
                    onDismissRequest = { sortOpen = false },
                ) {
                    InventorySortMode.entries.forEach { mode ->
                        DropdownMenuItem(
                            text = { Text(mode.name) },
                            onClick = {
                                onSortChange(mode)
                                sortOpen = false
                            },
                        )
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            FilterChipText("Type: ALL") { onCombatChange(null) }
            FilterChipText("Atk") { onCombatChange(CombatType.ATTACK) }
            FilterChipText("Def") { onCombatChange(CombatType.DEFENSE) }
            FilterChipText("Sta") { onCombatChange(CombatType.STAMINA) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            FilterChipText("Spin: ALL") { onSpinChange(null) }
            FilterChipText("R") { onSpinChange(SpinDirection.RIGHT) }
            FilterChipText("L") { onSpinChange(SpinDirection.LEFT) }
        }
    }
}

@Composable
private fun FilterChipText(label: String, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
    }
}
