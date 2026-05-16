package com.example.healthcareapp.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.healthcareapp.adapter.ConditionAdapter
import com.example.healthcareapp.data.ConditionRecord
import com.example.healthcareapp.data.PostConditionResponse
import com.example.healthcareapp.data.PreConditionResponse
import com.example.healthcareapp.data.StatusQuestion1
import com.example.healthcareapp.databinding.FragmentConditionCheckBinding

class ConditionCheckFragment : Fragment() {

    private var _binding: FragmentConditionCheckBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConditionCheckBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pre = arguments?.getSerializable("PRE_CONDITION") as? PreConditionResponse
        val post = arguments?.getSerializable("POST_CONDITION") as? PostConditionResponse

        val conditionList = buildConditionList(pre, post)

        binding.rvConditionList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ConditionAdapter(conditionList)
            itemAnimator = null
        }
    }

    private fun buildConditionList(
        pre: PreConditionResponse?,
        post: PostConditionResponse?
    ): List<ConditionRecord> {
        val result = mutableListOf<ConditionRecord>()

        if (pre != null) {
            val preQuestions = listOfNotNull(
                pre.jointMusclePain?.let { StatusQuestion1("1/5", "관절/근육 통증", it.toFloat(), "매우 심함", "통증 없음") },
                pre.sleepHours?.let     { StatusQuestion1("2/5", "수면 시간",      it.toFloat(), "1시간",   "10시간") },
                pre.sleepQuality?.let   { StatusQuestion1("3/5", "수면 질",        it.toFloat(), "거의 못 잠", "푹 개운함") },
                pre.previousFatigue?.let { StatusQuestion1("4/5", "이전 피로도",   it.toFloat(), "매우 많이 남음", "전혀 없음") },
                pre.overallCondition?.let { StatusQuestion1("5/5", "전반적 컨디션", it.toFloat(), "매우 안 좋음", "최상") }
            )
            if (preQuestions.isNotEmpty()) {
                result += ConditionRecord(title = "운동 전 컨디션", questions = preQuestions)
            }
        }

        if (post != null) {
            val postQuestions = listOf(
                StatusQuestion1("1/5", "관절/근육 통증",   post.jointMusclePain.toFloat(), "매우 심함",    "통증 없음"),
                StatusQuestion1("2/5", "운동 강도 적합도", post.intensityFit.toFloat(),    "너무 안 맞음", "딱 맞음"),
                StatusQuestion1("3/5", "목표 달성도",      post.goalAchieved.toFloat(),    "거의 못 함",   "많이 함"),
                StatusQuestion1("4/5", "어지러움",         post.dizziness.toFloat(),       "전혀 없음",    "매우 심함"),
                StatusQuestion1("5/5", "전반적 기분",      post.mood.toFloat(),            "매우 안 좋음", "매우 개운")
            )
            result += ConditionRecord(title = "운동 후 컨디션", questions = postQuestions)
        }

        return result
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
