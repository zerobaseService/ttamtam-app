package com.example.healthcareapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.data.StatusQuestion
import com.example.healthcareapp.databinding.ItemConditionQuestionBinding

/**
 * 운동 종료 후 상태 설문(통증, 강도 등) 리스트를 관리하는 어댑터
 * 사용자가 슬라이더를 조절하면 점수와 그에 따른 가이드 문구를 실시간으로 업데이트함
 */
class StatusQuestionAdapter(private val questions: List<StatusQuestion>) :
    RecyclerView.Adapter<StatusQuestionAdapter.ViewHolder>() {

    // ViewBinding을 사용하여 item_condition_question.xml 레이아웃 홀딩
    inner class ViewHolder(val binding: ItemConditionQuestionBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemConditionQuestionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = questions[position]
        holder.binding.apply {
            // 1. [기본 정보 바인딩] 질문 번호(1/5), 질문 제목, 최소/최대 라벨 설정
            tvStepCount.text = "${item.id}/5"
            tvQuestionTitle.text = item.title
            tvMinLabel.text = item.minLabel
            tvMaxLabel.text = item.maxLabel

            // 2. [슬라이더 초기 상태 설정]
            // 데이터 모델에 저장된 현재 점수를 슬라이더 위치에 반영
            slider.value = item.score.toFloat()
            // 현재 점수에 해당하는 가이드 문구(예: "8 - 잘 맞음")를 텍스트뷰에 표시
            tvSliderGuide.text = item.guides[item.score] ?: "${item.score} - 선택됨"

            // 3. [슬라이더 상호작용] 사용자가 슬라이더를 움직일 때 실시간 처리
            slider.addOnChangeListener { _, value, _ ->
                val selectedScore = value.toInt() // 소수점 값을 정수 점수로 변환

                // 사용자가 선택한 점수를 데이터 모델(StatusQuestion)에 즉시 저장
                item.score = selectedScore

                // 점수에 맞는 상세 가이드 문구로 텍스트 업데이트
                tvSliderGuide.text = item.guides[selectedScore] ?: "$selectedScore - 선택됨"
            }
        }
    }

    override fun getItemCount(): Int = questions.size
}