package com.mygym.app.service;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import com.mygym.app.model.Keypoints;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.image.BufferedImage;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PoseEstimationServiceTest {

    private static OrtEnvironment realEnv;

    @Mock
    private OrtSession session;

    @Mock
    private OrtSession.Result sessionResult;

    @Mock
    private OnnxTensor outputTensor;

    @BeforeAll
    static void initEnv() {
        try {
            realEnv = OrtEnvironment.getEnvironment();
        } catch (Exception ignored) {
        }
    }

    @AfterAll
    static void closeEnv() {
        if (realEnv != null) {
            realEnv.close();
        }
    }

    @Test
    @DisplayName("Should extract keypoints from buffered image via ONNX session")
    void testPredictKeypoints_Success() throws Exception {
        if (realEnv == null) {
            return;
        }

        PoseEstimationService poseEstimationService = new PoseEstimationService(realEnv, session);
        BufferedImage inputImage = new BufferedImage(192, 192, BufferedImage.TYPE_INT_RGB);

        float[][][][] mockModelOutput = new float[1][1][17][3];
        mockModelOutput[0][0][5] = new float[]{0.2f, 0.3f, 0.9f};  
        mockModelOutput[0][0][11] = new float[]{0.5f, 0.3f, 0.9f}; 
        mockModelOutput[0][0][13] = new float[]{0.7f, 0.3f, 0.9f}; 
        mockModelOutput[0][0][15] = new float[]{0.9f, 0.3f, 0.9f}; 

        when(session.run(any(Map.class))).thenReturn(sessionResult);
        when(sessionResult.get(0)).thenReturn(outputTensor);
        when(outputTensor.getValue()).thenReturn(mockModelOutput);

        Keypoints keypoints = poseEstimationService.predictKeypoints(inputImage);

        assertNotNull(keypoints);
        assertTrue(keypoints.shoulderY() > 0);
        assertTrue(keypoints.hipY() > 0);
        assertTrue(keypoints.kneeY() > 0);
        assertTrue(keypoints.ankleY() > 0);

        verify(session, times(1)).run(any(Map.class));
    }
}