package com.snow.mahjong.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.snow.mahjong.entity.MatchPlayer;

//対戦表

public interface MatchPlayerRepository extends JpaRepository<MatchPlayer, Long> {
	
	List<MatchPlayer> findByMatchIdOrderBySeatOrder(Long matchId);
	
	List<MatchPlayer> findAllByOrderByMatchIdAscSeatOrderAsc();

}