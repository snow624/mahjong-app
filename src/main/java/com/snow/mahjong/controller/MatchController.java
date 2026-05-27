package com.snow.mahjong.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.snow.mahjong.entity.Match;
import com.snow.mahjong.entity.MatchPlayer;
import com.snow.mahjong.entity.MatchResult;
import com.snow.mahjong.entity.Player;
import com.snow.mahjong.repository.MatchPlayerRepository;
import com.snow.mahjong.repository.MatchRepository;
import com.snow.mahjong.repository.MatchResultRepository;
import com.snow.mahjong.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Value;

@Controller
public class MatchController {

	@Value("${app.target-games-per-player}")
	private int targetGamesPerPlayer;

	// 1試合あたりの人数
	private static final int PLAYERS_PER_MATCH = 4;

	@Autowired
	private MatchRepository matchRepository;

	@Autowired
	private PlayerRepository playerRepository;

	@Autowired
	private MatchPlayerRepository matchPlayerRepository;

	@Autowired
	private MatchResultRepository matchResultRepository;

	// application.properties の app.admin.password を読み込む
	@Value("${app.admin.password}")
	private String adminPassword;

	/*
	 * 試合一覧画面
	 * URL: /matches
	 *
	 * 役割:
	 * - 試合一覧を表示する
	 * - 各試合に参加するプレイヤーを表示する
	 * - 入力済みの順位・ポイントも表示する
	 */
	@GetMapping("/matches")
	public String list(Model model) {
		List<Match> matches = matchRepository.findAll();

		Map<Long, List<MatchPlayer>> playerMap = new HashMap<>();
		Map<Long, List<MatchResult>> resultMap = new HashMap<>();

		for (Match match : matches) {
			playerMap.put(
					match.getId(),
					matchPlayerRepository.findByMatchIdOrderBySeatOrder(match.getId()));

			resultMap.put(
					match.getId(),
					matchResultRepository.findByMatchId(match.getId()));
		}

		model.addAttribute("matches", matches);
		model.addAttribute("playerMap", playerMap);
		model.addAttribute("resultMap", resultMap);

		return "matches";
	}

	/*
	 * 試合詳細・点数入力画面
	 * URL: /matches/{id}
	 *
	 * 役割:
	 * - 1試合分の詳細を表示する
	 * - その試合の4人を表示する
	 * - 点数入力フォームを表示する
	 */
	@GetMapping("/matches/{id}")
	public String detail(@PathVariable Long id, Model model) {
		Match match = matchRepository.findById(id).orElse(null);
		List<MatchPlayer> players = matchPlayerRepository.findByMatchIdOrderBySeatOrder(id);

		model.addAttribute("match", match);
		model.addAttribute("players", players);

		return "match_detail";
	}

	/*
	 * 管理者ページ
	 * URL: /matches/admin
	 *
	 * 役割:
	 * - 試合枠作成
	 * - 対戦表作成
	 * - 対戦表削除
	 * - 試合枠削除
	 * の管理操作をまとめて表示する
	 */
	@GetMapping("/matches/admin")
	public String adminPage(HttpSession session, Model model) {

		if (!Boolean.TRUE.equals(session.getAttribute("adminLogin"))) {
			return "redirect:/matches/admin/login";
		}

		setAdminPageInfo(model);
		return "match_admin";
	}

	/*
	 * 管理者ログイン画面表示
	 * URL: /matches/admin/login
	 *
	 * 役割:
	 * - 管理者ページに入る前のパスワード入力画面を表示する
	 */
	@GetMapping("/matches/admin/login")
	public String adminLoginForm() {
		return "match_admin_login";
	}

	/*
	 * 管理者ログイン処理
	 * URL: /matches/admin/login
	 *
	 * 役割:
	 * - 入力されたパスワードを確認する
	 * - 正しければセッションにログイン済み情報を保存する
	 */
	@PostMapping("/matches/admin/login")
	public String adminLogin(
			@RequestParam String password,
			HttpSession session,
			Model model) {

		if (!isAdmin(password)) {
			model.addAttribute("error", "パスワードが違います");
			return "match_admin_login";
		}

		session.setAttribute("adminLogin", true);
		return "redirect:/matches/admin";
	}

	/*
	 * 試合枠の自動作成
	 * URL: /matches/init
	 *
	 * 役割:
	 * - 登録プレイヤー数から必要な試合数を自動計算する
	 * - 全員が同じ試合数になるように試合枠を作る
	 * - 既に試合枠がある場合は、足りない分だけ追加する
	 */
	@PostMapping("/matches/init")
	public String init(HttpSession session, Model model) {
		if (!Boolean.TRUE.equals(session.getAttribute("adminLogin"))) {
			return "redirect:/matches/admin/login";
		}

		List<Player> players = playerRepository.findAll();

		if (players.size() < PLAYERS_PER_MATCH) {
			model.addAttribute("error", "プレイヤーが4人未満のため、試合枠を作成できません");
			setAdminPageInfo(model);
			return "match_admin";
		}

		// 全員が同じ試合数になるように計算
		// 必要な試合数 = プレイヤー数 × 目標試合数 ÷ 4
		int requiredMatchCount = (players.size() * targetGamesPerPlayer) / PLAYERS_PER_MATCH;

		long currentMatchCount = matchRepository.count();

		for (long i = currentMatchCount + 1; i <= requiredMatchCount; i++) {
			Match match = new Match();
			match.setMatchNumber((int) i);
			matchRepository.save(match);
		}

		return "redirect:/matches/admin";
	}

	/*
	 * 対戦表作成
	 * URL: /matches/generate
	 *
	 * 役割:
	 * - 試合枠に対してプレイヤー4人を割り当てる
	 * - 出場回数が少ない人から優先して選ぶ
	 * - 同じ出場回数の人はランダムにする
	 * - 既にメンバーが入っている試合はスキップする
	 */
	@PostMapping("/matches/generate")
	public String generate(HttpSession session, Model model) {
		if (!Boolean.TRUE.equals(session.getAttribute("adminLogin"))) {
			return "redirect:/matches/admin/login";
		}

		List<Player> players = playerRepository.findAll();

		if (players.size() < PLAYERS_PER_MATCH) {
			model.addAttribute("error", "プレイヤーが4人未満のため、対戦表を作成できません");
			setAdminPageInfo(model);
			return "match_admin";
		}

		Map<Long, Integer> playCount = new HashMap<>();
		for (Player player : players) {
			playCount.put(player.getId(), 0);
		}

		List<Match> matches = matchRepository.findAll();

		for (Match match : matches) {
			List<MatchPlayer> existingPlayers = matchPlayerRepository.findByMatchIdOrderBySeatOrder(match.getId());

			if (!existingPlayers.isEmpty()) {
				continue;
			}

			Collections.shuffle(players);
			players.sort(Comparator.comparing(player -> playCount.get(player.getId())));

			List<Player> selectedPlayers = players.subList(0, PLAYERS_PER_MATCH);

			int seatOrder = 1;

			for (Player player : selectedPlayers) {
				MatchPlayer matchPlayer = new MatchPlayer();
				matchPlayer.setMatch(match);
				matchPlayer.setPlayer(player);
				matchPlayer.setSeatOrder(seatOrder++);
				matchPlayerRepository.save(matchPlayer);

				playCount.put(player.getId(), playCount.get(player.getId()) + 1);
			}
		}

		return "redirect:/matches";
	}

	/*
	 * 対戦表削除
	 * URL: /matches/clear
	 *
	 * 役割:
	 * - 対戦表と点数結果を削除する
	 * - 試合枠そのものは残す
	 */
	@PostMapping("/matches/clear")
	public String clearMatches(HttpSession session, Model model) {
		if (!Boolean.TRUE.equals(session.getAttribute("adminLogin"))) {
			return "redirect:/matches/admin/login";
		}

		matchResultRepository.deleteAll();
		matchPlayerRepository.deleteAll();

		return "redirect:/matches/admin";
	}

	/*
	 * 試合枠も全部削除
	 * URL: /matches/clear-all
	 *
	 * 役割:
	 * - 点数結果を削除する
	 * - 対戦表を削除する
	 * - 試合枠も削除する
	 */
	@PostMapping("/matches/clear-all")
	public String clearAll(HttpSession session, Model model) {
		if (!Boolean.TRUE.equals(session.getAttribute("adminLogin"))) {
			return "redirect:/matches/admin/login";
		}

		matchResultRepository.deleteAll();
		matchPlayerRepository.deleteAll();
		matchRepository.deleteAll();

		return "redirect:/matches/admin";
	}

	/*
	 * 点数登録処理
	 * URL: /matches/result
	 *
	 * 役割:
	 * - 各プレイヤーの素点を保存する
	 * - 合計が100000点かチェックする
	 * - 順位を計算する
	 * - ウマ込みのポイントを計算する
	 */
	@PostMapping("/matches/result")
	public String saveResult(
			@RequestParam Long matchId,
			@RequestParam List<Long> playerIds,
			@RequestParam List<Integer> scores,
			Model model) {

		int total = scores.stream().mapToInt(Integer::intValue).sum();

		if (total != 100000) {
			model.addAttribute("error", "合計が100000点になっていません");
			setMatchDetailInfo(matchId, model);
			return "match_detail";
		}

		Match match = matchRepository.findById(matchId).orElse(null);

		// 既存結果削除（再入力対応）
		matchResultRepository.deleteAll(
				matchResultRepository.findByMatchId(matchId));

		// 順位計算用
		List<Integer> sortedScores = new ArrayList<>(scores);
		sortedScores.sort(Collections.reverseOrder());

		for (int i = 0; i < playerIds.size(); i++) {
			int score = scores.get(i);
			int rank = sortedScores.indexOf(score) + 1;

			double uma = switch (rank) {
			case 1 -> 10;
			case 2 -> 5;
			case 3 -> -5;
			case 4 -> -10;
			default -> 0;
			};

			double point = (score - 30000) / 1000.0 + uma;

			MatchResult matchResult = new MatchResult();
			matchResult.setMatch(match);
			matchResult.setPlayer(playerRepository.findById(playerIds.get(i)).orElse(null));
			matchResult.setScore(score);
			matchResult.setRankOrder(rank);
			matchResult.setPoint(point);

			matchResultRepository.save(matchResult);
		}

		return "redirect:/matches";
	}

	/*
	 * 管理者パスワード確認
	 */
	private boolean isAdmin(String password) {
		return adminPassword.equals(password);
	}

	/*
	 * 管理者ページに表示する情報をセットする
	 */
	private void setAdminPageInfo(Model model) {
		long playerCount = playerRepository.count();
		long matchCount = matchRepository.count();

		int requiredMatchCount = 0;

		if (playerCount >= PLAYERS_PER_MATCH) {
			requiredMatchCount = (int) (playerCount * targetGamesPerPlayer / PLAYERS_PER_MATCH);
		}

		model.addAttribute("playerCount", playerCount);
		model.addAttribute("matchCount", matchCount);
		model.addAttribute("requiredMatchCount", requiredMatchCount);
		model.addAttribute("targetGamesPerPlayer", targetGamesPerPlayer);
	}

	/*
	 * 試合詳細画面に戻る時に必要な情報をセットする
	 */
	private void setMatchDetailInfo(Long matchId, Model model) {
		Match match = matchRepository.findById(matchId).orElse(null);
		List<MatchPlayer> players = matchPlayerRepository.findByMatchIdOrderBySeatOrder(matchId);

		model.addAttribute("match", match);
		model.addAttribute("players", players);
	}
}