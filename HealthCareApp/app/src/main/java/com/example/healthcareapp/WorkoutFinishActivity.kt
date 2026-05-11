package com.example.healthcareapp

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.healthcareapp.adapter.BodyPart
import com.example.healthcareapp.adapter.StatusQuestionAdapter
import com.example.healthcareapp.adapter.BodyPartAdapter
import com.example.healthcareapp.data.StatusQuestion
import com.example.healthcareapp.databinding.StatusQuestionBinding
import com.example.healthcareapp.sheet.PainBottomSheetFragment
import java.util.*

/**
 * 운동 종료 후 요약 정보 표시 및 컨디션/통증 기록 화면
 */
class WorkoutFinishActivity : AppCompatActivity() {

    private lateinit var binding: StatusQuestionBinding
    private lateinit var questionAdapter: StatusQuestionAdapter
    private lateinit var bodyPartAdapter: BodyPartAdapter

    private lateinit var questionList: List<StatusQuestion>

    // 신체 부위 선택을 위한 방향(앞/뒤) + 카테고리별 데이터 맵
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

    private var currentDirection = "FRONT" // 현재 보고 있는 면 (앞/뒤)
    private var currentCategory = "머리/목" // 현재 선택된 카테고리 (칩 그룹)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = StatusQuestionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 초기 데이터 세팅 및 UI 바인딩
        setupTimeInfo()         // 상단 시간 정보 (총 시간, 시작, 종료) 세팅
        setupStatusQuestions()  // 설문 질문 리스트 구성 및 리사이클러뷰 설정
        setupBodyParts()        // 통증 부위 선택 로직 및 리사이클러뷰 설정
        initClickListeners()    // 각종 버튼 클릭 리스너 설정
    }

    private fun initClickListeners() {
        // 1. [통증 태그 영역] 클릭 시 상세 통증 기록을 위한 바텀시트 호출
        binding.layoutPainTagContainer.setOnClickListener {
            val bottomSheet = PainBottomSheetFragment { selectedPainInfo ->
                // 사용자가 바텀시트에서 기록을 마치고 '확인'을 누르면 결과 반영
                binding.tvPainTagContent.text = selectedPainInfo
                binding.tvPainTagContent.setTextColor(Color.parseColor("#3A8DFF")) // 선택 시 파란색 강조
            }
            bottomSheet.show(supportFragmentManager, "PainBottomSheet")
        }

        // 2. [최종 작성 완료] 버튼 클릭: 기록된 모든 데이터를 Intent에 담아 반환
        binding.btnFinishWorkout.setOnClickListener {
            val memo = binding.etFeedbackMemo.text.toString()           // 피드백 메모
            val scores = questionList.map { it.score }                  // 5개 질문의 점수 리스트
            val painRecord = binding.tvPainTagContent.text.toString()   // 통증 태그 내용

            val resultIntent = Intent().apply {
                putExtra("WORKOUT_MEMO", memo)
                putExtra("PAIN_RECORD", painRecord)
                putIntegerArrayListExtra("WORKOUT_SCORES", ArrayList(scores))
            }

            setResult(Activity.RESULT_OK, resultIntent)
            finish() // 액티비티 종료 및 이전 화면으로 결과 전송
        }

        // 3. [사진 추가] 버튼 (갤러리/카메라 연동 로직 필요)
        binding.btnAddPhoto.setOnClickListener {
            // TODO: 이미지 피커 오픈 로직 구현
        }
    }

    /**
     * 이전 화면(WorkoutExerciseActivity)에서 넘겨받은 시간 데이터 표시
     */
    private fun setupTimeInfo() {
        val totalTime = intent.getStringExtra("TOTAL_TIME") ?: "00:00:00"
        val startTime = intent.getStringExtra("START_TIME") ?: "00:00"
        val endTime = intent.getStringExtra("END_TIME") ?: "00:00"

        binding.tvTotalTime.text = totalTime
        binding.tvStartTime.text = "시작\n$startTime"
        binding.tvEndTime.text = "종료\n$endTime"
    }

    /**
     * 5가지 상태 질문(통증, 강도, 어지러움, 기분, 달성도) 데이터 구성
     */
    private fun setupStatusQuestions() {
        questionList = listOf(
            // 각 질문별로 1~10단계에 해당하는 상세 가이드 문구를 매핑함 (사용자 이해를 돕기 위함)
            StatusQuestion(1, "운동 후 평소와 다른 관절이나\n근육 통증이 있었나요?", "매우 심함", "통증 없음", 10,
                mapOf(1 to "1 - 매우 심함", 5 to "5 - 통증이 분명히 느껴짐", 10 to "10 - 통증 없음")),

            StatusQuestion(2, "오늘 운동 강도는 내 몸 상태에\n적절했나요?", "너무 약하거나 무리", "딱 맞았음", 8,
                mapOf(1 to "1 - 너무 약하거나 무리", 8 to "8 - 잘 맞음", 10 to "10 - 딱 맞음")),

            StatusQuestion(3, "운동 후 어지러움이나\n불편감이 있었나요?", "매우 심했음", "전혀 없었음", 10,
                mapOf(1 to "1 - 매우 심했음", 10 to "10 - 전혀 없었음")),

            StatusQuestion(4, "운동 후 전반적인 기분 상태는\n어떤가요?", "매우 안 좋음", "최상 ", 7,
                mapOf(1 to "1 - 매우 안 좋음", 7 to "7 - 괜찮은 편", 10 to "10 - 최상")),

            StatusQuestion(5, "오늘 계획한 운동 목표를\n달성했나요?", "거의 못 함", "계획보다 많이 더 함", 8,
                mapOf(1 to "1 - 거의 못 함", 8 to "8 - 계획한 만큼 함", 10 to "10 - 계획보다 많이 더 함"))
        )

        questionAdapter = StatusQuestionAdapter(questionList)
        binding.rvStatusQuestions.apply {
            layoutManager = LinearLayoutManager(this@WorkoutFinishActivity)
            adapter = questionAdapter
        }
    }

    /**
     * 통증 부위 선택을 위한 탭(앞/뒤), 칩(카테고리), 리스트 설정
     */
    private fun setupBodyParts() {
        // 부위 아이템 클릭 시 바로 통증 텍스트에 반영하는 콜백
        bodyPartAdapter = BodyPartAdapter(mutableListOf()) { part ->
            binding.tvPainTagContent.text = part.name
            binding.tvPainTagContent.setTextColor(Color.parseColor("#3A8DFF"))
        }

        binding.rvBodyParts.apply {
            layoutManager = LinearLayoutManager(this@WorkoutFinishActivity)
            adapter = bodyPartAdapter
        }

        // [앞면] 탭 클릭 시
        binding.btnFront.setOnClickListener {
            currentDirection = "FRONT"
            updateDirectionTabUI(isFront = true)
            updateBodyPartList()
        }

        // [뒷면] 탭 클릭 시
        binding.btnBack.setOnClickListener {
            currentDirection = "BACK"
            updateDirectionTabUI(isFront = false)
            updateBodyPartList()
        }

        // [카테고리 칩] 변경 시 (머리/목, 상체, 하체 등)
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

        updateBodyPartList() // 초기 리스트 로드
    }

    /**
     * 선택된 면(앞/뒤)에 따라 탭 버튼의 배경과 글자색 변경
     */
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

    /**
     * 현재 방향(Direction)과 카테고리(Category) 조합에 맞는 세부 부위 리스트 갱신
     */
    private fun updateBodyPartList() {
        val key = "${currentDirection}_${currentCategory}" // 예: "FRONT_상체"
        val names = bodyPartMap[key] ?: emptyList()
        val items = names.map { BodyPart(it) }
        bodyPartAdapter.updateItems(items)
    }
}