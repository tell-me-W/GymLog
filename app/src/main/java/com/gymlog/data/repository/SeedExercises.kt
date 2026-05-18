package com.gymlog.data.repository

import com.gymlog.data.local.ExerciseEntity
import com.gymlog.data.local.ExerciseInputType

object SeedExercises {
    val defaults = listOf(
    // 가슴
    ExerciseEntity(name = "체스트 프레스 머신", targetArea = "가슴", defaultRestSeconds = 90),
    ExerciseEntity(name = "인클라인 체스트 프레스 머신", targetArea = "가슴", defaultRestSeconds = 90),
    ExerciseEntity(name = "펙 덱 플라이", targetArea = "가슴", defaultRestSeconds = 60),
    ExerciseEntity(name = "디클라인 체스트 프레스 머신", targetArea = "가슴", defaultRestSeconds = 90),
    ExerciseEntity(name = "스미스 머신 인클라인 벤치 프레스", targetArea = "가슴", defaultRestSeconds = 120),
    ExerciseEntity(name = "벤치 프레스", targetArea = "가슴", defaultRestSeconds = 120),
    ExerciseEntity(name = "케이블 크로스오버", targetArea = "가슴", defaultRestSeconds = 60),
    ExerciseEntity(name = "어시스트 딥스", targetArea = "가슴", defaultRestSeconds = 90),
    ExerciseEntity(name = "스미스 머신 벤치 프레스", targetArea = "가슴", defaultRestSeconds = 120),
    ExerciseEntity(name = "덤벨 인클라인 벤치 프레스", targetArea = "가슴", defaultRestSeconds = 120),
    ExerciseEntity(name = "딥스", targetArea = "가슴", defaultRestSeconds = 90),
    ExerciseEntity(name = "인클라인 벤치 프레스", targetArea = "가슴", defaultRestSeconds = 120),
    ExerciseEntity(name = "덤벨 벤치 프레스", targetArea = "가슴", defaultRestSeconds = 120),
    ExerciseEntity(name = "푸쉬업 (팔굽혀펴기)", targetArea = "가슴", defaultRestSeconds = 60),
    ExerciseEntity(name = "케이블 플라이", targetArea = "가슴", defaultRestSeconds = 60),
    ExerciseEntity(name = "인클라인 벤치 프레스 머신", targetArea = "가슴", defaultRestSeconds = 90),
    ExerciseEntity(name = "덤벨 플라이", targetArea = "가슴", defaultRestSeconds = 60),
    ExerciseEntity(name = "인클라인 덤벨 플라이", targetArea = "가슴", defaultRestSeconds = 60),
    ExerciseEntity(name = "덤벨 풀오버", targetArea = "가슴", defaultRestSeconds = 90),
    ExerciseEntity(name = "벤치 프레스 머신", targetArea = "가슴", defaultRestSeconds = 90),
    ExerciseEntity(name = "시티드 딥스 머신", targetArea = "가슴", defaultRestSeconds = 90),
    ExerciseEntity(name = "덤벨 플라이 (플랫벤치)", targetArea = "가슴", defaultRestSeconds = 60),
    ExerciseEntity(name = "디클라인 케이블 플라이", targetArea = "가슴", defaultRestSeconds = 60),
    ExerciseEntity(name = "인클라인 푸쉬업", targetArea = "가슴", defaultRestSeconds = 60),
    ExerciseEntity(name = "케이블 어퍼 플라이", targetArea = "가슴", defaultRestSeconds = 60),

    // 등
    ExerciseEntity(name = "와이드 풀다운", targetArea = "등", defaultRestSeconds = 90),
    ExerciseEntity(name = "어시스트 풀업 머신", targetArea = "등", defaultRestSeconds = 90),
    ExerciseEntity(name = "케이블 암 풀다운", targetArea = "등", defaultRestSeconds = 60),
    ExerciseEntity(name = "시티드 케이블 로우 (롱 풀)", targetArea = "등", defaultRestSeconds = 90),
    ExerciseEntity(name = "맥 그립 랫 풀다운", targetArea = "등", defaultRestSeconds = 90),
    ExerciseEntity(name = "바벨 로우", targetArea = "등", defaultRestSeconds = 120),
    ExerciseEntity(name = "티바 로우", targetArea = "등", defaultRestSeconds = 120),
    ExerciseEntity(name = "원암 시티드 로우", targetArea = "등", defaultRestSeconds = 90),
    ExerciseEntity(name = "랫 풀다운", targetArea = "등", defaultRestSeconds = 90),
    ExerciseEntity(name = "원암 덤벨 로우", targetArea = "등", defaultRestSeconds = 90),
    ExerciseEntity(name = "덤벨 로우", targetArea = "등", defaultRestSeconds = 90),
    ExerciseEntity(name = "케이블 로우", targetArea = "등", defaultRestSeconds = 90),
    ExerciseEntity(name = "와이드 그립 랫 풀다운", targetArea = "등", defaultRestSeconds = 90),
    ExerciseEntity(name = "랫 풀다운 머신", targetArea = "등", defaultRestSeconds = 90),
    ExerciseEntity(name = "시티드 로우 머신", targetArea = "등", defaultRestSeconds = 90),
    ExerciseEntity(name = "언더그립 바벨 로우", targetArea = "등", defaultRestSeconds = 120),
    ExerciseEntity(name = "풀업 (턱걸이)", targetArea = "등", defaultRestSeconds = 120),
    ExerciseEntity(name = "하이 로우 머신", targetArea = "등", defaultRestSeconds = 90),
    ExerciseEntity(name = "클로스 그립 랫 풀다운", targetArea = "등", defaultRestSeconds = 90),
    ExerciseEntity(name = "루마니안 데드리프트", targetArea = "등", defaultRestSeconds = 180),
    ExerciseEntity(name = "체스트 서포티드 티바 로우", targetArea = "등", defaultRestSeconds = 90),

    // 하체
    ExerciseEntity(name = "몬스터 글루트 (링크 아웃타이)", targetArea = "하체", defaultRestSeconds = 60),
    ExerciseEntity(name = "스플릿 스쿼트", targetArea = "하체", defaultRestSeconds = 90),
    ExerciseEntity(name = "시티드 레그 컬", targetArea = "하체", defaultRestSeconds = 60),
    ExerciseEntity(name = "리버스 핵 스쿼트", targetArea = "하체", defaultRestSeconds = 120),
    ExerciseEntity(name = "레그 프레스", targetArea = "하체", defaultRestSeconds = 120),
    ExerciseEntity(name = "카프 레이즈", targetArea = "하체", defaultRestSeconds = 60),
    ExerciseEntity(name = "스미스 머신 스쿼트", targetArea = "하체", defaultRestSeconds = 120),
    ExerciseEntity(name = "힙 트러스트 머신", targetArea = "하체", defaultRestSeconds = 90),
    ExerciseEntity(name = "리버스 하이퍼", targetArea = "하체", defaultRestSeconds = 60),
    ExerciseEntity(name = "힙 어브덕션 (아웃타이)", targetArea = "하체", defaultRestSeconds = 60),
    ExerciseEntity(name = "덤벨 런지", targetArea = "하체", defaultRestSeconds = 90),
    ExerciseEntity(name = "리버스 브이 스쿼트", targetArea = "하체", defaultRestSeconds = 90),
    ExerciseEntity(name = "레그 익스텐션", targetArea = "하체", defaultRestSeconds = 60),
    ExerciseEntity(name = "힙 어덕션 (이너타이)", targetArea = "하체", defaultRestSeconds = 60),
    ExerciseEntity(name = "고블릿 스쿼트", targetArea = "하체", defaultRestSeconds = 90),
    ExerciseEntity(name = "레그 프레스 머신", targetArea = "하체", defaultRestSeconds = 120),
    ExerciseEntity(name = "데드리프트", targetArea = "하체", defaultRestSeconds = 180),
    ExerciseEntity(name = "스쿼트", targetArea = "하체", defaultRestSeconds = 180),
    ExerciseEntity(name = "오버헤드스쿼트", targetArea = "하체", defaultRestSeconds = 120),
    ExerciseEntity(name = "레그 컬", targetArea = "하체", defaultRestSeconds = 60),
    ExerciseEntity(name = "스티프레그 데드리프트", targetArea = "하체", defaultRestSeconds = 120),
    ExerciseEntity(name = "스탠딩 카프 레이즈", targetArea = "하체", defaultRestSeconds = 60),
    ExerciseEntity(name = "핵 스쿼트", targetArea = "하체", defaultRestSeconds = 120),
    ExerciseEntity(name = "불가리안 스플릿 스쿼트", targetArea = "하체", defaultRestSeconds = 90),
    ExerciseEntity(name = "시티드 레그 프레스", targetArea = "하체", defaultRestSeconds = 120),
    ExerciseEntity(name = "브이 스쿼트", targetArea = "하체", defaultRestSeconds = 120),
    ExerciseEntity(name = "프론트 스쿼트", targetArea = "하체", defaultRestSeconds = 150),
    ExerciseEntity(name = "런지", targetArea = "하체", defaultRestSeconds = 90),
    ExerciseEntity(name = "덤벨 스티프레그 데드리프트", targetArea = "하체", defaultRestSeconds = 120),

    // 어깨
    ExerciseEntity(name = "숄더 프레스 머신", targetArea = "어깨", defaultRestSeconds = 90),
    ExerciseEntity(name = "밴드 풀어파트", targetArea = "어깨", defaultRestSeconds = 60),
    ExerciseEntity(name = "리버스 펙 덱 플라이", targetArea = "어깨", defaultRestSeconds = 60),
    ExerciseEntity(name = "덤벨 사이드 레터럴 레이즈", targetArea = "어깨", defaultRestSeconds = 60),
    ExerciseEntity(name = "덤벨 스탠딩 숄더 프레스", targetArea = "어깨", defaultRestSeconds = 90),
    ExerciseEntity(name = "월 슬라이드", targetArea = "어깨", defaultRestSeconds = 45),
    ExerciseEntity(name = "스미스 머신 숄더 프레스", targetArea = "어깨", defaultRestSeconds = 120),
    ExerciseEntity(name = "밴드 후면 어깨", targetArea = "어깨", defaultRestSeconds = 45),
    ExerciseEntity(name = "원 암 덤벨 숄더 프레스", targetArea = "어깨", defaultRestSeconds = 90),
    ExerciseEntity(name = "덤벨 숄더 프레스", targetArea = "어깨", defaultRestSeconds = 90),
    ExerciseEntity(name = "시티드 덤벨 벤트 오버 레터럴 레이즈", targetArea = "어깨", defaultRestSeconds = 60),
    ExerciseEntity(name = "케이블 업라이트 로우", targetArea = "어깨", defaultRestSeconds = 60),
    ExerciseEntity(name = "페이스 풀", targetArea = "어깨", defaultRestSeconds = 60),
    ExerciseEntity(name = "바벨 업라이트 로우", targetArea = "어깨", defaultRestSeconds = 90),
    ExerciseEntity(name = "오버헤드 프레스", targetArea = "어깨", defaultRestSeconds = 120),
    ExerciseEntity(name = "덤벨 벤트 오버 레터럴 레이즈", targetArea = "어깨", defaultRestSeconds = 60),
    ExerciseEntity(name = "사이드 레터럴 레이즈 머신", targetArea = "어깨", defaultRestSeconds = 60),
    ExerciseEntity(name = "원 암 케이블 레터럴 레이즈", targetArea = "어깨", defaultRestSeconds = 60),
    ExerciseEntity(name = "케이블 프론트 레이즈", targetArea = "어깨", defaultRestSeconds = 60),
    ExerciseEntity(name = "밀리터리 프레스", targetArea = "어깨", defaultRestSeconds = 120),
    ExerciseEntity(name = "덤벨 프론트 레이즈", targetArea = "어깨", defaultRestSeconds = 60),
    ExerciseEntity(name = "덤벨 시티드 오버헤드 프레스", targetArea = "어깨", defaultRestSeconds = 90),
    ExerciseEntity(name = "비하인드 넥 프레스", targetArea = "어깨", defaultRestSeconds = 90),
    ExerciseEntity(name = "케이블 와이드 레터럴 레이즈", targetArea = "어깨", defaultRestSeconds = 60),
    ExerciseEntity(name = "케이블 페이스 풀", targetArea = "어깨", defaultRestSeconds = 60),

    // 팔
    ExerciseEntity(name = "딥 머신", targetArea = "팔", defaultRestSeconds = 90),
    ExerciseEntity(name = "암 컬 머신", targetArea = "팔", defaultRestSeconds = 90),
    ExerciseEntity(name = "덤벨 해머 컬", targetArea = "팔", defaultRestSeconds = 60),
    ExerciseEntity(name = "케이블 로프 푸쉬다운", targetArea = "팔", defaultRestSeconds = 60),
    ExerciseEntity(name = "케이블 오버헤드 트라이셉스 익스텐션", targetArea = "팔", defaultRestSeconds = 60),
    ExerciseEntity(name = "케이블 푸쉬다운", targetArea = "팔", defaultRestSeconds = 60),
    ExerciseEntity(name = "케이블 해머 컬", targetArea = "팔", defaultRestSeconds = 60),
    ExerciseEntity(name = "라잉 트라이셉스 익스텐션 (스컬 크러셔)", targetArea = "팔", defaultRestSeconds = 60),
    ExerciseEntity(name = "덤벨 컬", targetArea = "팔", defaultRestSeconds = 60),
    ExerciseEntity(name = "케이블 컬", targetArea = "팔", defaultRestSeconds = 60),
    ExerciseEntity(name = "바벨 컬", targetArea = "팔", defaultRestSeconds = 60),
    ExerciseEntity(name = "이지바(EZ바) 컬", targetArea = "팔", defaultRestSeconds = 60),
    ExerciseEntity(name = "덤벨 오버헤드 트라이셉스 익스텐션", targetArea = "팔", defaultRestSeconds = 60),
    ExerciseEntity(name = "인클라인 덤벨 컬", targetArea = "팔", defaultRestSeconds = 60),
    ExerciseEntity(name = "클로즈 그립 벤치 프레스", targetArea = "팔", defaultRestSeconds = 90),
    ExerciseEntity(name = "케이블 트라이셉스 익스텐션", targetArea = "팔", defaultRestSeconds = 60),
    ExerciseEntity(name = "덤벨 킥백", targetArea = "팔", defaultRestSeconds = 60),
    ExerciseEntity(name = "프리처 컬 머신", targetArea = "팔", defaultRestSeconds = 60),
    ExerciseEntity(name = "스미스 머신 클로즈 그립 벤치 프레스", targetArea = "팔", defaultRestSeconds = 90),
    ExerciseEntity(name = "원 암 케이블 푸쉬다운", targetArea = "팔", defaultRestSeconds = 60),
    ExerciseEntity(name = "프리처 컬", targetArea = "팔", defaultRestSeconds = 60),
    ExerciseEntity(name = "벤치 딥스", targetArea = "팔", defaultRestSeconds = 60),
    ExerciseEntity(name = "덤벨 컨센트레이션 컬", targetArea = "팔", defaultRestSeconds = 60),
    ExerciseEntity(name = "원 암 케이블 컬", targetArea = "팔", defaultRestSeconds = 60),
    ExerciseEntity(name = "원 암 덤벨 컬", targetArea = "팔", defaultRestSeconds = 60),
    ExerciseEntity(name = "덤벨 리스트 컬", targetArea = "팔", defaultRestSeconds = 45),

    // 복근
    ExerciseEntity(name = "라잉 레그 레이즈", targetArea = "복근", defaultRestSeconds = 45),
    ExerciseEntity(name = "행잉 레그 레이즈", targetArea = "복근", defaultRestSeconds = 45),
    ExerciseEntity(name = "데드 버그", targetArea = "복근", defaultRestSeconds = 45),
    ExerciseEntity(name = "짐볼 레그레이즈", targetArea = "복근", defaultRestSeconds = 45),
    ExerciseEntity(name = "싯업 (윗몸 일으키기)", targetArea = "복근", defaultRestSeconds = 45),
    ExerciseEntity(name = "행잉 니레이즈", targetArea = "복근", defaultRestSeconds = 60),
    ExerciseEntity(name = "크런치", targetArea = "복근", defaultRestSeconds = 45),
    ExerciseEntity(name = "플랭크", targetArea = "복근", defaultRestSeconds = 45),
    ExerciseEntity(name = "크런치 머신 (앱도미널 머신)", targetArea = "복근", defaultRestSeconds = 45),
    ExerciseEntity(name = "케이블 크런치", targetArea = "복근", defaultRestSeconds = 45),
    ExerciseEntity(name = "AB 슬라이드", targetArea = "복근", defaultRestSeconds = 60),
    ExerciseEntity(name = "러시안 트위스트", targetArea = "복근", defaultRestSeconds = 45),
    ExerciseEntity(name = "니업", targetArea = "복근", defaultRestSeconds = 45),
    ExerciseEntity(name = "복근", targetArea = "복근", defaultRestSeconds = 45),
    ExerciseEntity(name = "사이드 크런치", targetArea = "복근", defaultRestSeconds = 45),
    ExerciseEntity(name = "바이시클 크런치", targetArea = "복근", defaultRestSeconds = 45),
    ExerciseEntity(name = "디클라인 크런치", targetArea = "복근", defaultRestSeconds = 45),
    ExerciseEntity(name = "사이드 플랭크", targetArea = "복근", defaultRestSeconds = 45),
    ExerciseEntity(name = "마운틴 클라이머", targetArea = "복근", defaultRestSeconds = 45),
    ExerciseEntity(name = "힐 터치", targetArea = "복근", defaultRestSeconds = 45),
    ExerciseEntity(name = "리버스 크런치", targetArea = "복근", defaultRestSeconds = 45),
    ExerciseEntity(name = "플러터 킥", targetArea = "복근", defaultRestSeconds = 45),

    // 기타 시간 기준 운동 (Duration-based)
    cardioExercise("로잉 머신"),
    cardioExercise("스텝밀 (천국의계단)"),
    cardioExercise("마이 마운틴"),
    cardioExercise("사이클"),
    cardioExercise("러닝 (트레드밀)"),
    cardioExercise("유산소"),
    cardioExercise("걷기"),
    cardioExercise("인클라인 러닝 (트레드밀)"),
    cardioExercise("인터벌"),
    cardioExercise("조깅"),
    cardioExercise("스텝퍼"),
    cardioExercise("수영"),
    cardioExercise("풋살"),
    cardioExercise("페이스 마크"),
    cardioExercise("등산"),

    // 기타 횟수 기준 운동 (Repetition-based)
    ExerciseEntity(name = "버피 테스트", targetArea = "기타", defaultRestSeconds = 60),
    ExerciseEntity(name = "케틀벨 스윙", targetArea = "기타", defaultRestSeconds = 60),
    ExerciseEntity(name = "버피 클린", targetArea = "기타", defaultRestSeconds = 60),
    ExerciseEntity(name = "줄넘기", targetArea = "기타", defaultRestSeconds = 60),
    ExerciseEntity(name = "스내치", targetArea = "기타", defaultRestSeconds = 90),
    ExerciseEntity(name = "스러스터", targetArea = "기타", defaultRestSeconds = 90),
    ExerciseEntity(name = "클린 앤 저크", targetArea = "기타", defaultRestSeconds = 90),
    ExerciseEntity(name = "점핑잭", targetArea = "기타", defaultRestSeconds = 45)
        
)

    const val ALL_TARGET_AREA = "전체"
    const val UNCATEGORIZED_TARGET_AREA = "기타"

    val exerciseTargetAreas = listOf("가슴", "등", "하체", "어깨", "팔", "복근", "유산소", "기타")
    val targetAreas = listOf(ALL_TARGET_AREA) + exerciseTargetAreas

    fun queryTargetOrNull(targetArea: String): String? {
        return targetArea.takeUnless { it == ALL_TARGET_AREA }
    }

    fun defaultByName(name: String): ExerciseEntity? {
        val normalizedName = name.trim()
        return defaults.firstOrNull { it.name == normalizedName }
    }

    private fun cardioExercise(name: String): ExerciseEntity {
        return ExerciseEntity(
            name = name,
            targetArea = "유산소",
            defaultRestSeconds = 60,
            inputType = ExerciseInputType.DURATION,
        )
    }
}
