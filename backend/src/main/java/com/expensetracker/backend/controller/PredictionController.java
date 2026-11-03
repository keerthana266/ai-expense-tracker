package com.expensetracker.backend.controller;

import com.expensetracker.backend.dto.PredictionResponse;
import com.expensetracker.backend.service.PredictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/prediction")
@CrossOrigin("*")
public class PredictionController {

    @Autowired
    private PredictionService predictionService;

    @GetMapping
    public PredictionResponse predict() {
        return predictionService.predict();
    }
}