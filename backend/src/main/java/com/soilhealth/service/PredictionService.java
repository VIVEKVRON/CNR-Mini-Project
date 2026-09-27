package com.soilhealth.service;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.nio.FloatBuffer;
import java.util.Collections;

@Service
public class PredictionService {

    private OrtEnvironment env;
    private OrtSession session;

    @PostConstruct
    public void init() throws Exception {
        env = OrtEnvironment.getEnvironment();
        // The soil_model.onnx should be copied to the root or resources
        String modelPath = "soil_model.onnx"; 
        try {
            session = env.createSession(modelPath, new OrtSession.SessionOptions());
            System.out.println("ONNX model loaded successfully.");
        } catch (Exception e) {
            System.err.println("Warning: ONNX model not found. " + e.getMessage());
        }
    }

    public int predict(float ph, float moisture, float n, float p, float k) throws Exception {
        if (session == null) {
            // Fallback mock logic if model is not present during startup
            int points = 0;
            if (ph >= 6 && ph <= 7.5) points++;
            if (moisture >= 40 && moisture <= 60) points++;
            if (n > 30) points++;
            if (p > 20) points++;
            if (k > 20) points++;
            return points >= 4 ? 2 : (points >= 2 ? 1 : 0);
        }
        
        float[] inputs = {ph, moisture, n, p, k};
        FloatBuffer buffer = FloatBuffer.wrap(inputs);
        long[] shape = {1, 5};
        
        OnnxTensor tensor = OnnxTensor.createTensor(env, buffer, shape);
        OrtSession.Result result = session.run(Collections.singletonMap("float_input", tensor));
        
        // ML model returns a long[] of predicted class
        long[] labels = (long[]) result.get(0).getValue();
        return (int) labels[0];
    }
}
