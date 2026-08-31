package com.snow.mahjong;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.snow.mahjong.util.RankingSortUtils;

class RankingSortUtilsTest {

    @Test
    void avoidLastRankingShouldSortDescendingWithHundredPercentFirst() {
        List<Map<String, Object>> ranking = new ArrayList<>();
        ranking.add(newHashMap("A", 70.0));
        ranking.add(newHashMap("B", 100.0));
        ranking.add(newHashMap("C", 80.0));

        RankingSortUtils.sortAvoidLastRanking(ranking);

        assertEquals("B", ranking.get(0).get("name"));
        assertEquals("C", ranking.get(1).get("name"));
        assertEquals("A", ranking.get(2).get("name"));
    }

    private Map<String, Object> newHashMap(String name, double avoidLastRate) {
        Map<String, Object> item = new HashMap<>();
        item.put("name", name);
        item.put("avoidLastRate", avoidLastRate);
        return item;
    }
}
