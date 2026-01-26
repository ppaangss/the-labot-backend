package com.example.the_labot_backend.files.service;

import com.example.the_labot_backend.files.domain.File;
import com.example.the_labot_backend.files.domain.FileStorage;
import com.example.the_labot_backend.files.domain.StoredFile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileStorage fileStorage;
    private final FileStorage.FileRepository fileRepository;

    /**
     * 기존에 쓰던 메서드 (유지)
     */
    @Transactional
    public void saveFiles(
            List<MultipartFile> files,
            String targetType,
            Long targetId
    ) {
        if (files == null || files.isEmpty()) {
            return;
        }

        // 1️. 물리적 파일 저장 (로컬 / S3)
        List<StoredFile> storedFiles =
                fileStorage.upload(files, targetType.toLowerCase());

        // 2️. 업로드 결과 → Entity 변환
        List<File> entities = storedFiles.stream()
                .map(sf -> toEntity(sf, targetType, targetId))
                .toList();

        // 3️. DB 저장 (기존과 동일)
        fileRepository.saveAll(entities);
    }

    /**
     * 기존 메서드 유지
     */
    @Transactional
    public void deleteFilesByTarget(String targetType, Long targetId) {
        List<File> files =
                fileRepository.findByTargetTypeAndTargetId(targetType, targetId);

        for (File file : files) {
            fileStorage.delete(file.getStoredFileName());
            fileRepository.delete(file);
        }
    }

    /**
     * 기존 조회 메서드 유지
     */
    @Transactional(readOnly = true)
    public List<File> getFilesByTarget(String targetType, Long targetId) {
        return fileRepository.findByTargetTypeAndTargetId(targetType, targetId);
    }

    /* ================= 내부 전용 ================= */

    private File toEntity(
            StoredFile storedFile,
            String targetType,
            Long targetId
    ) {
        return File.builder()
                .originalFileName(storedFile.originalFileName())
                .storedFileName(storedFile.storedFileName())
                .fileUrl(storedFile.fileUrl())
                .contentType(storedFile.contentType())
                .size(storedFile.size())
                .targetType(targetType)
                .targetId(targetId)
                .build();
    }
}
