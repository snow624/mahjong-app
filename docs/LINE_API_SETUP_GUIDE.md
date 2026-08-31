# LINE API 導入ガイド - 麻雀ランキングアプリ

## 📋 概要
試合結果が入力されたときに、自動でLINEグループにメッセージを送信するシステムです。

---

## 🚀 セットアップ手順

### ステップ1: LINE Developersアカウント登録（5分）

1. **LINE Developersサイトにアクセス**
   - https://developers.line.biz/ja/ を開く
   - 右上の「ログイン」をクリック
   - LINEアカウントでログイン（LINEアプリで登録したアカウント）

2. **新しいプロバイダーを作成**
   - 左側メニュー「プロバイダー」をクリック
   - 「新規作成」ボタン
   - プロバイダー名を入力（例：「麻雀ランキングBot」）
   - 「作成」をクリック

3. **Messaging APIチャネルを作成**
   - 作成したプロバイダーをクリック
   - 「チャネル」→「新規作成」をクリック
   - チャネルタイプから「Messaging API」を選択
   - 必要な情報を入力：
     - チャネル名：「麻雀ランキング通知」など
     - チャネルの説明：任意
     - 大業種・小業種：任意で選択
     - メールアドレス：自分のメアド
   - 「作成」をクリック

---

### ステップ2: 認証情報の取得（3分）

1. **チャネルアクセストークンを生成**
   - 作成したチャネルをクリック
   - 「Messaging API」タブを開く
   - 「チャネルアクセストークン」セクション
   - 「新規発行」ボタンをクリック
   - トークンが表示されたら**コピーして安全に保管**
   - 例：`1234567890abcdef1234567890abcdef`

2. **チャネルシークレットをコピー**
   - 「基本設定」タブをクリック
   - 「チャネルシークレット」をコピーして安全に保管
   - 例：`abcdef1234567890abcdef1234567890`

---

### ステップ3: LINEグループの作成と設定（5分）

1. **テスト用グループの作成**
   - LINEアプリでテストグループを作成
   - 人数は最低2人以上（自分 + 1人）

2. **Botアカウントをグループに招待**
   - LINE Developersコンソールの「Messaging API」タブ
   - 「QRコード」を表示
   - LINEアプリでスキャン
   - Botを「グループに招待」を選択
   - テストグループを選択して招待

3. **グループIDを確認**
   - LINE Developersコンソール → 「チャネルアクセストークン」付近
   - 下の方に「Webhook設定」があります
   - 後ほどここに設定を追加します

   **簡易的なグループID確認方法：**
   - Botをテストグループに招待後、以下のコマンドを実行：
   ```bash
   curl -X GET https://api.line.me/v2/bot/group \
     -H "Authorization: Bearer YOUR_CHANNEL_TOKEN"
   ```
   - レスポンスにグループIDが表示されます

---

### ステップ4: アプリケーション設定ファイルの編集（2分）

1. **プロジェクトルートから設定ファイルを開く**
   - ファイル：`src/main/resources/application.properties`

2. **LINE設定を追加**
   ```properties
   # LINE BOT設定
   line.bot.channel-token=YOUR_CHANNEL_ACCESS_TOKEN_HERE
   line.bot.channel-secret=YOUR_CHANNEL_SECRET_HERE
   line.bot.group-id=YOUR_GROUP_ID_HERE
   line.bot.handler.path=/callback
   ```

3. **値を置き換え**
   - `YOUR_CHANNEL_ACCESS_TOKEN_HERE` → ステップ2で取得したトークン
   - `YOUR_CHANNEL_SECRET_HERE` → ステップ2で取得したシークレット
   - `YOUR_GROUP_ID_HERE` → ステップ3で取得したグループID

   **例：**
   ```properties
   line.bot.channel-token=1234567890abcdef1234567890abcdef
   line.bot.channel-secret=abcdef1234567890abcdef1234567890
   line.bot.group-id=C1234567890abcdef1234567890abcdef
   line.bot.handler.path=/callback
   ```

---

### ステップ5: 本番環境での設定（環境変数）

**ローカルテスト環境での注意：**
- `application.properties`に直接記入してOKです
- ただし、GitHubなどにアップロード時は`.gitignore`に追加してください

**本番環境（AWS、Herokuなど）での設定：**
1. 環境変数として設定：
   - `LINE_BOT_CHANNEL_TOKEN`
   - `LINE_BOT_CHANNEL_SECRET`
   - `LINE_BOT_GROUP_ID`

2. 設定方法は利用するサーバーによって異なります

---

### ステップ6: アプリケーション起動＆テスト（10分）

1. **アプリを起動**
   ```bash
   mvn spring-boot:run
   ```
   または
   ```bash
   ./mvnw spring-boot:run
   ```

2. **ログで確認**
   - コンソールに `Started MahjongAppApplication` と表示されればOK
   - エラーが出た場合は下の「トラブルシューティング」を確認

3. **テストメッセージを送信**
   - LINE Developersコンソール → 「Messaging API」タブ
   - 「チャネルアクセストークン」の下に「テスト送信」があります
   - または、curlでテスト：
   ```bash
   curl -X POST https://api.line.me/v2/bot/message/push \
     -H "Authorization: Bearer YOUR_CHANNEL_ACCESS_TOKEN" \
     -H "Content-Type: application/json" \
     -d '{
       "to":"YOUR_GROUP_ID",
       "messages":[{
         "type":"text",
         "text":"テストメッセージです🧪"
       }]
     }'
   ```

---

## 💡 実装されたコード

### 1️⃣ **LineNotificationService** (`src/main/java/com/snow/mahjong/service/LineNotificationService.java`)
- LINE Messaging APIにメッセージを送信するService
- 試合結果のメッセージを自動生成

### 2️⃣ **MatchController更新** (`src/main/java/com/snow/mahjong/controller/MatchController.java`)
- 試合結果入力時に`LineNotificationService`を呼び出し
- 非同期で通知を送信（UIのレスポンスに影響しない）

### 3️⃣ **match_detail.html更新** (`src/main/resources/templates/match_detail.html`)
- サーバーURLを自動取得して送信
- ランキングページのURLを通知に含める

---

## 📱 送信されるメッセージの例

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

## 🔧 トラブルシューティング

### ❌ メッセージが送信されない

**原因1: 認証情報が間違っている**
- `application.properties`のトークン、シークレット、グループIDを再確認
- 特に、コピー時のスペースを確認

**原因2: グループに招待されていない**
```bash
# LINE公式Botが本当にグループに参加しているか確認
curl -X GET https://api.line.me/v2/bot/message/profile/YOUR_GROUP_ID \
  -H "Authorization: Bearer YOUR_CHANNEL_TOKEN"
```

**原因3: ファイアウォール/プロキシ設定**
- LINE APIへのHTTPS通信がブロックされていないか確認

### ❌ ビルドエラー

**エラー: `cannot find symbol: class LineNotificationService`**
```bash
# 解決方法：pom.xmlを再度確認、Mavenを再構築
mvn clean install
```

**エラー: `line-bot-spring-boot not found`**
```bash
# 解決方法：pom.xmlにLINE BOT SDKが追加されているか確認
# version 8.3.0を使用しています
```

### ❌ 環境変数が反映されない

**ローカル環境での解決**
```bash
# application.propertiesに直接記入してテスト
# 本番環境では環境変数を使用
```

---

## 📊 メッセージカスタマイズ

`LineNotificationService.java`の`buildMatchResultMessage`メソッドを編集することで、メッセージ形式をカスタマイズできます。

**例：絵文字を追加したい場合**
```java
sb.append("🏆 第").append(matchNumber).append("試合の結果が入力されました！\n\n");
sb.append("🥇 ").append(rank).append("位 ").append(playerName)...
```

---

## 🔒 セキュリティに関する注意

⚠️ **重要：**
- チャネルアクセストークン、シークレットは**絶対にGitHubにコミットしないでください**
- `.gitignore`に`application.properties`を追加するか、環境変数で管理してください

**`.gitignore`に追加の例：**
```
application.properties
application-dev.properties
.env
```

---

## 📚 参考リンク

- [LINE Messaging API公式ドキュメント](https://developers.line.biz/ja/docs/messaging-api/)
- [LINE Developers Console](https://developers.line.biz/console/)
- [Spring Boot WebClient](https://spring.io/projects/spring-framework)

---

## 🎉 確認チェックリスト

- [ ] LINE Developersアカウントを作成
- [ ] プロバイダーとチャネルを作成
- [ ] チャネルアクセストークンを取得
- [ ] チャネルシークレットを取得
- [ ] グループIDを確認
- [ ] `application.properties`に設定を追加
- [ ] アプリを起動してエラーがないか確認
- [ ] テストメッセージが送信されるか確認
- [ ] 試合結果を入力してLINE通知が来るか確認

すべて完了したら、LINE通知機能の導入は完了です！🎊
