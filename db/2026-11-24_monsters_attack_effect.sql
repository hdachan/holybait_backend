-- 2026-11-24 몬스터별 공격 이펙트 (3단계)
-- 먼저 2026-11-24_monsters_replace.sql 을 실행한 뒤에 실행 (새 image_key 기준)
-- 값 = 앱 이펙트 이름: claw(발톱 3줄) / bite(물기) / crack(화면 금 가기) / fire_breath(불 뿜기) / slam(몸통 박치기) / poison(독 튀기기)
-- NULL 이면 앱이 발톱 3줄로 보여줌. 나중에 바꿀 땐 UPDATE 만 다시 하면 됨 (앱 배포 필요 없음)

-- 1) 칸 만들기
-- 새 서버를 한 번 켜면 ddl-auto: update 가 attack_effect 칸을 자동으로 만듦.
-- 아래 SELECT 결과가 0이면(칸이 아직 없으면) ALTER 를 실행하고, 1이면 건너뜀.
SELECT COUNT(*) AS has_attack_effect FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'monsters' AND COLUMN_NAME = 'attack_effect';
-- ALTER TABLE monsters ADD COLUMN attack_effect VARCHAR(50) NULL;

-- 2) 몬스터별 이펙트
UPDATE monsters SET attack_effect = 'bite'        WHERE image_key = 'monster_pink_snake';  -- 분홍 뱀 = 물기
UPDATE monsters SET attack_effect = 'poison'      WHERE image_key = 'monster_leaf';        -- 나뭇잎 몬스터 = 독 튀기기
UPDATE monsters SET attack_effect = 'slam'        WHERE image_key = 'monster_black_blob';  -- 검은 몬스터 = 몸통 박치기
UPDATE monsters SET attack_effect = 'fire_breath' WHERE image_key = 'monster_fire_blob';   -- 불덩이 몬스터 = 불 뿜기
UPDATE monsters SET attack_effect = 'fire_breath' WHERE image_key = 'monster_flame_black'; -- 불꽃 검은 몬스터 = 불 뿜기
UPDATE monsters SET attack_effect = 'crack'       WHERE image_key = 'monster_flame_ghost'; -- 불꽃 유령 = 화면 금 가기
UPDATE monsters SET attack_effect = 'fire_breath' WHERE image_key = 'monster_blue_flame';  -- 파란 불꽃 몬스터 = 불 뿜기
UPDATE monsters SET attack_effect = 'crack'       WHERE image_key = 'monster_witch_hat';   -- 마법사 모자 몬스터 = 화면 금 가기

-- 3) 확인 (8마리 모두 값이 있어야 함)
SELECT id, stage_id, name, image_key, attack_effect FROM monsters ORDER BY stage_id, id;
