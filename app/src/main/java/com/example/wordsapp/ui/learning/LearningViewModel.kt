package com.example.wordsapp.ui.learning

import android.app.Application
import android.database.Cursor
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordsapp.data.Word
import com.example.wordsapp.db.DbHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class LearningViewModel(application: Application) : AndroidViewModel(application) {
    private val dbHelper = DbHelper(application.applicationContext, null)

    private val _words = MutableLiveData<List<Word>>()
    val words: LiveData<List<Word>> = _words

    private val _currentWord = MutableLiveData<Word?>()
    val currentWord: LiveData<Word?> = _currentWord

    var currentPosition = 0
    var filteredWords = listOf<Word>()

    fun loadWordsWithProbability() {
        viewModelScope.launch(Dispatchers.IO) {
            // 1. Получаем все слова напрямую из DbHelper
            val allWords = getAllWordsFromDb()

            // 2. Фильтруем по вероятности
            filteredWords = selectWordsByProbability(allWords)

            // 3. Обновляем LiveData в основном потоке
            withContext(Dispatchers.Main) {
                _words.value = filteredWords
                _currentWord.value = filteredWords.firstOrNull()
                currentPosition = 0
            }
        }
    }

    private suspend fun getAllWordsFromDb() = withContext(Dispatchers.IO) {
        dbHelper.getAllWords()
    }

    private fun selectWordsByProbability(allWords: List<Word>): List<Word> {
        if (allWords.isEmpty()) return emptyList()

        val random = Random(System.currentTimeMillis())
        val weightedWords = allWords.flatMap { word ->
            // Для index=1 - 10 копий, index=10 - 1 копия
            List(11 - word.indexLearning) { word }
        }.shuffled(random)

        return weightedWords.distinctBy { it.id }.take(20)
    }

    fun nextWord() {
        if (currentPosition < filteredWords.size - 1) {
            currentPosition++
            _currentWord.value = filteredWords[currentPosition]
        }
    }

    fun previousWord() {
        if (currentPosition > 0) {
            currentPosition--
            _currentWord.value = filteredWords[currentPosition]
        }
    }
}