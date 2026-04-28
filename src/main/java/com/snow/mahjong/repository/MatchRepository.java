package com.snow.mahjong.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.snow.mahjong.entity.Match;

//試合一覧

public interface MatchRepository extends JpaRepository<Match, Long> {

}