package com.soilhealth.controller;

import com.soilhealth.service.PredictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/soil")
@CrossOrigin(origins = "*") // Allow frontend access
public class SoilController {

    @Autowired
    private PredictionService predictionService;

    @PostMapping("/predict")
    public ResponseEntity<?> predictSoilHealth(@RequestBody SoilDataRequest request) {
        try {
            // Predict using ML model
            int qualityScore = predictionService.predict(request.getPh(), request.getMoisture(), request.getN(), request.getP(), request.getK());
            
            String status = "Poor";
            if (qualityScore == 2) status = "Good";
            else if (qualityScore == 1) status = "Average";
            
            // Mock Stitch DB logging
            System.out.println("[Stitch Data Sync] Logging historical prediction to MongoDB Realm (Stitch)...");
            
            return ResponseEntity.ok(Map.of(
                "predictionScore", qualityScore,
                "status", status,
                "message", "Prediction successful"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}

class SoilDataRequest {
    private float ph;
    private float moisture;
    private float n;
    private float p;
    private float k;

    // Getters and Setters
    public float getPh() { return ph; }
    public void setPh(float ph) { this.ph = ph; }
    public float getMoisture() { return moisture; }
    public void setMoisture(float moisture) { this.moisture = moisture; }
    public float getN() { return n; }
    public void setN(float n) { this.n = n; }
    public float getP() { return p; }
    public void setP(float p) { this.p = p; }
    public float getK() { return k; }
    public void setK(float k) { this.k = k; }
}
