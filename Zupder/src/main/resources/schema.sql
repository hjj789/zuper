-- 創建數據庫（如果不存在）
CREATE DATABASE IF NOT EXISTS zupder DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE zupder;

-- 文件信息表
CREATE TABLE IF NOT EXISTS file_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主鍵ID',
    pickup_code VARCHAR(6) NOT NULL UNIQUE COMMENT '取件碼（6位）',
    file_name VARCHAR(500) NOT NULL COMMENT '文件名稱',
    file_size BIGINT NOT NULL COMMENT '文件大小（字節）',
    file_md5 VARCHAR(32) COMMENT '文件MD5值',
    file_path VARCHAR(1000) NOT NULL COMMENT '文件存儲路徑',
    upload_time DATETIME NOT NULL COMMENT '上傳時間',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已刪除（0:否, 1:是）',
    INDEX idx_pickup_code (pickup_code),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件信息表';

-- 上傳任務表
CREATE TABLE IF NOT EXISTS upload_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主鍵ID',
    upload_id VARCHAR(36) NOT NULL UNIQUE COMMENT '上傳任務ID（UUID）',
    file_name VARCHAR(500) NOT NULL COMMENT '文件名稱',
    file_size BIGINT NOT NULL COMMENT '文件大小（字節）',
    file_md5 VARCHAR(32) COMMENT '文件MD5值',
    total_chunks INT NOT NULL COMMENT '分片總數',
    uploaded_chunks INT NOT NULL DEFAULT 0 COMMENT '已上傳分片數',
    temp_dir VARCHAR(1000) NOT NULL COMMENT '臨時文件存儲目錄',
    create_time DATETIME NOT NULL COMMENT '創建時間',
    completed TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已完成（0:否, 1:是）',
    INDEX idx_upload_id (upload_id),
    INDEX idx_completed (completed),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='上傳任務表';



