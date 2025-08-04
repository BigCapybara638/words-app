package com.example.wordsapp.ui.home

import android.app.AlertDialog
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.wordsapp.R
import com.example.wordsapp.databinding.FragmentHomeBinding
import com.example.wordsapp.db.DbHelper
import com.google.android.material.internal.ViewUtils.hideKeyboard
import com.google.android.material.internal.ViewUtils.showKeyboard

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.text
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        fun showTextInputDialog() {
            val inputEditText = EditText(requireContext()).apply {
                hint = "Введите текст"
            }

            val dialog = AlertDialog.Builder(requireContext())
                .setTitle("Добавить коллекцию")
                .setView(inputEditText)
                .setPositiveButton("OK") { _, _ ->
                    val enteredText = inputEditText.text.toString()

                    // Проверка на пустую строку
                    if (enteredText.isNotBlank()) {
                        // Вызов метода из DbHelper
                        val dbHelper = DbHelper(requireContext(), null)
                        val result = dbHelper.addCategory(enteredText) // Предполагаемый метод

                        if (result) {
                            Toast.makeText(
                                requireContext(),
                                "Коллекция '$enteredText' добавлена",
                                Toast.LENGTH_SHORT
                            ).show()

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

            // Применяем анимацию к диалогу
            dialog.window?.apply {
                setWindowAnimations(R.style.DialogAnimation)
            }

            dialog.show()
        }


        binding.startLearn.setOnClickListener {
            showTextInputDialog()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}