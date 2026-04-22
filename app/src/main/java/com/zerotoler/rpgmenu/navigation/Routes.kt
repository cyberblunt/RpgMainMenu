package com.zerotoler.rpgmenu.navigation

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Explicit route constants for Navigation Compose — avoids string typos across the app.
 */
object Routes {
    const val SPLASH = "splash"
    const val ROOT = "root"
    const val MAIN = "main"

    const val PROFILE = "profile"
    const val GOLD = "gold"
    const val GOLD_PLUS = "gold_plus"
    const val GEMS = "gems"
    const val GEMS_PLUS = "gems_plus"
    const val MERCHANT = "merchant"
    const val EVENTS = "events"
    const val EVENT_MISSIONS = "event_missions"
    const val EVENT_BOARD = "event_board"
    const val MISSIONS = "missions"
    const val MENU = "menu"

    /** Standalone Canvas + Choreographer prototype (logical arena coords). */
    const val SPIN_TOP_ARENA = "spin_top_arena"
    const val CHAT_TICKER = "chat_ticker"
    const val FREE_CHEST = "free_chest"
    const val CHEST = "chest"
    const val EMPTY_SLOT_1 = "empty_slot_1"
    const val EMPTY_SLOT_2 = "empty_slot_2"
    const val EMPTY_SLOT_3 = "empty_slot_3"
    const val EMPTY_SLOT_4 = "empty_slot_4"
    const val ADVENTURE = "adventure"
    const val SUPER_CHAMPIONSHIP = "super_championship"
    const val RANKED_LADDER = "ranked_ladder"
    const val BOTTOM_NAV_1 = "bottom_nav_1"
    const val BOTTOM_NAV_2 = "bottom_nav_2"
    const val BOTTOM_NAV_BATTLE = "bottom_nav_battle"
    const val BOTTOM_NAV_4 = "bottom_nav_4"
    const val BOTTOM_NAV_5 = "bottom_nav_5"

    const val MAIL = "mail"
    const val ACADEMY = "academy"
    const val BATTLE_PASS = "battle_pass"
    const val TALENT_TREE = "talent_tree"
    const val COLLECTION_CODEX = "collection_codex"
    const val PART_ENHANCE = "part_enhance"
    const val PART_DETAIL = "part_detail"

    /** @deprecated Use [preBattle] — kept for readability in migrations. */
    const val BATTLE_SESSION_BASE = "pre_battle"

    /**
     * Pre-battle team selection (3 rounds). Arguments are URL-encoded.
     */
    const val PRE_BATTLE_SELECTION = "pre_battle/{mode}/{opponentToken}"

    /** Real-time spin-top combat for the current round. */
    const val BATTLE = "battle"

    /** End-of-session summary after 3 rounds. */
    const val BATTLE_SESSION_RESULT = "battle_session_result"

    fun partDetail(partId: String): String {
        val enc = URLEncoder.encode(partId, StandardCharsets.UTF_8.toString())
        return "$PART_DETAIL/$enc"
    }

    /**
     * Entry to the pre-battle / 3-round session flow (replaces the old battle placeholder route).
     */
    fun preBattle(mode: String, opponentToken: String): String {
        val encMode = URLEncoder.encode(mode, StandardCharsets.UTF_8.toString())
        val encTok = URLEncoder.encode(opponentToken, StandardCharsets.UTF_8.toString())
        return "pre_battle/$encMode/$encTok"
    }
}
