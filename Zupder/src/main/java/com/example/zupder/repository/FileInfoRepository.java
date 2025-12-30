package com.example.zupder.repository;

import com.example.zupder.entity.FileInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 文件信息Repository
 */
@Repository
public interface FileInfoRepository extends JpaRepository<FileInfo, Long> {
    
    /**
     * 根據取件碼查詢文件信息
     */
    Optional<FileInfo> findByPickupCodeAndDeletedFalse(String pickupCode);
}



