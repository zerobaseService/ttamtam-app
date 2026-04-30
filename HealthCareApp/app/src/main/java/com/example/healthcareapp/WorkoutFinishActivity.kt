package com.example.healthcareapp

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.healthcareapp.adapter.BodyPart
import com.example.healthcareapp.adapter.StatusQuestionAdapter
import com.example.healthcareapp.adapter.BodyPartAdapter // 통증 부위용 어댑터

import com.example.healthcareapp.data.StatusQuestion
import com.example.healthcareapp.databinding.StatusQuestionBinding
import java.util.*

class WorkoutFinishActivity : AppCompatActivity() {

    private lateinit var binding: StatusQuestionBinding
    private lateinit var questionAdapter: StatusQuestionAdapter
    private lateinit var bodyPartAdapter: BodyPartAdapter

    private lateinit var questionList: List<StatusQuestion>

    // 통증 부위 데이터 맵 정의
    private val bodyPartMap = mapOf(
        "FRONT_머리/목" to listOf("머리", "이마", "얼굴", "목"),
        "FRONT_상체" to listOf("어깨", "가슴", "윗배", "아랫배", "옆구리"),
        "FRONT_팔/손" to listOf("윗팔", "팔꿈치", "아랫팔", "손목", "손바닥", "손가락"),
        "FRONT_하체" to listOf("고관절", "사타구니", "생식기", "허벅지", "무릎", "정강이"),
        "FRONT_발" to listOf("발목", "발등", "발가락"),

        "BACK_머리/목" to listOf("경추 (목뼈 부위)"),
        "BACK_상체" to listOf("등", "어깨", "날개(견갑골)", "허리", "꼬리뼈"),
        "BACK_팔/손" to listOf("윗팔", "팔꿈치", "아랫팔", "손목", "손바닥", "손가락"),
        "BACK_하체" to listOf("엉덩이", "뒷허벅지", "오금", "종아리"),
        "BACK_발" to listOf("아킬레스건", "발바닥")
    )

    private var currentDirection = "FRONT" // 현재 선택된 방향 (FRONT / BACK)
    private var currentCategory = "머리/목" // 현재 선택된 카테고리

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = StatusQuestionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnFinishWorkout.setOnClickListener {
            // 메모 텍스트 가져오기
            val memo = binding.etFeedbackMemo.text.toString()

            // 선택된 통증 부위 정보 (선택된 데이터가 있다면)
            // val selectedBodyParts = bodyPartAdapter.getSelectedItems()

            // 상태 질문 점수들
            val scores = questionList.map { it.score }

            android.util.Log.d("WorkoutResult", "메모: $memo")
            android.util.Log.d("WorkoutResult", "점수들: $scores")

            // TODO: 서버 저장 또는 DB 저장 로직 수행 후 종료
            finish()
        }

// 사진 추가 버튼 클릭 시 (갤러리/카메라 호출 로직은 추후 구현 가능)
        binding.btnAddPhoto.setOnClickListener {
            // 갤러리 열기 코드 등
        }
        // 상단 시간 데이터 반영
        setupTimeInfo()

        // 상태 질문 리스트 초기화 및 설정
        setupStatusQuestions()

        // 통증 부위 설정 (어댑터 및 탭 로직)
        setupBodyParts()

        // 완료 버튼 클릭 시
//        binding.btnFinishWorkout.setOnClickListener {
//            val memo = binding.etFeedbackMemo.text.toString()
//            val scores = questionList.map { it.score }
//            android.util.Log.d("WorkoutResult", "최종 점수들: $scores, 메모: $memo")
//            finish()
//        }
    }

    private fun setupTimeInfo() {
        val totalTime = intent.getStringExtra("TOTAL_TIME") ?: "00:00:00"
        val startTime = intent.getStringExtra("START_TIME") ?: "00:00"
        val endTime = intent.getStringExtra("END_TIME") ?: "00:00"

        binding.tvTotalTime.text = totalTime
        binding.tvStartTime.text = "시작\n$startTime"
        binding.tvEndTime.text = "종료\n$endTime"
    }

    private fun setupStatusQuestions() {
        questionList = listOf(
            StatusQuestion(1, "운동 후 평소와 다른 관절/근육\n통증이 있었나요?", "매우 심함", "통증 없음", 1,
                mapOf(1 to "1 - 통증 없음", 5 to "5 - 약간의 뻐근함", 10 to "10 - 심한 통증")),
            StatusQuestion(2, "오늘 운동 강도는 내 몸 상태에 적절했나요?", "너무 약함", "딱 맞았음", 5,
                mapOf(1 to "1 - 너무 약하거나 무리", 10 to "10 - 아주 적절함")),
            StatusQuestion(3, "운동 후 어지러움이나 불편감이 있었나요?", "매우 심했음", "전혀 없었음", 10,
                mapOf(1 to "1 - 매우 심했음", 10 to "10 - 전혀 없었음")),
            StatusQuestion(4, "운동 후 전반적인 기분 상태는 어떤가요?", "매우 안 좋음", "최상", 5,
                mapOf(1 to "1 - 지치고 힘듦", 10 to "10 - 매우 상쾌함")),
            StatusQuestion(5, "오늘 계획한 운동 목표를 달성했나요?", "거의 못 함", "초과 달성", 5,
                mapOf(1 to "1 - 거의 못 함", 10 to "10 - 계획보다 많이 함"))
        )

        questionAdapter = StatusQuestionAdapter(questionList)
        binding.rvStatusQuestions.apply {
            layoutManager = LinearLayoutManager(this@WorkoutFinishActivity)
            adapter = questionAdapter
        }
    }

    private fun setupBodyParts() {
        // 어댑터 초기화 (빈 리스트로 시작)
        bodyPartAdapter = BodyPartAdapter(mutableListOf()) { part ->
            // 아이템 클릭 시 처리
            android.util.Log.d("BodyPart", "선택된 부위: ${part.name}")
        }

        binding.rvBodyParts.apply {
            layoutManager = LinearLayoutManager(this@WorkoutFinishActivity)
            adapter = bodyPartAdapter
        }

        // 앞면/뒷면 전환 버튼 설정
        binding.btnFront.setOnClickListener {
            currentDirection = "FRONT"
            updateDirectionTabUI(isFront = true)
            updateBodyPartList()
        }

        binding.btnBack.setOnClickListener {
            currentDirection = "BACK"
            updateDirectionTabUI(isFront = false)
            updateBodyPartList()
        }

        // 카테고리 칩 선택 설정
        binding.chipGroupBody.setOnCheckedStateChangeListener { _, checkedIds ->
            val checkedId = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            currentCategory = when (checkedId) {
                R.id.chip_head -> "머리/목"
                R.id.chip_upper -> "상체"
                R.id.chip_arm -> "팔/손"
                R.id.chip_lower -> "하체"
                R.id.chip_foot -> "발"
                else -> "머리/목"
            }
            updateBodyPartList()
        }

        // 초기 리스트 로드
        updateBodyPartList()
    }

    private fun updateDirectionTabUI(isFront: Boolean) {
        if (isFront) {
            binding.btnFront.setBackgroundResource(R.drawable.bg_tab_selected)
            binding.btnFront.setTextColor(Color.parseColor("#3A8DFF"))
            binding.btnBack.setBackgroundResource(android.R.color.transparent)
            binding.btnBack.setTextColor(Color.parseColor("#94A3B8"))
        } else {
            binding.btnBack.setBackgroundResource(R.drawable.bg_tab_selected)
            binding.btnBack.setTextColor(Color.parseColor("#3A8DFF"))
            binding.btnFront.setBackgroundResource(android.R.color.transparent)
            binding.btnFront.setTextColor(Color.parseColor("#94A3B8"))
        }
    }

    private fun updateBodyPartList() {
        val key = "${currentDirection}_${currentCategory}"
        val names = bodyPartMap[key] ?: emptyList()
        val items = names.map { BodyPart(it) }
        bodyPartAdapter.updateItems(items)
    }
}