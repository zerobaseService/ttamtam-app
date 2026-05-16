package com.example.healthcareapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.R
import com.example.healthcareapp.data.JournalSummaryResponse

class DiaryListAdapter(
    private val onItemClick: (JournalSummaryResponse) -> Unit
) : RecyclerView.Adapter<DiaryListAdapter.ViewHolder>() {

    private val items: MutableList<JournalSummaryResponse> = mutableListOf()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvWorkoutDate: TextView = view.findViewById(R.id.tv_workout_date)
        val tvCreatedTime: TextView = view.findViewById(R.id.tv_created_time)
        val tvPostRecordedBadge: TextView = view.findViewById(R.id.tv_post_recorded_badge)
        val layoutCondition: LinearLayout = view.findViewById(R.id.layout_condition)
        val tvPreOverallCondition: TextView = view.findViewById(R.id.tv_pre_overall_condition)
        val tvContentPreview: TextView = view.findViewById(R.id.tv_content_preview)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_diary, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val journal = items[position]

        holder.tvWorkoutDate.text = journal.workoutDate

        val time = journal.createdAt.substringAfter('T', "").take(5)
        holder.tvCreatedTime.text = time

        if (journal.postRecorded) {
            holder.tvPostRecordedBadge.visibility = View.VISIBLE
        } else {
            holder.tvPostRecordedBadge.visibility = View.GONE
        }

        if (journal.preOverallCondition != null) {
            holder.layoutCondition.visibility = View.VISIBLE
            holder.tvPreOverallCondition.text = journal.preOverallCondition.toString()
        } else {
            holder.layoutCondition.visibility = View.GONE
        }

        if (!journal.contentPreview.isNullOrBlank()) {
            holder.tvContentPreview.text = journal.contentPreview
            holder.tvContentPreview.setTextColor(holder.itemView.context.getColor(android.R.color.darker_gray))
        } else {
            holder.tvContentPreview.text = "기록 없음"
            holder.tvContentPreview.setTextColor(holder.itemView.context.getColor(android.R.color.darker_gray))
        }

        holder.itemView.setOnClickListener { onItemClick(journal) }
    }

    override fun getItemCount() = items.size

    fun submit(newItems: List<JournalSummaryResponse>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
