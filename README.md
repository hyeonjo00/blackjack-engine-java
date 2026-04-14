# blackjack-engine-java

[English](./README.md) | [한국어](./README.ko.md) | [日本語](./README.ja.md)

A portfolio-grade Blackjack Engine project built with Java and JavaFX.
It separates core game rules, strategy analysis, recommendation analytics, session persistence,
and the desktop UI so the engine remains reusable and easy to extend.

## Overview

This project is designed around the following goals:

- A reusable blackjack engine with game rules separated from UI code
- Real gameplay flow with `Hit`, `Stand`, `Double`, and `Split`
- Strategy analysis with `Best Move`, `Expected EV`, `Bust Risk`, and `Dealer Bust Chance`
- Recommendation-following analytics
- A premium JavaFX table UI built with FXML and CSS
- Session save/load and statistics export

## Features

- Single-player vs dealer blackjack
- Fresh 52-card deck creation and shuffle at the start of each round
- Immediate natural blackjack detection
- `Hit / Stand / Double Down / Split`
- Sequential play for split hands
- Real-time strategy analysis panel
  - `Best Move`
  - `Reason`
  - `Confidence`
  - `Expected EV`
  - `Bust Risk`
  - `Dealer Bust Chance`
- Recommendation analytics
- Session statistics
  - wins / losses / pushes
  - blackjacks
  - split / double down usage
  - recommendation accuracy
  - session profit/loss
  - best win streak
- Session save / load / statistics JSON export
- Card dealing animation, dealer hidden-card reveal, and chip animation

## Architecture

Main layers:

- Engine layer: `src/main/java/com/blackjackengine`
- UI layer: `src/main/java/com/blackjackengine/ui`
- FXML / CSS / card resources: `src/main/resources`
- Technical documents: `docs`

Core responsibilities:

- `Card`, `Deck`, `Hand`: card modeling and score calculation
- `Player`, `Dealer`, `PlayerHand`: participant and hand state
- `GameEngine`: round lifecycle, betting, branching, settlement
- `StrategyAdvisor`: rule-based recommendations and EV / risk calculations
- `RecommendationAnalytics`: follow-vs-ignore recommendation tracking
- `GameStatistics`: session statistics aggregation
- `SessionPersistence`: save / restore / export
- `BlackjackController`: JavaFX UI synchronization and animation orchestration
- `CardImageMapper`: card PNG loading and safe fallback handling

## Project Structure

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

## Run

### Console version

From PowerShell in the project root:

```powershell
.\run.ps1
```

### JavaFX UI

From PowerShell in the project root:

```powershell
.\run-ui.ps1
```

On the first UI run, `run-ui.ps1` downloads the required JavaFX runtime jars into `.javafx-cache/`
because standard JDK builds typically do not bundle JavaFX.

### Compile only

```powershell
.\run-ui.ps1 -CompileOnly
```

## Manual Compile Example

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

## Documentation

The `docs` folder includes technical whitepapers for the project:

- English PDF: `docs/premium-javafx-blackjack-engine-whitepaper.pdf`
- Korean PDF: `docs/premium-javafx-blackjack-engine-whitepaper-ko.pdf`
- English HTML source: `docs/blackjack-engine-algorithm-design.html`
- Korean HTML source: `docs/blackjack-engine-algorithm-design-ko.html`

## Implementation Notes

- Starting chips can be entered on the start screen.
- Each round uses a fresh deck.
- Natural blackjack currently pays even money, not 3:2.
- The current implementation allows one split per round.
- The dealer stops drawing at `score >= 17`.
- The strategy system is rule-based and heuristic-driven, not Monte Carlo-based.

## Distribution Notes

This repository does not currently include a separate external release pipeline.
For a practical “ready to distribute” check, use:

- up-to-date `README.md`, `README.ko.md`, and `README.ja.md`
- current `docs` artifacts
- `.\run-ui.ps1 -CompileOnly` to verify compilation
- `.\run-ui.ps1` to verify the desktop UI directly when needed

## Future Ideas

- Monte Carlo EV mode
- Reinforcement learning strategy mode
- Multi-player table support
- Online leaderboard
- Probability heatmap visualization
- Mobile client
