package com.example.healthcareapp

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.healthcareapp.data.ApiResponse
import com.example.healthcareapp.data.ExerciseItem
import com.example.healthcareapp.data.ExerciseSummaryDto
import com.example.healthcareapp.data.toExerciseItem
import com.example.healthcareapp.databinding.AddExerciseBinding
import com.example.healthcareapp.network.RetrofitClient
import com.google.android.material.chip.Chip
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddExerciseSessionActivity : AppCompatActivity() {
    private lateinit var binding: AddExerciseBinding
    private lateinit var adapter: ExerciseAddAdapter
    private var allExercises: List<ExerciseItem> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AddExerciseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        initChipStyles()
        setupCategoryFilter()
        loadExercisesFromApi()

        binding.btnSelectExercise.setOnClickListener {
            val selectedExercises = adapter.getSelectedItems().toList()
            if (selectedExercises.isNotEmpty()) {
                val selectedNames = selectedExercises.map { it.name }.toTypedArray()
                val selectedIds = selectedExercises.map { it.id }.toTypedArray()
                val selectedBodyParts = selectedExercises.map { it.bodyPart }.toTypedArray()
                val intent = android.content.Intent().apply {
                    putExtra("exercise_names", selectedNames)
                    putExtra("exercise_ids", selectedIds)
                    putExtra("exercise_body_parts", selectedBodyParts)
                }
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }

        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = ExerciseAddAdapter(
            items = allExercises,
            onSelectionChanged = { selectedList ->
                updateBottomButton(selectedList.size)
                updateSelectedExerciseTags(selectedList)
            },
            onFavoriteToggle = { exerciseId, currentIsFavorite ->
                toggleFavorite(exerciseId, currentIsFavorite)
            }
        )
        binding.rvExerciseList.layoutManager = LinearLayoutManager(this)
        binding.rvExerciseList.adapter = adapter
    }

    private fun loadExercisesFromApi() {
        RetrofitClient.exerciseService.getAllExercises()
            .enqueue(object : Callback<ApiResponse<List<ExerciseSummaryDto>>> {
                override fun onResponse(
                    call: Call<ApiResponse<List<ExerciseSummaryDto>>>,
                    response: Response<ApiResponse<List<ExerciseSummaryDto>>>
                ) {
                    when (response.code()) {
                        200 -> {
                            val dtos = response.body()?.data ?: emptyList()
                            allExercises = dtos.map { it.toExerciseItem() }
                            adapter.updateList(allExercises)
                        }
                        401, 403 -> {
                            Toast.makeText(this@AddExerciseSessionActivity,
                                "로그인이 필요합니다. 다시 로그인해주세요.", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        404 -> Toast.makeText(this@AddExerciseSessionActivity,
                            "운동 목록을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                        else -> Toast.makeText(this@AddExerciseSessionActivity,
                            "서버 오류가 발생했습니다. (${response.code()})", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse<List<ExerciseSummaryDto>>>, t: Throwable) {
                    Toast.makeText(this@AddExerciseSessionActivity,
                        "네트워크 연결을 확인해주세요.", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun toggleFavorite(exerciseId: String, currentIsFavorite: Boolean) {
        val call = if (currentIsFavorite)
            RetrofitClient.exerciseService.removeFavorite(exerciseId)
        else
            RetrofitClient.exerciseService.addFavorite(exerciseId)

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                when (response.code()) {
                    200, 204 -> {
                        val newState = !currentIsFavorite
                        allExercises = allExercises.map {
                            if (it.id == exerciseId) it.copy(isFavorite = newState) else it
                        }
                        adapter.updateFavoriteState(exerciseId, newState)
                    }
                    401, 403 -> Toast.makeText(this@AddExerciseSessionActivity,
                        "즐겨찾기 권한이 없습니다.", Toast.LENGTH_SHORT).show()
                    404 -> Toast.makeText(this@AddExerciseSessionActivity,
                        "해당 운동을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                    else -> Toast.makeText(this@AddExerciseSessionActivity,
                        "즐겨찾기 처리에 실패했습니다. (${response.code()})", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@AddExerciseSessionActivity,
                    "네트워크 오류로 즐겨찾기를 변경할 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun initChipStyles() {
        val states = arrayOf(
            intArrayOf(android.R.attr.state_checked),
            intArrayOf(android.R.attr.state_pressed),
            intArrayOf()
        )
        val colors = intArrayOf(
            Color.parseColor("#4A5768"),
            Color.parseColor("#4A5768"),
            Color.parseColor("#F2F4F6")
        )
        val backgroundStateList = ColorStateList(states, colors)
        val textColors = intArrayOf(Color.WHITE, Color.WHITE, Color.parseColor("#8E949A"))
        val textStateList = ColorStateList(states, textColors)

        for (i in 0 until binding.chipGroupCategory.childCount) {
            val chip = binding.chipGroupCategory.getChildAt(i) as? Chip
            chip?.apply {
                chipBackgroundColor = backgroundStateList
                setTextColor(textStateList)
                isCheckedIconVisible = false
                rippleColor = ColorStateList.valueOf(Color.TRANSPARENT)
                chipStrokeWidth = 0f
            }
        }
    }

    private fun setupCategoryFilter() {
        binding.chipGroupCategory.setOnCheckedStateChangeListener { group, checkedIds ->
            val categoryName = if (checkedIds.isEmpty()) "전체"
            else group.findViewById<Chip>(checkedIds.first()).text.toString()
            filterExercises(categoryName)
        }
    }

    private fun filterExercises(category: String) {
        val filtered = if (category == "전체") allExercises
        else allExercises.filter { item ->
            val muscles = item.target.split(",").map { it.trim().lowercase() }
            val cat = item.bodyPart.lowercase()
            when (category) {
                "유산소" -> cat == "cardio"
                "가슴"  -> muscles.any { it == "chest" }
                "등"   -> muscles.any { it in listOf("lats", "middle back", "lower back", "traps") }
                "어깨"  -> muscles.any { it == "shoulders" }
                "팔"   -> muscles.any { it in listOf("biceps", "triceps", "forearms") }
                "하체"  -> muscles.any { it in listOf("quadriceps", "hamstrings", "glutes", "calves", "adductors", "abductors") }
                else   -> false
            }
        }
        adapter.updateList(filtered)
        binding.rvExerciseList.scrollToPosition(0)
    }

    private fun updateBottomButton(count: Int) {
        binding.btnSelectExercise.apply {
            val tf = ResourcesCompat.getFont(this@AddExerciseSessionActivity, R.font.pretendard)
            typeface = tf
            if (count > 0) {
                text = "+ ${count}개의 운동 추가하기"
                backgroundTintList = ColorStateList.valueOf(Color.parseColor("#4A5768"))
                isEnabled = true
            } else {
                text = "+ 0개의 운동 추가하기"
                backgroundTintList = ColorStateList.valueOf(Color.parseColor("#B1B8C0"))
                isEnabled = false
            }
            setTextColor(Color.WHITE)
        }
    }

    private fun updateSelectedExerciseTags(selectedList: List<ExerciseItem>) {
        binding.chipGroupSelectedExercises.removeAllViews()
        selectedList.forEach { item ->
            val chip = Chip(this).apply {
                text = item.name
                isCloseIconVisible = true
                closeIconSize = 35f
                setCloseIconTintResource(android.R.color.darker_gray)
                chipBackgroundColor = ColorStateList.valueOf(Color.WHITE)
                chipStrokeColor = ColorStateList.valueOf(Color.parseColor("#D1D8E0"))
                chipStrokeWidth = 2f
                setTextColor(Color.parseColor("#8E949A"))
                textSize = 12f
                setOnCloseIconClickListener { adapter.removeSelection(item.id) }
            }
            binding.chipGroupSelectedExercises.addView(chip)
        }
    }
}
