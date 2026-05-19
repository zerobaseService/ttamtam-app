package com.example.healthcareapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.healthcareapp.databinding.ItemEditableImageAddBinding
import com.example.healthcareapp.databinding.ItemEditableImageBinding

class EditableImageAdapter(
    private val logic: EditableImageLogic,
    private val onAdd: () -> Unit,
    private val onRemove: (index: Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class ImageHolder(val binding: ItemEditableImageBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class AddHolder(val binding: ItemEditableImageAddBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun getItemCount(): Int = logic.itemCount

    override fun getItemViewType(position: Int): Int = logic.getItemViewType(position)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == EditableImageLogic.VIEW_TYPE_IMAGE) {
            ImageHolder(ItemEditableImageBinding.inflate(inflater, parent, false))
        } else {
            AddHolder(ItemEditableImageAddBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ImageHolder -> {
                val url = logic.getUrl(position)
                Glide.with(holder.binding.ivThumbnail).load(url).centerCrop()
                    .into(holder.binding.ivThumbnail)
                holder.binding.btnRemove.setOnClickListener {
                    val pos = holder.bindingAdapterPosition
                    if (pos != RecyclerView.NO_POSITION) onRemove(pos)
                }
            }
            is AddHolder -> {
                holder.binding.root.setOnClickListener { onAdd() }
            }
        }
    }
}
