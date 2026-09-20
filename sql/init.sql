-- ============================================================
-- 基于 SpringBoot 的在线文本情感分析系统 - 建库建表脚本
-- 适用：MySQL 8.x，字符集 utf8mb4
-- 说明：系统首次启动会自动创建管理员账号 admin/admin123，
--       无需在此处插入密码哈希。
-- ============================================================

CREATE DATABASE IF NOT EXISTS sentiment_db
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

USE sentiment_db;

-- ------------------------------------------------------------
-- 1. 用户表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
  id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  username    VARCHAR(50)  NOT NULL COMMENT '用户名',
  password    VARCHAR(100) NOT NULL COMMENT '密码（BCrypt 加密）',
  nickname    VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
  avatar      VARCHAR(255) DEFAULT NULL COMMENT '头像 URL',
  role        VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT '角色：USER / ADMIN',
  create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 否 1 是',
  PRIMARY KEY (id),
  UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ------------------------------------------------------------
-- 2. 情感分析记录表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sentiment_record;
CREATE TABLE sentiment_record (
  id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id        BIGINT       NOT NULL COMMENT '所属用户',
  content        TEXT         NOT NULL COMMENT '待分析文本',
  sentiment_type TINYINT      NOT NULL DEFAULT 0 COMMENT '情感结果：1 正向 / 0 中性 / -1 负向',
  score          DECIMAL(8,4) DEFAULT 0 COMMENT '情感得分',
  confidence     DECIMAL(8,4) DEFAULT 0 COMMENT '置信度（0~1）',
  keywords       VARCHAR(255) DEFAULT NULL COMMENT '命中的情感关键词（逗号分隔）',
  engine_type    VARCHAR(20)  NOT NULL DEFAULT 'dictionary' COMMENT '分析引擎：dictionary',
  create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '分析时间',
  deleted        TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 否 1 是',
  PRIMARY KEY (id),
  KEY idx_user_time (user_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='情感分析记录表';

-- ------------------------------------------------------------
-- 3. 情感词典表（管理员自定义词，运行时与内置 CSV 合并生效）
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sentiment_dict;
CREATE TABLE sentiment_dict (
  id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  word        VARCHAR(50)  NOT NULL COMMENT '情感词',
  polarity    TINYINT      NOT NULL COMMENT '极性：1 正向 / -1 负向',
  weight      DECIMAL(6,2) NOT NULL DEFAULT 1.00 COMMENT '情感强度权重',
  category    VARCHAR(50)  DEFAULT NULL COMMENT '类别（可选，如 名词/动词/评价词）',
  is_custom   TINYINT      NOT NULL DEFAULT 1 COMMENT '是否自定义：1 用户自定义 / 0 内置',
  create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_word (word)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='情感词典表';

-- ------------------------------------------------------------
-- 4. 操作日志表（可选，用于论文"系统测试"章节的审计展示）
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_log;
CREATE TABLE sys_log (
  id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id     BIGINT       DEFAULT NULL COMMENT '操作用户',
  action      VARCHAR(100) DEFAULT NULL COMMENT '操作描述',
  detail      VARCHAR(500) DEFAULT NULL COMMENT '操作详情',
  create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 测试用：插入少量示例情感词（与内置 CSV 功能一致，便于演示"词典管理"）
INSERT INTO sentiment_dict (word, polarity, weight, category, is_custom) VALUES
  ('性价比高', 1, 2.0, '评价词', 1),
  ('服务态度差', -1, 2.0, '评价词', 1),
  ('强烈推荐', 1, 2.5, '评价词', 1);
