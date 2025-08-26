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
    fun deleteWordsCategory(categoryId: Int): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        return try {
            val rowsAffected = db.delete(
                "words",
                "idCategory = ?",
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

    fun setIsCheckedCategory(category: WordCategory): Boolean {
        val db = writableDatabase
        return try {
            val values = ContentValues().apply {
                put("selected", if (category.selected == 1) 0 else 1)
            }

            val rowsAffected = db.update(
                "category",
                values,
                "id = ?",
                arrayOf(category.id.toString())
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

        readableDatabase.use { db ->
            db.query(
                "category",
                null,
                "selected = ?",
                arrayOf("1"),
                null,
                null,
                null
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                    val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                    val selected = cursor.getInt(cursor.getColumnIndexOrThrow("selected"))
                    selectedCategory.add(WordCategory(id, name, selected))
                }
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

    fun getWordsFromSelectedCategories(): List<Word> {
        val words = mutableListOf<Word>()

        readableDatabase.use { db ->
            val table = "words JOIN category ON words.idCategory = category.id"
            val columns = arrayOf("words.*", "category.name as category_name")
            val selection = "category.selected = ?"
            val selectionArgs = arrayOf("1")

            db.query(
                table,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                    val original = cursor.getString(cursor.getColumnIndexOrThrow("original"))
                    val translate = cursor.getString(cursor.getColumnIndexOrThrow("translate"))
                    val idCategory = cursor.getInt(cursor.getColumnIndexOrThrow("idCategory"))
                    val indexLearning = cursor.getInt(cursor.getColumnIndexOrThrow("indexLearning"))
                    words.add(Word(id, original, translate, idCategory, indexLearning))
                }
            }
        }

        return words
    }

    fun getWordsCountFromSelectedCategories(): Int {
        var count = 0

        readableDatabase.use { db ->
            val table = "words JOIN category ON words.idCategory = category.id"
            val columns = arrayOf("COUNT(*) as count")
            val selection = "category.selected = ?"
            val selectionArgs = arrayOf("1")

            db.query(
                table,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
            ).use { cursor ->
                if (cursor.moveToFirst()) {
                    count = cursor.getInt(cursor.getColumnIndexOrThrow("count"))
                }
            }
        }

        return count
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

        addCategory("Числа 1-20")
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
        addWord("Eleven ", "одиннадцать", 2)
        addWord("Twelve ", "двенадцать", 2)
        addWord("Thirteen ", "тринадцать", 2)
        addWord("Fourteen", "четырнадцать", 2)
        addWord("Fifteen", "пятнадцать", 2)
        addWord("Sixteen", "шестнадцать", 2)
        addWord("Seventeen", "семнадцать", 2)
        addWord("Eighteen", "восемнадцать", 2)
        addWord("Nineteen", "девятнадцать", 2)
        addWord("Twenty", "двадцать", 2)

        addCategory("Глаголы A1")
        addWord("Do ", "делать", 3)
        addWord("Feel ", "чувствовать", 3)
        addWord("See ", "видеть", 3)
        addWord("Hear", "слышать", 3)
        addWord("Run", "бежать", 3)
        addWord("Get", "получать", 3)
        addWord("Make", "делать", 3)
        addWord("Cook", "готовить", 3)
        addWord("Sing", "петь", 3)
        addWord("Speak", "разговаривать", 3)
        addWord("Say ", "говорить", 3)
        addWord("Tell ", "рассказывать", 3)
        addWord("Take", "брать", 3)
        addWord("Sit", "садиться", 3)
        addWord("Stand", "стоять", 3)
        addWord("Laugh", "смеяться", 3)
        addWord("Smile", "улыбаться", 3)
        addWord("Open", "открывать", 3)
        addWord("Close", "закрывать", 3)
        addWord("Love ", "любить", 3)
        addWord("Like ", "нравиться", 3)
        addWord("Give ", "давать", 3)
        addWord("Bring", "приносить", 3)
        addWord("Breath", "дышать", 3)
        addWord("Buy", "покупать", 3)
        addWord("Sell", "продавать", 3)
        addWord("Forget", "забывать", 3)
        addWord("Believe", "верить", 3)
        addWord("Have", "иметь", 3)
        addWord("Go", "идти", 3)
        addWord("Know", "знать", 3)
        addWord("Think", "думать", 3)
        addWord("Come", "приходить", 3)
        addWord("Want", "хотеть", 3)
        addWord("Use", "использовать", 3)
        addWord("Find", "находить", 3)
        addWord("Work ", "работать", 3)
        addWord("Eat ", "есть", 3)
        addWord("Drink ", "пить", 3)
        addWord("Write", "писать", 3)
        addWord("Read", "читать", 3)
        addWord("Call", "звонить", 3)
        addWord("Try", "пытаться", 3)
        addWord("Need", "нуждаться", 3)
        addWord("Become", "становиться", 3)
        addWord("Put", "класть", 3)
        addWord("Pay", "платить", 3)
        addWord("Play", "играть", 3)

        addCategory("Прилагательные A1")
        addCategory("Наречия A1")
        addCategory("Семья")
        addCategory("Окружающий мир")
        addCategory("Для туриста")



    }
}