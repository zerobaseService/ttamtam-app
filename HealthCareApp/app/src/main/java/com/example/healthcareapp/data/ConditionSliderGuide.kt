package com.example.healthcareapp.data

object ConditionSliderGuide {

    fun guideFor(questionIndex: Int, score: Int): String {
        val description = when (questionIndex) {
            0 -> getPainGuide(score)
            1 -> getSleepTimeGuide(score)
            2 -> getSleepQualityGuide(score)
            3 -> getFatigueGuide(score)
            4 -> getOverallConditionGuide(score)
            else -> "$score 단계"
        }
        return "$score - $description"
    }

    private fun getPainGuide(score: Int): String = when (score) {
        1 -> "매우 심함 / 운동이 어려움"; 2 -> "일상 움직임도 불편함"; 3 -> "운동 시 불편이 큼"; 4 -> "움직일 때 거슬리는 수준"; 5 -> "통증이 분명히 느껴짐"
        6 -> "신경은 쓰이지만 운동 가능"; 7 -> "약간 불편한 정도"; 8 -> "아주 약하게 느껴짐"; 9 -> "거의 느껴지지 않음"; 10 -> "통증 없음"
        else -> ""
    }

    private fun getSleepTimeGuide(score: Int): String = when (score) {
        1 -> "1시간 수준 / 거의 못 잠"; 2 -> "2시간 / 매우 부족"; 3 -> "3시간 / 많이 부족"; 4 -> "4시간 / 부족"; 5 -> "5시간 / 약간 부족"
        6 -> "6시간 / 다소 부족"; 7 -> "7시간 / 보통"; 8 -> "8시간 / 적절"; 9 -> "9시간 / 충분"; 10 -> "10시간 / 매우 충분"
        else -> ""
    }

    private fun getSleepQualityGuide(score: Int): String = when (score) {
        1 -> "거의 못 잠"; 2 -> "자주 깨고 매우 피곤함"; 3 -> "여러 번 깨고 피로함"; 4 -> "뒤척임 많고 개운하지 않음"; 5 -> "잤지만 개운하지 않음"
        6 -> "보통"; 7 -> "비교적 잘 잠"; 8 -> "깊게 잔 편"; 9 -> "거의 안 깨고 개운함"; 10 -> "푹 자고 매우 개운함"
        else -> ""
    }

    private fun getFatigueGuide(score: Int): String = when (score) {
        1 -> "매우 많이 남아있음"; 2 -> "많이 남아있음"; 3 -> "꽤 남아있음"; 4 -> "남아있는 편"; 5 -> "어느 정도 남아있음"
        6 -> "조금 남아있음"; 7 -> "약간 남아있음"; 8 -> "거의 없음"; 9 -> "아주 미세함"; 10 -> "전혀 없음"
        else -> ""
    }

    private fun getOverallConditionGuide(score: Int): String = when (score) {
        1 -> "매우 안 좋음 / 많이 지치고 힘든 상태"; 2 -> "많이 안 좋음 / 몸과 마음이 무거운 상태"; 3 -> "안 좋은 편 / 피로감이 큰 상태"
        4 -> "다소 안 좋음 / 불편하고 무거운 느낌"; 5 -> "보통 이하 / 썩 좋지는 않은 상태"; 6 -> "무난함 / 크게 나쁘지 않은 상태"
        7 -> "괜찮은 편 / 비교적 안정된 상태"; 8 -> "좋은 편 / 몸과 마음이 비교적 가벼움"; 9 -> "매우 좋음 / 활력이 있고 안정적임"; 10 -> "최상 / 몸과 마음이 매우 가볍고 개운함"
        else -> ""
    }
}
