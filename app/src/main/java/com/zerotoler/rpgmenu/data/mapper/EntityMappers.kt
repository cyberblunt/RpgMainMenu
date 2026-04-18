package com.zerotoler.rpgmenu.data.mapper

import com.zerotoler.rpgmenu.data.db.entity.CatalogPartEntity
import com.zerotoler.rpgmenu.data.db.entity.LoadoutEntity
import com.zerotoler.rpgmenu.data.db.entity.PlayerPartEntity
import com.zerotoler.rpgmenu.data.db.entity.TeamEntity
import com.zerotoler.rpgmenu.domain.model.CombatType
import com.zerotoler.rpgmenu.domain.model.DriverClass
import com.zerotoler.rpgmenu.domain.model.Loadout
import com.zerotoler.rpgmenu.domain.model.PartBase
import com.zerotoler.rpgmenu.domain.model.PartCategory
import com.zerotoler.rpgmenu.domain.model.PartStats
import com.zerotoler.rpgmenu.domain.model.PlayerPartState
import com.zerotoler.rpgmenu.domain.model.RingClass
import com.zerotoler.rpgmenu.domain.model.SpinDirection
import com.zerotoler.rpgmenu.domain.model.Team

fun CatalogPartEntity.toDomain(): PartBase =
    PartBase(
        id = id,
        name = name,
        category = PartCategory.valueOf(category),
        rarity = rarity,
        combatType = combatType.toCombatType(),
        spinDirection = spinDirection.toSpinDirection(),
        ringClass = ringClass?.toRingClass(),
        driverClass = driverClass?.toDriverClass(),
        tags = tagsJson.split('|').filter { it.isNotBlank() },
        stats = PartStats(
            health = health,
            attack = attack,
            defense = defense,
            stamina = stamina,
            intervalSeconds = intervalSeconds,
            weightGrams = weightGrams,
        ),
    )

fun PlayerPartEntity.toDomain(): PlayerPartState =
    PlayerPartState(
        partId = partId,
        owned = owned,
        level = level,
        shardCount = shardCount,
        copies = copies,
        isFavorite = isFavorite,
        obtainedAtEpochMillis = obtainedAtEpochMillis,
    )

fun LoadoutEntity.toDomain(): Loadout =
    Loadout(
        teamId = teamId,
        slotIndex = slotIndex,
        name = name,
        battleCapId = battleCapId,
        weightRingId = weightRingId,
        driverId = driverId,
    )

fun TeamEntity.toDomain(): Team =
    Team(
        id = id,
        displayName = displayName,
    )

fun PartBase.toEntity(): CatalogPartEntity =
    CatalogPartEntity(
        id = id,
        name = name,
        category = category.name,
        rarity = rarity,
        combatType = combatType.name,
        spinDirection = spinDirection.name,
        ringClass = ringClass?.name,
        driverClass = driverClass?.name,
        tagsJson = tags.joinToString("|"),
        health = stats.health,
        attack = stats.attack,
        defense = stats.defense,
        stamina = stats.stamina,
        intervalSeconds = stats.intervalSeconds,
        weightGrams = stats.weightGrams,
    )

fun Loadout.toEntity(): LoadoutEntity =
    LoadoutEntity(
        teamId = teamId,
        slotIndex = slotIndex,
        name = name,
        battleCapId = battleCapId,
        weightRingId = weightRingId,
        driverId = driverId,
    )

private fun String.toCombatType(): CombatType =
    runCatching { CombatType.valueOf(this) }.getOrDefault(CombatType.UNKNOWN)

private fun String.toSpinDirection(): SpinDirection =
    runCatching { SpinDirection.valueOf(this) }.getOrDefault(SpinDirection.UNKNOWN)

private fun String.toRingClass(): RingClass? =
    runCatching { RingClass.valueOf(this) }.getOrNull()

private fun String.toDriverClass(): DriverClass? =
    runCatching { DriverClass.valueOf(this) }.getOrNull()
