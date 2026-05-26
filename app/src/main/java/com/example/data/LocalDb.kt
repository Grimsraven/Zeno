package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val timeOfDay: String, // "Morning", "Afternoon", "Evening"
    val priority: Int,     // 1 = High, 2 = Medium, 3 = Low
    val completed: Boolean = false,
    val date: String,      // YYYY-MM-DD
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val streak: Int = 0,
    val lastCompletedDate: String = "", // YYYY-MM-DD
    // Comma-separated YYYY-MM-DD strings represent completed days
    val completedDates: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE date = :date ORDER BY priority ASC, createdAt ASC")
    fun getTasksForDate(date: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE date = :date AND completed = 0 ORDER BY priority ASC, createdAt ASC LIMIT 3")
    fun getTop3TasksForDate(date: String): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Int)
}

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY createdAt DESC")
    fun getAllHabits(): Flow<List<Habit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit)

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)
    
    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabitById(id: Int)
}

@Database(entities = [Task::class, Habit::class], version = 1, exportSchema = false)
abstract class ZenoDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun habitDao(): HabitDao

    companion object {
        @Volatile
        private var INSTANCE: ZenoDatabase? = null

        fun getDatabase(context: Context): ZenoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ZenoDatabase::class.java,
                    "zeno_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
