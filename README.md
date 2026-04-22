# Directional Stock Service

가상의 국내 주식 데이터를 조회하고, 사용자가 직접 구성한 주식 지수의 시세를 계산/조회하는 Kotlin Spring Boot API 서버입니다.

종목 데이터는 애플리케이션 시작 시 `src/main/resources/data.sql`로 H2 데이터베이스에 적재됩니다. 지수 생성과 지수 시세 계산에는 Directional 과제용 외부 가격 API를 사용합니다.

## 주요 기능

- 종목 목록 조회
  - 단축코드, 한글 종목명, 한글 종목약명, 영문 종목명 부분 검색
  - 시장구분, 증권구분, 소속부, 주식종류 필터
  - 단축코드, 한글 종목약명, 상장일, 상장주식수 정렬
- 사용자 지수 관리
  - 지수 생성, 목록 조회, 수정, 삭제
  - 지수명, 영문지수명 부분 검색
  - 지수명, 영문지수명, 기준일, 구성종목수 정렬
- 지수 시세 계산 및 조회
  - 외부 가격 API에서 종목별 open/high/low/close/change 값을 조회
  - 구성종목의 시가총액 합계를 기준으로 지수 open/high/low/close 계산
  - 15분마다 전체 지수 시세 자동 갱신
  - 지수명 검색 및 등락률 정렬

## 기술 스택

- Kotlin 2.3.20
- Java 25
- Spring Boot 4.0.3
- Spring MVC
- Spring Data JPA
- Querydsl 7.1
- H2 Database
- springdoc-openapi
- Gradle 9.4 Wrapper
- JUnit 5, MockMvc

## 프로젝트 구조

```text
src/main/kotlin/net/directional/recruitment
├── stock
│   ├── inbound    # 종목 API Controller/Response
│   ├── app        # 종목 조회 조건, 정렬 옵션, 서비스
│   └── outbound   # Stock Entity, Repository, Querydsl 구현체
└── stockindex
    ├── inbound    # 지수 API Controller/Request/Response
    ├── app        # 지수 생성/수정/삭제/조회/시세계산 서비스
    └── outbound   # Entity, Repository, 외부 가격 API Client
```

## 실행 방법

### 1. 외부 가격 API 계정 설정

지수 생성과 시세 계산은 외부 가격 API 인증 정보가 필요합니다.

```bash
export DIRECTIONAL_API_USERNAME="your-email@example.com"
export DIRECTIONAL_API_PASSWORD="your-password"
```

환경변수를 설정하지 않아도 종목 조회 API는 사용할 수 있지만, 지수 생성과 시세 계산 과정에서 외부 API 호출이 실패할 수 있습니다.

### 2. 애플리케이션 실행

```bash
./gradlew bootRun
```

기본 서버 주소는 `http://localhost:8080`입니다.

### 3. 테스트 실행

```bash
./gradlew test
```

## 개발 편의 URL

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- H2 Console: `http://localhost:8080/h2-console`
- H2 JDBC URL: `jdbc:h2:file:./data/recruitment`
- H2 User: `sa`
- H2 Password: 없음

## API 문서

### 종목 조회

```http
GET /stocks
```

국내 종목 목록을 조회합니다.

| Query Parameter | 타입 | 기본값 | 설명 |
| --- | --- | --- | --- |
| `search` | string | 없음 | 단축코드, 한글 종목명, 한글 종목약명, 영문 종목명 부분 검색 |
| `marketType` | string[] | 없음 | 시장구분 필터. 같은 필터 내 여러 값은 OR 조건 |
| `securityType` | string[] | 없음 | 증권구분 필터 |
| `affiliation` | string[] | 없음 | 소속부 필터 |
| `stockType` | string[] | 없음 | 주식종류 필터 |
| `sortBy` | enum | `SHORT_CODE` | `SHORT_CODE`, `NAME_KR_SHORT`, `LISTED_AT`, `LISTED_SHARES` |
| `sortDirection` | enum | `ASC` | `ASC`, `DESC` |

서로 다른 필터 카테고리는 AND 조건으로 결합됩니다.

요청 예시:

```bash
curl "http://localhost:8080/stocks?search=삼성&marketType=KOSPI&sortBy=LISTED_SHARES&sortDirection=DESC"
```

응답 예시:

```json
[
  {
    "shortCode": "005930",
    "standardCode": "KR7005930003",
    "nameKr": "삼성전자보통주",
    "nameKrShort": "삼성전자",
    "nameEn": "SamsungElectronics",
    "listedAt": "1975-06-11",
    "marketType": "KOSPI",
    "securityType": "주권",
    "affiliation": "",
    "stockType": "보통주",
    "parValue": "100",
    "listedShares": 5919637922
  }
]
```

### 지수 생성

```http
POST /stock-indices
```

사용자 지수를 생성합니다. 요청한 구성종목의 현재 시가와 상장주식수를 이용해 기준 시가총액을 계산하고 저장합니다.

요청 Body:

| Field | 타입 | 설명 |
| --- | --- | --- |
| `name` | string | 지수명. 공백 불가 |
| `nameEn` | string | 영문 지수명. 공백 불가 |
| `baseIndex` | number | 기준지수. 0보다 커야 함 |
| `stockShortCodes` | string[] | 구성종목 단축코드 목록. 비어 있을 수 없음 |

요청 예시:

```bash
curl -X POST "http://localhost:8080/stock-indices" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "K-Tech Index",
    "nameEn": "K-Tech Index",
    "baseIndex": 1000,
    "stockShortCodes": ["005930", "035720", "263750"]
  }'
```

응답 예시:

```json
{
  "id": 1,
  "name": "K-Tech Index",
  "nameEn": "K-Tech Index",
  "baseDate": "2026-04-22",
  "baseIndex": 1000,
  "constituentCount": 3
}
```

주요 검증:

- 존재하지 않는 종목 코드가 포함되면 `400 Bad Request`
- 구성종목 가격을 외부 API에서 찾을 수 없으면 `502 Bad Gateway`
- 중복된 종목 코드는 제거 후 저장

### 지수 목록 조회

```http
GET /stock-indices
```

생성된 지수 목록을 조회합니다.

| Query Parameter | 타입 | 기본값 | 설명 |
| --- | --- | --- | --- |
| `search` | string | 없음 | 지수명, 영문 지수명 부분 검색 |
| `sortBy` | enum | `NAME` | `NAME`, `NAME_EN`, `BASE_DATE`, `CONSTITUENT_COUNT` |
| `sortDirection` | enum | `ASC` | `ASC`, `DESC` |

요청 예시:

```bash
curl "http://localhost:8080/stock-indices?sortBy=CONSTITUENT_COUNT&sortDirection=DESC"
```

응답 예시:

```json
[
  {
    "id": 1,
    "name": "K-Tech Index",
    "nameEn": "K-Tech Index",
    "baseDate": "2026-04-22",
    "baseIndex": 1000.0000,
    "constituentCount": 3
  }
]
```

### 지수 수정

```http
PATCH /stock-indices/{id}
```

지수명, 영문 지수명, 구성종목을 수정합니다. 구성종목 목록은 요청 값으로 완전히 교체됩니다.

기준일, 기준지수, 기준 시가총액은 유지됩니다. 따라서 구성종목이 바뀌어도 최초 생성 시점의 기준 시가총액은 변하지 않습니다.

요청 예시:

```bash
curl -X PATCH "http://localhost:8080/stock-indices/1" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated K-Tech Index",
    "nameEn": "Updated K-Tech Index",
    "stockShortCodes": ["005930", "035720"]
  }'
```

응답 예시:

```json
{
  "id": 1,
  "name": "Updated K-Tech Index",
  "nameEn": "Updated K-Tech Index",
  "baseDate": "2026-04-22",
  "baseIndex": 1000.0000,
  "constituentCount": 2
}
```

### 지수 삭제

```http
DELETE /stock-indices/{id}
```

지수와 해당 지수의 구성종목 관계를 삭제합니다.

응답:

```http
204 No Content
```

존재하지 않는 지수 ID는 `404 Not Found`를 반환합니다.

### 지수 시세 조회

```http
GET /stock-indices/prices
```

계산된 지수 시세를 조회합니다. 아직 시세가 계산되지 않은 지수는 결과에 포함되지 않습니다.

| Query Parameter | 타입 | 기본값 | 설명 |
| --- | --- | --- | --- |
| `search` | string | 없음 | 지수명, 영문 지수명 부분 검색 |
| `sortBy` | enum | `NAME` | `NAME`, `CHANGE_RATE` |
| `sortDirection` | enum | `ASC` | `ASC`, `DESC` |

요청 예시:

```bash
curl "http://localhost:8080/stock-indices/prices?sortBy=CHANGE_RATE&sortDirection=DESC"
```

응답 예시:

```json
[
  {
    "stockIndexId": 1,
    "name": "K-Tech Index",
    "closePrice": 1012.3456,
    "changeAmount": 12.3456,
    "changeRate": 1.2345,
    "openPrice": 1000.0000,
    "highPrice": 1020.0000,
    "lowPrice": 990.0000
  }
]
```

## 지수 시세 계산 방식

지수는 구성종목의 시가총액 합계를 이용해 계산합니다.

```text
지수 가격 = 현재 시가총액 / 기준 시가총액 * 기준지수
```

계산 대상 가격:

- `openPrice`: 구성종목별 `open * listedShares` 합계 기준
- `highPrice`: 구성종목별 `high * listedShares` 합계 기준
- `lowPrice`: 구성종목별 `low * listedShares` 합계 기준
- `closePrice`: 구성종목별 `close * listedShares` 합계 기준
- `changeAmount`: 현재 지수 종가 - 전일 지수 종가
- `changeRate`: `changeAmount / 전일 지수 종가 * 100`

전일 종가는 외부 가격 API의 `close - change` 값을 사용해 역산합니다.

지수 생성 시 저장된 기준 시가총액은 이후 구성종목 수정 여부와 관계없이 유지됩니다.

## 외부 가격 API 연동

`PriceApiClient`는 다음 순서로 외부 API를 호출합니다.

1. `POST /auth/token`으로 Access Token 발급
2. `GET /v1/prices` 호출 시 `Authorization: Bearer <token>` 헤더 사용
3. 응답의 `ticker`, `open`, `high`, `low`, `close`, `change`로 지수 시세 계산

연동 설정은 `application.yaml`의 `directional.price-api` 아래에 있습니다.

```yaml
directional:
  price-api:
    base-url: https://api-recruitment.directional.net
    username: ${DIRECTIONAL_API_USERNAME:}
    password: ${DIRECTIONAL_API_PASSWORD:}
```

## 데이터베이스

- 런타임 DB는 H2 file DB입니다.
- 기본 위치는 프로젝트 루트 기준 `./data/recruitment`입니다.
- `data.sql`에는 2,885개 종목의 초기 데이터가 `MERGE INTO` 문으로 들어 있습니다.
- `spring.sql.init.mode=always`로 설정되어 있어 애플리케이션 시작 시 초기 종목 데이터가 반영됩니다.

## 테스트 범위

현재 테스트는 다음 동작을 검증합니다.

- 종목 검색, 필터, 정렬, 잘못된 enum 요청 처리
- 지수 생성 검증과 구성종목 저장
- 지수 목록 검색, 정렬
- 지수 시세 검색, 정렬, 미계산 지수 제외
- 지수 수정 시 이름과 구성종목 교체
- 지수 삭제와 존재하지 않는 ID 처리
