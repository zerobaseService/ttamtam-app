package com.example.healthcareapp

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.healthcareapp.fragment.DiaryMainFragment
import com.example.healthcareapp.fragment.DiaryListFragment
import com.example.healthcareapp.fragment.FolderMainFragment
import com.example.healthcareapp.fragment.MyPageFragment

class HomeActivity : AppCompatActivity() {

    private lateinit var tabFolder: LinearLayout
    private lateinit var tabJournal: LinearLayout
    private lateinit var tabMy: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.navhome)

        // 1. 뷰 초기화
        tabFolder = findViewById(R.id.tab_folder)
        tabJournal = findViewById(R.id.tab_journal)
        tabMy = findViewById(R.id.tab_my)

        // 2. 초기 화면 설정
        if (savedInstanceState == null) {
            replaceFragment(FolderMainFragment())
            updateTabUI(tabFolder)
        }

        // 3. 탭 클릭 리스너
        tabFolder.setOnClickListener {
            replaceFragment(FolderMainFragment())
            updateTabUI(tabFolder)
        }

        tabJournal.setOnClickListener {
            replaceFragment(DiaryMainFragment())
            updateTabUI(tabJournal)
        }

        tabMy.setOnClickListener {
            replaceFragment(MyPageFragment())
            updateTabUI(tabMy)
        }
    }

    /**
     * [핵심 수정] FolderAdapter에서 호출하는 함수 이름을 일치시켰습니다.
     * 폴더를 클릭하면 해당 폴더 정보를 가지고 일지 메인 화면으로 이동합니다.
     */
    // HomeActivity.kt 내부
    // HomeActivity.kt
    // HomeActivity.kt
    /**
     * 폴더를 클릭하면 해당 폴더 정보를 가지고 일지 메인 화면으로 이동합니다.
     */
    fun moveToJournalTab(folderId: Long, folderName: String, isSharedMode: Boolean) {
        val journalFragment = DiaryMainFragment().apply {
            arguments = Bundle().apply {
                putLong("FOLDER_ID", folderId)
                putString("FOLDER_NAME", folderName)
                putBoolean("IS_SHARED_MODE", isSharedMode)
            }
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.main_container, journalFragment)
            .commit()

        // ⭐ [추가] 네비게이션 바의 상태를 '일지' 탭으로 변경합니다.
        updateTabUI(tabJournal)
    }


    fun moveToDiaryList(folderId: Long, folderName: String?, isSharedMode: Boolean) {
        val listFragment = DiaryListFragment().apply {
            arguments = Bundle().apply {
                putLong("FOLDER_ID", folderId)
                putString("FOLDER_NAME", folderName)
                // ⭐ [수정] 이 한 줄이 빠져서 리스트 화면에서 공유 모드 인식을 못 했습니다.
                putBoolean("IS_SHARED_MODE", isSharedMode)
            }
        }


        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.main_container, listFragment)
            .addToBackStack(null)
            .commit()

        updateTabUI(tabJournal)
    }

    private fun replaceFragment(fragment: Fragment) {

        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.main_container, fragment)
            .commit()
    }

    private fun updateTabUI(selectedTab: LinearLayout) {
        val tabs = listOf(tabFolder, tabJournal, tabMy)

        tabs.forEach { tab ->
            val isSelected = (tab == selectedTab)
            tab.setBackgroundResource(if (isSelected) R.drawable.tab_selected else 0)

            // 탭 내부의 아이콘과 텍스트를 찾아서 색상 변경
            val icon = tab.getChildAt(0) as? ImageView
            val text = tab.getChildAt(1) as? TextView

            icon?.let {
                val color = if (isSelected) Color.BLACK else Color.parseColor("#AAAAAA")
                it.imageTintList = ColorStateList.valueOf(color)
            }

            text?.let {
                it.setTextColor(if (isSelected) Color.BLACK else Color.parseColor("#AAAAAA"))
            }
        }
    }
}