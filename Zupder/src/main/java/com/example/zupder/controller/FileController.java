package com.example.zupder.controller;

import com.example.zupder.service.FileService;
import com.example.zupder.service.SecurityService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件控制器
 */
@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class FileController {
    
    private final FileService fileService;
    private final SecurityService securityService;
    
    /**
     * 獲取客戶端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 處理多個IP的情況
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip != null ? ip : "unknown";
    }
    
    /**
     * 初始化文件上傳
     */
    @PostMapping("/init")
    public ResponseEntity<Map<String, Object>> initUpload(@RequestBody Map<String, Object> request) {
        try {
            String fileName = (String) request.get("fileName");
            Long fileSize = Long.valueOf(request.get("fileSize").toString());
            String fileMd5 = (String) request.get("fileMd5");
            
            Map<String, Object> result = fileService.initUpload(fileName, fileSize, fileMd5);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("初始化上傳失敗", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * 上傳文件分片
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadChunk(
            @RequestParam("uploadId") String uploadId,
            @RequestParam("chunkIndex") Integer chunkIndex,
            @RequestParam("chunk") MultipartFile chunk,
            @RequestParam(value = "chunkMd5", required = false) String chunkMd5) {
        try {
            Map<String, Object> result = fileService.uploadChunk(uploadId, chunkIndex, chunk, chunkMd5);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("上傳分片失敗", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * 完成文件上傳
     */
    @PostMapping("/complete")
    public ResponseEntity<Map<String, Object>> completeUpload(@RequestBody Map<String, Object> request) {
        try {
            String uploadId = (String) request.get("uploadId");
            Map<String, Object> result = fileService.completeUpload(uploadId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("完成上傳失敗", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * 生成驗證碼
     */
    @GetMapping("/captcha")
    public ResponseEntity<Map<String, Object>> generateCaptcha() {
        try {
            Map<String, Object> result = securityService.generateCaptcha();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("生成驗證碼失敗", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * 根據取件碼獲取文件信息（帶安全檢查）
     */
    @PostMapping("/info/{pickupCode}")
    public ResponseEntity<Map<String, Object>> getFileInfo(
            @PathVariable String pickupCode,
            @RequestBody(required = false) Map<String, Object> request,
            HttpServletRequest httpRequest) {
        try {
            String ipAddress = getClientIpAddress(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            
            // 驗證驗證碼（如果提供）
            if (request != null && request.containsKey("captchaId") && request.containsKey("captchaCode")) {
                String captchaId = (String) request.get("captchaId");
                String captchaCode = (String) request.get("captchaCode");
                if (!securityService.verifyCaptcha(captchaId, captchaCode)) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("success", false);
                    error.put("message", "驗證碼錯誤");
                    error.put("code", "CAPTCHA_ERROR");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
                }
            }
            
            Map<String, Object> result = fileService.getFileInfo(pickupCode, ipAddress, userAgent);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("獲取文件信息失敗", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    /**
     * 下載文件（帶安全檢查）
     */
    @GetMapping("/download/{pickupCode}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String pickupCode,
            HttpServletRequest request) {
        try {
            String ipAddress = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");
            
            Resource resource = fileService.downloadFile(pickupCode, ipAddress, userAgent);
            Map<String, Object> fileInfo = fileService.getFileInfo(pickupCode, ipAddress, userAgent);
            
            String fileName = (String) fileInfo.get("fileName");
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + fileName + "\"")
                    .body(resource);
        } catch (Exception e) {
            log.error("下載文件失敗", e);
            return ResponseEntity.notFound().build();
        }
    }
}

