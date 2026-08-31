# LINE API 統合 - 実装概要

## 🎯 実装内容

麻雄ランキングアプリに**LINE Messaging API**を統合し、試合結果入力時に自動でLINEグループに通知する機能を実装しました。

---

## 📁 追加・修正したファイル

### 新規作成ファイル

| ファイル | 説明 |
|---------|------|
| `src/main/java/com/snow/mahjong/service/LineNotificationService.java` | LINE通知を管理するService |
| `src/main/java/com/snow/mahjong/controller/LineWebhookController.java` | LINEからのWebhookを受信するController |
| `docs/LINE_API_SETUP_GUIDE.md` | セットアップガイド（初心者向け） |
| `test-line-connection.sh` | LINE API接続テストスクリプト |

### 修正したファイル

| ファイル | 変更内容 |
|---------|--------|
| `pom.xml` | LINE BOT SDKとWebFluxを依存関係に追加 |
| `src/main/resources/application.properties` | LINE設定の環境変数を追加 |
| `src/main/java/com/snow/mahjong/controller/MatchController.java` | 試合結果保存時にLINE通知を送信 |
| `src/main/java/com/snow/mahjong/config/WebConfig.java` | WebClientのBean定義を追加 |
| `src/main/resources/templates/match_detail.html` | サーバーURLを自動取得して送信 |

---

## 🔧 主要なクラス説明

### LineNotificationService
```java
// 試合結果をLINEグループに通知
public void notifyMatchResult(int matchNumber, List<Map<String, Object>> results, String rankingUrl)

// テストメッセージを送信
public void sendTestMessage()
```

**処理フロー：**
1. メッセージを生成（順位、プレイヤー名、ポイント）
2. LINE Messaging APIに送信
3. グループにメッセージが表示される

### MatchController（更新部分）
```java
@PostMapping("/matches/result")
public String saveResult(...) {
    // 既存の試合結果保存処理
    
    // LINE通知を非同期で送信
    sendLineNotificationAsync(match.getId(), lineResults, serverUrl);
}
```

---

## ⚙️ 設定例

**application.properties**
```properties
# LINE BOT設定
line.bot.channel-token=1234567890abcdef1234567890abcdef
line.bot.channel-secret=abcdef1234567890abcdef1234567890
line.bot.group-id=C1234567890abcdef1234567890abcdef
line.bot.handler.path=/callback
```

**環境変数での設定（本番環境）**
```bash
export LINE_BOT_CHANNEL_TOKEN=1234567890abcdef1234567890abcdef
export LINE_BOT_CHANNEL_SECRET=abcdef1234567890abcdef1234567890
export LINE_BOT_GROUP_ID=C1234567890abcdef1234567890abcdef
```

---

## 📊 送信メッセージ形式

```
第1試合の結果が入力されました！

1位 太郎 +50pt
2位 花子 +10pt
3位 次郎 -10pt
4位 美咲 -30pt

現在のランキングはこちら↓
http://localhost:8080/ranking
```

---

## 🚀 クイックスタート

### 1. LINE Developersアカウント登録
```
https://developers.line.biz/ja/
```

### 2. 設定情報の取得
- チャネルアクセストークン
- チャネルシークレット
- グループID

### 3. application.propertiesに設定
```bash
# docs/LINE_API_SETUP_GUIDE.md を参照
```

### 4. アプリ起動
```bash
mvn spring-boot:run
```

### 5. テスト
```bash
bash test-line-connection.sh
```

---

## 🔗 参考資料

- [セットアップガイド](./docs/LINE_API_SETUP_GUIDE.md) - 詳細な初心者向けガイド
- [LINE Messaging API公式](https://developers.line.biz/ja/docs/messaging-api/)

---

## 💡 カスタマイズ方法

### メッセージ形式を変更したい

`LineNotificationService.java`の`buildMatchResultMessage`メソッドを編集：

```java
private String buildMatchResultMessage(...) {
    StringBuilder sb = new StringBuilder();
    sb.append("🏆 第").append(matchNumber).append("試合！\n");
    // メッセージ形式をカスタマイズ
    return sb.toString();
}
```

### ポイント表示形式を変更したい

`MatchController.java`の`sendLineNotificationAsync`メソッド付近を編集：

```java
Map<String, Object> resultData = new HashMap<>();
resultData.put("rank", rank);
resultData.put("playerName", player.getName());
resultData.put("points", (int) point); // ここで計算方法を変更可能
```

---

## 🐛 トラブルシューティング

### メッセージが送信されない

**確認項目：**
1. チャネルアクセストークンが正しいか
2. グループIDが正しいか
3. Botがグループに参加しているか
4. ローカルPCのファイアウォール設定

**テストコマンド：**
```bash
bash test-line-connection.sh
```

### ビルドエラー

```bash
# キャッシュをクリアして再ビルド
mvn clean install
```

---

## ✅ 動作確認チェックリスト

- [ ] LINE Developersアカウント作成済み
- [ ] プロバイダーとチャネル作成済み
- [ ] 認証情報をapplication.propertiesに設定
- [ ] ローカルでテスト実行して通知が来ることを確認
- [ ] 本番環境では環境変数を使用

---

## 📝 ライセンス

このコードはMahjong Appプロジェクトの一部です。

---

## 🔐 セキュリティ注意

⚠️ **絶対にやってはいけないこと：**
- チャネルアクセストークンをGitHubにコミット
- チャネルシークレットをソースコードに記述
- 認証情報をログに出力

✅ **推奨される方法：**
- 環境変数で管理（本番環境）
- `.gitignore`に`application.properties`を追加
- 機密情報は環境変数から読み込む
