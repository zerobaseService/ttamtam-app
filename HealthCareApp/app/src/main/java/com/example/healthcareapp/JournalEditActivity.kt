package com.example.healthcareapp

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.adapter.ConditionEditableAdapter
import com.example.healthcareapp.adapter.EditableImageLogic
import com.example.healthcareapp.data.ApiResponse
import com.example.healthcareapp.data.ConditionEditState
import com.example.healthcareapp.data.JournalDetailResponse
import com.example.healthcareapp.data.PainRecordMapper
import com.example.healthcareapp.data.UpdateJournalRequest
import com.example.healthcareapp.network.ImageUploadApiService
import com.example.healthcareapp.network.ImageUploadResponse
import com.example.healthcareapp.network.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class JournalEditActivity : AppCompatActivity() {

    private var journalId: Long = -1L
    private var editState: ConditionEditState? = null
    private lateinit var imageLogic: EditableImageLogic
    private lateinit var adapter: ConditionEditableAdapter
    private lateinit var rvCards: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnSave: FrameLayout

    private var inflightUploads = 0

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { uploadImage(it) }
    }

    private var fetchCall: Call<ApiResponse<JournalDetailResponse>>? = null
    private var patchCall: Call<ApiResponse<Any>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_journal_edit)

        journalId = intent.getLongExtra("JOURNAL_ID", -1L)
        rvCards = findViewById(R.id.rv_condition_cards)
        progressBar = findViewById(R.id.progress_bar)
        btnSave = findViewById(R.id.btn_save)

        rvCards.layoutManager = LinearLayoutManager(this)

        btnSave.setOnClickListener { showSaveConfirmDialog() }

        findViewById<View>(R.id.btn_back).setOnClickListener { handleBack() }

        onBackPressedDispatcher.addCallback(this) { handleBack() }

        if (journalId == -1L) {
            Toast.makeText(this, "일지 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        fetchDetail()
    }

    private fun fetchDetail() {
        showLoading(true)
        fetchCall = RetrofitClient.journalService.getJournalDetail(journalId)
        fetchCall?.enqueue(object : Callback<ApiResponse<JournalDetailResponse>> {
            override fun onResponse(
                call: Call<ApiResponse<JournalDetailResponse>>,
                response: Response<ApiResponse<JournalDetailResponse>>
            ) {
                if (isFinishing) return
                showLoading(false)
                if (!response.isSuccessful) {
                    handleHttpError(response.code())
                    return
                }
                val detail = response.body()?.data ?: run {
                    Toast.makeText(this@JournalEditActivity, "데이터를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
                    finish()
                    return
                }
                initEditState(detail)
            }

            override fun onFailure(call: Call<ApiResponse<JournalDetailResponse>>, t: Throwable) {
                if (isFinishing || call.isCanceled) return
                showLoading(false)
                val msg = if (t is java.io.IOException) "네트워크 연결을 확인해주세요." else "오류가 발생했습니다."
                Toast.makeText(this@JournalEditActivity, msg, Toast.LENGTH_SHORT).show()
                finish()
            }
        })
    }

    private fun initEditState(detail: JournalDetailResponse) {
        val state = ConditionEditState.fromDetail(detail)
        editState = state
        imageLogic = EditableImageLogic(state.imageUrls)

        adapter = ConditionEditableAdapter(
            editState = state,
            fragmentManager = supportFragmentManager,
            imageLogic = imageLogic,
            onAddImage = {
                if (inflightUploads > 0) return@ConditionEditableAdapter
                if (!imageLogic.canAdd) {
                    Toast.makeText(this, "최대 5장까지 첨부할 수 있습니다.", Toast.LENGTH_SHORT).show()
                    return@ConditionEditableAdapter
                }
                pickImageLauncher.launch("image/*")
            },
            onRemoveImage = { index ->
                imageLogic.remove(index)
                state.imageDirty = true
                evaluateSaveButton()
            },
            onDirty = { evaluateSaveButton() }
        )
        rvCards.adapter = adapter
        evaluateSaveButton()
    }

    private fun uploadImage(uri: Uri) {
        val inputStream = contentResolver.openInputStream(uri) ?: return
        val bytes = inputStream.readBytes()
        inputStream.close()

        val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
        val ext = if (mimeType.contains("png")) "png" else "jpg"
        val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", "upload.$ext", requestBody)

        inflightUploads++
        evaluateSaveButton()

        RetrofitClient.imageUploadService.uploadImage(part)
            .enqueue(object : Callback<ApiResponse<ImageUploadResponse>> {
                override fun onResponse(
                    call: Call<ApiResponse<ImageUploadResponse>>,
                    response: Response<ApiResponse<ImageUploadResponse>>
                ) {
                    inflightUploads--
                    if (response.isSuccessful) {
                        val url = response.body()?.data?.imageUrl ?: run {
                            evaluateSaveButton()
                            return
                        }
                        imageLogic.add(url)
                        editState?.imageDirty = true
                        adapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(this@JournalEditActivity, "이미지 업로드 실패", Toast.LENGTH_SHORT).show()
                    }
                    evaluateSaveButton()
                }

                override fun onFailure(call: Call<ApiResponse<ImageUploadResponse>>, t: Throwable) {
                    inflightUploads--
                    Toast.makeText(this@JournalEditActivity, "이미지 업로드 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                    evaluateSaveButton()
                }
            })
    }

    private fun evaluateSaveButton() {
        val state = editState ?: return
        val enabled = state.isAnyDirty && inflightUploads == 0
        btnSave.isEnabled = enabled
        btnSave.alpha = if (enabled) 1f else 0.4f
    }

    private fun showSaveConfirmDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_save_confirm)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog.findViewById<View>(R.id.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }
        dialog.findViewById<View>(R.id.btn_confirm).setOnClickListener {
            dialog.dismiss()
            saveChanges()
        }
        dialog.show()
    }

    private fun saveChanges() {
        val state = editState ?: return
        patchCall?.cancel()

        val request = UpdateJournalRequest(
            preCondition = if (state.preConditionDirty) state.toPreConditionDto() else null,
            postCondition = if (state.postConditionDirty) state.toPostConditionDto() else null,
            prePainRecords = if (state.prePainDirty) PainRecordMapper.toServer(state.prePainState) else null,
            postPainRecords = if (state.postPainDirty) PainRecordMapper.toServer(state.postPainState) else null,
            content = if (state.memoDirty) state.memo else null,
            imageUrls = if (state.imageDirty) imageLogic.toList() else null
        )

        btnSave.isEnabled = false
        showLoading(true)

        patchCall = RetrofitClient.journalService.updateJournal(journalId, request)
        patchCall?.enqueue(object : Callback<ApiResponse<Any>> {
            override fun onResponse(
                call: Call<ApiResponse<Any>>,
                response: Response<ApiResponse<Any>>
            ) {
                if (isFinishing) return
                showLoading(false)
                if (response.isSuccessful) {
                    setResult(Activity.RESULT_OK)
                    finish()
                } else {
                    handlePatchError(response.code())
                    btnSave.isEnabled = state.isAnyDirty
                }
            }

            override fun onFailure(call: Call<ApiResponse<Any>>, t: Throwable) {
                if (isFinishing || call.isCanceled) return
                showLoading(false)
                Toast.makeText(this@JournalEditActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                btnSave.isEnabled = state.isAnyDirty
            }
        })
    }

    private fun handleBack() {
        val state = editState
        if (state != null && state.isAnyDirty) {
            AlertDialog.Builder(this)
                .setMessage("변경 사항을 취소하시겠습니까?")
                .setPositiveButton("취소") { _, _ -> finish() }
                .setNegativeButton("계속 편집", null)
                .show()
        } else {
            finish()
        }
    }

    private fun handleHttpError(code: Int) {
        val msg = when (code) {
            401 -> "로그인이 필요합니다."
            403 -> "접근 권한이 없습니다."
            404 -> "일지를 찾을 수 없습니다."
            else -> "오류가 발생했습니다. ($code)"
        }
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun handlePatchError(code: Int) {
        val msg = when (code) {
            401 -> "로그인이 필요합니다."; 403 -> "수정 권한이 없거나 폴더가 잠겨 있습니다."
            404 -> "일지를 찾을 수 없습니다."
            409, 400 -> "입력 내용을 확인해주세요."
            else -> "저장에 실패했습니다. ($code)"
        }
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        rvCards.visibility = if (show) View.GONE else View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        fetchCall?.cancel()
        patchCall?.cancel()
    }
}
