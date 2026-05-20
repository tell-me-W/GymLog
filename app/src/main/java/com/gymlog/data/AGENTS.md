# GymLog Data Guide

`data` 영역은 로컬 Room DB, DAO, Repository, 백업/가져오기 저장 정책을 담당합니다. UI 편의를 위해 저장 규칙을 약하게 만들지 않습니다.

## Responsibilities

- `local`: Room entity, relation, DAO, database, converter.
- `repository`: 앱 데이터 접근 규칙, seed 운동 동기화, 세션 저장 정책.
- `backup`: 완료 운동 기록 JSON encode/decode.
- `importer`: 텍스트/JSON 가져오기 DTO와 텍스트 파서.

## Storage Rules

- 데이터는 로컬 Room DB에만 저장합니다.
- 운동 시작 즉시 `DRAFT` 세션을 생성합니다.
- 운동 중 세트 추가, 중량, 반복 수, 완료 체크 변경은 드래프트에 즉시 저장합니다.
- 완료 기록에는 `isCompleted = true`인 세트만 남깁니다.
- 완료 세트가 없는 운동 종목은 완료 기록에서 제외합니다.
- 완료 세트가 하나도 없는 세션은 `COMPLETED`로 저장하지 않고 드래프트를 삭제합니다.
- 캘린더와 운동 기록 화면에는 `COMPLETED` 세션만 노출합니다.
- 이전 기록 복사는 `COMPLETED` 세션만 원본으로 허용하고 결과는 새 `DRAFT` 세션으로 저장합니다.
- 복사된 모든 세트의 `isCompleted`는 `false`로 초기화합니다.

## Room And Migration Rules

- Entity/table/schema 변경 시 DB 버전과 migration 정책을 함께 검토합니다.
- 기존 운동 기록 보존 여부를 테스트 또는 명시 검증으로 확인합니다.
- Relation 타입은 UI가 이름을 역조회하지 않도록 필요한 상세 정보를 포함하게 설계합니다.
- DAO는 화면 편의가 아니라 저장 불변식을 기준으로 쿼리를 제공합니다.

## Backup And Import Rules

- JSON 백업은 완료 운동 기록만 포함하고 사용자 프로필은 포함하지 않습니다.
- 백업/텍스트 불러오기는 기존 기록에 추가합니다.
- 시작 시각, 종료 시각, 운동명/세트 구성이 같은 완료 기록은 중복으로 건너뜁니다.
- 텍스트 기록은 `[자유 운동] yyyy년 M월 d일` 헤더마다 세션을 분리합니다.
- `#짐워크[자유 운동] ...`처럼 헤더 앞에 문자열이 붙어도 세션 헤더로 처리합니다.
- `총 볼륨`, `칼로리`, 단독 `#짐워크`, 코드블럭 표시는 저장하지 않습니다.
- 중량 없는 `15회` 세트는 `0kg x 15회`로 저장합니다.
- 기본 운동명은 가져오기 중에도 `SeedExercises`의 타겟 부위와 휴식 시간을 우선 사용합니다.
- DB에 없는 운동명은 `기타` 커스텀 운동으로 자동 생성합니다.

## Testing Focus

- Repository/DAO 저장 정책은 JVM 테스트로 보호합니다.
- Room entity나 migration 변경은 기존 완료 기록 보존을 확인합니다.
- 백업 codec, 텍스트 파서, seed 동기화는 UI 없이 테스트합니다.
