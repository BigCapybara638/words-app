package com.example.wordsapp.data

class WordCategory(
    val id: Int,
    val name: String,
    val selected: Int = 0,   // 0 - не выбрана, 1 - выбрана
) {
}