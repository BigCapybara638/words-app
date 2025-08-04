package com.example.wordsapp.data

data class Word(
    val id: Int,
    val original: String,
    val translate: String,
    val idCategory: Int,
    val indexLearning: Int = 0,
) {
}