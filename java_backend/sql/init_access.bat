@echo off
chcp 65001 >nul
echo ==========================================
echo  葛仙米 - 用户大棚授权表初始化
echo ==========================================
echo.

set MYSQL="C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
set USER=root
set PASS=3523048584

echo [1/3] 创建表 user_greenhouse_access ...
%MYSQL% -u%USER% -p%PASS% gexianmi -e "CREATE TABLE IF NOT EXISTS user_greenhouse_access (id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID', user_id BIGINT NOT NULL COMMENT '用户ID，关联 users.id', greenhouse_id BIGINT NOT NULL COMMENT '大棚ID，关联 greenhouses.id', created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '授权时间', PRIMARY KEY (id), UNIQUE KEY uk_user_greenhouse (user_id, greenhouse_id), CONSTRAINT fk_uga_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, CONSTRAINT fk_uga_greenhouse FOREIGN KEY (greenhouse_id) REFERENCES greenhouses(id) ON DELETE CASCADE) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户-大棚授权表';" 2>&1
if %ERRORLEVEL% NEQ 0 ( echo [失败] 建表出错，请检查MySQL是否启动 & pause & exit )

echo [2/3] 插入初始授权数据 ...
%MYSQL% -u%USER% -p%PASS% gexianmi -e "INSERT IGNORE INTO user_greenhouse_access (user_id, greenhouse_id) VALUES (4, 2);" 2>&1
%MYSQL% -u%USER% -p%PASS% gexianmi -e "INSERT IGNORE INTO user_greenhouse_access (user_id, greenhouse_id) VALUES (3, 1);" 2>&1
%MYSQL% -u%USER% -p%PASS% gexianmi -e "INSERT IGNORE INTO user_greenhouse_access (user_id, greenhouse_id) VALUES (3, 3);" 2>&1
%MYSQL% -u%USER% -p%PASS% gexianmi -e "INSERT IGNORE INTO user_greenhouse_access (user_id, greenhouse_id) VALUES (2, 1);" 2>&1

echo [3/3] 验证结果 ...
%MYSQL% -u%USER% -p%PASS% gexianmi -e "SELECT uga.id, u.username, g.name AS greenhouse FROM user_greenhouse_access uga JOIN users u ON uga.user_id = u.id JOIN greenhouses g ON uga.greenhouse_id = g.id;" 2>&1

echo.
echo ==========================================
echo  初始化完成！
echo  授权说明：
echo    lisi(uid=4)   → B区大棚
echo    bby(uid=3)    → A区大棚 + C区大棚
echo    newuser(uid=2)→ A区大棚
echo    admin(uid=1)  → 管理员，无需授权，可看全部
echo ==========================================
pause
