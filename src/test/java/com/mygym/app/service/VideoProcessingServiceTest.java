package com.mygym.app.service;

import com.mygym.app.model.Keypoints;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.image.BufferedImage;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoProcessingServiceTest {

    private final VideoProcessingService videoProcessingService = new VideoProcessingService();

    @Mock
    private PoseEstimationService poseService;

    @Test
    @DisplayName("Should verify static videos directory initialization")
    void testServiceInitialization_Success() {
        assertNotNull(videoProcessingService);
        File staticVideosDir = new File(System.getProperty("user.dir") + "/static/videos/");
        assertTrue(staticVideosDir.exists());
    }

    @Test
    @DisplayName("Should handle missing or corrupt video files gracefully during extraction")
    void testExtractKeyframes_InvalidFile_ThrowsException(@TempDir File tempDir) {
        File dummyFile = new File(tempDir, "invalid.mp4");

        assertThrows(Exception.class, () -> videoProcessingService.extractKeyframes(dummyFile, 3));
    }

    @Test
    @DisplayName("Should handle missing or corrupt video files gracefully during overlay generation")
    void testGenerateSkeletonOverlayVideo_InvalidFile_ThrowsException(@TempDir File tempDir) {
        File dummyFile = new File(tempDir, "invalid_overlay.mp4");

        assertThrows(Exception.class, () -> videoProcessingService.generateSkeletonOverlayVideo(dummyFile, poseService));
    }
}