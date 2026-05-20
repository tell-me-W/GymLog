# Testing Guide

기능 변경이나 버그 수정은 가능한 한 테스트를 먼저 추가합니다. 문서만 변경한 경우에는 링크/경로 검색과 diff 검증으로 충분할 수 있습니다.

## Standard Commands

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

## Core Test Targets

- 완료 세트만 저장되는지.
- 완료 세트가 없는 세션이 저장되지 않는지.
- 이전 기록 복사 시 값은 유지되고 완료 상태는 false가 되는지.
- 드래프트 세션이 캘린더/운동 기록에 나오지 않는지.
- 총 볼륨이 `중량 * 반복 횟수` 합계로 계산되는지.
- 월간 요약이 선택 월의 완료 세션과 완료 세트만 기준으로 계산되는지.
- 세트 값 전파가 같은 운동의 미완료 세트에만 적용되는지.
- `전체` 운동 분류가 필터 없음으로 동작하는지.
- `기타` 운동 분류가 실제 필터로 동작하는지.
- 최근 2개월 운동 기록 정렬이 최신 수행일 우선으로 동작하는지.
- 뒤로가기 목적지가 화면별로 올바른지.
- 휴식 타이머 알림 정책이 휴식 완료 시점만 알림으로 처리되는지.
- 프로필 저장/조회가 유지되는지.
- JSON 백업 encode/decode가 완료 기록 구조를 유지하는지.
- 백업/텍스트 가져오기가 중복 완료 기록을 건너뛰는지.
- 텍스트 파서가 여러 세션, 연결 헤더, 중량 없는 반복 수, 불필요한 요약 줄을 처리하는지.
- 기본 운동 seed 동기화가 누락 기본 종목을 추가하고 잘못 분류된 기본 종목을 보정하는지.

## Documentation-Only Verification

문서만 변경했을 때는 앱 테스트를 생략할 수 있습니다. 대신 다음을 확인합니다.

```powershell
rg "product-rules|workout-logging|exercise-library|backup-import|testing" AGENTS.md README.md docs
git diff --check
git status --short
```
