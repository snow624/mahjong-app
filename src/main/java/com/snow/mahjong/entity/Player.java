package com.snow.mahjong.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import lombok.Data;

//プレイヤー登録機能（IDと名前）

@Entity
@Data
public class Player {
	
	@Id
	
	//自動でIDは増える
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String name;
	
	private String iconPath;

}
