package com.mygym.app.service;

import com.mygym.app.model.Keypoints;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SquatAnalysisServiceTest {

    @Mock
    private PoseEstimationService poseEstimationService;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private SquatAnalysisService squatAnalysisService;

    @Test
    @DisplayName("Should delegate secure upload path generation to StorageService")
    void testGetSecureUploadPath_Success() {
        Map<String, String> mockPathMap = Map.of("uploadUrl", "http://storage/upload", "fileKey", "key.mp4");
        when(storageService.generateUploadUrl("test.mp4")).thenReturn(mockPathMap);

        Map<String, String> result = squatAnalysisService.getSecureUploadPath("test.mp4");

        assertNotNull(result);
        assertEquals("key.mp4", result.get("fileKey"));
        verify(storageService, times(1)).generateUploadUrl("test.mp4");
    }

    @Test
    @DisplayName("Should process and cleanup squat video file successfully")
    void testProcessAndCleanupSquatVideo_Success(@TempDir File tempDir) throws Exception {
        File dummyFile = new File(tempDir, "temp_video.mp4");
        try (FileOutputStream fos = new FileOutputStream(dummyFile)) {
            fos.write(new byte[]{1, 2, 3, 4});
        }

        // Using lenient() prevents UnnecessaryStubbingException if FFmpeg fails early
        lenient().when(poseEstimationService.predictKeypoints(any())).thenReturn(
                new Keypoints(50.0, 50.0, 50.0, 100.0, 50.0, 150.0, 50.0, 20.0)
        );
        when(storageService.downloadFileFromStorage("key.mp4")).thenReturn(dummyFile);

        assertThrows(Exception.class, () -> squatAnalysisService.processAndCleanupSquatVideo("key.mp4"));

        verify(storageService, times(1)).downloadFileFromStorage("key.mp4");
    }

    @Test
    @DisplayName("Should process MultipartFile analyzeSquatVideo stream")
    void testAnalyzeSquatVideo_MultipartFile_Success() {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", "squat.mp4", "video/mp4", new byte[]{0, 1, 2, 3}
        );

        assertThrows(Exception.class, () -> squatAnalysisService.analyzeSquatVideo(mockFile));
    }
}