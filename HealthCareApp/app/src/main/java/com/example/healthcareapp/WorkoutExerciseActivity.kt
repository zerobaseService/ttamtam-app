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

/**
 * 실제 운동을 기록하는 화면 (타이머, 세트 기록, 운동 추가)
 */
class WorkoutExerciseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWorkoutStartBinding
    private lateinit var workoutAdapter: RecordWorkoutAdapter

    // 모드별(개인/PT)로 운동 리스트를 따로 관리하여 탭 전환 시 데이터 유지
    private val personalWorkoutList = mutableListOf<ExerciseRecord>()
    private val ptWorkoutList = mutableListOf<ExerciseRecord>()

    private var isPtMode = false // 현재 PT 모드 여부
    private var startTime: String = "" // 운동 시작 시간 저장

    // 1. [운동 추가 화면] 결과 처리 Launcher
    // AddExerciseActivity에서 선택한 운동 이름 목록을 받아 리스트에 추가함
    private val addExerciseLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val names = result.data?.getStringArrayExtra("exercise_names")
            names?.forEach { name -> addNewExercise(name) }
        }
    }

    // 2. [최종 완료 화면] 결과 처리 Launcher
    // WorkoutFinishActivity에서 '확인'을 누르면 이전 화면(메인 등)으로 돌아가기 위해 종료
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

        // 앱 진입 시 현재 시간을 "14:00" 형태로 기록
        startTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        setupRecyclerView() // 리사이클러뷰 초기 설정
        setupUI()           // 클릭 리스너 및 기본 UI 세팅
        setupTabLogic()     // 개인/PT 탭 전환 로직 설정
        syncTimer()         // 전역 타이머와 UI 상태 동기화
    }

    private fun setupUI() {
        // 상단 날짜 표시 (yy.MM.dd)
        val sdf = SimpleDateFormat("yy.MM.dd", Locale.getDefault())
        binding.tvDate.text = sdf.format(Date())

        binding.btnClose.setOnClickListener { finish() }

        // 운동 추가하기 버튼: 현재 모드 정보를 가지고 이동
        binding.btnAddWorkout.setOnClickListener {
            val intent = Intent(this, AddExerciseActivity::class.java)
            intent.putExtra("IS_PT_MODE", isPtMode)
            addExerciseLauncher.launch(intent)
        }

        // 일시정지/재생 버튼: TimerManager 제어
        binding.btnPause.setOnClickListener {
            if (TimerManager.isRunning()) {
                TimerManager.pauseTimer()
                binding.btnPause.setImageResource(R.drawable.play) // 멈추면 재생 아이콘 표시
            } else {
                TimerManager.startTimer()
                binding.btnPause.setImageResource(R.drawable.pause) // 시작하면 일시정지 아이콘 표시
            }
        }

        // 운동 완료 버튼: 다이얼로그 띄우기
        binding.btnFinishWorkout.setOnClickListener {
            showFinishDialog()
        }
    }

    /**
     * TimerManager(전역)의 상태를 현재 액티비티 UI에 연결
     */
    private fun syncTimer() {
        // LiveData 관찰: 초 단위로 변하는 시간을 UI(tvTimer)에 실시간 반영
        TimerManager.timeLiveData.observe(this) {
            binding.tvTimer.text = TimerManager.getFormattedTime()
        }

        // 화면에 다시 들어왔을 때 타이머가 돌고 있는지 확인하여 아이콘 세팅
        if (TimerManager.isRunning()) {
            binding.btnPause.setImageResource(R.drawable.pause)
        } else {
            binding.btnPause.setImageResource(R.drawable.play)
        }
    }

    /**
     * 운동 완료 전 '정말 완료하시겠습니까?' 확인 팝업
     */
    private fun showFinishDialog() {
        val dialog = WorkoutFinishDialog {
            moveToFinishActivity() // 확인 누르면 완료 화면으로 이동
        }
        dialog.show(supportFragmentManager, "WorkoutFinishDialog")
    }

    /**
     * 총 시간, 시작/종료 시간을 가지고 요약 화면으로 이동
     */
    private fun moveToFinishActivity() {
        val endTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val totalTimerTime = binding.tvTimer.text.toString()

        val intent = Intent(this, WorkoutFinishActivity::class.java)
        intent.putExtra("TOTAL_TIME", totalTimerTime)
        intent.putExtra("START_TIME", startTime)
        intent.putExtra("END_TIME", endTime)

        finishResultLauncher.launch(intent)
    }

    /**
     * 개인운동 / PT 탭 클릭 시 리스트 전환 로직
     */
    private fun setupTabLogic() {
        updateTabUI() // 초기 UI 세팅
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

    /**
     * 선택된 탭에 따라 글씨 굵기 및 선택 상태(isSelected) 업데이트
     */
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

    /**
     * 탭 전환 시 어댑터에 새로운 리스트를 연결하고 애니메이션 실행
     */
    private fun switchListData() {
        val currentList = if (isPtMode) ptWorkoutList else personalWorkoutList
        workoutAdapter = RecordWorkoutAdapter(currentList)
        binding.rvWorkoutList.adapter = workoutAdapter
        binding.rvWorkoutList.scheduleLayoutAnimation() // 리스트 전환 시 부드러운 효과
    }

    /**
     * 리사이클러뷰 레이아웃 매니저 및 어댑터 초기화
     */
    private fun setupRecyclerView() {
        workoutAdapter = RecordWorkoutAdapter(personalWorkoutList)
        binding.rvWorkoutList.apply {
            layoutManager = LinearLayoutManager(this@WorkoutExerciseActivity)
            adapter = workoutAdapter
        }
    }

    /**
     * 새로운 운동 아이템을 리스트에 추가 (기본 1세트 포함)
     */
    private fun addNewExercise(name: String) {
        val currentList = if (isPtMode) ptWorkoutList else personalWorkoutList
        val newWorkout = ExerciseRecord(
            id = currentList.size + 1,
            name = name,
            sets = mutableListOf(ExerciseSet(setNumber = 1, weight = 0, reps = 0))
        )
        currentList.add(newWorkout)
        // 리스트 끝에 추가되었음을 알리고 그 위치로 스크롤
        workoutAdapter.notifyItemInserted(currentList.size - 1)
        binding.rvWorkoutList.scrollToPosition(currentList.size - 1)
    }

    override fun onDestroy() {
        super.onDestroy()
        // 타이머를 여기서 stop하지 않으므로, 화면을 나가도 시간은 계속 흐릅니다.
    }
}