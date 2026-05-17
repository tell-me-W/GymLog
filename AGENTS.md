# GymLog Agent Guide

이 문서는 GymLog 저장소에서 작업하는 Codex/에이전트가 따라야 할 개발 규칙입니다. 기능 설명은 `README.md`를 우선 참고하고, 이 문서는 구현 판단과 검증 기준으로 사용합니다.

## Project Basics

- GymLog는 Galaxy/Android 전용 Kotlin 네이티브 앱입니다.
- 기술 스택은 Kotlin, Jetpack Compose, Material 3, Room(SQLite), MVVM, Repository, DAO 구조입니다.
- 서버, 계정, 동기화, 광고, 결제 유도는 MVP 범위에 넣지 않습니다.
- 데이터는 로컬 Room DB에만 저장합니다.
- 기본 개발 JDK는 `C:\Program Files\Java\jdk-17`입니다.

## Architecture Map

- `app/src/main/java/com/gymlog/MainActivity.kt`
  - Compose 화면, 화면 전환, 다이얼로그, 주요 UI 이벤트 연결
- `app/src/main/java/com/gymlog/GymLogApplication.kt`
  - Room DB와 Repository 컨테이너 구성
- `app/src/main/java/com/gymlog/data/local/`
  - Room entity, relation, DAO, database, converter
- `app/src/main/java/com/gymlog/data/repository/`
  - 앱 데이터 접근 규칙과 seed 운동 목록
- `app/src/main/java/com/gymlog/data/backup/`
  - 완료 운동 기록 JSON 백업 encode/decode
- `app/src/main/java/com/gymlog/data/importer/`
  - 텍스트/JSON 가져오기용 DTO와 텍스트 파서
- `app/src/main/java/com/gymlog/domain/`
  - 순수 계산/도메인 로직
- `app/src/main/java/com/gymlog/ui/`
  - ViewModel, UI 상태, 포맷터, 휴식 타이머
- `app/src/test/java/com/gymlog/`
  - JVM 단위 테스트

## Core Product Rules

- 운동 시작 즉시 `DRAFT` 세션을 생성합니다.
- 운동 중 세트 추가, 중량, 반복 수, 완료 체크 변경은 드래프트에 즉시 저장합니다.
- `운동 완료` 시 `isCompleted = true`인 세트만 완료 기록에 남깁니다.
- 완료 체크되지 않은 세트는 완료 기록 저장 전에 제거합니다.
- 완료 세트가 없는 운동 종목은 완료 기록에서 제외합니다.
- 완료 세트가 하나도 없는 세션은 `COMPLETED`로 저장하지 않고 드래프트를 삭제합니다.
- 캘린더와 운동 기록 화면에는 `COMPLETED` 세션만 표시합니다.
- 대시보드 월간 요약은 선택 월의 `COMPLETED` 세션만 세고, 완료 체크된 세트의 `중량 * 반복 횟수`만 합산합니다.
- 이전 기록 복사는 `COMPLETED` 세션만 원본으로 허용합니다.
- 이전 기록 복사 결과는 새 `DRAFT` 세션이어야 하며, 복사된 모든 세트의 `isCompleted`는 `false`로 초기화합니다.
- 한 세트의 `kg` 또는 `reps` 수정은 같은 운동 종목 안의 미완료 세트에만 전파합니다.
- 이미 완료한 세트와 다른 운동 종목의 세트는 값 전파 대상이 아닙니다.
- 휴식 타이머 알림은 휴식 종료 시점에만 사용자 알림을 울립니다.
- JSON 백업은 완료 운동 기록만 포함하고, 사용자 프로필은 포함하지 않습니다.
- 백업/텍스트 불러오기는 기존 기록에 추가하되, 시작 시각, 종료 시각, 운동명/세트 구성이 같은 완료 기록은 중복으로 건너뜁니다.
- 텍스트 기록 추가는 `[자유 운동] yyyy년 M월 d일` 헤더마다 세션을 분리합니다.
- 텍스트 기록의 `총 볼륨`, `칼로리`, 단독 `#짐워크`, 코드블럭 표시는 저장하지 않습니다.
- `#짐워크[자유 운동] ...`처럼 헤더 앞에 불필요한 문자열이 붙어도 세션 헤더로 처리합니다.
- 중량 없는 `15회` 세트는 `0kg x 15회`로 저장합니다.
- 기본 운동명은 가져오기 중에도 `SeedExercises`의 타겟 부위와 휴식 시간을 우선 사용합니다.
- 앱 시작 시 기본 운동 seed를 DB와 동기화하고, 같은 이름의 기본 종목이 `기타` 커스텀으로 들어간 경우 seed 기준으로 보정합니다.

## UI Rules

- 첫 화면은 대시보드입니다.
- 대시보드는 월별 캘린더, 선택 월, 이전/다음 월 이동, 월간 요약, 운동 시작, 운동 기록 진입, 설정 진입을 제공합니다.
- 월간 요약은 캘린더 아래에 둡니다.
- 설정 화면은 프로필, 앱 버전, 운동 기록 불러오기, 텍스트로 추가, 운동 기록 백업하기를 제공합니다.
- 운동 종목 추가 화면은 여러 종목을 한 번에 선택할 수 있어야 합니다.
- 운동 종목 필터에는 `전체`가 있고, `전체`는 모든 운동을 보여주는 필터 없음 상태입니다.
- 운동 종목 필터에는 `기타`도 포함합니다.
- 운동 종목 목록은 최근 2개월 수행 기록이 있는 종목을 최신순으로 우선 정렬하고, 기록이 없는 종목은 뒤쪽에 이름순으로 정렬합니다.
- 최근 수행 기록은 `최근 yyyy.MM.dd · N세트` 형식으로 표시합니다.
- 커스텀 종목 추가 폼은 기본으로 접혀 있어야 하며 사용자가 열고 닫을 수 있어야 합니다.
- 커스텀 종목의 분류에는 `전체`를 저장하지 않습니다. 실제 운동 분류 중 하나를 저장합니다.
- 운동 기록 목록에는 기록 삭제 기능이 있어야 하며, 삭제 전 확인을 받습니다.
- Android 뒤로가기는 앱 내부 화면 이동을 우선합니다. 대시보드에서는 시스템 기본 동작을 허용합니다.
- 텍스트는 한국어 UI 문구를 기본으로 합니다.

## Development Rules

- 기존 구조와 패턴을 먼저 읽고, 필요한 범위만 수정합니다.
- 데이터 저장 정책은 Repository/DAO 레벨에서 보장하고, UI만 믿지 않습니다.
- 재사용 가능한 순수 로직은 `domain` 또는 작은 UI helper로 분리해 테스트 가능하게 만듭니다.
- Room entity나 schema를 바꿀 때는 마이그레이션 또는 DB 버전 정책을 함께 검토합니다.
- Room DB 버전 변경 시 기존 운동 기록 보존 여부를 테스트 또는 명시적으로 확인합니다.
- Android 권한, 알림, 타이머처럼 플랫폼 동작이 얽힌 코드는 작은 정책 객체나 순수 함수로 테스트 가능한 부분을 분리합니다.
- 텍스트 파서, 백업 codec, 최근 기록 정렬, 월간 요약 같은 정책은 UI에 묻지 말고 별도 helper/repository 테스트로 보호합니다.
- 불필요한 리팩터링, 대규모 파일 이동, 스타일 일괄 변경은 피합니다.
- 기존 사용자 변경이나 untracked 파일은 요청 없이 되돌리거나 삭제하지 않습니다.
- `app/google-services.json` 같은 로컬/비밀 가능성이 있는 파일은 명시 요청 없이 커밋하지 않습니다.

## Testing

기능 변경이나 버그 수정은 가능한 한 테스트를 먼저 추가합니다.

주요 테스트 대상:

- 완료 세트만 저장되는지
- 완료 세트가 없는 세션이 저장되지 않는지
- 이전 기록 복사 시 값은 유지되고 완료 상태는 false가 되는지
- 드래프트 세션이 캘린더/운동 기록에 나오지 않는지
- 총 볼륨이 `중량 * 반복 횟수` 합계로 계산되는지
- 월간 요약이 선택 월의 완료 세션과 완료 세트만 기준으로 계산되는지
- 세트 값 전파가 같은 운동의 미완료 세트에만 적용되는지
- `전체` 운동 분류가 필터 없음으로 동작하는지
- `기타` 운동 분류가 실제 필터로 동작하는지
- 최근 2개월 운동 기록 정렬이 최신 수행일 우선으로 동작하는지
- 뒤로가기 목적지가 화면별로 올바른지
- 휴식 타이머 알림 정책이 휴식 완료 시점만 알림으로 처리하는지
- 프로필 저장/조회가 유지되는지
- JSON 백업 encode/decode가 완료 기록 구조를 유지하는지
- 백업/텍스트 가져오기가 중복 완료 기록을 건너뛰는지
- 텍스트 파서가 여러 세션, `#짐워크` 연결 헤더, 중량 없는 반복 수, 불필요한 요약 줄을 처리하는지
- 기본 운동 seed 동기화가 누락 기본 종목을 추가하고 잘못 분류된 기본 종목을 보정하는지

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

특정 테스트만 확인할 때:

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-17'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat testDebugUnitTest --tests com.gymlog.domain.WorkoutCalculatorTest
```

## Git Workflow

- 커밋 전 `git status --short`로 staged/untracked 파일을 확인합니다.
- 관련 파일만 명시적으로 `git add`합니다.
- 기존 untracked 파일은 작업 범위와 무관하면 커밋하지 않습니다.
- 커밋 전 최소 `.\gradlew.bat test`를 실행합니다.
- UI나 Android 컴파일에 영향이 있으면 `.\gradlew.bat assembleDebug`도 실행합니다.
- 사용자가 푸시를 요청했을 때만 `git push`합니다.

## Completion Checklist

작업 완료 전에 확인합니다.

- 요청한 기능/버그가 실제 코드에 반영되었는가
- 핵심 저장 정책과 로컬 전용 원칙을 깨지 않았는가
- 관련 테스트를 추가하거나 기존 테스트로 충분히 검증했는가
- `test` 또는 필요한 빌드 명령이 통과했는가
- 의도하지 않은 파일이 staged 상태가 아닌가
- 최종 답변에 변경 내용과 검증 결과를 짧게 포함했는가
