package com.example.healthcareapp.fragment

import WorkoutFinishDialog
import android.app.Activity
import android.content.Intent
import android.graphics.Color
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
import com.example.healthcareapp.WorkoutActivity
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

/**
 * 일지 리스트 프래그먼트: 주간 캘린더와 운동 일지 목록을 표시하며, 필터링 기능을 제공함
 */
class DiaryListFragment : Fragment() {

    private lateinit var rvCalendar: RecyclerView
    private lateinit var rvDiaryList: RecyclerView
    private lateinit var tvWeekTitle: TextView
    private lateinit var btnPrevWeek: ImageView
    private lateinit var btnNextWeek: ImageView
    private lateinit var fabStartWorkout: View
    private lateinit var tvFolderName: TextView
    private var tvDiaryTitle: TextView? = null

    private lateinit var layoutFloatingTimer: View
    private lateinit var tvFloatingTimer: TextView
    private lateinit var btnFloatingFinish: Button
    private lateinit var btnFloatingPause: ImageView

    // 현재 적용된 필터 상태 (기본값: 최근순)
    private var currentFilterType = "최근순"

    private lateinit var dayAdapter: DayAdapter
    private lateinit var diaryAdapter: DiaryAdapter

    private var currentCalendar = Calendar.getInstance()
    private var currentDaysList = mutableListOf<DayItem>()
    private var selectedDateStr: String = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(Date())

    // 필터링을 위한 원본 데이터 리스트
    private val masterDiaryList = mutableListOf<DiaryItem>()
    private val displayList = ArrayList<DiaryItem>()

    private val emojiList = listOf(
        R.drawable.emoticon1, R.drawable.emoticon2, R.drawable.emoticon3,
        R.drawable.emoticon4, R.drawable.emoticon5
    )

    private var isWorkoutCompletedToday = false
    private var folderName: String? = null
    private var isSharedMode = false

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
        return inflater.inflate(R.layout.folder_detial, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            folderName = it.getString("FOLDER_NAME")
            isSharedMode = it.getBoolean("IS_SHARED_MODE", false)
        }

        initViews(view)
        setupModeUI()
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
        tvDiaryTitle = view.findViewById(R.id.tv_diary_title)

        layoutFloatingTimer = view.findViewById(R.id.layout_floating_timer)
        tvFloatingTimer = layoutFloatingTimer.findViewById(R.id.tv_bar_time)
        btnFloatingFinish = layoutFloatingTimer.findViewById(R.id.btn_bar_finish)
        btnFloatingPause = view.findViewById(R.id.btn_bar_pause)
    }

    private fun setupModeUI() {
        tvFolderName.text = folderName ?: "일지"
        tvDiaryTitle?.text = if (isSharedMode) "공유 일지" else "나의 일지"
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

        diaryAdapter = DiaryAdapter(displayList, { item ->
            // 일지 클릭 시 WorkoutActivity로 이동
            val intent = Intent(requireContext(), WorkoutActivity::class.java).apply {
                // "컨디션 체크" 탭을 먼저 보여주기 위한 데이터 (기존 유지)
                putExtra("SELECT_TAB", 1)

                // ⭐ [핵심 추가] 클릭한 일지의 날짜와 이모티콘 데이터 전달
                putExtra("DIARY_DATE", item.date) // 예: "26.04.10"
                putExtra("EMOJI_RES_ID", item.emojiResId) // 예: R.drawable.emoticon5
                putExtra("DIARY_ID", item.id) // 필요 시 일지 ID 전달
            }
            startActivity(intent)
        }, { position ->
            // 삭제 로직 등 (기존 유지)
        })

        rvDiaryList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = diaryAdapter
        }
    }
    /**
     * ⭐ 필터링 및 정렬 실행 함수
     */
    private fun applyFilterAndSort(filterType: String) {
        currentFilterType = filterType

        // 1. UI 상태 업데이트 (텍스트 색상 및 체크박스 이미지)
        updateFilterUI(filterType)

        // 2. 데이터 필터링
        var filteredList = masterDiaryList.toList()
        if (filterType == "운동한 날만") {
            // DiaryItem의 'title' 속성에 "운동 안한날"이 포함되지 않은 것만 필터링
            filteredList = filteredList.filter { !it.title.contains("운동 안한날") }
        }

        // 3. 정렬 로직
        filteredList = when (filterType) {
            "최근순", "운동한 날만" -> filteredList.sortedByDescending { it.date }
            "오래된순" -> filteredList.sortedBy { it.date }
            else -> filteredList
        }

        displayList.clear()
        displayList.addAll(filteredList)
        diaryAdapter.notifyDataSetChanged()
    }

    private fun setupDiaryList() {
        masterDiaryList.clear()
        masterDiaryList.add(DiaryItem("123123", "2026-04-12", "개인 운동",R.drawable.emoticon1))
        masterDiaryList.add(DiaryItem("321321", "2026-05-11", "PT",R.drawable.emoticon2))
        masterDiaryList.add(DiaryItem("456456", "2026-04-08", "개인 운동",R.drawable.emoticon3))
        masterDiaryList.add(DiaryItem("654654", "2026-05-09", "PT",R.drawable.emoticon4))
        masterDiaryList.add(DiaryItem("765765", "2026-05-11", "운동 안한날",R.drawable.emoticon5))

        applyFilterAndSort("최근순")
    }

    private fun initClickListeners(view: View) {
        view.findViewById<ImageView>(R.id.arrow_btn).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnPrevWeek.setOnClickListener { moveWeek(-1) }
        btnNextWeek.setOnClickListener { moveWeek(1) }

        // [정렬] 최신순 클릭
        view.findViewById<TextView>(R.id.tv_sort_latest)?.setOnClickListener {
            applyFilterAndSort("최근순")
        }

        // [정렬] 오래된순 클릭
        view.findViewById<TextView>(R.id.tv_sort_oldest)?.setOnClickListener {
            applyFilterAndSort("오래된순")
        }

        // ⭐ [필터] 체크박스 이미지 클릭 리스너 (토글 방식)
        view.findViewById<View>(R.id.cb_only_workout)?.setOnClickListener {
            if (currentFilterType == "운동한 날만") {
                applyFilterAndSort("최근순")
            } else {
                applyFilterAndSort("운동한 날만")
            }
        }

        fabStartWorkout.setOnClickListener {
            val intent = Intent(requireContext(), WorkoutExerciseActivity::class.java).apply {
                putExtra("IS_SHARED_MODE", isSharedMode)
            }
            workoutResultLauncher.launch(intent)
        }

        layoutFloatingTimer.setOnClickListener {
            val intent = Intent(requireContext(), WorkoutExerciseActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
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

    /**
     * ⭐ 필터 상태에 따라 UI 시각적 피드백 제공 (색상 및 이미지 변경)
     */
    private fun updateFilterUI(filterType: String) {
        val v = view ?: return

        val tvLatest = v.findViewById<View>(R.id.tv_sort_latest) as? TextView
        val tvOldest = v.findViewById<View>(R.id.tv_sort_oldest) as? TextView
        // val tvWorkoutOnly = v.findViewById<View>(R.id.exercise_filter) as? TextView // 색상 고정을 위해 사용 안 함
        val ivCheck = v.findViewById<View>(R.id.cb_only_workout) as? ImageView

        val colorActive = Color.parseColor("#000000") // 활성: 검정
        val colorInactive = Color.parseColor("#94A3B8") // 비활성: 회색

        // 1. 최신순/오래된순 텍스트 색상 업데이트 (이 기능은 유지)
        tvLatest?.setTextColor(if (filterType == "최근순") colorActive else colorInactive)
        tvOldest?.setTextColor(if (filterType == "오래된순") colorActive else colorInactive)

        // 2. 운동한 날만 필터: 텍스트 색상은 건드리지 않고 '체크박스 이미지'만 교체
        if (filterType == "운동한 날만") {
            ivCheck?.setImageResource(R.drawable.checkbox3) // 선택됨 이미지
        } else {
            ivCheck?.setImageResource(R.drawable.checkbox1) // 해제됨 이미지
        }
    }
    // --- 캘린더 및 타이머 로직 ---

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
        val totalTime = TimerManager.getFormattedTime()
        val endTime = SimpleDateFormat("HH:mm", Locale.KOREA).format(Date())
        val startTime = "16:16"

        val dialog = WorkoutFinishDialog {
            val intent = Intent(requireContext(), WorkoutFinishActivity::class.java).apply {
                putExtra("TOTAL_TIME", totalTime)
                putExtra("START_TIME", startTime)
                putExtra("END_TIME", endTime)
                putExtra("IS_SHARED_MODE", isSharedMode)
            }
            workoutResultLauncher.launch(intent)
        }
        dialog.show(parentFragmentManager, "WorkoutFinishDialog")
    }

    private fun moveWeek(offset: Int) {
        currentCalendar.add(Calendar.DAY_OF_MONTH, offset * 7)
        setupCalendar()
    }
}