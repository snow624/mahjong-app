package com.snow.mahjong.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

/**
 * LINEからのWebhook受信ハンドラー
 * （将来の拡張用に予約）
 */
@Slf4j
@RestController
public class LineWebhookController {

	/**
	 * LINEからのWebhookイベント受信
	 * 現在は受信確認のみ
	 */
	@PostMapping("/callback")
	public ResponseEntity<String> handleWebhook(@RequestBody String body) {
		log.info("LINE Webhook受信: {}", body);

		return ResponseEntity.ok("OK");
	}
}