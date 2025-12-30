package com.example.zupder.service;

import com.example.zupder.entity.FileInfo;
import com.example.zupder.entity.UploadTask;
import com.example.zupder.repository.FileInfoRepository;
import com.example.zupder.repository.UploadTaskRepository;
import com.example.zupder.service.SecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 文件服務類
 * 處理文件上傳、下載等業務邏輯
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {
    
    private final FileInfoRepository fileInfoRepository;
    private final UploadTaskRepository uploadTaskRepository;
    private final SecurityService securityService;
    
    @Value("${file.upload.dir:./uploads}")
    private String uploadDir;
    
    @Value("${file.chunk.size:5242880}")
    private Long chunkSize; // 默認5MB
    
    /**
     * 初始化文件上傳任務
     * @param fileName 文件名稱
     * @param fileSize 文件大小（字節）
     * @param fileMd5 文件MD5值
     * @return 包含uploadId、totalChunks、chunkSize的Map
     */
    @Transactional
    public Map<String, Object> initUpload(String fileName, Long fileSize, String fileMd5) {
        // 生成上傳任務ID
        String uploadId = UUID.randomUUID().toString();
        
        // 計算分片數量
        int totalChunks = (int) Math.ceil((double) fileSize / chunkSize);
        
        // 創建臨時目錄
        String tempDir = Paths.get(uploadDir, "temp", uploadId).toString();
        File dir = new File(tempDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        // 規範化MD5值（確保不超過32個字符）
        String normalizedMd5 = normalizeMd5(fileMd5);
        
        // 創建上傳任務
        UploadTask task = new UploadTask();
        task.setUploadId(uploadId);
        task.setFileName(fileName);
        task.setFileSize(fileSize);
        task.setFileMd5(normalizedMd5);
        task.setTotalChunks(totalChunks);
        task.setUploadedChunks(0);
        task.setTempDir(tempDir);
        task.setCreateTime(LocalDateTime.now());
        task.setCompleted(false);
        
        uploadTaskRepository.save(task);
        
        Map<String, Object> result = new HashMap<>();
        result.put("uploadId", uploadId);
        result.put("totalChunks", totalChunks);
        result.put("chunkSize", chunkSize);
        
        log.info("初始化上傳任務: uploadId={}, fileName={}, fileSize={}, totalChunks={}", 
                uploadId, fileName, fileSize, totalChunks);
        
        return result;
    }
    
    /**
     * 上傳文件分片
     * @param uploadId 上傳任務ID
     * @param chunkIndex 分片索引
     * @param chunk 分片文件
     * @param chunkMd5 分片MD5值（可選）
     * @return 上傳結果
     * @throws IOException IO異常
     */
    @Transactional
    public Map<String, Object> uploadChunk(String uploadId, Integer chunkIndex, MultipartFile chunk, String chunkMd5) throws IOException {
        // 查詢上傳任務
        UploadTask task = uploadTaskRepository.findByUploadId(uploadId)
                .orElseThrow(() -> new RuntimeException("上傳任務不存在: " + uploadId));
        
        if (task.getCompleted()) {
            throw new RuntimeException("上傳任務已完成");
        }
        
        // 保存分片文件
        String chunkFileName = String.format("chunk_%d", chunkIndex);
        Path chunkPath = Paths.get(task.getTempDir(), chunkFileName);
        Files.write(chunkPath, chunk.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        
        // 更新已上傳分片數
        task.setUploadedChunks(task.getUploadedChunks() + 1);
        uploadTaskRepository.save(task);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("chunkIndex", chunkIndex);
        result.put("uploadedChunks", task.getUploadedChunks());
        result.put("totalChunks", task.getTotalChunks());
        
        log.info("上傳分片: uploadId={}, chunkIndex={}, uploadedChunks={}/{}", 
                uploadId, chunkIndex, task.getUploadedChunks(), task.getTotalChunks());
        
        return result;
    }
    
    /**
     * 完成文件上傳，合併分片並生成取件碼
     * @param uploadId 上傳任務ID
     * @return 包含pickupCode、fileName、fileSize的Map
     * @throws IOException IO異常
     */
    @Transactional
    public Map<String, Object> completeUpload(String uploadId) throws IOException {
        // 查詢上傳任務
        UploadTask task = uploadTaskRepository.findByUploadId(uploadId)
                .orElseThrow(() -> new RuntimeException("上傳任務不存在: " + uploadId));
        
        if (task.getCompleted()) {
            throw new RuntimeException("上傳任務已完成");
        }
        
        // 檢查所有分片是否已上傳
        if (task.getUploadedChunks() < task.getTotalChunks()) {
            throw new RuntimeException("分片未全部上傳完成");
        }
        
        // 生成取件碼（6位隨機字母數字）
        String pickupCode = generatePickupCode();
        
        // 確保取件碼唯一
        while (fileInfoRepository.findByPickupCodeAndDeletedFalse(pickupCode).isPresent()) {
            pickupCode = generatePickupCode();
        }
        
        // 合併分片文件
        String finalFileName = task.getFileName();
        Path finalFilePath = Paths.get(uploadDir, "files", pickupCode, finalFileName);
        File finalFileDir = finalFilePath.getParent().toFile();
        if (!finalFileDir.exists()) {
            finalFileDir.mkdirs();
        }
        
        // 按順序合併所有分片
        try (var outputStream = Files.newOutputStream(finalFilePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (int i = 0; i < task.getTotalChunks(); i++) {
                String chunkFileName = String.format("chunk_%d", i);
                Path chunkPath = Paths.get(task.getTempDir(), chunkFileName);
                
                if (!Files.exists(chunkPath)) {
                    throw new RuntimeException("分片文件不存在: " + chunkFileName);
                }
                
                Files.copy(chunkPath, outputStream);
            }
        }
        
        // 保存文件信息
        FileInfo fileInfo = new FileInfo();
        fileInfo.setPickupCode(pickupCode);
        fileInfo.setFileName(task.getFileName());
        fileInfo.setFileSize(task.getFileSize());
        // 確保MD5值不超過32個字符
        fileInfo.setFileMd5(normalizeMd5(task.getFileMd5()));
        fileInfo.setFilePath(finalFilePath.toString());
        fileInfo.setUploadTime(LocalDateTime.now());
        fileInfo.setDeleted(false);
        fileInfoRepository.save(fileInfo);
        
        // 標記任務為已完成
        task.setCompleted(true);
        uploadTaskRepository.save(task);
        
        // 刪除臨時目錄
        deleteDirectory(new File(task.getTempDir()));
        
        Map<String, Object> result = new HashMap<>();
        result.put("pickupCode", pickupCode);
        result.put("fileName", task.getFileName());
        result.put("fileSize", task.getFileSize());
        
        log.info("完成上傳: uploadId={}, pickupCode={}, fileName={}", 
                uploadId, pickupCode, task.getFileName());
        
        return result;
    }
    
    /**
     * 根據取件碼獲取文件信息（帶安全檢查）
     * @param pickupCode 取件碼
     * @param ipAddress IP地址
     * @param userAgent 用戶代理
     * @return 文件信息Map
     */
    public Map<String, Object> getFileInfo(String pickupCode, String ipAddress, String userAgent) {
        // 檢查IP是否被鎖定
        if (securityService.isIpLocked(ipAddress)) {
            securityService.recordAccessAttempt(ipAddress, pickupCode, false, userAgent);
            throw new RuntimeException("訪問過於頻繁，請稍後再試");
        }
        
        // 檢查嘗試次數限制
        if (securityService.isAttemptLimitExceeded(ipAddress)) {
            securityService.recordAccessAttempt(ipAddress, pickupCode, false, userAgent);
            throw new RuntimeException("嘗試次數過多，請稍後再試");
        }
        
        // 查詢文件信息
        FileInfo fileInfo = fileInfoRepository.findByPickupCodeAndDeletedFalse(pickupCode)
                .orElse(null);
        
        boolean success = fileInfo != null;
        
        // 記錄訪問嘗試
        securityService.recordAccessAttempt(ipAddress, pickupCode, success, userAgent);
        
        if (!success) {
            throw new RuntimeException("取件碼不存在或文件已刪除");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("pickupCode", fileInfo.getPickupCode());
        result.put("fileName", fileInfo.getFileName());
        result.put("fileSize", fileInfo.getFileSize());
        result.put("uploadTime", fileInfo.getUploadTime().toString());
        
        return result;
    }
    
    /**
     * 根據取件碼獲取文件信息（兼容舊接口）
     * @param pickupCode 取件碼
     * @return 文件信息Map
     */
    public Map<String, Object> getFileInfo(String pickupCode) {
        return getFileInfo(pickupCode, "unknown", null);
    }
    
    /**
     * 下載文件（帶安全檢查）
     * @param pickupCode 取件碼
     * @param ipAddress IP地址
     * @param userAgent 用戶代理
     * @return 文件資源
     */
    public Resource downloadFile(String pickupCode, String ipAddress, String userAgent) {
        // 先驗證取件碼（會記錄訪問）
        getFileInfo(pickupCode, ipAddress, userAgent);
        
        FileInfo fileInfo = fileInfoRepository.findByPickupCodeAndDeletedFalse(pickupCode)
                .orElseThrow(() -> new RuntimeException("取件碼不存在或文件已刪除"));
        
        File file = new File(fileInfo.getFilePath());
        if (!file.exists()) {
            throw new RuntimeException("文件不存在");
        }
        
        return new FileSystemResource(file);
    }
    
    /**
     * 下載文件（兼容舊接口）
     * @param pickupCode 取件碼
     * @return 文件資源
     */
    public Resource downloadFile(String pickupCode) {
        return downloadFile(pickupCode, "unknown", null);
    }
    
    /**
     * 生成6位取件碼（字母和數字）
     * @return 6位取件碼
     */
    private String generatePickupCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }
    
    /**
     * 規範化MD5值，確保不超過32個字符
     * 如果為空或null，返回null
     * 如果超過32個字符，截取前32個字符
     * 只保留十六進制字符（0-9, a-f, A-F）
     * @param md5 原始MD5值
     * @return 規範化後的MD5值
     */
    private String normalizeMd5(String md5) {
        if (md5 == null || md5.trim().isEmpty()) {
            return null;
        }
        
        // 只保留十六進制字符
        String hexOnly = md5.replaceAll("[^0-9a-fA-F]", "");
        
        // 如果為空，返回null
        if (hexOnly.isEmpty()) {
            return null;
        }
        
        // 轉換為小寫並截取前32個字符
        String normalized = hexOnly.toLowerCase();
        if (normalized.length() > 32) {
            normalized = normalized.substring(0, 32);
        }
        
        return normalized;
    }
    
    /**
     * 遞歸刪除目錄
     * @param directory 要刪除的目錄
     */
    private void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
    }
}
