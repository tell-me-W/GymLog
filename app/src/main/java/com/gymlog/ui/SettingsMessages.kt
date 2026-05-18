package com.gymlog.ui

import com.gymlog.data.repository.ImportResult

object SettingsMessages {
    fun backupExportFailure(error: Throwable): String {
        return error.message ?: "백업 생성에 실패했습니다."
    }

    fun backupImportSuccess(result: ImportResult): String {
        return "불러오기 완료: ${result.inserted}개 추가, ${result.skipped}개 건너뜀"
    }

    fun backupImportFailure(error: Throwable): String {
        return error.message ?: "불러오기에 실패했습니다."
    }

    fun textImportSuccess(result: ImportResult): String {
        return "텍스트 추가 완료: ${result.inserted}개 추가, ${result.skipped}개 건너뜀"
    }

    fun textImportFailure(error: Throwable): String {
        return error.message ?: "텍스트 추가에 실패했습니다."
    }
}
