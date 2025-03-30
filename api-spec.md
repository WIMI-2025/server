# WIMI - 채팅 답장 추천 AI 챗봇 서비스 API 명세서

## 개요

WIMI(Replies That Fit with Me)는 AI 미로가 사용자와 함께 채팅 상황을 분석하고, 상황에 맞는 답장을 추천해주는 챗봇 서비스입니다. Flutter와 Firebase를 프론트엔드로, Spring Boot를 백엔드로 사용하며, 멀티모달 LLM을 활용해 채팅 스크린샷을 분석하여 맞춤형 답장을 추천합니다.

## 기본 정보

- 인증: Firebase Authentication 토큰 사용 (설정 파일에 의존)
- 응답 형식: JSON

## API 엔드포인트

### 1. 상태 확인 API

서비스가 정상 작동 중인지 확인하는 API입니다.

#### 요청

```
GET /health
```

#### 응답

**상태 코드**: 200 OK

**본문**:
```
"Service is running"
```

### 2. 이미지 업로드 API

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
```bash
curl -X POST "https://api.example.com/image" \
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
  "imageUrl": "https://cdn.example.com/users/user123/images/img456.jpg"
}
```

### 3. 이미지 삭제 API

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
```bash
curl -X DELETE "https://api.example.com/image" \
  -H "Content-Type: application/json" \
  -d '{"user_uid":"user123", "image_uid":"img456"}'
```

#### 응답

**상태 코드**: 200 OK

### 4. 채팅 스크린샷 분석 API

채팅 스크린샷을 분석하여 상황에 맞는 답장을 추천하는 API입니다.

#### 요청

```
POST /chatbot/analysis
```

**본문**:
```json
{
  "name": "상대방이름",
  "imageUrl": "https://cdn.example.com/users/user123/images/img456.jpg",
  "relationship": "업무 관계",
  "situation": "프로젝트 진행 중 상사에게 보고하는 상황"
}
```

**예시**:
```bash
curl -X POST "https://api.example.com/chatbot/analysis" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "김부장",
    "imageUrl": "https://cdn.example.com/users/user123/images/img456.jpg",
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

### 5. 채팅 답장 추천 API

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
  "imageUrl": null
}
```

**이미지 메시지 예시**:
```json
{
  "messageType": "image",
  "messageRequest": "이 이미지에 대해 어떻게 생각하세요?",
  "chatId": "chat789",
  "imageUrl": "https://cdn.example.com/users/user123/images/img457.jpg"
}
```

**예시**:
```bash
curl -X POST "https://api.example.com/chatbot/chat" \
  -H "Content-Type: application/json" \
  -d '{
    "messageType": "text",
    "messageRequest": "오늘 저녁에 시간 되세요? 같이 식사하면서 프로젝트 얘기 좀 하고 싶은데요.",
    "chatId": "chat789"
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
  "error": "잘못된 요청입니다",
  "details": "사용자 ID와 이미지 ID는 필수입니다."
}
```

### 서버 오류

**상태 코드**: 500 Internal Server Error

**본문**:
```json
{
  "error": "서버 오류가 발생했습니다",
  "details": "세부 오류 메시지"
}
```

### 파일 크기 초과

**상태 코드**: 413 Payload Too Large

**본문**:
```json
{
  "error": "파일 크기가 너무 큽니다.",
  "details": "오류 메시지"
}
```

## 테스트를 위한 다양한 시나리오 예시 데이터

### 1. 이미지 업로드 예시

**요청**:
```
POST /image
Content-Type: multipart/form-data

file: [이미지 파일]
user_uid: user_abcd1234
image_uid: img_xyz789
```

**응답**:
```json
{
  "imageUrl": "https://cdn.example.com/users/user_abcd1234/images/img_xyz789.jpg"
}
```

### 2. 채팅 스크린샷 분석 예시

#### 비즈니스 상황 예시

**요청**:
```json
{
  "name": "김부장",
  "imageUrl": "https://cdn.example.com/users/user_abcd1234/images/img_xyz789.jpg",
  "relationship": "업무 관계",
  "situation": "프로젝트 진행 중 상사에게 보고하는 상황"
}
```

**응답**:
```json
{
  "messageResponse": "[분석]\n이 대화는 프로젝트 진행 상황에 대한 보고를 요청하는 김부장님과의 업무 대화입니다. 김부장님은 구체적인 진행 상황과 문제점에 대해 질문하고 있으며, 공식적인 어조를 사용하고 있습니다.\n\n[감성 요소]\n김부장님의 관심에 감사드리며, 프로젝트에 대한 책임감을 가지고 응답하겠습니다.\n\n[이성 요소]\n현재 프로젝트 진행 상황과 데이터를 바탕으로 명확한 정보를 제공하겠습니다.\n\n[추천 답장]\n\n1. 😊 보다 감성적인 답장 (공감 중심)\n김부장님, 관심 가져주셔서 감사합니다. 프로젝트는 계획대로 진행 중이며, 팀원들의 노력 덕분에 지난주 목표를 모두 달성했습니다. 다음 단계 준비도 차질 없이 진행하고 있으니 걱정 마세요. 혹시 더 필요하신 자료가 있으시면 말씀해 주세요!\n\n2. 👍 균형잡힌 답장 (감성과 이성의 조화)\n김부장님, 말씀하신 내용 확인했습니다. 현재 프로젝트는 일정대로 70% 완료되었으며, 다행히 큰 이슈 없이 진행 중입니다. 다음 주 중으로 1차 결과물을 공유드릴 예정이며, 요청하신 자료는 오늘 오후까지 메일로 보내드리겠습니다.\n\n3. 💡 보다 이성적인 답장 (명확한 의사소통 중심)\n김부장님께 보고드립니다. 프로젝트 진행률은 현재 70%로, 계획 대비 5% 앞서 있습니다. 지난주 발생한 시스템 연동 문제는 해결 완료했으며, 세부 진행 상황을 정리한 보고서를 첨부해 드립니다. 추가 질문이나 필요사항 있으시면 즉시 답변 드리겠습니다.",
  "chatId": "chat_7890abcd"
}
```

#### 연인 관계 예시

**요청**:
```json
{
  "name": "지민",
  "imageUrl": "https://cdn.example.com/users/user_abcd1234/images/img_xyz790.jpg",
  "relationship": "연인 관계",
  "situation": "데이트 약속을 잡는 상황"
}
```

**응답**:
```json
{
  "messageResponse": "[분석]\n지민씨가 주말 데이트 계획에 대해 물어보는 상황입니다. 메시지 톤은 친근하고 기대감이 느껴지며, 함께 시간을 보내고 싶다는 의사를 표현하고 있습니다.\n\n[감성 요소]\n지민씨의 데이트 제안에 설렘과 기대감을 표현하며 따뜻하게 반응합니다.\n\n[이성 요소]\n구체적인 시간과 장소를 제안하여 계획을 명확히 합니다.\n\n[추천 답장]\n\n1. 😊 보다 감성적인 답장 (공감 중심)\n지민아, 주말에 만나자는 네 제안 너무 기다렸어! 요즘 너무 보고 싶었거든 ❤️ 토요일이나 일요일 중에 네가 편한 날 알려줘. 같이 보낼 시간이 벌써부터 기대돼!\n\n2. 👍 균형잡힌 답장 (감성과 이성의 조화)\n응, 지민아! 이번 주말 데이트 너무 좋을 것 같아 😊 토요일 오후가 어떨까? 지난번 네가 가보고 싶다던 그 카페 다녀온 후에, 저녁에는 새로 개봉한 영화 보는 건 어때? 어떤 계획이 좋을지 네 생각도 알려줘!\n\n3. 💡 보다 이성적인 답장 (명확한 의사소통 중심)\n지민아, 주말 데이트 좋아. 토요일은 12시 이후, 일요일은 종일 가능해. 영화나 식사, 아니면 다른 활동 중에 뭐가 좋을지 알려줘. 장소는 강남이나 홍대 중 네가 편한 곳으로 맞춰볼게. 어떤 계획이 좋을지 생각해봐.",
  "chatId": "chat_7891abcd"
}
```

#### 친구 관계 예시

**요청**:
```json
{
  "name": "현우",
  "imageUrl": "https://cdn.example.com/users/user_abcd1234/images/img_xyz791.jpg",
  "relationship": "친구 관계",
  "situation": "오랜만에 연락이 온 상황"
}
```

**응답**:
```json
{
  "messageResponse": "[분석]\n오랜만에 연락한 현우가 근황을 물어보는 친구 간의 대화입니다. 친근한 어조가 느껴지며, 다시 연결하고 싶어하는 의도가 보입니다.\n\n[감성 요소]\n오랜만에 연락한 친구의 연락에 반가움을 표현하고 따뜻하게 응답합니다.\n\n[이성 요소]\n간략한 근황을 공유하고 만남의 가능성을 열어둡니다.\n\n[추천 답장]\n\n1. 😊 보다 감성적인 답장 (공감 중심)\n어, 현우야! 완전 오랜만이다! 연락줘서 너무 반가워 ㅎㅎ 보고싶었어~ 나는 요즘 그냥 평범하게 지내고 있어. 너는 어때? 요즘 뭐하고 지내? 시간 날 때 만나서 얘기하면 좋을 것 같다! 😄\n\n2. 👍 균형잡힌 답장 (감성과 이성의 조화)\n우와 현우야, 진짜 오랜만이다! 연락줘서 정말 반가워 😊 나는 요즘 회사일로 좀 바쁘게 지내는 중이야. 다행히 재미있는 프로젝트를 맡아서 해보고 있어. 너는 어떻게 지내? 다음 주 주말에 시간 되면 만나서 밥이라도 먹을래?\n\n3. 💡 보다 이성적인 답장 (명확한 의사소통 중심)\n현우야, 오랜만이네. 잘 지냈어? 나는 최근에 새 회사로 이직해서 적응하느라 바빴어. 이번달 말에는 좀 여유가 생길 것 같은데, 시간 괜찮으면 만나자. 혹시 3월 28일이나 29일 저녁 어때? 그동안 어떻게 지냈는지 듣고 싶네.",
  "chatId": "chat_7892abcd"
}
```

### 3. 채팅 답장 추천 API 예시

#### 텍스트 메시지 예시

**요청**:
```json
{
  "messageType": "text",
  "messageRequest": "내일 프로젝트 회의에 꼭 참석해주세요. 중요한 안건이 있습니다.",
  "chatId": "chat_7890abcd"
}
```

**응답**:
```json
{
  "messageResponse": "네, 김부장님. 내일 프로젝트 회의 참석 확실히 하겠습니다. 혹시 사전에 준비해야 할 자료나 검토해야 할 사항이 있으면 알려주시면 미리 준비하겠습니다. 회의 시간과 장소는 기존과 동일한가요?",
  "chatId": "chat_7890abcd"
}
```

#### 이미지 메시지 예시

**요청**:
```json
{
  "messageType": "image",
  "messageRequest": "이 디자인 시안에 대한 의견을 알려주세요.",
  "chatId": "chat_7890abcd",
  "imageUrl": "https://cdn.example.com/users/user_abcd1234/images/img_xyz792.jpg"
}
```

**응답**:
```json
{
  "messageResponse": "디자인 시안을 검토해봤습니다. 전체적인 레이아웃과 색상 구성이 프로젝트 콘셉트와 잘 맞는다고 생각합니다. 특히 메인 페이지의 사용자 흐름이 직관적으로 구성된 점이 좋습니다. 다만, 모바일 환경에서 버튼 크기가 다소 작아 보이는데, 접근성 측면에서 조금 더 크게 조정하는 것이 어떨까요? 또한 하단 푸터 영역의 정보 구성을 조금 더 간결하게 정리하면 사용자 경험이 개선될 것 같습니다. 전반적으로는 매우 좋은 방향으로 진행되고 있다고 생각합니다.",
  "chatId": "chat_7890abcd"
}
```

이 API 명세를 통해 WIMI 서비스의 기능을 이해하고 활용할 수 있습니다. 서비스는 채팅 분석 및 답장 추천을 중심으로 구성되어 있으며, 이미지 분석 기능을 통해 스크린샷을 업로드하고 상황에 맞는 답변을 받을 수 있습니다.