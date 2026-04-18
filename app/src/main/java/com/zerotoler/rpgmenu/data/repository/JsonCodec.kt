package com.zerotoler.rpgmenu.data.repository

import com.zerotoler.rpgmenu.domain.model.progress.AcademyProgress
import com.zerotoler.rpgmenu.domain.model.progress.BattlePassProgress
import com.zerotoler.rpgmenu.domain.model.progress.CollectionProgress
import com.zerotoler.rpgmenu.domain.model.progress.EventProgress
import com.zerotoler.rpgmenu.domain.model.progress.IdempotencyLedger
import com.zerotoler.rpgmenu.domain.model.progress.MailProgress
import com.zerotoler.rpgmenu.domain.model.progress.MatchHistoryProgress
import com.zerotoler.rpgmenu.domain.model.progress.ProfileProgress
import com.zerotoler.rpgmenu.domain.model.progress.RankedProgress
import com.zerotoler.rpgmenu.domain.model.progress.TalentProgress
import kotlinx.serialization.json.Json

internal val progressJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

internal inline fun <reified T> Json.decodeOrDefault(raw: String?, default: T): T {
    if (raw.isNullOrBlank()) return default
    return runCatching { decodeFromString<T>(raw) }.getOrDefault(default)
}

internal fun profileDefault(): ProfileProgress = ProfileProgress()
internal fun talentDefault(): TalentProgress = TalentProgress()
internal fun collectionDefault(): CollectionProgress = CollectionProgress()
internal fun battlePassDefault(): BattlePassProgress = BattlePassProgress()
internal fun academyDefault(): AcademyProgress = AcademyProgress()
internal fun eventsDefault(): EventProgress = EventProgress()
internal fun mailDefault(): MailProgress = MailProgress()
internal fun rankedDefault(): RankedProgress = RankedProgress()
internal fun matchHistoryDefault(): MatchHistoryProgress = MatchHistoryProgress()
internal fun idempotencyDefault(): IdempotencyLedger = IdempotencyLedger()
