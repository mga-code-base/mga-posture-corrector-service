package com.mygym.app.controller;

import com.mygym.app.model.SquatAnalysisResult;
import com.mygym.app.service.SquatAnalysisService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SquatControllerTest {

    @Mock
    private SquatAnalysisService squatAnalysisService;

    @InjectMocks
    private SquatController squatController;

    @Test
    @DisplayName("Should return upload URL map successfully")
    void testGetUploadUrl_Success() throws Exception {
        Map<String, String> mockUrlMap = Map.of(
                "uploadUrl", "http://s3.storage/upload/video.mp4",
                "fileKey", "uuid_video.mp4"
        );

        when(squatAnalysisService.getSecureUploadPath("video.mp4")).thenReturn(mockUrlMap);

        ResponseEntity<Map<String, String>> response = squatController.getUploadUrl("video.mp4");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("uuid_video.mp4", response.getBody().get("fileKey"));
    }

    @Test
    @DisplayName("Should process squat analysis payload successfully")
    void testVerifySquat_Success() throws Exception {
        SquatAnalysisResult mockResult = new SquatAnalysisResult(
                "SQUAT", 5, 85.0, 25.0, false, "Great depth!", List.of(85.0), "/videos/out.mp4"
        );

        when(squatAnalysisService.processAndCleanupSquatVideo("uuid_video.mp4")).thenReturn(mockResult);

        ResponseEntity<Map<String, Object>> response = squatController.verifySquat(Map.of("fileKey", "uuid_video.mp4"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(true, response.getBody().get("success"));
        assertEquals(5, response.getBody().get("repsCounted"));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when fileKey payload is missing")
    void testVerifySquat_MissingFileKey_ReturnsBadRequest() {
        ResponseEntity<Map<String, Object>> response = squatController.verifySquat(Map.of());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(false, response.getBody().get("success"));
    }
}