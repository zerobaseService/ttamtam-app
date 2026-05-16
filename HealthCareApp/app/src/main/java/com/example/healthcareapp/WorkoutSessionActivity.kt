package com.example.healthcareapp

import WorkoutFinishDialog
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.healthcareapp.adapter.WorkoutSessionAdapter
import com.example.healthcareapp.data.ExerciseRecord
import com.example.healthcareapp.data.ExerciseSet
import com.example.healthcareapp.data.WorkoutSessionExercise
import com.example.healthcareapp.data.WorkoutSessionSet
import com.example.healthcareapp.databinding.ActivityWorkoutSessionBinding
import com.example.healthcareapp.utils.TimerManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WorkoutSessionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWorkoutSessionBinding
    private lateinit var workoutSessionAdapter: WorkoutSessionAdapter

    private val personalWorkoutList = mutableListOf<WorkoutSessionExercise>()
    private val ptWorkoutList = mutableListOf<WorkoutSessionExercise>()

    private var isPtMode = false
    private var startTime: String = ""

    private var journalId: Long = -1L
    private var workoutDate: String = ""
    private var startedAt: String = ""

    private val currentList get() = if (isPtMode) ptWorkoutList else personalWorkoutList

    private val addExerciseLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val names = result.data?.getStringArrayExtra("exercise_names")
            val ids = result.data?.getStringArrayExtra("exercise_ids")
            val bodyParts = result.data?.getStringArrayExtra("exercise_body_parts")
            names?.forEachIndexed { index, name ->
                val id = ids?.getOrNull(index) ?: ""
                val bodyPart = bodyParts?.getOrNull(index) ?: ""
                addNewExercise(name, id, bodyPart)
            }
        }
    }

    private val finishResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            setResult(Activity.RESULT_OK, result.data)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkoutSessionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        journalId = intent.getLongExtra("JOURNAL_ID", -1L)
        workoutDate = intent.getStringExtra("WORKOUT_DATE") ?: ""
        startedAt = intent.getStringExtra("STARTED_AT") ?: ""

        setupRecyclerView()
        setupUI()
        setupTabLogic()
        syncTimer()
    }

    private fun setupUI() {
        val sdf = SimpleDateFormat("yy.MM.dd", Locale.getDefault())
        binding.tvDate.text = sdf.format(Date())

        binding.btnClose.setOnClickListener { finish() }

        binding.btnAddWorkout.setOnClickListener {
            val intent = Intent(this, AddExerciseSessionActivity::class.java)
            intent.putExtra("IS_PT_MODE", isPtMode)
            addExerciseLauncher.launch(intent)
        }

        binding.btnPause.setOnClickListener {
            if (TimerManager.isRunning()) {
                TimerManager.pauseTimer()
                binding.btnPause.setImageResource(R.drawable.play)
            } else {
                TimerManager.startTimer()
                binding.btnPause.setImageResource(R.drawable.pause)
            }
        }

        binding.btnFinishWorkout.setOnClickListener {
            showFinishDialog()
        }
    }

    private fun syncTimer() {
        TimerManager.timeLiveData.observe(this) {
            binding.tvTimer.text = TimerManager.getFormattedTime()
        }

        if (TimerManager.isRunning()) {
            binding.btnPause.setImageResource(R.drawable.pause)
        } else {
            binding.btnPause.setImageResource(R.drawable.play)
        }
    }

    private fun showFinishDialog() {
        val dialog = WorkoutFinishDialog {
            moveToFinishActivity()
        }
        dialog.show(supportFragmentManager, "WorkoutFinishDialog")
    }

    private fun moveToFinishActivity() {
        val endTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val totalTimerTime = binding.tvTimer.text.toString()

        val intent = Intent(this, WorkoutFinishActivity::class.java)
        intent.putExtra("TOTAL_TIME", totalTimerTime)
        intent.putExtra("START_TIME", startTime)
        intent.putExtra("END_TIME", endTime)

        val workoutType = if (isPtMode) "PT" else "개인운동"
        intent.putExtra("WORKOUT_TYPE", workoutType)
        intent.putExtra("JOURNAL_ID", journalId)
        intent.putExtra("WORKOUT_DATE", workoutDate)
        intent.putExtra("STARTED_AT", startedAt)

        val exercises = ArrayList(currentList.toExerciseRecords())
        intent.putExtra("EXERCISES", exercises)

        finishResultLauncher.launch(intent)
    }

    private fun setupTabLogic() {
        updateTabUI()
        binding.tvTabPersonal.setOnClickListener {
            if (isPtMode) {
                isPtMode = false
                updateTabUI()
                refreshAdapter()
            }
        }
        binding.tvTabPt.setOnClickListener {
            if (!isPtMode) {
                isPtMode = true
                updateTabUI()
                refreshAdapter()
            }
        }
    }

    private fun updateTabUI() {
        binding.tvTabPersonal.isSelected = !isPtMode
        binding.tvTabPt.isSelected = isPtMode

        if (isPtMode) {
            binding.tvTabPt.paint.isFakeBoldText = true
            binding.tvTabPersonal.paint.isFakeBoldText = false
        } else {
            binding.tvTabPersonal.paint.isFakeBoldText = true
            binding.tvTabPt.paint.isFakeBoldText = false
        }
    }

    private fun setupRecyclerView() {
        workoutSessionAdapter = WorkoutSessionAdapter(currentList, ::onDeleteExercise)
        binding.rvWorkoutList.apply {
            layoutManager = LinearLayoutManager(this@WorkoutSessionActivity)
            adapter = workoutSessionAdapter
        }
    }

    private fun refreshAdapter() {
        workoutSessionAdapter = WorkoutSessionAdapter(currentList, ::onDeleteExercise)
        binding.rvWorkoutList.adapter = workoutSessionAdapter
        binding.rvWorkoutList.scheduleLayoutAnimation()
    }

    private fun onDeleteExercise(position: Int) {
        currentList.removeAt(position)
        workoutSessionAdapter.notifyItemRemoved(position)
    }

    private fun addNewExercise(name: String, id: String, bodyPart: String) {
        val isCardio = bodyPart.lowercase() == "cardio"
        val initialSets = if (isCardio) {
            mutableListOf(WorkoutSessionSet(setNumber = 1))
        } else {
            mutableListOf(WorkoutSessionSet(setNumber = 1))
        }
        val newExercise = WorkoutSessionExercise(
            id = id,
            name = name,
            bodyPart = bodyPart,
            sets = initialSets
        )
        currentList.add(newExercise)
        workoutSessionAdapter.notifyItemInserted(currentList.size - 1)
        binding.rvWorkoutList.scrollToPosition(currentList.size - 1)
    }

    private fun List<WorkoutSessionExercise>.toExerciseRecords(): List<ExerciseRecord> {
        return mapIndexed { index, exercise ->
            val isCardio = exercise.bodyPart.lowercase() == "cardio"
            ExerciseRecord(
                id = index + 1,
                name = exercise.name,
                sets = exercise.sets.map { set ->
                    if (isCardio) {
                        ExerciseSet(set.setNumber, weight = 0.0, reps = 0, durationMinutes = set.durationMinutes)
                    } else {
                        ExerciseSet(set.setNumber, weight = set.weight, reps = set.reps)
                    }
                }.toMutableList()
            )
        }
    }
}
