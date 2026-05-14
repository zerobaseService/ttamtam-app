package com.example.healthcareapp.sheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.TextView
import com.example.healthcareapp.R
import com.example.healthcareapp.data.PainRecord
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PainBottomSheetFragment(
    private val bodyPartName: String,
    private val onComplete: (PainRecord) -> Unit
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.pain_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvTitle = view.findViewById<TextView>(R.id.tv_title)
        val btnLeft = view.findViewById<TextView>(R.id.btn_left)
        val btnRight = view.findViewById<TextView>(R.id.btn_right)
        val rgPainLevel = view.findViewById<RadioGroup>(R.id.rg_pain_level)
        val btnComplete = view.findViewById<Button>(R.id.btn_complete)
        val btnClose = view.findViewById<ImageView>(R.id.btn_close)

        tvTitle.text = bodyPartName

        btnLeft.isSelected = true

        btnLeft.setOnClickListener {
            btnLeft.isSelected = true
            btnRight.isSelected = false
        }

        btnRight.setOnClickListener {
            btnLeft.isSelected = false
            btnRight.isSelected = true
        }

        btnClose.setOnClickListener { dismiss() }

        btnComplete.setOnClickListener {
            val side = if (btnLeft.isSelected) "좌" else "우"
            val painLevel = when (rgPainLevel.checkedRadioButtonId) {
                R.id.rb_level1 -> 1
                R.id.rb_level2 -> 2
                R.id.rb_level3 -> 3
                R.id.rb_level4 -> 4
                R.id.rb_level5 -> 5
                else -> 1
            }
            onComplete(PainRecord(bodyPartName, side, painLevel))
            dismiss()
        }
    }

    override fun getTheme(): Int = R.style.CustomBottomSheetDialogTheme
}
