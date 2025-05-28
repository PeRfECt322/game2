package com.example.game2

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "game_results")
data class GameResult(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val word: String,
    val result: String, // "Победа" или "Поражение"
    val date: Long = System.currentTimeMillis()
)
