package com.snow.mahjong.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.snow.mahjong.entity.MatchResult;
import com.snow.mahjong.entity.Player;
import com.snow.mahjong.repository.MatchResultRepository;
import com.snow.mahjong.repository.PlayerRepository;

@Controller
public class RankingController {

	@Autowired
	private PlayerRepository playerRepository;

	@Autowired
	private MatchResultRepository matchResultRepository;

	@GetMapping("/ranking")
	public String ranking(Model model) {

		List<Player> players = playerRepository.findAll();
		List<MatchResult> results = matchResultRepository.findAll();

		//        ポイント
		Map<Long, Double> pointMap = new HashMap<>();
		//        試合数
		Map<Long, Integer> gameCountMap = new HashMap<>();
		//        最高スコア
		Map<Long, Integer> maxScoreMap = new HashMap<>();
		//        １位回数
		Map<Long, Integer> topCountMap = new HashMap<>();
		//        ４着回避率
		Map<Long, Integer> avoidLastCountMap = new HashMap<>();

		// 初期化
		for (Player p : players) {
			pointMap.put(p.getId(), 0.0);
			gameCountMap.put(p.getId(), 0);
			maxScoreMap.put(p.getId(), 0);
			topCountMap.put(p.getId(), 0);
			avoidLastCountMap.put(p.getId(), 0);
		}

		// 集計
		for (MatchResult r : results) {
			Long playerId = r.getPlayer().getId();

			//          ポイント
			pointMap.put(playerId,
					pointMap.get(playerId) + r.getPoint());
			//          試合数
			gameCountMap.put(playerId,
					gameCountMap.get(playerId) + 1);

			// 最高スコア
			if (r.getScore() > maxScoreMap.get(playerId)) {
				maxScoreMap.put(playerId, r.getScore());
			}

			// トップ回数
			if (r.getRankOrder() == 1) {
				topCountMap.put(playerId, topCountMap.get(playerId) + 1);
			}

			// 4着回避回数
			if (r.getRankOrder() != 4) {
				avoidLastCountMap.put(playerId, avoidLastCountMap.get(playerId) + 1);
			}
		}

		// 並び替え用リスト
		List<Map<String, Object>> rankingList = new ArrayList<>();

		for (Player p : players) {
			Map<String, Object> map = new HashMap<>();

			Long playerId = p.getId();
			int games = gameCountMap.get(playerId);
			int avoidLastCount = avoidLastCountMap.get(playerId);

			double avoidLastRate = 0.0;
			if (games > 0) {
				avoidLastRate = avoidLastCount * 100.0 / games;
			}

			//			mapにデータを入れる
			map.put("name", p.getName());
			map.put("point", pointMap.get(playerId));
			map.put("games", games);
			map.put("maxScore", maxScoreMap.get(playerId));
			map.put("topCount", topCountMap.get(playerId));
			map.put("avoidLastRate", avoidLastRate);
			map.put("iconPath", p.getIconPath());

			rankingList.add(map);
		}

		// ポイント順でソート
		rankingList.sort((a, b) -> Double.compare((Double) b.get("point"), (Double) a.get("point")));

		// 1位との差計算
		double topPoint = (double) rankingList.get(0).get("point");

		for (Map<String, Object> r : rankingList) {
			double diff = topPoint - (double) r.get("point");
			r.put("diff", diff);
		}
		
		List<Map<String, Object>> pointRanking = new ArrayList<>(rankingList);
		pointRanking.sort((a, b) ->
		    Double.compare((Double) b.get("point"), (Double) a.get("point"))
		);

		List<Map<String, Object>> maxScoreRanking = new ArrayList<>(rankingList);
		maxScoreRanking.sort((a, b) ->
		    Integer.compare((Integer) b.get("maxScore"), (Integer) a.get("maxScore"))
		);

		List<Map<String, Object>> avoidLastRanking = new ArrayList<>(rankingList);
		avoidLastRanking.sort((a, b) ->
		    Double.compare((Double) b.get("avoidLastRate"), (Double) a.get("avoidLastRate"))
		);

		List<Map<String, Object>> topCountRanking = new ArrayList<>(rankingList);
		topCountRanking.sort((a, b) ->
		    Integer.compare((Integer) b.get("topCount"), (Integer) a.get("topCount"))
		);

		model.addAttribute("pointRanking", pointRanking);
		model.addAttribute("maxScoreRanking", maxScoreRanking);
		model.addAttribute("avoidLastRanking", avoidLastRanking);
		model.addAttribute("topCountRanking", topCountRanking);

		model.addAttribute("ranking", rankingList);

		return "ranking";
	}
}