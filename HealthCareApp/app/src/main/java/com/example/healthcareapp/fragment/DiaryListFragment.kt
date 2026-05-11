package com.example.healthcareapp.fragment

import WorkoutFinishDialog
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.R
import com.example.healthcareapp.WorkoutExerciseActivity
import com.example.healthcareapp.WorkoutFinishActivity
import com.example.healthcareapp.adapter.DayAdapter
import com.example.healthcareapp.adapter.DiaryAdapter
import com.example.healthcareapp.data.DayItem
import com.example.healthcareapp.data.DiaryItem
import com.example.healthcareapp.utils.DateUtils
import com.example.healthcareapp.utils.TimerManager
import java.text.SimpleDateFormat
import java.util.*

class DiaryListFragment : Fragment() {

    private lateinit var rvCalendar: RecyclerView
    private lateinit var rvDiaryList: RecyclerView
    private lateinit var tvWeekTitle: TextView
    private lateinit var btnPrevWeek: ImageView
    private lateinit var btnNextWeek: ImageView
    private lateinit var fabStartWorkout: View
    private lateinit var tvFolderName: TextView
    private var tvDiaryTitle: TextView? = null // ⭐ 추가: "공유 일지" 문구 변경용

    private lateinit var layoutFloatingTimer: View
    private lateinit var tvFloatingTimer: TextView
    private lateinit var btnFloatingFinish: Button

    private lateinit var dayAdapter: DayAdapter
    private lateinit var diaryAdapter: DiaryAdapter

    private var currentCalendar = Calendar.getInstance()
    private lateinit var btnFloatingPause: ImageView
    private var currentDaysList = mutableListOf<DayItem>()
    private var selectedDateStr: String = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(Date())

    private val displayList = ArrayList<DiaryItem>()
    private val emojiList = listOf(
        R.drawable.emoticon1, R.drawable.emoticon2, R.drawable.emoticon3,
        R.drawable.emoticon4, R.drawable.emoticon5
    )

    private var isWorkoutCompletedToday = false
    private var folderName: String? = null
    private var isSharedMode = false // ⭐ 추가: 공유 모드 상태 변수

    // 액티비티 결과 런처 (운동 완료 시 처리)
    private val workoutResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            isWorkoutCompletedToday = true
            markTodayWithEmoji()
            TimerManager.stopTimer()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 기존 folder_detial.xml 레이아웃 사용
        return inflater.inflate(R.layout.folder_detial, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 전달받은 데이터 꺼내기
        arguments?.let {
            folderName = it.getString("FOLDER_NAME")
            // ⭐ DiaryMainFragment가 넘겨주는 IS_SHARED_MODE를 받습니다.
            isSharedMode = it.getBoolean("IS_SHARED_MODE", false)
        }

        initViews(view)
        setupModeUI() // ⭐ 추가: 모드에 따른 텍스트 설정
        initAdapters()
        setupCalendar()
        setupDiaryList()
        setupFloatingTimerObserver()
        initClickListeners(view)
    }

    private fun initViews(view: View) {
        rvCalendar = view.findViewById(R.id.rv_calendar)
        rvDiaryList = view.findViewById(R.id.rv_diary_list)
        tvWeekTitle = view.findViewById(R.id.tv_week_title)
        btnPrevWeek = view.findViewById(R.id.btn_prev_week)
        btnNextWeek = view.findViewById(R.id.btn_next_week)
        fabStartWorkout = view.findViewById(R.id.fab_start_workout)
        tvFolderName = view.findViewById(R.id.tv_folder_name)

        // ⭐ 추가: XML의 tv_diary_title(공유 일지 써있던 곳) 연결
        tvDiaryTitle = view.findViewById(R.id.tv_diary_title)

        layoutFloatingTimer = view.findViewById(R.id.layout_floating_timer)
        tvFloatingTimer = layoutFloatingTimer.findViewById(R.id.tv_bar_time)
        btnFloatingFinish = layoutFloatingTimer.findViewById(R.id.btn_bar_finish)
        btnFloatingPause = layoutFloatingTimer.findViewById(R.id.btn_bar_pause)
    }

    // ⭐ 추가: 공유/개인 모드 UI 텍스트 설정 함수
    private fun setupModeUI() {
        // 상단 폴더명 업데이트 (예: 새 폴더 10)
        tvFolderName.text = folderName ?: "일지"

        // 중간 타이틀 업데이트 (공유 일지 vs 나의 일지)
        tvDiaryTitle?.let { textView ->
            if (isSharedMode) {
                textView.text = "공유 일지"
            } else {
                textView.text = "나의 일지"
            }
        }
    }

    private fun initAdapters() {
        dayAdapter = DayAdapter(emptyList()) { clickedItem ->
            selectedDateStr = clickedItem.fullDate
        }

        rvCalendar.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = dayAdapter
            itemAnimator = null
            setHasFixedSize(true)
        }
    }

    private fun setupCalendar() {
        val weekInfo = DateUtils.getWeekInfo(currentCalendar.time)
        tvWeekTitle.text = weekInfo.first

        val rawDays = weekInfo.second
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(Date())

        rawDays.forEach { day ->
            day.isSelected = (day.fullDate == selectedDateStr)
            if (day.fullDate == todayStr && isWorkoutCompletedToday) {
                day.hasExercise = true
                if (day.emojiResId == 0) day.emojiResId = emojiList.random()
            }
        }

        currentDaysList = rawDays.toMutableList()
        dayAdapter.updateData(currentDaysList)
    }

    private fun markTodayWithEmoji() {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(Date())
        currentDaysList.forEach { day ->
            if (day.fullDate == todayStr) {
                day.hasExercise = true
                if (day.emojiResId == 0) day.emojiResId = emojiList.random()
            }
        }
        dayAdapter.notifyDataSetChanged()
    }

    private fun setupFloatingTimerObserver() {
        TimerManager.timeLiveData.observe(viewLifecycleOwner) {
            if (TimerManager.isTimerActive()) {
                layoutFloatingTimer.visibility = View.VISIBLE
                tvFloatingTimer.text = TimerManager.getFormattedTime()
                fabStartWorkout.visibility = View.GONE
            } else {
                layoutFloatingTimer.visibility = View.GONE
                fabStartWorkout.visibility = View.VISIBLE
            }
        }
        btnFloatingFinish.setOnClickListener { showFinishDialog() }
    }

    private fun showFinishDialog() {
        // 1. 현재 타이머 시간과 시작/종료 시간 준비
        val totalTime = TimerManager.getFormattedTime()
        val sdf = SimpleDateFormat("HH:mm", Locale.KOREA)
        val endTime = sdf.format(Date())
        // 시작 시간은 TimerManager 등에 저장된 값을 쓰거나, 임시로 넣습니다.
        val startTime = "16:16"

        // 2. 다이얼로그 띄우기 (WorkoutFinishDialog 내부 콜백 수정)
        val dialog = WorkoutFinishDialog {
            // [확인] 버튼을 눌렀을 때 실행될 로직

            // A. WorkoutFinishActivity로 이동하는 Intent 생성
            val intent = Intent(requireContext(), WorkoutFinishActivity::class.java).apply {
                putExtra("TOTAL_TIME", totalTime)
                putExtra("START_TIME", startTime)
                putExtra("END_TIME", endTime)
                putExtra("IS_SHARED_MODE", isSharedMode)
            }

            // B. 화면 전환 (결과를 받아오기 위해 Launcher 사용)
            workoutResultLauncher.launch(intent)

            // C. (중요) 여기서 바로 TimerManager.stopTimer()를 하지 마세요!
            // WorkoutFinishActivity에서 '운동 완료'를 최종적으로 눌렀을 때
            // workoutResultLauncher 콜백에서 멈추는 것이 데이터 흐름상 안전합니다.
        }
        dialog.show(parentFragmentManager, "WorkoutFinishDialog")
    }

    private fun setupDiaryList() {
        displayList.clear()
        // 여기서는 나중에 isSharedMode에 따라 서버에서 다른 데이터를 받아오도록 수정 가능합니다.
        displayList.add(DiaryItem("26.04.10 qwer123", "개인운동"))
        displayList.add(DiaryItem("26.04.09 asdf456", "PT"))
        displayList.add(DiaryItem("26.04.08 qwer123", "개인운동"))
        displayList.add(DiaryItem("26.04.07 asdf456", "PT"))

        rvDiaryList.layoutManager = LinearLayoutManager(requireContext())
        diaryAdapter = DiaryAdapter(displayList, { item -> }, { position -> })
        rvDiaryList.adapter = diaryAdapter
    }


    private fun initClickListeners(view: View) {
        view.findViewById<ImageView>(R.id.arrow_btn).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnPrevWeek.setOnClickListener { moveWeek(-1) }
        btnNextWeek.setOnClickListener { moveWeek(1) }

        layoutFloatingTimer.setOnClickListener {
            val intent = Intent(requireContext(), WorkoutExerciseActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
        }

        fabStartWorkout.setOnClickListener {
            val intent = Intent(requireContext(), WorkoutExerciseActivity::class.java).apply {
                // 운동 액티비티로 넘어갈 때도 모드 정보를 함께 줍니다.
                putExtra("IS_SHARED_MODE", isSharedMode)
            }
            workoutResultLauncher.launch(intent)
        }

        btnFloatingPause.setOnClickListener {
            if (TimerManager.isRunning()) {
                TimerManager.pauseTimer()
                btnFloatingPause.setImageResource(R.drawable.play)
            } else {
                TimerManager.startTimer()
                btnFloatingPause.setImageResource(R.drawable.pause)
            }
        }
    }

    private fun moveWeek(offset: Int) {
        currentCalendar.add(Calendar.DAY_OF_MONTH, offset * 7)
        setupCalendar()
    }
}