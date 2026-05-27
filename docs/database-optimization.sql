-- 麻雀アプリ データベース最適化スクリプト
-- このスクリプトは、パフォーマンス向上のためのインデックスを追加します

-- ====================================
-- MatchPlayer テーブルのインデックス
-- ====================================

-- match_id でのクエリが頻繁なため
CREATE INDEX IF NOT EXISTS idx_match_player_match_id ON match_player(match_id);

-- player_id でのクエリに対応
CREATE INDEX IF NOT EXISTS idx_match_player_player_id ON match_player(player_id);

-- match_id + seat_order でのクエリに最適
CREATE INDEX IF NOT EXISTS idx_match_player_match_seat ON match_player(match_id, seat_order);


-- ====================================
-- MatchResult テーブルのインデックス
-- ====================================

-- match_id でのクエリが頻繁なため
CREATE INDEX IF NOT EXISTS idx_match_result_match_id ON match_result(match_id);

-- player_id でのクエリに対応
CREATE INDEX IF NOT EXISTS idx_match_result_player_id ON match_result(player_id);

-- ランキング計算用：player_id でのポイント集計が頻繁
CREATE INDEX IF NOT EXISTS idx_match_result_player_point ON match_result(player_id, point);


-- ====================================
-- Match テーブルのインデックス
-- ====================================

-- matchNumber でのクエリ
CREATE INDEX IF NOT EXISTS idx_match_number ON matches(match_number);


-- ====================================
-- Player テーブルのインデックス
-- ====================================

-- Player テーブルは小規模なのでインデックスは不要な可能性がありますが、
-- 将来の拡張に備えて name でのインデックスを追加するのは有効です
CREATE INDEX IF NOT EXISTS idx_player_name ON player(name);
