package com.example.healthcareapp.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.ConditionCheckActivity
import com.example.healthcareapp.HomeActivity
import com.example.healthcareapp.R
import com.example.healthcareapp.WorkoutActivity
import com.example.healthcareapp.WorkoutSessionActivity
import com.example.healthcareapp.adapter.DayAdapter
import com.example.healthcareapp.adapter.DiaryAdapter
import com.example.healthcareapp.data.DiaryItem
import com.example.healthcareapp.data.JournalSummaryResponse
import com.example.healthcareapp.network.RetrofitClient
import com.example.healthcareapp.data.ApiResponse
import com.example.healthcareapp.sheet.AddEntrySheet
import com.example.healthcareapp.utils.DateUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DiaryMainFragment : Fragment() {

    private lateinit var rvCalendar: RecyclerView
    private lateinit var tvWeekTitle: TextView
    private lateinit var btnPrevWeek: ImageView
    private lateinit var btnNextWeek: ImageView

    private lateinit var exerciseStartBtn: AppCompatButton
    private lateinit var conditionCheckBtn: AppCompatButton
    private var fabAddEntry: FloatingActionButton? = null

    private var tvDiaryHeader: TextView? = null
    private var tvFolderTitle: TextView? = null

    private lateinit var dayAdapter: DayAdapter
    private var currentCalendar = Calendar.getInstance()

    private var folderId: Long = -1L
    private var folderName: String? = null
    private var isSharedMode = false

    private lateinit var layoutDiaryContent: ConstraintLayout
    private lateinit var layoutEmptyState: ConstraintLayout
    private lateinit var layoutErrorState: ConstraintLayout
    private lateinit var progressDiaryList: ProgressBar
    private lateinit var rvDiaryList: RecyclerView
    private lateinit var tvErrorMessage: TextView

    private lateinit var diaryAdapter: DiaryAdapter

    private val emojiList = listOf(
        R.drawable.emoticon1, R.drawable.emoticon2, R.drawable.emoticon3,
        R.drawable.emoticon4, R.drawable.emoticon5
    )

    private var currentCall: Call<ApiResponse<List<JournalSummaryResponse>>>? = null
    private var lastSelectedDate: String? = null

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
        return inflater.inflate(R.layout.calendarbar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupCalendar()
        setupModeUI()
        initClickListeners()
    }

    private fun initViews(view: View) {
        rvCalendar = view.findViewById(R.id.rv_calendar)
        tvWeekTitle = view.findViewById(R.id.tv_week_title)
        btnPrevWeek = view.findViewById(R.id.btn_prev_week)
        btnNextWeek = view.findViewById(R.id.btn_next_week)

        conditionCheckBtn = view.findViewById(R.id.condition_check)
        exerciseStartBtn = view.findViewById(R.id.exercise_start)
        fabAddEntry = view.findViewById(R.id.fab_add_entry)

        tvDiaryHeader = view.findViewById(R.id.tv_diary_header)
        tvFolderTitle = view.findViewById(R.id.tv_folder_title)

        layoutDiaryContent = view.findViewById(R.id.layout_diary_content)
        layoutEmptyState = view.findViewById(R.id.layout_empty_state)
        layoutErrorState = view.findViewById(R.id.layout_error_state)
        progressDiaryList = view.findViewById(R.id.progress_diary_list)
        rvDiaryList = view.findViewById(R.id.rv_diary_list)
        tvErrorMessage = view.findViewById(R.id.tv_error_message)

        diaryAdapter = DiaryAdapter(mutableListOf(), { item ->
            val journalId = item.id.toLongOrNull() ?: return@DiaryAdapter
            val intent = Intent(requireContext(), WorkoutActivity::class.java).apply {
                putExtra("JOURNAL_ID", journalId)
            }
            startActivity(intent)
        }, { _ -> })
        rvDiaryList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = diaryAdapter
        }
    }

    private fun setupModeUI() {
        tvDiaryHeader?.text = if (isSharedMode) "공유 일지" else "나의 일지"
        tvFolderTitle?.text = folderName ?: "일지"
    }

    private fun setupCalendar() {
        val (title, days) = DateUtils.getWeekInfo(currentCalendar.time)
        tvWeekTitle.text = title

        if (lastSelectedDate == null) {
            lastSelectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(Date())
        }
        days.find { it.fullDate == lastSelectedDate }?.isSelected = true

        dayAdapter = DayAdapter(days) { clickedDay ->
            updateDiaryList(clickedDay.fullDate)
        }

        rvCalendar.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = dayAdapter
            post { dayAdapter.notifyDataSetChanged() }
        }
    }

    override fun onResume() {
        super.onResume()
        lastSelectedDate?.let { updateDiaryList(it) }
    }

    private fun initClickListeners() {
        view?.let { safeView ->

            safeView.findViewById<ImageView>(R.id.arrow_btn)?.setOnClickListener {
                if (parentFragmentManager.backStackEntryCount > 0) {
                    parentFragmentManager.popBackStack()
                } else {
                    activity?.onBackPressedDispatcher?.onBackPressed()
                }
            }

            btnPrevWeek.setOnClickListener { moveWeek(-1) }
            btnNextWeek.setOnClickListener { moveWeek(1) }

            conditionCheckBtn.setOnClickListener {
                val intent = Intent(requireContext(), ConditionCheckActivity::class.java).apply {
                    putExtra("FOLDER_ID", folderId)
                    putExtra("FOLDER_NAME", folderName)
                    putExtra("IS_SHARED_MODE", isSharedMode)
                    putExtra("SELECT_TAB", 1)
                }
                startActivity(intent)
            }

            exerciseStartBtn.setOnClickListener {
                val intent = Intent(requireContext(), WorkoutSessionActivity::class.java)
                startActivity(intent)
            }

            fabAddEntry?.setOnClickListener {
                AddEntrySheet(
                    onConditionCheckClick = {
                        val intent = Intent(requireContext(), ConditionCheckActivity::class.java).apply {
                            putExtra("FOLDER_ID", folderId)
                            putExtra("FOLDER_NAME", folderName)
                            putExtra("IS_SHARED_MODE", isSharedMode)
                            putExtra("SELECT_TAB", 1)
                        }
                        startActivity(intent)
                    },
                    onExerciseStartClick = {
                        val intent = Intent(requireContext(), WorkoutSessionActivity::class.java)
                        startActivity(intent)
                    }
                ).show(parentFragmentManager, "AddEntrySheet")
            }

            safeView.findViewById<AppCompatButton>(R.id.btn_retry)?.setOnClickListener {
                lastSelectedDate?.let { updateDiaryList(it) }
            }
        }
    }

    private fun moveWeek(offset: Int) {
        currentCalendar.add(Calendar.DAY_OF_MONTH, offset * 7)
        val (title, days) = DateUtils.getWeekInfo(currentCalendar.time)
        tvWeekTitle.text = title
        dayAdapter.updateData(days)
    }

    private fun showState(state: DiaryState) {
        progressDiaryList.visibility = if (state == DiaryState.LOADING) View.VISIBLE else View.GONE
        layoutDiaryContent.visibility = if (state == DiaryState.CONTENT) View.VISIBLE else View.GONE
        layoutEmptyState.visibility = if (state == DiaryState.EMPTY) View.VISIBLE else View.GONE
        layoutErrorState.visibility = if (state == DiaryState.ERROR) View.VISIBLE else View.GONE
    }

    private fun updateDiaryList(date: String) {
        currentCall?.cancel()
        lastSelectedDate = date

        showState(DiaryState.LOADING)

        val actualFolderId = if (folderId == -1L) null else folderId
        val call = if (actualFolderId != null) {
            RetrofitClient.journalService.getJournals(date = date, folderId = actualFolderId)
        } else {
            RetrofitClient.journalService.getJournals(date = date, unfiled = true)
        }
        currentCall = call

        call.enqueue(object : Callback<ApiResponse<List<JournalSummaryResponse>>> {
            override fun onResponse(
                call: Call<ApiResponse<List<JournalSummaryResponse>>>,
                response: Response<ApiResponse<List<JournalSummaryResponse>>>
            ) {
                if (!isAdded) return

                if (!response.isSuccessful) {
                    when (response.code()) {
                        401 -> {
                            Toast.makeText(requireContext(), "로그인이 만료되었습니다. 다시 로그인해주세요.", Toast.LENGTH_SHORT).show()
                            // TODO: 토큰 클리어 + 로그인 화면 이동
                        }
                        403 -> {
                            tvErrorMessage.text = "접근 권한이 없습니다."
                            showState(DiaryState.ERROR)
                        }
                        404 -> showState(DiaryState.EMPTY)
                        else -> {
                            tvErrorMessage.text = "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
                            showState(DiaryState.ERROR)
                        }
                    }
                    return
                }

                val body = response.body()
                if (body == null || !body.success) {
                    tvErrorMessage.text = body?.message ?: "오류가 발생했습니다."
                    showState(DiaryState.ERROR)
                    return
                }

                val data = body.data
                if (data.isNullOrEmpty()) {
                    showState(DiaryState.EMPTY)
                } else {
                    val items = data.map { journal ->
                        DiaryItem(
                            id = journal.journalId.toString(),
                            date = journal.workoutDate,
                            title = journal.workoutType ?: "개인운동",
                            emojiResId = emojiList[(journal.journalId % emojiList.size).toInt()]
                        )
                    }
                    diaryAdapter.updateData(items)
                    showState(DiaryState.CONTENT)
                }
            }

            override fun onFailure(call: Call<ApiResponse<List<JournalSummaryResponse>>>, t: Throwable) {
                if (!isAdded || call.isCanceled) return
                if (t is java.io.IOException) {
                    tvErrorMessage.text = "네트워크 연결을 확인해주세요."
                } else {
                    tvErrorMessage.text = "오류가 발생했습니다."
                }
                showState(DiaryState.ERROR)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        currentCall?.cancel()
    }

    private enum class DiaryState { LOADING, CONTENT, EMPTY, ERROR }
}