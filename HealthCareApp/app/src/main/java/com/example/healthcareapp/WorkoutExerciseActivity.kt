package com.example.healthcareapp

import WorkoutFinishDialog
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.healthcareapp.adapter.RecordWorkoutAdapter
import com.example.healthcareapp.databinding.ActivityWorkoutStartBinding
import com.example.healthcareapp.data.ExerciseRecord
import com.example.healthcareapp.data.ExerciseSet
import com.example.healthcareapp.utils.TimerManager
import java.text.SimpleDateFormat
import java.util.*

/**
 * 실제 운동을 기록하는 화면 (타이머, 세트 기록, 운동 추가)
 */
class WorkoutExerciseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWorkoutStartBinding
    private lateinit var workoutAdapter: RecordWorkoutAdapter

    private val personalWorkoutList = mutableListOf<ExerciseRecord>()
    private val ptWorkoutList = mutableListOf<ExerciseRecord>()

    private var isPtMode = false
    private var startTime: String = ""

    private val addExerciseLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val names = result.data?.getStringArrayExtra("exercise_names")
            names?.forEach { name -> addNewExercise(name) }
        }
    }
    private val finishResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // 1. WorkoutFinishActivity가 보낸 Intent(메모, 타입 등)를 가져옵니다.
            val intentData = result.data

            // 2. 그 데이터를 그대로 DiaryListFragment로 전달합니다.
            setResult(Activity.RESULT_OK, intentData)

            // 3. 그리고 종료해야 프래그먼트의 Launcher가 작동합니다.
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkoutStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

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
            val intent = Intent(this, AddExerciseActivity::class.java)
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
        Log.d("JaehoonTest", "[전송직전] isPtMode: $isPtMode, 결정된 타입: $workoutType")

        intent.putExtra("WORKOUT_TYPE", workoutType)

        // ⭐ 반드시 finishResultLauncher로 실행해야 결과를 받아올 수 있습니다.
        finishResultLauncher.launch(intent)
    }

    private fun setupTabLogic() {
        updateTabUI()
        binding.tvTabPersonal.setOnClickListener {
            if (isPtMode) {
                isPtMode = false
                // ⭐ 로그 2: 개인운동 탭 클릭 시 상태 변화 확인
                Log.d("JaehoonTest", "[탭클릭] 개인운동 선택됨 (isPtMode: $isPtMode)")
                updateTabUI()
                switchListData()
            }
        }
        binding.tvTabPt.setOnClickListener {
            if (!isPtMode) {
                isPtMode = true
                // ⭐ 로그 3: PT 탭 클릭 시 상태 변화 확인
                Log.d("JaehoonTest", "[탭클릭] PT 선택됨 (isPtMode: $isPtMode)")
                updateTabUI()
                switchListData()
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
        binding.btnAddWorkout.text = "+ 운동 추가하기"
    }

    private fun switchListData() {
        val currentList = if (isPtMode) ptWorkoutList else personalWorkoutList
        workoutAdapter = RecordWorkoutAdapter(currentList)
        binding.rvWorkoutList.adapter = workoutAdapter
        binding.rvWorkoutList.scheduleLayoutAnimation()
    }

    private fun setupRecyclerView() {
        workoutAdapter = RecordWorkoutAdapter(personalWorkoutList)
        binding.rvWorkoutList.apply {
            layoutManager = LinearLayoutManager(this@WorkoutExerciseActivity)
            adapter = workoutAdapter
        }
    }

    private fun addNewExercise(name: String) {
        val currentList = if (isPtMode) ptWorkoutList else personalWorkoutList
        val newWorkout = ExerciseRecord(
            id = currentList.size + 1,
            name = name,
            sets = mutableListOf(ExerciseSet(setNumber = 1, weight = 0, reps = 0))
        )
        currentList.add(newWorkout)
        workoutAdapter.notifyItemInserted(currentList.size - 1)
        binding.rvWorkoutList.scrollToPosition(currentList.size - 1)
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}