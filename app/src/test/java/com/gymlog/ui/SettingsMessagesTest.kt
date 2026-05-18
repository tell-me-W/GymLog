package com.gymlog.ui

import com.gymlog.data.repository.ImportResult
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsMessagesTest {
    @Test
    fun importMessagesShowInsertedAndSkippedCounts() {
        val result = ImportResult(inserted = 3, skipped = 2)

        assertEquals("불러오기 완료: 3개 추가, 2개 건너뜀", SettingsMessages.backupImportSuccess(result))
        assertEquals("텍스트 추가 완료: 3개 추가, 2개 건너뜀", SettingsMessages.textImportSuccess(result))
    }

    @Test
    fun failureMessagesUseFallbackWhenErrorMessageIsMissing() {
        val error = Throwable()

        assertEquals("백업 생성에 실패했습니다.", SettingsMessages.backupExportFailure(error))
        assertEquals("불러오기에 실패했습니다.", SettingsMessages.backupImportFailure(error))
        assertEquals("텍스트 추가에 실패했습니다.", SettingsMessages.textImportFailure(error))
    }
}
