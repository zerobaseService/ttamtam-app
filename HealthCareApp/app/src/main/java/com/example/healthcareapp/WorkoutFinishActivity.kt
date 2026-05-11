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
import com.example.healthcareapp.databinding.StatusQuestionBinding // XML 파일명이 status_question.xml인 경우
import com.example.healthcareapp.sheet.PainBottomSheetFragment
import java.util.*

class WorkoutFinishActivity : AppCompatActivity() {

    private lateinit var binding: StatusQuestionBinding
    private lateinit var questionAdapter: StatusQuestionAdapter
    private lateinit var bodyPartAdapter: BodyPartAdapter

    private lateinit var questionList: List<StatusQuestion>

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

    private var currentDirection = "FRONT"
    private var currentCategory = "머리/목"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = StatusQuestionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 초기 설정 함수들 호출
        setupTimeInfo()
        setupStatusQuestions()
        setupBodyParts()
        initClickListeners()
    }

    private fun initClickListeners() {
        // 1. 통증 태그 영역 클릭 시 바텀시트 호출
        binding.layoutPainTagContainer.setOnClickListener {
            val bottomSheet = PainBottomSheetFragment { selectedPainInfo ->
                // 바텀시트 완료 클릭 시 텍스트 변경
                binding.tvPainTagContent.text = selectedPainInfo
                binding.tvPainTagContent.setTextColor(Color.parseColor("#3A8DFF")) // 선택 시 파란색으로 변경
            }
            bottomSheet.show(supportFragmentManager, "PainBottomSheet")
        }

        // 2. 운동 완료 버튼 클릭
        binding.btnFinishWorkout.setOnClickListener {
            val memo = binding.etFeedbackMemo.text.toString()
            val scores = questionList.map { it.score }
            val painRecord = binding.tvPainTagContent.text.toString()

            val resultIntent = Intent().apply {
                putExtra("WORKOUT_MEMO", memo)
                putExtra("PAIN_RECORD", painRecord)
                putIntegerArrayListExtra("WORKOUT_SCORES", ArrayList(scores))
            }

            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        // 3. 사진 추가 버튼
        binding.btnAddPhoto.setOnClickListener {
            // 사진 추가 로직
        }

        // 4. 닫기 버튼
        // binding.btnClose가 있다면 추가 (현재 XML에는 btnClose 혹은 상단 X 버튼이 있을 것임)
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
            // 1번 질문: 통증
            StatusQuestion(1, "운동 후 평소와 다른 관절이나\n근육 통증이 있었나요?", "매우 심함", "통증 없음", 10,
                mapOf(
                    1 to "1 - 매우 심함 / 일상 움직임도 힘들 정도",
                    2 to "2 - 통증이 많이 심한 편임",
                    3 to "3 - 통증이 뚜렷하고 불편함이 큼",
                    4 to "4 - 통증이 꽤 느껴지고 거슬림",
                    5 to "5 - 통증이 분명히 느껴짐",
                    6 to "6 - 약간 신경 쓰이는 통증이 있음",
                    7 to "7 - 가벼운 불편감이 있음",
                    8 to "8 - 아주 약하게 느껴짐",
                    9 to "9 - 거의 느껴지지 않음",
                    10 to "10 - 통증 없음"
                )),

            // 2번 질문: 운동 강도 적절성
            StatusQuestion(2, "오늘 운동 강도는 내 몸 상태에\n적절했나요?", "너무 약하거나 무리", "딱 맞았음", 8,
                mapOf(
                    1 to "1 - 너무 약하거나 무리 / 호흡, 근육, 자세 등이 안 맞음",
                    2 to "2 - 많이 안 맞음 / 너무 쉽거나 너무 버거움",
                    3 to "3 - 안 맞는 편 / 숨이 너무 차거나 자극이 부족했음",
                    4 to "4 - 조금 아쉬움 / 강도가 다소 안 맞았음",
                    5 to "5 - 무난하지만 애매 / 숨참이나 근육 피로가 부족하거나 과했음",
                    6 to "6 - 크게 무리 없음 / 전반적으로 소화 가능했음",
                    7 to "7 - 대체로 잘 맞음 / 숨은 차지만 자세는 유지됨",
                    8 to "8 - 잘 맞음 / 근육 피로와 호흡이 적절했음",
                    9 to "9 - 매우 잘 맞음 / 힘들었지만 끝까지 안정적으로 수행함",
                    10 to "10 - 딱 맞음 / 숨참, 근육 피로, 자세 유지가 모두 적절했음"
                )),

            // 3번 질문: 어지러움 및 불편감
            StatusQuestion(3, "운동 후 어지러움이나\n불편감이 있었나요?", "매우 심했음", "전혀 없었음", 10,
                mapOf(
                    1 to "1 - 매우 심했음 / 움직이기 어렵고 오래 불편했음",
                    2 to "2 - 많이 심했음 / 한참 쉬어야 했음",
                    3 to "3 - 심한 편이었음 / 바로 회복되지 않았음",
                    4 to "4 - 꽤 불편했음 / 잠시 멈추고 쉬고 싶었음",
                    5 to "5 - 분명히 느껴졌음 / 신경 쓰일 정도였음",
                    6 to "6 - 조금 있었음 / 잠깐 불편했음",
                    7 to "7 - 약하게 있었음 / 금방 괜찮아졌음",
                    8 to "8 - 아주 미세했음 / 거의 신경 쓰이지 않았음",
                    9 to "9 - 거의 없었음",
                    10 to "10 - 전혀 없었음"
                )),

            // 4번 질문: 전반적인 기분 상태
            StatusQuestion(4, "운동 후 전반적인 기분 상태는\n어떤가요?", "매우 안 좋음", "최상 ", 7,
                mapOf(
                    1 to "1 - 매우 안 좋음 / 많이 지치고 힘든 상태",
                    2 to "2 - 많이 안 좋은 상태",
                    3 to "3 - 안 좋은 편 / 피로감이 큼",
                    4 to "4 - 다소 안 좋은 상태",
                    5 to "5 - 보통 이하 / 썩 좋지 않음",
                    6 to "6 - 무난한 상태",
                    7 to "7 - 괜찮은 편 / 비교적 안정적임",
                    8 to "8 - 좋은 편 / 몸과 마음이 가벼운 편임",
                    9 to "9 - 매우 좋음 / 활력이 있음",
                    10 to "10 - 최상 / 매우 개운하고 만족스러움"
                )),

            // 5번 질문: 목표 달성도 (재훈님이 주신 10단계 가이드 적용)
            StatusQuestion(5, "오늘 계획한 운동 목표를\n달성했나요?", "거의 못 함", "계획보다 많이 더 함", 8,
                mapOf(
                    1 to "1 - 거의 못 함",
                    2 to "2 - 조금만 함",
                    3 to "3 - 일부만 함",
                    4 to "4 - 절반도 못 함",
                    5 to "5 - 절반 정도 함",
                    6 to "6 - 절반 넘게 함",
                    7 to "7 - 대부분 함",
                    8 to "8 - 계획한 만큼 함",
                    9 to "9 - 계획보다 조금 더 함",
                    10 to "10 - 계획보다 많이 더 함"
                ))
        )

        questionAdapter = StatusQuestionAdapter(questionList)
        binding.rvStatusQuestions.apply {
            layoutManager = LinearLayoutManager(this@WorkoutFinishActivity)
            adapter = questionAdapter
        }
    }

    private fun setupBodyParts() {
        bodyPartAdapter = BodyPartAdapter(mutableListOf()) { part ->
            // 특정 부위 클릭 시 통증 태그 영역에 텍스트 반영 (바텀시트 없이 바로 입력하는 경우 대비)
            binding.tvPainTagContent.text = part.name
            binding.tvPainTagContent.setTextColor(Color.parseColor("#3A8DFF"))
        }

        binding.rvBodyParts.apply {
            layoutManager = LinearLayoutManager(this@WorkoutFinishActivity)
            adapter = bodyPartAdapter
        }

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