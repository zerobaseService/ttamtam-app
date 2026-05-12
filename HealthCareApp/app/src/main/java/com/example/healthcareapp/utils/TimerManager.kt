package com.example.healthcareapp.utils

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData

object TimerManager {
    private var seconds = 0
    private var isRunning = false
    private val handler = Handler(Looper.getMainLooper())

    val timeLiveData = MutableLiveData<Int>()

    private val runnable = object : Runnable {
        override fun run() {
            if (isRunning) {
                seconds++
                timeLiveData.value = seconds
                handler.postDelayed(this, 1000)
            }
        }
    }

    // 타이머 시작 (중복 실행 방지 및 running 상태 전환)
    fun startTimer() {
        if (!isRunning) {
            isRunning = true
            handler.removeCallbacks(runnable) // 중복 실행 방지를 위한 안전장치
            handler.post(runnable)
        }
    }

    // ⭐ 추가: 타이머 일시정지 (시간은 유지하고 루프만 멈춤)
    fun pauseTimer() {
        isRunning = false
        handler.removeCallbacks(runnable)
    }

    // 타이머 완전히 종료 (초기화)
    fun stopTimer() {
        isRunning = false
        handler.removeCallbacks(runnable)
        seconds = 0
        timeLiveData.value = 0
    }

    fun getFormattedTime(): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return String.format("%02d:%02d:%02d", h, m, s)
    }

    // ⭐ 수정: 현재 타이머가 돌아가고 있는지 확인하는 함수
    fun isRunning(): Boolean = isRunning

    // 타이머 기록이 있는지 확인
    fun isTimerActive(): Boolean = seconds > 0
}