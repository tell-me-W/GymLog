package com.gymlog.data.backup

import com.gymlog.data.importer.ImportedExercise
import com.gymlog.data.importer.ImportedSet
import com.gymlog.data.importer.ImportedWorkoutSession
import com.gymlog.data.local.ExerciseInputType
import org.json.JSONArray
import org.json.JSONObject

object WorkoutBackupCodec {
    fun encode(sessions: List<ImportedWorkoutSession>): String {
        val root = JSONObject()
            .put("schemaVersion", 1)
            .put(
                "sessions",
                JSONArray().also { sessionsJson ->
                    sessions.forEach { session ->
                        sessionsJson.put(session.toJson())
                    }
                },
            )
        return root.toString(2)
    }

    fun decode(json: String): Result<List<ImportedWorkoutSession>> = runCatching {
        val root = JSONObject(json)
        require(root.getInt("schemaVersion") == 1) { "지원하지 않는 백업 버전입니다." }
        root.getJSONArray("sessions").mapObjects { sessionJson ->
            ImportedWorkoutSession(
                startedAtMillis = sessionJson.getLong("startedAtMillis"),
                endedAtMillis = sessionJson.getLong("endedAtMillis"),
                exercises = sessionJson.getJSONArray("exercises").mapObjects { exerciseJson ->
                    ImportedExercise(
                        name = exerciseJson.getString("name"),
                        targetArea = exerciseJson.optString("targetArea", "기타"),
                        defaultRestSeconds = exerciseJson.optInt("defaultRestSeconds", 90),
                        inputType = ExerciseInputType.valueOf(
                            exerciseJson.optString("inputType", ExerciseInputType.REPS.name)
                        ),
                        sets = exerciseJson.getJSONArray("sets").mapObjects { setJson ->
                            ImportedSet(
                                weightKg = setJson.getDouble("weightKg"),
                                reps = setJson.getInt("reps"),
                                durationSeconds = setJson.optInt("durationSeconds", 0),
                                isCompleted = true,
                            )
                        },
                    )
                },
            )
        }
    }

    private fun ImportedWorkoutSession.toJson(): JSONObject {
        return JSONObject()
            .put("startedAtMillis", startedAtMillis)
            .put("endedAtMillis", endedAtMillis)
            .put(
                "exercises",
                JSONArray().also { exercisesJson ->
                    exercises.forEach { exercisesJson.put(it.toJson()) }
                },
            )
    }

    private fun ImportedExercise.toJson(): JSONObject {
        return JSONObject()
            .put("name", name)
            .put("targetArea", targetArea)
            .put("defaultRestSeconds", defaultRestSeconds)
            .put("inputType", inputType.name)
            .put(
                "sets",
                JSONArray().also { setsJson ->
                    sets.forEach { setsJson.put(it.toJson()) }
                },
            )
    }

    private fun ImportedSet.toJson(): JSONObject {
        return JSONObject()
            .put("weightKg", weightKg)
            .put("reps", reps)
            .put("durationSeconds", durationSeconds)
    }

    private fun <T> JSONArray.mapObjects(transform: (JSONObject) -> T): List<T> {
        return (0 until length()).map { index -> transform(getJSONObject(index)) }
    }
}
