package com.example.healthcareapp.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.data.ExerciseSet
import com.example.healthcareapp.databinding.ItemWorkoutSetInputBinding

class WorkoutSetAdapter(private var sets: MutableList<ExerciseSet>) :
    RecyclerView.Adapter<WorkoutSetAdapter.SetViewHolder>() {

    class SetViewHolder(val binding: ItemWorkoutSetInputBinding) : RecyclerView.ViewHolder(binding.root) {
        var weightWatcher: TextWatcher? = null
        var repsWatcher: TextWatcher? = null
    }

    // 다른 exercise로 교체될 때만 notifyDataSetChanged 호출 (같은 list 재bind 시 생략)
    fun replaceSets(newSets: MutableList<ExerciseSet>) {
        if (sets !== newSets) {
            sets = newSets
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SetViewHolder {
        val binding = ItemWorkoutSetInputBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SetViewHolder, position: Int) {
        // 기존 watcher 제거 (ViewHolder 재활용 시 오염 방지)
        holder.binding.etWeight.removeTextChangedListener(holder.weightWatcher)
        holder.binding.etReps.removeTextChangedListener(holder.repsWatcher)

        val set = sets[position]
        holder.binding.tvSetNumber.text = "${set.setNumber}"
        holder.binding.etWeight.setText(
            if (set.weight == 0.0) "" else set.weight.toBigDecimal().stripTrailingZeros().toPlainString()
        )
        holder.binding.etReps.setText(if (set.reps == 0) "" else "${set.reps}")

        // adapterPosition 기반으로 현재 bind된 위치의 set을 수정 (recycling 시 오염 방지)
        holder.weightWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val pos = holder.adapterPosition
                if (pos != RecyclerView.NO_POSITION && pos < sets.size) {
                    sets[pos].weight = s.toString().toDoubleOrNull() ?: 0.0
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        holder.repsWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val pos = holder.adapterPosition
                if (pos != RecyclerView.NO_POSITION && pos < sets.size) {
                    sets[pos].reps = s.toString().toIntOrNull() ?: 0
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        holder.binding.etWeight.addTextChangedListener(holder.weightWatcher)
        holder.binding.etReps.addTextChangedListener(holder.repsWatcher)
    }

    // RecycledViewPool으로 반환될 때 watcher 완전 제거
    override fun onViewRecycled(holder: SetViewHolder) {
        super.onViewRecycled(holder)
        holder.binding.etWeight.removeTextChangedListener(holder.weightWatcher)
        holder.binding.etReps.removeTextChangedListener(holder.repsWatcher)
        holder.weightWatcher = null
        holder.repsWatcher = null
    }

    override fun getItemCount(): Int = sets.size
}
