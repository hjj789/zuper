# IDE 配置說明

## 關於 StringConcatFactory 錯誤

如果 IDE 顯示 `StringConcatFactory cannot be resolved` 錯誤，這通常是 IDE 的 Java 版本配置問題，而不是代碼本身的問題。

### 解決方法：

#### 1. 檢查 IDE 的 Java 版本配置
- **IntelliJ IDEA**: 
  - File → Project Structure → Project Settings → Project
  - 確保 "SDK" 和 "Language level" 都設置為 Java 17
  - File → Settings → Build, Execution, Deployment → Compiler → Java Compiler
  - 確保 "Project bytecode version" 為 17

- **Eclipse**:
  - Project → Properties → Java Build Path → Libraries
  - 確保 JRE 版本為 Java 17
  - Project → Properties → Java Compiler
  - 確保 "Compiler compliance level" 為 17

#### 2. 重新構建項目
```bash
# 清理並重新編譯
mvn clean compile
```

#### 3. 刷新 Maven 項目
- **IntelliJ IDEA**: 右鍵點擊 `pom.xml` → Maven → Reload Project
- **Eclipse**: 右鍵點擊項目 → Maven → Update Project

#### 4. 如果問題仍然存在
嘗試刪除 `.idea` 或 `.settings` 目錄，然後重新導入項目。

## 項目要求

- **Java 版本**: 17 或更高
- **Maven 版本**: 3.6 或更高
- **Spring Boot 版本**: 4.0.1

## 編譯和運行

```bash
# 編譯項目
mvn clean compile

# 運行項目
mvn spring-boot:run

# 打包項目
mvn clean package
```



