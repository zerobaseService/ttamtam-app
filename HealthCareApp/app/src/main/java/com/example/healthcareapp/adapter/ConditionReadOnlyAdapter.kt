package com.example.healthcareapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.data.ConditionRecord
import com.example.healthcareapp.data.ConditionSliderGuide
import com.example.healthcareapp.data.RecordedAtFormatter
import com.example.healthcareapp.data.StatusQuestion1
import com.example.healthcareapp.databinding.ItemConditionReadonlyBinding
import com.example.healthcareapp.databinding.ItemConditionQuestionV2Binding

class ConditionReadOnlyAdapter(
    private val items: List<ConditionRecord>
) : RecyclerView.Adapter<ConditionReadOnlyAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemConditionReadonlyBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemConditionReadonlyBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val binding = holder.binding

        binding.tvConditionTitle.text = item.title

        val formattedTime = RecordedAtFormatter.format(item.recordedAt)
        if (formattedTime != null) {
            binding.tvFinishStatus.text = "$formattedTime 작성 완료"
            binding.tvFinishStatus.visibility = View.VISIBLE
        } else {
            binding.tvFinishStatus.visibility = View.GONE
        }

        binding.layoutDetail.visibility = if (item.isExpanded) View.VISIBLE else View.GONE
        binding.ivArrow.rotation = if (item.isExpanded) 180f else 0f

        binding.layoutHeader.setOnClickListener {
            item.isExpanded = !item.isExpanded
            notifyItemChanged(holder.bindingAdapterPosition)
        }

        binding.rvQuestions.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ReadOnlyQuestionAdapter(item.questions)
            isNestedScrollingEnabled = false
        }

        val hasPain = item.painRecords.isNotEmpty()
        binding.layoutPainSection.visibility = if (hasPain) View.VISIBLE else View.GONE
        if (hasPain) {
            binding.rvPainTags.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = PainTagAdapter(item.painRecords)
                isNestedScrollingEnabled = false
            }
        }

        val hasMemo = item.memo.isNotBlank()
        binding.layoutMemoSection.visibility = if (hasMemo) View.VISIBLE else View.GONE
        if (hasMemo) binding.tvFeedbackMemo.text = item.memo
    }

    override fun getItemCount(): Int = items.size

    private inner class ReadOnlyQuestionAdapter(
        private val questions: List<StatusQuestion1>
    ) : RecyclerView.Adapter<ReadOnlyQuestionAdapter.QViewHolder>() {

        inner class QViewHolder(val binding: ItemConditionQuestionV2Binding) :
            RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QViewHolder {
            val binding = ItemConditionQuestionV2Binding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return QViewHolder(binding)
        }

        override fun onBindViewHolder(holder: QViewHolder, position: Int) {
            val q = questions[position]
            holder.binding.apply {
                tvStepCount.text = q.step
                tvQuestionTitle.text = q.title
                tvMinLabel.text = q.minLabel
                tvMaxLabel.text = q.maxLabel

                val score = q.score.toInt().coerceIn(1, 10)
                slider.isEditEnabled = false
                slider.max = 10
                slider.progress = score
                tvSliderGuide.text = ConditionSliderGuide.guideFor(position, score)
            }
        }

        override fun getItemCount(): Int = questions.size
    }
}
