package com.example.healthcareapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.R
import com.example.healthcareapp.data.ExerciseRecord
import com.example.healthcareapp.data.ExerciseSet
import com.example.healthcareapp.databinding.ItemSetRowBinding
import com.example.healthcareapp.databinding.ItemSetRowCardioBinding

class ReadOnlyWorkoutAdapter(private val items: List<ExerciseRecord>) :
    RecyclerView.Adapter<ReadOnlyWorkoutAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tv_exercise_title)
        val rvSets: RecyclerView = view.findViewById(R.id.rv_sets)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_record_workoutcard, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val exercise = items[position]
        holder.tvTitle.text = String.format("%02d %s", position + 1, exercise.name)

        val setAdapter = if (exercise.isCardio) {
            ReadOnlyCardioSetAdapter(exercise.sets)
        } else {
            ReadOnlyStrengthSetAdapter(exercise.sets)
        }

        holder.rvSets.apply {
            layoutManager = LinearLayoutManager(holder.itemView.context)
            adapter = setAdapter
            isNestedScrollingEnabled = false
        }
    }

    override fun getItemCount() = items.size
}

class ReadOnlyStrengthSetAdapter(private val sets: List<ExerciseSet>) :
    RecyclerView.Adapter<ReadOnlyStrengthSetAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemSetRowBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSetRowBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        binding.etWeight.isFocusable = false
        binding.etWeight.isFocusableInTouchMode = false
        binding.etReps.isFocusable = false
        binding.etReps.isFocusableInTouchMode = false
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val set = sets[position]
        holder.binding.apply {
            tvSetNumber.text = "${set.setNumber}"
            etWeight.setText(set.weight.toBigDecimal().stripTrailingZeros().toPlainString())
            etReps.setText(set.reps.toString())
        }
    }

    override fun getItemCount() = sets.size
}

class ReadOnlyCardioSetAdapter(private val sets: List<ExerciseSet>) :
    RecyclerView.Adapter<ReadOnlyCardioSetAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemSetRowCardioBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSetRowCardioBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        binding.etDuration.isFocusable = false
        binding.etDuration.isFocusableInTouchMode = false
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val set = sets[position]
        holder.binding.apply {
            tvSetNumber.text = "${set.setNumber}"
            etDuration.setText(set.durationMinutes.toString())
        }
    }

    override fun getItemCount() = sets.size
}
