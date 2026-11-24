-- 2026-11-24 몬스터 교체 (앱 배틀 연출 1단계 그림과 맞춤)
-- 기존 6마리: 이름·image_key만 바꿈 (능력치·보상·드롭은 그대로)
-- 하늘 궁전(stage 3)에 2마리 추가 (지금까지 0마리라 탐험하면 "몬스터를 찾을 수 없습니다.")
-- 여러 번 실행해도 결과가 같도록 작성 (WHERE 조건 / NOT EXISTS)

-- 0) 실행 전 확인
SELECT id, stage_id, name, image_key, level, hp, atk, def FROM monsters ORDER BY id;

-- 1) 기존 6마리 교체 (예전 image_key일 때만 바뀜)
UPDATE monsters SET name = '나뭇잎 몬스터',    image_key = 'monster_leaf'        WHERE id = 1 AND image_key = 'monster_gorilla';
UPDATE monsters SET name = '검은 몬스터',      image_key = 'monster_black_blob'  WHERE id = 2 AND image_key = 'monster_Shadow_black';
UPDATE monsters SET name = '분홍 뱀',          image_key = 'monster_pink_snake'  WHERE id = 3 AND image_key = 'monster_black';
UPDATE monsters SET name = '불덩이 몬스터',    image_key = 'monster_fire_blob'   WHERE id = 4 AND image_key = 'monster_werewolf';
UPDATE monsters SET name = '불꽃 검은 몬스터', image_key = 'monster_flame_black' WHERE id = 5 AND image_key = 'monster_dragon';
UPDATE monsters SET name = '불꽃 유령',        image_key = 'monster_flame_ghost' WHERE id = 6 AND image_key = 'monster_giant';

-- 2) 하늘 궁전(stage 3)에 2마리 추가 (이미 있으면 건너뜀)
-- 능력치 기준: Lv20 홀리 ≈ 공격 72 / 방어 62 / HP 500 (레벨업 평균 +3/+3/+21)
--   → 내가 주는 데미지 약 40, 받는 데미지 약 20~26, 10~13턴에 승리
INSERT INTO monsters (stage_id, name, level, hp, atk, def, double_attack_chance, exp_reward, gold_reward, image_key, drop_character_id, drop_chance)
SELECT 3, '파란 불꽃 몬스터', 20, 450, 80, 30, 0.10, 150, 100, 'monster_blue_flame', NULL, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM monsters WHERE image_key = 'monster_blue_flame');

INSERT INTO monsters (stage_id, name, level, hp, atk, def, double_attack_chance, exp_reward, gold_reward, image_key, drop_character_id, drop_chance)
SELECT 3, '마법사 모자 몬스터', 23, 520, 88, 32, 0.12, 190, 125, 'monster_witch_hat', NULL, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM monsters WHERE image_key = 'monster_witch_hat');

-- 3) 실행 후 확인 (8줄, 1~6번 새 이름, stage 3에 2마리)
SELECT id, stage_id, name, image_key, level, hp, atk, def, exp_reward, gold_reward FROM monsters ORDER BY stage_id, id;
