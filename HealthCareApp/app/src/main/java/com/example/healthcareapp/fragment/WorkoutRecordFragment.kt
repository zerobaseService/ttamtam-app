package com.example.healthcareapp.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.R
import com.example.healthcareapp.adapter.ReadOnlyWorkoutAdapter
import com.example.healthcareapp.data.ExerciseDetailResponse
import com.example.healthcareapp.data.ExerciseRecord
import com.example.healthcareapp.data.ExerciseSet

class WorkoutRecordFragment : Fragment(R.layout.fragment_workout) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvWorkout = view.findViewById<RecyclerView>(R.id.rv_workout_list)

        @Suppress("UNCHECKED_CAST")
        val exercises = (arguments?.getSerializable("EXERCISES") as? ArrayList<ExerciseDetailResponse>)
            ?.map { it.toExerciseRecord() }
            ?.toMutableList()
            ?: mutableListOf()

        rvWorkout.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ReadOnlyWorkoutAdapter(exercises)
        }
    }
}

private fun ExerciseDetailResponse.toExerciseRecord() = ExerciseRecord(
    id = exerciseId.toInt(),
    name = exerciseName,
    isCardio = sets.any { (it.durationMinutes ?: 0) > 0 },
    sets = sets.map { set ->
        ExerciseSet(
            setNumber = set.setNumber,
            weight = set.weightKg,
            reps = set.reps,
            durationMinutes = set.durationMinutes ?: 0
        )
    }.toMutableList()
)
