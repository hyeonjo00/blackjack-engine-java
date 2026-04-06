package com.blackjackengine;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SessionPersistence {
    private SessionPersistence() {}

    public static void saveSession(Path path, SessionSnapshot snapshot) throws IOException {
        Files.writeString(path, toSessionJson(snapshot), StandardCharsets.UTF_8);
    }

    public static SessionSnapshot loadSession(Path path) throws IOException {
        return parseSessionJson(Files.readString(path, StandardCharsets.UTF_8));
    }

    public static void exportStatisticsJson(
        Path path,
        GameStatisticsSnapshot statisticsSnapshot,
        RecommendationAnalyticsSnapshot recommendationSnapshot,
        int currentChips,
        String themeName
    ) throws IOException {
        String json = "{\n"
            + "  \"currentChips\": " + currentChips + ",\n"
            + "  \"theme\": \"" + escapeJson(themeName) + "\",\n"
            + "  \"statistics\": " + toStatisticsJson(statisticsSnapshot, 2) + ",\n"
            + "  \"recommendationAnalytics\": " + toRecommendationJson(recommendationSnapshot, 2) + "\n"
            + "}\n";
        Files.writeString(path, json, StandardCharsets.UTF_8);
    }

    private static String toSessionJson(SessionSnapshot snapshot) {
        return "{\n"
            + "  \"startingChips\": " + snapshot.getStartingChips() + ",\n"
            + "  \"currentChips\": " + snapshot.getCurrentChips() + ",\n"
            + "  \"previewBetAmount\": " + snapshot.getPreviewBetAmount() + ",\n"
            + "  \"lastRoundResultText\": \"" + escapeJson(snapshot.getLastRoundResultText()) + "\",\n"
            + "  \"theme\": \"" + escapeJson(snapshot.getThemeName()) + "\",\n"
            + "  \"statistics\": " + toStatisticsJson(snapshot.getStatisticsSnapshot(), 2) + ",\n"
            + "  \"recommendationAnalytics\": " + toRecommendationJson(snapshot.getRecommendationAnalyticsSnapshot(), 2) + "\n"
            + "}\n";
    }

    private static String toStatisticsJson(GameStatisticsSnapshot snapshot, int indent) {
        String prefix = " ".repeat(indent);
        return "{\n"
            + prefix + "  \"totalRoundsPlayed\": " + snapshot.getTotalRoundsPlayed() + ",\n"
            + prefix + "  \"wins\": " + snapshot.getWins() + ",\n"
            + prefix + "  \"losses\": " + snapshot.getLosses() + ",\n"
            + prefix + "  \"pushes\": " + snapshot.getPushes() + ",\n"
            + prefix + "  \"blackjacks\": " + snapshot.getBlackjacks() + ",\n"
            + prefix + "  \"splitUsage\": " + snapshot.getSplitUsage() + ",\n"
            + prefix + "  \"doubleDownUsage\": " + snapshot.getDoubleDownUsage() + ",\n"
            + prefix + "  \"totalChipProfitLoss\": " + snapshot.getTotalChipProfitLoss() + ",\n"
            + prefix + "  \"currentWinStreak\": " + snapshot.getCurrentWinStreak() + ",\n"
            + prefix + "  \"bestWinStreak\": " + snapshot.getBestWinStreak() + "\n"
            + prefix + "}";
    }

    private static String toRecommendationJson(RecommendationAnalyticsSnapshot snapshot, int indent) {
        String prefix = " ".repeat(indent);
        return "{\n"
            + prefix + "  \"totalRecommendationsShown\": " + snapshot.getTotalRecommendationsShown() + ",\n"
            + prefix + "  \"totalRecommendationsFollowed\": " + snapshot.getTotalRecommendationsFollowed() + ",\n"
            + prefix + "  \"totalRecommendationsIgnored\": " + snapshot.getTotalRecommendationsIgnored() + ",\n"
            + prefix + "  \"resolvedFollowedCount\": " + snapshot.getResolvedFollowedCount() + ",\n"
            + prefix + "  \"resolvedIgnoredCount\": " + snapshot.getResolvedIgnoredCount() + ",\n"
            + prefix + "  \"followedWinCount\": " + snapshot.getFollowedWinCount() + ",\n"
            + prefix + "  \"ignoredWinCount\": " + snapshot.getIgnoredWinCount() + ",\n"
            + prefix + "  \"followedPushCount\": " + snapshot.getFollowedPushCount() + ",\n"
            + prefix + "  \"ignoredPushCount\": " + snapshot.getIgnoredPushCount() + ",\n"
            + prefix + "  \"followedChipProfitLoss\": " + snapshot.getFollowedChipProfitLoss() + ",\n"
            + prefix + "  \"ignoredChipProfitLoss\": " + snapshot.getIgnoredChipProfitLoss() + ",\n"
            + prefix + "  \"ignoredHitRecommendations\": " + snapshot.getIgnoredHitRecommendations() + ",\n"
            + prefix + "  \"ignoredStandRecommendations\": " + snapshot.getIgnoredStandRecommendations() + ",\n"
            + prefix + "  \"ignoredDoubleDownRecommendations\": " + snapshot.getIgnoredDoubleDownRecommendations() + ",\n"
            + prefix + "  \"ignoredSplitRecommendations\": " + snapshot.getIgnoredSplitRecommendations() + "\n"
            + prefix + "}";
    }

    private static SessionSnapshot parseSessionJson(String json) {
        String statisticsJson = extractObject(json, "statistics");
        String recommendationJson = extractObject(json, "recommendationAnalytics");

        return new SessionSnapshot(
            extractInt(json, "startingChips"),
            extractInt(json, "currentChips"),
            extractInt(json, "previewBetAmount"),
            extractString(json, "lastRoundResultText"),
            extractString(json, "theme"),
            parseStatisticsSnapshot(statisticsJson),
            parseRecommendationSnapshot(recommendationJson)
        );
    }

    private static GameStatisticsSnapshot parseStatisticsSnapshot(String json) {
        return new GameStatisticsSnapshot(
            extractInt(json, "totalRoundsPlayed"),
            extractInt(json, "wins"),
            extractInt(json, "losses"),
            extractInt(json, "pushes"),
            extractInt(json, "blackjacks"),
            extractInt(json, "splitUsage"),
            extractInt(json, "doubleDownUsage"),
            extractInt(json, "totalChipProfitLoss"),
            extractInt(json, "currentWinStreak"),
            extractInt(json, "bestWinStreak")
        );
    }

    private static RecommendationAnalyticsSnapshot parseRecommendationSnapshot(String json) {
        return new RecommendationAnalyticsSnapshot(
            extractInt(json, "totalRecommendationsShown"),
            extractInt(json, "totalRecommendationsFollowed"),
            extractInt(json, "totalRecommendationsIgnored"),
            extractInt(json, "resolvedFollowedCount"),
            extractInt(json, "resolvedIgnoredCount"),
            extractInt(json, "followedWinCount"),
            extractInt(json, "ignoredWinCount"),
            extractInt(json, "followedPushCount"),
            extractInt(json, "ignoredPushCount"),
            extractInt(json, "followedChipProfitLoss"),
            extractInt(json, "ignoredChipProfitLoss"),
            extractInt(json, "ignoredHitRecommendations"),
            extractInt(json, "ignoredStandRecommendations"),
            extractInt(json, "ignoredDoubleDownRecommendations"),
            extractInt(json, "ignoredSplitRecommendations")
        );
    }

    private static int extractInt(String json, String key) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(-?\\d+)").matcher(json);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Missing integer field: " + key);
        }
        return Integer.parseInt(matcher.group(1));
    }

    private static String extractString(String json, String key) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"((?:\\\\.|[^\\\\\"])*)\"").matcher(json);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Missing string field: " + key);
        }
        return unescapeJson(matcher.group(1));
    }

    private static String extractObject(String json, String key) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\\{").matcher(json);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Missing object field: " + key);
        }

        int startIndex = matcher.end() - 1;
        int depth = 0;
        boolean inString = false;
        boolean escaped = false;

        for (int index = startIndex; index < json.length(); index++) {
            char current = json.charAt(index);

            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (current == '\\') {
                    escaped = true;
                } else if (current == '"') {
                    inString = false;
                }
                continue;
            }

            if (current == '"') {
                inString = true;
                continue;
            }

            if (current == '{') {
                depth++;
            } else if (current == '}') {
                depth--;
                if (depth == 0) {
                    return json.substring(startIndex, index + 1);
                }
            }
        }

        throw new IllegalArgumentException("Unterminated object field: " + key);
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\r", "\\r")
            .replace("\n", "\\n");
    }

    private static String unescapeJson(String value) {
        return value
            .replace("\\n", "\n")
            .replace("\\r", "\r")
            .replace("\\\"", "\"")
            .replace("\\\\", "\\");
    }
}
