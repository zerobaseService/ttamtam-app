package com.example.healthcareapp.data

import java.io.Serializable

data class PainRecord(
    val bodyPartName: String,
    val side: String,
    val painLevel: Int,
    val painReason: String? = null
) : Serializable {
    fun sameTarget(other: PainRecord): Boolean =
        bodyPartName == other.bodyPartName && side == other.side
}
