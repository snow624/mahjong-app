package com.snow.mahjong.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import lombok.Data;

//対戦表

@Entity
@Data
public class MatchPlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 試合
    @ManyToOne
    @JoinColumn(name = "match_id")
    private Match match;

    // プレイヤー
    @ManyToOne
    @JoinColumn(name = "player_id")
    private Player player;

    // 席順（1〜4）
    private Integer seatOrder;
}