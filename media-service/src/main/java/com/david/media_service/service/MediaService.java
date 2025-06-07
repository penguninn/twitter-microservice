package com.david.media_service.service;

import com.david.media_service.dto.response.MediaResponse;
import com.david.media_service.exception.MediaServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaService {

    @Value("${spring.cloud.azure.storage.blob.container-name}")
    private String containerName;

    private final AzureStorageService azureStorageService;
    private final Tika tika;

    public List<MediaResponse> uploadFiles(MultipartFile[] files, List<String> allowedContentTypes) {
        log.info("MediaService::uploadFiles - Execution started");

        if(files == null || files.length == 0) {
            log.error("MediaService::uploadFiles - Original files are null or empty");
            throw new MediaServiceException("Original files cannot be null or empty");
        }

        List<MediaResponse> responses = new ArrayList<>();

        for (MultipartFile file : files) {
            String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown";
            byte[] fileData;
            try (InputStream inputStream = file.getInputStream()) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                inputStream.transferTo(byteArrayOutputStream);
                fileData = byteArrayOutputStream.toByteArray();
                byteArrayOutputStream.close();
            } catch (IOException e) {
                log.error("MediaService::uploadFiles - Error while get content-type for file: {}", originalFilename, e);
                throw new MediaServiceException("Error while get content-type for file: " + originalFilename);
            }

            String contentType = tika.detect(fileData, originalFilename);
            if(allowedContentTypes != null && !allowedContentTypes.isEmpty()) {
                if(allowedContentTypes.stream().noneMatch(contentType::startsWith)) {
                    log.error("MediaService::uploadFiles - Unsupported content type: {} for file: {}", contentType, originalFilename);
                    throw new MediaServiceException("Unsupported content type: " + contentType + " for file: " + originalFilename);
                }
            }

            String optimizeOriginalFilename = originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
            String blobFileName = UUID.randomUUID() + "-" + optimizeOriginalFilename;
            String url = azureStorageService.uploadFile(fileData, blobFileName, contentType, containerName);

            responses.add(MediaResponse.builder()
                    .mediaId(blobFileName)
                    .mediaType(contentType)
                    .mediaUrl(url)
                    .build());
        }

        log.info("MediaService::uploadFiles - Processed {} files successfully", responses.size());
        return responses;
    }

}
