package com.example.healthcareapp

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.data.InviteLinkResponse
import com.example.healthcareapp.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class FolderAdapter(
    val items: MutableList<FolderItem>,
    val onMoreClick: (FolderItem) -> Unit
) : RecyclerView.Adapter<FolderAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tv_folder_title)
        val tvStatus: TextView = view.findViewById(R.id.tv_status)
        val btnMore: View = view.findViewById(R.id.btn_more)
        val lastmodified: TextView = view.findViewById(R.id.tv_last_modified)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.folder1_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val folder = items[position]
        holder.tvTitle.text = folder.name
        holder.lastmodified.text = "최종 수정일: ${folder.lastmodified}"
        if (folder.isShared) {
            holder.tvStatus.text = "공유중"
            holder.tvStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFEB3B")))
        } else {
            holder.tvStatus.text = "비공개"
            holder.tvStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F2F2F2")))
        }
        holder.btnMore.setOnClickListener { onMoreClick(folder) }
    }

    override fun getItemCount() = items.size
}

class FolderActivity : AppCompatActivity() {

    private val folderList = mutableListOf<FolderItem>()
    private lateinit var folderAdapter: FolderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.folder1)

        val emptyView = findViewById<View>(R.id.layout_empty)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView_folders)
        val btnAdd = findViewById<Button>(R.id.btn_add_folder)

        folderAdapter = FolderAdapter(folderList) { clickedFolder ->
            val bottomSheet = FolderOptionSheet(
                folder = clickedFolder,
                onShareClick = { shareFolder(clickedFolder) },
                onEditClick = { showEditDialog(clickedFolder) },
                onExitClick = { exitFolder(clickedFolder) }
            )
            bottomSheet.show(supportFragmentManager, "FolderOptions")
        }

        recyclerView.adapter = folderAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        btnAdd.setOnClickListener {
            val newFolder = FolderItem(
                folderId = 0L, // TODO: replace with real folderId from backend create API
                name = "untitled (${folderList.size + 1})",
                isShared = false,
                lastmodified = getCurrentTime()
            )
            folderList.add(newFolder)
            emptyView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            folderAdapter.notifyItemInserted(folderList.size - 1)
        }
    }

    private fun shareFolder(folder: FolderItem) {
        if (folder.folderId <= 0L) {
            Toast.makeText(this, "폴더를 저장한 후 공유할 수 있습니다.", Toast.LENGTH_SHORT).show()
            return
        }
        RetrofitClient.folderService.createInviteLink(folder.folderId)
            .enqueue(object : Callback<InviteLinkResponse> {
                override fun onResponse(call: Call<InviteLinkResponse>, response: Response<InviteLinkResponse>) {
                    if (response.isSuccessful) {
                        val link = response.body()?.inviteLink ?: return
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
                        Toast.makeText(this@FolderActivity, msg, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<InviteLinkResponse>, t: Throwable) {
                    Toast.makeText(this@FolderActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showEditDialog(folder: FolderItem) {
        val editText = EditText(this)
        editText.setText(folder.name)
        editText.setSelectAllOnFocus(true)
        AlertDialog.Builder(this)
            .setTitle("폴더 이름 변경")
            .setView(editText)
            .setPositiveButton("변경") { _, _ ->
                val newName = editText.text.toString()
                if (newName.isNotEmpty()) {
                    folder.name = newName
                    folder.lastmodified = getCurrentTime()
                    folderAdapter.notifyDataSetChanged()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun exitFolder(folder: FolderItem) {
        val index = folderList.indexOf(folder)
        if (index != -1) {
            folderList.removeAt(index)
            folderAdapter.notifyItemRemoved(index)
            if (folderList.isEmpty()) {
                findViewById<View>(R.id.layout_empty).visibility = View.VISIBLE
            }
        }
    }

    fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA)
        sdf.timeZone = TimeZone.getTimeZone("Asia/Seoul")
        return sdf.format(java.util.Date())
    }
}
