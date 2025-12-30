# 數據庫初始化說明

## 方式一：使用 SQL 腳本手動創建（推薦）

1. 確保 MySQL 服務已啟動
2. 執行 `init.sql` 腳本：
   ```bash
   mysql -u root -p < database/init.sql
   ```
   或在 MySQL 客戶端中執行：
   ```sql
   source database/init.sql
   ```

## 方式二：使用 JPA 自動創建

1. 確保 `application.properties` 中的配置正確：
   ```properties
   spring.jpa.hibernate.ddl-auto=update
   ```

2. 啟動 Spring Boot 應用程序，JPA 會自動根據實體類創建表結構

3. **注意**：首次啟動時，確保數據庫 `zupder` 已存在（但表可以不存在）

## 數據庫表結構

### file_info（文件信息表）
- `id`: 主鍵，自增
- `pickup_code`: 取件碼（6位），唯一索引
- `file_name`: 文件名稱
- `file_size`: 文件大小（字節）
- `file_md5`: 文件MD5值
- `file_path`: 文件存儲路徑
- `upload_time`: 上傳時間
- `deleted`: 是否已刪除（0/1）

### upload_task（上傳任務表）
- `id`: 主鍵，自增
- `upload_id`: 上傳任務ID（UUID），唯一索引
- `file_name`: 文件名稱
- `file_size`: 文件大小（字節）
- `file_md5`: 文件MD5值
- `total_chunks`: 分片總數
- `uploaded_chunks`: 已上傳分片數
- `temp_dir`: 臨時文件存儲目錄
- `create_time`: 創建時間
- `completed`: 是否已完成（0/1）

## 注意事項

1. 數據庫字符集使用 `utf8mb4`，支持完整的 Unicode 字符
2. 所有表使用 InnoDB 引擎，支持事務
3. 已為常用查詢字段創建索引，提升查詢性能



