package com.snow.mahjong.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.Data;

//試合結果

@Entity
@Data
@Table(name = "match_result")
public class MatchResult {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// どの試合か
	@ManyToOne
	@JoinColumn(name = "match_id")
	private Match match;

	// 誰の結果か
	@ManyToOne
	@JoinColumn(name = "player_id")
	private Player player;

	// 素点
	private Integer score;

	// 着順
	private Integer rankOrder;

	// 計算後ポイント
	private Double point;
}