package com.example.healthcareapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.example.healthcareapp.R
import com.example.healthcareapp.data.ConditionRecord
import com.google.android.material.chip.ChipGroup
import com.google.android.material.slider.Slider

class ConditionAdapter(private val items: MutableList<ConditionRecord>) :
    RecyclerView.Adapter<ConditionAdapter.ViewHolder>() {

    // 단일 뷰홀더 구조로 통합
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tv_condition_title)
        val tvTime: TextView = view.findViewById(R.id.tv_complete_time)
        val ivArrow: ImageView = view.findViewById(R.id.iv_arrow)
        val layoutDetail: View = view.findViewById(R.id.layout_detail)


        val sliderPain: Slider = view.findViewById(R.id.slider_pain)
        val chipGroupBody: ChipGroup = view.findViewById(R.id.chip_group_body)
        val etMemo: EditText = view.findViewById(R.id.et_condition_memo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_condition, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.tvTitle.text = item.title
        holder.tvTime.text = "${item.time} 작성 완료"

        // 1. 펼침 상태에 따른 UI 처리
        if (item.isExpanded) {
            holder.layoutDetail.visibility = View.VISIBLE
            holder.ivArrow.rotation = 180f
        } else {
            holder.layoutDetail.visibility = View.GONE
            holder.ivArrow.rotation = 0f
        }

        // 2. 항목 클릭 시 드롭다운 토글
        holder.itemView.setOnClickListener {
            val currentPos = holder.adapterPosition
            if (currentPos != RecyclerView.NO_POSITION) {
                // 상태 반전
                item.isExpanded = !item.isExpanded

                // 부드러운 펼치기 애니메이션 적용
                TransitionManager.beginDelayedTransition(holder.itemView.parent as ViewGroup)

                // 해당 아이템만 다시 그리기
                notifyItemChanged(currentPos)
            }
        }


        // 슬라이더 값이 변경되면 데이터 객체에 저장
        holder.sliderPain.value = item.painScore // 기존 저장된 값 세팅
        holder.sliderPain.addOnChangeListener { _, value, _ ->
            item.painScore = value
        }


    }

    override fun getItemCount() = items.size

    // 새 항목 추가 함수 (맨 위에 추가)
    fun addItem(item: ConditionRecord) {
        items.add(0, item)
        notifyItemInserted(0)
    }
}