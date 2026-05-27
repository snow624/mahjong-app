package com.snow.mahjong.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.snow.mahjong.entity.MatchResult;


//試合結果

public interface MatchResultRepository extends JpaRepository<MatchResult, Long> {
	List<MatchResult> findByMatchId(Long matchId);
	
	List<MatchResult> findAllByOrderByMatchIdAsc();

	// N+1問題を解決するための最適化クエリ
	// PlayerオブジェクトをJOIN FETCHで一緒に取得
	@Query("SELECT mr FROM MatchResult mr "
			+ "LEFT JOIN FETCH mr.player "
			+ "ORDER BY mr.match.id ASC")
	List<MatchResult> findAllWithPlayer();

	// 特定の試合のMatchResultをPlayerと一緒に取得
	@Query("SELECT mr FROM MatchResult mr "
			+ "LEFT JOIN FETCH mr.player "
			+ "WHERE mr.match.id = :matchId")
	List<MatchResult> findByMatchIdWithPlayer(Long matchId);
}