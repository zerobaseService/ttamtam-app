package com.example.healthcareapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.databinding.ItemWorkoutCardBinding
import com.example.healthcareapp.data.ExerciseRecord
import com.example.healthcareapp.data.ExerciseSet

class RecordWorkoutAdapter(private val items: MutableList<ExerciseRecord>) :
    RecyclerView.Adapter<RecordWorkoutAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemWorkoutCardBinding) : RecyclerView.ViewHolder(binding.root) {
        val setAdapter = WorkoutSetAdapter(mutableListOf())

        init {
            binding.rvSets.layoutManager = LinearLayoutManager(binding.root.context)
            binding.rvSets.adapter = setAdapter
            binding.rvSets.isNestedScrollingEnabled = false
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemWorkoutCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.binding.tvExerciseTitle.text = String.format("%02d %s", position + 1, item.name)

        holder.setAdapter.replaceSets(item.sets)

        holder.binding.btnAddSet.setOnClickListener {
            val nextSetNum = item.sets.size + 1
            item.sets.add(ExerciseSet(nextSetNum, 0.0, 0))
            holder.setAdapter.notifyItemInserted(item.sets.size - 1)
        }

        holder.binding.btnDeleteSet.setOnClickListener {
            if (item.sets.isNotEmpty()) {
                item.sets.removeAt(item.sets.size - 1)
                item.sets.forEachIndexed { index, set -> set.setNumber = index + 1 }
                holder.setAdapter.notifyDataSetChanged()
            }
        }

        holder.binding.btnMore.setOnClickListener { }
    }

    override fun getItemCount(): Int = items.size
}
