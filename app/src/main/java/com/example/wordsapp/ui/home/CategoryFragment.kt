package com.example.wordsapp.ui.home

import android.app.AlertDialog
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.wordsapp.R
import com.example.wordsapp.data.Word
import com.example.wordsapp.data.WordCategory
import com.example.wordsapp.databinding.FragmentCategoryBinding
import com.example.wordsapp.databinding.FragmentNotificationsBinding
import com.example.wordsapp.db.DbHelper
import com.example.wordsapp.ui.home.HomeFragment.CategoryAdapter

class CategoryFragment : Fragment() {

    private var _binding: FragmentCategoryBinding? = null
    private lateinit var dbHelper: DbHelper
    private lateinit var adapter: WordsAdapter

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)
        dbHelper = DbHelper(requireContext(), null)
        val root: View = binding.root

        binding.addWord.setOnClickListener {
            showTwoFieldDialog()
        }

        val categoryName = arguments?.getString("categoryName") ?: "Коллекция"

        // Устанавливаем заголовок в AppBar
        (activity as AppCompatActivity).supportActionBar?.title = categoryName

        return root
    }

    // Kotlin версия
    fun showTwoFieldDialog() {
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_two_fields, null)
        val editText1 = dialogView.findViewById<EditText>(R.id.editText1)
        val editText2 = dialogView.findViewById<EditText>(R.id.editText2)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Введите данные")
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                val text1 = editText1.text.toString()
                val text2 = editText2.text.toString()
                // Действия при нажатии OK

                if (text1.isNotEmpty() && text2.isNotEmpty()) {
                    val result = dbHelper.addWord(text1, text2, 3)
                    Toast.makeText(requireContext(), "Слово добавлено!", Toast.LENGTH_SHORT).show()
                    // Обновляем список слов, если нужно
                    // (например, через интерфейс обратного вызова)

                } else {
                    Toast.makeText(requireContext(), "Заполните оба поля", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            .setNegativeButton("Отмена", null)
            .create()

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadCategories() {
        val words = dbHelper.getWordsInCategory(3) // Предполагается, что такой метод есть в DbHelper
        adapter.updateWords(words)
    }

    inner class WordsAdapter(
        private var words: List<Word>,
        private val onItemClick: (Word) -> Unit
    ) : androidx.recyclerview.widget.RecyclerView.Adapter<com.example.wordsapp.ui.home.CategoryFragment.WordsAdapter.WordsViewHolder>() {

        inner class WordsViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
            fun bind(category: Word) {
                itemView.findViewById<android.widget.TextView>(R.id.category_name).text = category.translate
                // Здесь можно установить изображение, если оно есть
                itemView.setOnClickListener {
                   // onItemClick(words)
                }
            }

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordsViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_category_card, parent, false)
            return WordsViewHolder(view)
        }

        override fun onBindViewHolder(holder: WordsViewHolder, position: Int) {
            holder.bind(words[position])

        }


        override fun getItemCount() = words.size

        fun updateWords(newWords: List<Word>) {
            words = newWords
        }
    }

}