package com.snow.mahjong.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.snow.mahjong.entity.Match;
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
 * - 1位との差を計算する
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
		List<Map<String, Object>> pointRanking = new ArrayList<>(rankingList);

		pointRanking.sort((a, b) -> Double.compare(
				(Double) b.get("point"),
				(Double) a.get("point")));

		/*
		 * 1位との差を計算する
		 *
		 * 例:
		 * 1位  120.0pt → 差 0.0
		 * 2位  100.0pt → 差 20.0
		 */
		double topPoint = (double) pointRanking.get(0).get("point");

		for (Map<String, Object> rankingData : pointRanking) {
			double diff = topPoint - (double) rankingData.get("point");
			rankingData.put("diff", diff);
		}

		/*
		 * 最高スコアランキング
		 *
		 * 最高スコアが高い順に並べる
		 */
		List<Map<String, Object>> maxScoreRanking = new ArrayList<>(rankingList);

		maxScoreRanking.sort((a, b) -> Integer.compare(
				(Integer) b.get("maxScore"),
				(Integer) a.get("maxScore")));

		/*
		 * 4着回避率ランキング
		 *
		 * 4着を回避した割合が高い順に並べる
		 */
		List<Map<String, Object>> avoidLastRanking = new ArrayList<>(rankingList);

		avoidLastRanking.sort((a, b) -> Double.compare(
				(Double) b.get("avoidLastRate"),
				(Double) a.get("avoidLastRate")));

		/*
		 * 最多トップランキング
		 *
		 * 1位回数が多い順に並べる
		 */
		List<Map<String, Object>> topCountRanking = new ArrayList<>(rankingList);

		topCountRanking.sort((a, b) -> Integer.compare(
				(Integer) b.get("topCount"),
				(Integer) a.get("topCount")));

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