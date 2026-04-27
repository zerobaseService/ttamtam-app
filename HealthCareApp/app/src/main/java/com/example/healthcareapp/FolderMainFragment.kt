//package com.example.healthcareapp
//
//
//import android.app.AlertDialog
//import android.graphics.Color
//import android.graphics.Typeface
//import android.os.Bundle
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Button
//import android.widget.CheckBox
//import android.widget.EditText
//import android.widget.ImageView
//import android.widget.TextView
//
//
//import androidx.fragment.app.Fragment
//
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.example.healthcareapp.adapter.FolderAdapter
//
//
//import java.text.SimpleDateFormat
//import java.util.Date
//import java.util.Locale
//
//class FolderMainFragment : Fragment() {
//
//    private val allFolders = mutableListOf<FolderItem>()
//    private lateinit var folderAdapter: FolderAdapter
//
//    private lateinit var emptyView: View
//    private lateinit var recyclerView: RecyclerView
//    private lateinit var cbFilterShared: ImageView
//    private lateinit var tvSortModified: TextView
//    private lateinit var tvSortCreated: TextView
//    private lateinit var btnAddFolder: ImageView
//    private lateinit var btnMakeFolderEmpty: Button
//    private lateinit var layoutSortFilter: View
//
//    private var currentSortType = "최근수정일순"
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        val view = inflater.inflate(R.layout.folder_list_test2, container, false)
//
//        initViews(view)
//        setupAdapter()
//        setupListeners()
//        applyFilterAndSort()
//
//        return view
//    }
//
//    private fun initViews(view: View) {
//        emptyView = view.findViewById(R.id.layout_empty)
//        recyclerView = view.findViewById(R.id.folder_list)
//        cbFilterShared = view.findViewById(R.id.filter_shared)
//        tvSortModified = view.findViewById(R.id.sort_modified)
//        tvSortCreated = view.findViewById(R.id.sort_created)
//        btnAddFolder = view.findViewById(R.id.btn_add_folder)
//        btnMakeFolderEmpty = view.findViewById(R.id.btn_make_empty)
//        layoutSortFilter = view.findViewById(R.id.layout_sort_filter)
//    }
//
//    private fun setupAdapter() {
//        folderAdapter = FolderAdapter(mutableListOf()) { clickedFolder ->
//            // 1단계: 기본 옵션 바텀시트 띄우기
//            val bottomSheet = FolderOptionSheet(
//                folder = clickedFolder,
//                onLinkClick = {
//
//                    showShareSheet(clickedFolder)
//                },
//                onEditClick = { showEditSheet(clickedFolder) },
//                onExitClick = {
//
//                    showExitSheet(clickedFolder)
//                }
//            )
//            bottomSheet.show(parentFragmentManager, "FolderOptions")
//        }
//        recyclerView.adapter = folderAdapter
//        recyclerView.layoutManager = LinearLayoutManager(requireContext())
//    }
//
//
//    private fun showShareSheet(folder: FolderItem) {
//        val shareSheet = FolderShareSheet(folder.name) {
//
//            folder.isShared = true
//            folder.status = "공유중"
//            applyFilterAndSort()
//
//        }
//        shareSheet.show(parentFragmentManager, "FolderShare")
//    }
//
//
//    private fun showExitSheet(folder: FolderItem) {
//        val exitSheet = FolderExitSheet(folder.name) {
//
//            allFolders.remove(folder)
//            applyFilterAndSort()
//        }
//        exitSheet.show(parentFragmentManager, "FolderExit")
//    }
//    private var isFilterSharedChecked = false
//    private fun setupListeners() {
//        cbFilterShared.setOnClickListener {
//            isFilterSharedChecked = !isFilterSharedChecked
//
//
//            cbFilterShared.isSelected = isFilterSharedChecked
//
//            applyFilterAndSort()
//        }
//
//        tvSortModified.setOnClickListener {
//            currentSortType = "최근수정일순"
//            applyFilterAndSort()
//        }
//
//        tvSortCreated.setOnClickListener {
//            currentSortType = "생성일순"
//            applyFilterAndSort()
//        }
//
//        btnAddFolder.setOnClickListener { addNewFolder() }
//        btnMakeFolderEmpty.setOnClickListener { addNewFolder() }
//    }
//
//    private fun applyFilterAndSort() {
//        updateSortUI()
//
//
//        var displayList = if (isFilterSharedChecked) {
//            allFolders.filter { it.isShared }
//        } else {
//            allFolders
//        }
//
//
//        displayList = when (currentSortType) {
//            "최근수정일순" -> displayList.sortedByDescending { it.lastmodified }
//            "생성일순" -> displayList.sortedByDescending { it.createdAt }
//            else -> displayList
//        }
//
//        folderAdapter.updateData(displayList)
//
//
//        if (allFolders.isEmpty()) {
//
//            emptyView.visibility = View.VISIBLE
//            recyclerView.visibility = View.GONE
//            layoutSortFilter.visibility = View.GONE // 상단바도 숨김
//        } else {
//
//            emptyView.visibility = View.GONE
//
//
//            layoutSortFilter.visibility = View.VISIBLE
//            recyclerView.visibility = View.VISIBLE
//        }
//    }
//
//    private fun updateSortUI() {
//        val activeColor = Color.BLACK
//        val inactiveColor = Color.parseColor("#AAAAAA")
//
//        if (currentSortType == "최근수정일순") {
//            tvSortModified.setTextColor(activeColor)
//            tvSortModified.setTypeface(null, Typeface.BOLD)
//            tvSortCreated.setTextColor(inactiveColor)
//            tvSortCreated.setTypeface(null, Typeface.NORMAL)
//        } else {
//            tvSortCreated.setTextColor(activeColor)
//            tvSortCreated.setTypeface(null, Typeface.BOLD)
//            tvSortModified.setTextColor(inactiveColor)
//            tvSortModified.setTypeface(null, Typeface.NORMAL)
//        }
//    }
//
//    private fun addNewFolder() {
//
//        val confirmSheet = FolderAddConfirmSheet {
//
//            val newFolder = FolderItem(
//                name = "untitled${allFolders.size + 1}",
//                lastmodified = getCurrentTime(),
//                status = "공유대기",
//                isShared = false,
//                createdAt = System.currentTimeMillis()
//            )
//            allFolders.add(newFolder)
//            applyFilterAndSort()
//
//
//            val completeSheet = FolderAddCompleteSheet()
//            completeSheet.show(parentFragmentManager, "AddComplete")
//        }
//
//        confirmSheet.show(parentFragmentManager, "AddConfirm")
//    }
//
//    private fun showEditDialog(folder: FolderItem) {
//        val editText = EditText(requireContext())
//        editText.setText(folder.name)
//        AlertDialog.Builder(requireContext())
//            .setTitle("폴더 이름 변경")
//            .setView(editText)
//            .setPositiveButton("변경") { _, _ ->
//                val newName = editText.text.toString()
//                if (newName.isNotEmpty()) {
//                    folder.name = newName
//                    folder.lastmodified = getCurrentTime()
//                    applyFilterAndSort()
//                }
//            }
//            .setNegativeButton("취소", null)
//            .show()
//    }
//    private fun showEditSheet(folder: FolderItem) {
//        val editSheet = FolderEditSheet(folder.name) { newName ->
//
//            folder.name = newName
//            folder.lastmodified = getCurrentTime()
//            applyFilterAndSort()
//        }
//        editSheet.show(parentFragmentManager, "FolderEdit")
//    }
//
//    private fun getCurrentTime(): String {
//        return SimpleDateFormat("yy.MM.dd", Locale.KOREA).format(Date())
//    }
//}
