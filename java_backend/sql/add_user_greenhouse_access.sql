-- ============================================
-- 用户-大棚 数据权限授权表
-- 用于实现操作员与大棚的多对多绑定
-- 管理员不经过此表，直接查全部
-- ============================================

CREATE TABLE IF NOT EXISTS user_greenhouse_access (
    id              BIGINT      NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    user_id         BIGINT      NOT NULL                 COMMENT '用户ID，关联 users.id',
    greenhouse_id   BIGINT      NOT NULL                 COMMENT '大棚ID，关联 greenhouses.id',
    created_at      DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '授权时间',

    PRIMARY KEY (id),
    UNIQUE KEY uk_user_greenhouse (user_id, greenhouse_id),

    CONSTRAINT fk_uga_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_uga_greenhouse
        FOREIGN KEY (greenhouse_id) REFERENCES greenhouses(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户-大棚授权表';


-- ============================================
-- 初始数据：假设已有 3 个操作员和 3 个大棚
-- 分配规则：
--   zhangsan(操作员) → A区大棚 + C区大棚
--   lisi(操作员)     → B区大棚
--   wangwu(操作员)   → A区大棚（与张三共享）
-- 管理员(admin)不需要授权记录
-- ============================================

INSERT INTO user_greenhouse_access (user_id, greenhouse_id)
SELECT u.id, g.id FROM users u, greenhouses g
WHERE u.username = 'zhangsan' AND u.role = 'operator'
  AND g.code IN ('GH-A', 'GH-C');

INSERT INTO user_greenhouse_access (user_id, greenhouse_id)
SELECT u.id, g.id FROM users u, greenhouses g
WHERE u.username = 'lisi' AND u.role = 'operator'
  AND g.code = 'GH-B';

INSERT INTO user_greenhouse_access (user_id, greenhouse_id)
SELECT u.id, g.id FROM users u, greenhouses g
WHERE u.username = 'wangwu' AND u.role = 'operator'
  AND g.code = 'GH-A';
