package com.example.healthcareapp.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.R
import com.example.healthcareapp.data.DiaryItem

class DiaryAdapter(
    private val items: MutableList<DiaryItem>,
    private val onItemClick: (DiaryItem) -> Unit,
    private val onDotClick: (Int) -> Unit
) : RecyclerView.Adapter<DiaryAdapter.ViewHolder>() {

    // ⭐ 재훈님이 정하신 10가지 무지개 색상 배열 그대로 유지
    private val stripeColors = intArrayOf(
        Color.parseColor("#FF6969"), Color.parseColor("#FF9245"),
        Color.parseColor("#FFD153"), Color.parseColor("#94A769"),
        Color.parseColor("#5DCE46"), Color.parseColor("#83F2FA"),
        Color.parseColor("#53A1FF"), Color.parseColor("#8A38F5"),
        Color.parseColor("#FF5DEF"), Color.parseColor("#A39288")
    )

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMainText: TextView = view.findViewById(R.id.tv_date)
        val tvSubText: TextView = view.findViewById(R.id.tv_sub)
        val viewStripe: View = view.findViewById(R.id.view_stripe)
        val btnDot: ImageView = view.findViewById(R.id.exercise_dot)

        fun bind(item: DiaryItem, position: Int) {
            tvMainText.text = item.date
            tvSubText.text = item.title

            // ⭐ 에러 해결: IntArray에서 직접 색상 값을 꺼내와서 적용합니다.
            // position % stripeColors.size 로직으로 10개 색상이 순환됩니다.
            val targetColor = stripeColors[position % stripeColors.size]
            viewStripe.setBackgroundColor(targetColor)

            // 이미지 가이드에 맞춰 우측 아이콘 설정 (운동 유무 등을 고려)
            // 임시로 이모티콘 3번과 5번을 번갈아 보여주게 설정했습니다.
            if (position % 2 == 0) {
                btnDot.setImageResource(R.drawable.emoticon3)
            } else {
                btnDot.setImageResource(R.drawable.emoticon5)
            }

            itemView.setOnClickListener { onItemClick(item) }
            btnDot.setOnClickListener { onDotClick(adapterPosition) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.diary_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount() = items.size
}