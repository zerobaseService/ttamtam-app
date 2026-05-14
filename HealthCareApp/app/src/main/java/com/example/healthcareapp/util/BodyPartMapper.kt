package com.example.healthcareapp.util

object BodyPartMapper {

    fun toServerBodyPart(koreanName: String): String = when (koreanName) {
        "머리", "이마", "얼굴" -> "HEAD"
        "목", "경추 (목뼈 부위)" -> "NECK"
        "어깨", "날개(견갑골)" -> "SHOULDER"
        "가슴" -> "CHEST"
        "등" -> "BACK"
        "윗배", "아랫배", "옆구리" -> "ABDOMEN"
        "허리", "꼬리뼈" -> "WAIST"
        "윗팔" -> "ARM_UPPER"
        "아랫팔" -> "ARM_LOWER"
        "팔꿈치", "오금" -> "ELBOW"
        "손목" -> "WRIST"
        "손바닥" -> "HAND"
        "손가락" -> "FINGER"
        "고관절", "엉덩이" -> "HIP"
        "사타구니" -> "GROIN"
        "생식기" -> "GENITALS"
        "허벅지", "뒷허벅지" -> "THIGH"
        "무릎" -> "KNEE"
        "정강이", "종아리" -> "SHIN"
        "발목", "아킬레스건" -> "ANKLE"
        "발등", "발바닥" -> "FOOT"
        "발가락" -> "TOE"
        else -> "HEAD"
    }

    fun toServerBodySide(side: String): String = when (side) {
        "좌" -> "LEFT"
        "우" -> "RIGHT"
        else -> "CENTER"
    }
}
