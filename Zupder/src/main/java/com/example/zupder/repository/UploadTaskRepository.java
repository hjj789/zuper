package com.example.zupder.repository;

import com.example.zupder.entity.UploadTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 上傳任務Repository
 */
@Repository
public interface UploadTaskRepository extends JpaRepository<UploadTask, Long> {
    
    /**
     * 根據上傳ID查詢任務
     */
    Optional<UploadTask> findByUploadId(String uploadId);
}



