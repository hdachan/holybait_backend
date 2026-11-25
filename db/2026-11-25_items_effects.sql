-- 2026-11-25 가방(아이템) 1단계: 내 공격 이펙트 6종
-- 표 items / user_items 는 새 서버를 한 번 켜면 ddl-auto: update 가 자동으로 만듦
--   → 순서: 새 서버 배포·재시작 → 아래 1) 확인 → 2) 실행
-- 여러 번 실행해도 결과가 같음 (NOT EXISTS)
-- 나중에 재료를 넣을 때는 type = 'MATERIAL' 로 같은 표에 추가

-- 1) 표가 생겼는지 확인 (2가 나와야 함)
SELECT COUNT(*) AS tables_ready FROM information_schema.TABLES
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME IN ('items', 'user_items');

-- 2) 이펙트 6종 (item_key = 앱 assets/images/effects/{item_key})
--    불꽃 베기 = 기본(모두 보유, 무료). 나머지 = 2200원 (인앱결제는 다음 단계, product_id 는 그때 채움)
INSERT INTO items (type, item_key, name, description, price_krw, product_id, is_default, sort_order, is_active, created_at)
SELECT 'EFFECT', 'fire_slash', '불꽃 베기', '기본 이펙트', NULL, NULL, 1, 1, 1, NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM items WHERE item_key = 'fire_slash');

INSERT INTO items (type, item_key, name, description, price_krw, product_id, is_default, sort_order, is_active, created_at)
SELECT 'EFFECT', 'cross_slash', '십자 베기', NULL, 2200, NULL, 0, 2, 1, NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM items WHERE item_key = 'cross_slash');

INSERT INTO items (type, item_key, name, description, price_krw, product_id, is_default, sort_order, is_active, created_at)
SELECT 'EFFECT', 'impact', '주먹 충격', NULL, 2200, NULL, 0, 3, 1, NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM items WHERE item_key = 'impact');

INSERT INTO items (type, item_key, name, description, price_krw, product_id, is_default, sort_order, is_active, created_at)
SELECT 'EFFECT', 'lightning', '번개', NULL, 2200, NULL, 0, 4, 1, NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM items WHERE item_key = 'lightning');

INSERT INTO items (type, item_key, name, description, price_krw, product_id, is_default, sort_order, is_active, created_at)
SELECT 'EFFECT', 'star_pop', '별 팡팡', NULL, 2200, NULL, 0, 5, 1, NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM items WHERE item_key = 'star_pop');

INSERT INTO items (type, item_key, name, description, price_krw, product_id, is_default, sort_order, is_active, created_at)
SELECT 'EFFECT', 'magic_burst', '마법 폭발', NULL, 2200, NULL, 0, 6, 1, NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM items WHERE item_key = 'magic_burst');

-- 3) 확인 (6줄)
SELECT id, type, item_key, name, price_krw, is_default, sort_order FROM items ORDER BY type, sort_order;

-- ── 테스트용: 결제 전에 내 계정에 이펙트 직접 넣어 보기 (로컬 DB에서만 권장) ──
-- {내 users.id} 를 바꿔서 실행. 지울 때는 DELETE FROM user_items WHERE user_id = {id} AND source = 'ADMIN';
-- INSERT INTO user_items (user_id, item_id, quantity, equipped, source, acquired_at)
-- SELECT {내 users.id}, id, 1, 0, 'ADMIN', NOW() FROM items WHERE item_key = 'lightning';
