package com.example.the_labot_backend.files.infrastructure;

import com.example.the_labot_backend.files.domain.FileStorage;
import com.example.the_labot_backend.files.domain.StoredFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@Profile("local")
public class LocalFileStorage implements FileStorage {

    private final Path rootLocation;

    // @Value로 경로 주입, 하드코딩 x

    public LocalFileStorage(
            @Value("${file.upload-dir}") String uploadDir
    ) {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        init();
    }

    /**
     * 업로드 루트 디렉토리 초기화
     */
    private void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new IllegalStateException("파일 업로드 디렉토리 생성 실패", e);
        }
    }

    @Override
    public List<StoredFile> upload(List<MultipartFile> files, String directory) {
        List<StoredFile> results = new ArrayList<>();

        if (files == null || files.isEmpty()) {
            return results;
        }

        // resolve 메소드
        // 경로를 결합해주는 메소드
        Path targetDir = rootLocation.resolve(directory).normalize();

        try {
            Files.createDirectories(targetDir);
        } catch (IOException e) {
            throw new RuntimeException("디렉토리 생성 실패: " + directory, e);
        }

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;

            String originalFileName = file.getOriginalFilename();
            String uuid = UUID.randomUUID().toString();
            String storedFileName = uuid + "_" + originalFileName;

            Path targetPath = targetDir.resolve(storedFileName);

            try {
                Files.copy(
                        file.getInputStream(),
                        targetPath,
                        StandardCopyOption.REPLACE_EXISTING
                );
            } catch (IOException e) {
                throw new RuntimeException("파일 저장 실패: " + originalFileName, e);
            }

            StoredFile storedFile = new StoredFile(
                    originalFileName,
                    directory + "/" + storedFileName,
                    targetPath.toUri().toString(),
                    file.getContentType(),
                    file.getSize()
            );

            results.add(storedFile);
        }

        return results;
    }

    @Override
    public void delete(String storedFileName) {
        try {
            Path filePath = rootLocation.resolve(storedFileName).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("파일 삭제 실패: " + storedFileName, e);
        }
    }
}

