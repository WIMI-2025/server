# WIMI - 채팅 답장 추천 AI 챗봇 서비스 API 명세서

## 개요

WIMI(Replies That Fit with Me)는 AI 미로가 사용자와 함께 채팅 상황을 분석하고, 상황에 맞는 답장을 추천해주는 챗봇 서비스입니다. 

이 API 명세서는 Flutter + Firebase + Spring Boot로 개발된 WIMI 서비스의 백엔드 API에 대한 정보를 제공합니다.

## 기본 정보

- 기본 URL: `https://api.wimi.com` (예시)
- 인증: 현재 명시되지 않음 (추후 Firebase Authentication 토큰 사용 예정)
- 응답 형식: JSON

## API 엔드포인트

### 1. 이미지 업로드 API

채팅 스크린샷 분석을 위한 이미지 업로드 API입니다.

#### 요청

```
POST /image
```

**매개변수**:

| 이름 | 위치 | 타입 | 필수 | 설명 |
|------|------|------|------|------|
| file | form-data | File | 예 | 업로드할 이미지 파일 |
| user_uid | form-data | String | 예 | 사용자 고유 ID |
| image_uid | form-data | String | 예 | 이미지 고유 ID |

**예시**:
```
curl -X POST "https://api.wimi.com/image" \
  -H "Content-Type: multipart/form-data" \
  -F "file=@screenshot.jpg" \
  -F "user_uid=user123" \
  -F "image_uid=img456"
```

#### 응답

**상태 코드**: 200 OK

**본문**:
```json
{
  "imageUrl": "https://cdn.wimi.com/users/user123/images/img456.jpg"
}
```

### 2. 이미지 삭제 API

업로드한 이미지를 삭제하는 API입니다.

#### 요청

```
DELETE /image
```

**본문**:
```json
{
  "user_uid": "user123",
  "image_uid": "img456"
}
```

**예시**:
```
curl -X DELETE "https://api.wimi.com/image" \
  -H "Content-Type: application/json" \
  -d '{"user_uid":"user123", "image_uid":"img456"}'
```

#### 응답

**상태 코드**: 200 OK

**본문**: 없음

### 3. 채팅 스크린샷 분석 API

채팅 스크린샷을 분석하여 상황에 맞는 답장을 추천하는 API입니다.

#### 요청

```
POST /chatbot/analysis
```

**본문**:
```json
{
  "name": "상대방이름",
  "imageUrl": "https://cdn.wimi.com/users/user123/images/img456.jpg",
  "relationship": "업무 관계",
  "situation": "프로젝트 진행 중 상사에게 보고하는 상황"
}
```

**예시**:
```
curl -X POST "https://api.wimi.com/chatbot/analysis" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "김부장",
    "imageUrl": "https://cdn.wimi.com/users/user123/images/img456.jpg",
    "relationship": "업무 관계",
    "situation": "프로젝트 진행 중 상사에게 보고하는 상황"
  }'
```

#### 응답

**상태 코드**: 200 OK

**본문**:
```json
{
  "messageResponse": "김부장님, 말씀하신 내용 확인했습니다. 현재 프로젝트 진행 상황은 예정대로 진행 중이며, 요청하신 자료는 오늘 오후까지 준비하여 공유드리겠습니다. 추가로 필요하신 사항이 있으시면 말씀해 주세요.",
  "chatId": "chat789"
}
```

### 4. 채팅 답장 추천 API

사용자의 채팅 내용을 분석하여 상황에 맞는 답장을 추천하는 API입니다.

#### 요청

```
POST /chatbot/chat
```

**본문**:
```json
{
  "messageType": "text",
  "messageRequest": "오늘 저녁에 시간 되세요? 같이 식사하면서 프로젝트 얘기 좀 하고 싶은데요.",
  "chatId": "chat789",
  "relationship": "업무 관계"
}
```

**예시**:
```
curl -X POST "https://api.wimi.com/chatbot/chat" \
  -H "Content-Type: application/json" \
  -d '{
    "messageType": "text",
    "messageRequest": "오늘 저녁에 시간 되세요? 같이 식사하면서 프로젝트 얘기 좀 하고 싶은데요.",
    "chatId": "chat789",
    "relationship": "업무 관계"
  }'
```

#### 응답

**상태 코드**: 200 OK

**본문**:
```json
{
  "messageResponse": "네, 오늘 저녁 시간 괜찮습니다. 몇 시쯤 생각하고 계신가요? 프로젝트 관련해서 논의할 점이 있으시다면 미리 준비하겠습니다.",
  "chatId": "chat789"
}
```

## 오류 응답

모든 API는 다음과 같은 오류 응답을 반환할 수 있습니다:

### 잘못된 요청

**상태 코드**: 400 Bad Request

**본문**:
```json
{
  "error": "사용자 ID와 이미지 ID는 필수입니다."
}
```

### 서버 오류

**상태 코드**: 500 Internal Server Error

**본문**:
```json
{
  "error": "서버 오류가 발생했습니다: 세부 오류 메시지"
}
```

### 파일 크기 초과

**상태 코드**: 413 Payload Too Large

**본문**:
```json
{
  "error": "파일 크기가 너무 큽니다."
}
```

## 테스트를 위한 예시 데이터

### 1. 이미지 업로드 예시

**요청**:
```
POST /image
Content-Type: multipart/form-data

file: [이미지 파일]
user_uid: user123
image_uid: img456
```

**응답**:
```json
{
  "imageUrl": "https://cdn.wimi.com/users/user123/images/img456.jpg"
}
```

### 2. 채팅 스크린샷 분석 예시

#### 비즈니스 상황 예시

**요청**:
```json
{
  "name": "김부장",
  "imageUrl": "https://cdn.wimi.com/users/user123/images/img456.jpg",
  "relationship": "업무 관계",
  "situation": "프로젝트 진행 중 상사에게 보고하는 상황"
}
```

**응답**:
```json
{
  "messageResponse": "김부장님, 말씀하신 내용 확인했습니다. 현재 프로젝트 진행 상황은 예정대로 진행 중이며, 요청하신 자료는 오늘 오후까지 준비하여 공유드리겠습니다. 추가로 필요하신 사항이 있으시면 말씀해 주세요.",
  "chatId": "chat789"
}
```

#### 연인 관계 예시

**요청**:
```json
{
  "name": "지민",
  "imageUrl": "https://cdn.wimi.com/users/user123/images/img457.jpg",
  "relationship": "연인 관계",
  "situation": "데이트 약속을 잡는 상황"
}
```

**응답**:
```json
{
  "messageResponse": "지민아, 이번 주말에 시간 괜찮으면 같이 영화 보러 갈까? 요즘 평점 좋은 영화가 몇 개 있더라구. 보고 싶은 영화 있으면 말해줘~",
  "chatId": "chat790"
}
```

#### 친구 관계 예시

**요청**:
```json
{
  "name": "현우",
  "imageUrl": "https://cdn.wimi.com/users/user123/images/img458.jpg",
  "relationship": "친구 관계",
  "situation": "오랜만에 연락이 온 상황"
}
```

**응답**:
```json
{
  "messageResponse": "오랜만이다 현우야! 잘 지냈어? 연락줘서 반갑다. 나는 요즘 일 때문에 좀 바쁘게 지내고 있