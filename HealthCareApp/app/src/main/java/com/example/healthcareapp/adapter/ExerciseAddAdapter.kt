package com.example.healthcareapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.data.ExerciseItem
import com.example.healthcareapp.databinding.ItemExersiseAddBinding

class ExerciseAddAdapter(
    private var items: List<ExerciseItem>,
    private val onSelectionChanged: (List<ExerciseItem>) -> Unit,
    private val onFavoriteToggle: (exerciseId: String, currentIsFavorite: Boolean) -> Unit
) : RecyclerView.Adapter<ExerciseAddAdapter.ViewHolder>() {

    private val selectedItems = mutableSetOf<ExerciseItem>()

    inner class ViewHolder(val binding: ItemExersiseAddBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemExersiseAddBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.apply {
            tvExerciseName.text = item.name
            btnFavorite.isSelected = item.isFavorite

            btnFavorite.setOnClickListener {
                onFavoriteToggle(item.id, item.isFavorite)
            }

            cbExercise.setOnCheckedChangeListener(null)
            cbExercise.isChecked = selectedItems.any { it.id == item.id }
            cbExercise.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) selectedItems.add(item)
                else selectedItems.removeAll { it.id == item.id }
                onSelectionChanged(selectedItems.toList())
            }

            root.setOnClickListener { cbExercise.isChecked = !cbExercise.isChecked }
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newList: List<ExerciseItem>) {
        items = newList
        notifyDataSetChanged()
    }

    fun getSelectedItems(): MutableSet<ExerciseItem> = selectedItems

    fun removeSelection(exerciseId: String) {
        val removed = selectedItems.removeIf { it.id == exerciseId }
        if (removed) {
            notifyDataSetChanged()
            onSelectionChanged(selectedItems.toList())
        }
    }

    fun updateFavoriteState(exerciseId: String, isFavorite: Boolean) {
        val idx = items.indexOfFirst { it.id == exerciseId }
        if (idx != -1) {
            items = items.toMutableList().also { it[idx] = it[idx].copy(isFavorite = isFavorite) }
            notifyItemChanged(idx)
        }
    }
}
