package com.snow.mahjong.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
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

//試合一覧

@Controller
public class MatchController {

	@Autowired
	private MatchRepository matchRepository;

	@Autowired
	private PlayerRepository playerRepository;

	@Autowired
	private MatchPlayerRepository matchPlayerRepository;

	@Autowired
	private MatchResultRepository matchResultRepository;

	@GetMapping("/matches")
	public String list(Model model) {

		List<Match> matches = matchRepository.findAll();

		Map<Long, List<MatchPlayer>> playerMap = new HashMap<>();
		Map<Long, List<MatchResult>> resultMap = new HashMap<>();

		for (Match m : matches) {
			playerMap.put(
					m.getId(),
					matchPlayerRepository.findByMatchIdOrderBySeatOrder(m.getId()));

			resultMap.put(
					m.getId(),
					matchResultRepository.findByMatchId(m.getId()));
		}

		model.addAttribute("matches", matches);
		model.addAttribute("playerMap", playerMap);
		model.addAttribute("resultMap", resultMap);

		return "matches";
	}

	//    試合数
	@GetMapping("/matches/init")
	public String init() {

		for (int i = 1; i <= 30; i++) {
			Match match = new Match();
			match.setMatchNumber(i);
			matchRepository.save(match);
		}

		return "redirect:/matches";
	}

	//    試合詳細画面
	@GetMapping("/matches/{id}")
	public String detail(@PathVariable Long id, Model model) {

		Match match = matchRepository.findById(id).orElse(null);

		List<MatchPlayer> players = matchPlayerRepository.findAll()
				.stream()
				.filter(mp -> mp.getMatch().getId().equals(id))
				.toList();

		model.addAttribute("match", match);
		model.addAttribute("players", players);

		return "match_detail";
	}

	//    対戦表
	@GetMapping("/matches/generate")
	public String generate() {

		List<Player> players = playerRepository.findAll();

		// 出場回数管理
		Map<Long, Integer> playCount = new HashMap<>();
		for (Player p : players) {
			playCount.put(p.getId(), 0);
		}

		List<Match> matches = matchRepository.findAll();

		Random rand = new Random();

		for (Match match : matches) {

			// 出場回数少ない順で並び替え
			players.sort(Comparator.comparing(p -> playCount.get(p.getId())));

			List<Player> candidates = new ArrayList<>(players);

			// ランダムで4人選ぶ
			Collections.shuffle(candidates);

			// 4人未満ならスキップ（安全対策）
			if (candidates.size() < 4) {
				continue;
			}

			// 4人選ぶ
			List<Player> selected = candidates.subList(0, 4);

			int seat = 1;

			for (Player p : selected) {
				MatchPlayer mp = new MatchPlayer();
				mp.setMatch(match);
				mp.setPlayer(p);
				mp.setSeatOrder(seat++);
				matchPlayerRepository.save(mp);

				// 出場回数カウントアップ
				playCount.put(p.getId(), playCount.get(p.getId()) + 1);
			}
		}

		return "redirect:/matches";
	}

	@PostMapping("/matches/result")
	public String saveResult(
	        @RequestParam Long matchId,
	        @RequestParam List<Long> playerIds,
	        @RequestParam List<Integer> scores,
	        Model model
	) {

		int total = scores.stream().mapToInt(Integer::intValue).sum();

	    if (total != 100000) {
	        model.addAttribute("error", "合計が100000点になっていません");

	        Match match = matchRepository.findById(matchId).orElse(null);
	        List<MatchPlayer> players = matchPlayerRepository.findByMatchIdOrderBySeatOrder(matchId);

	        model.addAttribute("match", match);
	        model.addAttribute("players", players);

	        return "match_detail";
	    }
	    
	    Match match = matchRepository.findById(matchId).orElse(null);

	    // 既存結果削除（再入力対応）
	    matchResultRepository.deleteAll(
	        matchResultRepository.findByMatchId(matchId)
	    );

	    // 順位計算用
	    List<Integer> sorted = new ArrayList<>(scores);
	    sorted.sort(Collections.reverseOrder());

	    for (int i = 0; i < playerIds.size(); i++) {

	        int score = scores.get(i);
	        int rank = sorted.indexOf(score) + 1;

	        double uma = switch (rank) {
	            case 1 -> 10;
	            case 2 -> 5;
	            case 3 -> -5;
	            case 4 -> -10;
	            default -> 0;
	        };

	        double point = (score - 30000) / 1000.0 + uma;

	        MatchResult mr = new MatchResult();
	        mr.setMatch(match);
	        mr.setPlayer(playerRepository.findById(playerIds.get(i)).orElse(null));
	        mr.setScore(score);
	        mr.setRankOrder(rank);
	        mr.setPoint(point);

	        matchResultRepository.save(mr);
	    }

	    return "redirect:/matches";
	}
}