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
import com.snow.mahjong.repository.MatchRepository;
import com.snow.mahjong.repository.MatchResultRepository;
import com.snow.mahjong.repository.PlayerRepository;

/*
 * ランキング画面Controller
 *
 * 役割:
 * - ランキング画面を表示する
 * - 各プレイヤーの合計ポイントを集計する
 * - 試合数を集計する
 * - 最高スコアを集計する
 * - トップ回数を集計する
 * - 4着回避率を計算する
 * - 一つ上の上位とのポイント差を計算する
 * - メインランキング、最高スコアランキング、4着回避率ランキング、最多トップランキングを作る
 */
@Controller
public class RankingController {

	@Autowired
	private PlayerRepository playerRepository;

	@Autowired
	private MatchResultRepository matchResultRepository;

	@Autowired
	private MatchRepository matchRepository;
	
	/*
	 * ランキング画面
	 * URL: /ranking
	 *
	 * 役割:
	 * - DBからプレイヤーと試合結果を取得する
	 * - 各種ランキング用のデータを集計する
	 * - ranking.html にデータを渡す
	 */
	@GetMapping("/ranking")
	public String ranking(Model model) {

		// 登録済みプレイヤーを全件取得
		List<Player> players = playerRepository.findAll();

		// 入力済みの試合結果をPlayerと一緒に取得（N+1問題を回避）
		List<MatchResult> results = matchResultRepository.findAllWithPlayer();

		// 試合数から実際の目標試合数を計算
		// 目標試合数 = 試合数 × 4 ÷ プレイヤー数
		int targetGamesPerPlayer = 0;
		if (players.size() > 0) {
			long matchCount = matchRepository.count();
			targetGamesPerPlayer = (int) ((matchCount * 4) / players.size());
		}

		/*
		 * プレイヤーごとの集計用Map
		 *
		 * key:
		 * - playerId
		 *
		 * value:
		 * - 各プレイヤーの集計値
		 */
		Map<Long, Double> pointMap = new HashMap<>();
		Map<Long, Integer> gameCountMap = new HashMap<>();
		Map<Long, Integer> maxScoreMap = new HashMap<>();
		Map<Long, Integer> topCountMap = new HashMap<>();
		Map<Long, Integer> avoidLastCountMap = new HashMap<>();

		/*
		 * 集計用Mapを初期化する
		 *
		 * まだ試合結果がないプレイヤーもランキングに出せるように、
		 * 最初に全プレイヤー分の初期値を入れておく
		 */
		for (Player player : players) {
			Long playerId = player.getId();

			pointMap.put(playerId, 0.0);
			gameCountMap.put(playerId, 0);
			maxScoreMap.put(playerId, 0);
			topCountMap.put(playerId, 0);
			avoidLastCountMap.put(playerId, 0);
		}

		/*
		 * 試合結果をもとに各プレイヤーの成績を集計する
		 */
		for (MatchResult result : results) {
			Long playerId = result.getPlayer().getId();

			// 合計ポイント
			pointMap.put(
					playerId,
					pointMap.get(playerId) + result.getPoint());

			// 試合数
			gameCountMap.put(
					playerId,
					gameCountMap.get(playerId) + 1);

			// 最高スコア
			if (result.getScore() > maxScoreMap.get(playerId)) {
				maxScoreMap.put(playerId, result.getScore());
			}

			// トップ回数
			if (result.getRankOrder() == 1) {
				topCountMap.put(
						playerId,
						topCountMap.get(playerId) + 1);
			}

			// 4着回避回数
			if (result.getRankOrder() != 4) {
				avoidLastCountMap.put(
						playerId,
						avoidLastCountMap.get(playerId) + 1);
			}
		}

		/*
		 * Thymeleafで扱いやすいように、
		 * プレイヤーごとの情報をMap形式にまとめる
		 */
		List<Map<String, Object>> rankingList = new ArrayList<>();

		for (Player player : players) {
			Long playerId = player.getId();

			int games = gameCountMap.get(playerId);
			int avoidLastCount = avoidLastCountMap.get(playerId);

			double avoidLastRate = 0.0;

			if (games > 0) {
				avoidLastRate = avoidLastCount * 100.0 / games;
			}

			Map<String, Object> rankingData = new HashMap<>();

			rankingData.put("name", player.getName());
			rankingData.put("iconPath", player.getIconPath());

			rankingData.put("point", pointMap.get(playerId));
			rankingData.put("games", games);
			rankingData.put("maxScore", maxScoreMap.get(playerId));
			rankingData.put("topCount", topCountMap.get(playerId));
			rankingData.put("avoidLastRate", avoidLastRate);

			rankingList.add(rankingData);
		}

		/*
		 * プレイヤーが1人もいない場合
		 *
		 * 空のランキングリストを画面へ渡して終了する
		 */
		if (rankingList.isEmpty()) {
			model.addAttribute("pointRanking", rankingList);
			model.addAttribute("maxScoreRanking", rankingList);
			model.addAttribute("avoidLastRanking", rankingList);
			model.addAttribute("topCountRanking", rankingList);
			model.addAttribute("targetGamesPerPlayer", targetGamesPerPlayer);

			return "ranking";
		}

		/*
		 * メインランキング
		 *
		 * 合計ポイントが高い順に並べる
		 */
		List<Map<String, Object>> pointRanking = new ArrayList<>();
		for (Map<String, Object> data : rankingList) {
			pointRanking.add(new HashMap<>(data));
		}

		pointRanking.sort((a, b) -> Double.compare(
				(Double) b.get("point"),
				(Double) a.get("point")));

		/*
		 * 一つ上の順位の人との差を計算し、同率対応の順位を設定する
		 *
		 * 例:
		 * 1位  120.0pt → 差 0.0
		 * 2位  100.0pt → 差 20.0
		 * 2位   100.0pt → 差 0.0 (同率)
		 * 4位   80.0pt → 差 20.0
		 */
		int rank = 1;
		for (int i = 0; i < pointRanking.size(); i++) {
			Map<String, Object> rankingData = pointRanking.get(i);
			
			// 同率かどうかを判定
			if (i > 0) {
				double prevPoint = (double) pointRanking.get(i - 1).get("point");
				double currentPoint = (double) rankingData.get("point");
				
				// ポイントが異なる場合は順位を進める
				if (prevPoint != currentPoint) {
					rank = i + 1;
				}
				// ポイントが同じ場合は順位を進めない（同率）
				
				// 一つ上の順位の人との差を計算
				double diff = prevPoint - currentPoint;
				rankingData.put("diff", diff);
			} else {
				rankingData.put("diff", 0.0);
			}
			
			rankingData.put("rank", rank);
		}

		/*
		 * 最高スコアランキング
		 *
		 * 最高スコアが高い順に並べる
		 */
		List<Map<String, Object>> maxScoreRanking = new ArrayList<>();
		for (Map<String, Object> data : rankingList) {
			maxScoreRanking.add(new HashMap<>(data));
		}

		maxScoreRanking.sort((a, b) -> Integer.compare(
				(Integer) b.get("maxScore"),
				(Integer) a.get("maxScore")));

		/*
		 * 最高スコアランキングの順位を計算する
		 */
		rank = 1;
		for (int i = 0; i < maxScoreRanking.size(); i++) {
			Map<String, Object> rankingData = maxScoreRanking.get(i);
			
			if (i > 0) {
				int prevScore = (Integer) maxScoreRanking.get(i - 1).get("maxScore");
				int currentScore = (Integer) rankingData.get("maxScore");
				
				if (prevScore != currentScore) {
					rank = i + 1;
				}
			}
			
			rankingData.put("rank", rank);
		}

		/*
		 * 4着回避率ランキング
		 *
		 * 4着を回避した割合が高い順に並べる
		 */
		List<Map<String, Object>> avoidLastRanking = new ArrayList<>();
		for (Map<String, Object> data : rankingList) {
			avoidLastRanking.add(new HashMap<>(data));
		}

		avoidLastRanking.sort((a, b) -> Double.compare(
				(Double) b.get("avoidLastRate"),
				(Double) a.get("avoidLastRate")));

		/*
		 * 4着回避率ランキングの順位を計算する
		 */
		rank = 1;
		for (int i = 0; i < avoidLastRanking.size(); i++) {
			Map<String, Object> rankingData = avoidLastRanking.get(i);
			
			if (i > 0) {
				double prevRate = (Double) avoidLastRanking.get(i - 1).get("avoidLastRate");
				double currentRate = (Double) rankingData.get("avoidLastRate");
				
				if (prevRate != currentRate) {
					rank = i + 1;
				}
			}
			
			rankingData.put("rank", rank);
		}

		/*
		 * 最多トップランキング
		 *
		 * 1位回数が多い順に並べる
		 */
		List<Map<String, Object>> topCountRanking = new ArrayList<>();
		for (Map<String, Object> data : rankingList) {
			topCountRanking.add(new HashMap<>(data));
		}

		topCountRanking.sort((a, b) -> Integer.compare(
				(Integer) b.get("topCount"),
				(Integer) a.get("topCount")));

		/*
		 * 最多トップランキングの順位を計算する
		 */
		rank = 1;
		for (int i = 0; i < topCountRanking.size(); i++) {
			Map<String, Object> rankingData = topCountRanking.get(i);
			
			if (i > 0) {
				int prevCount = (Integer) topCountRanking.get(i - 1).get("topCount");
				int currentCount = (Integer) rankingData.get("topCount");
				
				if (prevCount != currentCount) {
					rank = i + 1;
				}
			}
			
			rankingData.put("rank", rank);
		}

		/*
		 * ranking.html に渡すデータ
		 */
		model.addAttribute("pointRanking", pointRanking);
		model.addAttribute("maxScoreRanking", maxScoreRanking);
		model.addAttribute("avoidLastRanking", avoidLastRanking);
		model.addAttribute("topCountRanking", topCountRanking);

		/*
		 * 1人あたりの目標試合数
		 *
		 * 画面側で
		 * 3/10
		 * のように表示するために使う
		 */
		model.addAttribute("targetGamesPerPlayer", targetGamesPerPlayer);

		return "ranking";
	}
}