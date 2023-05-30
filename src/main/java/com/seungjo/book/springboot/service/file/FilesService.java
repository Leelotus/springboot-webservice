package com.seungjo.book.springboot.service.file;

import com.seungjo.book.springboot.domain.file.FilesRepository;
import com.seungjo.book.springboot.domain.file.Files;
import com.seungjo.book.springboot.domain.file.UploadFile;
import com.seungjo.book.springboot.web.dto.FilesDto;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FilesService {
    @Value("${file.dir}")
    private String fileDir;

    private final FilesRepository filesRepository;

    public String getFullPath(String filename) {
        return fileDir + filename;
    }

    @Transactional
    public Long saveDb(FilesDto requestDto) {
        return filesRepository.save(requestDto.toEntity()).getId();
    }

    public List<UploadFile> storeFiles(List<MultipartFile> multipartFiles, Long postId) throws IOException {
        List<UploadFile> storeFileResult = new ArrayList<>();
        for (MultipartFile multipartFile : multipartFiles) {
            if (!multipartFile.isEmpty()) {
                storeFileResult.add(storeFile(multipartFile, postId));
            }
        }
        return storeFileResult;
    }

    public UploadFile storeFile(MultipartFile multipartFile, Long postId) throws IOException {
        if (multipartFile.isEmpty()) {
            return null;
        }
        String originalFilename = multipartFile.getOriginalFilename();
        String storeFileName = createStoreFileName(originalFilename);
        multipartFile.transferTo(new File(getFullPath(storeFileName)));

        // FileDto 객체 생성
        FilesDto filesDto = FilesDto.builder()
                .originalFileName(originalFilename)
                .savedFileName(storeFileName)
                .size(multipartFile.getSize())
                .postId(postId)
                .build();

        // File Insert
        Long fileId = saveDb(filesDto);


        return new UploadFile(originalFilename, storeFileName);
    }

    private String createStoreFileName(String originalFilename) {
        String ext = extractExt(originalFilename);
        String uuid = UUID.randomUUID().toString();
        return uuid + "." + ext;
    }

    private String extractExt(String originalFilename) {
        int pos = originalFilename.lastIndexOf(".");
        return originalFilename.substring(pos + 1);
    }
    public List<Files> findByPostId(Long postId) {
        return filesRepository.findByPostId(postId);
    }

}
