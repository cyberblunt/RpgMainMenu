package com.zerotoler.rpgmenu.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.zerotoler.rpgmenu.data.db.dao.CatalogPartDao
import com.zerotoler.rpgmenu.data.db.dao.KvStoreDao
import com.zerotoler.rpgmenu.data.db.dao.LoadoutDao
import com.zerotoler.rpgmenu.data.db.dao.PlayerPartDao
import com.zerotoler.rpgmenu.data.db.dao.TeamDao
import com.zerotoler.rpgmenu.data.db.dao.WalletDao
import com.zerotoler.rpgmenu.data.db.entity.CatalogPartEntity
import com.zerotoler.rpgmenu.data.db.entity.KvStoreEntity
import com.zerotoler.rpgmenu.data.db.entity.LoadoutEntity
import com.zerotoler.rpgmenu.data.db.entity.PlayerPartEntity
import com.zerotoler.rpgmenu.data.db.entity.TeamEntity
import com.zerotoler.rpgmenu.data.db.entity.WalletEntity

@Database(
    entities = [
        CatalogPartEntity::class,
        PlayerPartEntity::class,
        LoadoutEntity::class,
        TeamEntity::class,
        WalletEntity::class,
        KvStoreEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun catalogPartDao(): CatalogPartDao
    abstract fun playerPartDao(): PlayerPartDao
    abstract fun loadoutDao(): LoadoutDao
    abstract fun teamDao(): TeamDao
    abstract fun walletDao(): WalletDao
    abstract fun kvStoreDao(): KvStoreDao
}
