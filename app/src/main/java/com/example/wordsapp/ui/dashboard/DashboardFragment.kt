package com.example.wordsapp.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.wordsapp.R
import com.example.wordsapp.databinding.FragmentDashboardBinding
import com.example.wordsapp.db.DbHelper
import com.google.android.material.slider.Slider

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private lateinit var dbHelper: DbHelper

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.startLearning.setOnClickListener {
            findNavController().navigate(R.id.action_third_to_fourth)
        }

        dbHelper = DbHelper(requireContext(), null)
        binding.allLearn.text = dbHelper.getCountOfWordsWithIndexLearning()
        binding.addLearn.text = dbHelper.getWordsCountFromSelectedCategories().toString()

//        binding.discreteSlider.setLabelFormatter { value ->
//            when (value) {
//                1f -> "День"
//                2f -> "Месяц"
//                3f -> "Год"
//                4f -> "Позиция 4"
//                else -> ""
//            }
//        }
//
//        binding.discreteSlider.addOnChangeListener { _, value, _ ->
//            when (value) {
//                1f -> updateSelection(1)
//                2f -> updateSelection(2)
//                3f -> updateSelection(3)
//                4f -> updateSelection(4)
//            }
//        }

    }

    fun updateSelection(position: Int) {
        Toast.makeText(requireContext(), "Выбрана позиция $position", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}