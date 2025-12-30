package com.example.zupder.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 文件信息實體類
 */
@Entity
@Table(name = "file_info")
public class FileInfo {

    @Override
    public String toString() {
        return "FileInfo{" +
                "id=" + id +
                ", pickupCode='" + pickupCode + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileSize=" + fileSize +
                ", fileMd5='" + fileMd5 + '\'' +
                ", filePath='" + filePath + '\'' +
                ", uploadTime=" + uploadTime +
                ", deleted=" + deleted +
                '}';
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 取件碼（6位）
     */
    @Column(unique = true, nullable = false, length = 6)
    private String pickupCode;
    
    /**
     * 文件名稱
     */
    @Column(nullable = false, length = 500)
    private String fileName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPickupCode() {
        return pickupCode;
    }

    public void setPickupCode(String pickupCode) {
        this.pickupCode = pickupCode;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileMd5() {
        return fileMd5;
    }

    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public LocalDateTime getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    /**
     * 文件大小（字節）
     */
    @Column(nullable = false)
    private Long fileSize;
    
    /**
     * 文件MD5值
     */
    @Column(length = 32)
    private String fileMd5;
    
    /**
     * 文件存儲路徑
     */
    @Column(nullable = false, length = 1000)
    private String filePath;
    
    /**
     * 上傳時間
     */
    @Column(nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime uploadTime;
    
    /**
     * 是否已刪除
     */
    @Column(nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean deleted = false;
}

