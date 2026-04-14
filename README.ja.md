# blackjack-engine-java

[English](./README.md) | [한국어](./README.ko.md) | [日本語](./README.ja.md)

Java と JavaFX で構築した、ポートフォリオ向けの Blackjack Engine プロジェクトです。  
ゲームルール、戦略分析、推薦分析、セッション永続化、デスクトップ UI を分離し、
エンジンの再利用性と拡張性を高めています。

## プロジェクト概要

このプロジェクトは、次の目標を中心に設計されています。

- UI コードから分離された再利用可能なブラックジャックエンジン
- `Hit`, `Stand`, `Double`, `Split` を含む実際のゲーム進行
- `Best Move`, `Expected EV`, `Bust Risk`, `Dealer Bust Chance` を表示する戦略分析
- 推薦に従ったかどうかを追跡する推薦分析システム
- FXML + CSS ベースのプレミアム JavaFX テーブル UI
- セッション保存 / 読み込み / 統計エクスポート

## 主な機能

- シングルプレイヤー vs ディーラーのブラックジャック
- 各ラウンド開始時に新しい 52 枚デッキを生成してシャッフル
- ナチュラルブラックジャックの即時判定
- `Hit / Stand / Double Down / Split`
- split 後の複数ハンドを順番にプレイ
- リアルタイム戦略分析パネル
  - `Best Move`
  - `Reason`
  - `Confidence`
  - `Expected EV`
  - `Bust Risk`
  - `Dealer Bust Chance`
- 推薦追従分析
- セッション統計
  - wins / losses / pushes
  - blackjacks
  - split / double down usage
  - recommendation accuracy
  - session profit/loss
  - best win streak
- セッション保存 / 読み込み / 統計 JSON export
- カード配布アニメーション、dealer hidden-card reveal、チップアニメーション

## アーキテクチャ

主なレイヤー:

- エンジン層: `src/main/java/com/blackjackengine`
- UI 層: `src/main/java/com/blackjackengine/ui`
- FXML / CSS / カードリソース: `src/main/resources`
- 技術文書: `docs`

主要な責務:

- `Card`, `Deck`, `Hand`: カードモデルとスコア計算
- `Player`, `Dealer`, `PlayerHand`: 参加者とハンド状態
- `GameEngine`: ラウンドライフサイクル、ベット、分岐、精算
- `StrategyAdvisor`: ルールベース推薦と EV / risk 計算
- `RecommendationAnalytics`: 推薦追従分析
- `GameStatistics`: セッション統計集計
- `SessionPersistence`: 保存 / 復元 / export
- `BlackjackController`: JavaFX UI 同期とアニメーション制御
- `CardImageMapper`: カード PNG 読み込みと fallback 処理

## プロジェクト構成

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

## 実行方法

### コンソール版

プロジェクトルートの PowerShell で:

```powershell
.\run.ps1
```

### JavaFX UI

プロジェクトルートの PowerShell で:

```powershell
.\run-ui.ps1
```

初回 UI 実行時には、標準 JDK に JavaFX が含まれていない場合があるため、
`run-ui.ps1` が `.javafx-cache/` 配下に必要な JavaFX runtime jar を自動でダウンロードします。

### コンパイルのみ確認

```powershell
.\run-ui.ps1 -CompileOnly
```

## 手動コンパイル例

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

## ドキュメント

`docs` フォルダにはプロジェクト用の技術ホワイトペーパーが含まれています。

- 英語 PDF: `docs/premium-javafx-blackjack-engine-whitepaper.pdf`
- 韓国語 PDF: `docs/premium-javafx-blackjack-engine-whitepaper-ko.pdf`
- 英語 HTML ソース: `docs/blackjack-engine-algorithm-design.html`
- 韓国語 HTML ソース: `docs/blackjack-engine-algorithm-design-ko.html`

## 実装メモ

- 開始チップ数はスタート画面で直接入力できます。
- 各ラウンドは fresh deck ベースで進行します。
- 現在の natural blackjack payout は 3:2 ではなく even-money 方式です。
- 現在の実装では 1 ラウンドにつき 1 回の split のみを許可しています。
- dealer は `score >= 17` になるとそれ以上 draw しません。
- 戦略推薦は Monte Carlo ではなく rule-based heuristic 方式です。

## 配布メモ

このリポジトリには外部向けの独立したリリースパイプラインは含まれていません。  
実用的な「配布可能状態」の確認基準は次の通りです。

- `README.md`, `README.ko.md`, `README.ja.md` を最新状態に保つ
- `docs` の成果物を最新状態に保つ
- `.\run-ui.ps1 -CompileOnly` でコンパイル検証
- 必要に応じて `.\run-ui.ps1` で実際の UI を起動して確認

## 今後の拡張アイデア

- Monte Carlo ベースの EV モード
- reinforcement learning 戦略モード
- multi-player テーブル構造
- オンラインリーダーボード
- 確率 heatmap 可視化
- モバイルクライアント
