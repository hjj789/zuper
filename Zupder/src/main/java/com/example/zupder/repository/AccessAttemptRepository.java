package com.example.zupder.repository;

import com.example.zupder.entity.AccessAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * 訪問嘗試記錄Repository
 */
@Repository
public interface AccessAttemptRepository extends JpaRepository<AccessAttempt, Long> {
    
    /**
     * 統計指定IP在指定時間內的失敗嘗試次數
     */
    @Query("SELECT COUNT(a) FROM AccessAttempt a WHERE a.ipAddress = :ipAddress AND a.success = false AND a.attemptTime >= :since")
    Long countFailedAttemptsByIpSince(@Param("ipAddress") String ipAddress, @Param("since") LocalDateTime since);
    
    /**
     * 統計指定IP在指定時間內的總嘗試次數
     */
    @Query("SELECT COUNT(a) FROM AccessAttempt a WHERE a.ipAddress = :ipAddress AND a.attemptTime >= :since")
    Long countAttemptsByIpSince(@Param("ipAddress") String ipAddress, @Param("since") LocalDateTime since);
}


