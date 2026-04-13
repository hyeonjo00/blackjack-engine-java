# blackjack-engine-java

[English](./README.md) | [Korean](./README.ko.md) | [Japanese](./README.ja.md)

Java와 JavaFX로 만든 포트폴리오형 블랙잭 엔진 프로젝트입니다.  
게임 규칙, 전략 분석, 추천 분석, 세션 영속화, 데스크톱 UI를 분리해서 구성해
엔진 재사용성과 확장성을 높였습니다.

## 프로젝트 개요

이 프로젝트는 다음 목표를 중심으로 설계되었습니다.

- 규칙 로직을 UI 코드와 분리한 재사용 가능한 블랙잭 엔진
- `Hit`, `Stand`, `Double`, `Split` 를 지원하는 실제 플레이 흐름
- `Best Move`, `Expected EV`, `Bust Risk`, `Dealer Bust Chance` 를 보여주는 전략 분석
- 추천을 따랐는지 추적하는 추천 분석 시스템
- FXML + CSS 기반의 프리미엄 JavaFX 테이블 UI
- 세션 저장/불러오기 및 통계 내보내기 지원

## 주요 기능

- 싱글 플레이어 vs 딜러 블랙잭
- 매 라운드 시작 시 새 52장 덱 생성 및 셔플
- 자연 블랙잭 즉시 판정
- `Hit / Stand / Double Down / Split`
- split 이후 다중 손패를 순차적으로 플레이
- 실시간 전략 분석 패널
  - `Best Move`
  - `Reason`
  - `Confidence`
  - `Expected EV`
  - `Bust Risk`
  - `Dealer Bust Chance`
- 추천 추종 여부 분석
- 세션 통계
  - wins / losses / pushes
  - blackjacks
  - split / double down usage
  - recommendation accuracy
  - session profit/loss
  - best win streak
- 세션 저장 / 불러오기 / 통계 JSON export
- 카드 딜링 애니메이션, dealer hidden-card reveal, 칩 애니메이션

## 아키텍처

주요 계층:

- 엔진 계층: `src/main/java/com/blackjackengine`
- UI 계층: `src/main/java/com/blackjackengine/ui`
- FXML / CSS / 카드 리소스: `src/main/resources`
- 기술 문서: `docs`

핵심 역할:

- `Card`, `Deck`, `Hand`: 카드 모델과 점수 계산
- `Player`, `Dealer`, `PlayerHand`: 참가자와 손패 상태
- `GameEngine`: 라운드 생명주기, 베팅, 분기, 정산
- `StrategyAdvisor`: 규칙 기반 추천과 EV / risk 계산
- `RecommendationAnalytics`: 추천 추종 여부 분석
- `GameStatistics`: 세션 통계 집계
- `SessionPersistence`: 저장 / 복원 / export
- `BlackjackController`: JavaFX UI 동기화와 애니메이션 제어
- `CardImageMapper`: 카드 PNG 로딩과 fallback 처리

## 프로젝트 구조

```text
blackjack-engine-java
|-- README.md
|-- README.ko.md
|-- README.ja.md
|-- run.ps1
|-- run-ui.ps1
|-- docs
|   |-- premium-javafx-blackjack-engine-whitepaper.pdf
|   |-- premium-javafx-blackjack-engine-whitepaper-ko.pdf
|   |-- blackjack-engine-algorithm-design.html
|   `-- blackjack-engine-algorithm-design-ko.html
|-- src
|   `-- main
|       |-- java
|       |   `-- com
|       |       `-- blackjackengine
|       |           |-- Card.java
|       |           |-- Deck.java
|       |           |-- Hand.java
|       |           |-- Player.java
|       |           |-- Dealer.java
|       |           |-- PlayerHand.java
|       |           |-- GameEngine.java
|       |           |-- StrategyAdvisor.java
|       |           |-- RecommendationAnalytics.java
|       |           |-- SessionPersistence.java
|       |           `-- ...
|       `-- resources
|           |-- cards
|           |-- ui
|           |   |-- blackjack-table.fxml
|           |   |-- start-screen.fxml
|           |   `-- blackjack.css
|           `-- ui-assets
```

## 실행 방법

### 콘솔 버전

프로젝트 루트 PowerShell에서:

```powershell
.\run.ps1
```

### JavaFX UI

프로젝트 루트 PowerShell에서:

```powershell
.\run-ui.ps1
```

첫 UI 실행 시에는 기본 JDK에 JavaFX가 포함되지 않을 수 있으므로,
`run-ui.ps1`이 `.javafx-cache/` 아래에 필요한 JavaFX runtime jar를 자동으로 내려받습니다.

### 컴파일만 확인

```powershell
.\run-ui.ps1 -CompileOnly
```

## 수동 컴파일 예시

```powershell
$outDir = Join-Path $PWD "out"
if (-not (Test-Path $outDir)) {
    New-Item -ItemType Directory -Path $outDir | Out-Null
}

$sources = Get-ChildItem -Path .\src\main\java -Recurse -Filter *.java |
    Select-Object -ExpandProperty FullName

javac -d $outDir $sources
java -cp $outDir com.blackjackengine.Main
```

## 문서

`docs` 폴더에는 프로젝트용 기술 화이트페이퍼가 포함되어 있습니다.

- 영문 PDF: `docs/premium-javafx-blackjack-engine-whitepaper.pdf`
- 국문 PDF: `docs/premium-javafx-blackjack-engine-whitepaper-ko.pdf`
- 영문 HTML 소스: `docs/blackjack-engine-algorithm-design.html`
- 국문 HTML 소스: `docs/blackjack-engine-algorithm-design-ko.html`

## 구현 메모

- 시작 칩 수는 시작 화면에서 직접 입력할 수 있습니다.
- 각 라운드는 fresh deck 기반으로 진행됩니다.
- 현재 자연 블랙잭 payout은 3:2가 아니라 even-money 방식입니다.
- 현재 구현은 라운드당 1회 split만 허용합니다.
- dealer는 `score >= 17`이 되면 더 이상 draw 하지 않습니다.
- 전략 추천은 Monte Carlo가 아니라 rule-based heuristic 방식입니다.

## 배포 메모

이 저장소에는 별도 외부 릴리스 파이프라인은 포함되어 있지 않습니다.  
실질적인 “배포 가능한 상태” 확인 기준은 아래와 같습니다.

- `README.md`, `README.ko.md`, `README.ja.md` 최신화
- `docs` 산출물 최신 상태 유지
- `.\run-ui.ps1 -CompileOnly`로 컴파일 검증
- 필요 시 `.\run-ui.ps1`로 실제 UI 실행 확인

## 향후 확장 아이디어

- Monte Carlo 기반 EV 모드
- reinforcement learning 전략 모드
- multi-player 테이블 구조
- 온라인 리더보드
- 확률 heatmap 시각화
- 모바일 클라이언트
