package com.example.healthcareapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.data.PainRecord
import com.example.healthcareapp.data.PainRecordFormatter
import com.example.healthcareapp.databinding.ItemPainTagReadonlyBinding

class PainTagAdapter(private val items: List<PainRecord>) :
    RecyclerView.Adapter<PainTagAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemPainTagReadonlyBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPainTagReadonlyBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val record = items[position]
        holder.binding.tvPainSummary.text = PainRecordFormatter.displaySummary(record)
        val reasonVisible = PainRecordFormatter.reasonVisible(record.painReason)
        holder.binding.tvPainReason.visibility = if (reasonVisible) View.VISIBLE else View.GONE
        if (reasonVisible) holder.binding.tvPainReason.text = record.painReason
    }

    override fun getItemCount(): Int = items.size
}
