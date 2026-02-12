package com.bowlingclub.app.data.local.dao

import androidx.room.*
import com.bowlingclub.app.data.local.entity.Setting
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingDao {
    @Query("SELECT * FROM settings WHERE key = :key")
    fun getSetting(key: String): Flow<Setting?>

    @Query("SELECT * FROM settings")
    fun getAllSettings(): Flow<List<Setting>>

    @Query("SELECT value FROM settings WHERE key = :key")
    suspend fun getSettingValue(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(setting: Setting)

    @Query("DELETE FROM settings WHERE key = :key")
    suspend fun delete(key: String)

    @Query("DELETE FROM settings")
    suspend fun deleteAll()
}
