package com.example.wordsapp.ui.learning

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wordsapp.R
import com.example.wordsapp.databinding.FragmentLearningBinding
import androidx.recyclerview.widget.PagerSnapHelper
import com.example.wordsapp.StreakManager
import com.example.wordsapp.data.Word
import com.example.wordsapp.db.DbHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LearningFragment : Fragment() {
    private var _binding: FragmentLearningBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper: DbHelper
    private lateinit var adapter: LearnWordsAdapter
    private var currentWords = emptyList<Word>()
    private var currentPosition = 0
    private lateinit var streakManager: StreakManager

    // Слушатель прокрутки RecyclerView
    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                updateCurrentPosition()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLearningBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = DbHelper(requireContext(), null)
        setupRecyclerView()
        loadWords()

    }

    private fun setupRecyclerView() {
        adapter = LearnWordsAdapter(
            onYesClick = { updateWordAndShowNext(isKnown = true) },
            onNoClick = { updateWordAndShowNext(isKnown = false) }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = this@LearningFragment.adapter
            addOnScrollListener(scrollListener)
            PagerSnapHelper().attachToRecyclerView(this)
        }
    }

    private fun loadWords() {
        lifecycleScope.launch(Dispatchers.IO) {
            currentWords = dbHelper.getWordsFromSelectedCategories()
                .flatMap { word -> List(11 - word.indexLearning) { word } }
                .shuffled()
                .distinctBy { it.id }
                .take(20)

            withContext(Dispatchers.Main) {
                adapter.submitList(currentWords)
                currentPosition = 0
            }
        }
    }

    private fun updateCurrentPosition() {
        val layoutManager = binding.recyclerView.layoutManager as LinearLayoutManager
        val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
        if (firstVisiblePosition != RecyclerView.NO_POSITION && firstVisiblePosition != currentPosition) {
            currentPosition = firstVisiblePosition
        }
    }

    private fun updateWordAndShowNext(isKnown: Boolean) {
        if (currentPosition !in currentWords.indices) return

        val word = currentWords[currentPosition]
        val newIndex = if (isKnown) maxOf(1, word.indexLearning - 1) else minOf(10, word.indexLearning + 1)

        lifecycleScope.launch(Dispatchers.IO) {
            dbHelper.updateWordIndex(word.id, newIndex)

            withContext(Dispatchers.Main) {
                 // Плавный переход к следующему слову
                if (currentPosition + 1 < currentWords.size) {
                    currentPosition++
                    binding.recyclerView.smoothScrollToPosition(currentPosition)
                } else {
                    // Все слова пройдены - загружаем новые
                    loadWords()

                }
            }
        }
    }

    override fun onDestroyView() {
        binding.recyclerView.removeOnScrollListener(scrollListener)
        super.onDestroyView()
        _binding = null
    }
}

class LearnWordsAdapter(
    private val onYesClick: (Int) -> Unit,
    private val onNoClick: (Int) -> Unit
) : RecyclerView.Adapter<LearnWordsAdapter.LearnWordViewHolder>() {

    private var words = listOf<Word>()

    inner class LearnWordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCardText: TextView = itemView.findViewById(R.id.tv_card_text)
        private val btnYes: Button = itemView.findViewById(R.id.btn_yes)
        private val btnNo: Button = itemView.findViewById(R.id.btn_no)
        private val btnTranslate: TextView = itemView.findViewById(R.id.tv_card_text_translate)


        fun bind(word: Word) {
            tvCardText.text = word.original
            btnYes.setOnClickListener {
                onYesClick(adapterPosition)
                btnTranslate.text = "Показать перевод"
            }
            btnNo.setOnClickListener {
                onNoClick(adapterPosition)
                btnTranslate.text = "Показать перевод"
            }
            btnTranslate.setOnClickListener {
                btnTranslate.text = word.translate
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LearnWordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_learn_word, parent, false)
        return LearnWordViewHolder(view)
    }

    override fun onBindViewHolder(holder: LearnWordViewHolder, position: Int) {
        holder.bind(words[position])
    }

    override fun getItemCount() = words.size

    fun submitList(newWords: List<Word>) {
        words = newWords
        notifyDataSetChanged()
    }
}