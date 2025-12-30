package com.example.zupder.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 上傳任務實體類
 */
@Entity
@Table(name = "upload_task")
public class UploadTask {

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
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

    public Integer getTotalChunks() {
        return totalChunks;
    }

    public void setTotalChunks(Integer totalChunks) {
        this.totalChunks = totalChunks;
    }

    public Integer getUploadedChunks() {
        return uploadedChunks;
    }

    public void setUploadedChunks(Integer uploadedChunks) {
        this.uploadedChunks = uploadedChunks;
    }

    public String getTempDir() {
        return tempDir;
    }

    public void setTempDir(String tempDir) {
        this.tempDir = tempDir;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    @Override
    public String toString() {
        return "UploadTask{" +
                "id=" + id +
                ", uploadId='" + uploadId + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileSize=" + fileSize +
                ", fileMd5='" + fileMd5 + '\'' +
                ", totalChunks=" + totalChunks +
                ", uploadedChunks=" + uploadedChunks +
                ", tempDir='" + tempDir + '\'' +
                ", createTime=" + createTime +
                ", completed=" + completed +
                '}';
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 上傳任務ID（UUID）
     */
    @Column(unique = true, nullable = false, length = 36)
    private String uploadId;
    
    /**
     * 文件名稱
     */
    @Column(nullable = false, length = 500)
    private String fileName;
    
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
     * 分片總數
     */
    @Column(nullable = false)
    private Integer totalChunks;
    
    /**
     * 已上傳分片數
     */
    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer uploadedChunks = 0;
    
    /**
     * 臨時文件存儲目錄
     */
    @Column(nullable = false, length = 1000)
    private String tempDir;
    
    /**
     * 創建時間
     */
    @Column(nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime createTime;
    
    /**
     * 是否已完成
     */
    @Column(nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean completed = false;
}

