package com.example.healthcareapp

import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.healthcareapp.adapter.ViewPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class WorkoutActivity : AppCompatActivity() {

    // 드롭다운 상태(열림/닫힘)를 추적하기 위한 플래그 변수
    private var isExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 운동 시작/기록 화면 레이아웃 설정
        setContentView(R.layout.exercise_start)

        // 1. UI 컴포넌트 찾아오기
        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
        val viewPager = findViewById<ViewPager2>(R.id.view_pager)
        val arrowBtn = findViewById<View>(R.id.arrow_btn)
        val tvMainTimer = findViewById<TextView>(R.id.tv_main_timer)
        val tvStartTime = findViewById<TextView>(R.id.tv_start_time)
        val tvEndTime = findViewById<TextView>(R.id.tv_end_time)

        // ⭐ 상단 헤더의 날짜와 이모티콘 뷰 (XML ID 일치 여부를 확인하세요)
        val tvHeaderDate = findViewById<TextView>(R.id.tv_date_header)
        val ivHeaderEmoji = findViewById<ImageView>(R.id.condition_emoticon)

        val conditionname = "컨디션 체크"

        // 2. ⭐ 이전 화면(DiaryListFragment)에서 보낸 데이터 받아오기
        val diaryDate = intent.getStringExtra("DIARY_DATE") ?: "날짜 없음"
        val emojiResId = intent.getIntExtra("EMOJI_RES_ID", -1)
        val startTab = intent.getIntExtra("SELECT_TAB", 0)

        // 3. ⭐ 받아온 데이터를 상단 헤더에 적용
        tvHeaderDate.text = diaryDate
        if (emojiResId != -1) {
            ivHeaderEmoji.setImageResource(emojiResId)
            ivHeaderEmoji.visibility = View.VISIBLE
        } else {
            ivHeaderEmoji.visibility = View.GONE
        }

        // 4. 타이머 데이터 더미 세팅
        tvMainTimer.text = "01:12:32"
        tvStartTime.text = "16:16"
        tvEndTime.text = "17:30"

        // 5. 뷰페이저 어댑터 연결
        viewPager.adapter = ViewPagerAdapter(this)

        // 6. TabLayout과 ViewPager2 결합
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            val customView = layoutInflater.inflate(R.layout.custom_tab, tabLayout, false) as TextView
            customView.text = if (position == 0) "운동 기록" else conditionname

            if (position == 0) {
                customView.setTextColor(ContextCompat.getColor(this, R.color.black))
                customView.setTypeface(null, Typeface.BOLD)
            } else {
                customView.setTextColor(ContextCompat.getColor(this, R.color.chip_selected))
                customView.setTypeface(null, Typeface.NORMAL)
            }
            tab.customView = customView
        }.attach()

        // 7. 탭 선택 시 스타일 변경 리스너
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                (tab?.customView as? TextView)?.apply {
                    setTextColor(ContextCompat.getColor(this@WorkoutActivity, R.color.black))
                    setTypeface(null, Typeface.BOLD)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {
                (tab?.customView as? TextView)?.apply {
                    setTextColor(ContextCompat.getColor(this@WorkoutActivity, R.color.chip_selected))
                    setTypeface(null, Typeface.NORMAL)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                }
            }
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // 8. ⭐ 전달받은 포지션으로 탭 즉시 이동 (컨디션 체크 등)
        viewPager.post {
            viewPager.setCurrentItem(startTab, false)
        }

        // 9. 뒤로가기 버튼
        arrowBtn.setOnClickListener {
            finish()
        }
    }
}