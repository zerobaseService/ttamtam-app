package com.example.healthcareapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.data.StatusQuestion
import com.example.healthcareapp.databinding.ItemConditionQuestionV2Binding

class StatusQuestionAdapter(private val questions: List<StatusQuestion>) :
    RecyclerView.Adapter<StatusQuestionAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemConditionQuestionV2Binding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemConditionQuestionV2Binding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = questions[position]

        holder.binding.apply {
            tvStepCount.text = "${item.id}/5"
            tvQuestionTitle.text = item.title
            tvMinLabel.text = item.minLabel
            tvMaxLabel.text = item.maxLabel

            val dotSlider = slider
            dotSlider.isEditEnabled = true
            dotSlider.max = 10
            dotSlider.progress = item.score
            tvSliderGuide.text = item.guides[item.score] ?: "${item.score} - 선택됨"

            dotSlider.onProgressChanged = { value ->
                val currentPos = holder.bindingAdapterPosition
                if (currentPos != RecyclerView.NO_POSITION) {
                    questions[currentPos].score = value
                    tvSliderGuide.text = item.guides[value] ?: "$value - 선택됨"
                }
            }
        }
    }

    override fun getItemCount(): Int = questions.size
}
