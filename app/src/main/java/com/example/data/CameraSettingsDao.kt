package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CameraSettingsDao {
    @Query("SELECT * FROM camera_settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<CameraSettings?>

    @Query("SELECT * FROM camera_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsDirect(): CameraSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: CameraSettings)
}
