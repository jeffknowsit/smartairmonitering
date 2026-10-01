package com.smartair.data.database

import androidx.room.*
import com.smartair.data.model.AirStatus
import com.smartair.data.model.SensorReading
import kotlinx.coroutines.flow.Flow

@Dao
interface SensorReadingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reading: SensorReading): Long

    @Query("SELECT * FROM sensor_readings ORDER BY timestamp DESC LIMIT 1")
    fun getLatestReading(): Flow<SensorReading?>

    @Query("SELECT * FROM sensor_readings WHERE timestamp > :since ORDER BY timestamp ASC")
    fun getReadingsSince(since: Long): Flow<List<SensorReading>>

    @Query("SELECT * FROM sensor_readings WHERE timestamp > :since ORDER BY timestamp ASC")
    suspend fun getReadingsSinceSnapshot(since: Long): List<SensorReading>

    @Query("SELECT * FROM sensor_readings ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentReadings(limit: Int): List<SensorReading>

    @Query("SELECT COUNT(*) FROM sensor_readings WHERE timestamp > :since AND status != 'NORMAL'")
    suspend fun getWarningCountSince(since: Long): Int

    @Query("SELECT AVG(temperature) FROM sensor_readings WHERE timestamp > :since AND temperature IS NOT NULL")
    suspend fun getAvgTemperatureSince(since: Long): Float?

    @Query("SELECT AVG(humidity) FROM sensor_readings WHERE timestamp > :since AND humidity IS NOT NULL")
    suspend fun getAvgHumiditySince(since: Long): Float?

    @Query("SELECT AVG(dust) FROM sensor_readings WHERE timestamp > :since AND dust IS NOT NULL")
    suspend fun getAvgDustSince(since: Long): Float?

    @Query("SELECT AVG(gas) FROM sensor_readings WHERE timestamp > :since AND gas IS NOT NULL")
    suspend fun getAvgGasSince(since: Long): Float?

    @Query("DELETE FROM sensor_readings WHERE timestamp < :before")
    suspend fun deleteOlderThan(before: Long)
}

class Converters {
    @TypeConverter
    fun fromAirStatus(status: AirStatus): String = status.name

    @TypeConverter
    fun toAirStatus(value: String): AirStatus = try {
        AirStatus.valueOf(value)
    } catch (e: Exception) {
        AirStatus.NORMAL
    }
}

@Database(entities = [SensorReading::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class SmartAirDatabase : RoomDatabase() {
    abstract fun sensorReadingDao(): SensorReadingDao

    companion object {
        @Volatile
        private var INSTANCE: SmartAirDatabase? = null

        fun getDatabase(context: android.content.Context): SmartAirDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SmartAirDatabase::class.java,
                    "smartair_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
