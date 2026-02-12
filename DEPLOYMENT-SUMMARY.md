# Play Store 배포 준비 완료 요약

## 완료된 작업 (Task 8-7 + 8-8)

### 1. Play Store 등록정보 생성 ✅

#### 한국어 설명 파일
```
app/src/main/play/listings/ko-KR/
├── title.txt              # 앱 제목: "볼링클럽 - 동호회 관리 앱"
├── short-description.txt  # 짧은 설명 (80자 이내)
└── full-description.txt   # 전체 설명 (4000자 이내)
```

#### 메타데이터 파일
```
app/src/main/play/
├── contact-email.txt      # bowlingclub.app@gmail.com
└── default-language.txt   # ko-KR
```

### 2. 개인정보처리방침 작성 ✅

**위치**: `docs/privacy-policy.md`

**내용**:
- 수집하는 개인정보 (회원 정보, 점수 기록)
- 이용 목적 (회원 관리, 통계 분석)
- 보관 방법 (로컬 저장, 서버 없음)
- 외부 서비스 (Naver CLOVA OCR)
- 카메라 권한 설명
- 연락처 정보

**다음 단계**: 웹에 호스팅하여 URL 확보 필요

### 3. 서명 설정 추가 ✅

#### app/build.gradle.kts 변경사항
- `versionCode = 1` (유지)
- `versionName = "1.0.0"` (SemVer 형식으로 변경)
- `signingConfigs` 블록 추가 (release 서명 설정)
- `buildTypes.release`에 `signingConfig` 연결

#### 서명 설정 특징
- `local.properties`에서 keystore 경로 및 비밀번호 읽기
- 설정이 없어도 빌드 실패 없음 (graceful handling)
- 민감한 정보 하드코딩 방지

### 4. 서명 설정 템플릿 생성 ✅

**파일**: `local.properties.template`

**포함 내용**:
- Keystore 파일 경로 설정 방법
- 비밀번호 및 alias 설정 예시
- Keystore 생성 명령어
- 보안 주의사항

### 5. 배포 가이드 문서 작성 ✅

**파일**: `docs/deployment-guide.md`

**섹션**:
1. 서명 키 생성 방법
2. Release APK/AAB 빌드 절차
3. Play Console 등록 단계별 가이드
4. 스토어 등록정보 작성 방법
5. 콘텐츠 등급 설정
6. 앱 버전 업데이트 방법
7. 배포 전 체크리스트
8. 문제 해결 가이드
9. 참고 자료 링크

### 6. 에셋 체크리스트 작성 ✅

**파일**: `docs/play-store-assets-checklist.md`

**포함 내용**:
- 완료된 항목 목록
- 준비 필요한 그래픽 에셋 상세 (아이콘, 스크린샷, 배너)
- 스토어 등록정보 체크리스트
- 스크린샷 촬영 가이드
- 다음 단계 안내

## 생성된 파일 목록

```
BowlingClubApp/
├── app/
│   ├── build.gradle.kts                               # 서명 설정 추가됨
│   └── src/main/play/
│       ├── contact-email.txt                          # 연락처
│       ├── default-language.txt                       # 기본 언어
│       └── listings/ko-KR/
│           ├── title.txt                              # 앱 제목
│           ├── short-description.txt                  # 짧은 설명
│           └── full-description.txt                   # 전체 설명
├── docs/
│   ├── privacy-policy.md                              # 개인정보처리방침
│   ├── deployment-guide.md                            # 배포 가이드
│   └── play-store-assets-checklist.md                 # 에셋 체크리스트
├── local.properties.template                          # 서명 설정 템플릿
└── DEPLOYMENT-SUMMARY.md                              # 이 파일
```

## 다음 단계 (수동 작업 필요)

### 1. Keystore 생성 및 설정
```bash
# 1. Release keystore 생성
keytool -genkey -v -keystore release.keystore -alias bowlingclub \
  -keyalg RSA -keysize 2048 -validity 10000

# 2. local.properties 생성 및 설정
cp local.properties.template local.properties
# 편집기로 열어 실제 경로와 비밀번호 입력

# 3. Keystore 백업 (중요!)
# 안전한 위치에 release.keystore 파일 백업
```

### 2. 그래픽 에셋 준비

#### 필수 에셋:
- [ ] **앱 아이콘** (512x512 PNG)
  - Android Studio: res 우클릭 → New → Image Asset
- [ ] **스크린샷** (최소 2개)
  - 메인 화면 (회원 목록)
  - 정기전 관리 화면
  - 추가 권장: 점수 입력, 통계, 팀전 결과
- [ ] **홍보용 그래픽** (1024x500 JPG/PNG)

### 3. 개인정보처리방침 호스팅

**옵션 1: GitHub Pages**
```bash
# 1. docs/privacy-policy.md를 HTML로 변환
# 2. GitHub Pages 활성화 (Settings → Pages)
# 3. URL: https://yourusername.github.io/BowlingClubApp/privacy-policy.html
```

**옵션 2: 웹사이트**
- 자체 웹사이트에 게시
- HTTPS URL 필수

### 4. Release 빌드 및 테스트
```bash
# AAB (권장)
./gradlew bundleRelease

# 또는 APK
./gradlew assembleRelease

# 빌드 결과 확인
ls -lh app/build/outputs/bundle/release/app-release.aab
```

### 5. Play Console 등록
1. https://play.google.com/console 접속
2. 개발자 계정 등록 (25 USD)
3. 새 앱 만들기
4. 스토어 등록정보 입력 (위 생성된 파일 사용)
5. 그래픽 에셋 업로드
6. AAB/APK 업로드
7. 콘텐츠 등급 설문지 작성
8. 검토용 제출

## 참고 문서

- **배포 절차**: `docs/deployment-guide.md`
- **에셋 준비**: `docs/play-store-assets-checklist.md`
- **개인정보처리방침**: `docs/privacy-policy.md`
- **서명 설정**: `local.properties.template`

## 주의사항

### 보안
- ⚠️ `release.keystore` 파일을 Git에 커밋하지 마세요
- ⚠️ `local.properties` 파일은 이미 .gitignore에 포함됨
- ⚠️ Keystore 비밀번호를 안전하게 보관하세요
- ⚠️ Keystore 파일을 분실하면 앱 업데이트 불가능

### 버전 관리
- 첫 출시: `versionCode = 1`, `versionName = "1.0.0"`
- 업데이트 시: versionCode는 1씩 증가, versionName은 SemVer 규칙 준수
  - 패치: 1.0.1 (버그 수정)
  - 마이너: 1.1.0 (기능 추가)
  - 메이저: 2.0.0 (호환성 변경)

## 체크리스트 요약

**완료됨** ✅:
- [x] Play Store 등록정보 파일 생성
- [x] 개인정보처리방침 작성
- [x] 서명 설정 코드 추가
- [x] 버전 정보 업데이트 (1.0.0)
- [x] 배포 가이드 문서 작성
- [x] 에셋 체크리스트 작성

**남은 작업** 📋:
- [ ] Release keystore 생성
- [ ] local.properties 설정
- [ ] 앱 아이콘 제작 (512x512)
- [ ] 스크린샷 촬영 (최소 2개)
- [ ] 홍보용 그래픽 제작 (1024x500)
- [ ] 개인정보처리방침 URL 호스팅
- [ ] Release 빌드 테스트
- [ ] Play Console 계정 등록
- [ ] 앱 등록 및 제출

---

**작성일**: 2026-02-09
**작성자**: Claude Code (Sisyphus-Junior)
**태스크**: Task 8-7 + 8-8 (Play Store 배포 준비)
