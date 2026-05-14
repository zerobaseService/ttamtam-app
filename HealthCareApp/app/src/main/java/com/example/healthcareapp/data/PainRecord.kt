package com.example.healthcareapp.data

import java.io.Serializable

data class PainRecord(
    val bodyPartName: String, // 한국어 부위명 (표시용)
    val side: String,         // "좌" or "우"
    val painLevel: Int        // 1~5
) : Serializable
