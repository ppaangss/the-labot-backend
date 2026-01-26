package com.example.the_labot_backend.files.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileStorage {

    List<StoredFile> upload(
            List<MultipartFile> files,
            String directory
    );
    void delete(String storedFileName);

    interface FileRepository extends JpaRepository<File, Long> {
        List<File> findByTargetTypeAndTargetId(String targetType, Long targetId);
    }
}