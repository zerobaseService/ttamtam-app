package com.example.healthcareapp.adapter

import androidx.recyclerview.widget.RecyclerView

class AccordionState(initialExpanded: Int = RecyclerView.NO_POSITION) {

    var expandedPosition: Int = initialExpanded
        private set

    /**
     * position을 토글하고, notifyItemChanged 대상 인덱스 목록을 반환한다.
     * 어댑터는 반환된 인덱스마다 notifyItemChanged를 호출하면 된다.
     */
    fun toggle(position: Int): List<Int> {
        val prev = expandedPosition
        expandedPosition = if (expandedPosition == position) RecyclerView.NO_POSITION else position

        val changed = mutableListOf<Int>()
        if (prev != RecyclerView.NO_POSITION) changed.add(prev)
        if (expandedPosition != RecyclerView.NO_POSITION) changed.add(expandedPosition)
        return changed.distinct()
    }
}
