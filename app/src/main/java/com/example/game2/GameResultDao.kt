package com.example.game2

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface GameResultDao {
    @Insert
    suspend fun insert(result: GameResult)

    @Query("SELECT * FROM game_results ORDER BY date DESC")
    suspend fun getAll(): List<GameResult>
}