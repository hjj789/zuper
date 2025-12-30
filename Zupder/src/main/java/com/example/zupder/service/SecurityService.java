package com.example.zupder.service;

import com.example.zupder.entity.AccessAttempt;
import com.example.zupder.repository.AccessAttemptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 安全服務類
 * 處理驗證碼、IP限制、訪問控制等安全相關功能
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityService {
    
    private final AccessAttemptRepository accessAttemptRepository;
    
    @Value("${security.max-failed-attempts:5}")
    private Integer maxFailedAttempts; // 最大失敗嘗試次數
    
    @Value("${security.lockout-duration-minutes:15}")
    private Integer lockoutDurationMinutes; // 鎖定時長（分鐘）
    
    @Value("${security.attempt-window-minutes:10}")
    private Integer attemptWindowMinutes; // 嘗試時間窗口（分鐘）
    
    // 內存中存儲驗證碼（生產環境建議使用Redis）
    private final Map<String, String> captchaStore = new ConcurrentHashMap<>();
    private final Map<String, Long> captchaExpireTime = new ConcurrentHashMap<>();
    
    /**
     * 生成驗證碼
     * @return 包含驗證碼ID和圖片的Base64字符串
     */
    public Map<String, Object> generateCaptcha() {
        String captchaId = UUID.randomUUID().toString();
        String captchaCode = generateRandomCode(4); // 4位數字驗證碼
        
        // 存儲驗證碼（5分鐘過期）
        captchaStore.put(captchaId, captchaCode);
        captchaExpireTime.put(captchaId, System.currentTimeMillis() + 5 * 60 * 1000);
        
        Map<String, Object> result = new HashMap<>();
        result.put("captchaId", captchaId);
        result.put("captchaCode", captchaCode); // 前端顯示用
        result.put("expiresIn", 300); // 5分鐘
        
        log.debug("生成驗證碼: captchaId={}, code={}", captchaId, captchaCode);
        
        return result;
    }
    
    /**
     * 驗證驗證碼
     * @param captchaId 驗證碼ID
     * @param captchaCode 用戶輸入的驗證碼
     * @return 是否驗證通過
     */
    public boolean verifyCaptcha(String captchaId, String captchaCode) {
        if (captchaId == null || captchaCode == null) {
            return false;
        }
        
        // 檢查是否過期
        Long expireTime = captchaExpireTime.get(captchaId);
        if (expireTime == null || System.currentTimeMillis() > expireTime) {
            captchaStore.remove(captchaId);
            captchaExpireTime.remove(captchaId);
            return false;
        }
        
        // 驗證碼不區分大小寫
        String storedCode = captchaStore.get(captchaId);
        boolean valid = storedCode != null && storedCode.equalsIgnoreCase(captchaCode.trim());
        
        // 驗證後刪除（一次性使用）
        if (valid) {
            captchaStore.remove(captchaId);
            captchaExpireTime.remove(captchaId);
        }
        
        return valid;
    }
    
    /**
     * 檢查IP是否被鎖定
     * @param ipAddress IP地址
     * @return 是否被鎖定
     */
    public boolean isIpLocked(String ipAddress) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(lockoutDurationMinutes);
        Long failedCount = accessAttemptRepository.countFailedAttemptsByIpSince(ipAddress, since);
        
        return failedCount != null && failedCount >= maxFailedAttempts;
    }
    
    /**
     * 檢查IP在時間窗口內的嘗試次數
     * @param ipAddress IP地址
     * @return 是否超過限制
     */
    public boolean isAttemptLimitExceeded(String ipAddress) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(attemptWindowMinutes);
        Long attemptCount = accessAttemptRepository.countAttemptsByIpSince(ipAddress, since);
        
        // 每10分鐘最多嘗試20次
        return attemptCount != null && attemptCount >= 20;
    }
    
    /**
     * 記錄訪問嘗試
     * @param ipAddress IP地址
     * @param pickupCode 取件碼
     * @param success 是否成功
     * @param userAgent 用戶代理
     */
    @Transactional
    public void recordAccessAttempt(String ipAddress, String pickupCode, boolean success, String userAgent) {
        AccessAttempt attempt = new AccessAttempt();
        attempt.setIpAddress(ipAddress);
        attempt.setPickupCode(pickupCode);
        attempt.setSuccess(success);
        attempt.setAttemptTime(LocalDateTime.now());
        attempt.setUserAgent(userAgent != null && userAgent.length() > 500 ? userAgent.substring(0, 500) : userAgent);
        
        accessAttemptRepository.save(attempt);
        
        log.info("記錄訪問嘗試: ip={}, pickupCode={}, success={}", ipAddress, pickupCode, success);
    }
    
    /**
     * 獲取IP地址（從請求中提取）
     * 此方法已移至Controller中，這裡保留用於未來擴展
     * @param request 請求對象
     * @return IP地址
     */
    @Deprecated
    public String getClientIpAddress(Object request) {
        // 此方法已移至Controller中實現
        return "unknown";
    }
    
    /**
     * 生成隨機驗證碼
     * @param length 長度
     * @return 驗證碼字符串
     */
    private String generateRandomCode(int length) {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(random.nextInt(10)); // 0-9數字
        }
        return code.toString();
    }
    
    /**
     * 清理過期的驗證碼
     */
    public void cleanExpiredCaptchas() {
        long now = System.currentTimeMillis();
        captchaExpireTime.entrySet().removeIf(entry -> {
            if (entry.getValue() < now) {
                captchaStore.remove(entry.getKey());
                return true;
            }
            return false;
        });
    }
}

