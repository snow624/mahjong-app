package com.snow.mahjong.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

/**
 * LINE通知サービス
 * 試合結果をLINEグループに通知
 */
@Slf4j
@Service
public class LineNotificationService {

    @Value("${line.bot.channel-token:}")
    private String channelToken;

    @Value("${line.bot.group-id:}")
    private String groupId;

    private final WebClient webClient;

    public LineNotificationService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.line.me").build();
    }

    /**
     * 試合結果をLINEグループに通知
     * 
     * @param matchNumber 試合番号
     * @param results ランキング結果 [{"rank": 1, "playerName": "太郎", "points": 50}, ...]
     * @param rankingUrl ランキングページのURL
     */
    public void notifyMatchResult(int matchNumber, List<Map<String, Object>> results, String rankingUrl) {
        if (channelToken.isEmpty() || groupId.isEmpty()) {
            log.warn("LINE Bot設定が不完全です。通知をスキップします。");
            return;
        }

        try {
            String message = buildMatchResultMessage(matchNumber, results, rankingUrl);
            sendLineMessage(message);
            log.info("LINE通知完了: 第{}試合", matchNumber);
        } catch (Exception e) {
            log.error("LINE通知エラー", e);
        }
    }

    /**
     * メッセージを構築
     */
    private String buildMatchResultMessage(int matchNumber, List<Map<String, Object>> results, String rankingUrl) {
        StringBuilder sb = new StringBuilder();
        sb.append("第").append(matchNumber).append("試合の結果が入力されました！\n\n");

        // ランキング情報
        for (Map<String, Object> result : results) {
            int rank = (int) result.get("rank");
            String playerName = (String) result.get("playerName");
            int points = (int) result.get("points");

            sb.append(rank).append("位 ").append(playerName).append(" ");
            if (points > 0) {
                sb.append("+");
            }
            sb.append(points).append("pt\n");
        }

        sb.append("\n現在のランキングはこちら↓\n");
        sb.append(rankingUrl);

        return sb.toString();
    }

    /**
     * LINE Messaging APIでメッセージを送信
     */
    private void sendLineMessage(String messageText) {
        String url = "/v2/bot/message/push";

        Map<String, Object> requestBody = Map.of(
            "to", groupId,
            "messages", List.of(
                Map.of(
                    "type", "text",
                    "text", messageText
                )
            )
        );

        webClient.post()
            .uri(url)
            .header("Authorization", "Bearer " + channelToken)
            .header("Content-Type", "application/json")
            .bodyValue(requestBody)
            .retrieve()
            .toBodilessEntity()
            .doOnError(e -> log.error("LINE API呼び出しエラー", e))
            .block();
    }

    /**
     * テスト用: 簡単なメッセージを送信
     */
    public void sendTestMessage() {
        sendLineMessage("🧪 テストメッセージです。LINE Bot接続テスト成功！");
    }
}
