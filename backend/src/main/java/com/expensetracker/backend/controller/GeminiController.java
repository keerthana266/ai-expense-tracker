package com.expensetracker.backend.controller;

import com.expensetracker.backend.service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gemini")
public class GeminiController {

    @Autowired
    private GeminiService geminiService;

    @GetMapping("/test")
    public String testGemini() {
          return geminiService.askGemini("Say Hello in one word.");

    }
}