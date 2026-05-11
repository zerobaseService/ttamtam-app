package com.example.healthcareapp

import WorkoutFinishDialog
import android.app.Activity
import android.content.Intent
import android.os.Bundle
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

class WorkoutExerciseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWorkoutStartBinding
    private lateinit var workoutAdapter: RecordWorkoutAdapter

    private val personalWorkoutList = mutableListOf<ExerciseRecord>()
    private val ptWorkoutList = mutableListOf<ExerciseRecord>()

    private var isPtMode = false
    private var startTime: String = ""

    // 1. 운동 추가 화면 결과 처리
    private val addExerciseLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val names = result.data?.getStringArrayExtra("exercise_names")
            names?.forEach { name -> addNewExercise(name) }
        }
    }

    // 2. 최종 완료 화면 결과 처리
    private val finishResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            setResult(Activity.RESULT_OK)
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
        syncTimer() // 타이머 연결 및 상태 동기화
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

        // ⭐ 핵심: TimerManager를 직접 제어하도록 수정
        binding.btnPause.setOnClickListener {
            if (TimerManager.isRunning()) {
                // 현재 돌아가고 있으면 일시정지
                TimerManager.pauseTimer()
                binding.btnPause.setImageResource(R.drawable.play) // 재생 아이콘으로 변경
            } else {
                // 멈춰 있으면 다시 시작
                TimerManager.startTimer()
                binding.btnPause.setImageResource(R.drawable.pause) // 일시정지 아이콘으로 변경
            }
        }

        binding.btnFinishWorkout.setOnClickListener {
            showFinishDialog()
        }
    }

    /**
     * 전역 타이머와 현재 UI를 연결하는 함수
     */
    private fun syncTimer() {
        // 1. 관찰자 등록 (시간 변경 시 텍스트 업데이트)
        TimerManager.timeLiveData.observe(this) {
            binding.tvTimer.text = TimerManager.getFormattedTime()
        }

        // 2. 진입 시 현재 타이머 상태에 따라 아이콘 세팅
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

        finishResultLauncher.launch(intent)
    }

    private fun setupTabLogic() {
        updateTabUI()
        binding.tvTabPersonal.setOnClickListener {
            if (isPtMode) {
                isPtMode = false
                updateTabUI()
                switchListData()
            }
        }
        binding.tvTabPt.setOnClickListener {
            if (!isPtMode) {
                isPtMode = true
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
        // 전역 타이머이므로 여기서 멈추지 않습니다.
    }
}