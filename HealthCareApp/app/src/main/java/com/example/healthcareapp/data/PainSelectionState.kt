package com.example.healthcareapp.data

data class SelectedPain(val direction: String, val record: PainRecord)

class PainSelectionState {
    private val items = mutableListOf<SelectedPain>()
    val all: List<SelectedPain> get() = items.toList()

    fun addIfAbsent(direction: String, record: PainRecord): Boolean {
        val alreadyExists = items.any {
            it.direction == direction && it.record.sameTarget(record)
        }
        if (alreadyExists) return false
        items.add(SelectedPain(direction, record))
        return true
    }

    fun remove(direction: String, record: PainRecord) {
        items.removeAll { it.direction == direction && it.record.sameTarget(record) }
    }

    fun countByDirection(direction: String): Int = items.count { it.direction == direction }

    fun isSelected(direction: String, bodyPartName: String, side: String): Boolean =
        items.any { it.direction == direction && it.record.bodyPartName == bodyPartName && it.record.side == side }

    fun isAnySelected(direction: String, bodyPartName: String): Boolean =
        items.any { it.direction == direction && it.record.bodyPartName == bodyPartName }
}
