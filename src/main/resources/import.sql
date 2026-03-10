-- 1. 테스트 유저 생성 (ID: 1)
-- 엔티티의 필드명이 user_id가 맞는지, 혹은 id인지 확인 필요합니다.
INSERT INTO users (user_id, total_point) VALUES (1, 0);

-- 2. 적립 정책 설정 (POINT_ADD_LIMIT)
-- 한 줄로 길게 쓰지 않고 명확하게 VALUES를 붙여줍니다.
INSERT INTO point_policies (policy_id, policy_name, min_value, max_value) VALUES (1, 'POINT_ADD_LIMIT', 1, 1000000);