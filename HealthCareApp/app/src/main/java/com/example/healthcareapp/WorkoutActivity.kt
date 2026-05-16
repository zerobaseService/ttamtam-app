package com.example.healthcareapp

import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.healthcareapp.adapter.ViewPagerAdapter
import com.example.healthcareapp.data.ApiResponse
import com.example.healthcareapp.data.JournalDetailResponse
import com.example.healthcareapp.network.RetrofitClient
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WorkoutActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var tvMainTimer: TextView
    private lateinit var tvStartTime: TextView
    private lateinit var tvEndTime: TextView
    private lateinit var tvHeaderDate: TextView
    private lateinit var ivHeaderEmoji: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var layoutError: LinearLayout
    private lateinit var tvErrorMessage: TextView

    private var currentCall: Call<ApiResponse<JournalDetailResponse>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.exercise_start)

        initViews()

        val journalId = intent.getLongExtra("JOURNAL_ID", -1L)
        val emojiResId = intent.getIntExtra("EMOJI_RES_ID", -1)
        ivHeaderEmoji.setImageResource(if (emojiResId != -1) emojiResId else R.drawable.emoticon1)

        findViewById<View>(R.id.arrow_btn).setOnClickListener { finish() }

        if (journalId == -1L) {
            showError("일지 정보를 불러올 수 없습니다.")
            return
        }

        fetchJournalDetail(journalId)
    }

    private fun initViews() {
        tabLayout = findViewById(R.id.tab_layout)
        viewPager = findViewById(R.id.view_pager)
        tvMainTimer = findViewById(R.id.tv_main_timer)
        tvStartTime = findViewById(R.id.tv_start_time)
        tvEndTime = findViewById(R.id.tv_end_time)
        tvHeaderDate = findViewById(R.id.tv_date_header)
        ivHeaderEmoji = findViewById(R.id.condition_emoticon)
        progressBar = findViewById(R.id.progress_bar)
        layoutError = findViewById(R.id.layout_error)
        tvErrorMessage = findViewById(R.id.tv_error_message)
    }

    private fun fetchJournalDetail(journalId: Long) {
        showLoading()
        currentCall = RetrofitClient.journalService.getJournalDetail(journalId)
        currentCall?.enqueue(object : Callback<ApiResponse<JournalDetailResponse>> {
            override fun onResponse(
                call: Call<ApiResponse<JournalDetailResponse>>,
                response: Response<ApiResponse<JournalDetailResponse>>
            ) {
                if (isFinishing) return

                if (!response.isSuccessful) {
                    showError("일지를 불러오지 못했습니다.")
                    return
                }

                val detail = response.body()?.data
                if (detail == null) {
                    showError("일지 데이터가 없습니다.")
                    return
                }

                bindHeader(detail)
                setupViewPager(detail)
                showContent()
            }

            override fun onFailure(call: Call<ApiResponse<JournalDetailResponse>>, t: Throwable) {
                if (isFinishing || call.isCanceled) return
                val message = if (t is java.io.IOException) "네트워크 연결을 확인해주세요." else "오류가 발생했습니다."
                showError(message)
            }
        })
    }

    private fun bindHeader(detail: JournalDetailResponse) {
        tvHeaderDate.text = detail.workoutDate

        tvMainTimer.text = detail.totalDurationSeconds
            ?.let { formatDuration(it) } ?: "--:--:--"

        tvStartTime.text = detail.startedAt?.let { parseHourMinute(it) } ?: "--:--"

        tvEndTime.text = if (detail.startedAt != null && detail.totalDurationSeconds != null) {
            calculateEndTime(detail.startedAt, detail.totalDurationSeconds)
        } else {
            "--:--"
        }
    }

    private fun setupViewPager(detail: JournalDetailResponse) {
        viewPager.adapter = ViewPagerAdapter(this, detail)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            val customView = layoutInflater.inflate(R.layout.custom_tab, tabLayout, false) as TextView
            customView.text = if (position == 0) "운동 기록" else "컨디션 체크"
            tab.customView = customView
        }.attach()

        applyTabStyle(tabLayout.getTabAt(0), selected = true)
        applyTabStyle(tabLayout.getTabAt(1), selected = false)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) = applyTabStyle(tab, selected = true)
            override fun onTabUnselected(tab: TabLayout.Tab?) = applyTabStyle(tab, selected = false)
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        val startTab = intent.getIntExtra("SELECT_TAB", 0)
        viewPager.post { viewPager.setCurrentItem(startTab, false) }
    }

    private fun applyTabStyle(tab: TabLayout.Tab?, selected: Boolean) {
        (tab?.customView as? TextView)?.apply {
            setTextColor(ContextCompat.getColor(this@WorkoutActivity, if (selected) R.color.black else R.color.chip_selected))
            setTypeface(null, if (selected) Typeface.BOLD else Typeface.NORMAL)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        }
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        viewPager.visibility = View.GONE
        layoutError.visibility = View.GONE
    }

    private fun showContent() {
        progressBar.visibility = View.GONE
        viewPager.visibility = View.VISIBLE
        layoutError.visibility = View.GONE
    }

    private fun showError(message: String) {
        progressBar.visibility = View.GONE
        viewPager.visibility = View.GONE
        tvErrorMessage.text = message
        layoutError.visibility = View.VISIBLE
    }

    private fun formatDuration(totalSeconds: Int): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun parseHourMinute(isoDateTime: String): String? = try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = sdf.parse(isoDateTime) ?: return null
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
    } catch (e: Exception) {
        null
    }

    private fun calculateEndTime(startedAt: String, totalDurationSeconds: Int): String = try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val startDate = sdf.parse(startedAt) ?: return "--:--"
        val endDate = Date(startDate.time + totalDurationSeconds * 1000L)
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(endDate)
    } catch (e: Exception) {
        "--:--"
    }

    override fun onDestroy() {
        super.onDestroy()
        currentCall?.cancel()
    }
}
