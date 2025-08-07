package com.example.wordsapp.ui.home

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wordsapp.R
import com.example.wordsapp.data.WordCategory
import com.example.wordsapp.databinding.FragmentHomeBinding
import com.example.wordsapp.db.DbHelper

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper: DbHelper
    private lateinit var adapter: CategoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        dbHelper = DbHelper(requireContext(), null)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadCategories()

        binding.startLearn.setOnClickListener {
            showAddCategoryDialog()
        }
    }

    private fun setupRecyclerView() {
        adapter = CategoryAdapter(emptyList()) { category ->
            // Обработка нажатия
        }

        binding.categoriesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext()) // Линейный макет (одна колонка)
            adapter = this@HomeFragment.adapter
        }
    }

    private fun loadCategories() {
        val categories = dbHelper.getAllCategory() // Предполагается, что такой метод есть в DbHelper
        adapter.updateCategories(categories)
    }

    private fun showAddCategoryDialog() {
        val inputEditText = EditText(requireContext()).apply {
            hint = "Введите название коллекции"
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Добавить коллекцию")
            .setView(inputEditText)
            .setPositiveButton("Добавить") { _, _ ->
                val categoryName = inputEditText.text.toString().trim()
                if (categoryName.isNotBlank()) {
                    if (dbHelper.addCategory(categoryName)) {
                        Toast.makeText(
                            requireContext(),
                            "Коллекция '$categoryName' добавлена",
                            Toast.LENGTH_SHORT
                        ).show()
                        loadCategories() // Обновляем список после добавления
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Ошибка при добавлении коллекции",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    inputEditText.error = "Введите название коллекции"
                }
            }
            .setNegativeButton("Отмена", null)
            .create()
            .apply {
                window?.setWindowAnimations(R.style.DialogAnimation)
                show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Внутренний класс Adapter
    inner class CategoryAdapter(
        private var categories: List<WordCategory>,
        private val onItemClick: (WordCategory) -> Unit
    ) : androidx.recyclerview.widget.RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

        inner class CategoryViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
            fun bind(category: WordCategory) {
                itemView.findViewById<android.widget.TextView>(R.id.category_name).text = category.name
                // Здесь можно установить изображение, если оно есть
                itemView.setOnClickListener { onItemClick(category) }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_category_card, parent, false)
            return CategoryViewHolder(view)
        }

        override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
            holder.bind(categories[position])
        }

        override fun getItemCount() = categories.size

        fun updateCategories(newCategories: List<WordCategory>) {
            categories = newCategories
            notifyDataSetChanged()
        }
    }
}