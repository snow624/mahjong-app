# LINE API テスト設定ガイド

## 🎯 チャネルシークレット・アクセストークンの記述場所

---

## 📝 取得した情報を記述する場所

### LINE Developers で取得した情報：

```
✏️ チャネルアクセストークン:  1234567890abcdef1234567890abcdef
✏️ チャネルシークレット:      abcdef1234567890abcdef1234567890
✏️ グループID:                C1234567890abcdef1234567890abcdef
```

---

## 🔧 方法A: ローカル開発（テスト用）

### ファイル：`src/main/resources/application.properties`

**記入場所：** ファイルの最後の方

```properties
# LINE BOT設定
line.bot.channel-token=1234567890abcdef1234567890abcdef
line.bot.channel-secret=abcdef1234567890abcdef1234567890
line.bot.group-id=C1234567890abcdef1234567890abcdef
line.bot.handler.path=/callback
```

### 操作手順：

1. Eclipse で `mahjong-app` プロジェクトを展開
2. `src/main/resources/` → `application.properties` をダブルクリック
3. ファイルの最後に上記を記入
4. `Ctrl+S` で保存

### 起動方法：

```bash
./mvnw spring-boot:run
```

---

## 🔒 方法B: 本番環境（推奨・安全）

### application.properties の設定

```properties
# 環境変数から読み込む
line.bot.channel-token=${LINE_BOT_CHANNEL_TOKEN:}
line.bot.channel-secret=${LINE_BOT_CHANNEL_SECRET:}
line.bot.group-id=${LINE_BOT_GROUP_ID:}
```

### 環境変数の設定方法

**macOS / Linux:**
```bash
# ターミナルで実行
export LINE_BOT_CHANNEL_TOKEN=1234567890abcdef1234567890abcdef
export LINE_BOT_CHANNEL_SECRET=abcdef1234567890abcdef1234567890
export LINE_BOT_GROUP_ID=C1234567890abcdef1234567890abcdef

# その後にアプリを起動
./mvnw spring-boot:run
```

**Windows:**
```cmd
set LINE_BOT_CHANNEL_TOKEN=1234567890abcdef1234567890abcdef
set LINE_BOT_CHANNEL_SECRET=abcdef1234567890abcdef1234567890
set LINE_BOT_GROUP_ID=C1234567890abcdef1234567890abcdef

mvnw.cmd spring-boot:run
```

---

## 🔐 セキュリティのポイント

### ❌ やってはいけないこと：
- `application.properties` に直接記入してGitHubにコミット
- トークンをスクリーンショットで共有
- ログにトークンを出力

### ✅ 推奨される方法：
- **ローカルテスト時:** `application.properties` に直接記入（Git管理外）
- **本番環境:** 環境変数で設定
- **GitHub:** `.gitignore` に `application.properties` を追加（既に追加済み）

---

## 📍 eclipse での操作

### 1️⃣ application.properties を開く

```
Project Explorer
 └─ mahjong-app
     └─ src/main/resources
         └─ application.properties ← ここをダブルクリック
```

### 2️⃣ 最後に以下を追加

```
# LINE BOT設定
line.bot.channel-token=取得したトークン
line.bot.channel-secret=取得したシークレット
line.bot.group-id=取得したグループID
```

### 3️⃣ 保存

`Ctrl+S` で保存

### 4️⃣ 起動

```bash
cd /Users/yuki/git/mahjong-app
./mvnw spring-boot:run
```

---

## ✅ チェックリスト

- [ ] LINE Developers で認証情報を取得
- [ ] `application.properties` に記入（またはLINE環境変数を設定）
- [ ] `Ctrl+S` で保存
- [ ] アプリを起動
- [ ] LINEグループでテストメッセージが届くか確認

---

## 🆘 よくある間違い

### ❌ トークンが見つからない場合

LINE Developers コンソールで確認：
1. チャネルを選択
2. 「Messaging API」タブをクリック
3. 「チャネルアクセストークン」セクションを探す
4. 「新規発行」があればクリック

### ❌ グループIDが見つからない場合

```bash
# 以下のコマンドでグループを確認
curl -X GET https://api.line.me/v2/bot/group \
  -H "Authorization: Bearer YOUR_CHANNEL_TOKEN"
```

### ❌ 保存が反映されない

- [ ] `Ctrl+S` で保存したか？
- [ ] ファイルが正しく編集されたか？
- [ ] アプリを**再起動**したか？

---

## 📞 サポート情報

詳細は以下を参照：
- `docs/LINE_API_SETUP_GUIDE.md` - 初心者向け詳細ガイド
- `LINE_API_INTEGRATION.md` - 技術実装詳細
