package com.example.wordsapp.ui.learning

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wordsapp.R
import com.example.wordsapp.databinding.FragmentLearningBinding
import androidx.recyclerview.widget.PagerSnapHelper
import com.example.wordsapp.db.DbHelper

class LearningFragment : Fragment() {

    private var _binding: FragmentLearningBinding? = null
    private lateinit var adapter: CardAdapter
    private lateinit var viewModel: LearningViewModel

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentLearningBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Создаем тестовые данные
        val cardItems = List(20) { index ->
            CardItem("Карточка ${index + 1}")
        }

        // Инициализируем адаптер
        adapter = CardAdapter(
            items = cardItems,
            onYesClick = { position ->
                val db = DbHelper(requireContext(), null)
                db.upperIndexLearning(id)
            },
            onNoClick = { position ->
                Toast.makeText(
                    requireContext(),
                    "Нет нажато на карточке ${position + 1}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        // Правильная настройка RecyclerView
        binding.recyclerView.apply {
            // 1. Настройка LayoutManager
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL, // Горизонтальная прокрутка
                false
            )

            // 2. Установка адаптера
            adapter = this@LearningFragment.adapter

            // 3. Настройка SnapHelper (выберите один вариант)

            // Вариант A: PagerSnapHelper (как ViewPager)
            PagerSnapHelper().attachToRecyclerView(this)

            // ИЛИ Вариант B: LinearSnapHelper (плавное притягивание)
            // LinearSnapHelper().attachToRecyclerView(this)

            // 4. Отключение эффекта overscroll
            overScrollMode = RecyclerView.OVER_SCROLL_NEVER

            // 5. Добавление разделителей (опционально)
            addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    LinearLayoutManager.HORIZONTAL
                ).apply {
                    setDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.divider
                        )!!
                    )
                }
            )

            // 6. Оптимизация для постраничного скролла
            setHasFixedSize(true)
            clipToPadding = false
            clipChildren = false

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class CardAdapter(
        private val items: List<CardItem>,
        private val onYesClick: (Int) -> Unit,
        private val onNoClick: (Int) -> Unit
    ) : RecyclerView.Adapter<CardAdapter.CardViewHolder>() {

        inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvText: TextView = itemView.findViewById(R.id.tv_card_text)
            private val btnYes: Button = itemView.findViewById(R.id.btn_yes)
            private val btnNo: Button = itemView.findViewById(R.id.btn_no)

            fun bind(item: CardItem, position: Int) {
                tvText.text = item.text

                btnYes.setOnClickListener { onYesClick(position) }
                btnNo.setOnClickListener { onNoClick(position) }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_learn_word, parent, false)
            return CardViewHolder(view)
        }

        override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
            holder.bind(items[position], position)
        }

        override fun getItemCount() = items.size
    }

    data class CardItem(val text: String)
}