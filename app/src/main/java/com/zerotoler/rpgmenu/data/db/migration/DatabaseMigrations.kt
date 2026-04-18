package com.zerotoler.rpgmenu.data.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.zerotoler.rpgmenu.data.db.entity.WalletEntity

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `wallet` (
                `id` INTEGER NOT NULL,
                `gold` INTEGER NOT NULL,
                `gems` INTEGER NOT NULL,
                `chestKeys` INTEGER NOT NULL,
                `championshipTickets` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT OR IGNORE INTO wallet (id, gold, gems, chestKeys, championshipTickets)
            VALUES (${WalletEntity.SINGLETON_ID}, 5000, 120, 3, 5)
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `kv_store` (
                `key` TEXT NOT NULL,
                `value` TEXT NOT NULL,
                PRIMARY KEY(`key`)
            )
            """.trimIndent(),
        )
    }
}
