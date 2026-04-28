//package com.example.healthcareapp
//
//import androidx.fragment.app.Fragment
//import android.os.Bundle
//import android.widget.LinearLayout
//import androidx.appcompat.app.AppCompatActivity
//
//
//class HomeActivity : AppCompatActivity(){
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.navhome) // 하단 탭바와 컨테이너가 포함된 레이아웃 연결
//
//        // 하단 탭 버튼(LinearLayout)들을 ID로 연결
//        val tabFolder = findViewById<LinearLayout>(R.id.tab_folder)   // 폴더 탭
//        val tabJournal = findViewById<LinearLayout>(R.id.tab_journal) // 일지 탭
//        val tabMy = findViewById<LinearLayout>(R.id.tab_my)           // 마이페이지 탭
//
//        // 앱 처음 실행 시 기본 화면 설정 (저장된 상태가 없을 때만 실행)
//        if (savedInstanceState == null) {
//            // 첫 화면으로 '폴더 메인 프래그먼트'를 띄움
//            replaceFragment(FolderMainFragment())
//            // 폴더 탭을 선택된 상태의 디자인으로 변경
//            updateTabUI(tabFolder)
//        }
//
//
//
//        // 폴더 탭 클릭 시
//        tabFolder.setOnClickListener {
//            replaceFragment(FolderMainFragment()) // 폴더 화면으로 교체
//            updateTabUI(tabFolder)               // 탭 하이라이트 변경
//        }
//
//        // 일지 탭 클릭 시
//        tabJournal.setOnClickListener {
//            // 일지 프레그먼트가 완성되면 여기에 replaceFragment 추가
//            updateTabUI(tabJournal)
//        }
//
//        // 마이페이지 탭 클릭 시
//        tabMy.setOnClickListener {
//            // 나의페이지가 완성되면 여기에 replaceFragment 추가
//            updateTabUI(tabMy)
//        }
//    }
//
//    /**
//     * 프래그먼트 교체 함수
//     * @param fragment 교체할 목적지 프래그먼트 객체
//     */
//    private fun replaceFragment(fragment: androidx.fragment.app.Fragment) {
//        supportFragmentManager.beginTransaction()
//            .replace(R.id.main_container, fragment) // main_container라는 영역을 새 프래그먼트로 채움
//            .commit() // 변경 사항 적용
//    }
//
//
//    private fun updateTabUI(selectedTab: LinearLayout) {
//        // 모든 탭을 리스트로 묶어서 관리
//        val tabs = listOf(
//            findViewById<LinearLayout>(R.id.tab_folder),
//            findViewById<LinearLayout>(R.id.tab_journal),
//            findViewById<LinearLayout>(R.id.tab_my)
//        )
//
//
//        tabs.forEach { tab ->
//            if (tab == selectedTab) {
//                // 선택된 탭 :  tab_selected 배경(테두리나 색상) 적용
//                tab.setBackgroundResource(R.drawable.tab_selected)
//            } else {
//                // 선택되지 않은 탭 :  배경 제거
//                tab.setBackgroundResource(0)
//            }
//        }
//    }
//}