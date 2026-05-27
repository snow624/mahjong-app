package com.snow.mahjong.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.snow.mahjong.entity.MatchPlayer;

//対戦表

public interface MatchPlayerRepository extends JpaRepository<MatchPlayer, Long> {
	
	List<MatchPlayer> findByMatchIdOrderBySeatOrder(Long matchId);
	
	List<MatchPlayer> findAllByOrderByMatchIdAscSeatOrderAsc();

	// N+1問題を解決するための最適化クエリ
	// PlayerオブジェクトをJOIN FETCHで一緒に取得
	@Query("SELECT mp FROM MatchPlayer mp "
			+ "LEFT JOIN FETCH mp.player "
			+ "ORDER BY mp.match.id ASC, mp.seatOrder ASC")
	List<MatchPlayer> findAllWithPlayer();

	// 特定の試合のMatchPlayerをPlayerと一緒に取得
	@Query("SELECT mp FROM MatchPlayer mp "
			+ "LEFT JOIN FETCH mp.player "
			+ "WHERE mp.match.id = :matchId "
			+ "ORDER BY mp.seatOrder ASC")
	List<MatchPlayer> findByMatchIdWithPlayer(Long matchId);
}