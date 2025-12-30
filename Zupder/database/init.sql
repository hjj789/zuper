-- ============================================
-- Zupder 數據庫初始化腳本
-- ============================================
-- 說明：此腳本用於手動創建數據庫和表結構
-- 執行方式：在 MySQL 客戶端執行此腳本，或使用命令行：
-- mysql -u root -p < init.sql
-- ============================================

-- 創建數據庫（如果不存在）
CREATE DATABASE IF NOT EXISTS zupder DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE zupder;

-- ============================================
-- 文件信息表 (file_info)
-- ============================================
-- 用於存儲已上傳完成的文件信息
-- ============================================
DROP TABLE IF EXISTS file_info;
CREATE TABLE file_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主鍵ID',
    pickup_code VARCHAR(6) NOT NULL UNIQUE COMMENT '取件碼（6位字母數字）',
    file_name VARCHAR(500) NOT NULL COMMENT '文件名稱',
    file_size BIGINT NOT NULL COMMENT '文件大小（字節）',
    file_md5 VARCHAR(32) COMMENT '文件MD5值（32位）',
    file_path VARCHAR(1000) NOT NULL COMMENT '文件存儲路徑',
    upload_time DATETIME NOT NULL COMMENT '上傳時間',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已刪除（0:否, 1:是）',
    INDEX idx_pickup_code (pickup_code),
    INDEX idx_deleted (deleted),
    INDEX idx_upload_time (upload_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件信息表';

-- ============================================
-- 上傳任務表 (upload_task)
-- ============================================
-- 用於管理分片上傳任務的進度
-- ============================================
DROP TABLE IF EXISTS upload_task;
CREATE TABLE upload_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主鍵ID',
    upload_id VARCHAR(36) NOT NULL UNIQUE COMMENT '上傳任務ID（UUID）',
    file_name VARCHAR(500) NOT NULL COMMENT '文件名稱',
    file_size BIGINT NOT NULL COMMENT '文件大小（字節）',
    file_md5 VARCHAR(32) COMMENT '文件MD5值（32位）',
    total_chunks INT NOT NULL COMMENT '分片總數',
    uploaded_chunks INT NOT NULL DEFAULT 0 COMMENT '已上傳分片數',
    temp_dir VARCHAR(1000) NOT NULL COMMENT '臨時文件存儲目錄',
    create_time DATETIME NOT NULL COMMENT '創建時間',
    completed TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已完成（0:否, 1:是）',
    INDEX idx_upload_id (upload_id),
    INDEX idx_completed (completed),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='上傳任務表';

-- ============================================
-- 訪問嘗試記錄表 (access_attempt)
-- ============================================
-- 用於記錄取件碼驗證嘗試，防止暴力破解
-- ============================================
DROP TABLE IF EXISTS access_attempt;
CREATE TABLE access_attempt (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主鍵ID',
    ip_address VARCHAR(45) NOT NULL COMMENT 'IP地址',
    pickup_code VARCHAR(6) COMMENT '嘗試的取件碼',
    attempt_time DATETIME NOT NULL COMMENT '嘗試時間',
    success TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否成功（0:否, 1:是）',
    user_agent VARCHAR(500) COMMENT '用戶代理（瀏覽器信息）',
    INDEX idx_ip_address (ip_address),
    INDEX idx_attempt_time (attempt_time),
    INDEX idx_success (success),
    INDEX idx_ip_time (ip_address, attempt_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='訪問嘗試記錄表';

-- ============================================
-- 表結構說明
-- ============================================
-- file_info 表字段說明：
--   - id: 自增主鍵
--   - pickup_code: 6位取件碼，唯一索引，用於下載文件
--   - file_name: 原始文件名
--   - file_size: 文件大小（字節）
--   - file_md5: 文件MD5值，用於校驗文件完整性
--   - file_path: 文件在服務器上的存儲路徑
--   - upload_time: 文件上傳時間
--   - deleted: 軟刪除標記，0表示未刪除，1表示已刪除
--
-- upload_task 表字段說明：
--   - id: 自增主鍵
--   - upload_id: 上傳任務的唯一標識（UUID）
--   - file_name: 要上傳的文件名
--   - file_size: 文件總大小
--   - file_md5: 文件MD5值
--   - total_chunks: 文件分片總數
--   - uploaded_chunks: 已成功上傳的分片數量
--   - temp_dir: 臨時存儲分片的目錄路徑
--   - create_time: 任務創建時間
--   - completed: 任務是否已完成，完成後會合併分片並生成取件碼
-- ============================================


