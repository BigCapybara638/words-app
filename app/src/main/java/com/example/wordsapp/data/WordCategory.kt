package com.example.wordsapp.data

class WordCategory(
    val id: Int,
    val name: String,
    var selected: Int = 0,   // 0 - не выбрана, 1 - выбрана
    var isExpanded: Boolean = false,
) {
}