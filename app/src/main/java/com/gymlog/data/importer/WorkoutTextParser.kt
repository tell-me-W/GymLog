package com.gymlog.data.importer

import com.gymlog.data.repository.SeedExercises
import java.time.LocalDate
import java.time.ZoneId

class WorkoutTextParser(
    private val zoneId: ZoneId = ZoneId.systemDefault(),
) {
    fun parseMany(text: String): Result<List<ImportedWorkoutSession>> = runCatching {
        val blocks = splitSessionBlocks(text)
        require(blocks.isNotEmpty()) { "운동 기록이 없습니다." }
        blocks.map { parse(it).getOrThrow() }
    }

    fun parse(text: String): Result<ImportedWorkoutSession> = runCatching {
        val lines = text.lines().map { it.trim() }
        val nonEmpty = lines.filter { it.isNotBlank() }
        require(nonEmpty.size >= 3) { "운동 기록이 너무 짧습니다." }

        val date = parseDate(nonEmpty[0])
        val durationMinutes = parseDuration(nonEmpty[1])
        val startedAt = date.atTime(12, 0).atZone(zoneId).toInstant().toEpochMilli()
        val endedAt = startedAt + durationMinutes * 60_000L
        val exercises = parseExercises(nonEmpty.drop(2))
        require(exercises.isNotEmpty()) { "운동 종목이 없습니다." }

        ImportedWorkoutSession(
            startedAtMillis = startedAt,
            endedAtMillis = endedAt,
            exercises = exercises,
        )
    }

    private fun parseDate(line: String): LocalDate {
        val match = headerRegex.matchEntire(line)
            ?: error("날짜 형식이 올바르지 않습니다.")
        return LocalDate.of(
            match.groupValues[1].toInt(),
            match.groupValues[2].toInt(),
            match.groupValues[3].toInt(),
        )
    }

    private fun parseDuration(line: String): Long {
        val match = durationRegex.matchEntire(line)
            ?: error("운동 시간 형식이 올바르지 않습니다.")
        return match.groupValues[1].toLong()
    }

    private fun parseExercises(lines: List<String>): List<ImportedExercise> {
        val exercises = mutableListOf<ImportedExercise>()
        var currentName: String? = null
        var currentSets = mutableListOf<ImportedSet>()

        fun flush() {
            val name = currentName ?: return
            require(currentSets.isNotEmpty()) { "$name 세트가 없습니다." }
            exercises += ImportedExercise(
                name = name,
                targetArea = SeedExercises.UNCATEGORIZED_TARGET_AREA,
                defaultRestSeconds = 90,
                sets = currentSets.toList(),
            )
            currentName = null
            currentSets = mutableListOf()
        }

        lines.forEach { line ->
            if (isIgnorableLine(line)) return@forEach
            val setMatch = setRegex.matchEntire(line)
            val repsOnlyMatch = repsOnlyRegex.matchEntire(line)
            when {
                setMatch != null -> {
                    require(currentName != null) { "운동명 없이 세트가 입력되었습니다." }
                    currentSets += ImportedSet(
                        weightKg = setMatch.groupValues[1].replace(",", "").toDouble(),
                        reps = setMatch.groupValues[2].toInt(),
                        isCompleted = true,
                    )
                }
                repsOnlyMatch != null -> {
                    require(currentName != null) { "운동명 없이 세트가 입력되었습니다." }
                    currentSets += ImportedSet(
                        weightKg = 0.0,
                        reps = repsOnlyMatch.groupValues[1].toInt(),
                        isCompleted = true,
                    )
                }
                else -> {
                    if (currentName != null) flush()
                    currentName = line
                }
            }
        }
        flush()
        return exercises
    }

    private fun splitSessionBlocks(text: String): List<String> {
        val blocks = mutableListOf<MutableList<String>>()
        text.lines().forEach { rawLine ->
            val line = rawLine.trim()
            val headerMatch = headerRegex.find(line)
            if (headerMatch != null) {
                blocks += mutableListOf(line.substring(headerMatch.range.first))
            } else if (blocks.isNotEmpty()) {
                if (!isIgnorableLine(line)) {
                    blocks.last() += rawLine
                }
            } else if (line.isNotBlank()) {
                error("첫 줄은 [자유 운동] yyyy년 M월 d일 형식이어야 합니다.")
            }
        }
        return blocks
            .map { it.joinToString("\n").trim() }
            .filter { it.isNotBlank() }
    }

    private fun isIgnorableLine(line: String): Boolean {
        return line.isBlank() ||
            line == "```" ||
            line.startsWith("총 볼륨:") ||
            line.startsWith("칼로리:") ||
            line == "#짐워크"
    }

    private companion object {
        val headerRegex = Regex("""\[.*]\s*(\d{4})년\s*(\d{1,2})월\s*(\d{1,2})일""")
        val durationRegex = Regex("""^(\d+)분$""")
        val setRegex = Regex("""^([\d,]+(?:\.\d+)?)kg\s*x\s*(\d+)회$""", RegexOption.IGNORE_CASE)
        val repsOnlyRegex = Regex("""^(\d+)회$""")
    }
}
