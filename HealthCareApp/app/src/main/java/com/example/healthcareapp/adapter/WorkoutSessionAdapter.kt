package com.example.healthcareapp.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.data.WorkoutSessionExercise
import com.example.healthcareapp.data.WorkoutSessionSet
import com.example.healthcareapp.databinding.ItemSetRowCardioBinding
import com.example.healthcareapp.databinding.ItemWorkoutCardSessionBinding
import com.example.healthcareapp.databinding.ItemWorkoutSetInputBinding

class WorkoutSessionAdapter(
    private val items: MutableList<WorkoutSessionExercise>,
    private val onDeleteExercise: (position: Int) -> Unit
) : RecyclerView.Adapter<WorkoutSessionAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemWorkoutCardSessionBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemWorkoutCardSessionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val isCardio = item.bodyPart.lowercase() == "cardio"

        holder.binding.apply {
            tvExerciseTitle.text = String.format("%02d %s", position + 1, item.name)

            btnDeleteExercise.setOnClickListener {
                val adapterPosition = holder.bindingAdapterPosition
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onDeleteExercise(adapterPosition)
                }
            }

            if (isCardio) {
                val cardioAdapter = WorkoutCardioSetAdapter(item.sets)
                rvSets.apply {
                    layoutManager = LinearLayoutManager(root.context)
                    adapter = cardioAdapter
                    isNestedScrollingEnabled = false
                }
                layoutSetActions.visibility = View.GONE
            } else {
                val setAdapter = WorkoutSessionSetAdapter(item.sets)
                rvSets.apply {
                    layoutManager = LinearLayoutManager(root.context)
                    adapter = setAdapter
                    isNestedScrollingEnabled = false
                }
                layoutSetActions.visibility = View.VISIBLE

                btnAddSet.setOnClickListener {
                    val nextSetNum = item.sets.size + 1
                    item.sets.add(WorkoutSessionSet(setNumber = nextSetNum))
                    setAdapter.notifyDataSetChanged()
                }

                btnDeleteSet.setOnClickListener {
                    if (item.sets.size > 1) {
                        item.sets.removeAt(item.sets.size - 1)
                        setAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size
}

class WorkoutSessionSetAdapter(private val sets: MutableList<WorkoutSessionSet>) :
    RecyclerView.Adapter<WorkoutSessionSetAdapter.SetViewHolder>() {

    inner class SetViewHolder(val binding: ItemWorkoutSetInputBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SetViewHolder {
        val binding = ItemWorkoutSetInputBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SetViewHolder, position: Int) {
        val set = sets[position]
        holder.binding.apply {
            tvSetNumber.text = "${set.setNumber}"
            etWeight.setText(if (set.weight == 0.0) "" else set.weight.toBigDecimal().stripTrailingZeros().toPlainString())
            etReps.setText(if (set.reps == 0) "" else "${set.reps}")

            etWeight.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    set.weight = s.toString().toDoubleOrNull() ?: 0.0
                }
                override fun afterTextChanged(s: Editable?) {}
            })

            etReps.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    set.reps = s.toString().toIntOrNull() ?: 0
                }
                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }

    override fun getItemCount(): Int = sets.size
}

class WorkoutCardioSetAdapter(private val sets: MutableList<WorkoutSessionSet>) :
    RecyclerView.Adapter<WorkoutCardioSetAdapter.CardioViewHolder>() {

    inner class CardioViewHolder(val binding: ItemSetRowCardioBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardioViewHolder {
        val binding = ItemSetRowCardioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CardioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CardioViewHolder, position: Int) {
        val set = sets[position]
        holder.binding.apply {
            tvSetNumber.text = "${set.setNumber}"
            etDuration.setText(if (set.durationMinutes == 0) "" else "${set.durationMinutes}")

            etDuration.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    set.durationMinutes = s.toString().toIntOrNull() ?: 0
                }
                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }

    override fun getItemCount(): Int = sets.size
}
