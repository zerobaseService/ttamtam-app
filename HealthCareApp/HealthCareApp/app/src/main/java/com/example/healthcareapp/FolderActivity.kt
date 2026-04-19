package com.example.healthcareapp

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

import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.dynamic.SupportFragmentWrapper
import java.sql.Date
import java.util.Locale

class FolderAdapter(
    val items: MutableList<FolderItem>,
    val onMoreClick: (FolderItem) -> Unit
) : RecyclerView.Adapter<FolderAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tv_folder_title)
        val tvStatus : TextView = view.findViewById(R.id.tv_status)
        val btnMore: View = view.findViewById(R.id.btn_more)
        val lastmodified : TextView = view.findViewById(R.id.tv_last_modified)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.folder1_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val folderName = items[position]
        holder.tvTitle.text = folderName.name
        holder.lastmodified.text = "최종 수정일: ${folderName.lastmodified}"

        if(folderName.isShared){
            holder.tvStatus.text = "공유중"
            holder.tvStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFEB3B"))) // 연파랑

        }
        else{
            holder.tvStatus.text = "비공개"
            holder.tvStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F2F2F2"))) // 회색

        }

        holder.btnMore.setOnClickListener {
            onMoreClick(folderName)
        }
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

        // 2. 어댑터 연결 (클릭 시 folder 객체 자체를 넘깁니다)
        folderAdapter = FolderAdapter(folderList) { clickedFolder ->
            // FolderOptionSheet 생성자에 객체와 리스너들을 전달
            val bottomSheet = FolderOptionSheet(
                folder = clickedFolder,
                onShareClick = { toggleShareStatus(clickedFolder) },
                onEditClick = { showEditDialog(clickedFolder) },
                onExitClick = { exitFolder(clickedFolder) }
            )
            bottomSheet.show(supportFragmentManager, "FolderOptions")
        }

        recyclerView.adapter = folderAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)


        btnAdd.setOnClickListener {
            val newFolder = FolderItem(
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

    // 이름 변경 다이얼로그
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
                    folder.name = newName // 객체 내부 이름 변경
                    folder.lastmodified = getCurrentTime() // 수정일 갱신
                    folderAdapter.notifyDataSetChanged()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    // 공유 상태 토글 기능
    private fun toggleShareStatus(folder: FolderItem) {
        folder.isShared = !folder.isShared
        folderAdapter.notifyDataSetChanged()
    }

    // 폴더 나가기 기능
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

    // 이름 변경 시 호출
    private fun updateFolderName(folder: FolderItem, newName: String) {
        folder.name = newName
        folder.lastmodified = getCurrentTime() // 하드코딩 대신 진짜 현재 시간!
        folderAdapter.notifyDataSetChanged()
    }
}
