package com.example.healthcareapp.sheet

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import com.example.healthcareapp.R

class AddEntrySheet(
    private val onConditionCheckClick: () -> Unit,
    private val onExerciseStartClick: () -> Unit
) {

    fun show(context: Context, anchor: View) {
        val density = context.resources.displayMetrics.density
        val marginPx = (16 * density).toInt()
        val screenWidth = context.resources.displayMetrics.widthPixels
        val popupWidth = screenWidth - 2 * marginPx

        val contentView = LayoutInflater.from(context).inflate(R.layout.popup_add_entry, null)

        val popup = PopupWindow(
            contentView,
            popupWidth,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            isOutsideTouchable = true
            elevation = density * 8f
        }

        anchor.visibility = View.INVISIBLE
        popup.setOnDismissListener { anchor.visibility = View.VISIBLE }

        contentView.findViewById<View>(R.id.btn_popup_condition_check).setOnClickListener {
            onConditionCheckClick()
            popup.dismiss()
        }
        contentView.findViewById<View>(R.id.btn_popup_exercise_start).setOnClickListener {
            onExerciseStartClick()
            popup.dismiss()
        }

        contentView.measure(
            View.MeasureSpec.makeMeasureSpec(popupWidth, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )

        val anchorLocation = IntArray(2)
        anchor.getLocationInWindow(anchorLocation)

        // 바텀 탭(약 110dp) 위에 여유를 두고, 팝업 하단을 FAB 하단보다 24dp 위에 정렬
        val bottomClearancePx = (24 * density).toInt()
        val popupX = marginPx
        val popupY = anchorLocation[1] + anchor.height - contentView.measuredHeight - bottomClearancePx

        popup.showAtLocation(anchor, Gravity.NO_GRAVITY, popupX, popupY)
    }
}
