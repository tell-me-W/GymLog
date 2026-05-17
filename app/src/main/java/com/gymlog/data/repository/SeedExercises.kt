package com.gymlog.data.repository

import com.gymlog.data.local.ExerciseEntity

object SeedExercises {
    val defaults = listOf(
        ExerciseEntity(name = "벤치프레스", targetArea = "가슴", defaultRestSeconds = 120),
        ExerciseEntity(name = "인클라인 벤치프레스", targetArea = "가슴", defaultRestSeconds = 120),
        ExerciseEntity(name = "덤벨 프레스", targetArea = "가슴", defaultRestSeconds = 90),
        ExerciseEntity(name = "체스트 프레스", targetArea = "가슴", defaultRestSeconds = 90),
        ExerciseEntity(name = "케이블 플라이", targetArea = "가슴", defaultRestSeconds = 60),
        ExerciseEntity(name = "푸시업", targetArea = "가슴", defaultRestSeconds = 60),
        ExerciseEntity(name = "풀업", targetArea = "등", defaultRestSeconds = 120),
        ExerciseEntity(name = "랫풀다운", targetArea = "등", defaultRestSeconds = 90),
        ExerciseEntity(name = "바벨 로우", targetArea = "등", defaultRestSeconds = 120),
        ExerciseEntity(name = "시티드 로우", targetArea = "등", defaultRestSeconds = 90),
        ExerciseEntity(name = "원암 덤벨 로우", targetArea = "등", defaultRestSeconds = 90),
        ExerciseEntity(name = "데드리프트", targetArea = "등", defaultRestSeconds = 180),
        ExerciseEntity(name = "스쿼트", targetArea = "하체", defaultRestSeconds = 180),
        ExerciseEntity(name = "레그 프레스", targetArea = "하체", defaultRestSeconds = 120),
        ExerciseEntity(name = "런지", targetArea = "하체", defaultRestSeconds = 90),
        ExerciseEntity(name = "레그 익스텐션", targetArea = "하체", defaultRestSeconds = 60),
        ExerciseEntity(name = "레그 컬", targetArea = "하체", defaultRestSeconds = 60),
        ExerciseEntity(name = "카프 레이즈", targetArea = "하체", defaultRestSeconds = 60),
        ExerciseEntity(name = "오버헤드 프레스", targetArea = "어깨", defaultRestSeconds = 120),
        ExerciseEntity(name = "덤벨 숄더 프레스", targetArea = "어깨", defaultRestSeconds = 90),
        ExerciseEntity(name = "사이드 레터럴 레이즈", targetArea = "어깨", defaultRestSeconds = 60),
        ExerciseEntity(name = "리어 델트 플라이", targetArea = "어깨", defaultRestSeconds = 60),
        ExerciseEntity(name = "프론트 레이즈", targetArea = "어깨", defaultRestSeconds = 60),
        ExerciseEntity(name = "바벨 컬", targetArea = "팔", defaultRestSeconds = 60),
        ExerciseEntity(name = "덤벨 컬", targetArea = "팔", defaultRestSeconds = 60),
        ExerciseEntity(name = "해머 컬", targetArea = "팔", defaultRestSeconds = 60),
        ExerciseEntity(name = "트라이셉스 푸시다운", targetArea = "팔", defaultRestSeconds = 60),
        ExerciseEntity(name = "라잉 트라이셉스 익스텐션", targetArea = "팔", defaultRestSeconds = 60),
        ExerciseEntity(name = "딥스", targetArea = "팔", defaultRestSeconds = 90),
        ExerciseEntity(name = "크런치", targetArea = "복근", defaultRestSeconds = 45),
        ExerciseEntity(name = "레그 레이즈", targetArea = "복근", defaultRestSeconds = 45),
        ExerciseEntity(name = "플랭크", targetArea = "복근", defaultRestSeconds = 45),
        ExerciseEntity(name = "러시안 트위스트", targetArea = "복근", defaultRestSeconds = 45),
        ExerciseEntity(name = "행잉 니 레이즈", targetArea = "복근", defaultRestSeconds = 60),
    )

    const val ALL_TARGET_AREA = "전체"

    val exerciseTargetAreas = listOf("가슴", "등", "하체", "어깨", "팔", "복근")
    val targetAreas = listOf(ALL_TARGET_AREA) + exerciseTargetAreas

    fun queryTargetOrNull(targetArea: String): String? {
        return targetArea.takeUnless { it == ALL_TARGET_AREA }
    }
}
