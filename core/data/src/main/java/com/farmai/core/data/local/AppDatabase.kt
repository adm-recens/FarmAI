package com.farmai.core.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.farmai.core.data.local.dao.BrokerDao
import com.farmai.core.data.local.dao.FarmerDao
import com.farmai.core.data.local.dao.ReceiptDao
import com.farmai.core.data.local.dao.ReportDao
import com.farmai.core.data.local.dao.SupplierDao
import com.farmai.core.data.local.dao.ValidationSnapshotDao
import com.farmai.core.data.local.dao.BatchDao
import com.farmai.core.data.local.dao.ReceiptJobDao
import com.farmai.core.data.local.entity.BrokerEntity
import com.farmai.core.data.local.entity.DeductionEntity
import com.farmai.core.data.local.entity.FarmerEntity
import com.farmai.core.data.local.entity.ReceiptEntity
import com.farmai.core.data.local.entity.ReceiptLineItemEntity
import com.farmai.core.data.local.entity.BatchEntity
import com.farmai.core.data.local.entity.ReceiptJobEntity
import com.farmai.core.data.local.entity.SupplierEntity
import com.farmai.core.data.local.entity.ValidationSnapshotEntity
import com.farmai.core.data.local.migration.DatabaseMigrations

@Database(
    entities = [
        FarmerEntity::class,
        BrokerEntity::class,
        ReceiptEntity::class,
        ReceiptLineItemEntity::class,
        DeductionEntity::class,
        BatchEntity::class,
        ReceiptJobEntity::class,
        SupplierEntity::class,
        ValidationSnapshotEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun farmerDao(): FarmerDao
    abstract fun brokerDao(): BrokerDao
    abstract fun receiptDao(): ReceiptDao
    abstract fun reportDao(): ReportDao
    abstract fun batchDao(): BatchDao
    abstract fun receiptJobDao(): ReceiptJobDao
    abstract fun supplierDao(): SupplierDao
    abstract fun validationSnapshotDao(): ValidationSnapshotDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "farmai_database"
                ).addMigrations(*DatabaseMigrations.ALL)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
