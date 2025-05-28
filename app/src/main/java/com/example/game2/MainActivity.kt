package com.example.game2

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var wordDisplayTextView: TextView
    private lateinit var wordLengthTextView: TextView
    private lateinit var attemptsLeftTextView: TextView
    private lateinit var usedLettersTextView: TextView
    private lateinit var statusTextView: TextView
    private lateinit var letterEditText: EditText
    private lateinit var guessButton: Button
    private lateinit var restartButton: Button
    private lateinit var database: GameDatabase

    private var selectedWord = ""
    private var guessedLetters = mutableSetOf<Char>()
    private var incorrectGuesses = 0
    private val maxAttempts = 6

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Инициализация views
        wordDisplayTextView = findViewById(R.id.wordDisplayTextView)
        wordLengthTextView = findViewById(R.id.wordLengthTextView)
        attemptsLeftTextView = findViewById(R.id.attemptsLeftTextView)
        usedLettersTextView = findViewById(R.id.usedLettersTextView)
        statusTextView = findViewById(R.id.statusTextView)
        letterEditText = findViewById(R.id.letterEditText)
        guessButton = findViewById(R.id.guessButton)
        restartButton = findViewById(R.id.restartButton)

        // Установка текстов из ресурсов
        findViewById<TextView>(R.id.titleTextView).text = getString(R.string.game_title)
        letterEditText.hint = getString(R.string.letter_hint)
        guessButton.text = getString(R.string.guess_button)
        restartButton.text = getString(R.string.restart_button)

        database = GameDatabase.getDatabase(this)

        startNewGame()

        findViewById<Button>(R.id.historyButton).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        guessButton.setOnClickListener {
            guessLetter()
        }

        restartButton.setOnClickListener {
            startNewGame()
        }
    }

    private fun startNewGame() {
        val words = resources.getStringArray(R.array.game_words)
        selectedWord = words.random().toLowerCase()
        guessedLetters.clear()
        incorrectGuesses = 0

        wordLengthTextView.text = getString(R.string.word_length, selectedWord.length)
        updateWordDisplay()
        attemptsLeftTextView.text = getString(R.string.attempts_left, maxAttempts - incorrectGuesses)
        usedLettersTextView.text = getString(R.string.used_letters, "")
        statusTextView.text = getString(R.string.enter_letter)
        letterEditText.text.clear()
        restartButton.visibility = View.GONE
        guessButton.isEnabled = true
    }

    private fun guessLetter() {
        val input = letterEditText.text.toString().toLowerCase()

        if (input.isEmpty()) {
            Toast.makeText(this, "Введите букву", Toast.LENGTH_SHORT).show()
            return
        }

        val letter = input[0]

        if (!letter.isLetter() || !letter.toString().matches("[а-яё]".toRegex())) {
            Toast.makeText(this, "Пожалуйста, введите русскую букву", Toast.LENGTH_SHORT).show()
            letterEditText.text.clear()
            return
        }

        if (letter in guessedLetters) {
            Toast.makeText(this, "Вы уже пробовали эту букву", Toast.LENGTH_SHORT).show()
            letterEditText.text.clear()
            return
        }

        guessedLetters.add(letter)
        updateUsedLetters()

        if (letter in selectedWord) {
            updateWordDisplay()
            checkWin()
        } else {
            incorrectGuesses++
            attemptsLeftTextView.text = getString(R.string.attempts_left, maxAttempts - incorrectGuesses)
            updateStatus()
            checkLose()
        }

        letterEditText.text.clear()
    }

    private fun updateWordDisplay() {
        val display = StringBuilder()
        for (char in selectedWord) {
            if (char in guessedLetters) {
                display.append("$char ")
            } else {
                display.append("_ ")
            }
        }
        wordDisplayTextView.text = display.toString().trim()
    }

    private fun updateUsedLetters() {
        val usedLetters = guessedLetters.sorted().joinToString(", ")
        usedLettersTextView.text = getString(R.string.used_letters, usedLetters)
    }

    private fun updateStatus() {
        val statusMessages = resources.getStringArray(R.array.status_messages)
        statusTextView.text = if (incorrectGuesses < statusMessages.size) {
            statusMessages[incorrectGuesses]
        } else {
            statusMessages.last()
        }
    }

    private fun checkWin() {
        val allLettersGuessed = selectedWord.all { it in guessedLetters }
        if (allLettersGuessed) {
            statusTextView.text = getString(R.string.win_message)
            guessButton.isEnabled = false
            restartButton.visibility = View.VISIBLE

            // Запись в базу данных
            lifecycleScope.launch {
                database.gameResultDao().insert(
                    GameResult(word = selectedWord, result = "Победа", date = System.currentTimeMillis())
                )
            }
        }
    }

    private fun checkLose() {
        if (incorrectGuesses >= maxAttempts) {
            statusTextView.text = getString(R.string.lose_message, selectedWord)
            wordDisplayTextView.text = selectedWord
            guessButton.isEnabled = false
            restartButton.visibility = View.VISIBLE

            // Запись в базу данных
            lifecycleScope.launch {
                database.gameResultDao().insert(
                    GameResult(word = selectedWord, result = "Поражение", date = System.currentTimeMillis())
                )
            }
        }
    }
}
