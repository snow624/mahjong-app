package com.snow.mahjong.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.snow.mahjong.entity.MatchResult;


//試合結果

public interface MatchResultRepository extends JpaRepository<MatchResult, Long> {
	List<MatchResult> findByMatchId(Long matchId);
}