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
    fun addWord(original: String, translate: String, idCategory: Int): Boolean {
        val db = this.writableDatabase
        var newId = 1L
        val cursor = db.rawQuery("SELECT MAX(id) FROM words", null)
        cursor.use {
            if (it.moveToFirst() && !it.isNull(0)) {
                newId = it.getLong(0) + 1
            }
        }

        val values = ContentValues().apply {
            put("id", newId)
            put("original", original)
            put("translate", translate)
            put("idCategory", idCategory)
            put("indexLearning", 5) // Начальный индекс обучения
        }

        return try {
            val result = db.insert("words", null, values) != -1L
            result
        } catch (e: Exception) {
            false
        } finally {
            db.close()
        }
    }

    fun getWordsInCategory(categoryId: Int): List<Word> {
        val db = readableDatabase
        val wordsList = mutableListOf<Word>()

        val query = """
        SELECT * FROM words
        WHERE idCategory = ?
    """.trimIndent()

        db.rawQuery(query, arrayOf(categoryId.toString())).use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow("id")
            val originalIndex = cursor.getColumnIndexOrThrow("original")
            val translateIndex = cursor.getColumnIndexOrThrow("translate")
            val idCategoryIndex = cursor.getColumnIndexOrThrow("idCategory")
            val indexLearningIndex = cursor.getColumnIndexOrThrow("indexLearning")

            while (cursor.moveToNext()) {
                val id = cursor.getInt(idIndex)
                val original = cursor.getString(originalIndex)
                val translate = cursor.getString(translateIndex)
                val idCategory = cursor.getInt(idCategoryIndex)
                val indexLearning = cursor.getInt(indexLearningIndex)

                wordsList.add(Word(id, original, translate, idCategory, indexLearning))
            }
        }

        return wordsList
    }

    fun updateWordIndex(wordId: Int, newIndex: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("indexLearning", newIndex)
        }
        db.update(
            "words",
            values,
            "id = ?",
            arrayOf(wordId.toString())
        )
    }

    // добавление категории
    fun addCategory(name: String): Boolean {
        val db = this.writableDatabase
        var newId = 1L // Значение по умолчанию, если таблица пуста

        db.beginTransaction()
        try {
            // 1. Получаем текущий максимальный ID
            val cursor = db.rawQuery("SELECT MAX(id) FROM category", null)
            cursor.use {
                if (it.moveToFirst() && !it.isNull(0)) {
                    newId = it.getLong(0) + 1
                }
            }

            // 2. Вставляем новую запись
            val values = ContentValues().apply {
                put("id", newId)
                put("name", name)
                put("selected", 0)
            }

            val result = db.insert("category", null, values)
            db.setTransactionSuccessful()
            return true
        } catch (e: Exception) {
            Log.e("DB", "Error adding category", e)
            return false
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    fun deleteCategory(categoryId: Int): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        return try {
            val rowsAffected = db.delete(
                "category",
                "id = ?",
                arrayOf(categoryId.toString())
            )

            db.setTransactionSuccessful()
            rowsAffected > 0
        } catch (e: Exception) {
            false
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    fun renameCategory(id: Int, name: String): Boolean {
        val db = writableDatabase
        return try {
            val values = ContentValues().apply {
                put("name", name)
            }

            val rowsAffected = db.update(
                "category",
                values,
                "id = ?",
                arrayOf(id.toString())
            )

            rowsAffected > 0
        } catch (e: Exception) {
            false
        } finally {
            db.close()
        }
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

    // Метод для получения всех категорий
    fun getAllCategory(): List<WordCategory> {
        val items = mutableListOf<WordCategory>()
        val db = readableDatabase
        val cursor = db.query(
            "category",
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
                val name = getString(getColumnIndexOrThrow("name"))
                val selected = getInt(getColumnIndexOrThrow("selected"))
                items.add(WordCategory(id, name, selected))
            }
            close()
        }
        db.close()
        return items
    }

    fun getAllWords(): List<Word> {
        val items = mutableListOf<Word>()
        val db = readableDatabase
        val cursor = db.query(
            "words",
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
                val original = getString(getColumnIndexOrThrow("original"))
                val translate = getString(getColumnIndexOrThrow("translate"))
                val idCategory = getInt(getColumnIndexOrThrow("idCategory"))
                val indexLearning = getInt(getColumnIndexOrThrow("indexLearning"))
                items.add(Word(id, original, translate, idCategory, indexLearning))
            }
            close()
        }
        db.close()
        return items
    }

    fun getCountOfWordsWithIndexLearning(): String {
        val db = readableDatabase
        var count = 0

        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM words WHERE indexLearning = 1 OR indexLearning = 2",
            null
        )

        cursor.use { // Автоматически закрывает курсор после использования
            if (it.moveToFirst()) {
                count = it.getInt(0) // Получаем результат COUNT
            }
        }

        db.close()
        return count.toString()
    }


    fun firstStart() {
        addCategory("Животные")
        addWord("Lion ", "лев", 1)
        addWord("Elephant ", "слон", 1)
        addWord("Panda ", "панда", 1)
        addWord("Bird", "птица", 1)
        addWord("Camel", "верблюд", 1)
        addWord("Tiger", "тигр", 1)
        addWord("Monkey", "обезьяна", 1)
        addWord("Fox", "лиса", 1)
        addWord("Cat", "кот", 1)
        addWord("Dog", "собака", 1)
        addWord("Bear", "медведь", 1)
        addWord("Horse", "лошадь", 1)
        addWord("Giraffe", "жираф", 1)
        addWord("Zebra", "зебра", 1)
        addWord("Squirrel", "белка", 1)
        addWord("Tortoise", "черепаха", 1)
        addWord("Snake", "змея", 1)
        addWord("Lizard", "ящерица", 1)
        addWord("Alligator", "крокодил", 1)
        addWord("Elk", "олень", 1)

        addCategory("Числа 1-10")
        addWord("One ", "один", 2)
        addWord("Two ", "два", 2)
        addWord("Three ", "три", 2)
        addWord("Four", "четыре", 2)
        addWord("Five", "пять", 2)
        addWord("Six", "шесть", 2)
        addWord("Seven", "семь", 2)
        addWord("Eight", "восемь", 2)
        addWord("Nine", "девять", 2)
        addWord("Ten", "десять", 2)

        addCategory("Числа 11-20")
        addWord("Eleven ", "одиннадцать", 3)
        addWord("Twelve ", "двенадцать", 3)
        addWord("Thirteen ", "тринадцать", 3)
        addWord("Fourteen", "четырнадцать", 3)
        addWord("Fifteen", "пятнадцать", 3)
        addWord("Sixteen", "шестнадцать", 3)
        addWord("Seventeen", "семнадцать", 3)
        addWord("Eighteen", "восемнадцать", 3)
        addWord("Nineteen", "девятнадцать", 3)
        addWord("Twenty", "двадцать", 3)
    }

    /*fun getTodayItems(): List<Item> {
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