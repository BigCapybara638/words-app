package com.example.wordsapp.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.wordsapp.data.Word
import com.example.wordsapp.data.WordCategory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DbHelper(val context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, "app", factory, 1)
{
    override fun onCreate(db: SQLiteDatabase?) {
        val query = "CREATE TABLE words (id INT PRIMARY KEY, original TEXT, translate TEXT, idCategory INT, indexLearning INT)"
        val query2 = "CREATE TABLE category (id INT PRIMARY KEY, name TEXT, selected INT)"
        db!!.execSQL(query)
        db.execSQL(query2)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS words")
        db.execSQL("DROP TABLE IF EXISTS category")
        onCreate(db)    }

    // добавление слова
    fun addWord(word: Word) {
        val values = ContentValues()
        values.put("original", word.original)
        values.put("translate", word.translate)
        values.put("idCategory", word.idCategory)
        values.put("indexLearning", word.indexLearning)

        val db = this.writableDatabase
        db.insert("words", null, values)

        db.close()
    }

    // понизить индекс
    fun loverIndexLearning(wordId: Int): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("indexLearning", -1)
        }

        val rowsAffected = db.update(
            "words",
            values,
            "id = ?",
            arrayOf(wordId.toString())
        )

        db.close()
        return rowsAffected > 0
    }

    // повысить индекс
    fun upperIndexLearning(wordId: Int): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("indexLearning", +1)
        }

        val rowsAffected = db.update(
            "words",
            values,
            "id = ?",
            arrayOf(wordId.toString())
        )

        db.close()
        return rowsAffected > 0
    }

    // добавление категории
    fun addCategory(name: String) : Boolean {
        val values = ContentValues()
        values.put("name", name)

        val db = this.writableDatabase
        db.insert("category", null, values)

        db.close()
        return true
    }

    fun getSelectedCategory(): List<WordCategory> {
        val selectedCategory = mutableListOf<WordCategory>()

        val db = readableDatabase

        val cursor = db.rawQuery(
            """SELECT * FROM category 
           WHERE selected = ?""",
            arrayOf("1")
        )

        cursor.use {
            while (it.moveToNext()) {
                val id = it.getInt(it.getColumnIndexOrThrow("id"))
                val name = it.getString(it.getColumnIndexOrThrow("name"))
                val selected = it.getInt(it.getColumnIndexOrThrow("selected"))
                selectedCategory.add(WordCategory(id, name, selected))
            }
        }

        return selectedCategory
    }


    /*// Метод для получения всех элементов
    fun getAllItems(): List<Item> {
        val items = mutableListOf<Item>()
        val db = readableDatabase
        val cursor = db.query(
            "plans",
            null,
            null,
            null,
            null,
            null,
            null
        )

        with(cursor) {
            while (moveToNext()) {
                val id = getInt(getColumnIndexOrThrow("id"))
                val title = getString(getColumnIndexOrThrow("title"))
                val date = getString(getColumnIndexOrThrow("date"))
                val publish = getString(getColumnIndexOrThrow("published"))
                items.add(Item(id, title, date, publish))
            }
            close()
        }
        db.close()
        return items
    }

    fun getTodayItems(): List<Item> {
        val today = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
        val todayItems = mutableListOf<Item>()

        val db = readableDatabase

        // Лучший вариант: если дата хранится в формате dd.MM.yyyy
        val cursor = db.rawQuery(
            """SELECT * FROM plans 
           WHERE date = ?""",
            arrayOf(today)
        )

        cursor.use {
            while (it.moveToNext()) {
                val id = it.getInt(it.getColumnIndexOrThrow("id"))
                val title = it.getString(it.getColumnIndexOrThrow("title"))
                val date = it.getString(it.getColumnIndexOrThrow("date"))
                val publish = it.getString(it.getColumnIndexOrThrow("published"))
                todayItems.add(Item(id, title, date, publish))
            }
        }

        return todayItems
    }

    fun updatePublisher(itemId: Int, newPublisher: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("publisher", newPublisher)
        }

        val rowsAffected = db.update(
            "your_table_name",
            values,
            "id = ?",
            arrayOf(itemId.toString())
        )

        db.close()
        return rowsAffected > 0
    }*/
}