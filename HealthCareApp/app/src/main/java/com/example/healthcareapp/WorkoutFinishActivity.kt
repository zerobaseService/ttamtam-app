package com.example.healthcareapp

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.healthcareapp.adapter.BodyPart
import com.example.healthcareapp.adapter.BodyPartAdapter
import com.example.healthcareapp.adapter.StatusQuestionAdapter
import com.example.healthcareapp.data.ApiResponse
import com.example.healthcareapp.data.CompleteJournalRequest
import com.example.healthcareapp.network.ImageUploadApiService
import com.example.healthcareapp.network.ImageUploadResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import com.example.healthcareapp.data.ExerciseDto
import com.example.healthcareapp.data.ExerciseRecord
import com.example.healthcareapp.data.ExerciseSetDto
import com.example.healthcareapp.data.PainRecord
import com.example.healthcareapp.data.PainRecordDto
import com.example.healthcareapp.data.PostConditionDto
import com.example.healthcareapp.data.StatusQuestion
import com.example.healthcareapp.databinding.StatusQuestionBinding
import com.example.healthcareapp.network.RetrofitClient
import com.example.healthcareapp.sheet.PainBottomSheetFragment
import com.example.healthcareapp.util.BodyPartMapper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class WorkoutFinishActivity : AppCompatActivity() {

    private lateinit var binding: StatusQuestionBinding
    private lateinit var questionAdapter: StatusQuestionAdapter
    private lateinit var bodyPartAdapter: BodyPartAdapter
    private lateinit var questionList: List<StatusQuestion>

    private var receivedWorkoutType: String = "개인운동"
    private var selectedPainRecord: PainRecord? = null

    private var journalId: Long = -1L
    private var workoutDate: String = ""
    private var startedAt: String = ""
    private var exercises: List<ExerciseRecord> = emptyList()
    private val uploadedImageUrls = mutableListOf<String>()

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { uploadImage(it) }
    }

    private val emojiList = listOf(
        R.drawable.emoticon1, R.drawable.emoticon2, R.drawable.emoticon3,
        R.drawable.emoticon4, R.drawable.emoticon5
    )

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

        receivedWorkoutType = intent.getStringExtra("WORKOUT_TYPE") ?: "개인운동"
        journalId = intent.getLongExtra("JOURNAL_ID", -1L)
        workoutDate = intent.getStringExtra("WORKOUT_DATE") ?: ""
        startedAt = intent.getStringExtra("STARTED_AT") ?: ""

        @Suppress("UNCHECKED_CAST")
        exercises = (intent.getSerializableExtra("EXERCISES") as? ArrayList<ExerciseRecord>) ?: emptyList()

        setupTimeInfo()
        setupStatusQuestions()
        setupBodyParts()
        setupPainTagClear()
        setupPhotoAttachment()
        initClickListeners()
    }

    private fun setupPhotoAttachment() {
        binding.btnAddPhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
        binding.btnRemovePhoto.setOnClickListener {
            uploadedImageUrls.clear()
            binding.ivPreview.setImageDrawable(null)
            binding.btnRemovePhoto.visibility = View.GONE
            binding.ivPreview.visibility = View.GONE
        }
        binding.btnRemovePhoto.visibility = View.GONE
        binding.ivPreview.visibility = View.GONE
    }

    private fun uploadImage(uri: Uri) {
        val inputStream = contentResolver.openInputStream(uri) ?: return
        val bytes = inputStream.readBytes()
        inputStream.close()

        val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
        val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        val ext = if (mimeType.contains("png")) "png" else "jpg"
        val part = MultipartBody.Part.createFormData("file", "upload.$ext", requestBody)

        binding.btnAddPhoto.isClickable = false
        RetrofitClient.imageUploadService.uploadImage(part)
            .enqueue(object : Callback<ApiResponse<ImageUploadResponse>> {
                override fun onResponse(
                    call: Call<ApiResponse<ImageUploadResponse>>,
                    response: Response<ApiResponse<ImageUploadResponse>>
                ) {
                    binding.btnAddPhoto.isClickable = true
                    if (response.isSuccessful) {
                        val url = response.body()?.data?.imageUrl ?: return
                        uploadedImageUrls.clear()
                        uploadedImageUrls.add(url)
                        Glide.with(this@WorkoutFinishActivity).load(uri).into(binding.ivPreview)
                        binding.ivPreview.visibility = View.VISIBLE
                        binding.btnRemovePhoto.visibility = View.VISIBLE
                    } else {
                        Toast.makeText(this@WorkoutFinishActivity, "이미지 업로드 실패", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<ApiResponse<ImageUploadResponse>>, t: Throwable) {
                    binding.btnAddPhoto.isClickable = true
                    Toast.makeText(this@WorkoutFinishActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun setupPainTagClear() {
        binding.btnClearPain.setOnClickListener {
            selectedPainRecord = null
            updatePainTagUI()
        }
    }

    private fun updatePainTagUI() {
        val record = selectedPainRecord
        if (record != null) {
            binding.tvPainTagContent.text = "${record.side} ${record.bodyPartName} : ${record.painLevel}단계"
            binding.tvPainTagContent.setTextColor(Color.parseColor("#2D3A4B"))
            binding.btnClearPain.visibility = View.VISIBLE
        } else {
            binding.tvPainTagContent.text = "통증 부위를 선택해주세요"
            binding.tvPainTagContent.setTextColor(Color.parseColor("#94A3B8"))
            binding.btnClearPain.visibility = View.GONE
        }
    }

    private fun initClickListeners() {
        binding.btnFinishWorkout.setOnClickListener {
            binding.btnFinishWorkout.isEnabled = false
            submitComplete()
        }
    }

    private fun submitComplete() {
        val scores = questionList.map { it.score }
        // POST 컨디션 순서: 통증, 강도적합, 어지러움, 기분, 목표달성
        val postCondition = PostConditionDto(
            jointMusclePain = scores[0],
            intensityFit = scores[1],
            dizziness = scores[2],
            mood = scores[3],
            goalAchieved = scores[4]
        )

        val painRecords = selectedPainRecord?.let { record ->
            listOf(PainRecordDto(
                bodyPart = BodyPartMapper.toServerBodyPart(record.bodyPartName),
                side = BodyPartMapper.toServerBodySide(record.side),
                painLevel = record.painLevel
            ))
        }

        val exerciseDtos = exercises.mapIndexed { index, record ->
            ExerciseDto(
                exerciseName = record.name,
                displayOrder = index,
                sets = record.sets.map { set ->
                    ExerciseSetDto(
                        setNumber = set.setNumber,
                        reps = set.reps,
                        weightKg = set.weight,
                        durationMinutes = set.durationMinutes.takeIf { it > 0 }
                    )
                }
            )
        }

        val now = Date()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val resolvedWorkoutDate = workoutDate.ifBlank { dateFormat.format(now) }
        val resolvedStartedAt = startedAt.ifBlank { dateTimeFormat.format(now) }

        val totalSeconds = parseTotalSeconds(binding.tvTotalTime.text.toString())
        val content = binding.etFeedbackMemo.text.toString().ifBlank { null }

        val request = CompleteJournalRequest(
            workoutDate = resolvedWorkoutDate,
            startedAt = resolvedStartedAt,
            totalDurationSeconds = totalSeconds,
            postCondition = postCondition,
            painRecords = painRecords,
            exercises = exerciseDtos,
            content = content,
            workoutType = receivedWorkoutType,
            imageUrls = uploadedImageUrls.ifEmpty { null }
        )

        RetrofitClient.journalService.completeJournal(request)
            .enqueue(object : Callback<ApiResponse<Any>> {
                override fun onResponse(call: Call<ApiResponse<Any>>, response: Response<ApiResponse<Any>>) {
                    if (response.isSuccessful) {
                        val index = (System.currentTimeMillis() / 1000 % emojiList.size).toInt()
                        val resultIntent = Intent().apply {
                            putExtra("WORKOUT_MEMO", content)
                            putExtra("WORKOUT_TYPE", receivedWorkoutType)
                            putExtra("EMOJI_RES_ID", emojiList[index])
                        }
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    } else {
                        Toast.makeText(this@WorkoutFinishActivity, "저장 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                        binding.btnFinishWorkout.isEnabled = true
                    }
                }

                override fun onFailure(call: Call<ApiResponse<Any>>, t: Throwable) {
                    Toast.makeText(this@WorkoutFinishActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                    binding.btnFinishWorkout.isEnabled = true
                }
            })
    }

    private fun parseTotalSeconds(timeStr: String): Int? {
        return try {
            val parts = timeStr.split(":")
            if (parts.size == 3) {
                parts[0].toInt() * 3600 + parts[1].toInt() * 60 + parts[2].toInt()
            } else null
        } catch (e: Exception) { null }
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
            StatusQuestion(1, "운동 후 평소와 다른 관절이나\n근육 통증이 있었나요?", "매우 심함", "통증 없음", 10,
                mapOf(1 to "매우 심함 / 일상 움직임도 힘들 정도", 2 to "통증이 많이 심한 편임", 3 to "통증이 뚜렷하고 불편함이 큼", 4 to "통증이 꽤 느껴지고 거슬림", 5 to "통증이 분명히 느껴짐", 6 to "약간 신경 쓰이는 통증이 있음", 7 to "가벼운 불편감이 있음", 8 to "아주 약하게 느껴짐", 9 to "거의 느껴지지 않음", 10 to "통증 없음")),
            StatusQuestion(2, "오늘 운동 강도는 내 몸 상태에\n적절했나요?", "너무 약하거나 무리", "딱 맞았음", 8,
                mapOf(1 to "너무 약하거나 무리", 2 to "많이 안 맞음", 3 to "안 맞는 편", 4 to "조금 아쉬움", 5 to "무난하지만 애매", 6 to "크게 무리 없음", 7 to "대체로 잘 맞음", 8 to "잘 맞음", 9 to "매우 잘 맞음", 10 to "딱 맞음")),
            StatusQuestion(3, "운동 후 어지러움이나\n불편감이 있었나요?", "매우 심했음", "전혀 없었음", 10,
                mapOf(1 to "매우 심했음", 2 to "많이 심했음", 3 to "심한 편이었음", 4 to "꽤 불편했음", 5 to "분명히 느껴졌음", 6 to "조금 있었음", 7 to "약하게 있었음", 8 to "아주 미세했음", 9 to "거의 없었음", 10 to "전혀 없었음")),
            StatusQuestion(4, "운동 후 전반적인 기분 상태는\n어떤가요?", "매우 안 좋음", "최상", 7,
                mapOf(1 to "매우 안 좋음", 2 to "많이 안 좋은 상태", 3 to "안 좋은 편", 4 to "다소 안 좋은 상태", 5 to "보통 이하", 6 to "무난한 상태", 7 to "괜찮은 편", 8 to "좋은 편", 9 to "매우 좋음", 10 to "최상")),
            StatusQuestion(5, "오늘 계획한 운동 목표를\n달성했나요?", "거의 못 함", "계획보다 많이 더 함", 8,
                mapOf(1 to "거의 못 함", 2 to "조금만 함", 3 to "일부만 함", 4 to "절반도 못 함", 5 to "절반 정도 함", 6 to "절반 넘게 함", 7 to "대부분 함", 8 to "계획한 만큼 함", 9 to "계획보다 조금 더 함", 10 to "계획보다 많이 더 함"))
        )

        questionAdapter = StatusQuestionAdapter(questionList)
        binding.rvStatusQuestions.apply {
            layoutManager = LinearLayoutManager(this@WorkoutFinishActivity)
            adapter = questionAdapter
        }
    }

    private fun setupBodyParts() {
        bodyPartAdapter = BodyPartAdapter(mutableListOf()) { part ->
            val bottomSheet = PainBottomSheetFragment(part.name) { painRecord ->
                selectedPainRecord = painRecord
                updatePainTagUI()
            }
            bottomSheet.show(supportFragmentManager, "PainBottomSheet")
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
