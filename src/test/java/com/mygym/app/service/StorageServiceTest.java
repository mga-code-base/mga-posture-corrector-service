package com.mygym.app.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StorageServiceTest {

    private StorageService storageService;

    @BeforeEach
    void setUp() {
        storageService = new StorageService();
        ReflectionTestUtils.setField(storageService, "endpoint", "http://localhost:9000");
        ReflectionTestUtils.setField(storageService, "region", "us-east-1");
        ReflectionTestUtils.setField(storageService, "accessKey", "testAccessKey");
        ReflectionTestUtils.setField(storageService, "secretKey", "testSecretKey256BitLengthRequirement1234");
        ReflectionTestUtils.setField(storageService, "bucketName", "gym-videos");
    }

    @Test
    @DisplayName("Should generate presigned upload URL and unique file key")
    void testGenerateUploadUrl_Success() {
        Map<String, String> result = storageService.generateUploadUrl("squat_clip.mp4");

        assertNotNull(result);
        assertTrue(result.containsKey("uploadUrl"));
        assertTrue(result.containsKey("fileKey"));
        assertTrue(result.get("fileKey").endsWith("_squat_clip.mp4"));
    }

    @Test
    @DisplayName("Should complete deleteFileFromStorage gracefully without throwing exception for blank or null key")
    void testDeleteFileFromStorage_BlankKey_DoesNotThrow() {
        assertDoesNotThrow(() -> storageService.deleteFileFromStorage(""));
        assertDoesNotThrow(() -> storageService.deleteFileFromStorage("   "));
        assertDoesNotThrow(() -> storageService.deleteFileFromStorage(null));
    }

    @Test
    @DisplayName("Should catch exception gracefully when deleting unresolvable file key")
    void testDeleteFileFromStorage_UnresolvableKey_HandledGracefully() {
        assertDoesNotThrow(() -> storageService.deleteFileFromStorage("non_existent_key.mp4"));
    }

    @Test
    @DisplayName("Should fail download attempts against unresolvable local test endpoints")
    void testDownloadFileFromStorage_Failure() {
        assertThrows(Exception.class, () -> storageService.downloadFileFromStorage("invalid_file_key.mp4"));
    }
}