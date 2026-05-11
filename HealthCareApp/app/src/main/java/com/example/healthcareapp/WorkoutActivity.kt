package com.example.healthcareapp

import android.graphics.Typeface
import android.os.Bundle
import android.transition.TransitionManager
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

        // 1. UI 컴포넌트 찾아오기 (View Binding 대신 findViewById 사용)
        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)      // 상단 탭 레이아웃
        val viewPager = findViewById<ViewPager2>(R.id.view_pager)    // 탭에 따라 화면을 전환할 뷰페이저2
        val arrowBtn = findViewById<View>(R.id.arrow_btn)            // 상단바 뒤로가기 버튼
        val tvMainTimer = findViewById<TextView>(R.id.tv_main_timer) // 중앙의 큰 운동 시간 타이머
        val tvStartTime = findViewById<TextView>(R.id.tv_start_time) // 운동 시작 시간 텍스트
        val tvEndTime = findViewById<TextView>(R.id.tv_end_time)     // 운동 종료 예정 시간 텍스트
        val conditionname = "컨디션 체크" // 두 번째 탭에 표시될 이름 변수

        // 2. 타이머 데이터 더미 세팅 (실제 구현 시에는 정지/진행 로직 필요)
        tvMainTimer.text = "01:12:32" // 현재 총 운동 시간
        tvStartTime.text = "16:16"    // 시작 시각
        tvEndTime.text = "17:30"      // 종료 시각

        // 3. 뷰페이저 어댑터 연결
        // FragmentStateAdapter를 상속받은 ViewPagerAdapter를 설정하여 탭마다 다른 프래그먼트를 보여줌
        viewPager.adapter = ViewPagerAdapter(this)

        // 4. TabLayout과 ViewPager2를 결합 (TabLayoutMediator)
        // 각 탭의 커스텀 뷰(custom_tab.xml)를 생성하고 초기 스타일을 지정함
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            // custom_tab.xml 레이아웃을 인플레이트하여 TextView로 캐스팅
            val customView = layoutInflater.inflate(R.layout.custom_tab, tabLayout, false) as TextView

            // 포지션에 따른 탭 제목 설정 (0: 운동 기록, 1: 컨디션 체크)
            customView.text = if (position == 0) "운동 기록" else conditionname

            // 초기 선택 상태(포지션 0)에 따른 스타일 차별화
            if (position == 0) {
                customView.setTextColor(ContextCompat.getColor(this, R.color.black))
                customView.setTypeface(null, Typeface.BOLD) // 선택된 탭은 굵게
            } else {
                customView.setTextColor(ContextCompat.getColor(this, R.color.chip_selected))
                customView.setTypeface(null, Typeface.NORMAL) // 선택 안 된 탭은 일반
            }
            // 탭 객체에 커스텀 뷰 적용
            tab.customView = customView
        }.attach() // 설정 완료 후 연결

        // 5. 탭 선택 시 스타일 변경 리스너
        // 사용자가 탭을 눌렀을 때 텍스트 색상과 굵기를 실시간으로 변경해주는 로직
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                // 선택된 탭의 스타일을 검정색 Bold로 변경
                (tab?.customView as? TextView)?.apply {
                    setTextColor(ContextCompat.getColor(this@WorkoutActivity, R.color.black))
                    setTypeface(null, Typeface.BOLD)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // 선택 해제된 탭의 스타일을 흐린 회색(chip_selected 색상) Normal로 변경
                (tab?.customView as? TextView)?.apply {
                    setTextColor(ContextCompat.getColor(this@WorkoutActivity, R.color.chip_selected))
                    setTypeface(null, Typeface.NORMAL)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                }
            }

            // 이미 선택된 탭을 다시 눌렀을 때의 동작 (필요 시 구현)
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // 6. 뒤로가기 화살표 버튼 클릭 시 현재 액티비티 종료
        arrowBtn.setOnClickListener {
            finish()
        }
    }
}