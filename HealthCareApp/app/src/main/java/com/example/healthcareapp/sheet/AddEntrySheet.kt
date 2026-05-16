package com.example.healthcareapp.sheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.healthcareapp.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddEntrySheet(
    private val onConditionCheckClick: () -> Unit,
    private val onExerciseStartClick: () -> Unit
) : BottomSheetDialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.TransparentBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_add_entry_sheet, container, false)

        view.findViewById<View>(R.id.btn_close_add_entry).setOnClickListener { dismiss() }

        view.findViewById<View>(R.id.btn_sheet_condition_check).setOnClickListener {
            onConditionCheckClick()
            dismiss()
        }

        view.findViewById<View>(R.id.btn_sheet_exercise_start).setOnClickListener {
            onExerciseStartClick()
            dismiss()
        }

        return view
    }
}
