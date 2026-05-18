package com.example.healthcareapp.sheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.healthcareapp.R
import com.example.healthcareapp.data.PainRecord
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PainBottomSheetFragment : BottomSheetDialogFragment() {

    enum class Mode { CREATE, VIEW }

    private var bodyPartName: String = ""
    private var mode: Mode = Mode.CREATE
    private var onComplete: ((PainRecord) -> Unit)? = null

    companion object {
        private const val ARG_BODY_PART = "bodyPartName"
        private const val ARG_MODE = "mode"

        fun newInstance(
            bodyPartName: String,
            mode: Mode = Mode.CREATE,
            onComplete: ((PainRecord) -> Unit)? = null
        ): PainBottomSheetFragment {
            return PainBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_BODY_PART, bodyPartName)
                    putString(ARG_MODE, mode.name)
                }
                this.onComplete = onComplete
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bodyPartName = arguments?.getString(ARG_BODY_PART) ?: ""
        mode = Mode.valueOf(arguments?.getString(ARG_MODE) ?: Mode.CREATE.name)
    }

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
        val etPainMemo = view.findViewById<EditText>(R.id.et_pain_memo)
        val btnComplete = view.findViewById<Button>(R.id.btn_complete)
        val btnClose = view.findViewById<ImageView>(R.id.btn_close)

        val dotViews = listOf(
            view.findViewById<ImageView>(R.id.dot_level1),
            view.findViewById<ImageView>(R.id.dot_level2),
            view.findViewById<ImageView>(R.id.dot_level3),
            view.findViewById<ImageView>(R.id.dot_level4),
            view.findViewById<ImageView>(R.id.dot_level5)
        )
        val levelLayouts = listOf(
            view.findViewById<LinearLayout>(R.id.ll_level1),
            view.findViewById<LinearLayout>(R.id.ll_level2),
            view.findViewById<LinearLayout>(R.id.ll_level3),
            view.findViewById<LinearLayout>(R.id.ll_level4),
            view.findViewById<LinearLayout>(R.id.ll_level5)
        )

        tvTitle.text = bodyPartName
        btnLeft.isSelected = true
        var selectedLevel = -1

        fun selectDot(index: Int) {
            selectedLevel = index
            dotViews.forEachIndexed { i, dot ->
                dot.setImageResource(
                    if (i == index) R.drawable.dot_pain_selected else R.drawable.dot_pain_unselected
                )
            }
        }

        levelLayouts.forEachIndexed { index, layout ->
            layout.setOnClickListener { selectDot(index) }
        }

        btnLeft.setOnClickListener {
            btnLeft.isSelected = true
            btnRight.isSelected = false
        }

        btnRight.setOnClickListener {
            btnLeft.isSelected = false
            btnRight.isSelected = true
        }

        btnClose.setOnClickListener { dismiss() }

        if (mode == Mode.VIEW) {
            btnLeft.isEnabled = false
            btnRight.isEnabled = false
            levelLayouts.forEach { it.isEnabled = false }
            etPainMemo.isEnabled = false
            btnComplete.visibility = View.GONE
            return
        }

        btnComplete.setOnClickListener {
            val side = if (btnLeft.isSelected) "좌" else "우"
            val painLevel = if (selectedLevel >= 0) selectedLevel + 1 else 1
            val reason = etPainMemo.text?.toString()?.trim()?.ifEmpty { null }
            onComplete?.invoke(PainRecord(bodyPartName, side, painLevel, reason))
            dismiss()
        }
    }

    override fun getTheme(): Int = R.style.CustomBottomSheetDialogTheme
}
