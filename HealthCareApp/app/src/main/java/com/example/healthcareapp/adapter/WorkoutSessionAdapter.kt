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

    inner class ViewHolder(val binding: ItemWorkoutCardSessionBinding) : RecyclerView.ViewHolder(binding.root) {
        val setAdapter = WorkoutSessionSetAdapter(mutableListOf())
        val cardioAdapter = WorkoutCardioSetAdapter(mutableListOf())

        init {
            binding.rvSets.layoutManager = LinearLayoutManager(binding.root.context)
            binding.rvSets.isNestedScrollingEnabled = false
        }
    }

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
                val pos = holder.bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onDeleteExercise(pos)
                }
            }

            if (isCardio) {
                rvSets.adapter = holder.cardioAdapter
                holder.cardioAdapter.replaceSets(item.sets)
                layoutSetActions.visibility = View.GONE
            } else {
                rvSets.adapter = holder.setAdapter
                holder.setAdapter.replaceSets(item.sets)
                layoutSetActions.visibility = View.VISIBLE

                btnAddSet.setOnClickListener {
                    val nextSetNum = item.sets.size + 1
                    item.sets.add(WorkoutSessionSet(setNumber = nextSetNum))
                    holder.setAdapter.notifyItemInserted(item.sets.size - 1)
                }

                btnDeleteSet.setOnClickListener {
                    if (item.sets.size > 1) {
                        item.sets.removeAt(item.sets.size - 1)
                        item.sets.forEachIndexed { index, set -> set.setNumber = index + 1 }
                        holder.setAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size
}

class WorkoutSessionSetAdapter(private var sets: MutableList<WorkoutSessionSet>) :
    RecyclerView.Adapter<WorkoutSessionSetAdapter.SetViewHolder>() {

    class SetViewHolder(val binding: ItemWorkoutSetInputBinding) : RecyclerView.ViewHolder(binding.root) {
        var weightWatcher: TextWatcher? = null
        var repsWatcher: TextWatcher? = null
    }

    fun replaceSets(newSets: MutableList<WorkoutSessionSet>) {
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
        holder.binding.etWeight.removeTextChangedListener(holder.weightWatcher)
        holder.binding.etReps.removeTextChangedListener(holder.repsWatcher)

        val set = sets[position]
        holder.binding.tvSetNumber.text = "${set.setNumber}"
        holder.binding.etWeight.setText(
            if (set.weight == 0.0) "" else set.weight.toBigDecimal().stripTrailingZeros().toPlainString()
        )
        holder.binding.etReps.setText(if (set.reps == 0) "" else "${set.reps}")

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

    override fun onViewRecycled(holder: SetViewHolder) {
        super.onViewRecycled(holder)
        holder.binding.etWeight.removeTextChangedListener(holder.weightWatcher)
        holder.binding.etReps.removeTextChangedListener(holder.repsWatcher)
        holder.weightWatcher = null
        holder.repsWatcher = null
    }

    override fun getItemCount(): Int = sets.size
}

class WorkoutCardioSetAdapter(private var sets: MutableList<WorkoutSessionSet>) :
    RecyclerView.Adapter<WorkoutCardioSetAdapter.CardioViewHolder>() {

    class CardioViewHolder(val binding: ItemSetRowCardioBinding) : RecyclerView.ViewHolder(binding.root) {
        var durationWatcher: TextWatcher? = null
    }

    fun replaceSets(newSets: MutableList<WorkoutSessionSet>) {
        if (sets !== newSets) {
            sets = newSets
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardioViewHolder {
        val binding = ItemSetRowCardioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CardioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CardioViewHolder, position: Int) {
        holder.binding.etDuration.removeTextChangedListener(holder.durationWatcher)

        val set = sets[position]
        holder.binding.tvSetNumber.text = "${set.setNumber}"
        holder.binding.etDuration.setText(if (set.durationMinutes == 0) "" else "${set.durationMinutes}")

        holder.durationWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val pos = holder.adapterPosition
                if (pos != RecyclerView.NO_POSITION && pos < sets.size) {
                    sets[pos].durationMinutes = s.toString().toIntOrNull() ?: 0
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        holder.binding.etDuration.addTextChangedListener(holder.durationWatcher)
    }

    override fun onViewRecycled(holder: CardioViewHolder) {
        super.onViewRecycled(holder)
        holder.binding.etDuration.removeTextChangedListener(holder.durationWatcher)
        holder.durationWatcher = null
    }

    override fun getItemCount(): Int = sets.size
}
