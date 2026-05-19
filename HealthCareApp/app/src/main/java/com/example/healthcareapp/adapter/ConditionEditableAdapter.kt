package com.example.healthcareapp.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.R
import com.example.healthcareapp.data.ConditionEditState
import com.example.healthcareapp.data.ConditionSliderGuide
import com.example.healthcareapp.data.PainRecord
import com.example.healthcareapp.data.PainSelectionState
import com.example.healthcareapp.data.StatusQuestion1
import com.example.healthcareapp.databinding.ItemConditionEditableCardBinding
import com.example.healthcareapp.databinding.ItemConditionQuestionV2Binding
import com.example.healthcareapp.sheet.PainBottomSheetFragment

class ConditionEditableAdapter(
    private val editState: ConditionEditState,
    private val fragmentManager: FragmentManager,
    private val imageLogic: EditableImageLogic,
    private val onAddImage: () -> Unit,
    private val onRemoveImage: (Int) -> Unit,
    private val onDirty: () -> Unit
) : RecyclerView.Adapter<ConditionEditableAdapter.CardHolder>() {

    private val accordion = AccordionState(0)

    enum class CardType { PRE, POST }

    private val cards: List<CardType> = buildList {
        if (editState.hasPreCondition) add(CardType.PRE)
        if (editState.hasPostCondition) add(CardType.POST)
    }

    // 카드별 UI 상태 (direction, category)
    private val directions = Array(cards.size) { "앞면" }
    private val categories = Array(cards.size) { "머리/목" }

    private val bodyPartMap = mapOf(
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

    inner class CardHolder(val binding: ItemConditionEditableCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        lateinit var bodyPartAdapter: BodyPartAdapter
        lateinit var imageAdapter: EditableImageAdapter
    }

    override fun getItemCount(): Int = cards.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardHolder {
        val binding = ItemConditionEditableCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CardHolder(binding)
    }

    override fun onBindViewHolder(holder: CardHolder, position: Int) {
        val cardType = cards[position]
        val isPre = cardType == CardType.PRE
        val painState = if (isPre) editState.prePainState else editState.postPainState
        val questions = buildQuestions(isPre)

        val binding = holder.binding
        binding.tvConditionTitle.text = if (isPre) "운동 전 컨디션" else "운동 후 컨디션"

        val isExpanded = accordion.expandedPosition == position
        binding.layoutDetail.visibility = if (isExpanded) View.VISIBLE else View.GONE
        binding.ivArrow.rotation = if (isExpanded) 180f else 0f

        binding.layoutHeader.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            val changed = accordion.toggle(pos)
            changed.forEach { notifyItemChanged(it) }
        }

        if (isExpanded) {
            setupSliders(holder, questions, isPre)
            setupPainSection(holder, position, painState, isPre)
            setupPostOnlySections(holder, isPre)
        }
    }

    private fun buildQuestions(isPre: Boolean): List<StatusQuestion1> {
        return if (isPre) {
            listOf(
                StatusQuestion1("1/5", "관절/근육 통증이 있나요?", editState.preJointMusclePain.toFloat(), "매우 심함", "통증 없음"),
                StatusQuestion1("2/5", "어젯밤 수면 시간은?", editState.preSleepHours.toFloat(), "1시간 이하", "10시간 이상"),
                StatusQuestion1("3/5", "수면의 질은?", editState.preSleepQuality.toFloat(), "매우 나쁨", "매우 좋음"),
                StatusQuestion1("4/5", "이전 운동 피로가 남아있나요?", editState.prePreviousFatigue.toFloat(), "매우 많이 남아있음", "전혀 없음"),
                StatusQuestion1("5/5", "전반적인 컨디션은?", editState.preOverallCondition.toFloat(), "매우 안 좋음", "최상")
            )
        } else {
            listOf(
                StatusQuestion1("1/5", "운동 후 관절/근육 통증?", editState.postJointMusclePain.toFloat(), "매우 심함", "통증 없음"),
                StatusQuestion1("2/5", "오늘 운동 강도는 적절했나요?", editState.postIntensityFit.toFloat(), "너무 약하거나 무리", "딱 맞았음"),
                StatusQuestion1("3/5", "어지러움/불편감이 있었나요?", editState.postDizziness.toFloat(), "매우 심했음", "전혀 없었음"),
                StatusQuestion1("4/5", "운동 후 전반적인 기분은?", editState.postMood.toFloat(), "매우 안 좋음", "최상"),
                StatusQuestion1("5/5", "오늘 운동 목표를 달성했나요?", editState.postGoalAchieved.toFloat(), "거의 못 함", "계획보다 많이 더 함")
            )
        }
    }

    private fun setupSliders(
        holder: CardHolder,
        questions: List<StatusQuestion1>,
        isPre: Boolean
    ) {
        holder.binding.rvQuestions.apply {
            layoutManager = LinearLayoutManager(context)
            isNestedScrollingEnabled = false
            adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

                inner class QHolder(val b: ItemConditionQuestionV2Binding) :
                    RecyclerView.ViewHolder(b.root)

                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                    QHolder(ItemConditionQuestionV2Binding.inflate(LayoutInflater.from(parent.context), parent, false))

                override fun getItemCount() = questions.size

                override fun onBindViewHolder(h: RecyclerView.ViewHolder, pos: Int) {
                    val q = questions[pos]
                    val b = (h as QHolder).b
                    b.tvStepCount.text = q.step
                    b.tvQuestionTitle.text = q.title
                    b.tvMinLabel.text = q.minLabel
                    b.tvMaxLabel.text = q.maxLabel
                    b.slider.isEditEnabled = true
                    b.slider.max = 10
                    b.slider.progress = q.score.toInt().coerceIn(1, 10)
                    b.tvSliderGuide.text = ConditionSliderGuide.guideFor(pos, b.slider.progress)
                    b.slider.onProgressChanged = { value ->
                        b.tvSliderGuide.text = ConditionSliderGuide.guideFor(pos, value)
                        applyScoreChange(isPre, pos, value)
                    }
                }
            }
        }
    }

    private fun applyScoreChange(isPre: Boolean, index: Int, value: Int) {
        if (isPre) {
            when (index) {
                0 -> editState.preJointMusclePain = value
                1 -> editState.preSleepHours = value
                2 -> editState.preSleepQuality = value
                3 -> editState.prePreviousFatigue = value
                4 -> editState.preOverallCondition = value
            }
            editState.preConditionDirty = true
        } else {
            when (index) {
                0 -> editState.postJointMusclePain = value
                1 -> editState.postIntensityFit = value
                2 -> editState.postDizziness = value
                3 -> editState.postMood = value
                4 -> editState.postGoalAchieved = value
            }
            editState.postConditionDirty = true
        }
        onDirty()
    }

    private fun setupPainSection(
        holder: CardHolder,
        position: Int,
        painState: PainSelectionState,
        isPre: Boolean
    ) {
        val b = holder.binding
        val ctx = b.root.context

        holder.bodyPartAdapter = BodyPartAdapter(
            mutableListOf(),
            isSelectedProvider = { partName ->
                painState.isAnySelected(directions[position], partName)
            }
        ) { part ->
            val sheet = PainBottomSheetFragment.newInstance(
                part.name, PainBottomSheetFragment.Mode.CREATE
            ) { painRecord ->
                val added = painState.addIfAbsent(directions[position], painRecord)
                if (!added) {
                    Toast.makeText(ctx, "이미 선택된 부위입니다", Toast.LENGTH_SHORT).show()
                } else {
                    setDirty(isPre)
                    refreshPainTags(holder, painState)
                    holder.bodyPartAdapter.notifyDataSetChanged()
                }
            }
            sheet.show(fragmentManager, "PainSheet")
        }

        b.rvBodyParts.apply {
            layoutManager = LinearLayoutManager(ctx)
            isNestedScrollingEnabled = false
            adapter = holder.bodyPartAdapter
        }
        updateBodyPartList(holder, position)

        b.btnFront.setOnClickListener {
            directions[position] = "앞면"
            updateDirectionTab(holder, isFront = true)
            updateBodyPartList(holder, position)
        }
        b.btnBack.setOnClickListener {
            directions[position] = "뒷면"
            updateDirectionTab(holder, isFront = false)
            updateBodyPartList(holder, position)
        }

        b.chipGroupBody.setOnCheckedStateChangeListener { _, checkedIds ->
            val id = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            categories[position] = when (id) {
                R.id.chip_head -> "머리/목"
                R.id.chip_upper -> "상체"
                R.id.chip_arm -> "팔/손"
                R.id.chip_lower -> "하체"
                R.id.chip_foot -> "발"
                else -> "머리/목"
            }
            updateBodyPartList(holder, position)
        }

        refreshPainTags(holder, painState)
    }

    private fun refreshPainTags(holder: CardHolder, painState: PainSelectionState) {
        val b = holder.binding
        val ctx = b.root.context
        b.layoutSelectedPainTags.removeAllViews()

        val all = painState.all
        if (all.isEmpty()) {
            b.tvPainTagPlaceholder.visibility = View.VISIBLE
            b.layoutSelectedPainTags.visibility = View.GONE
        } else {
            b.tvPainTagPlaceholder.visibility = View.GONE
            b.layoutSelectedPainTags.visibility = View.VISIBLE

            val dp = ctx.resources.displayMetrics.density
            val dpI = { n: Int -> (n * dp).toInt() }
            val gray = Color.parseColor("#8896A8")

            for (sp in all) {
                val row = LinearLayout(ctx).apply {
                    orientation = LinearLayout.HORIZONTAL
                    android.view.Gravity.CENTER_VERTICAL.also { gravity = it }
                    setBackgroundResource(R.drawable.bg_selected_tag)
                    setPadding(dpI(12), dpI(8), dpI(8), dpI(8))
                }
                val leftGroup = LinearLayout(ctx).apply {
                    orientation = LinearLayout.HORIZONTAL
                    android.view.Gravity.CENTER_VERTICAL.also { gravity = it }
                }
                row.addView(leftGroup, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
                leftGroup.addView(TextView(ctx).apply {
                    text = "${sp.record.side} ${sp.record.bodyPartName}"
                    setTextColor(gray)
                    textSize = 14f
                })
                val divLp = LinearLayout.LayoutParams(dpI(1), dpI(10)).apply {
                    marginStart = dpI(6); marginEnd = dpI(6)
                }
                leftGroup.addView(View(ctx).apply { setBackgroundColor(Color.parseColor("#B8BDC3")) }, divLp)
                leftGroup.addView(TextView(ctx).apply {
                    text = "통증정도: ${sp.record.painLevel}단계"
                    setTextColor(gray)
                    textSize = 14f
                })
                row.addView(ImageView(ctx).apply {
                    setImageResource(R.drawable.ic_tag_close)
                    setOnClickListener {
                        painState.remove(sp.direction, sp.record)
                        val pos = holder.bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            val isPre = cards[pos] == CardType.PRE
                            setDirty(isPre)
                        }
                        refreshPainTags(holder, painState)
                        holder.bodyPartAdapter.notifyDataSetChanged()
                    }
                }, LinearLayout.LayoutParams(dpI(20), dpI(20)))

                val rowLp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dpI(8) }
                b.layoutSelectedPainTags.addView(row, rowLp)
            }
        }

        b.tvFrontCount.text = painState.countByDirection("앞면").toString()
        b.tvBackCount.text = painState.countByDirection("뒷면").toString()
    }

    private fun setDirty(isPre: Boolean) {
        if (isPre) editState.prePainDirty = true
        else editState.postPainDirty = true
        onDirty()
    }

    private fun updateBodyPartList(holder: CardHolder, position: Int) {
        val dir = directions[position]
        val cat = categories[position]
        val names = bodyPartMap[dir]?.get(cat) ?: emptyList()
        holder.bodyPartAdapter.updateItems(names.map { BodyPart(it) })
    }

    private fun updateDirectionTab(holder: CardHolder, isFront: Boolean) {
        val active = Color.parseColor("#4A5768")
        val inactive = Color.parseColor("#8896A8")
        val countActive = Color.parseColor("#53A1FF")
        val b = holder.binding
        if (isFront) {
            b.btnFront.setBackgroundResource(R.drawable.bg_tab_selected)
            b.btnBack.setBackgroundResource(android.R.color.transparent)
            b.tvFrontLabel.setTextColor(active)
            b.tvFrontCount.setTextColor(countActive)
            b.tvBackLabel.setTextColor(inactive)
            b.tvBackCount.setTextColor(inactive)
        } else {
            b.btnBack.setBackgroundResource(R.drawable.bg_tab_selected)
            b.btnFront.setBackgroundResource(android.R.color.transparent)
            b.tvBackLabel.setTextColor(active)
            b.tvBackCount.setTextColor(countActive)
            b.tvFrontLabel.setTextColor(inactive)
            b.tvFrontCount.setTextColor(inactive)
        }
    }

    private fun setupPostOnlySections(holder: CardHolder, isPre: Boolean) {
        val b = holder.binding
        if (isPre) {
            b.layoutMemoSection.visibility = View.GONE
            b.layoutImageSection.visibility = View.GONE
        } else {
            b.layoutMemoSection.visibility = View.VISIBLE
            b.layoutImageSection.visibility = View.VISIBLE

            b.etMemo.setText(editState.memo)
            b.etMemo.setOnFocusChangeListener { _, _ -> }
            b.etMemo.addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: android.text.Editable?) {
                    editState.memo = s?.toString()?.ifEmpty { null }
                    editState.memoDirty = true
                    onDirty()
                }
            })

            holder.imageAdapter = EditableImageAdapter(
                logic = imageLogic,
                onAdd = onAddImage,
                onRemove = { index ->
                    onRemoveImage(index)
                    holder.imageAdapter.notifyDataSetChanged()
                }
            )
            b.rvImages.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                isNestedScrollingEnabled = false
                adapter = holder.imageAdapter
            }
        }
    }
}
