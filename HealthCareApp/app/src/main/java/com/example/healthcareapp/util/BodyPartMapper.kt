package com.example.healthcareapp.util

object BodyPartMapper {

    fun toServerBodyPart(koreanName: String): String = when (koreanName) {
        "경추 (목뼈 부위)" -> "경추"
        "날개(견갑골)" -> "날개"
        else -> koreanName
    }

    fun toDisplayBodyPart(serverValue: String): String = when (serverValue) {
        "경추" -> "경추 (목뼈 부위)"
        "날개" -> "날개(견갑골)"
        else -> serverValue
    }

    fun toServerBodySide(side: String): String = side
}
