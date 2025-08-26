package com.example.wordsapp.ui.home

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wordsapp.R
import com.example.wordsapp.data.WordCategory
import com.example.wordsapp.databinding.FragmentHomeBinding
import com.example.wordsapp.db.DbHelper
import com.example.wordsapp.ui.dashboard.DashboardFragment

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
                    if (categoryName.length < 15) {
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
                        Toast.makeText(
                            requireContext(),
                            "Ошибка: Максимальная длинна строки: 15 символов",
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
            private val checkBox: CheckBox = itemView.findViewById(R.id.checkbox2)
            private val expandedButtons: LinearLayout = itemView.findViewById(R.id.expandedButtons)
            private val btnEdit: Button = itemView.findViewById(R.id.btnEdit)
            private val btnDelete: Button = itemView.findViewById(R.id.btnDelete)

            fun bind(category: WordCategory) {
                itemView.findViewById<android.widget.TextView>(R.id.category_name).text = category.name
                // Здесь можно установить изображение, если оно есть
                val bundle = Bundle().apply {
                    putString("categoryName", category.name)
                    putInt("categoryId", category.id) // предполагается, что у WordCategory есть поле id
                }

                expandedButtons.requestLayout() // Принудительное обновление
                itemView.requestLayout()

                checkBox.isChecked = category.selected == 1

                checkBox.setOnCheckedChangeListener { _, _ ->
                    dbHelper.setIsCheckedCategory(category)
                }

                itemView.setOnClickListener {
                    onItemClick(category)
                    findNavController().navigate(R.id.action_first_to_second, bundle)
                }

                itemView.setOnLongClickListener {
                    toggleExpansion(category)
                    true
                }

                btnEdit.setOnClickListener {
                    onRename(category)
                    loadCategories()
                }

                btnDelete.setOnClickListener {
                    onDelete(category)
                    loadCategories()
                }
            }

            private fun toggleExpansion(category: WordCategory) {
                category.isExpanded = !category.isExpanded

                if (category.isExpanded) {
                    expandItem()
                } else {
                    collapseItem()
                }

                // Анимируем изменение
                TransitionManager.beginDelayedTransition(itemView as ViewGroup)
            }

            private fun expandItem() {
                expandedButtons.visibility = View.VISIBLE
                // фон itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.purple_222))
                val params = itemView.layoutParams as ViewGroup.MarginLayoutParams
                params.width = ViewGroup.LayoutParams.MATCH_PARENT
                params.height = 260
                itemView.layoutParams = params
            }

            private fun collapseItem() {
                expandedButtons.visibility = View.GONE
                // Возвращаем обычную ширину
                val params = itemView.layoutParams as ViewGroup.MarginLayoutParams
                params.width = ViewGroup.LayoutParams.MATCH_PARENT
                params.height = -260
                itemView.layoutParams = params
            }

            private fun onDelete(category: WordCategory) {
                dbHelper = DbHelper(requireContext(), null)
                AlertDialog.Builder(requireContext())
                    .setTitle("Подтверждение")
                    .setMessage("Вы точно хотите удалить категорию ${category.name}?")
                    .setPositiveButton("Ок") { _, _ ->
                        dbHelper.deleteWordsCategory(category.id)
                        dbHelper.deleteCategory(category.id)
                        loadCategories()
                        Toast.makeText(
                            requireContext(),
                            "Категория ${category.name} успешно удалена",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .setNegativeButton("Отмена", null)
                    .create().show()
            }

            private fun onRename(category: WordCategory) {
                val inputEditText = EditText(requireContext()).apply {
                    hint = "Название категории"
                    setText(category.name) // Tекущее название
                    setSelection(category.name.length) // Курсор в конец текста
                }
                dbHelper = DbHelper(requireContext(), null)
                AlertDialog.Builder(requireContext())
                    .setTitle("Изменить название")
                    .setView(inputEditText)
                    .setPositiveButton("Ок") { _, _ ->
                        dbHelper.renameCategory(category.id, inputEditText.text.toString())
                        loadCategories()
                        Toast.makeText(
                            requireContext(),
                            "Категория ${category.name} успешно переименована",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .setNegativeButton("Отмена", null)
                    .create().show()
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