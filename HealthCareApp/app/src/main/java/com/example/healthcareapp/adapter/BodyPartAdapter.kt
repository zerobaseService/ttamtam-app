package com.example.healthcareapp.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.R
import com.example.healthcareapp.databinding.ItemBodyPartSelectionBinding

data class BodyPart(val name: String)

class BodyPartAdapter(
    private var parts: List<BodyPart>,
    private val isSelectedProvider: (String) -> Boolean = { false },
    private val onItemClick: (BodyPart) -> Unit
) : RecyclerView.Adapter<BodyPartAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemBodyPartSelectionBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBodyPartSelectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val part = parts[position]
        val selected = isSelectedProvider(part.name)

        holder.binding.tvPartName.text = part.name
        holder.binding.tvPartName.setTextColor(
            if (selected) Color.parseColor("#53A1FF") else Color.parseColor("#181818")
        )
        holder.binding.ivCheck.visibility = if (selected) View.VISIBLE else View.GONE
        holder.binding.ivArrow.setImageResource(R.drawable.arrowreverse)
        holder.itemView.setOnClickListener { onItemClick(part) }
    }

    fun updateItems(newItems: List<BodyPart>) {
        this.parts = newItems
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = parts.size
}