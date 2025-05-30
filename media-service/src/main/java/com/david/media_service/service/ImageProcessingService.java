package com.david.media_service.service;

import com.david.media_service.exception.ImageProcessingServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageProcessingService {

    @Value("${spring.cloud.azure.storage.blob.container-name}")
    private String containerName;

    private final AzureStorageService azureStorageService;
    private final Tika tika;

    public String uploadImage(MultipartFile file) {
        log.info("ImageProcessingService::uploadImage execution started");

        if(file == null || file.isEmpty()) {
            log.error("ImageProcessingService::uploadImage original image file is null or empty");
            throw new ImageProcessingServiceException("Original image file cannot be null or empty");
        }
        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown";
        byte[] fileData;
        try (InputStream inputStream = file.getInputStream()) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            inputStream.transferTo(byteArrayOutputStream);
            fileData = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();
        } catch (IOException e) {
            log.error("ImageProcessingService::uploadImage error while get content-type");
            throw new ImageProcessingServiceException("Error while get content-type");
        }
        String contentType = tika.detect(fileData, originalFilename);
        if(contentType == null || !contentType.startsWith("image/")) {
            log.error("ImageProcessingService::uploadImage file is not a valid image type");
            throw new ImageProcessingServiceException("File is not a valid image type");
        }
        String optimizeOriginalFilename = originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
        String blobFileName = UUID.randomUUID() + "-" + optimizeOriginalFilename;
        String uploadUrl = azureStorageService.uploadFile(fileData, blobFileName, contentType, containerName);
        return uploadUrl;
    }

}
