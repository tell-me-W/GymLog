# GymLog Agent Guide

GymLog 작업자는 이 파일을 먼저 읽고, 상세 정책은 연결된 하위 문서를 확인합니다. 루트 가이드는 공통 원칙과 검증 기준만 유지합니다.

## Core Agent Principles

- Think before coding: 모호한 요구사항, 충돌하는 정책, 위험한 데이터 변경은 먼저 확인합니다.
- Simplicity first: 요청 범위 밖 기능, 추측성 옵션, 1회성 추상화는 만들지 않습니다.
- Surgical changes: 필요한 파일만 수정하고, 관련 없는 정리나 대규모 포맷 변경은 하지 않습니다.
- Goal-driven execution: 성공 기준을 정하고 테스트나 명령으로 검증한 뒤 완료를 말합니다.

## Project Basics

- GymLog는 Galaxy/Android 전용 Kotlin 네이티브 앱입니다.
- 기술 스택은 Kotlin, Jetpack Compose, Material 3, Room(SQLite), MVVM, Repository, DAO입니다.
- 서버, 계정, 동기화, 광고, 결제 유도는 MVP 범위에 넣지 않습니다.
- 데이터는 로컬 Room DB에만 저장합니다.
- 기본 개발 JDK는 `C:\Program Files\Java\jdk-17`입니다.

## Where To Read Detailed Rules

- Data, Room, Repository rules: `app/src/main/java/com/gymlog/data/AGENTS.md`
- UI, ViewModel, Compose rules: `app/src/main/java/com/gymlog/ui/AGENTS.md`
- Domain calculation rules: `app/src/main/java/com/gymlog/domain/AGENTS.md`
- Product behavior rules: `docs/product-rules.md`
- Workout logging flow: `docs/workout-logging.md`
- Exercise library and routines: `docs/exercise-library.md`
- Backup and import rules: `docs/backup-import.md`
- Testing guide: `docs/testing.md`

## Architecture Map

- `app/src/main/java/com/gymlog/MainActivity.kt`: Compose entry, screen routing, top-level UI event wiring.
- `app/src/main/java/com/gymlog/GymLogApplication.kt`: Room DB and repository container.
- `app/src/main/java/com/gymlog/data/`: Room entities, DAO, repository, backup, import.
- `app/src/main/java/com/gymlog/domain/`: Pure calculation and domain logic.
- `app/src/main/java/com/gymlog/ui/`: ViewModel, UI state helpers, formatters, rest timer.
- `app/src/test/java/com/gymlog/`: JVM unit tests.

## Development Rules

- Read the existing structure and local patterns before editing.
- Keep storage policies in Repository/DAO/domain layers; do not rely on UI alone.
- Extract reusable pure logic into `domain` or small UI helpers that can be JVM-tested.
- Review migration/version policy whenever Room entity or schema changes.
- Do not revert or delete user changes, unrelated untracked files, or local generated files.
- Do not commit local/secret-like files such as `app/google-services.json` unless the user explicitly asks.

## Verification Commands

PowerShell에서 JDK 17을 지정한 뒤 실행합니다.

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-17'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat test
```

디버그 APK 빌드까지 확인할 때:

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-17'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat assembleDebug
```

문서만 바꾸는 작업은 앱 테스트 대신 링크/경로 검색과 `git diff --check`로 검증할 수 있습니다.

## Git Workflow

- 커밋 전 `git status --short`로 staged/untracked 파일을 확인합니다.
- 관련 파일만 명시적으로 `git add`합니다.
- 기존 untracked 파일은 작업 범위와 무관하면 커밋하지 않습니다.
- 기능/버그 수정은 최소 `.\gradlew.bat test`를 실행합니다.
- UI나 Android 컴파일에 영향이 있으면 `.\gradlew.bat assembleDebug`도 실행합니다.
- 사용자가 푸시를 요청했을 때만 `git push`합니다.

## Completion Checklist

- 요청한 범위가 실제 파일에 반영되었는가
- 로컬 전용 데이터 원칙과 핵심 저장 정책을 깨지 않았는가
- 관련 테스트 또는 문서 검증을 실행했는가
- 의도하지 않은 파일이 staged 상태가 아닌가
- 최종 답변에 변경 내용과 검증 결과를 짧게 포함했는가
