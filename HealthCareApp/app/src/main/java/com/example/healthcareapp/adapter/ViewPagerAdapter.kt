package com.example.healthcareapp.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.healthcareapp.data.JournalDetailResponse
import com.example.healthcareapp.fragment.ConditionCheckFragment
import com.example.healthcareapp.fragment.WorkoutRecordFragment

class ViewPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val detail: JournalDetailResponse
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> WorkoutRecordFragment().apply {
            arguments = Bundle().apply {
                putSerializable("EXERCISES", ArrayList(detail.exercises ?: emptyList()))
            }
        }
        1 -> ConditionCheckFragment().apply {
            arguments = Bundle().apply {
                putSerializable("PRE_CONDITION", detail.preCondition)
                putSerializable("POST_CONDITION", detail.postCondition)
            }
        }
        else -> throw IllegalArgumentException("Unknown tab position: $position")
    }
}
