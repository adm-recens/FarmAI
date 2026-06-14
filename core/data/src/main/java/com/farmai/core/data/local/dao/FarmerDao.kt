package com.farmai.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import androidx.room.Update
import com.farmai.core.data.local.entity.FarmerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FarmerDao {
    @Query("SELECT * FROM farmers ORDER BY name ASC")
    fun getAllFarmers(): Flow<List<FarmerEntity>>

    @Query("SELECT * FROM farmers WHERE id = :id")
    fun getFarmerById(id: String): Flow<FarmerEntity?>

    @Query("SELECT * FROM farmers WHERE code = :code")
    fun getFarmerByCode(code: String): FarmerEntity?

    @Query("SELECT * FROM farmers WHERE name LIKE :query OR code LIKE :query OR phone LIKE :query ORDER BY name ASC")
    fun searchFarmers(query: String): List<FarmerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFarmer(farmer: FarmerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllFarmers(farmers: List<FarmerEntity>)

    @Update
    suspend fun updateFarmer(farmer: FarmerEntity)

    @Delete
    suspend fun deleteFarmer(farmer: FarmerEntity)

    @Query("DELETE FROM farmers WHERE id = :id")
    suspend fun deleteFarmerById(id: String)
}