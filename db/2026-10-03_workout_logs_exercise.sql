-- 운동 기록을 루틴이 아닌 "운동" 기준으로 저장하도록 변경 (2026-10-03)
-- 서버를 끈 상태에서 1번만 실행 → 그다음 새 코드로 서버 실행
-- (로컬 DB는 2026-10-03 적용 완료. 운영 DB는 새 서버 배포 전에 실행)

-- 1) 기록에 운동 칸 추가
ALTER TABLE workout_logs ADD COLUMN exercise_id BIGINT NULL;

-- 2) 기존 기록에 운동 채우기
UPDATE workout_logs wl
JOIN routine_exercises re ON wl.routine_exercise_id = re.id
SET wl.exercise_id = re.exercise_id
WHERE wl.exercise_id IS NULL;

-- 3) 루틴에서 운동이 빠져도 기록이 남도록 루틴 연결을 비울 수 있게 변경
ALTER TABLE workout_logs MODIFY routine_exercise_id BIGINT NULL;

-- 확인: 0 이 나와야 정상
SELECT COUNT(*) AS logs_without_exercise FROM workout_logs WHERE exercise_id IS NULL;
