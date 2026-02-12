# Play Store 배포 에셋 체크리스트

## 완료된 항목 ✅

### 1. 앱 설명 (한국어)
- ✅ **제목**: `app/src/main/play/listings/ko-KR/title.txt`
- ✅ **짧은 설명**: `app/src/main/play/listings/ko-KR/short-description.txt`
- ✅ **전체 설명**: `app/src/main/play/listings/ko-KR/full-description.txt`

### 2. 개인정보처리방침
- ✅ **개인정보처리방침**: `docs/privacy-policy.md`
- ⚠️ **TODO**: 웹에 호스팅하여 URL 확보 필요

### 3. 서명 설정
- ✅ **서명 설정**: `app/build.gradle.kts`에 추가됨
- ✅ **템플릿**: `local.properties.template` 생성됨
- ⚠️ **TODO**: 실제 keystore 생성 및 `local.properties` 설정 필요

### 4. 버전 정보
- ✅ **versionCode**: 1
- ✅ **versionName**: 1.0.0 (Semantic Versioning)

### 5. 연락처 정보
- ✅ **이메일**: `app/src/main/play/contact-email.txt`
- ✅ **기본 언어**: `app/src/main/play/default-language.txt`

### 6. 문서
- ✅ **배포 가이드**: `docs/deployment-guide.md`
- ✅ **에셋 체크리스트**: 이 파일

## 준비 필요한 항목 📋

### 1. 그래픽 에셋 (필수)

#### 앱 아이콘
- [ ] **512x512 PNG** 아이콘
  - 32비트 PNG
  - 투명도 없음
  - 512x512 정사각형
  - 파일 크기: 1MB 이하

#### 스크린샷 (휴대전화용)
최소 2개, 최대 8개 필요:

1. [ ] **메인 화면** (회원 목록)
2. [ ] **정기전 관리** (대회 목록/상세)
3. [ ] **점수 입력** (OCR 또는 수동 입력)
4. [ ] **통계 화면** (차트/그래프)
5. [ ] **팀전 결과**
6. [ ] **설정 화면**

**규격**:
- 해상도: 320dp ~ 3840dp
- 비율: 16:9 또는 9:16 권장
- 형식: PNG 또는 JPEG

#### 홍보용 그래픽 (필수)
- [ ] **1024x500 JPG/PNG**
  - Play Store 상단에 표시되는 배너
  - 앱의 주요 기능 시각화

#### 추천 에셋 (선택)
- [ ] **홍보 동영상** (YouTube URL)
- [ ] **7인치 태블릿 스크린샷** (최소 2개)
- [ ] **10인치 태블릿 스크린샷** (최소 2개)

### 2. 스토어 등록정보

#### 카테고리
- [ ] **앱 카테고리**: 도구 (Tools) 또는 스포츠
- [ ] **태그**: 볼링, 동호회, 클럽, 관리, 점수

#### 타겟 사용자
- [ ] **콘텐츠 등급 설문지** 작성
- [ ] **타겟 국가**: 대한민국 선택
- [ ] **기기 지원**: 휴대전화, 태블릿

### 3. 웹 호스팅

#### 개인정보처리방침 URL
옵션 1: GitHub Pages
```bash
# docs/privacy-policy.md를 HTML로 변환
# GitHub Pages 활성화
# URL 예: https://yourusername.github.io/BowlingClubApp/privacy-policy.html
```

옵션 2: 웹사이트
- [ ] 웹사이트에 개인정보처리방침 게시
- [ ] HTTPS URL 확보

#### 앱 홈페이지 (선택)
- [ ] 앱 소개 페이지 (선택사항)

### 4. 테스트

#### 기능 테스트
- [ ] 모든 화면 동작 확인
- [ ] OCR 기능 테스트
- [ ] 데이터 백업/복원 테스트
- [ ] 공유 기능 테스트
- [ ] 다양한 기기에서 테스트

#### Release 빌드 테스트
- [ ] Release APK 빌드 성공
- [ ] ProGuard 적용 후 동작 확인
- [ ] 서명된 APK 설치 및 테스트

### 5. Play Console 계정

- [ ] Google Play Console 개발자 계정 등록 (25 USD)
- [ ] 결제 정보 등록
- [ ] 세금 정보 입력

## 스크린샷 촬영 가이드

### 준비 사항
1. 실제 앱 실행
2. 테스트 데이터 입력 (회원, 대회, 점수)
3. 에뮬레이터 또는 실제 기기 사용

### 촬영 방법
- **에뮬레이터**: Screenshot 버튼 클릭
- **실제 기기**: 전원 + 볼륨 다운 동시 누르기

### 편집 권장사항
- 상태바 제거 또는 통일 (시간 10:00, 배터리 100%)
- 깔끔한 배경
- 중요 기능 강조 (화살표, 텍스트 추가)

## 다음 단계

### 1단계: 그래픽 에셋 준비
```bash
# 앱 아이콘 생성
# Android Studio에서 Image Asset 사용:
# Right-click on res → New → Image Asset

# 스크린샷 촬영
# 위 가이드 참고
```

### 2단계: Keystore 생성
```bash
keytool -genkey -v -keystore release.keystore -alias bowlingclub -keyalg RSA -keysize 2048 -validity 10000
```

### 3단계: local.properties 설정
```bash
cp local.properties.template local.properties
# 편집기로 실제 경로 및 비밀번호 입력
```

### 4단계: Release 빌드
```bash
./gradlew assembleRelease
# 또는
./gradlew bundleRelease  # AAB 권장
```

### 5단계: Play Console 등록
1. https://play.google.com/console 접속
2. 새 앱 만들기
3. 스토어 등록정보 입력
4. APK/AAB 업로드
5. 검토용 제출

## 체크리스트 요약

**필수 항목** (배포 전 반드시 완료):
- [x] 앱 제목, 짧은 설명, 전체 설명
- [x] 서명 설정 (코드)
- [ ] 서명 Keystore 생성
- [ ] 앱 아이콘 (512x512)
- [ ] 스크린샷 (최소 2개)
- [ ] 홍보용 그래픽 (1024x500)
- [ ] 개인정보처리방침 URL
- [ ] 콘텐츠 등급 설문지
- [ ] Release APK/AAB 빌드 및 테스트

**권장 항목** (품질 향상):
- [ ] 홍보 동영상
- [ ] 다양한 스크린샷 (6-8개)
- [ ] 태블릿 스크린샷
- [ ] 앱 홈페이지

---

**작성일**: 2026-02-09
**버전**: 1.0
