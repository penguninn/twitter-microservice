package com.david.media_service.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.david.media_service.exception.AzureStorageServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class AzureStorageService {

    private final BlobServiceClient blobServiceClient;

    public String uploadFile(byte[] fileData, String blobFilename, String contentType, String containerName) {
        log.info("AzureStorageService::uploadFile execution started");
        if (fileData == null || fileData.length == 0) {
            log.error("AzureStorageService::uploadFile cannot upload empty file data");
            throw new IllegalArgumentException("Cannot upload empty file data");
        }

        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
        containerClient.createIfNotExists();
        BlobClient blobClient = containerClient.getBlobClient(blobFilename);

        try (InputStream inputStream = new ByteArrayInputStream(fileData)) {
            blobClient.upload(inputStream, fileData.length, true);
            BlobHttpHeaders headers = new BlobHttpHeaders().setContentType(contentType);
            blobClient.setHttpHeaders(headers);
        } catch (IOException e) {
            log.error("AzureStorageService::uploadFile - IOException");
            throw new AzureStorageServiceException("IO error while preparing to upload file");
        } catch (RuntimeException e) {
            log.error("AzureStorageService::uploadFile - RuntimeException");
            throw new AzureStorageServiceException("Failed to upload file to Azure");
        }
        log.info("AzureStorageService::uploadFile execution ended");
        return blobClient.getBlobUrl();
    }
}