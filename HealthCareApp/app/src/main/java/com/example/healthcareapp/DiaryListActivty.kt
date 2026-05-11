package com.example.healthcareapp

import WorkoutFinishDialog
import android.app.Activity
import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.adapter.DayAdapter
import com.example.healthcareapp.adapter.DiaryAdapter
import com.example.healthcareapp.data.DayItem
import com.example.healthcareapp.data.DiaryItem
import com.example.healthcareapp.utils.DateUtils
import com.example.healthcareapp.utils.TimerManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

class DiaryListActivity : AppCompatActivity() {

    private lateinit var rvCalendar: RecyclerView
    private lateinit var rvDiaryList: RecyclerView
    private lateinit var tvWeekTitle: TextView
    private lateinit var btnPrevWeek: ImageView
    private lateinit var btnNextWeek: ImageView
    private lateinit var fabStartWorkout: View

    private lateinit var layoutFloatingTimer: View
    private lateinit var tvFloatingTimer: TextView
    private lateinit var btnFloatingFinish: Button

    private lateinit var dayAdapter: DayAdapter
    private lateinit var diaryAdapter: DiaryAdapter

    private var currentCalendar = Calendar.getInstance()
    private var currentDaysList = mutableListOf<DayItem>()

    // ⭐ 현재 선택된 날짜 문자열을 저장 (뷰 갱신 시 상태 유지용)
    private var selectedDateStr: String = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(Date())

    private val displayList = ArrayList<DiaryItem>()

    private val emojiList = listOf(
        R.drawable.emoticon1, R.drawable.emoticon2, R.drawable.emoticon3,
        R.drawable.emoticon4, R.drawable.emoticon5
    )

    private var isWorkoutCompletedToday = false

    private val workoutResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            isWorkoutCompletedToday = true
            markTodayWithEmoji()
            TimerManager.stopTimer()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.folder_detial)

        initViews()
        initAdapters()
        setupCalendar()
        setupDiaryList()
        setupFloatingTimerObserver()
        initClickListeners()
    }

    private fun initViews() {
        rvCalendar = findViewById(R.id.rv_calendar)
        rvDiaryList = findViewById(R.id.rv_diary_list)
        tvWeekTitle = findViewById(R.id.tv_week_title)
        btnPrevWeek = findViewById(R.id.btn_prev_week)
        btnNextWeek = findViewById(R.id.btn_next_week)
        fabStartWorkout = findViewById(R.id.fab_start_workout)

        layoutFloatingTimer = findViewById(R.id.layout_floating_timer)
        tvFloatingTimer = layoutFloatingTimer.findViewById(R.id.tv_bar_time)
        btnFloatingFinish = layoutFloatingTimer.findViewById(R.id.btn_bar_finish)
    }

    private fun initAdapters() {
        // 어댑터 초기화 (클릭 시 selectedDateStr 업데이트)
        dayAdapter = DayAdapter(emptyList()) { clickedItem ->
            selectedDateStr = clickedItem.fullDate
            // 필요한 경우 여기서 해당 날짜의 일지 필터링 수행
        }

        rvCalendar.apply {
            layoutManager = LinearLayoutManager(this@DiaryListActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = dayAdapter

            // ⭐ 버그 해결 핵심: 애니메이터를 끄고 크기를 고정함
            itemAnimator = null
            setHasFixedSize(true)
        }
    }

    private fun setupCalendar() {
        // DateUtils에서 주간 정보를 가져올 때 선택된 날짜 정보도 함께 활용
        val weekInfo = DateUtils.getWeekInfo(currentCalendar.time)
        tvWeekTitle.text = weekInfo.first

        // 데이터 리스트를 새로 받아오면서 선택 상태와 운동 기록을 주입
        val rawDays = weekInfo.second
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(Date())

        rawDays.forEach { day ->
            // 1. 선택 상태 복구
            day.isSelected = (day.fullDate == selectedDateStr)

            // 2. 오늘 운동 완료 상태 복구
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
        TimerManager.timeLiveData.observe(this) {
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
        val dialog = WorkoutFinishDialog {
            isWorkoutCompletedToday = true
            markTodayWithEmoji()
            TimerManager.stopTimer()
        }
        dialog.show(supportFragmentManager, "WorkoutFinishDialog")
    }

    private fun setupDiaryList() {
        // 1. 이미지와 유사한 더미 데이터 생성
        displayList.clear()
        displayList.add(DiaryItem("26.04.10 qwer123", "개인운동"))
        displayList.add(DiaryItem("26.04.09 asdf456", "PT"))
        displayList.add(DiaryItem("26.04.08 qwer123", "개인운동"))
        displayList.add(DiaryItem("26.04.07 asdf456", "PT"))

        // 2. 어댑터 설정
        rvDiaryList.layoutManager = LinearLayoutManager(this)
        diaryAdapter = DiaryAdapter(displayList, { item ->
            // 아이템 클릭 시 상세 화면 이동 등
        }, { position ->
            // 도트(더보기) 클릭 시 메뉴 띄우기 등
        })
        rvDiaryList.adapter = diaryAdapter
    }
    private fun initClickListeners() {
        findViewById<ImageView>(R.id.arrow_btn).setOnClickListener { finish() }
        btnPrevWeek.setOnClickListener { moveWeek(-1) }
        btnNextWeek.setOnClickListener { moveWeek(1) }

        layoutFloatingTimer.setOnClickListener {
            val intent = Intent(this, WorkoutExerciseActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
        }

        fabStartWorkout.setOnClickListener {
            val intent = Intent(this, WorkoutExerciseActivity::class.java)
            workoutResultLauncher.launch(intent)
        }
    }

    private fun moveWeek(offset: Int) {
        currentCalendar.add(Calendar.DAY_OF_MONTH, offset * 7)
        setupCalendar()
    }
}