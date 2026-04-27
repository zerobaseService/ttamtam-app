package com.example.healthcareapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.adapter.FolderAdapter
import com.example.healthcareapp.data.ApiResponse
import com.example.healthcareapp.data.CreateFolderRequest
import com.example.healthcareapp.data.CreateFolderResponse
import com.example.healthcareapp.data.FolderListResponse
import com.example.healthcareapp.data.InviteLinkResponse
import com.example.healthcareapp.data.UpdateFolderRequest
import com.example.healthcareapp.data.UpdateFolderResponse
import com.example.healthcareapp.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FolderActivity2 : AppCompatActivity() {

    private val folderList = mutableListOf<FolderItem>()
    private lateinit var folderAdapter: FolderAdapter
    private lateinit var emptyView: View
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.folder_list_test2)

        emptyView = findViewById(R.id.layout_empty)
        recyclerView = findViewById(R.id.folder_list)

        folderAdapter = FolderAdapter(folderList) { clickedFolder ->
            val bottomSheet = FolderOptionSheet(
                folder = clickedFolder,
                onLinkClick = { shareFolder(clickedFolder) },
                onEditClick = { showEditDialog(clickedFolder) },
                onExitClick = { confirmAndExit(clickedFolder) }
            )
            bottomSheet.show(supportFragmentManager, "FolderOptions")
        }

        recyclerView.adapter = folderAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        findViewById<View>(R.id.btn_add_folder).setOnClickListener {
            showCreateDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        loadFolders()
    }

    private fun loadFolders() {
        RetrofitClient.folderService.getFolders()
            .enqueue(object : Callback<ApiResponse<FolderListResponse>> {
                override fun onResponse(call: Call<ApiResponse<FolderListResponse>>, response: Response<ApiResponse<FolderListResponse>>) {
                    if (response.isSuccessful) {
                        val folders = response.body()?.data?.folders ?: return
                        folderList.clear()
                        folders.forEach { dto ->
                            folderList.add(FolderItem(
                                folderId = dto.folderId,
                                name = dto.name,
                                isShared = dto.isShared,
                                lastmodified = formatDate(dto.updatedAt ?: dto.createdAt)
                            ))
                        }
                        folderAdapter.notifyDataSetChanged()
                        updateEmptyView()
                    } else {
                        Toast.makeText(this@FolderActivity2, "목록 조회 실패 (${response.code()})", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse<FolderListResponse>>, t: Throwable) {
                    Toast.makeText(this@FolderActivity2, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showCreateDialog() {
        val editText = EditText(this).apply {
            hint = "폴더 이름 (최대 18자)"
        }
        AlertDialog.Builder(this)
            .setTitle("새 폴더 만들기")
            .setView(editText)
            .setPositiveButton("만들기") { _, _ ->
                val name = editText.text.toString().trim()
                if (name.isEmpty()) {
                    Toast.makeText(this, "폴더 이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
                } else {
                    createFolder(name)
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun createFolder(name: String) {
        RetrofitClient.folderService.createFolder(CreateFolderRequest(name))
            .enqueue(object : Callback<ApiResponse<CreateFolderResponse>> {
                override fun onResponse(call: Call<ApiResponse<CreateFolderResponse>>, response: Response<ApiResponse<CreateFolderResponse>>) {
                    if (response.isSuccessful) {
                        val body = response.body()?.data ?: return
                        folderList.add(FolderItem(
                            folderId = body.folderId,
                            name = body.name,
                            isShared = body.isShared,
                            lastmodified = formatDate(body.createdAt)
                        ))
                        folderAdapter.notifyItemInserted(folderList.size - 1)
                        updateEmptyView()
                    } else {
                        val msg = when (response.code()) {
                            400 -> "폴더 이름이 올바르지 않습니다."
                            else -> "폴더 생성 실패 (${response.code()})"
                        }
                        Toast.makeText(this@FolderActivity2, msg, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse<CreateFolderResponse>>, t: Throwable) {
                    Toast.makeText(this@FolderActivity2, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun shareFolder(folder: FolderItem) {
        RetrofitClient.folderService.createInviteLink(folder.folderId)
            .enqueue(object : Callback<ApiResponse<InviteLinkResponse>> {
                override fun onResponse(call: Call<ApiResponse<InviteLinkResponse>>, response: Response<ApiResponse<InviteLinkResponse>>) {
                    if (response.isSuccessful) {
                        val link = response.body()?.data?.inviteLink ?: return
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, link)
                        }
                        startActivity(Intent.createChooser(shareIntent, "초대 링크 공유"))
                    } else {
                        val msg = when (response.code()) {
                            403 -> "폴더 접근 권한이 없습니다."
                            409 -> "이미 2명이 참여 중입니다."
                            400 -> "닫힌 폴더입니다."
                            else -> "초대 링크 생성 실패 (${response.code()})"
                        }
                        Toast.makeText(this@FolderActivity2, msg, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse<InviteLinkResponse>>, t: Throwable) {
                    Toast.makeText(this@FolderActivity2, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showEditDialog(folder: FolderItem) {
        val editText = EditText(this).apply {
            setText(folder.name)
            setSelectAllOnFocus(true)
        }
        AlertDialog.Builder(this)
            .setTitle("폴더 이름 변경")
            .setView(editText)
            .setPositiveButton("변경") { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isEmpty()) {
                    Toast.makeText(this, "폴더 이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
                } else {
                    updateFolderName(folder, newName)
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun updateFolderName(folder: FolderItem, newName: String) {
        RetrofitClient.folderService.updateFolderName(folder.folderId, UpdateFolderRequest(newName))
            .enqueue(object : Callback<ApiResponse<UpdateFolderResponse>> {
                override fun onResponse(call: Call<ApiResponse<UpdateFolderResponse>>, response: Response<ApiResponse<UpdateFolderResponse>>) {
                    if (response.isSuccessful) {
                        val body = response.body()?.data ?: return
                        val index = folderList.indexOf(folder)
                        if (index != -1) {
                            folder.name = body.name
                            folder.lastmodified = formatDate(body.updatedAt ?: body.createdAt)
                            folderAdapter.notifyItemChanged(index)
                        }
                    } else {
                        val msg = when (response.code()) {
                            400 -> "폴더 이름이 올바르지 않습니다."
                            403 -> "권한이 없습니다."
                            else -> "이름 변경 실패 (${response.code()})"
                        }
                        Toast.makeText(this@FolderActivity2, msg, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse<UpdateFolderResponse>>, t: Throwable) {
                    Toast.makeText(this@FolderActivity2, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun confirmAndExit(folder: FolderItem) {
        AlertDialog.Builder(this)
            .setTitle("폴더 나가기")
            .setMessage("'${folder.name}' 폴더에서 나가시겠습니까?")
            .setPositiveButton("나가기") { _, _ -> leaveFolder(folder) }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun leaveFolder(folder: FolderItem) {
        RetrofitClient.folderService.leaveFolder(folder.folderId)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        val index = folderList.indexOf(folder)
                        if (index != -1) {
                            folderList.removeAt(index)
                            folderAdapter.notifyItemRemoved(index)
                            updateEmptyView()
                        }
                        Toast.makeText(this@FolderActivity2, "폴더에서 나갔습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        val msg = when (response.code()) {
                            400 -> "이미 나간 폴더입니다."
                            403 -> "권한이 없습니다."
                            else -> "나가기 실패 (${response.code()})"
                        }
                        Toast.makeText(this@FolderActivity2, msg, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@FolderActivity2, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun updateEmptyView() {
        if (folderList.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun formatDate(dateStr: String?): String {
        if (dateStr == null) return ""
        return dateStr.replace("T", " ").take(16)
    }
}
