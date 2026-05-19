package com.example.healthcareapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.adapter.BodyPart
import com.example.healthcareapp.adapter.BodyPartAdapter
import com.example.healthcareapp.adapter.ConditionCheckAdapterV2
import com.example.healthcareapp.data.CreateJournalRequest
import com.example.healthcareapp.data.PainRecordDto
import com.example.healthcareapp.data.PainSelectionState
import com.example.healthcareapp.data.PreConditionDto
import com.example.healthcareapp.data.ApiResponse
import com.example.healthcareapp.data.JournalCreateResponse
import com.example.healthcareapp.data.StatusQuestion1
import com.example.healthcareapp.network.RetrofitClient
import com.example.healthcareapp.sheet.PainBottomSheetFragment
import com.example.healthcareapp.util.BodyPartMapper
import com.google.android.material.chip.ChipGroup
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ConditionCheckActivity : AppCompatActivity() {

    private lateinit var questionAdapter: ConditionCheckAdapterV2
    private lateinit var bodyPartAdapter: BodyPartAdapter
    private val questions = mutableListOf<StatusQuestion1>()

    private val painSelectionState = PainSelectionState()
    private var folderId: Long? = null

    private lateinit var tvPainTagPlaceholder: TextView
    private lateinit var layoutSelectedPainTags: LinearLayout
    private lateinit var tvFrontCount: TextView
    private lateinit var tvBackCount: TextView

    private val bodyDataMap = mapOf(
        "앞면" to mapOf(
            "머리/목" to listOf("머리", "이마", "얼굴", "목"),
            "상체" to listOf("어깨", "가슴", "윗배", "아랫배", "옆구리"),
            "팔/손" to listOf("윗팔", "팔꿈치", "아랫팔", "손목", "손바닥", "손가락"),
            "하체" to listOf("고관절", "사타구니", "생식기", "허벅지", "무릎", "정강이"),
            "발" to listOf("발목", "발등", "발가락")
        ),
        "뒷면" to mapOf(
            "머리/목" to listOf("경추 (목뼈 부위)"),
            "상체" to listOf("등", "어깨", "날개(견갑골)", "허리", "꼬리뼈"),
            "팔/손" to listOf("윗팔", "팔꿈치", "아랫팔", "손목", "손등", "손가락"),
            "하체" to listOf("엉덩이", "뒷허벅지", "오금", "종아리"),
            "발" to listOf("아킬레스건", "발바닥")
        )
    )

    private var currentDirection = "앞면"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.condition_check)

        folderId = intent.getLongExtra("FOLDER_ID", -1L).takeIf { it != -1L }

        tvPainTagPlaceholder = findViewById(R.id.tv_pain_tag_placeholder)
        layoutSelectedPainTags = findViewById(R.id.layout_selected_pain_tags)
        tvFrontCount = findViewById<LinearLayout>(R.id.btn_front).findViewById(R.id.tv_front_count)
        tvBackCount = findViewById<LinearLayout>(R.id.btn_back).findViewById(R.id.tv_back_count)

        initData()
        setupQuestions()
        setupBodyParts()
        setupFinishButton()

        findViewById<ImageView>(R.id.btn_close_condition).setOnClickListener { finish() }
    }

    private fun updatePainTagUI() {
        layoutSelectedPainTags.removeAllViews()

        if (painSelectionState.all.isEmpty()) {
            tvPainTagPlaceholder.visibility = View.VISIBLE
            layoutSelectedPainTags.visibility = View.GONE
        } else {
            tvPainTagPlaceholder.visibility = View.GONE
            layoutSelectedPainTags.visibility = View.VISIBLE

            val d = resources.displayMetrics.density
            val dp = { n: Int -> (n * d).toInt() }
            val gray = Color.parseColor("#8896A8")

            for (sp in painSelectionState.all) {
                val tagRow = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setBackgroundResource(R.drawable.bg_selected_tag)
                    setPadding(dp(12), dp(8), dp(8), dp(8))
                }

                val leftGroup = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                }
                val leftLp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                tagRow.addView(leftGroup, leftLp)

                leftGroup.addView(TextView(this).apply {
                    text = "${sp.record.side} ${sp.record.bodyPartName}"
                    setTextColor(gray)
                    textSize = 14f
                })

                val dividerLp = LinearLayout.LayoutParams(dp(1), dp(10)).apply {
                    marginStart = dp(6)
                    marginEnd = dp(6)
                }
                leftGroup.addView(View(this).apply {
                    setBackgroundColor(Color.parseColor("#B8BDC3"))
                }, dividerLp)

                leftGroup.addView(TextView(this).apply {
                    text = "통증정도: ${sp.record.painLevel}단계"
                    setTextColor(gray)
                    textSize = 14f
                })

                tagRow.addView(ImageView(this).apply {
                    setImageResource(R.drawable.ic_tag_close)
                    setOnClickListener {
                        painSelectionState.remove(sp.direction, sp.record)
                        updatePainTagUI()
                        bodyPartAdapter.notifyDataSetChanged()
                    }
                }, LinearLayout.LayoutParams(dp(20), dp(20)))

                val rowLp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(8) }
                layoutSelectedPainTags.addView(tagRow, rowLp)
            }
        }

        tvFrontCount.text = painSelectionState.countByDirection("앞면").toString()
        tvBackCount.text = painSelectionState.countByDirection("뒷면").toString()
    }

    private fun setupFinishButton() {
        val btnFinish = findViewById<AppCompatButton>(R.id.btn_finish_workout)
        btnFinish.setOnClickListener {
            btnFinish.isEnabled = false
            submitPreCondition(btnFinish)
        }
    }

    private fun submitPreCondition(btnFinish: AppCompatButton) {
        val scores = questions.map { it.score.toInt().coerceIn(1, 10) }
        val preCondition = PreConditionDto(
            jointMusclePain = scores[0],
            sleepHours = scores[1],
            sleepQuality = scores[2],
            previousFatigue = scores[3],
            overallCondition = scores[4]
        )

        val painRecords = painSelectionState.all.map { sp ->
            PainRecordDto(
                bodyPart = BodyPartMapper.toServerBodyPart(sp.record.bodyPartName),
                side = BodyPartMapper.toServerBodySide(sp.record.side),
                painLevel = sp.record.painLevel,
                painReason = sp.record.painReason
            )
        }.takeIf { it.isNotEmpty() }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val now = Date()
        val workoutDate = dateFormat.format(now)
        val startedAt = dateTimeFormat.format(now)

        val request = CreateJournalRequest(
            workoutDate = workoutDate,
            folderId = folderId,
            preCondition = preCondition,
            painRecords = painRecords,
            startedAt = startedAt
        )

        RetrofitClient.journalService.createJournal(request)
            .enqueue(object : Callback<ApiResponse<JournalCreateResponse>> {
                override fun onResponse(
                    call: Call<ApiResponse<JournalCreateResponse>>,
                    response: Response<ApiResponse<JournalCreateResponse>>
                ) {
                    if (response.isSuccessful) {
                        val journalId = response.body()?.data?.journalId
                        navigateToWorkout(journalId, workoutDate, startedAt)
                    } else {
                        Toast.makeText(this@ConditionCheckActivity, "저장 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                        btnFinish.isEnabled = true
                    }
                }

                override fun onFailure(call: Call<ApiResponse<JournalCreateResponse>>, t: Throwable) {
                    Toast.makeText(this@ConditionCheckActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                    btnFinish.isEnabled = true
                }
            })
    }

    private fun navigateToWorkout(journalId: Long?, workoutDate: String, startedAt: String) {
        val intent = Intent(this, WorkoutSessionActivity::class.java).apply {
            putExtra("JOURNAL_ID", journalId ?: -1L)
            putExtra("WORKOUT_DATE", workoutDate)
            putExtra("STARTED_AT", startedAt)
            putExtra("FOLDER_ID", folderId ?: -1L)
        }
        startActivity(intent)
        finish()
    }

    private fun initData() {
        questions.clear()
        for (i in 1..5) {
            questions.add(StatusQuestion1(step = "$i/5", title = "", score = 10f))
        }
    }

    private fun setupQuestions() {
        val rvStatus = findViewById<RecyclerView>(R.id.rv_status_questions)
        questionAdapter = ConditionCheckAdapterV2(questions)
        rvStatus.apply {
            layoutManager = LinearLayoutManager(this@ConditionCheckActivity)
            adapter = questionAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupBodyParts() {
        val btnFront = findViewById<LinearLayout>(R.id.btn_front)
        val btnBack = findViewById<LinearLayout>(R.id.btn_back)
        val chipGroupBody = findViewById<ChipGroup>(R.id.chip_group_body)
        val rvBodyParts = findViewById<RecyclerView>(R.id.rv_body_parts)

        btnFront.setOnClickListener {
            currentDirection = "앞면"
            updateDirectionUI(btnFront, btnBack)
            updateBodyPartList(chipGroupBody, rvBodyParts)
        }

        btnBack.setOnClickListener {
            currentDirection = "뒷면"
            updateDirectionUI(btnFront, btnBack)
            updateBodyPartList(chipGroupBody, rvBodyParts)
        }

        chipGroupBody.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) updateBodyPartList(chipGroupBody, rvBodyParts)
        }

        rvBodyParts.layoutManager = LinearLayoutManager(this)
        updateBodyPartList(chipGroupBody, rvBodyParts)
    }

    private fun updateDirectionUI(front: LinearLayout, back: LinearLayout) {
        val frontLabel = front.findViewById<TextView>(R.id.tv_front_label)
        val frontCount = front.findViewById<TextView>(R.id.tv_front_count)
        val backLabel = back.findViewById<TextView>(R.id.tv_back_label)
        val backCount = back.findViewById<TextView>(R.id.tv_back_count)
        val activeColor = Color.parseColor("#4A5768")
        val inactiveColor = Color.parseColor("#8896A8")
        val countActiveColor = Color.parseColor("#53A1FF")
        if (currentDirection == "앞면") {
            front.setBackgroundResource(R.drawable.bg_tab_selected)
            back.setBackgroundResource(android.R.color.transparent)
            frontLabel.setTextColor(activeColor)
            frontCount.setTextColor(countActiveColor)
            backLabel.setTextColor(inactiveColor)
            backCount.setTextColor(inactiveColor)
        } else {
            back.setBackgroundResource(R.drawable.bg_tab_selected)
            front.setBackgroundResource(android.R.color.transparent)
            backLabel.setTextColor(activeColor)
            backCount.setTextColor(countActiveColor)
            frontLabel.setTextColor(inactiveColor)
            frontCount.setTextColor(inactiveColor)
        }
    }

    private fun updateBodyPartList(chipGroup: ChipGroup, recyclerView: RecyclerView) {
        val selectedChipId = chipGroup.checkedChipId
        val bodyKey = when (selectedChipId) {
            R.id.chip_head -> "머리/목"
            R.id.chip_upper -> "상체"
            R.id.chip_arm -> "팔/손"
            R.id.chip_lower -> "하체"
            R.id.chip_foot -> "발"
            else -> "머리/목"
        }

        val stringList = bodyDataMap[currentDirection]?.get(bodyKey) ?: emptyList()
        val detailList = stringList.map { BodyPart(it) }

        bodyPartAdapter = BodyPartAdapter(
            detailList,
            isSelectedProvider = { partName -> painSelectionState.isAnySelected(currentDirection, partName) }
        ) { clickedPart ->
            val bottomSheet = PainBottomSheetFragment.newInstance(
                clickedPart.name,
                PainBottomSheetFragment.Mode.CREATE
            ) { painRecord ->
                val added = painSelectionState.addIfAbsent(currentDirection, painRecord)
                if (!added) {
                    Toast.makeText(this, "이미 선택된 부위입니다", Toast.LENGTH_SHORT).show()
                } else {
                    updatePainTagUI()
                    bodyPartAdapter.notifyDataSetChanged()
                }
            }
            bottomSheet.show(supportFragmentManager, "PainBottomSheet")
        }

        recyclerView.adapter = bodyPartAdapter
    }
}
