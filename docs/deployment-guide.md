# Play Store 배포 가이드

## 1. 서명 키 생성

### 1.1 Release Keystore 생성

```bash
keytool -genkey -v -keystore release.keystore -alias bowlingclub -keyalg RSA -keysize 2048 -validity 10000
```

다음 정보를 입력하세요:
- Keystore 비밀번호 (최소 6자)
- 이름 및 조직 정보
- Key 비밀번호 (Keystore 비밀번호와 같게 설정 가능)

### 1.2 Keystore 백업

생성된 `release.keystore` 파일을:
1. 안전한 위치에 백업 (USB, 클라우드 등)
2. 비밀번호를 안전하게 보관
3. **절대 Git에 커밋하지 마세요**

### 1.3 local.properties 설정

1. `local.properties.template`을 `local.properties`로 복사
2. 다음 값을 입력:

```properties
RELEASE_KEYSTORE_FILE=/absolute/path/to/release.keystore
RELEASE_KEYSTORE_PASSWORD=your_keystore_password
RELEASE_KEY_ALIAS=bowlingclub
RELEASE_KEY_PASSWORD=your_key_password
```

## 2. Release APK 빌드

### 2.1 빌드 명령어

```bash
./gradlew assembleRelease
```

### 2.2 APK 위치

빌드가 완료되면 APK가 다음 경로에 생성됩니다:
```
app/build/outputs/apk/release/app-release.apk
```

### 2.3 빌드 검증

```bash
# APK 서명 확인
jarsigner -verify -verbose -certs app/build/outputs/apk/release/app-release.apk

# APK 정보 확인
aapt dump badging app/build/outputs/apk/release/app-release.apk
```

## 3. Play Store 등록

### 3.1 Play Console 접속

1. [Google Play Console](https://play.google.com/console) 로그인
2. 개발자 계정 등록 (25 USD 일회성 등록비)

### 3.2 앱 등록

1. **앱 만들기** 클릭
2. 기본 정보 입력:
   - 앱 이름: 볼링클럽 - 동호회 관리 앱
   - 기본 언어: 한국어 (ko-KR)
   - 앱 유형: 앱
   - 무료/유료: 무료

### 3.3 스토어 등록정보 작성

#### 앱 세부정보
- **앱 이름**: `app/src/main/play/listings/ko-KR/title.txt`
- **간단한 설명**: `app/src/main/play/listings/ko-KR/short-description.txt`
- **전체 설명**: `app/src/main/play/listings/ko-KR/full-description.txt`

#### 그래픽 에셋 (필수)
- **앱 아이콘**: 512x512 PNG (32비트, 투명도 없음)
- **주요 스크린샷**: 최소 2개, 최대 8개
  - 휴대전화: 320dp ~ 3840dp
  - 16:9 또는 9:16 비율 권장
- **홍보용 그래픽**: 1024x500 JPG/PNG

#### 분류
- **앱 카테고리**: 도구 (Tools)
- **콘텐츠 등급**: 모든 연령

### 3.4 개인정보처리방침

1. 개인정보처리방침 URL 등록 필요
2. `docs/privacy-policy.md` 내용을:
   - GitHub Pages에 호스팅하거나
   - 웹사이트에 게시
3. URL을 Play Console에 입력

예: `https://yourusername.github.io/BowlingClubApp/privacy-policy.html`

### 3.5 콘텐츠 등급 설정

1. **콘텐츠 등급 설문지** 작성
2. 질문에 답변:
   - 폭력성: 없음
   - 성적 콘텐츠: 없음
   - 욕설: 없음
   - 약물/주류: 없음
3. 등급 확인 (한국: 전체 이용가 예상)

### 3.6 타겟 국가 및 기기

- **국가/지역**: 대한민국 (추가 국가 선택 가능)
- **기기**: 휴대전화, 태블릿

## 4. 앱 버전 업로드

### 4.1 프로덕션 트랙

1. **프로덕션** 트랙 선택
2. **새 출시 만들기**
3. **APK/AAB 업로드** (AAB 권장)

### 4.2 AAB (Android App Bundle) 생성

AAB는 APK보다 권장됩니다 (파일 크기 최적화):

```bash
./gradlew bundleRelease
```

AAB 위치:
```
app/build/outputs/bundle/release/app-release.aab
```

### 4.3 출시 노트 작성

```
버전 1.0.0 - 첫 출시

[주요 기능]
- 정기전 생성 및 참가자 관리
- OCR 점수 자동 인식
- 회원 관리 시스템
- 상세 통계 및 차트
- 팀전 시스템
- 공유 기능
- 데이터 백업/복원
```

## 5. 검토 및 게시

### 5.1 앱 검토 제출

1. 모든 정보 입력 완료 확인
2. **검토용으로 제출** 클릭
3. Google 검토 대기 (일반적으로 1-3일)

### 5.2 검토 상태 확인

- Play Console 대시보드에서 상태 확인
- 거부될 경우 피드백에 따라 수정 후 재제출

## 6. 버전 업데이트

### 6.1 버전 번호 증가

`app/build.gradle.kts` 수정:

```kotlin
defaultConfig {
    versionCode = 2      // 1씩 증가
    versionName = "1.0.1"  // SemVer 규칙
}
```

### 6.2 빌드 및 업로드

```bash
./gradlew bundleRelease
```

새 AAB를 Play Console에 업로드

### 6.3 출시 노트 작성

```
버전 1.0.1

[수정사항]
- 버그 수정: ...
- 성능 개선: ...
```

## 7. 체크리스트

배포 전 필수 확인사항:

- [ ] Release keystore 생성 및 백업
- [ ] local.properties 설정 완료
- [ ] 앱 아이콘 준비 (512x512)
- [ ] 스크린샷 준비 (최소 2개)
- [ ] 개인정보처리방침 URL 준비
- [ ] versionCode 및 versionName 확인
- [ ] ProGuard 규칙 테스트
- [ ] Release APK/AAB 빌드 성공
- [ ] 실제 기기에서 테스트
- [ ] 모든 주요 기능 동작 확인

## 8. 문제 해결

### 8.1 서명 오류

```
Execution failed for task ':app:packageRelease'.
```

해결:
- local.properties 경로 확인
- Keystore 비밀번호 확인
- Keystore 파일 존재 여부 확인

### 8.2 ProGuard 오류

특정 클래스가 난독화되어 오류 발생 시:

`app/proguard-rules.pro`에 추가:
```proguard
-keep class com.bowlingclub.app.data.** { *; }
-keep class com.bowlingclub.app.ui.** { *; }
```

### 8.3 APK 크기 초과

- 사용하지 않는 리소스 제거
- 이미지 압축 (WebP 형식)
- 언어별 APK 분리 고려

## 9. 참고 자료

- [Android 앱 서명](https://developer.android.com/studio/publish/app-signing)
- [Play Console 도움말](https://support.google.com/googleplay/android-developer)
- [앱 번들 가이드](https://developer.android.com/guide/app-bundle)
- [콘텐츠 정책](https://play.google.com/about/developer-content-policy/)

---

**작성일**: 2026-02-09
**버전**: 1.0
