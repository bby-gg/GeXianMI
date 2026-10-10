USE gexianmi;

CREATE TABLE IF NOT EXISTS alert_rules (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  param VARCHAR(32) NOT NULL UNIQUE COMMENT '参数名 waterTemp/ph/dissolvedOxygen/lightIntensity/conductivity',
  param_name VARCHAR(32) NOT NULL COMMENT '中文名 水温/pH值/溶解氧/光照/电导率',
  unit VARCHAR(16) NOT NULL DEFAULT '' COMMENT '单位 °C/mg/L/lux/μS/cm',
  min_val DECIMAL(8,2) COMMENT '正常下限',
  max_val DECIMAL(8,2) COMMENT '正常上限',
  threshold_low DECIMAL(8,2) COMMENT '告警下限(低于此值告警)',
  threshold_high DECIMAL(8,2) COMMENT '告警上限(高于此值告警)',
  level VARCHAR(16) NOT NULL DEFAULT 'warning' COMMENT 'warning/error',
  enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用 0/1',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO alert_rules (param, param_name, unit, min_val, max_val, threshold_low, threshold_high, level, enabled) VALUES
('waterTemp',        '水温',   '°C',    15, 25,  10, 30,  'warning', 1),
('ph',               'pH值',   '',      6.5, 7.5, 6.0, 8.0, 'warning', 1),
('dissolvedOxygen',  '溶解氧', 'mg/L',  5, 10,   4, 12,   'error',   1),
('lightIntensity',   '光照',   'lux',   800, 1500, 500, 2000, 'warning', 1),
('conductivity',     '电导率', 'μS/cm', 300, 400, 250, 450, 'warning', 1)
ON DUPLICATE KEY UPDATE param_name=VALUES(param_name);
