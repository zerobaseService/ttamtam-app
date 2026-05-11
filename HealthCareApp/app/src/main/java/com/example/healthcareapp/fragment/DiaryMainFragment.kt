package com.example.healthcareapp.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.HomeActivity
import com.example.healthcareapp.R
import com.example.healthcareapp.WorkoutActivity
import com.example.healthcareapp.adapter.DayAdapter
import com.example.healthcareapp.utils.DateUtils
import java.util.Calendar

class DiaryMainFragment : Fragment() {

    private lateinit var rvCalendar: RecyclerView
    private lateinit var tvWeekTitle: TextView
    private lateinit var btnPrevWeek: ImageView
    private lateinit var btnNextWeek: ImageView

    // ⭐ XML의 ID인 exercise_start와 condition_check에 맞게 버튼 선언
    private lateinit var exerciseStartBtn: AppCompatButton
    private lateinit var conditionCheckBtn: AppCompatButton

    // ⭐ XML의 ID 구조에 맞게 수정
    private var tvDiaryHeader: TextView? = null  // 중간의 "나의 일지" (ID: tv_diary_header)
    private var tvFolderTitle: TextView? = null  // 상단의 "홍길동" (ID: tv_folder_title)

    private lateinit var dayAdapter: DayAdapter
    private var currentCalendar = Calendar.getInstance()

    private var folderId: Long = -1L
    private var folderName: String? = null
    private var isSharedMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            folderId = it.getLong("FOLDER_ID", -1L)
            folderName = it.getString("FOLDER_NAME")
            isSharedMode = it.getBoolean("IS_SHARED_MODE", false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // ⭐ DiaryMainFragment가 참조하는 레이아웃
        val view = inflater.inflate(R.layout.calendarbar, container, false)

        initViews(view)
        setupCalendar()
        setupModeUI()
        initClickListeners()

        return view
    }

    private fun initViews(view: View) {
        rvCalendar = view.findViewById(R.id.rv_calendar)
        tvWeekTitle = view.findViewById(R.id.tv_week_title)
        btnPrevWeek = view.findViewById(R.id.btn_prev_week)
        btnNextWeek = view.findViewById(R.id.btn_next_week)

        // ⭐ 보내주신 XML의 실제 ID와 매칭 (AppCompatButton 타입)
        conditionCheckBtn = view.findViewById(R.id.condition_check)
        exerciseStartBtn = view.findViewById(R.id.exercise_start)

        // ⭐ 문구를 바꿀 헤더와 폴더 타이틀 ID 매칭
        tvDiaryHeader = view.findViewById(R.id.tv_diary_header)
        tvFolderTitle = view.findViewById(R.id.tv_folder_title)
    }

    private fun setupModeUI() {
        // 1. 중간 타이틀 변경 (공유 모드 여부에 따라)
        tvDiaryHeader?.text = if (isSharedMode) "공유 일지" else "나의 일지"

        // 2. 상단 폴더 이름 설정
        tvFolderTitle?.text = folderName ?: "일지"
    }

    private fun setupCalendar() {
        val (title, days) = DateUtils.getWeekInfo(currentCalendar.time)
        tvWeekTitle.text = title

        dayAdapter = DayAdapter(days) { clickedDay ->
            updateDiaryList(clickedDay.fullDate)
        }

        rvCalendar.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = dayAdapter
            post { dayAdapter.notifyDataSetChanged() }
        }
    }

    private fun initClickListeners() {
        btnPrevWeek.setOnClickListener { moveWeek(-1) }
        btnNextWeek.setOnClickListener { moveWeek(1) }

        // 컨디션 체크 버튼 클릭 시
        conditionCheckBtn.setOnClickListener {
            val intent = Intent(requireContext(), WorkoutActivity::class.java).apply {
                putExtra("FOLDER_ID", folderId)
                putExtra("FOLDER_NAME", folderName)
                putExtra("IS_SHARED_MODE", isSharedMode)
            }
            startActivity(intent)
        }

        // 운동 시작 버튼 클릭 시 (DiaryListFragment로 이동하는 HomeActivity 함수 호출)
        exerciseStartBtn.setOnClickListener {
            (activity as? HomeActivity)?.moveToDiaryList(folderId, folderName, isSharedMode)
        }
    }

    private fun moveWeek(offset: Int) {
        currentCalendar.add(Calendar.DAY_OF_MONTH, offset * 7)
        val (title, days) = DateUtils.getWeekInfo(currentCalendar.time)
        tvWeekTitle.text = title
        dayAdapter.updateData(days)
    }

    private fun updateDiaryList(date: String) {
        // 선택된 날짜에 따른 일지 목록 업데이트 로직
    }
}