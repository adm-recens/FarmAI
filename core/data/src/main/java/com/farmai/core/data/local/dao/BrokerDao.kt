package com.farmai.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import androidx.room.Update
import com.farmai.core.data.local.entity.BrokerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BrokerDao {
    @Query("SELECT * FROM brokers ORDER BY name ASC")
    fun getAllBrokers(): Flow<List<BrokerEntity>>

    @Query("SELECT * FROM brokers WHERE id = :id")
    fun getBrokerById(id: String): Flow<BrokerEntity?>

    @Query("SELECT * FROM brokers WHERE name LIKE :query OR phone LIKE :query ORDER BY name ASC")
    fun searchBrokers(query: String): List<BrokerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBroker(broker: BrokerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBrokers(brokers: List<BrokerEntity>)

    @Update
    suspend fun updateBroker(broker: BrokerEntity)

    @Delete
    suspend fun deleteBroker(broker: BrokerEntity)

    @Query("DELETE FROM brokers WHERE id = :id")
    suspend fun deleteBrokerById(id: String)
}