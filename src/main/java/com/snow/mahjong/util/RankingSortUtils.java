package com.snow.mahjong.util;

import java.util.List;
import java.util.Map;

public final class RankingSortUtils {

    private RankingSortUtils() {
    }

    public static void sortAvoidLastRanking(List<Map<String, Object>> ranking) {
        ranking.sort((a, b) -> Double.compare(
                toDouble(b.get("avoidLastRate")),
                toDouble(a.get("avoidLastRate"))));

        int rank = 1;
        for (int i = 0; i < ranking.size(); i++) {
            Map<String, Object> rankingData = ranking.get(i);

            if (i > 0) {
                double prevRate = toDouble(ranking.get(i - 1).get("avoidLastRate"));
                double currentRate = toDouble(rankingData.get("avoidLastRate"));

                if (Double.compare(prevRate, currentRate) != 0) {
                    rank = i + 1;
                }
            }

            rankingData.put("rank", rank);
        }
    }

    private static double toDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return 0.0;
    }
}
