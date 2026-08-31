# ⚡ 1分でわかる！トークン記述ガイド

## 📝 2つの場所のどちらかに記述

---

## 🔵 **方法1: 直接記入（ローカルテスト向け）** ← 今すぐテストしたい人

### ファイルを開く
```
Eclipse → mahjong-app → src/main/resources → application.properties
```

### 最後に以下を追加
```properties
line.bot.channel-token=1234567890abcdef1234567890abcdef
line.bot.channel-secret=abcdef1234567890abcdef1234567890
line.bot.group-id=C1234567890abcdef1234567890abcdef
```

### 保存＆実行
```bash
Ctrl+S で保存
./mvnw spring-boot:run
```

---

## 🟢 **方法2: 環境変数（本番環境向け）** ← GitHubにアップするなら

### ターミナルで実行
```bash
export LINE_BOT_CHANNEL_TOKEN=1234567890abcdef1234567890abcdef
export LINE_BOT_CHANNEL_SECRET=abcdef1234567890abcdef1234567890
export LINE_BOT_GROUP_ID=C1234567890abcdef1234567890abcdef

./mvnw spring-boot:run
```

### application.properties は
```properties
# このまま（トークンは書かない）
line.bot.channel-token=${LINE_BOT_CHANNEL_TOKEN:}
line.bot.channel-secret=${LINE_BOT_CHANNEL_SECRET:}
line.bot.group-id=${LINE_BOT_GROUP_ID:}
```

---

## 💡 どちらを選べばいい？

| 方法 | 場面 | セキュリティ |
|-----|------|----------|
| 🔵 直接記入 | ローカルで動作確認 | ⚠️ 注意が必要 |
| 🟢 環境変数 | 本番環境・GitHub | ✅ 安全 |

---

## ⚠️ 重要

**方法1を使う場合は、GitHubにアップする前に削除してください！**

```bash
# 削除方法
line.bot.channel-token=実際のトークン
↓
# 削除する
```

`.gitignore` に `application.properties` が登録されているので自動的に除外されます。

---

## 🧪 動作確認

トークンを記入したら、以下で確認：

```bash
bash test-line-connection.sh
```

プロンプトが出たら、記入したトークンとグループIDを入力してテスト。

---

## ✅ できました！

次は試合結果を入力してLINE通知が来るか確認します。
