package com.snow.mahjong.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.snow.mahjong.entity.Player;

//プレイヤー登録機能（IDと名前）
//DB操作を自動でやってくれるクラス。保存（save）、全件取得（findAll）、1件取得（findById）、削除（delete）できる
//Controller → Repository → DB

public interface PlayerRepository extends JpaRepository<Player, Long> {
}