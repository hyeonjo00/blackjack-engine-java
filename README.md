# blackjack-engine-java

A small Java console blackjack MVP built with clear, object-oriented classes and a lightweight `javac` workflow.

## Project Structure

```text
blackjack-engine-java
|-- run.ps1
|-- run-ui.ps1
|-- README.md
|-- src
|   |-- main
|   |   `-- java
|   |       `-- com
|   |           `-- blackjackengine
|   |               |-- Card.java
|   |               |-- Dealer.java
|   |               |-- Deck.java
|   |               |-- GameStatistics.java
|   |               |-- GameEngine.java
|   |               |-- Hand.java
|   |               |-- HandResult.java
|   |               |-- Main.java
|   |               |-- MoveRecommendation.java
|   |               |-- Player.java
|   |               |-- PlayerAction.java
|   |               |-- PlayerHand.java
|   |               |-- RecommendationAnalytics.java
|   |               |-- RecommendationDecision.java
|   |               |-- RoundOutcome.java
|   |               |-- RoundResult.java
|   |               `-- StrategyAdvisor.java
|   `-- ui
|       |-- blackjack-table.fxml
|       |-- blackjack.css
|       `-- com
|           `-- blackjackengine
|               `-- ui
|                   |-- BlackjackApplication.java
|                   `-- BlackjackController.java
```

## Run

From PowerShell in the project root:

```powershell
.\run.ps1
```

## Run The JavaFX UI

From PowerShell in the project root:

```powershell
.\run-ui.ps1
```

The first UI run downloads the JavaFX runtime jars into `.javafx-cache/` because the base JDK does not bundle JavaFX.

## Manual Compile and Run

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

## Notes

- The game starts the player with 100 chips.
- Each round uses a freshly shuffled 52-card deck.
- Natural blackjack is detected immediately after the opening deal.
- Split is supported once per round when the opening hand is a pair, creating two separately played hands with matching bets.
- Double down adds one matching bet to the active hand, deals exactly one card, and then ends that hand.
- A dedicated strategy advisor recommends hit, stand, double down, or split before each player decision.
- Recommendation analytics track whether the player followed the advice and how those decisions performed by the end of the hand.
- The JavaFX desktop layer uses FXML plus CSS, and keeps card views as separate nodes so animations can be added later without touching the engine.
- Session statistics are shown when the game ends.
