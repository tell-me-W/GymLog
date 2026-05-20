# GymLog

GymLog는 광고, 계정, 서버 동기화 없이 기기 안에서만 운동을 기록하는 로컬 퍼스트 Android 운동 로그 앱입니다. Kotlin, Jetpack Compose, Room(SQLite)을 사용하며, 네트워크가 없어도 운동 시작, 세트 입력, 기록 조회, 이전 기록 복사가 동작하도록 설계되어 있습니다.

## 핵심 방향

- 모든 운동 데이터는 로컬 Room DB에만 저장합니다.
- 운동 중 입력은 `DRAFT` 세션에 즉시 반영합니다.
- 운동 완료 시 실제 완료 체크된 세트만 `COMPLETED` 기록으로 확정합니다.
- 완료된 과거 운동 기록을 새 드래프트 운동으로 복사해 다시 수행할 수 있습니다.
- 운동 종목, 루틴, 백업/불러오기, 텍스트 기록 추가를 로컬 데이터 안에서 처리합니다.
- Galaxy/Android 네이티브 사용 경험을 우선합니다.

## 주요 기능

- 월별 대시보드: 월별 캘린더, 선택 월 이동, 월간 운동 횟수와 총 볼륨 요약.
- 운동 시작: 빈 운동 시작 또는 이전 완료 기록 복사.
- 실시간 로깅: 세트 추가/삭제, 중량/반복 수 입력, 시간 기준 운동, 완료 체크, 휴식 타이머.
- 운동 종목 추가: 검색, 타겟 부위 필터, 최근 수행 기록 표시, 여러 종목 동시 추가.
- 커스텀 운동과 루틴: 사용자 운동 추가, 자주 묶는 종목 목록 저장/불러오기/삭제.
- 운동 완료: 완료 체크된 세트만 기록 저장, 텍스트/이미지 공유.
- 기록 보기: 완료 기록 목록, 상세 확인, 기록 삭제, 기존 기록으로 새 운동 시작.
- 설정: 프로필, 앱 버전, JSON 백업/불러오기, 텍스트 기록 추가.

상세 동작 정책은 `docs/product-rules.md`, `docs/workout-logging.md`, `docs/exercise-library.md`, `docs/backup-import.md`를 참고합니다.

## 기술 스택

- Language: Kotlin
- UI: Jetpack Compose, Material 3
- Architecture: MVVM + Repository
- Database: Room(SQLite)
- Async: Kotlin Coroutines, Flow
- Build: Gradle, Android Gradle Plugin, KSP
- Test: JUnit, kotlinx-coroutines-test

## 프로젝트 구조

```text
app/src/main/java/com/gymlog
├── MainActivity.kt                  # Compose entry, screen routing, UI event wiring
├── GymLogApplication.kt             # Room DB and repository container
├── data
│   ├── local                        # Room entities, relations, DAO, database
│   ├── backup                       # JSON backup encode/decode
│   ├── importer                     # Text/JSON import DTO and parser
│   └── repository                   # App data access and storage policies
├── domain                           # Pure calculations and domain rules
└── ui                               # ViewModel, UI helpers, rest timer, sharing helpers
```

에이전트용 상세 작업 규칙은 각 영역의 `AGENTS.md`를 참고합니다.

- `app/src/main/java/com/gymlog/data/AGENTS.md`
- `app/src/main/java/com/gymlog/ui/AGENTS.md`
- `app/src/main/java/com/gymlog/domain/AGENTS.md`

## 개발 환경

권장 환경은 다음과 같습니다.

- JDK 17
- Android Studio
- Android SDK 35

Windows PowerShell에서 JDK 경로를 지정합니다.

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-17'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
```

## 빌드 및 테스트

전체 JVM 테스트:

```powershell
.\gradlew.bat test
```

디버그 APK 빌드:

```powershell
.\gradlew.bat assembleDebug
```

테스트 범위와 주요 검증 기준은 `docs/testing.md`를 참고합니다.

## 설계 원칙

- 운동 중 입력 흐름을 빠르게 유지합니다.
- 네트워크 연결을 전제로 하지 않습니다.
- 실제 수행한 세트만 기록에 남깁니다.
- 이전 기록 복사를 핵심 기능으로 취급합니다.
- 기능이 늘어나도 로컬 데이터 소유권은 사용자 기기에 둡니다.
