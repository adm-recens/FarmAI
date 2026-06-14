package com.farmai.core.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.farmai.core.data.local.dao.BrokerDao
import com.farmai.core.data.local.dao.FarmerDao
import com.farmai.core.data.local.dao.ReceiptDao
import com.farmai.core.data.local.dao.ReportDao
import com.farmai.core.data.local.entity.BrokerEntity
import com.farmai.core.data.local.entity.DeductionEntity
import com.farmai.core.data.local.entity.FarmerEntity
import com.farmai.core.data.local.entity.ReceiptEntity
import com.farmai.core.data.local.entity.ReceiptLineItemEntity

@Database(
    entities = [
        FarmerEntity::class,
        BrokerEntity::class,
        ReceiptEntity::class,
        ReceiptLineItemEntity::class,
        DeductionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun farmerDao(): FarmerDao
    abstract fun brokerDao(): BrokerDao
    abstract fun receiptDao(): ReceiptDao
    abstract fun reportDao(): ReportDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "farmai_database"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
