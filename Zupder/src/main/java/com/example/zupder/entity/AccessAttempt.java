package com.example.zupder.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 訪問嘗試記錄實體類
 * 用於記錄取件碼驗證嘗試，防止暴力破解
 */
@Entity
@Table(name = "access_attempt")
public class AccessAttempt {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * IP地址
     */
    @Column(nullable = false, length = 45)
    private String ipAddress;
    
    /**
     * 嘗試的取件碼
     */
    @Column(length = 6)
    private String pickupCode;
    
    /**
     * 嘗試時間
     */
    @Column(nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime attemptTime;
    
    /**
     * 是否成功
     */
    @Column(nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean success = false;
    
    /**
     * 用戶代理（瀏覽器信息）
     */
    @Column(length = 500)
    private String userAgent;
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getPickupCode() {
        return pickupCode;
    }
    
    public void setPickupCode(String pickupCode) {
        this.pickupCode = pickupCode;
    }
    
    public LocalDateTime getAttemptTime() {
        return attemptTime;
    }
    
    public void setAttemptTime(LocalDateTime attemptTime) {
        this.attemptTime = attemptTime;
    }
    
    public Boolean getSuccess() {
        return success;
    }
    
    public void setSuccess(Boolean success) {
        this.success = success;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}


