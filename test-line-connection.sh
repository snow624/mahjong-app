#!/bin/bash

# LINE API接続テストスクリプト
# 使用方法: bash test-line-connection.sh

echo "🧪 LINE API接続テストを開始します..."
echo ""

# 設定ファイルから値を読み込む
read -p "チャネルアクセストークンを入力: " CHANNEL_TOKEN
read -p "グループIDを入力: " GROUP_ID

if [ -z "$CHANNEL_TOKEN" ] || [ -z "$GROUP_ID" ]; then
    echo "❌ エラー: トークンとグループIDは必須です"
    exit 1
fi

echo ""
echo "📍 テストメッセージを送信中..."

# LINE Messaging APIを呼び出し
RESPONSE=$(curl -s -X POST https://api.line.me/v2/bot/message/push \
  -H "Authorization: Bearer $CHANNEL_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"to\":\"$GROUP_ID\",
    \"messages\":[{
      \"type\":\"text\",
      \"text\":\"✅ LINE Bot接続テスト成功！\nこのメッセージが表示されれば設定は正しいです。\"
    }]
  }")

# レスポンス確認
if [ "$RESPONSE" == "{}" ]; then
    echo "✅ 成功! メッセージがグループに送信されました"
    echo ""
    echo "次のステップ:"
    echo "1. LINEグループでメッセージが届いているか確認"
    echo "2. アプリを起動"
    echo "3. 試合結果を入力して自動通知をテスト"
else
    echo "❌ エラーが発生しました"
    echo "レスポンス: $RESPONSE"
    echo ""
    echo "確認事項:"
    echo "- チャネルアクセストークンが正しいか"
    echo "- グループIDが正しいか"
    echo "- Botがグループに参加しているか"
fi
