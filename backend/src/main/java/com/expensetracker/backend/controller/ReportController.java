package com.expensetracker.backend.controller;

import com.expensetracker.backend.dto.ReportResponse;
import com.expensetracker.backend.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/report")
@CrossOrigin("*")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping
    public ReportResponse getReport() {
        return reportService.generateReport();
    }
}