package com.example.healthcareapp.adapter

class EditableImageLogic(private val urls: MutableList<String>) {

    companion object {
        const val VIEW_TYPE_IMAGE = 0
        const val VIEW_TYPE_ADD = 1
        const val MAX_IMAGES = 5
    }

    val canAdd: Boolean get() = urls.size < MAX_IMAGES

    val itemCount: Int get() = urls.size + if (canAdd) 1 else 0

    fun getItemViewType(position: Int): Int =
        if (position < urls.size) VIEW_TYPE_IMAGE else VIEW_TYPE_ADD

    fun getUrl(position: Int): String = urls[position]

    fun add(url: String) {
        if (canAdd) urls.add(url)
    }

    fun remove(index: Int) {
        if (index in urls.indices) urls.removeAt(index)
    }

    fun toList(): List<String> = urls.toList()
}
