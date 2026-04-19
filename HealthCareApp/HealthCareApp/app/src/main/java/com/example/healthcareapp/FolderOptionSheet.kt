package com.example.healthcareapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
class FolderOptionSheet(
    val folder: FolderItem,           // 1. 폴더 객체
    val onShareClick: () -> Unit,     // 2. 공유 클릭 리스너
    val onEditClick: () -> Unit,      // 3. 수정 클릭 리스너
    val onExitClick: () -> Unit       // 4. 나가기 클릭 리스너
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.folder2, container, false)

        // 1. 공유하기 버튼 연결
        view.findViewById<View>(R.id.btn_share).setOnClickListener {
            onShareClick()
            dismiss() // 클릭 후 바텀시트 닫기
        }

        // 2. 이름 변경 버튼 연결
        view.findViewById<View>(R.id.btn_edit_name).setOnClickListener {
            onEditClick()
            dismiss()
        }

        // 3. 폴더 나가기 버튼 연결
        view.findViewById<View>(R.id.btn_exit).setOnClickListener {
            onExitClick()
            dismiss()
        }

        return view
    }
}