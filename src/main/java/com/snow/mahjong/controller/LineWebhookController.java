package com.snow.mahjong.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * LINEからのWebhook受信ハンドラー
 * （将来の拡張用に予約）
 */
@Slf4j
@RestController
public class LineWebhookController {

    /**
     * LINEからのWebhookイベント受信
     * 現在は受信確認のみ（将来はここでBotからのメッセージ処理が可能）
     */
    @PostMapping("/callback")
    public void handleWebhook(@RequestBody String body) {
        log.debug("Webhook受信: {}", body);
        // 現在は何もしない（将来のBotメッセージ受信に備えて予約）
    }
}
